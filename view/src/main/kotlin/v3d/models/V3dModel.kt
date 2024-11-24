package v3d.models

import java.nio.FloatBuffer

interface V3dModel {
    fun buildModel(buffer: FloatBuffer): Int
    fun getSizeBuf(): Int
}