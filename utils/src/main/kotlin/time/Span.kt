package era.utils.time

import era.utils.files.KiLogger
import java.util.Date

class Span {
    private var dateCircle: Date = Now.get()
    private var numOfCircle: Int = 0
    private var middleCircle: Double = 0.0

    fun resetTimer() {
        dateCircle = Now.get()
    }

    fun middleCircle(onIs: Int, title: String = "") {
        val cir = Now.get().time - dateCircle.time
        middleCircle = ((middleCircle*numOfCircle) + cir) / (numOfCircle + 1)
        numOfCircle++
        if(onIs == numOfCircle) {
            KiLogger.inf("$title ${String.format("%.2f", middleCircle)}")
            numOfCircle = 0
        }
        dateCircle = Now.get()
    }
}