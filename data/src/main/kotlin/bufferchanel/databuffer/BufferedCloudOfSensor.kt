package bufferchanel.databuffer

class BufferedCloudOfSensor(name: String = "",
                            val id: Int) : DataForBufferedChannel {

    var width: Int = 0
    var height: Int = 0
    var size: Int = 0
    var source: ByteArray? = null

    override fun getIdData(): Int {
        return id
    }

    override fun validate(): Boolean {
        return width*height == size
    }
}