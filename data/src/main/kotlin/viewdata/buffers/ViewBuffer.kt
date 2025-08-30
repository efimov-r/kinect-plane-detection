package viewdata.buffers

import java.nio.FloatBuffer

open class ViewBuffer (initialCapacity: Int) {
    protected var data = FloatArray(initialCapacity)
    private var isUpdated = false
    protected var sizeI = 0
    val size: Int
        get() {
            return sizeI
        }
    val arraySize: Int
        get() {
            return data.size
        }

    protected fun resize(newCapacity: Int) {
        data = data.copyOf(newCapacity)
    }

    fun clear() {
        sizeI = 0
        isUpdated = true
    }

    fun isUpdated(): Boolean = isUpdated

    fun updated() {
        isUpdated = true
    }

    fun dataToBuffer(buffer: FloatBuffer) {
        buffer.put(data)
        isUpdated = false
    }

    //inline ахренеть как влияет на производительность
    protected inline fun addDat(dataIn: Float) {
        data[sizeI] = dataIn
        sizeI++
    }
}