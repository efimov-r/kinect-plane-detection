package era.utils.time

import java.text.SimpleDateFormat
import java.util.Date

class Now {
    companion object {
        fun get(): Date = Date()

        fun getDateTimeStr() : String =
            SimpleDateFormat("dd.MMM.yyyy HH:mm:ss").format(get())

        fun getFileDateTimeStr() : String =
            SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(get())

        fun getDateTime(date: Date) : String =
            SimpleDateFormat("dd.MMM.yyyy HH:mm:ss").format(date)
    }
}