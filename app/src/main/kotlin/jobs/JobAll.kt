package era.app.jobs

import bufferchanel.databuffer.BufferedChannelForView
import clouds.Cloud
import clouds.Hash
import clouds.KDTree
import era.app.utils.subclouders.Voxeler
import era.utils.files.KiLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import utils.Threadripper.threadripp
import vectors.Plane
import kotlin.math.min

object JobAll {

    private const val COT_CREATE_HASH = 4
    private const val COT_FIND_PLAINS = 20
    private const val MIN_POINT_IN_PLAINS = 8




    suspend fun CoroutineScope.doJobAll(
        fastCloud: Cloud, writeCloud: BufferedChannelForView
    ) = launch (Dispatchers.IO) {
        val voxelCloud = createVoxelCloud(fastCloud, COT_CREATE_HASH)
        val planes = findPlanesDeterministic(voxelCloud, COT_FIND_PLAINS)
//                KiLogger.inf("ddd: ${planes.size}")
    }.join()

    private suspend fun createVoxelCloud(fastCloud: Cloud, countOfThread: Int) : Cloud {
        val hashFastCloud = Hash(300, fastCloud)
        hashFastCloud.init(countOfThread)
        return Voxeler.createVoxelsFromHash(fastCloud, hashFastCloud, countOfThread)
    }

    private suspend fun CoroutineScope.findPlanesDeterministic(
        cloud: Cloud,
        countOfThread: Int,
        eps: Float = 1e-3f
    ): List<Plane> {

        val fPlans = createFPlans(cloud, countOfThread)

        setInlinersAll(fPlans, eps, cloud, countOfThread) //долгое, полторы на 10 потоках

        fPlans.removeIf { plane -> plane.points.size < MIN_POINT_IN_PLAINS }
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
                fPlans.removeIf { p ->
                    tp.points.contains(p.p1i) && tp.points.contains(p.p2i) && tp.points.contains(p.p3i)
                }
                gPlans.add(tp)
            }
        }

        return gPlans
    }

    private suspend fun CoroutineScope.createFPlans(cloud: Cloud, countOfThread: Int) : MutableList<Plane> {
        val tree = KDTree.instanceParallel(cloud)

        val maxDist = 400
        val maxDistFloat = maxDist.toFloat()
        val maxDistSquared = maxDist * maxDist
        val size = cloud.size-2
        val chunk = (size + countOfThread - 1) / countOfThread

        return (0 until countOfThread).map { t->
            async(Dispatchers.Default) {
                val start = t * chunk
                val end = min(size, start + chunk)
                val fPlansLocal = mutableListOf<Plane>()
                if (start < end) {
                    for (a in start until end) {
                        val aNei = tree.radiusSearch(maxDistFloat, a)
                        for (b in 0 until aNei.size - 1) {
                            for (c in 1 until aNei.size) {
                                val bT = aNei[b]
                                val cT = aNei[c]
                                if (cloud.distSquared(bT, cT) < maxDistSquared) {
                                    cloud.planeFromPoints(a, bT, cT)?.let {
                                        fPlansLocal.add(it)
                                    }
                                }
                            }
                        }
                    }
                }
                return@async fPlansLocal
            }
        }.awaitAll().flatten().toMutableList()
    }

    private suspend fun CoroutineScope.setInlinersAll(
        fPlans: MutableList<Plane>,
        eps: Float,
        cloud: Cloud,
        countOfThread: Int)
    {
        threadripp(fPlans.size, countOfThread) { start, end ->
            for(i in start until end){
                fPlans[i].setInliersSIMD(cloud, eps)
            }
        }
    }




}