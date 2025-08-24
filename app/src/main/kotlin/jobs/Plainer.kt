package era.app.jobs

import bufferchanel.databuffer.BufferedChannelForView
import clouds.Cloud
import clouds.Hash
import era.app.AppConstants
import era.app.utils.subclouders.Voxeler
import era.utils.count.Counter
import era.utils.time.Span
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import vectors.Plane
import vectors.Vector3Float
import java.util.concurrent.ThreadLocalRandom

object Plainer {

    private const val COTH_CREATE_HASH = 4
    private const val MIN_POINT_IN_PLAINS = 40
    private const val VOXEL_SIZE = 200
    private const val MIN_DIST_RANDOM_POINT = (VOXEL_SIZE*3).toFloat()
    private const val MAX_DIST_RANDOM_POINT = (VOXEL_SIZE*10).toFloat()
    private const val COUNT_RANDOM_POINT = 2000 //изначальная выборка точек
    private const val EPS = 25f // отклонение в мм когда точка еще в плоскости
    private const val POINTS_ON_VOXEL = 4 //количество точек на площадь вокселя допустимое
    private const val MAX_DIV_INTER_POINT = 6 //число, если 1/х частей точек облака пересекается с другим то другова не будет

    val span = Span()
    val span2 = Span()
    val counter = Counter()
    val counter2 = Counter()
    val counter3 = Counter()

    suspend fun CoroutineScope.create(
        fastCloud: Cloud, writeCloud: BufferedChannelForView
    ) = async (Dispatchers.Default) {
        //todo защита от маленьких облаков

        val voxelCloud = createVoxelRandomCloud(fastCloud)
        val planes = findPlanesDeterministic(voxelCloud, writeCloud).toMutableList()

        writeCloud.pointData.ofCloud(voxelCloud, 0f, AppConstants.AD_MAX_MM_ON_V3D, 10)
//        writeCloud.pointData.ofCloud(fastCloud, planes, AppConstants.AD_MAX_MM_ON_V3D, 10, EPS)


        writeCloud.planesData.ofPlains(planes, 1f, AppConstants.AD_MAX_MM_ON_V3D)
        planes.forEach { plane ->
            writeCloud.cubeData.writeCube(plane.getP1(), 1f,  AppConstants.AD_MAX_MM_ON_V3D)
            writeCloud.cubeData.writeCube(plane.getP2(), 1f, AppConstants.AD_MAX_MM_ON_V3D)
            writeCloud.cubeData.writeCube(plane.getP3(), 1f, AppConstants.AD_MAX_MM_ON_V3D)

            plane.points.forEach { p->
                writeCloud.cubeData.writeCube(plane.getPofCloud(p), 0f,  AppConstants.AD_MAX_MM_ON_V3D)
            }
        }

//        writePlanesOfTriangle(planes, 1f, writeCloud)

//                KiLogger.inf("ddd: ${planes.size}")

        return@async planes
    }.await()


    private suspend fun createVoxelRandomCloud(fastCloud: Cloud) : Cloud {
        val hashFastCloud = Hash(VOXEL_SIZE, fastCloud)
        hashFastCloud.init(COTH_CREATE_HASH)
        return Voxeler.createVoxelsRandomFromHash(fastCloud, hashFastCloud, COTH_CREATE_HASH)
    }

    private fun planesIsOkOfSq(plane: Plane) : Boolean {
        val p = plane.area()/(VOXEL_SIZE*VOXEL_SIZE) //сколько вокселей в площаде
        return plane.points.size * POINTS_ON_VOXEL < p
    }


    private fun findPlanesDeterministic(
        cloud: Cloud, writeCloud: BufferedChannelForView
    ): List<Plane> {

        span.resetTimer()
        val fPlans = createFPlansRandom2(cloud)
        span.middleCircle(100 ,"random")

//        counter.setCircle(fPlans.size)
//        counter.middleCircle(100, "count of planes of random")

//        val tpPlans = mutableListOf<Plane>()
//        val fpPlans = mutableListOf<Plane>()
//        fPlans.forEach { plane ->
//            if(planesIsOkOfSq(plane)) {
//                tpPlans.add(plane)
//            } else {
//                fpPlans.add(plane)
//            }
//        }
//        writePlanesOfTriangle(fpPlans, 1f, writeCloud)
//        writePlanesOfTriangle(tpPlans, 0f, writeCloud)


        fPlans.removeIf(::planesIsOkOfSq)
//        counter2.setCircle(fPlans.size)
//        counter2.middleCircle(100, "count of planes after delpl")
//        KiLogger.inf("2: ${fPlans.size}")

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
                    val inter = fPlan.points.intersect(tp.points).size
                    inter*MAX_DIV_INTER_POINT > fPlan.points.size
                }
                gPlans.add(tp)
            }
        }
//        counter3.setCircle(gPlans.size)
//        counter3.middleCircle(100, "g planes")


        return gPlans
    }

    val rnd = ThreadLocalRandom.current()

    private fun createFPlansRandom2(cloud: Cloud): MutableList<Plane> {
        val fPlans = mutableListOf<Plane>()
        val maxDistSq = MAX_DIST_RANDOM_POINT * MAX_DIST_RANDOM_POINT
        val minDistSq = MIN_DIST_RANDOM_POINT * MIN_DIST_RANDOM_POINT

        repeat(COUNT_RANDOM_POINT) {
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
                                if(plane.points.size >= MIN_POINT_IN_PLAINS) fPlans.add(plane)
                            }
                        }
                    }
                }
            }
        }

        return fPlans
    }

    private fun writePlanesOfTriangle(planes: List<Plane>, color: Float, writeCloud: BufferedChannelForView) {
        planes.forEach { plane ->
            writeCloud.lineData.addVec(plane.getP1(), plane.getP2(), color, AppConstants.AD_MAX_MM_ON_V3D)
            writeCloud.lineData.addVec(plane.getP2(), plane.getP3(), color, AppConstants.AD_MAX_MM_ON_V3D)
            writeCloud.lineData.addVec(plane.getP1(), plane.getP3(), color, AppConstants.AD_MAX_MM_ON_V3D)
        }
    }
}