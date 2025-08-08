package era.app.utils.neighbour

import era.app.AppConstants.MULTIPLICIT_VITH_NEIGHBOURS_LENGTH
import era.app.AppConstants.NEIGHBOURS_MIN_COUNT
import era.app.AppConstants.STEP_CREATE_VERTICAL_VECTORS
import vectors.Vector3Childed

object NeighbourHelper {
    fun findAndFilter(vectors: MutableList<Vector3Childed>) {
        val sqrMaxLength = STEP_CREATE_VERTICAL_VECTORS * MULTIPLICIT_VITH_NEIGHBOURS_LENGTH
        val maxDistSqr = sqrMaxLength * sqrMaxLength

        for (i in vectors.indices) {
            val mainV = vectors[i]

            for (j in i + 1 until vectors.size) {
                val childV = vectors[j]

                val dx = mainV.x - childV.x
                val dy = mainV.y - childV.y
                val dz = mainV.z - childV.z
                val distSqr = dx * dx + dy * dy + dz * dz

                if (distSqr < maxDistSqr) {
                    mainV.neighbours.add(childV)
                    childV.neighbours.add(mainV) // двусторонняя связь
                }
            }
        }

        vectors.removeIf { it.neighbours.size < NEIGHBOURS_MIN_COUNT }
    }
}