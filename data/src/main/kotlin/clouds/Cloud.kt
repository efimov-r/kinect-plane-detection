package clouds

import era.utils.files.KiLogger
import jdk.incubator.vector.FloatVector
import jdk.incubator.vector.VectorSpecies
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import serialize.ShortArraySerialize
import kotlin.concurrent.thread
import kotlin.math.min

open class Cloud(initialCapacity: Int = 1000, name: String = ""){
    protected var xArray = FloatArray(initialCapacity)
    protected var yArray = FloatArray(initialCapacity)
    protected var zArray = FloatArray(initialCapacity)
    protected var sizeI = 0
    val size: Int
        get() {
            return sizeI
        }

    open fun add(x: Float, y: Float, z: Float) {
        if (sizeI == xArray.size) {
            resize(sizeI * 2)
        }
        xArray[sizeI] = x
        yArray[sizeI] = y
        zArray[sizeI] = z
        sizeI++
    }

    protected fun resize(newCapacity: Int) {
        xArray = xArray.copyOf(newCapacity)
        yArray = yArray.copyOf(newCapacity)
        zArray = zArray.copyOf(newCapacity)
    }

    suspend fun transformSIMD(
        matrix: Array<FloatArray>,
        threads: Int = Runtime.getRuntime().availableProcessors()
    ) {
        val SPEC = FloatVector.SPECIES_PREFERRED
        val len = SPEC.length()
        val chunkSize = (size + threads - 1) / threads

        coroutineScope {
            repeat(threads) { t ->
                launch(Dispatchers.Default) {
                    val start = t * chunkSize
                    val end = min(start + chunkSize, size)
                    var i = start

                    while (i <= end - len) {
                        val vx = FloatVector.fromArray(SPEC, xArray, i)
                        val vy = FloatVector.fromArray(SPEC, yArray, i)
                        val vz = FloatVector.fromArray(SPEC, zArray, i)

                        // row 0
                        val rx = vx.mul(matrix[0][0])
                            .add(vy.mul(matrix[0][1]))
                            .add(vz.mul(matrix[0][2]))

                        // row 1
                        val ry = vx.mul(matrix[1][0])
                            .add(vy.mul(matrix[1][1]))
                            .add(vz.mul(matrix[1][2]))

                        // row 2
                        val rz = vx.mul(matrix[2][0])
                            .add(vy.mul(matrix[2][1]))
                            .add(vz.mul(matrix[2][2]))

                        rx.intoArray(xArray, i)
                        ry.intoArray(yArray, i)
                        rz.intoArray(zArray, i)

                        i += len
                    }

                    // хвост
                    while (i < end) {
                        val x = xArray[i]
                        val y = yArray[i]
                        val z = zArray[i]
                        xArray[i] = matrix[0][0] * x + matrix[0][1] * y + matrix[0][2] * z
                        yArray[i] = matrix[1][0] * x + matrix[1][1] * y + matrix[1][2] * z
                        zArray[i] = matrix[2][0] * x + matrix[2][1] * y + matrix[2][2] * z
                        i++
                    }
                }
            }
        }
    }

    fun transformSIMD(matrix: Array<FloatArray>) {
        val SPEC = FloatVector.SPECIES_PREFERRED
        val len = SPEC.length()

        var i = 0
        while (i <= size - len) {
            val vx = FloatVector.fromArray(SPEC, xArray, i)
            val vy = FloatVector.fromArray(SPEC, yArray, i)
            val vz = FloatVector.fromArray(SPEC, zArray, i)

            // row 0
            val rx = vx.mul(matrix[0][0])
                .add(vy.mul(matrix[0][1]))
                .add(vz.mul(matrix[0][2]))

            // row 1
            val ry = vx.mul(matrix[1][0])
                .add(vy.mul(matrix[1][1]))
                .add(vz.mul(matrix[1][2]))

            // row 2
            val rz = vx.mul(matrix[2][0])
                .add(vy.mul(matrix[2][1]))
                .add(vz.mul(matrix[2][2]))

            rx.intoArray(xArray, i)
            ry.intoArray(yArray, i)
            rz.intoArray(zArray, i)

            i += len
        }

        // хвост без SIMD
        while (i < size) {
            val x = xArray[i]
            val y = yArray[i]
            val z = zArray[i]
            xArray[i] = (matrix[0][0] * x + matrix[0][1] * y + matrix[0][2] * z)
            yArray[i] = (matrix[1][0] * x + matrix[1][1] * y + matrix[1][2] * z)
            zArray[i] = (matrix[2][0] * x + matrix[2][1] * y + matrix[2][2] * z)
            i++
        }
    }

    fun forEach(action: (x: Float, y: Float, z: Float) -> Unit) {
        for (i in 0 until size) {
            action(xArray[i], yArray[i], zArray[i])
        }
    }

    fun forEach(
        trX: (x: Float, y: Float, z: Float) -> Float,
        trY: (x: Float, y: Float, z: Float) -> Float,
        trZ: (x: Float, y: Float, z: Float) -> Float
    ) {
        var vrX: Float
        var vrY: Float
        var vrZ: Float
        for (i in 0 until size) {
            vrX = trX(xArray[i], yArray[i], zArray[i])
            vrY = trY(xArray[i], yArray[i], zArray[i])
            vrZ = trZ(xArray[i], yArray[i], zArray[i])
            xArray[i] = vrX
            yArray[i] = vrY
            zArray[i] = vrZ
        }
    }

    suspend fun forEach(
        trX: (x: Float, y: Float, z: Float) -> Float,
        trY: (x: Float, y: Float, z: Float) -> Float,
        trZ: (x: Float, y: Float, z: Float) -> Float,
        countOfThread: Int) = coroutineScope {


        val chunk = (size + countOfThread - 1) / countOfThread
        val jobs = ArrayList<Job>(countOfThread)

        for (t in 0 until countOfThread) {
            val start = t * chunk
            val end = min(size, start + chunk)

            if (start >= end) break

            jobs += launch(Dispatchers.Default) {
                var i = start
                var vrX: Float
                var vrY: Float
                var vrZ: Float
                while (i < end) {
                    vrX = trX(xArray[i], yArray[i], zArray[i])
                    vrY = trY(xArray[i], yArray[i], zArray[i])
                    vrZ = trZ(xArray[i], yArray[i], zArray[i])
                    xArray[i] = vrX
                    yArray[i] = vrY
                    zArray[i] = vrZ
                    i++
                }
            }
        }

        jobs.joinAll()
    }

    fun parallelForEach(chunkSize: Int = 1000, action: (x: Float, y: Float, z: Float) -> Unit) {
        val threads = Runtime.getRuntime().availableProcessors()
        val chunk = (size) / threads + 1

        List(threads) { threadIndex -> //todo to coroutines
            thread {
                val from = threadIndex * chunk
                val to = minOf((size), from + chunk)
                for (i in from until to) {
                    action(xArray[i], yArray[i], zArray[i])
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

    fun getXFV(SPECIES: VectorSpecies<Float>, offset: Int) : FloatVector = FloatVector.fromArray(SPECIES, xArray, offset)
    fun getYFV(SPECIES: VectorSpecies<Float>, offset: Int) : FloatVector = FloatVector.fromArray(SPECIES, yArray, offset)
    fun getZFV(SPECIES: VectorSpecies<Float>, offset: Int) : FloatVector = FloatVector.fromArray(SPECIES, zArray, offset)

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
