package viewdata.elementlist

class ElementList<T: IElement>(val initialCapacity: Int = 10,
                               val growSize: Int = 1,
                               val dataCreator: () -> T) {
    private var elementData: Array<IElement>
    private var size = 0
    private var isUpdated = false;

    companion object {
        private const val MAX_ARRAY_SIZE = Int.MAX_VALUE - 8
    }

    init {
        if (initialCapacity > 0) {
            this.elementData = Array(initialCapacity){dataCreator()}
        } else {
            throw IllegalArgumentException(
                "Illegal Capacity: " +
                        initialCapacity
            )
        }
    }

    fun trimToSize() {
        if (size < elementData.size) {
            if(size == 0) {
                resizeElementData(initialCapacity)
            } else {
                resizeElementData(size)
            }
        }
    }

    fun ensureCapacity(minCapacity: Int) {
        if (minCapacity > elementData.size) {
            resizeElementData(calkNewCapacity(minCapacity))
        }
    }

    private fun resizeElementData(newSize: Int) {
        Array(newSize) { index ->
            if (index < elementData.size) {
                elementData[index]
            } else {
                dataCreator()
            }
        }.also { elementData = it }
    }

    private fun calkNewCapacity(minCapacity: Int): Int {
        val newCapacity = elementData.size + (elementData.size shr 1)
        if (newCapacity - minCapacity <= 0) {
            if (minCapacity < 0)  // overflow
                throw OutOfMemoryError()
            return minCapacity
        }
        if (newCapacity - MAX_ARRAY_SIZE <= 0) return newCapacity
        if (minCapacity < 0)  // overflow
            throw OutOfMemoryError()
        return if (minCapacity > MAX_ARRAY_SIZE) Int.MAX_VALUE else MAX_ARRAY_SIZE
    }

    fun size(): Int {
        return size
    }

    @Suppress("UNCHECKED_CAST")
    fun add(callback: (dat: T) -> Unit): Boolean {
        if (size == elementData.size) resizeElementData(calkNewCapacity(this.size + this.growSize))
        callback(elementData[size] as T)
        this.size += 1
        isUpdated = true
        return true
    }

    fun clear() {
        size=0
        isUpdated = true
    }

    fun isUpdated(): Boolean {
        return isUpdated
    }

    fun endRead() {
        isUpdated = false
    }

    @Suppress("UNCHECKED_CAST")
    fun forEach(callback: (dat: T, i: Int) -> Unit) {
        var i = 0
        while (i < this.size) {
            callback(elementData[i] as T, i)
            i++
        }
        isUpdated = true //на всякий, не мешает, а вдруг в цикле редактировали
    }
}