package kinect.freenect

enum class Resolution(private val value: Int) {
    LOW(0),
    MEDIUM(1),
    HIGH(2);

    fun intValue(): Int {
        return value
    }

    companion object {
        private val MAP: MutableMap<Int?, Resolution?> = HashMap<Int?, Resolution?>(3)

        init {
            for (v in Resolution.entries) {
                MAP.put(v.intValue(), v)
            }
        }

        fun fromInt(value: Int): Resolution? {
            return MAP.get(value)
        }
    }
}