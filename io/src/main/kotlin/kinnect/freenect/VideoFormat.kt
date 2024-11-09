package kinnect.freenect

enum class VideoFormat(private val value: Int) {
    RGB(0),
    BAYER(1),
    IR_8BIT(2),
    IR_10BIT(3),
    IR_10BIT_PACKED(4),
    YUV_RGB(5),
    YUV_RAW(6);

    fun intValue(): Int {
        return value
    }

    companion object {
        private val MAP: MutableMap<Int?, VideoFormat?> = HashMap<Int?, VideoFormat?>(7)

        init {
            for (v in VideoFormat.entries) {
                MAP.put(v.intValue(), v)
            }
        }

        fun fromInt(value: Int): VideoFormat? {
            return MAP.get(value)
        }
    }
}