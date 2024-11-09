package kinnect.freenect

enum class TiltStatus(private val value: Int) {
    STOPPED(0),
    LIMIT(1),
    MOVING(4);

    fun intValue(): Int {
        return value
    }

    companion object {
        private val MAP: MutableMap<Int?, TiltStatus?> = HashMap<Int?, TiltStatus?>(3)

        init {
            for (v in TiltStatus.entries) {
                MAP.put(v.intValue(), v)
            }
        }

        fun fromInt(value: Int): TiltStatus? {
            return MAP.get(value)
        }
    }
}