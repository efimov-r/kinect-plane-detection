package viewdata.buffers

import vectors.Vector3Float
import java.lang.Integer.max

class TriangleBuffer(initialCapasity: Int) : ViewBuffer(initialCapasity) {

    fun addTriangle(v1: Vector3Float, v2: Vector3Float, v3: Vector3Float, color: Float, multFor3D: Float) {
        resizeForTriangle(1)
        addDat(v1.z * multFor3D)
        addDat(v1.x * multFor3D)
        addDat(v1.y * multFor3D)
        addDat(color)

        addDat(v2.z * multFor3D)
        addDat(v2.x * multFor3D)
        addDat(v2.y * multFor3D)
        addDat(color)

        addDat(v3.z * multFor3D)
        addDat(v3.x * multFor3D)
        addDat(v3.y * multFor3D)
        addDat(color)
        updated()
    }

    private fun resizeForTriangle(count: Int) {
        if ((sizeI + (12*count)) > data.size) {
            resize(max(sizeI * 2, sizeI+(12*count)))
        }
    }
}