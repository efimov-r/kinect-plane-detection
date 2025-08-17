package vectors

class Vector3Childed3D(x: Float,
                       y: Float,
                       z: Float,
                       val neighbours: ArrayList<Vector3Float> = ArrayList(),
                       val vecs: ArrayList<Vector3Float> = ArrayList(),
                       var per: Vector3Float? = null) : Vector3Float(x, y, z)  {

    fun createPerpendicularVectors() {//todo to simd
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

//                val finalPx = if (py > 0) -px else px
//                val finalPy = if (py > 0) -py else py
//                val finalPz = if (py > 0) -pz else pz

                vecs.add(Vector3Float(px, py, pz))
            }
        }
    }

    fun groupPerpendicularVectors(cosAngleMin: Double) {
        if (vecs.size < 2) return

        val lengthsSquared = vecs.map { it.length() }

        val cos2Min = cosAngleMin * cosAngleMin
        val res = mutableListOf<MutableList<Vector3Float>>()

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

//        if (maxGroup != null && maxGroup.size > minGroupSize) {
        if (maxGroup != null) {
            var rx = 0f
            var ry = 0f
            var rz = 0f

            for (v in maxGroup) {
                rx += v.x
                ry += v.y
                rz += v.z
            }

            per = Vector3Float(
                rx / maxGroup.size,
                ry / maxGroup.size,
                rz / maxGroup.size
            )

        }
    }
}