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

class CloudSerializer(private val channel: BufferedChannel<BufferedCloudOfSensor>): Cloudsorce {

    private var eventThread: EventThread? = null
    private var kiSer: KiSerializer<Cloud> = KiSerializer(System.getProperty("user.dir") + "/clouds/serializedInputCloud.cl")
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
            kiSer.writeObject(it)
            channel.endWrite(it)
        }
    }

    private class EventThread(private val kiSer: KiSerializer<Cloud>,
                              private val channel: BufferedChannel<BufferedCloudOfSensor>
    ) : Thread() {
        @Volatile
        private var alive = true
        private var fileCloud: Cloud? = null

        init {
            setDaemon(true)
            setName("FilesensorEventThread")
        }

        fun kill() {
            this.alive = false
        }

        override fun run() {
            while (alive) {
                if(fileCloud == null) {
                    fileCloud = kiSer.readObject()
                }
                fileCloud?.let {
                    runBlocking(Dispatchers.IO){
                        channel.getDataForWrite()?.let {
                            it.width = KINECT_DEPTH_COLS
                            it.height = KINECT_DEPTH_ROWS
                            it.copyOf(fileCloud)
                            it.minMaxDo()
                            channel.endWrite(it)
                        }
                    }
                }
            }
        }
    }
}