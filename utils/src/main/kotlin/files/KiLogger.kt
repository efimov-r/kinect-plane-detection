package era.utils.files

import era.utils.time.Now

class KiLogger private constructor(fileName: String) {
    private var writer: java.io.BufferedWriter? = null

    init {
        try {
            val logDir = java.io.File("log")
            if (!logDir.exists()) {
                logDir.mkdir() // Создаем папку log, если она не существует
            }
            val logFile = java.io.File(logDir, fileName)
            writer = java.io.BufferedWriter(java.io.FileWriter(logFile, true)) // true для добавления в конец файла
        } catch (e: java.io.IOException) {
            e.printStackTrace()
        }
    }

    private fun writeLn(str: String) {
        try {
            writer!!.write(str)
            writer!!.newLine() // Переход на новую строку
        } catch (e: java.io.IOException) {
            e.printStackTrace()
        }
    }

    private fun flush() {
        try {
            writer!!.flush() // Сбрасываем буфер
        } catch (e: java.io.IOException) {
            e.printStackTrace()
        }
    }

    private fun closeWriter() {
        try {
            writer?.close()
        } catch (e: java.io.IOException) {
            e.printStackTrace()
        }
    }

    companion object {
        private var instance: KiLogger? = null
        fun inf(inf: String) {
            if (instance == null) {
                instance = KiLogger("${Now.getDateTimeStr()}.log")
            }
            val log = "INF: ${Now.getDateTimeStr()}: $inf"
            instance!!.writeLn(log)
            instance!!.flush()
            println(log)
        }

        fun devInfo(dev: String, inf: String) {
            inf("device $dev: $inf")
        }

        fun err(err: kotlin.String) {
            if (instance == null) {
                instance = KiLogger("${Now.getDateTimeStr()}.log")
            }
            val log = "ERR: ${Now.getDateTimeStr()}: $err"
            instance!!.writeLn(log)
            instance!!.flush()
            println(log)
        }

        fun err(ex: java.lang.Exception) {
            if (instance == null) {
                instance = KiLogger("${Now.getDateTimeStr()}.log")
            }
            val log = "ERR: ${Now.getDateTimeStr()}: ${ex.message}"
            instance!!.writeLn(log)
            println(log)
            for (stackTraceElement in ex.getStackTrace()) {
                val errStr = "          $stackTraceElement"
                instance!!.writeLn(errStr)
                println(errStr)
            }
            instance!!.flush()
        }

        fun close() {
            if (instance != null) {
                instance!!.writeLn("---------- Closed this file! ----------")
                instance!!.closeWriter()
                instance = null
            }
        }
    }
}