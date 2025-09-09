package sensors.cams.file

import bufferchanel.BufferedChannel
import bufferchanel.databuffer.BufferedCloudOfSensor
import clouds.Cloud
import files.serialize.KiSerializer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import sensors.cams.Cloudsorce
import sensors.cams.kinect.KinectConstant.KINECT_DEPTH_COLS
import sensors.cams.kinect.KinectConstant.KINECT_DEPTH_ROWS
import java.io.Serializable

class CloudSerializer(private val channel: BufferedChannel<BufferedCloudOfSensor>): Cloudsorce {

    private var eventThread: EventThread? = null
    private var kiSer: KiSerializer<DataSer> = KiSerializer(System.getProperty("user.dir") + "/clouds/serializedInputByte.cl")
    private var isRun = false

    override fun startDepth() {
        if (eventThread == null || !eventThread!!.isAlive()) {
            eventThread = EventThread(kiSer, channel)
            eventThread!!.start()
            isRun = true;
        }
    }

    override fun stopDepth() {
        if (eventThread != null) {
            eventThread!!.kill()
            eventThread = null
            isRun = false;
        }
    }

    override fun isRun(): Boolean {
        return isRun
    }

    suspend fun writeCloud() {
        channel.getFirstDataForRead()?.let {
            kiSer.writeObject(DataSer(
                it.source!!, it.width, it.height, it.size
            ))
            channel.endWrite(it)
        }
    }

    class DataSer(
        var source: ByteArray,
        var width: Int,
        var height: Int,
        var size: Int,
    ) : Serializable

    private class EventThread(private val kiSer: KiSerializer<DataSer>,
                              private val channel: BufferedChannel<BufferedCloudOfSensor>
    ) : Thread() {
        @Volatile
        private var alive = true
        private var dataSer: DataSer? = null

        init {
            setDaemon(true)
            setName("FilesensorEventThread")
        }

        fun kill() {
            this.alive = false
        }

        override fun run() {
            while (alive) {
                if(dataSer == null) {
                    dataSer = kiSer.readObject()
                }
                dataSer?.let {
                    runBlocking(Dispatchers.IO){
                        channel.getDataForWrite()?.let {
                            it.width = dataSer!!.width
                            it.height = dataSer!!.height
                            it.size = dataSer!!.size
                            it.source = dataSer!!.source
                            channel.endWrite(it)
                        }
                    }
                }
            }
        }
    }
}