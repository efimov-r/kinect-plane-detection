package kinect.freenect

fun interface LogHandler {
    fun onMessage(dev: Device?, level: LogLevel?, msg: String?)
}