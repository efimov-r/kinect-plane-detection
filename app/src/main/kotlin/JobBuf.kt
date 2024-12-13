package era.app

import bufferchanel.BufferedChannel
import bufferchanel.databuffer.BufferedChannelForView
import bufferchanel.databuffer.BufferedCloudOfSensor
import clouds.ChankCloudJ
import clouds.CloudJ
import era.app.utils.neighbour.NeighbourHelper
import era.app.utils.subclouders.Chanker
import era.app.utils.subclouders.CloudOfSensorCompressor
import era.app.utils.writers.WriterCloud
import era.utils.time.Span
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import vectors.V2Vertical
import vectors.Vector3ChildedShort
import viewdata.transferdata.ToViewText

class JobBuf {
    companion object{

        val span = Span()

        suspend fun execute(
            formSensorChannel: BufferedChannel<BufferedCloudOfSensor>,
            toViewChannel: BufferedChannel<BufferedChannelForView>,
            channelText: Channel<ToViewText>
        ) {
            coroutineScope {
                formSensorChannel.getFirstDataForRead()?.let { readCloud ->
                    toViewChannel.getDataForWriteOrRewriteOld()?.let { writeCloud ->

                        val launchPoint = doJobPointToView(readCloud, writeCloud, channelText)
                        val launchCube = doJobCube(readCloud, writeCloud)

                        launchPoint.join()
                        launchCube.join()

                        toViewChannel.endWrite(writeCloud)
                    }
                    formSensorChannel.endRead(readCloud)
                }
            }
        }

        fun CoroutineScope.doJobCube(
            readCloud: BufferedCloudOfSensor, writeCloud: BufferedChannelForView
        ):Job = launch(Dispatchers.IO) {
//            span.resetTimer()
            val fastCloud = CloudOfSensorCompressor.compressHexadrupleRare(readCloud)
            val verticalVectors = createVerticalVectors(fastCloud)
//            WriterCloud.viewVerticalVectors(verticalVectors, writeCloud.lineData)

            val asyncList: ArrayList<Deferred<Vector3ChildedShort?>>  = ArrayList()

            for (createVerticalVector in verticalVectors) {
                asyncList.add(async(Dispatchers.IO) {
                    val chank = Chanker.getChank(
                        fastCloud,
                        createVerticalVector,
                        (AppConstants.STEP_CREATE_VERTICAL_VECTORS/3).toShort()
                    )
                    val i = findNearestPointCentrMaxYInChank(chank)
                    if(i == null) {
                        return@async null
                    }
                    return@async Vector3ChildedShort(
                        chank.getGlobX(i),
                        chank.getGlobY(i),
                        chank.getGlobZ(i)
                    )
                })
            }

            val v3List : ArrayList<Vector3ChildedShort> = ArrayList()
            asyncList.forEach {
                it.await()?.let { v3->
                    v3List.add(v3)
                }
            }
            NeighbourHelper.findAndFilter(v3List)
//            v3List.forEach {
//                it.createPerpendicularVectors()
//            }
            v3List.get(10).createPerpendicularVectors()

            WriterCloud.viewPerpendicular(v3List, writeCloud.lineData)


//            span.middleCircle(100, "asdsa")

            WriterCloud.writeCube(v3List, writeCloud.cubeData)
        }

        fun findNearestPointCentrMaxYInChank(chank: ChankCloudJ):Int? {
            var minLength = Int.MAX_VALUE
            var length: Int
            var result:Int? = null

            chank.forEach{x, y, z, i ->
                length = ((x-chank.centrX)*(x-chank.centrX)) + ((y-chank.maxY) * (y-chank.maxY)) + ((z-chank.centrZ) * (z-chank.centrZ))
                if(length < minLength) {
                    minLength = length
                    result = i
                }
            }

            return result
        }

        fun CoroutineScope.doJobPointToView(inp: BufferedCloudOfSensor,
                                            outp: BufferedChannelForView,
                                            channelText: Channel<ToViewText>
        ): Job = launch(Dispatchers.IO) {
            val text = "         min      max \n" +
                    "X:      ${inp.minX}    ${inp.maxX}\n" +
                    "Y:      ${inp.minY}    ${inp.maxY}\n" +
                    "Z:      ${inp.minZ}    ${inp.maxZ}"
            val compressHexadruple = CloudOfSensorCompressor.compressHexadrupleRare(inp)
            channelText.send(ToViewText(text))

            WriterCloud.writePoint(compressHexadruple, outp.pointData)
        }


        private fun createVerticalVectors(inp: CloudJ): ArrayList<V2Vertical> {
            val res = ArrayList<V2Vertical>()
            if(inp.maxX == null || inp.minX == null || inp.maxX!! - inp.minX!! < AppConstants.MIN_SIZE_CREATE_VERTICAL_VECTORS) {
                return res
            }
            if(inp.maxZ == null || inp.minZ == null || inp.maxZ!! - inp.minZ!! < AppConstants.MIN_SIZE_CREATE_VERTICAL_VECTORS) {
                return res
            }
            var curX: Short = (inp.minX!! + AppConstants.STEP_CREATE_VERTICAL_VECTORS).toShort()
            var curZ: Short
            while (curX <= inp.maxX!!) {
                curZ = (inp.minZ!! + AppConstants.STEP_CREATE_VERTICAL_VECTORS).toShort()
                while(curZ <= inp.maxZ!!) {
                    res.add(V2Vertical(curX, curZ))
                    curZ = (curZ + AppConstants.STEP_CREATE_VERTICAL_VECTORS).toShort()
                }
                curX = (curX + AppConstants.STEP_CREATE_VERTICAL_VECTORS).toShort()
            }
            return res
        }
    }
}