package cloud.databuffer

import cloud.entities.Glob
import java.io.Serializable

class CloudOfSensor(size: Int, val id: Int) : DataForBufferedChannel, Serializable {
    private val data: Array<Glob> = Array(size) { Glob.createEmpty() }
    var minX: Short = 0
    var maxX: Short = 0
    var minY: Short = 0
    var maxY: Short = 0
    var minZ: Short = 0
    var maxZ: Short = 0

    fun editData(callback: (glob: Glob, id: Int) -> Unit) {
        for(i in 0..<data.size) {
            callback(data[i], i)
        }
    }

    private fun getGlob(i: Int):Glob = data.get(i)

    override fun getIdData(): Int {
        return id
    }

    fun copyOf(inp: CloudOfSensor) {
        editData{ glob, i ->
            glob.copyOf(inp.getGlob(i))
        }
        this.minX = inp.minX
        this.maxX = inp.maxX
        this.minY = inp.minY
        this.maxY = inp.maxY
        this.minZ = inp.minZ
        this.maxZ = inp.maxZ
    }
}