package sensors.cams.file

import bufferchanel.BufferedChannel
import bufferchanel.databuffer.BufferedCloudOfSensor
import clouds.CloudJ
import files.serialize.KiSerializer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import sensors.cams.Cloudsorce
import serialize.ShortArraySerialize

class CloudSerializer(private val channel: BufferedChannel<BufferedCloudOfSensor>): Cloudsorce {

    private var eventThread: EventThread? = null
    private var kiSer: KiSerializer<ShortArraySerialize> = KiSerializer("clouds/serializedInputCloud.cl")
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

    fun writeCloud(cloud: BufferedCloudOfSensor) {
        kiSer.writeObject(cloud.objForSerialize)
    }

    private class EventThread(private val kiSer: KiSerializer<ShortArraySerialize>,
                              private val channel: BufferedChannel<BufferedCloudOfSensor>
    ) : Thread() {
        @Volatile
        private var alive = true
        private var fileCloud: ShortArraySerialize? = null

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
                            it.copyOf(fileCloud)
                            channel.endWrite(it)
                        }
                    }
                }
            }
        }
    }
}