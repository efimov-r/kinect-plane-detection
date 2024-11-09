package kinnect.freenect

interface Device {
    val accel: DoubleArray
    fun setLed(status: LedStatus?): Int
    fun refreshTiltState()
    val tiltAngle: Double
    fun setTiltAngle(angle: Double): Int
    val tiltStatus: TiltStatus?
    fun setDepthFormat(fmt: DepthFormat?)
    fun setVideoFormat(fmt: VideoFormat?)
    fun setDepthFormat(fmt: DepthFormat?, res: Resolution?)
    fun setVideoFormat(fmt: VideoFormat?, res: Resolution?)
    val depthMode: FrameMode?
    val videoMode: FrameMode?
    fun startDepth(handler: DepthHandler?): Int
    fun startVideo(handler: VideoHandler?): Int
    fun stopDepth(): Int
    fun stopVideo(): Int
    fun close()
    val deviceIndex: Int
}