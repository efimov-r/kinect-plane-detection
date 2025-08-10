package bufferchanel.databuffer

import clouds.Cloud

class BufferedCloudOfSensor(initialCapacity: Int = 10,
                            name: String = "",
                            val id: Int) : Cloud(initialCapacity, name), DataForBufferedChannel {

    var width: Int = 0
    var height: Int = 0

    override fun getIdData(): Int {
        return id
    }

    override fun validate(): Boolean {
        return width*height == size
    }

    fun asRow(): RowCloudData {
        return RowCloudData(
            xArray,
            yArray,
            zArray,
            width
        )
    }

    data class RowCloudData(
        val x: FloatArray,
        val y: FloatArray,
        val z: FloatArray,
        val width: Int
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as RowCloudData

            if (width != other.width) return false
            if (!x.contentEquals(other.x)) return false
            if (!y.contentEquals(other.y)) return false
            if (!z.contentEquals(other.z)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = width
            result = 31 * result + x.contentHashCode()
            result = 31 * result + y.contentHashCode()
            result = 31 * result + z.contentHashCode()
            return result
        }
    }
}