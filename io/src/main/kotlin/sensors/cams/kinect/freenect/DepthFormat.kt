package sensors.cams.kinect.freenect

enum class DepthFormat(private val value: Int) {
    D11BIT(0),
    D10BIT(1),
    D11BIT_PACKED(2),
    D10BIT_PACKED(3),
    REGISTERED(4),
    MM(5);

    fun intValue(): Int {
        return value
    }

    companion object {
        private val MAP: MutableMap<Int?, DepthFormat?> = HashMap<Int?, DepthFormat?>(6)

        init {
            for (v in DepthFormat.entries) {
                MAP.put(v.intValue(), v)
            }
        }

        @JvmStatic
        fun fromInt(value: Int): DepthFormat? {
            return MAP.get(value)
        }
    }
}