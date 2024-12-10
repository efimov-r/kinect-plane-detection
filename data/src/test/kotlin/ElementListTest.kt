import viewdata.elementlist.ElementList
import viewdata.elementlist.elements.Point
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertEquals

internal class ElementListTest {

    @Test
    fun testNotUpdated() {
        val myArr = ElementList{
            return@ElementList Point.createEmpty()
        }

        assertEquals(myArr.isUpdated(), false)

        myArr.add{}

        assertEquals(myArr.isUpdated(), true)

        myArr.forEach { glob, i ->

        }

        assertEquals(myArr.isUpdated(), true)

        myArr.endRead()

        assertEquals(myArr.isUpdated(), false)

        myArr.forEach { glob, i ->

        }

        assertEquals(myArr.isUpdated(), true)

        myArr.endRead()

        assertEquals(myArr.isUpdated(), false)

        myArr.trimToSize()
        myArr.ensureCapacity(20)

        assertEquals(myArr.isUpdated(), false)
    }

    @Test
    fun testOutOfMemory() {
        try {
            val myArr = ElementList{
                return@ElementList Point.createEmpty()
            }
            val start = Date().time
            for(j in 0..10) {

                myArr.clear()
                myArr.trimToSize()

                for(i in 0..9_000_000) {
                    myArr.add{
                        it.z = i.toFloat()
                        it.x = (i+1).toFloat()
                        it.y = (i+2 + j).toFloat()
                    }
                }

                var ii = 0
                myArr.forEach { glob, i ->
                    assertEquals(glob.z, ii.toFloat())
                    assertEquals(glob.x, (ii+1).toFloat())
                    assertEquals(glob.y, (ii+2 + j).toFloat())
                    ii++
                }

            }

            println(Date().time - start)

        } catch (ex:Exception) {
            println(ex.message)
        }
    }
}