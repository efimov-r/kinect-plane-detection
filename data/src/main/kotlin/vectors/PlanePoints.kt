package vectors

import era.utils.files.KiLogger
import kotlin.math.abs
import kotlin.math.sqrt

data class PlanePoints(
    private val a: Float,
    private val b: Float,
    private val c: Float,
    private val d: Float,
    private var norm: Float,
    val initialCapacity: Int = 1000,
    val name: String = ""
) {

    private var xArray = FloatArray(initialCapacity)
    private var yArray = FloatArray(initialCapacity)
    private var zArray = FloatArray(initialCapacity)
    private var sizeI = 0
    val size: Int
        get() {
            return sizeI
        }

    fun getNorm() : Float { //todo to private
        return norm
    }

    fun setPoints(xArray: FloatArray, yArray: FloatArray, zArray: FloatArray, size: Int) {
        this.xArray = xArray
        this.yArray = yArray
        this.zArray = zArray
        this.sizeI = size
    }

    fun addPoint(x: Float, y: Float, z: Float) {
        if (sizeI == xArray.size) {
            resize(sizeI * 2)
        }
        xArray[sizeI] = x
        yArray[sizeI] = y
        zArray[sizeI] = z
        sizeI++
    }

    fun addVec(v: Vector3Float) {
        if (sizeI == xArray.size) {
            resize(sizeI * 2)
        }
        xArray[sizeI] = v.x
        yArray[sizeI] = v.y
        zArray[sizeI] = v.z
        sizeI++
    }

    fun getPointsAsVector() : List<Vector3Float> =
        (0 until sizeI).map { i ->
            Vector3Float(xArray[i], yArray[i], zArray[i])
        }

    private fun resize(newCapacity: Int) {
        KiLogger.inf("resize ${name}, old cap: ${xArray.size}, new cap: ${newCapacity}")
        xArray = xArray.copyOf(newCapacity)
        yArray = yArray.copyOf(newCapacity)
        zArray = zArray.copyOf(newCapacity)
    }

    fun getNormalize() = Vector3Float(a,b,c).normalize()

    fun getVecCloud(p: Int) = Vector3Float(xArray[p], yArray[p], zArray[p])

    fun isPointOnPlane(x: Float, y: Float, z: Float, eps: Float): Boolean {
        val epsNorm = eps * getNorm()
        val value = a * x + b * y + c * z + d
        return abs(value) < epsNorm
    }

//    fun isPointOnPlaneFast(index: Int): Boolean {
//        return points.contains(index)
//    }

    override fun toString(): String {
        return "Plane(a=$a, b=$b, c=$c, d=$d)"
    }
}
