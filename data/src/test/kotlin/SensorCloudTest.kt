import cloud.realization.BufferedChannel
import cloud.realization.CloudOfSensor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertEquals

internal class SensorCloudTest {
    @Test
    fun testWriteMess() {
        val start2 = Date().time
        val sc = BufferedChannel {
            return@BufferedChannel CloudOfSensor(640*480, it)
        }

        println(Date().time - start2)

        val start = Date().time
        runBlocking{
            launch(Dispatchers.Default) {
                for(i in 0..20) {
                    launch(Dispatchers.IO){
                        val dataForWrite = sc.getDataForWrite()

                        if(dataForWrite == null) {
//                        println("ret null for write, id: $i")
                        } else {
                            println("st, id: ${dataForWrite.getIdData()}")
                            dataForWrite.editData { glob, id ->
                                glob.y = id.toShort()
                                glob.x = (id+1).toShort()
                                glob.z = (id+2).toShort()
                            }
                            println("ed, id: ${dataForWrite.getIdData()}")
                            sc.endWrite(dataForWrite)
//                        println("Success for write, id: $i")
                        }
                    }
                    delay(60)
                }
            }

            var co = 0
            launch(Dispatchers.Default) {
                println("!START READ")
                while (co < 9) {
                    val dataForRead = sc.getFirstDataForRead()
                    if(dataForRead != null) {
                        println("!st, id: ${dataForRead.getIdData()}")
                        co++
                        dataForRead.editData { glob, id ->
                            assertEquals(glob.y, id.toShort())
                            assertEquals(glob.x, (id+1).toShort())
                            assertEquals(glob.z, (id+2).toShort())
                            assertEquals(glob.z, (id+2).toShort())
                            assertEquals(glob.z, (id+2).toShort())
                            assertEquals(glob.z, (id+2).toShort())

                        }
                        sc.endRead(dataForRead)
                        println("!ed, id: ${dataForRead.getIdData()}")
                    }
                }
            }
        }
        println(Date().time - start)




//        for(i in 0..<640*480) {
//            sc.setGlob { glob ->
//                glob.z = 1
//                glob.x = 2
//                glob.y = 3
//            }
//        }
//        sc.endSend()
//        val start = Date().time
//        sc.copyToMain()
//        println(Date().time - start)
//        sc.copyToOut()
//        sc.getGlobs { glob ->
//            assertEquals(glob.z, 1)
//        }




        assertEquals(1, 1)
    }
}