package bufferchanel.databuffer

interface DataForBufferedChannel {
    fun getIdData(): Int
    fun validate(): Boolean
}