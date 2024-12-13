package bufferchanel.databuffer

import clouds.CloudJ

class BufferedCloudOfSensor(initialCapacity: Int = 10,
                            name: String = "",
                            val id: Int) : CloudJ(initialCapacity, name), DataForBufferedChannel {

    var width: Int = 0
    var height: Int = 0

    override fun getIdData(): Int {
        return id
    }

    override fun validate(): Boolean {
        return width*height*3 == size()
    }
}