package clouds

import serialize.ShortArraySerialize
import kotlin.concurrent.thread

open class Cloud(initialCapacity: Int = 1000, name: String = ""){
    private var xArray = FloatArray(initialCapacity)
    private var yArray = FloatArray(initialCapacity)
    private var zArray = FloatArray(initialCapacity)
    private var sizeI = 0
    val size: Int
        get() {
            return sizeI
        }

    fun size(): Int = sizeI

    open fun add(x: Float, y: Float, z: Float) {
        if (sizeI == xArray.size) {
            resize(sizeI * 2)
        }
        xArray[sizeI] = x
        yArray[sizeI] = y
        zArray[sizeI] = z
        sizeI++
    }

    private fun resize(newCapacity: Int) {
        xArray = xArray.copyOf(newCapacity)
        yArray = yArray.copyOf(newCapacity)
        zArray = zArray.copyOf(newCapacity)
    }

    inline fun forEach(action: (x: Float, y: Float, z: Float) -> Unit) {
        for (i in 0 until (size)) {
            action(getX(i), getY(i), getZ(i))
        }
    }

    inline fun parallelForEach(chunkSize: Int = 1000, crossinline action: (x: Float, y: Float, z: Float) -> Unit) {
        val threads = Runtime.getRuntime().availableProcessors()
        val chunk = (size) / threads + 1

        List(threads) { threadIndex -> //todo to coroutines
            thread {
                val from = threadIndex * chunk
                val to = minOf((size), from + chunk)
                for (i in from until to) {
                    action(getX(i), getY(i), getZ(i))
                }
            }
        }.forEach { it.join() }
    }

    fun clear() {
        sizeI = 0
    }

    fun getX(i: Int): Float = xArray[i]
    fun getY(i: Int): Float = yArray[i]
    fun getZ(i: Int): Float = zArray[i]

    open fun minX() : Float? = xArray.minOrNull()
    open fun minY() : Float? = yArray.minOrNull()
    open fun minZ() : Float? = zArray.minOrNull()

    open fun maxX() : Float? = xArray.maxOrNull()
    open fun maxY() : Float? = yArray.maxOrNull()
    open fun maxZ() : Float? = zArray.maxOrNull()

    val objForSerialize: ShortArraySerialize
        get() { //todo file!
            val elementData = ShortArray(size*3)
            var i = 0
            forEach { x, y, z ->
                elementData[i] = x.toInt().toShort()
                i++
                elementData[i] = y.toInt().toShort()
                i++
                elementData[i] = z.toInt().toShort()
                i++
            }
            return ShortArraySerialize(elementData, size*3)
        }

    fun copyOf(inp: ShortArraySerialize?) {
        if (inp == null) { //todo file!
            return
        }
        clear()
        resize(inp.getSize())
        for (i in 0..<(inp.getSize() / 3)) {
            add(inp.getData()[i * 3].toFloat(), inp.getData()[i * 3 + 1].toFloat(), inp.getData()[i * 3 + 2].toFloat())
        }
    }
}
