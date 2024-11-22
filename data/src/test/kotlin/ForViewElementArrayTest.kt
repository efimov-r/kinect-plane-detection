import cloud.entities.forview.ForViewElementArray
import cloud.entities.forview.Point
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertEquals

internal class ForViewElementArrayTest {

    @Test
    fun testNotUpdated() {
        val myArr = ForViewElementArray{
            return@ForViewElementArray Point.createEmpty()
        }

        assertEquals(myArr.isUpdated(), false)

        myArr.add{}

        assertEquals(myArr.isUpdated(), true)

        myArr.forEach {  }

        assertEquals(myArr.isUpdated(), true)

        myArr.endRead()

        assertEquals(myArr.isUpdated(), false)

        myArr.forEach {  }

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
            val myArr = ForViewElementArray{
                return@ForViewElementArray Point.createEmpty()
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
                myArr.forEach {
                    assertEquals(it.z, ii.toFloat())
                    assertEquals(it.x, (ii+1).toFloat())
                    assertEquals(it.y, (ii+2 + j).toFloat())
                    ii++
                }

            }

            println(Date().time - start)

        } catch (ex:Exception) {
            println(ex.message)
        }
    }
}