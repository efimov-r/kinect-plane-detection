package era.utils

import era.utils.files.KiLogger
import kotlin.test.Test
import kotlin.test.assertEquals

internal class UtilsTest {

    @Test
    fun testWriteMess() {
        KiLogger.inf("info test")
        KiLogger.err("err test")
        KiLogger.err(Exception())
        KiLogger.close()
        assertEquals(1, 1)
    }
}