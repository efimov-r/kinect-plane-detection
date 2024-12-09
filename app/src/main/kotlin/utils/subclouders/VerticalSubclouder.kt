package era.app.utils.subclouders

import cloud.FreeCloud
import cloud.databuffer.CloudOfSensor
import vectors.V2Vertical

object VerticalSubclouder {
    fun getStolb(inp: CloudOfSensor, vec: V2Vertical, width: Short): FreeCloud {
        val res = FreeCloud()
        val minZ = vec.z - (width/2)
        val maxZ = vec.z + (width/2)
        val minX = vec.x - (width/2)
        val maxX = vec.x + (width/2)
        inp.editData { glob, id ->
            if(glob.x > minX && glob.x < maxX && glob.z > minZ && glob.z < maxZ) {
                res.add(glob)
            }
        }
        return res
    }
}