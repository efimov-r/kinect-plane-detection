package era.app.jobs

import bufferchanel.databuffer.BufferedChannelForView
import bufferchanel.databuffer.BufferedCloudOfSensor
import clouds.Cloud
import clouds.Hash
import era.app.utils.subclouders.CloudOfSensorCompressor.compressHexadrupleRare
import era.app.utils.subclouders.Voxeler
import era.utils.time.Span
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import vectors.P2D
import vectors.Plane
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
    val span3 = Span()
    val span4 = Span()

    data class PlainerResult(
        val cloud: Cloud,
        val planes: List<Plane>
    )

    suspend fun CoroutineScope.create(
        readCloud: BufferedCloudOfSensor, writeCloud: BufferedChannelForView
    ) = async (Dispatchers.Default) {
        //todo защита от маленьких облаков
        span3.resetTimer()

        val fastCloud = compressHexadrupleRare(readCloud, COTH_CREATE_HASH)
        val voxelCloud = createVoxelRandomCloud(fastCloud)
        val planes = findPlanesDeterministic(voxelCloud)

//        writeCloud.pointData.ofCloud(voxelCloud, 0f, AppConstants.AD_MAX_MM_ON_V3D, 10)
//        writeCloud.pointData.ofCloud(fastCloud, planes, AppConstants.AD_MAX_MM_ON_V3D, 10, EPS)


//        writeCloud.planesData.ofPlains(planes, 1f, AppConstants.AD_MAX_MM_ON_V3D)
//        planes.forEach { plane ->
//            writeCloud.cubeData.writeCube(plane.getP1(), 1f,  AppConstants.AD_MAX_MM_ON_V3D)
//            writeCloud.cubeData.writeCube(plane.getP2(), 1f, AppConstants.AD_MAX_MM_ON_V3D)
//            writeCloud.cubeData.writeCube(plane.getP3(), 1f, AppConstants.AD_MAX_MM_ON_V3D)
//
//            plane.points.forEach { p->
//                writeCloud.cubeData.writeCube(plane.getPofCloud(p), 0f,  AppConstants.AD_MAX_MM_ON_V3D)
//            }
//        }

//        writeCloud.planesData.clear()

        val recloudJobs = planes.map { plane ->
            async {
                plane.recloud(readCloud, EPS)
                return@async plane
            }
        }

        val hullJobs = recloudJobs.map { job ->
            async {
                val pp = job.await()
                convexHullOnPlane(pp)
                return@async pp
            }
        }

        val resCloud = async {
            recloudJobs.awaitAll()
            val mask = BooleanArray(readCloud.size) { true }
            for (plane in planes) {
                for (i in plane.pointsWithSecondOrder) mask[i] = false
            }

            val resCloud = readCloud.cloneOfIndex(mask)
            return@async resCloud
        }.await()

        span3.middleCircle(100, "all do")

        return@async PlainerResult(resCloud, hullJobs.awaitAll())
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
        cloud: Cloud
    ): List<Plane> {
        val fPlans = createFPlansRandom(cloud)

        fPlans.removeIf(::planesIsOkOfSq)

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


        return gPlans
    }

    private fun createFPlansRandom(cloud: Cloud): MutableList<Plane> {
        val fPlans = mutableListOf<Plane>()
        val rnd = ThreadLocalRandom.current()
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


    // кросс-продукт 2D
    inline fun cross(o: P2D, a: P2D, b: P2D): Float {
        return (a.x - o.x) * (b.y - o.y) - (a.y - o.y) * (b.x - o.x)
    }

    suspend fun CoroutineScope.convexHullOnPlane(
        plane: Plane
    ) {
        val points2D = plane.get2DPoints().toMutableList()

        if (points2D.size <= 2) {
            plane.points = points2D.map { it.original }.toMutableList()
            return
        }

        points2D.sortWith { a, b ->
            if (a.x == b.x) a.y.compareTo(b.y) else a.x.compareTo(b.x)
        }

        val j1 = async(Dispatchers.Default) {
            val lower = ArrayList<P2D>()
            for (p in points2D) {
                while (lower.size >= 2 && cross(lower[lower.size - 2], lower.last(), p) <= 0f) {
                    lower.removeAt(lower.lastIndex)
                }
                lower.add(p)
            }
            return@async lower
        }

        val j2 = async(Dispatchers.Default) {
            val upper = ArrayList<P2D>()
            for (i in points2D.size - 1 downTo 0) {
                while (upper.size >= 2 && cross(upper[upper.size - 2], upper[upper.size - 1], points2D[i]) <= 0f) {
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
        plane.points = hull
    }
}