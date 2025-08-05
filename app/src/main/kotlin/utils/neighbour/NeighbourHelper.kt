package era.app.utils.neighbour

import era.app.AppConstants.MULTIPLICIT_VITH_NEIGHBOURS_LENGTH
import era.app.AppConstants.NEIGHBOURS_MIN_COUNT
import era.app.AppConstants.STEP_CREATE_VERTICAL_VECTORS
import vectors.Vector3
import vectors.Vector3ChildedShort

object NeighbourHelper {
    fun findAndFilter(vectors: MutableList<Vector3ChildedShort>){
        val sqrMaxLength = (STEP_CREATE_VERTICAL_VECTORS * MULTIPLICIT_VITH_NEIGHBOURS_LENGTH) * (STEP_CREATE_VERTICAL_VECTORS * MULTIPLICIT_VITH_NEIGHBOURS_LENGTH)
        vectors.forEach { mainV->
            vectors.forEach{childV->
                if(calkSqrLength(mainV, childV) < sqrMaxLength && mainV != childV) {
                    mainV.neighbours.add(childV)
                }
            }
        }
        vectors.removeIf { it.neighbours.size < NEIGHBOURS_MIN_COUNT }
    }

    private fun calkSqrLength(v1: Vector3<Short>, v2: Vector3<Short>) : Int {
        return ((v1.x - v2.x) * (v1.x - v2.x)) + ((v1.y - v2.y) * (v2.y - v2.y)) + ((v1.z - v2.z) * (v1.z - v2.z))
    }
}