package clouds

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import utils.Threadripper.threadripp

class Hash(val voxelSize: Int, val cloud: Cloud) {
    private val data: LongArray = LongArray(cloud.size)
    val size: Int
        get() = data.size

    suspend fun init(countOfThread: Int) {
        coroutineScope {
            threadripp(cloud.size, countOfThread) { start, end ->
                for (i in start until end) {
                    data[i] = createHash(cloud.getX(i), cloud.getY(i), cloud.getZ(i), voxelSize)
                }
            }
        }
    }

    suspend fun getVoxelMap(initialBranchCap: Int, countOfThread: Int): Map<Long, MutableList<Int>> =
        coroutineScope {
            val n = data.size
            val chunk = (n + countOfThread - 1) / countOfThread

            // Шаг 1: локальные карты (без синхронизации внутри потока)
            val parts = (0 until countOfThread).map { t ->
                async(Dispatchers.Default) {
                    val start = t * chunk
                    val end = minOf(start + chunk, n)
                    val local = HashMap<Long, MutableList<Int>>(end - start)

                    for (i in start until end) {
                        val key = data[i]
                        val list = local[key]
                        if (list == null) {
                            val newList = ArrayList<Int>(initialBranchCap)
                            newList.add(i)
                            local[key] = newList
                        } else {
                            list.add(i)
                        }
                    }
                    local
                }
            }.awaitAll()

            // Шаг 2: слияние локальных карт
            val res = HashMap<Long, MutableList<Int>>(parts.sumOf { it.size })
            for (part in parts) {
                for ((k, v) in part) {
                    val dst = res[k]
                    if (dst == null) {
                        // можно сразу выделить «правильный» размер
                        res[k] = ArrayList<Int>(maxOf(initialBranchCap, v.size)).also { it.addAll(v) }
                    } else {
                        dst.addAll(v)
                    }
                }
            }
            res
        }

    fun getVoxelMap(initialBranchCap: Int) : Map<Long, MutableList<Int>> =
        mutableMapOf<Long, MutableList<Int>>().apply {
            data.forEachIndexed { index, value ->
                this.computeIfAbsent(value) { ArrayList(initialBranchCap) }.add(index)
            }
        }.toMap()




    companion object {
        const val BIAS = 1_048_576L
        const val MASK = 0x1FFFFFL

        fun createHash(x: Float, y: Float, z: Float, voxelSize: Int): Long {
            val ix = ((x + BIAS) / voxelSize).toLong()
            val iy = ((y + BIAS) / voxelSize).toLong()
            val iz = ((z + BIAS) / voxelSize).toLong()

            return ((ix and MASK) shl 42) or
                    ((iy and MASK) shl 21) or
                    (iz and MASK)
        }

        fun getX(key: Long, voxelSize: Int) : Float {
            val ix = (key ushr 42) and MASK
            return ix.toFloat() * voxelSize - BIAS
        }

        fun getY(key: Long, voxelSize: Int) : Float {
            val iy = (key ushr 21) and MASK
            return iy.toFloat() * voxelSize - BIAS
        }

        fun getZ(key: Long, voxelSize: Int) : Float {
            val iz = key and MASK
            return iz.toFloat() * voxelSize - BIAS
        }
    }
}