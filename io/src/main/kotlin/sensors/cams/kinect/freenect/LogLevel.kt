package sensors.cams.kinect.freenect

enum class LogLevel(private val value: Int) {
    FATAL(0),
    ERROR(1),
    WARNING(2),
    NOTICE(3),
    INFO(4),
    DEBUG(5),
    SPEW(6),
    FLOOD(7);

    fun intValue(): Int {
        return value
    }

    companion object {
        private val MAP: MutableMap<Int?, LogLevel?> = HashMap<Int?, LogLevel?>(8)

        init {
            for (v in LogLevel.entries) {
                MAP.put(v.intValue(), v)
            }
        }

        fun fromInt(value: Int): LogLevel? {
            return MAP.get(value)
        }
    }
}