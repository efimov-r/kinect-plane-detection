package bufferchanel.databuffer

import clouds.CloudJ

class BufferedCloudOfSensor(initialCapacity: Int = 10, name: String = "", val id: Int) : CloudJ(initialCapacity, name), DataForBufferedChannel {
    override fun getIdData(): Int {
        return id
    }
}