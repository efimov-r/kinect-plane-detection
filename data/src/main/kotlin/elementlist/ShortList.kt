package elementlist

import serialize.ShortArraySerialize
import kotlin.math.min

open class ShortList @JvmOverloads constructor(
    initialCapacity: Int,
    private val name: String?,
    private val growSize: Int = 1
) {
    @JvmField
    protected var elementData: ShortArray
    private var size = 0

    init {
        if (initialCapacity > 0) {
            elementData = ShortArray(initialCapacity)
        } else {
            throw IllegalArgumentException("Illegal Capacity: " + initialCapacity)
        }
    }

    fun trimToSize() {
        if (size < elementData.size) {
            resize(size)
        }
    }

    fun ensureCapacity(minCapacity: Int) {
        if (minCapacity > elementData.size) {
            resize(minCapacity)
        }
    }

    fun size(): Int {
        return size
    }

    fun add(v: Short) {
        if (size == elementData.size) resize(this.size + 1 + this.growSize)
        elementData[size] = v
        this.size += 1
    }

    private fun resize(minCapacity: Int) {
        if (minCapacity < 0) throw OutOfMemoryError()
        val newCapacity = elementData.size + (elementData.size shr 1)
        if (newCapacity <= minCapacity) {
            elementData = elementData.copyOf(minCapacity)
            return
        }
        elementData = elementData.copyOf(min(newCapacity, MAX_ARRAY_SIZE))
    }

    open fun clear() {
        size = 0
    }

    fun get(i: Int): Short {
        if (i < 0 || i >= size) {
            throw IndexOutOfBoundsException()
        }
        return elementData[i]
    }

    protected fun copyOf(inp: ShortList) {
        this.elementData = inp.elementData.clone()
        this.size = inp.size
    }

    val objForSerialize: ShortArraySerialize
        get() = ShortArraySerialize(elementData, size)

    companion object {
        private val MAX_ARRAY_SIZE = Int.Companion.MAX_VALUE - 8
    }
}
