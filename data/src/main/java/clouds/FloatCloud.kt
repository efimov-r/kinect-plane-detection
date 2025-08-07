package clouds

class FloatCloud(initialCapacity: Int = 1000) {
    private var xArray = FloatArray(initialCapacity)
    private var yArray = FloatArray(initialCapacity)
    private var zArray = FloatArray(initialCapacity)
    private var sizeI = 0
    val size: Int
        get() {
            return sizeI
        }

    fun size(): Int = sizeI

    fun add(x: Float, y: Float, z: Float) {
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
        for (i in 0 until size) {
            action(getX(i), getY(i), getZ(i))
        }
    }

    inline fun parallelForEach(chunkSize: Int = 1000, crossinline action: (x: Float, y: Float, z: Float) -> Unit) {
        val threads = Runtime.getRuntime().availableProcessors()
        val chunk = size / threads + 1

        val jobs = List(threads) { threadIndex ->
            kotlin.concurrent.thread {
                val from = threadIndex * chunk
                val to = minOf(size, from + chunk)
                for (i in from until to) {
                    action(getX(i), getY(i), getZ(i))
                }
            }
        }
        jobs.forEach { it.join() }
    }

    fun clear() {
        sizeI = 0
    }

    fun getX(i: Int): Float = xArray[i]
    fun getY(i: Int): Float = yArray[i]
    fun getZ(i: Int): Float = zArray[i]
}