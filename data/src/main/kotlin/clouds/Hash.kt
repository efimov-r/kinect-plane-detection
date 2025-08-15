package clouds

import era.utils.files.KiLogger

class Hash(val voxelSize: Int) {

    private var data: LongArray? = null
    val size: Int
        get() = data?.size ?: 0

    fun create(cloud: Cloud) {
        data = cloud.createVoxelHash(voxelSize)
    }

    fun getVoxelMap(initialBranchCap: Int) : Map<Long, MutableList<Int>> =
        data?.let {
            mutableMapOf<Long, MutableList<Int>>().apply {
                it.forEachIndexed { index, value ->
                    this.computeIfAbsent(value) { ArrayList(initialBranchCap) }.add(index)
                }
            }.toMap()
        } ?: run {
            KiLogger.err("not init hash")
            emptyMap()
        }



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