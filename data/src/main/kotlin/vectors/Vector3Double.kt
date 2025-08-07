package vectors

import kotlin.math.sqrt

data class Vector3Double(val x: Double, val y: Double, val z: Double) {
    fun length() = sqrt(x * x + y * y + z * z)
    fun normalize(): Vector3Double {
        val len = length()
        return if (len != 0.0) Vector3Double(x / len, y / len, z / len) else this
    }

    fun dot(other: Vector3Double) = x * other.x + y * other.y + z * other.z
    fun cross(other: Vector3Double) = Vector3Double(
        y * other.z - z * other.y,
        z * other.x - x * other.z,
        x * other.y - y * other.x
    )

    operator fun times(s: Double) = Vector3Double(x * s, y * s, z * s)
    operator fun plus(other: Vector3Double) = Vector3Double(x + other.x, y + other.y, z + other.z)
}