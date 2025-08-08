package vectors

import kotlin.math.sqrt

open class Vector3Float(x: Float, y: Float, z: Float) : Vector3<Float>(x, y, z) {
    infix fun plus(other: Vector3Float) = Vector3Float(
        x + other.x,
        y + other.y,
        z + other.z)

    infix fun minus(other: Vector3Float) = Vector3Float(
        x - other.x,
        y - other.y,
        z - other.z)

    infix fun times(scalar: Float) = Vector3Float(
        x * scalar,
        y * scalar,
        z * scalar)

    // Векторное произведение
    infix fun cross(other: Vector3Float): Vector3Float {
        return Vector3Float(
            y * other.z - z * other.y,
            z * other.x - x * other.z,
            x * other.y - y * other.x
        )
    }

    fun normalize(): Vector3Float {
        val len = length()
        return if (len != 0.0) Vector3Float((x / len).toFloat(), (y / len).toFloat(), (z / len).toFloat()) else this
    }

    // Вычисление длины вектора
    fun length(): Double = sqrt((x * x + y * y + z * z).toDouble())

    // Скалярное произведение двух векторов
    fun dot(other: Vector3Float): Double = (x * other.x + y * other.y + z * other.z).toDouble()

    override fun toString(): String {
        return "x: $x, y: $y, z: $z"
    }
}