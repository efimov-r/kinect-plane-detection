package viewdata.buffers

import vectors.Vector3Childed
import vectors.Vector3Childed3D
import vectors.Vector3Float
import java.lang.Integer.max

class LineBuffer(initialCapasity: Int = 10) : ViewBuffer(initialCapasity) {

    fun addPerpendicular(v3: Vector3Childed, multFor3D: Float) {
        resizeForVEctors(v3.vecs.size)
        v3.vecs.forEach { pv ->
            addDat(v3.z * multFor3D)
            addDat(v3.x * multFor3D)
            addDat(v3.y * multFor3D)
            addDat(1f)

            addDat((v3.z + pv.z) * multFor3D)
            addDat((v3.x + pv.x) * multFor3D)
            addDat((v3.y + pv.y) * multFor3D)
            addDat(1f)
        }
        updated()
    }

    fun addPerpendicular(v3: Vector3Childed3D, multFor3D: Float) {
        resizeForVEctors(v3.vecs.size)
        v3.per?.let { pv ->
            addDat(v3.z * multFor3D)
            addDat(v3.x * multFor3D)
            addDat(v3.y * multFor3D)
            addDat(1f)

            addDat((v3.z + pv.z) * multFor3D)
            addDat((v3.x + pv.x) * multFor3D)
            addDat((v3.y + pv.y) * multFor3D)
            addDat(1f)
        }
        updated()
    }

    fun addNullVector(v3: Vector3Float, color: Float, multFor3D: Float) {
        resizeForVEctors(1)
        addDat(v3.z * multFor3D)
        addDat(v3.x * multFor3D)
        addDat(v3.y * multFor3D)
        addDat(color)

        addDat(0f)
        addDat(0f)
        addDat(0f)
        addDat(color)

        updated()
    }

    fun addVec(v1: Vector3Float, v2: Vector3Float, multFor3D: Float) {
        resizeForVEctors(1)
        addDat(v1.z * multFor3D)
        addDat(v1.x * multFor3D)
        addDat(v1.y * multFor3D)
        addDat(1f)

        addDat(v2.z * multFor3D)
        addDat(v2.x * multFor3D)
        addDat(v2.y * multFor3D)
        addDat(1f)
        updated()
    }

    private fun resizeForVEctors(count: Int) {
        if ((sizeI + (8*count)) > data.size) {
            resize(max(sizeI * 2, sizeI+(8*count)))
        }
    }
}