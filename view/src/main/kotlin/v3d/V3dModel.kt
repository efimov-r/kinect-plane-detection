package v3d

import java.nio.FloatBuffer

interface V3dModel {
    fun buildModel(buffer: FloatBuffer): Int
    fun getSizeBuf(): Int
}