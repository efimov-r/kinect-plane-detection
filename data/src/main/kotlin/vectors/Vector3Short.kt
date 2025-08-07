package vectors

import kotlin.math.sqrt

open class Vector3Short(x: Short, y: Short, z: Short) : Vector3<Short>(x, y, z) {
    infix fun plus(other: Vector3Short) = Vector3Short(
        (x + other.x).toShort(),
        (y + other.y).toShort(),
        (z + other.z).toShort())

    infix fun minus(other: Vector3Short) = Vector3Short(
        (x - other.x).toShort(),
        (y - other.y).toShort(),
        (z - other.z).toShort())

    infix fun times(scalar: Double) = Vector3Short(
        (x * scalar).toInt().toShort(),
        (y * scalar).toInt().toShort(),
        (z * scalar).toInt().toShort())

    // Векторное произведение
    infix fun cross(other: Vector3Short): Vector3Short {
        return Vector3Short(
            ((y * other.z - z * other.y)).toShort(),
            ((z * other.x - x * other.z)).toShort(),
            ((x * other.y - y * other.x)).toShort()
        )
    }

    fun normalize(): Vector3Short {
        val len = length()
        return if (len != 0.0) Vector3Short((x / len).toInt().toShort(), (y / len).toInt().toShort(), (z / len).toInt().toShort()) else this
    }

    // Вычисление длины вектора
    fun length(): Double = sqrt((x * x + y * y + z * z).toDouble())

    // Скалярное произведение двух векторов
    fun dot(other: Vector3Short): Double = (x * other.x + y * other.y + z * other.z).toDouble()

    override fun toString(): String {
        return "x: $x, y: $y, z: $z"
    }
}