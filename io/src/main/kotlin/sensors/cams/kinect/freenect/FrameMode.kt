package sensors.cams.kinect.freenect

import com.sun.jna.Structure


open class FrameMode : Structure() {
    /* All fields are public because Structure requires it.
       However, fields should NOT be altered by external code. */
    @JvmField var reserved: Int = 0
    @JvmField var resolution: Int = 0
    @JvmField var format: Int = 0
    @JvmField var bytes: Int = 0
    @JvmField var width: Short = 0
    @JvmField var height: Short = 0
    @JvmField var dataBitsPerPixel: Byte = 0
    @JvmField var paddingBitsPerPixel: Byte = 0
    @JvmField var framerate: Byte = 0
    @JvmField var valid: Byte = 0

    override fun getFieldOrder(): List<String> {
        return listOf(
            "reserved",
            "resolution",
            "format",
            "bytes",
            "width",
            "height",
            "dataBitsPerPixel",
            "paddingBitsPerPixel",
            "framerate",
            "valid"
        )
    }

    fun getResolution(): Resolution? {
        return Resolution.fromInt(resolution)
    }

    val frameSize: Int
        get() = bytes

    val depthFormat: DepthFormat?
        get() = DepthFormat.fromInt(format)

    val videoFormat: VideoFormat?
        get() = VideoFormat.fromInt(format)

    val frameRate: Int
        get() = framerate.toInt()

    fun isValid(): Boolean {
        return (valid.toInt() != 0)
    }

    class ByValue : FrameMode(), Structure.ByValue
}
