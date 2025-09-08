package clouds

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class Cloud2D(
    private val xArray: FloatArray,
    private val yArray: FloatArray,
    private val original: IntArray,
    private var sizeI: Int
) {
    val size: Int get() = sizeI

    companion object {
        fun create(outX: FloatArray, outY: FloatArray, oouIdx: IntArray, size: Int): Cloud2D =
            Cloud2D(outX, outY, oouIdx, size)
    }

    suspend fun oldHull(): List<Int> =
        coroutineScope {
            val j1 = async(Dispatchers.Default) {
                val lower = ArrayList<Int>()
                for (p in (0 until sizeI)) {
                    while (lower.size >= 2 && crossOld(lower[lower.size - 2], lower.last(), p) <= 0f) {
                        lower.removeAt(lower.lastIndex)
                    }
                    lower.add(p)
                }
                return@async lower.map { i->
                    original[i]
                }
            }

            val j2 = async(Dispatchers.Default) {
                val upper = ArrayList<Int>()
                for (i in sizeI - 1 downTo 0) {
                    while (upper.size >= 2 && crossOld(upper[upper.size - 2], upper.last(), i) <= 0f) {
                        upper.removeAt(upper.size - 1)
                    }
                    upper.add(i)
                }
                return@async upper.map { i->
                    original[i]
                }
            }
            return@coroutineScope j1.await() + j2.await()
        }


    private inline fun crossOld(o: Int, a: Int, b: Int): Float {
        return (xArray[a] - xArray[o]) * (yArray[b] - yArray[o]) - (yArray[a] - yArray[o]) * (xArray[b] - xArray[o])
    }
}
