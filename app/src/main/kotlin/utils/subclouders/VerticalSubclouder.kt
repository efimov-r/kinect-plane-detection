package era.app.utils.subclouders

import clouds.CloudJ
import vectors.V2Vertical

object VerticalSubclouder {
    fun getStolb(inp: CloudJ, vec: V2Vertical, width: Short): CloudJ {
        val res = CloudJ(300, "Stolb cloud, $vec")
        val minZ = vec.z - (width/2)
        val maxZ = vec.z + (width/2)
        val minX = vec.x - (width/2)
        val maxX = vec.x + (width/2)

        inp.forEach{x, y, z ->
            if(x > minX && x < maxX && z > minZ && z < maxZ) {
                res.addGlob(x, y, z)
            }
        }
        return res
    }
}