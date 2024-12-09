package sensors.cams.file

import cloud.BufferedChannel
import cloud.databuffer.CloudOfSensor
import files.KiSerializer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import sensors.cams.Cloudsorce

class CloudSerializer(private val channel: BufferedChannel<CloudOfSensor>): Cloudsorce {

    private var eventThread: EventThread? = null
    private var kiSer: KiSerializer<CloudOfSensor> = KiSerializer("clouds/serializedInputCloud.cl")

    override fun startDepth() {
        if (eventThread == null || !eventThread!!.isAlive()) {
            eventThread = EventThread(kiSer, channel)
            eventThread!!.start()
        }
    }

    override fun stopDepth() {
        if (eventThread != null) {
            eventThread!!.kill()
            eventThread = null
        }
    }

    fun writeCloud(cloud: CloudOfSensor) {
        kiSer.writeObject(cloud)
    }

    private class EventThread(private val kiSer: KiSerializer<CloudOfSensor>,
                              private val channel: BufferedChannel<CloudOfSensor>) : Thread() {
        @Volatile
        private var alive = true
        private var fileCloud: CloudOfSensor? = null

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
                            it.copyOf(fileCloud!!)
                            channel.endWrite(it)
                        }
                    }
                }
            }
        }
    }
}