package vectors

class Vector3ChildedShort(x: Short,
                        y: Short,
                        z: Short,
                        val neighbours: ArrayList<Vector3Short> = ArrayList(),
                        val vecs: ArrayList<Vector3Short> = ArrayList()) : Vector3Short(x, y, z) { //neighboursPerpendicularVectors

    fun createPerpendicularVectors() {
        for (i in 0 until neighbours.size) {
            val n1x = (neighbours[i].x - x)
            val n1y = (neighbours[i].y - y)
            val n1z = (neighbours[i].z - z)

            for (j in i + 1 until neighbours.size) {
                val n2x = (neighbours[j].x - x)
                val n2y = (neighbours[j].y - y)
                val n2z = (neighbours[j].z - z)

                // Векторное произведение n2 × n1
                val px = n2y * n1z - n2z * n1y
                val py = n2z * n1x - n2x * n1z
                val pz = n2x * n1y - n2y * n1x

                val finalPx = if (py > 0) -px else px
                val finalPy = if (py > 0) -py else py
                val finalPz = if (py > 0) -pz else pz

                vecs.add(Vector3Short((finalPx/100).toShort(), (finalPy/100).toShort(), (finalPz/100).toShort()))
            }
        }
    }

    fun filterPerpendicularVectors() {
        val v2 = Vector3Short(0, -1, 0)
        val v2Len2 = 1  // (0, -1, 0) → длина² = 1

        vecs.removeIf { v1 ->
            val dot = v1.x * v2.x + v1.y * v2.y + v1.z * v2.z
            val v1Len2 = (v1.x * v1.x + v1.y * v1.y + v1.z * v1.z).toDouble()


            // избегаем деления на ноль
            if (v1Len2 == 0.0) return@removeIf true

            val cos2 = (dot * dot).toDouble() / (v1Len2 * v2Len2)  // cos²(θ)
            // cos(45°) ≈ 0.7071 → cos² ≈ 0.5
            return@removeIf cos2 < 0.5
        }
    }

    fun groupPerpendicularVectors(cosAngleMin: Double) {
        if (vecs.size < 2) return

        val lengthsSquared = vecs.map { it.length() }

        val cos2Min = cosAngleMin * cosAngleMin
        val res = mutableListOf<MutableList<Vector3Short>>()

        for (i in vecs.indices) {
            for (j in i + 1 until vecs.size) {
                val dot = vecs[i].dot(vecs[j])
                val cos2 = (dot * dot) / (lengthsSquared[i] * lengthsSquared[j])

                if (cos2 > cos2Min) {
                    val group = mutableListOf(vecs[i], vecs[j])

                    for (k in j + 1 until vecs.size) {
                        val dot2 = vecs[i].dot(vecs[k])
                        val cos2k = (dot2 * dot2) / (lengthsSquared[i] * lengthsSquared[k])

                        if (cos2k > cos2Min) {
                            group.add(vecs[k])
                        }
                    }

                    res.add(group)
                }
            }
        }

        val maxGroup = res.maxByOrNull { it.size }
        val minGroupSize = vecs.size / 2

        vecs.clear()

        if (maxGroup != null && maxGroup.size > minGroupSize) {
            var rx = 0
            var ry = 0
            var rz = 0

            for (v in maxGroup) {
                rx += v.x.toInt()
                ry += v.y.toInt()
                rz += v.z.toInt()
            }

            val avg = Vector3Short(
                (rx / maxGroup.size).toShort(),
                (ry / maxGroup.size).toShort(),
                (rz / maxGroup.size).toShort()
            )

            vecs.add(avg)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Vector3ChildedShort

        return (x == other.x) && (y == other.y) && (z == other.z)
    }
}