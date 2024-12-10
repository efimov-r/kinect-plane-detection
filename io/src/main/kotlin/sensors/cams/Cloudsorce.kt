package sensors.cams

interface Cloudsorce {
    fun startDepth()
    fun stopDepth()
    fun isRun():Boolean
}