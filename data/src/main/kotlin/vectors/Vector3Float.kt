package vectors

import kotlin.math.sqrt

open class Vector3Float(var x: Float, var y: Float, var z: Float) {
//    infix fun plus(other: Vector3Float) = Vector3Float(
//        x + other.x,
//        y + other.y,
//        z + other.z)
//
//    infix fun minus(other: Vector3Float) = Vector3Float(
//        x - other.x,
//        y - other.y,
//        z - other.z)
//
//    infix fun times(scalar: Float) = Vector3Float(
//        x * scalar,
//        y * scalar,
//        z * scalar)

    // Векторное произведение
    infix fun cross(other: Vector3Float): Vector3Float {
        return Vector3Float(
            y * other.z - z * other.y,
            z * other.x - x * other.z,
            x * other.y - y * other.x
        )
    }

    operator fun plus(o: Vector3Float) = Vector3Float(x+o.x, y+o.y, z+o.z)
    operator fun minus(o: Vector3Float) = Vector3Float(x-o.x, y-o.y, z-o.z)
    operator fun times(s: Float) = Vector3Float(x*s, y*s, z*s)

    fun normalize(): Vector3Float {
        val len = length()
        return if (len != 0.0f) Vector3Float(x / len, y / len, z / len) else this
    }

    fun normalized(): Vector3Float {
        val len = length()
        return if (len > 1e-6f) Vector3Float(x / len, y / len, z / len) else this
    }

    // Вычисление длины вектора
    fun length(): Float = sqrt(x * x + y * y + z * z)

    // Скалярное произведение двух векторов
    infix fun dot(other: Vector3Float): Float = x * other.x + y * other.y + z * other.z

    override fun toString(): String {
        return "Vector3(x=$x, y=$y, z=$z)"
    }
}