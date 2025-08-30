package era.app.utils.subclouders


import bufferchanel.databuffer.BufferedCloudOfSensor
import clouds.Cloud
import kotlinx.coroutines.*
import utils.Threadripper.threadripp
import kotlin.math.min

object CloudOfSensorCompressor {

    //берет точку одну из 2*2 из центра поля 16*16, ненулевую если она есть в поле 2*2
    suspend fun CoroutineScope.compressHexadrupleRare(inp: BufferedCloudOfSensor, countOfThread: Int) : Cloud {
        val outSize = inp.size / 16
        val res = Cloud(outSize, "cloud/16")
        if(!inp.validate()) {
            return res
        }

        val chunk = (outSize + countOfThread - 1) / countOfThread
        val jobs = mutableListOf<Deferred<Cloud>>()
        for (start in 0 until outSize step chunk) {
            val end = min(start + chunk, outSize)
            jobs += async(Dispatchers.Default) {
                return@async do16(inp, start, end)
            }
        }

        for (cloud in jobs.awaitAll()) {
            res.add(cloud)
        }


        return res
    }

    private fun do16(inp: BufferedCloudOfSensor, at: Int, to: Int): Cloud {
        val res = Cloud(inp.size / 16, "cloud/16")
        val gg = inp.width/4
        var v : Float
        var x : Int
        var y : Int
        var c: Int
        for(i in at until to) {
            x = (i % gg)*4 + 1
            y = (i / gg)*4 + 1

            c = y*inp.width+x
            v = inp.getZ(c)
            if(v!=0f) {
                res.add(inp.getX(c), inp.getY (c), v)
                continue
            }

            c += inp.width //y++
            v = inp.getZ(c)
            if(v!=0f) {
                res.add(inp.getX(c), inp.getY (c), v)
                continue
            }

            c = c - inp.width + 1 // y-- x++
            v = inp.getZ(c)
            if(v!=0f) {
                res.add(inp.getX(c), inp.getY (c), v)
                continue
            }

            c += inp.width //y++
            v = inp.getZ(c)
            if(v!=0f) {
                res.add(inp.getX(c), inp.getY (c), v)
                continue
            }
        }
        return res
    }
}