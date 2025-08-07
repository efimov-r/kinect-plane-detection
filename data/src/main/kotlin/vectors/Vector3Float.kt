package vectors

import kotlin.math.sqrt

class Vector3Float(val x: Float, val y: Float, val z: Float) {
    fun length() = sqrt(x * x + y * y + z * z)
    fun normalize(): Vector3Float {
        val len = length()
        return if (len != 0f) Vector3Float(x / len, y / len, z / len) else this
    }

    fun dot(other: Vector3Float) = x * other.x + y * other.y + z * other.z
    fun cross(other: Vector3Float) = Vector3Float(
        y * other.z - z * other.y,
        z * other.x - x * other.z,
        x * other.y - y * other.x
    )

    operator fun times(s: Double) = Vector3Float((x * s).toFloat(), (y * s).toFloat(), (z * s).toFloat())
    operator fun plus(other: Vector3Float) = Vector3Float(x + other.x, y + other.y, z + other.z)
}