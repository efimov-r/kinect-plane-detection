package era.app.jobs

import bufferchanel.databuffer.BufferedChannelForView
import bufferchanel.databuffer.BufferedCloudOfSensor
import clouds.Cloud
import clouds.Hash
import era.app.AppConstants
import era.app.utils.subclouders.CloudOfSensorCompressor
import era.app.utils.subclouders.Voxeler
import era.utils.count.Counter
import era.utils.time.Span
import jdk.incubator.vector.FloatVector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
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
    val span3 = Span()
    val span4 = Span()
    val counter = Counter()
    val counter2 = Counter()
    val counter3 = Counter()

    suspend fun CoroutineScope.create(
        readCloud: BufferedCloudOfSensor, writeCloud: BufferedChannelForView
    ) = async (Dispatchers.Default) {
        //todo защита от маленьких облаков

        val fastCloud = CloudOfSensorCompressor.compressHexadrupleRare(readCloud)

        val voxelCloud = createVoxelRandomCloud(fastCloud)
        val planes = findPlanesDeterministic(voxelCloud, writeCloud).toMutableList()

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

        planes.map { plane ->
            async {
                plane.recloud(readCloud, EPS)
            }
        }.awaitAll()

        val resCloud = Cloud(300_000, "resCloudWithoutPoint")
        readCloud.forEachIndexed { x, y, z, i ->
            var pointInPlain = false
            planes.forEach { plane ->
                if(plane.isPointOnPlaneFast(i)) pointInPlain = true
            }
            if(!pointInPlain) {
                resCloud.add(x, y, z)
            }
        }

        span2.resetTimer()
//        planes.map { pp->
//            async {
//                convexHullOnPlane3(pp)
//            }
//        }.awaitAll()

//        planes.map { pp->
//                convexHullOnPlane3(pp)
//        }

        convexHullOnPlane3(planes[0])

        span2.middleCircle(100, "hull")


        writeCloud.planesData.ofPlainPolygon3(planes, 1f, AppConstants.AD_MAX_MM_ON_V3D)
        writeCloud.pointData.ofCloud(resCloud, 0f, AppConstants.AD_MAX_MM_ON_V3D, 10)

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

    private val V1Y = Vector3Float(0f, 1f, 0f)
    private val V1X = Vector3Float(1f, 0f, 0f)

    // проекция точек на (u,v) + оригинальные 3D координаты
    data class P2Di(val x: Float,
                   val y: Float,
                   val original: Int)

    // кросс-продукт 2D
    inline fun cross(o: P2Di, a: P2Di, b: P2Di): Float {
        return (a.x - o.x) * (b.y - o.y) - (a.y - o.y) * (b.x - o.x)
    }

    suspend fun CoroutineScope.convexHullOnPlane3(
        plane: Plane
    ) {

        val normal = plane.getNormalize()

        // базис u,v в плоскости
        val tmp = if (kotlin.math.abs(normal.x) > 0.9f) V1Y else V1X

        val u = (tmp cross normal).normalized()
        val v = (normal cross u).normalized()
        span4.resetTimer()
//        val points2D = projectPointsSIMD(plane.cloud, plane.points.toIntArray(), u, v)

        val points2D = ArrayList<P2Di>(plane.points.size)
        plane.points.forEach { po->
            val p = plane.getPofCloud(po)
            points2D.add(P2Di((p dot u), (p dot v), po))
        }
        span4.middleCircle(100, "hInt")
        // сортировка (без compareBy для скорости)
        points2D.sortWith { a, b ->
            if (a.x == b.x) a.y.compareTo(b.y) else a.x.compareTo(b.x)
        }

        if (points2D.size <= 2) {
            plane.points = points2D.map { it.original }.toMutableList()
            return
        }


        val j1 = async {
//            span3.resetTimer()
            val lower = ArrayList<P2Di>()
            for (p in points2D) {
                while (lower.size >= 2 && cross(lower[lower.size - 2], lower[lower.size - 1], p) <= 0f) {
                    lower.removeAt(lower.size - 1)
                }
                lower.add(p)
            }
//            span3.middleCircle(100, "lower")
            return@async lower
        }

        val j2 = async {
//            span4.resetTimer()
            val upper = ArrayList<P2Di>()
            for (i in points2D.size - 1 downTo 0) {
                while (upper.size >= 2 && cross(upper[upper.size - 2], upper[upper.size - 1], points2D[i]) <= 0f) {
                    upper.removeAt(upper.size - 1)
                }
                upper.add(points2D[i])
            }
//            span4.middleCircle(100, "upper")
            return@async upper
        }

        val lower = j1.await()
        val upper = j2.await()

        // итоговая оболочка
        val hull = ArrayList<Int>(lower.size + upper.size - 2)
        for (i in 0 until lower.size - 1) hull.add(lower[i].original)
        for (i in 0 until upper.size - 1) hull.add(upper[i].original)
        plane.points = hull
    }

    val SPECIES = FloatVector.SPECIES_PREFERRED

    fun projectPointsSIMD(
        cloud: Cloud,
        indices: IntArray,
        u: Vector3Float,
        v: Vector3Float
    ): ArrayList<P2Di> {
        val res = ArrayList<P2Di>(indices.size)

        val ux = u.x; val uy = u.y; val uz = u.z
        val vx = v.x; val vy = v.y; val vz = v.z

        // массивы координат точек
        val xs = FloatArray(indices.size) { cloud.getX(indices[it]) }
        val ys = FloatArray(indices.size) { cloud.getY(indices[it]) }
        val zs = FloatArray(indices.size) { cloud.getZ(indices[it]) }

        var i = 0
        val upperBound = SPECIES.loopBound(indices.size) // кратная граница

        while (i < upperBound) {
            // загружаем пачку координат
            val X = FloatVector.fromArray(SPECIES, xs, i)
            val Y = FloatVector.fromArray(SPECIES, ys, i)
            val Z = FloatVector.fromArray(SPECIES, zs, i)

            // проекции на базис (u, v)
            val dotU = X.mul(ux).add(Y.mul(uy)).add(Z.mul(uz))
            val dotV = X.mul(vx).add(Y.mul(vy)).add(Z.mul(vz))

            // выгружаем во временные массивы
            val arrU = FloatArray(SPECIES.length())
            val arrV = FloatArray(SPECIES.length())
            dotU.intoArray(arrU, 0)
            dotV.intoArray(arrV, 0)

            // сохраняем результаты с индексами
            for (j in 0 until SPECIES.length()) {
                res.add(P2Di(arrU[j], arrV[j], indices[i + j]))
            }

            i += SPECIES.length()
        }

        // обрабатываем хвост (если элементов не хватает на полный вектор)
        while (i < indices.size) {
            val p = cloud.getVec(i)
            res.add(P2Di((p dot u), (p dot v), i))
            i++
        }

        return res
    }

}