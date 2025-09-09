package era.app.jobs

import bufferchanel.databuffer.BufferedCloudOfSensor
import clouds.Cloud
import clouds.GpuCloud
import clouds.Hash
import era.app.gpu.Gpu
import era.app.gpu.JoclAddPointsToPlane
import era.app.gpu.JoclGetCloudOfIndex
import era.app.gpu.JoclGetFastCloudWithHash
import era.app.gpu.JoclProjectPointsIndexed
import era.app.utils.subclouders.Voxeler
import era.utils.time.Span
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import vectors.PlaneCloud
import vectors.PlanePoints
import vectors.Vector3Float
import java.util.concurrent.ThreadLocalRandom

object Plainer {

    private const val COTH_CREATE_HASH = 10
    private const val COTH_RANDOM = 10
    private const val MIN_POINT_IN_PLAINS = 40
    private const val VOXEL_SIZE = 200
    private const val MIN_DIST_RANDOM_POINT = (VOXEL_SIZE*3).toFloat()
    private const val MAX_DIST_RANDOM_POINT = (VOXEL_SIZE*10).toFloat()
    private const val COUNT_RANDOM_POINT = 3000 //изначальная выборка точек
    private const val EPS = 25f // отклонение в мм когда точка еще в плоскости
    private const val POINTS_ON_VOXEL = 4 //количество точек на площадь вокселя допустимое
    private const val MAX_DIV_INTER_POINT = 30 //число, если 1/х частей точек облака пересекается с другим то другова не будет

    val span = Span()
    val span2 = Span()
    val span3 = Span()
    val span4 = Span()

    val mutex2 = Mutex()

    data class PlainerResult(
        val cloud: Cloud,
        val planes: List<PlanePoints>
    )

    suspend fun CoroutineScope.create(
        readCloud: BufferedCloudOfSensor
    ) = async (Dispatchers.Default) {
        val readCloudSize = readCloud.size
        val readCloudWidth = readCloud.width

        span3.resetTimer()

        val gpuCloud = GpuCloud().loadCloud(Gpu.getContext(), readCloud)

        val hashFastCloud = JoclGetFastCloudWithHash.runKernel(Gpu.getDevice(), gpuCloud,
            readCloudWidth, readCloudSize, Hash.BIAS.toFloat(), VOXEL_SIZE, Hash.MASK)

        val voxelCloud = Voxeler.createVoxelsFastFromHash(hashFastCloud)

        if(voxelCloud.size < 10) {
            return@async PlainerResult(
                JoclGetCloudOfIndex.runKernel(Gpu.getDevice(),
                    gpuCloud, (0 until readCloudSize).toList().toIntArray()),
                emptyList())
        }

        val planes = findPlanesDeterministic(voxelCloud)

        val mask = BooleanArray(readCloudSize) { true }

        val recloudJobs = planes.map { plane ->
            async {
                val planeNorm = plane.getNorm()
                val res = JoclAddPointsToPlane.runKernel(Gpu.getDevice(), gpuCloud, plane.a, plane.b, plane.c, plane.d,
                    EPS * planeNorm, EPS*3 * planeNorm)
                return@async PlaneLocalData(
                    res.first, res.second,
                    plane.getUandV(), plane.createPlanePoints())
            }
        }

        val hullJobs = recloudJobs.map { job ->
            async {
                val planeData = job.await()
                val hull = convexHullOnPlane(planeData, gpuCloud)

                JoclGetCloudOfIndex.runKernel(Gpu.getDevice(),
                    planeData.plane, gpuCloud, hull)

                return@async planeData.plane
            }
        }

        val maskJobs = recloudJobs.map{ job ->
            async {
                val plane = job.await()
                mutex2.withLock {
                    for (i in plane.pointsWithSecondOrder) mask[i] = false
                }
            }
        }

        val resCloud = async {
            maskJobs.awaitAll()
            val indexes = IntArray(readCloudSize - mask.count { !it }) // сразу считаем размер
            var pos = 0
            for (n in 0 until readCloudSize) {
                if (mask[n]) indexes[pos++] = n
            }
            val resCloud = JoclGetCloudOfIndex.runKernel(Gpu.getDevice(),
                gpuCloud, indexes)

            return@async resCloud
        }.await()

        span2.resetTimer()
        val plRes = hullJobs.awaitAll()
        span2.middleCircle(100, "await")

        launch {
            gpuCloud.releaseCloud()
        }
        span3.middleCircle(100, "all do")

        return@async PlainerResult(resCloud, plRes)
    }.await()

    data class PlaneLocalData(
        val points: IntArray,
        val pointsWithSecondOrder: IntArray,
        val uAndV: Pair<Vector3Float, Vector3Float>,
        val plane: PlanePoints
    )

    private fun planesIsNotOkOfSq(plane: PlaneCloud) : Boolean {
        val p = plane.area()/(VOXEL_SIZE*VOXEL_SIZE) //сколько вокселей в площаде
        return plane.points.size * POINTS_ON_VOXEL < p
    }


    private suspend fun CoroutineScope.findPlanesDeterministic(
        cloud: Cloud
    ): List<PlaneCloud> {
        val fPlans = createFPlansRandom(cloud)

        val gPlans = mutableListOf<PlaneCloud>()
        while(fPlans.isNotEmpty()) {
            var indexTopPlan = -1
            var maxPoints = Int.MIN_VALUE

            for(i in 0 until fPlans.size) {
                val p = fPlans[i]
                if(maxPoints < p.points.size) {
                    maxPoints = p.points.size
                    indexTopPlan = i
                }
            }

            val topPlan = if(indexTopPlan >=0) fPlans[indexTopPlan] else null

            topPlan?.let { tp->
                fPlans.removeAt(indexTopPlan)
                fPlans.removeIf { fPlan ->
                    val inter = fPlan.getPointsSet().intersect(tp.getPointsSet()).size
                    inter*MAX_DIV_INTER_POINT > fPlan.points.size
                }
                gPlans.add(tp)
            }
        }

        return gPlans
    }

    val mutex = Mutex()

    private suspend fun CoroutineScope.createFPlansRandom(cloud: Cloud): MutableList<PlaneCloud> {
        val fPlans = mutableListOf<PlaneCloud>()
        val rnd = ThreadLocalRandom.current()
        val maxDistSq = MAX_DIST_RANDOM_POINT * MAX_DIST_RANDOM_POINT
        val minDistSq = MIN_DIST_RANDOM_POINT * MIN_DIST_RANDOM_POINT

        (0..COTH_RANDOM-1).map{
            async {
                repeat(COUNT_RANDOM_POINT /COTH_RANDOM) {
                    val p1 = rnd.nextInt(cloud.size)
                    var p2: Int
                    do { p2 = rnd.nextInt(cloud.size) } while (p2 == p1)
                    var p3: Int
                    do { p3 = rnd.nextInt(cloud.size) } while (p3 == p1 || p3 == p2)

                    val dist1 = cloud.distSquared(p1, p2)
                    if(dist1 > minDistSq && dist1 < maxDistSq) {
                        val dist2 = cloud.distSquared(p2, p3)
                        if(dist2 > minDistSq && dist2 < maxDistSq) {
                            val dist3 = cloud.distSquared(p3, p1)
                            if( dist3 > minDistSq && dist3 < maxDistSq) {
                                var doDo = true
                                for(q in 0 until fPlans.size) {
                                    if(fPlans[q].points.contains(p1) &&
                                        fPlans[q].points.contains(p2) &&
                                        fPlans[q].points.contains(p3)) doDo = false
                                }

                                if(doDo) {
                                    cloud.planeFromPoints(p1, p2, p3)?.let { plane ->
                                        plane.setInliersSIMD(cloud, EPS)
                                        if(plane.points.size >= MIN_POINT_IN_PLAINS && (!planesIsNotOkOfSq(plane))) {
                                            mutex.withLock{
                                                fPlans.add(plane)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }.awaitAll()

        return fPlans
    }

    suspend fun convexHullOnPlane(
        plane: PlaneLocalData, gpuCloud: GpuCloud
    ) : IntArray {
        val uAndV = plane.uAndV
        var indexes = plane.points
        val d = (0 until indexes.size step 100).toList()
        indexes = indexes.sliceArray(d)
        val points2D = JoclProjectPointsIndexed.runKernel(Gpu.getDevice(), gpuCloud, indexes, uAndV.first, uAndV.second)

        return points2D.oldHull().toIntArray()
    }
}