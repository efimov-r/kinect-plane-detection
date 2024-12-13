package vectors

open class Vector3Short(x: Short, y: Short, z: Short) : Vector3<Short>(x, y, z) {
    operator fun plus(other: Vector3Short) = Vector3Short(
        (x + other.x).toShort(),
        (y + other.y).toShort(),
        (z + other.z).toShort())

    operator fun minus(other: Vector3Short) = Vector3Short(
        (x - other.x).toShort(),
        (y - other.y).toShort(),
        (z - other.z).toShort())

    operator fun times(scalar: Double) = Vector3Short(
        (x * scalar).toInt().toShort(),
        (y * scalar).toInt().toShort(),
        (z * scalar).toInt().toShort())

    // Векторное произведение
    infix fun cross(other: Vector3Short): Vector3Short {
        return Vector3Short(
            (y * other.z - z * other.y).toShort(),
            (z * other.x - x * other.z).toShort(),
            (x * other.y - y * other.x).toShort()
        )
    }
}