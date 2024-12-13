package era.app.utils.subclouders

import clouds.ChankCloudJ
import clouds.CloudJ
import vectors.V2Vertical

object Chanker {
    fun getChank(inp: CloudJ, vec: V2Vertical, halfSize: Short): ChankCloudJ {
        val res = ChankCloudJ(300, "Stolb cloud, $vec")

        inp.forEach{x, y, z ->
            if(
                x > vec.x - halfSize &&
                x < vec.x + halfSize &&
                z > vec.z - halfSize &&
                z < vec.z + halfSize
                ) {
                res.addGlob(x, y, z)
            }
        }
        res.centrX = vec.x
        res.centrZ = vec.z
        return res
    }
}