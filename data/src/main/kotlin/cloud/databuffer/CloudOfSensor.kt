package cloud.databuffer

import cloud.entities.Glob

class CloudOfSensor(size: Int, val id: Int) : DataForBufferedChannel {
    private val data: Array<Glob> = Array(size) { Glob.createEmpty() }

//    init {
//        for(i in 0..<data.size) {
//            data[i] = Glob.createEmpty()
//        }
//    }

    fun editData(callback: (glob: Glob, id: Int) -> Unit) {
        for(i in 0..<data.size) {
            callback(data[i], i)
        }
    }

    override fun getIdData(): Int {
        return id
    }
}