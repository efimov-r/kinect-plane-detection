package era.app.jobs

import bufferchanel.databuffer.BufferedChannelForView
import clouds.Cloud
import clouds.Hash
import clouds.KDTree
import era.app.utils.subclouders.Voxeler
import era.utils.files.KiLogger
import era.utils.time.Span
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import vectors.Plane

object JobAll {

    val span = Span()


    fun CoroutineScope.doJobAll(
        fastCloud: Cloud, writeCloud: BufferedChannelForView
    ) = async(Dispatchers.IO) {
        val hashFastCloud = Hash(300)
        hashFastCloud.create(fastCloud)
        val voxelCloud = Voxeler.createVoxelsFromHash(fastCloud, hashFastCloud)

        val planes = findPlanesDeterministic(voxelCloud)

    }

    private fun createFPlans(cloud: Cloud) : MutableList<Plane> {
        val fPlans = mutableListOf<Plane>()
        val maxDistSquared = 400*400
        // полный перебор троек среди оставшихся
        for (a in 0 until cloud.size - 2) {
            for (b in a + 1 until cloud.size - 1) {
                if(cloud.distSquared(a, b) < maxDistSquared) {
                    for (c in b + 1 until cloud.size) {
                        if(cloud.distSquared(a, c) < maxDistSquared) {
                            if(cloud.distSquared(b, c) < maxDistSquared) {
                                cloud.planeFromPoints(a, b, c)?.let{
                                    fPlans.add(it)
                                }
                            }
                        }
                    }
                }
            }
        }
        return fPlans
    }

    private fun createFPlans2(cloud: Cloud) : MutableList<Plane> {
        val fPlans = mutableListOf<Plane>()
        val maxDist = 400
        val maxDistFloat = maxDist.toFloat()
        val maxDistSquared = maxDist * maxDist

        val tree = KDTree.instanceSingle(cloud) //todo parallel
        for (a in 0 until cloud.size - 2) {
            val aNei = tree.radiusSearch(maxDistFloat, a)
            for (b in 0 until aNei.size - 1) {
                for (c in 0 until aNei.size) {
                    val bT = aNei[b]
                    val cT = aNei[c]
                    if(cloud.distSquared(bT, cT) < maxDistSquared) {
                        cloud.planeFromPoints(a, bT, cT)?.let{
                            fPlans.add(it)
                        }
                    }
                }
            }
        }

        return fPlans
    }

    private fun findPlanesDeterministic(
        cloud: Cloud,
        eps: Float = 1e-3f,
        minPoints: Int = 8
    ): List<Plane> {


        val fPlans = createFPlans2(cloud)

        span.resetTimer()
        fPlans.forEach { plane ->
            plane.setInliersSIMD(cloud, eps)
        }
        span.middleCircle(100, "create fPlans")

        fPlans.removeIf { plane -> plane.points.size < minPoints }

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



        KiLogger.inf("ddd: ${gPlans.size}")
        return gPlans
    }


}