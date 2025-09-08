package era.app.utils.subclouders

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import vectors.P2D

object QuickHull {
    suspend fun CoroutineScope.quickHull(points: List<P2D>): Set<Int> {
        if (points.size <= 3) return points.map { it.original }.toSet()

        val minXJob = async {
            points.minByOrNull { it.x }!!
        }
        val maxXJob = async {
            points.maxByOrNull { it.x }!!
        }
        val minX = minXJob.await()
        val maxX = maxXJob.await()

        val hullRightJob = async {
            val hull = mutableListOf<Int>()
            hull.add(minX.original)
            hull.add(maxX.original)
            val rightSet = points.filter { it.cross(minX, maxX) < 0 }
            buildHull(minX, maxX, rightSet, hull)
            return@async hull
        }
        val hullLeftJob = async {
            val hull = mutableListOf<Int>()
            hull.add(minX.original)
            hull.add(maxX.original)
            val leftSet = points.filter { it.cross(minX, maxX) > 0 }
            buildHull(maxX, minX, leftSet, hull)
            return@async hull
        }

        val hullLeft = hullLeftJob.await()
        val hullRight = hullRightJob.await()

        val hull = mutableSetOf(hullLeft.size + hullRight.size - 2)
        hull.addAll(hullLeft)
        hull.addAll(hullRight)
        return hull
    }

    private fun CoroutineScope.buildHull(a: P2D, b: P2D, set: List<P2D>, hull: MutableList<Int>) {
        val farthest = set.maxByOrNull { it.distanceFromLine(a, b) }!!
        hull.add(hull.indexOf(b.original), farthest.original)

        val leftSetAP = mutableListOf<P2D>()
        val leftSetPB = mutableListOf<P2D>()

        set.forEach {
            when {
                it.cross(a, farthest) > 0 -> leftSetAP.add(it)
                it.cross(farthest, b) > 0 -> leftSetPB.add(it)
            }
        }
        if(leftSetAP.isNotEmpty()) {
            buildHull(a, farthest, leftSetAP, hull)
        }
        if(leftSetPB.isNotEmpty()) {
            buildHull(farthest, b, leftSetPB, hull)
        }
    }
}