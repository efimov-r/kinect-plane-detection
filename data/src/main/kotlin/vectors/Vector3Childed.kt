package vectors

class Vector3ChildedShort(x: Short,
                        y: Short,
                        z: Short,
                        val neighbours: ArrayList<Vector3Short> = ArrayList(),
                        val neighboursPerpendicularVectors: ArrayList<Vector3Short> = ArrayList()) : Vector3Short(x, y, z) {

    fun createPerpendicularVectors() {
        println("neighbours.size: ${neighbours.size}")
        for(i in 0..<neighbours.size) {
            for(j in (i+1)..<neighbours.size) {
//                println("asdasdasd")
                neighboursPerpendicularVectors.add(neighbours.get(i) cross neighbours.get(j))
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Vector3ChildedShort

        return (x == other.x) && (y == other.y) && (z == other.z)
    }
}