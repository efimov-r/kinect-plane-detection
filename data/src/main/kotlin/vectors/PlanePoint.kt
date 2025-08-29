package vectors

import kotlin.math.abs
import kotlin.math.sqrt

data class PlanePoint(
    val a: Float, val b: Float, val c: Float, val d: Float,
    val points: ArrayList<Vector3Float>
) {
    private var norm: Float? = null

    fun isPointOnPlane(x: Float, y: Float, z: Float, eps: Float): Boolean {
        val epsNorm = eps * getNorm()
        val value = a * x + b * y + c * z + d
        return abs(value) < epsNorm
    }

    private fun getNorm() : Float {
        if(norm == null) {
            norm = sqrt(a * a + b * b + c * c)
        }
        return norm!!
    }

    fun getNormalize() = Vector3Float(a,b,c).normalize()

}