package vectors

open class Vector3<T:Number>(var x: T,
                      var y: T,
                      var z: T) {
    override fun toString(): String {
        return "Vector3(x=$x, y=$y, z=$z)"
    }
}
