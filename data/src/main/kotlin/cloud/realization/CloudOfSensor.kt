package cloud.realization

import era.kinect.cloud.Glob

class CloudOfSensor(size: Int, val id: Int) : DataForBufferedChannel {
    private val data: Array<Glob> = Array(size) { Glob.createEmpty() }

    init {
        for(i in 0..<data.size) {
            data[i] = Glob.createEmpty()
        }
    }

    fun editData(callback: (glob: Glob, id: Int) -> Unit) {
        for(i in 0..<data.size) {
            callback(data[i], i)
        }
    }

    fun setDataGlob(i: Int, x: Short, y: Short, z: Short) {
        data[i].x = x
        data[i].y = y
        data[i].z = z
    }

    override fun getIdData(): Int {
        return id
    }
}