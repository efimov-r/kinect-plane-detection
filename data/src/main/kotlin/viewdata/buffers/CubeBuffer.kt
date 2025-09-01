package viewdata.buffers

import vectors.Vector3Childed
import vectors.Vector3Float
import java.lang.Integer.max

class CubeBuffer(initialCapasity: Int = 10) : ViewBuffer(initialCapasity) {

    fun writeCube(vector: Vector3Float, color:Float, multFor3D: Float) {
        resizeForCube(1)
        addDat(vector.z * multFor3D)
        addDat(vector.x * multFor3D)
        addDat(vector.y * multFor3D)
        addDat(color)
        updated()
    }

    fun writeCubes(vectors: List<Vector3Float>, color:Float, multFor3D: Float) {
        vectors.forEach { vectors ->
            writeCube(vectors, color, multFor3D)
        }
    }

    fun writeCubes(vectors: List<Vector3Childed>, multFor3D: Float) {
        var v: Vector3Childed? = null
        vectors.forEach{
            if(it.vecs.size > 0) {
                v = it
            }
        }

        resizeForCube(vectors.size)
        vectors.forEach { v3->
            addDat(v3.z * multFor3D)
            addDat(v3.x * multFor3D)
            addDat(v3.y * multFor3D)
            addDat(if(isThisNeighbours(v3.neighbours, v!!)) 0f else 1f)
        }
        updated()
    }

    fun writeCubeWithChild(vector: Vector3Childed, multFor3D: Float) {
        resizeForCube(vector.neighbours.size + 1)
        addDat(vector.z * multFor3D)
        addDat(vector.x * multFor3D)
        addDat(vector.y * multFor3D)
        addDat(0f)

        vector.neighbours.forEach { vector ->
            addDat(vector.z * multFor3D)
            addDat(vector.x * multFor3D)
            addDat(vector.y * multFor3D)
            addDat(1f)
        }
        updated()
    }

    private fun isThisNeighbours(vectors: ArrayList<Vector3Float>, v: Vector3Childed): Boolean {
        vectors.forEach {
            if(v.equals(it))
                return true
        }
        return false
    }

    private fun resizeForCube(count: Int) {
        if ((sizeI + (4*count)) > data.size) {
            resize(max(sizeI * 2, sizeI+(4*count)))
        }
    }
}