package era.app.jobs

import bufferchanel.databuffer.BufferedCloudOfSensor
import clouds.Cloud
import clouds.GpuCloud
import clouds.Hash
import era.app.gpu.Gpu
import era.app.gpu.JoclAddPointsToPlane
import era.app.gpu.JoclProjectPointsIndexed
import era.app.utils.subclouders.CloudOfSensorCompressor.compressHexadrupleRare
import era.app.utils.subclouders.Voxeler
import era.utils.time.Span
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import vectors.P2D
import vectors.Plane
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.abs

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
        val planes: List<Plane>
    )

    suspend fun CoroutineScope.create(
        readCloud: BufferedCloudOfSensor
    ) = async (Dispatchers.Default) {
        if(readCloud.size < 10_000) {
            return@async PlainerResult(readCloud, emptyList())
        }
        span3.resetTimer()

        val loadGpu = async {
            GpuCloud().loadCloud(Gpu.getContext(), readCloud)
        }

        span.resetTimer()
        val fastCloud = compressHexadrupleRare(readCloud, COTH_CREATE_HASH)
        if(fastCloud.size < 100) {
            return@async PlainerResult(readCloud, emptyList())
        }


        val voxelCloud = createVoxelRandomCloud(fastCloud)
        if(voxelCloud.size < 10) {
            return@async PlainerResult(readCloud, emptyList())
        }
        span.middleCircle(100, "fast")


        val planes = findPlanesDeterministic(voxelCloud)
        val gpuCloud = loadGpu.await()
        span4.resetTimer()

        val mask = BooleanArray(readCloud.size) { true }

//        planes.forEach { plane ->
//            plane.recloud(readCloud, EPS, EPS*3)
//
//        }
//
//        planes.forEach { plane ->
//            val planeNorm = plane.getNorm()
//            val res = inlaerser.runKernel(readCloud.name, plane.a, plane.b, plane.c, plane.d,
//                EPS * planeNorm, EPS*3 * planeNorm)
//            plane.points = res.first
//            plane.pointsWithSecondOrder = res.second
//            for (i in plane.pointsWithSecondOrder) mask[i] = false
//        }
//
//        planes.forEach { plane ->
//            convexHullOnPlane(plane, inlaerser)
//        }
//
//
//
//        val resCloud = readCloud.cloneOfIndex(mask)
//        val plRes = planes

        val recloudJobs = planes.map { plane ->
            async {
                plane.recloud(readCloud, EPS, EPS*3)
                val planeNorm = plane.getNorm()
                val res = JoclAddPointsToPlane.runKernel(Gpu.getDevice(), gpuCloud, plane.a, plane.b, plane.c, plane.d,
                    EPS * planeNorm, EPS*3 * planeNorm)
//                val res = inlaerser.runKernel(readCloud.name, plane.a, plane.b, plane.c, plane.d,
//                    EPS * planeNorm, EPS*3 * planeNorm)
                plane.points = res.first
                plane.pointsWithSecondOrder = res.second
                return@async plane
            }
        }

        val hullJobs = recloudJobs.map { job ->
            async {
                val plane = job.await()
                convexHullOnPlane(plane, gpuCloud)
                return@async plane
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
            val resCloud = readCloud.cloneOfIndex(mask)

            return@async resCloud
        }.await()

        val plRes = hullJobs.awaitAll()
        span4.middleCircle(100, "koro")
        span3.middleCircle(100, "all do")

        gpuCloud.releaseCloud()
        return@async PlainerResult(resCloud, plRes)
    }.await()


    private suspend fun createVoxelRandomCloud(fastCloud: Cloud) : Cloud {
        val hashFastCloud = Hash(VOXEL_SIZE, fastCloud)
        hashFastCloud.init(COTH_CREATE_HASH)
        return Voxeler.createVoxelsRandomFromHash(fastCloud, hashFastCloud, COTH_CREATE_HASH)
    }

    private fun planesIsNotOkOfSq(plane: Plane) : Boolean {
        val p = plane.area()/(VOXEL_SIZE*VOXEL_SIZE) //сколько вокселей в площаде
        return plane.points.size * POINTS_ON_VOXEL < p
    }


    private suspend fun CoroutineScope.findPlanesDeterministic(
        cloud: Cloud
    ): List<Plane> {
        val fPlans = createFPlansRandom(cloud)

        val gPlans = mutableListOf<Plane>()
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

    private suspend fun CoroutineScope.createFPlansRandom(cloud: Cloud): MutableList<Plane> {
        val fPlans = mutableListOf<Plane>()
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

    suspend fun CoroutineScope.quickHull(points: MutableList<P2D>): MutableList<Int> {
        if (points.size <= 3) return points.map { it.original }.toMutableList()

        val mutex = Mutex()
        val hull = mutableListOf<Int>()

//        span.resetTimer()
        val minXJob = async {
            points.minByOrNull { it.x }!!
        }
        val maxXJob = async {
            points.maxByOrNull { it.x }!!
        }
        val minX = minXJob.await()
        val maxX = maxXJob.await()

        hull.add(minX.original)
        hull.add(maxX.original)

        listOf(launch {
                val rightSet = points.filter { cross(minX, maxX, it) < 0 }
                buildHull(minX, maxX, rightSet, hull, mutex)
            },
            launch {
                val leftSet = points.filter { cross(minX, maxX, it) > 0 }
                buildHull(maxX, minX, leftSet, hull, mutex)
            }).joinAll()

//        span.middleCircle(100, "hull")
        return hull
    }

    private suspend fun buildHull(a: P2D, b: P2D, set: List<P2D>, hull: MutableList<Int>, mutex: Mutex) {
        if (set.isEmpty()) return
        val farthest = set.maxByOrNull { distanceFromLine(a, b, it) }!!
        mutex.withLock {
            val idx = hull.indexOf(b.original)
            hull.add(idx, farthest.original)
        }

        val leftSetAP = set.filter { cross(a, farthest, it) > 0 }
        val leftSetPB = set.filter { cross(farthest, b, it) > 0 }

        buildHull(a, farthest, leftSetAP, hull, mutex)
        buildHull(farthest, b, leftSetPB, hull, mutex)
    }

    private fun cross(a: P2D, b: P2D, c: P2D): Float =
        (b.x - a.x) * (c.y - a.y) - (b.y - a.y) * (c.x - a.x)

    private fun distanceFromLine(a: P2D, b: P2D, c: P2D): Float =
        abs((b.x - a.x) * (a.y - c.y) - (a.x - c.x) * (b.y - a.y))


    // кросс-продукт 2D
    inline fun crossOld(o: P2D, a: P2D, b: P2D): Float {
        return (a.x - o.x) * (b.y - o.y) - (a.y - o.y) * (b.x - o.x)
    }

    suspend fun CoroutineScope.oldHull(points2D: MutableList<P2D>): MutableList<Int> {
        if (points2D.size > 2) {
            points2D.sortWith { a, b ->
                if (a.x == b.x) a.y.compareTo(b.y) else a.x.compareTo(b.x)
            }
        }

        if (points2D.size <= 2) {
            return points2D.map { it.original }.toMutableList()
        }

        val j1 = async(Dispatchers.Default) {
            val lower = ArrayList<P2D>()
            for (p in points2D) {
                while (lower.size >= 2 && crossOld(lower[lower.size - 2], lower.last(), p) <= 0f) {
                    lower.removeAt(lower.lastIndex)
                }
                lower.add(p)
            }
            return@async lower
        }

        val j2 = async(Dispatchers.Default) {
            val upper = ArrayList<P2D>()
            for (i in points2D.size - 1 downTo 0) {
                while (upper.size >= 2 && crossOld(upper[upper.size - 2], upper[upper.size - 1], points2D[i]) <= 0f) {
                    upper.removeAt(upper.size - 1)
                }
                upper.add(points2D[i])
            }
            return@async upper
        }

        val lower = j1.await()
        val upper = j2.await()

        val hull = ArrayList<Int>(lower.size + upper.size - 2)
        for (i in 0 until lower.size - 1) hull.add(lower[i].original)
        for (i in 0 until upper.size - 1) hull.add(upper[i].original)
        return hull
    }

    suspend fun CoroutineScope.convexHullOnPlane(
        plane: Plane, gpuCloud: GpuCloud
    ) {
        val uAndV = plane.getUandV()
//        val points2D = plane.get2DPoints()
        val points2D = JoclProjectPointsIndexed.runKernel(Gpu.getDevice(), gpuCloud, plane.points, uAndV.first, uAndV.second)

        plane.points = quickHull(points2D).toIntArray()
//        plane.points = oldHull(points2D).toIntArray()
    }
}