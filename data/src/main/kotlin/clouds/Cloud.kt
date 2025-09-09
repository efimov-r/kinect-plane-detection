package clouds

import era.utils.files.KiLogger
import jdk.incubator.vector.FloatVector
import jdk.incubator.vector.VectorMask
import jdk.incubator.vector.VectorSpecies
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.jocl.CL
import org.jocl.Pointer
import org.jocl.Sizeof
import org.jocl.cl_context
import vectors.P2D
import vectors.PlaneCloud
import vectors.Vector3Float
import java.io.Serializable
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt


open class Cloud : Serializable{
    val name: String

    protected var xArray: FloatArray
    protected var yArray: FloatArray
    protected var zArray: FloatArray
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

    var hash : LongArray? = null
    var hashSize : Int? = null

    constructor(initialCapacity: Int = 1000, name: String = "") {
        this.name = name
        this.xArray = FloatArray(initialCapacity)
        this.yArray = FloatArray(initialCapacity)
        this.zArray = FloatArray(initialCapacity)
    }

    constructor(xArray: FloatArray, yArray: FloatArray, zArray: FloatArray, size: Int,
                hash : LongArray?, hashSize : Int?, name: String = "") {
        this.xArray = xArray
        this.yArray = yArray
        this.zArray = zArray
        this.sizeI = size
        this.hash = hash
        this.hashSize = hashSize
        this.name = name
    }

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

    val SPECIES: VectorSpecies<Float> = FloatVector.SPECIES_PREFERRED

//    fun forEachAddPointToPlaneSimd(
//        a: Float, b: Float, c: Float, d: Float,
//        plane: Plane,
//        epsNorm: Float,
//        epsNormSec: Float
//    ) {
//        var i = 0
//        val upperBound = SPECIES.loopBound(size)
//
//        while (i < size) {
//            if(i < upperBound) {
//                val xv = FloatVector.fromArray(SPECIES, xArray, i)
//                val yv = FloatVector.fromArray(SPECIES, yArray, i)
//                val zv = FloatVector.fromArray(SPECIES, zArray, i)
//
//                var res = xv.mul(a).add(yv.mul(b)).add(zv.mul(c)).add(d)
//                res = res.abs()
//
//                val lenArray = FloatArray(SPECIES.length())
//                res.intoArray(lenArray, 0)
//
//                // проверка условий по каждому элементу в векторе
//                for (j in 0 until SPECIES.length()) {
//                    val idx = i + j
//                    val len = lenArray[j]
//                    if (len < epsNorm) {
//                        plane.points.add(idx)
//                        plane.pointsWithSecondOrder.add(idx)
//                    } else if (len < epsNormSec) {
//                        plane.pointsWithSecondOrder.add(idx)
//                    }
//                }
//                i += SPECIES.length()
//            } else {
//                val len = abs(a * xArray[i] + b * yArray[i] + c * zArray[i] + d)
//                if (len < epsNorm) {
//                    plane.points.add(i)
//                    plane.pointsWithSecondOrder.add(i)
//                } else if (len < epsNormSec) {
//                    plane.pointsWithSecondOrder.add(i)
//                }
//                i++
//            }
//        }
//    }

    fun forEachIndexed(action: (x: Float, i: Int) -> Unit) {
        for (i in 0 until size) {
            action(xArray[i], i)
        }
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

    fun getVec(p: Int) = Vector3Float(xArray[p], yArray[p], zArray[p])

    fun getXMem(context: cl_context?) = CL.clCreateBuffer(context,
        CL.CL_MEM_READ_ONLY or CL.CL_MEM_COPY_HOST_PTR,
        Sizeof.cl_float * size.toLong(), Pointer.to(xArray), null)
    fun getYMem(context: cl_context?) = CL.clCreateBuffer(context,
        CL.CL_MEM_READ_ONLY or CL.CL_MEM_COPY_HOST_PTR,
        Sizeof.cl_float * size.toLong(), Pointer.to(yArray), null)
    fun getZMem(context: cl_context?) = CL.clCreateBuffer(context,
        CL.CL_MEM_READ_ONLY or CL.CL_MEM_COPY_HOST_PTR,
        Sizeof.cl_float * size.toLong(), Pointer.to(zArray), null)

    fun minMaxDo() {
        KiLogger.inf("minmaxdo!")
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

    fun planeFromPoints(p1: Int, p2: Int, p3: Int): PlaneCloud? {
        val ux = xArray[p2] - xArray[p1]
        val uy = yArray[p2] - yArray[p1]
        val uz = zArray[p2] - zArray[p1]

        val vx = xArray[p3] - xArray[p1]
        val vy = yArray[p3] - yArray[p1]
        val vz = zArray[p3] - zArray[p1]

        // скалярное произведение
        val dot = ux * vx + uy * vy + uz * vz
        val lenU = sqrt(ux*ux + uy*uy + uz*uz)
        val lenV = sqrt(vx*vx + vy*vy + vz*vz)

        if (lenU < 1e-6f || lenV < 1e-6f) return null // почти нулевой вектор

        val cosTheta = dot / (lenU * lenV)

        // cos(30°) ≈ 0.866
        if (cosTheta in -0.866f..0.866f) {
            // нормаль = u × v
            val a = uy * vz - uz * vy
            val b = uz * vx - ux * vz
            val c = ux * vy - uy * vx

            val norm = sqrt(a * a + b * b + c * c)
            if (norm < 1e-6f) return null

            val d = -(a * xArray[p1] + b * yArray[p1] + c * zArray[p1])
            return PlaneCloud(a, b, c, d, p1, p2, p3, cloud = this)
        }
        return null
    }

    fun distSquared(p1: Int, p2: Int) : Float {
        val dx = xArray[p1] - xArray[p2]
        val dy = yArray[p1] - yArray[p2]
        val dz = zArray[p1] - zArray[p2]
        return dx*dx + dy*dy + dz*dz
    }

    fun cloneOfIndex(forCopy: List<Int>): Cloud {
        val resCloud = Cloud(forCopy.size, "resCloudWithoutPoint")
        var j = 0
        for(i in forCopy) {
            resCloud.xArray[j] = xArray[i]
            resCloud.yArray[j] = yArray[i]
            resCloud.zArray[j] = zArray[i]
            j++
        }
        resCloud.sizeI = forCopy.size
        return resCloud
    }

    fun cloneOfIndex(mask: BooleanArray): Cloud {
        val resCloud = Cloud(400_000, "resCloudWithoutPoint")
        if(mask.size != size) {
            throw Exception("mask size error with clone cloud")
        }
        for(i in 0 until size) {
            if(mask[i]) {
                resCloud.add(
                    xArray[i], yArray[i], zArray[i]
                )
            }
        }
        return resCloud
    }

    fun create2DPoints(u: Vector3Float, v: Vector3Float, indexes: IntArray) : MutableList<P2D> {
        val res = indexes.map { i ->
            val x = xArray[i]
            val y = yArray[i]
            val z = zArray[i]
            val newX = x * u.x + y * u.y + z * u.z
            val newY = x * v.x + y * v.y + z * v.z
            P2D(newX, newY, i)
        }.toMutableList()

        if(res.size > 2) {
            res.sortWith { a, b ->
                if (a.x == b.x) a.y.compareTo(b.y) else a.x.compareTo(b.x)
            }
        }

        return res
    }

    fun recreateWithoutNull() : Cloud {
        val res = Cloud(size, name)
        for (i in 0 until size) {
            if(zArray[i] != 0f) {
                res.xArray[res.sizeI] = xArray[i]
                res.yArray[res.sizeI] = yArray[i]
                res.zArray[res.sizeI] = zArray[i]
                res.sizeI++
            }
        }
        return res
    }
}
