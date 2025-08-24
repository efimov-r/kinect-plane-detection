package era.utils.count

import era.utils.files.KiLogger

class Counter {
    private var countCircle: Long = 0
    private var numOfCircle: Int = 0
    private var middleCircle: Double = 0.0

    fun appCircle() {
        countCircle++
    }

    fun setCircle(c: Long) {
        countCircle = c
    }

    fun setCircle(c: Int) {
        countCircle = c.toLong()
    }

    fun middleCircle(onIs: Int, title: String = "") {
        middleCircle = ((middleCircle*numOfCircle) + countCircle) / (numOfCircle + 1)
        numOfCircle++
        if(onIs == numOfCircle) {
            KiLogger.inf("$title ${String.format("%.2f", middleCircle)}")
            numOfCircle = 0
        }
        countCircle = 0
    }
}