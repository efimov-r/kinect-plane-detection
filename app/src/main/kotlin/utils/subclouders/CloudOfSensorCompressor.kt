package era.app.utils.subclouders


import bufferchanel.databuffer.BufferedCloudOfSensor
import clouds.CloudI
import era.utils.files.KiLogger
import era.utils.time.Span
import kotlinx.coroutines.*
import kotlin.math.min

object CloudOfSensorCompressor {
//    fun compressHexadruple(inp: BufferedCloudOfSensor) : Cloud{
//        val res = Cloud(inp.size()/16, "cloud/16")
//        if(!inp.validate()) {
//            return res
//        }
//
//        val gg = inp.width/4
//        var sum0:Int
//        var sum1:Int
//        var sum2:Int
//        var countNotNull:Int
//        var v:Int
//        for(i in 0..<inp.size()/(3*16)) {
//            val x = (i % gg)*4
//            val y = (i / gg)*4
//
//            sum0 = 0
//            sum1 = 0
//            sum2 = 0
//            countNotNull = 0
//            for(g in 0..<4) {
//                for(j in 0..<4) {
//                    v = getGlobComponent(inp, x+g, y+j, 2).toInt()
//                    if(v!=0) {
//                        sum2 += v
//                        countNotNull++
//                        sum1 += getGlobComponent(inp, x+g, y+j, 1).toInt()
//                        sum0 += getGlobComponent(inp, x+g, y+j, 0).toInt()
//                    }
//                }
//            }
//            if(countNotNull != 0) {
//                res.add(sum0/countNotNull, (sum1/countNotNull).toShort(), (sum2/countNotNull).toShort())
//            }
//        }
//        return res
//    }
//
//    //берет среднее от 4 из 16 точек, по центру
//    fun compressHexadrupleLazy(inp: BufferedCloudOfSensor) : Cloud{
//        val res = Cloud(inp.size()/16, "cloud/16")
//        if(!inp.validate()) {
//            return res
//        }
//
//        val gg = inp.width/4
//        var sum0:Int
//        var sum1:Int
//        var sum2:Int
//        var countNotNull:Int
//        var v:Int
//        var x : Int
//        var y : Int
//        for(i in 0..<inp.size()/(3*16)) {
//            x = (i % gg)*4 + 1
//            y = (i / gg)*4 + 1
//
//            sum0 = 0
//            sum1 = 0
//            sum2 = 0
//            countNotNull = 0
//
//
//            v = inp.get( (y*inp.width+x) * 3+2).toInt()
//            if(v!=0) {
//                sum2 += v
//                countNotNull++
//                sum1 += inp.get( (y*inp.width+x) * 3 + 1).toInt()
//                sum0 += inp.get( (y*inp.width+x) * 3).toInt()
//            }
//
//            y++
//            v = inp.get( (y*inp.width+x) * 3+2).toInt()
//            if(v!=0) {
//                sum2 += v
//                countNotNull++
//                sum1 += inp.get( (y*inp.width+x) * 3 + 1).toInt()
//                sum0 += inp.get( (y*inp.width+x) * 3).toInt()
//            }
//
//            x++
//            y--
//            v = inp.get( (y*inp.width+x) * 3+2).toInt()
//            if(v!=0) {
//                sum2 += v
//                countNotNull++
//                sum1 += inp.get( (y*inp.width+x) * 3 + 1).toInt()
//                sum0 += inp.get( (y*inp.width+x) * 3).toInt()
//            }
//
//            y++
//            v = inp.get( (y*inp.width+x) * 3+2).toInt()
//            if(v!=0) {
//                sum2 += v
//                countNotNull++
//                sum1 += inp.get( (y*inp.width+x) * 3 + 1).toInt()
//                sum0 += inp.get( (y*inp.width+x) * 3).toInt()
//            }
//
//
//            if(countNotNull != 0) {
//                res.add((sum0/countNotNull).toShort(), (sum1/countNotNull).toShort(), (sum2/countNotNull).toShort())
//            }
//        }
//        return res
//    }

    //берет точку одну из 2*2 из центра поля 16*16, ненулевую если она есть в поле 2*2
    suspend fun compressHexadrupleRare(inp: BufferedCloudOfSensor) : CloudI {
        val outSize = inp.size() / 16
        val res = CloudI(outSize, "cloud/16")
        if(!inp.validate()) {
            return res
        }

        val chunk = 1000
        coroutineScope {
            val jobs = mutableListOf<Deferred<CloudI>>()
            for (start in 0 until outSize step chunk) {
                val end = min(start + chunk, outSize)
                jobs += async(Dispatchers.Default) {
                    return@async do16(inp, start, end)
                }
            }

            for (cloud in jobs.awaitAll()) {
                res.addCloud(cloud)
            }
        }

        return res
    }

    private fun do16(inp: BufferedCloudOfSensor, at: Int, to: Int): CloudI {
        val res = CloudI(inp.size() / 16, "cloud/16")
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