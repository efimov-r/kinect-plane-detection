package era.app.utils.subclouders

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import vectors.P2D

object OldHull {
    suspend fun CoroutineScope.oldHull(points2D: MutableList<P2D>): MutableList<Int> {
        val comparator = Comparator<P2D> { a, b ->
            if (a.x == b.x) a.y.compareTo(b.y) else a.x.compareTo(b.x)
        }
        points2D.sortWith(comparator)

        val j1 = async(Dispatchers.Default) {
            val lower = ArrayList<P2D>()
            for (p in points2D) {
                while (lower.size >= 2 && p.crossOld(lower[lower.size - 2], lower.last()) <= 0f) {
                    lower.removeAt(lower.lastIndex)
                }
                lower.add(p)
            }
            return@async lower
        }

        val j2 = async(Dispatchers.Default) {
            val upper = ArrayList<P2D>()
            for (i in points2D.size - 1 downTo 0) {
                while (upper.size >= 2 && points2D[i].crossOld(upper[upper.size - 2], upper[upper.size - 1]) <= 0f) {
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
}