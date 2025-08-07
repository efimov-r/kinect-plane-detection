import elementlist.ShortList
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class ShortListJTest {

    @Test
    fun testResizeList() {
        try {
            val list = ShortList(10, "test cloud")
            for(i in 0..10000) {
                list.add(i.toShort())
            }

            for(i in 0..10000) {
                assertEquals(list.get(i), i.toShort())
            }
        } catch (ex:Exception) {
            println(ex.message)
        }
    }
}