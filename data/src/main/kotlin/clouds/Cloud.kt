package clouds

import era.utils.files.KiLogger
import jdk.incubator.vector.FloatVector
import jdk.incubator.vector.VectorMask
import jdk.incubator.vector.VectorSpecies
import kotlinx.coroutines.*
import utils.Threadripper.threadripp
import vectors.Plane
import java.io.Serializable
import kotlin.concurrent.thread
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt


open class Cloud(initialCapacity: Int = 1000, val name: String = "") : Serializable{
    protected var xArray = FloatArray(initialCapacity)
    protected var yArray = FloatArray(initialCapacity)
    protected var zArray = FloatArray(initialCapacity)
    protected var sizeI = 0
    val size: Int
        get() {
            return sizeI
        }

    private var minX = Float.MAX_VALUE
    private var minY = Float.MAX_VALUE
    private var minZ = Float.MAX_VALUE
    private var maxX = Float.MIN_VALUE
    private var maxY = Float.MIN_VALUE
    private var maxZ = Float.MIN_VALUE
    private var minmaxRelevant = false

    companion object {
        @JvmStatic
        private val serialVersionUID = -6401584170084423324L
    }

    open fun add(x: Float, y: Float, z: Float) {
        if (sizeI == xArray.size) {
            resize(sizeI * 2)
        }
        xArray[sizeI] = x
        yArray[sizeI] = y
        zArray[sizeI] = z
        sizeI++
        minmaxRelevant = false
    }

    fun add(cloud: Cloud, li: List<Int>) {
        if(sizeI + li.size > xArray.size) {
            val newCap = max(sizeI + li.size, sizeI * 2)
            resize(newCap)
        }
        for(i in 0 until li.size) {
            val ci = li[i]
            if(ci < Integer.MAX_VALUE) {
                xArray[sizeI] = cloud.xArray[ci]
                yArray[sizeI] = cloud.yArray[ci]
                zArray[sizeI] = cloud.zArray[ci]
                sizeI++
            }
        }
        minmaxRelevant = false
    }

    fun add(a: Cloud) {
        val newCap = sizeI + a.sizeI
        if (newCap > xArray.size) {
            val newSize = maxOf(newCap, xArray.size * 2)
            resize(newSize)
        }
        System.arraycopy(a.xArray, 0, xArray, sizeI, a.sizeI)
        System.arraycopy(a.yArray, 0, yArray, sizeI, a.sizeI)
        System.arraycopy(a.zArray, 0, zArray, sizeI, a.sizeI)
        sizeI += a.sizeI

        if(a.minmaxRelevant && minmaxRelevant) {
            if(a.minX < minX) minX = a.minX
            if(a.minY < minY) minY = a.minY
            if(a.minZ < minZ) minZ = a.minZ

            if(a.maxX > maxX) maxX = a.maxX
            if(a.maxY > maxY) maxY = a.maxY
            if(a.maxZ > maxZ) maxZ = a.maxZ
        } else {
            minmaxRelevant = false
        }
    }

    protected fun resize(newCapacity: Int) {
        KiLogger.inf("resize ${name}, old cap: ${xArray.size}, new cap: ${newCapacity}")
        xArray = xArray.copyOf(newCapacity)
        yArray = yArray.copyOf(newCapacity)
        zArray = zArray.copyOf(newCapacity)
    }

    suspend fun transformSIMD(
        matrix: Array<FloatArray>,
        threads: Int = Runtime.getRuntime().availableProcessors()
    ) {
        minmaxRelevant = false
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
        minmaxRelevant = false
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

    fun forEachIndexed(action: (x: Float, y: Float, z: Float, i: Int) -> Unit) {
        for (i in 0 until size) {
            action(xArray[i], yArray[i], zArray[i], i)
        }
    }

    fun forEachIndexed(action: (x: Float, i: Int) -> Unit) {
        for (i in 0 until size) {
            action(xArray[i], i)
        }
    }

    fun forEach(
        trX: (x: Float, y: Float, z: Float) -> Float,
        trY: (x: Float, y: Float, z: Float) -> Float,
        trZ: (x: Float, y: Float, z: Float) -> Float
    ) {
        minmaxRelevant = false
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
        minmaxRelevant = false

        threadripp(size, countOfThread) { start, end ->
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
        minmaxRelevant = false
    }

    fun getX(i: Int): Float = xArray[i]
    fun getY(i: Int): Float = yArray[i]
    fun getZ(i: Int): Float = zArray[i]

    fun getXFV(species: VectorSpecies<Float>, offset: Int) : FloatVector = FloatVector.fromArray(species, xArray, offset)
    fun getYFV(species: VectorSpecies<Float>, offset: Int) : FloatVector = FloatVector.fromArray(species, yArray, offset)
    fun getZFV(species: VectorSpecies<Float>, offset: Int) : FloatVector = FloatVector.fromArray(species, zArray, offset)

    fun getXFV(species: VectorSpecies<Float>, offset: Int, m: VectorMask<Float>) : FloatVector = FloatVector.fromArray(species, xArray, offset, m)
    fun getYFV(species: VectorSpecies<Float>, offset: Int, m: VectorMask<Float>) : FloatVector = FloatVector.fromArray(species, yArray, offset, m)
    fun getZFV(species: VectorSpecies<Float>, offset: Int, m: VectorMask<Float>) : FloatVector = FloatVector.fromArray(species, zArray, offset, m)

    fun getXFVB(species: VectorSpecies<Float>, index: Int) : FloatVector = FloatVector.broadcast(species, xArray[index])
    fun getYFVB(species: VectorSpecies<Float>, index: Int) : FloatVector = FloatVector.broadcast(species, yArray[index])
    fun getZFVB(species: VectorSpecies<Float>, index: Int) : FloatVector = FloatVector.broadcast(species, zArray[index])

    fun minMaxDo() {
        if(minmaxRelevant) return
        for(i in 0 until sizeI) {
            val x = xArray[i]
            val y = yArray[i]
            val z = zArray[i]
            if(x < minX) minX = x
            if(y < minY) minY = y
            if(z < minZ && z!=0f) minZ = z //todo 0f
            if(x > maxX) maxX = x
            if(y > maxY) maxY = y
            if(z > maxZ) maxZ = z
        }
        minmaxRelevant = true
    }

    fun minX(): Float? {
        minMaxDo()
        return if (minX == Float.MAX_VALUE) null else minX
    }
    fun minY(): Float? {
        minMaxDo()
        return if (minY == Float.MAX_VALUE) null else minY
    }
    fun minZ(): Float? {
        minMaxDo()
        return if (minZ == Float.MAX_VALUE) null else minZ
    }

    fun maxX(): Float? {
        minMaxDo()
        return if (maxX == Float.MIN_VALUE) null else maxX
    }
    fun maxY(): Float? {
        minMaxDo()
        return if (maxY == Float.MIN_VALUE) null else maxY
    }
    fun maxZ(): Float? {
        minMaxDo()
        return if (maxZ == Float.MIN_VALUE) null else maxZ
    }

    fun copyOf(inp: Cloud?) {
        if (inp == null) { //todo file!
            return
        }
        clear()
        if(inp.size > xArray.size) {
            resize(inp.size)
        }
        System.arraycopy(inp.xArray, 0, xArray, sizeI, inp.sizeI)
        System.arraycopy(inp.yArray, 0, yArray, sizeI, inp.sizeI)
        System.arraycopy(inp.zArray, 0, zArray, sizeI, inp.sizeI)
        sizeI = inp.sizeI
        if(inp.minmaxRelevant) {
            minX = inp.minX
            minY = inp.minY
            minZ = inp.minZ

            maxX = inp.maxX
            maxY = inp.maxY
            maxZ = inp.maxZ
            minmaxRelevant = true
        } else {
            minmaxRelevant = false
        }
    }

    fun planeFromPoints(p1: Int, p2: Int, p3: Int): Plane? {
        // Векторы
        val ux = xArray[p2] - xArray[p1]
        val uy = yArray[p2] - yArray[p1]
        val uz = zArray[p2] - zArray[p1]

        val vx = xArray[p3] - xArray[p1]
        val vy = yArray[p3] - yArray[p1]
        val vz = zArray[p3] - zArray[p1]

        // нормаль = u × v
        val a = uy * vz - uz * vy
        val b = uz * vx - ux * vz
        val c = ux * vy - uy * vx

        val norm = sqrt(a * a + b * b + c * c)
        if (norm < 1e-6f) return null // коллинеарные точки → плоскость не существует

        val md = a * xArray[p1] + b * yArray[p1] + c * zArray[p1]
        val d = -1 * md
        return Plane(a, b, c, d, p1, p2, p3)
    }

    fun distSquared(p1: Int, p2: Int) : Float {
        val dx = xArray[p1] - xArray[p2]
        val dy = yArray[p1] - yArray[p2]
        val dz = zArray[p1] - zArray[p2]
        return dx*dx + dy*dy + dz*dz
    }
}
