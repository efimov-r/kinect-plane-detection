package era.app.utils.subclouders

import clouds.ChankCloudJ
import clouds.CloudJ
import vectors.V2Vertical

object Chanker {
    fun getChank(inp: CloudJ, vec: V2Vertical, halfSize: Short): ChankCloudJ {
        val xMin = vec.x - halfSize
        val xMax = vec.x + halfSize
        val zMin = vec.z - halfSize
        val zMax = vec.z + halfSize

        // Добавляем точки в облако
        val res = ChankCloudJ(300, "Stolb cloud, $vec")
        inp.forEach{x, y, z ->
            if (x in xMin..xMax && z in zMin..zMax) {
                res.addGlob(x, y, z)
            }
        }

        // Устанавливаем центр
        res.centrX = vec.x
        res.centrZ = vec.z

        return res
    }
}