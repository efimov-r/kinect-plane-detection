package era.app.utils.neighbour

import era.app.AppConstants.STEP_CREATE_VERTICAL_VECTORS
import vectors.Vector3
import vectors.Vector3ChildedShort

object NeighbourHelper {
    fun findAndFilter(vectors: ArrayList<Vector3ChildedShort>){
        val sqrMaxLength = (STEP_CREATE_VERTICAL_VECTORS * 1.2) * (STEP_CREATE_VERTICAL_VECTORS * 1.2)
        vectors.forEach { mainV->

            vectors.forEach{childV->
                if(calkSqrLength(mainV, childV) < sqrMaxLength && !mainV.equals(childV)) {
                    mainV.neighbours.add(childV)
                }
            }


        }
        vectors.removeIf { it.neighbours.size < 4 }
    }

    private fun calkSqrLength(v1: Vector3<Short>, v2: Vector3<Short>) : Int {
        return ((v1.x - v2.x) * (v1.x - v2.x)) + ((v1.y - v2.y) * (v2.y - v2.y)) + ((v1.z - v2.z) * (v1.z - v2.z))
    }
}