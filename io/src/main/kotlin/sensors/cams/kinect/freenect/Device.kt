package sensors.cams.kinect.freenect

interface Device {
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