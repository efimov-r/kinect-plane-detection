package era.app

import bufferchanel.BufferedChannel
import bufferchanel.databuffer.BufferedChannelForView
import bufferchanel.databuffer.BufferedCloudOfSensor
import viewdata.transferdata.ToViewText
import clouds.CloudJ
import era.app.utils.subclouders.VerticalSubclouder
import era.utils.time.Span
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import vectors.V2Vertical
import vectors.V3

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
            readCloud: CloudJ, writeCloud: BufferedChannelForView
        ):Job = launch(Dispatchers.IO) {
            val verticalVectors = createVerticalVectors(readCloud)
//                                    viewVerticalVectors(writeCloud, verticalVectors)
            writeCloud.cubeData.clear()
//                                    KiLogger.inf("size: ${verticalVectors.size}")

            val asyncList: ArrayList<Deferred<V3<Float>?>>  = ArrayList()

            for (createVerticalVector in verticalVectors) {
                asyncList.add(async(Dispatchers.IO) {
                    val stolb = VerticalSubclouder.getStolb(
                        readCloud,
                        createVerticalVector,
                        AppConstants.STEP_CREATE_VERTICAL_VECTORS
                    )
                    stolb.getMaxY()?.let { maxY ->
                        return@async V3(
                            x = createVerticalVector.x.toFloat() / AppConstants.MAX_MM_ON_V3D,
                            y = maxY.toFloat() / AppConstants.MAX_MM_ON_V3D,
                            z = createVerticalVector.z.toFloat() / AppConstants.MAX_MM_ON_V3D
                        )
                    }
                })
            }

            asyncList.forEach{
                it.await()?.let{ v3 ->
                    writeCloud.cubeData.add {
                        it.color = 1f
                        it.y = v3.y
                        it.x = v3.x
                        it.z = v3.z
                    }
                }
            }
        }

        fun CoroutineScope.doJobPointToView(inp: CloudJ,
                                            outp: BufferedChannelForView,
                                            channelText: Channel<ToViewText>
        ): Job = launch(Dispatchers.IO) {
            outp.pointData.clear()
            val text = "         min      max \n" +
                    "X:      ${inp.minX}    ${inp.maxX}\n" +
                    "Y:      ${inp.minY}    ${inp.maxY}\n" +
                    "Z:      ${inp.minZ}    ${inp.maxZ}"
            channelText.send(ToViewText(text))
            inp.forEach { x, y, z ->
                outp.pointData.add {
                    it.z = z.toFloat()/ AppConstants.MAX_MM_ON_V3D
                    it.x = x.toFloat()/ AppConstants.MAX_MM_ON_V3D
                    it.y = y.toFloat()/ AppConstants.MAX_MM_ON_V3D
                }
            }
        }


        private fun createVerticalVectors(inp: CloudJ): ArrayList<V2Vertical> {
            val res = ArrayList<V2Vertical>()
            if(inp.maxX == null || inp.minX == null || inp.maxX!! - inp.minX!! < AppConstants.MIN_SIZE_CREATE_VERTICAL_VECTORS) {
                return res
            }
            if(inp.maxZ == null || inp.minZ == null || inp.maxZ!! - inp.minZ!! < AppConstants.MIN_SIZE_CREATE_VERTICAL_VECTORS) {
                return res
            }
            var curX: Short = inp.minX!!
            while (curX <= inp.maxX!!) {
                var curZ: Short = inp.minZ!!
                while(curZ <= inp.maxZ!!) {
                    res.add(V2Vertical(curX, curZ))
                    curZ = (curZ + AppConstants.STEP_CREATE_VERTICAL_VECTORS).toShort()
                }
                curX = (curX + AppConstants.STEP_CREATE_VERTICAL_VECTORS).toShort()
            }
            return res
        }

        private fun viewVerticalVectors(outp: BufferedChannelForView, inp: ArrayList<V2Vertical>) {
            outp.lineData.clear()
            for (v2 in inp) {
                outp.lineData.add {
                    it.v1.y = 1f
                    it.v1.x = v2.x.toFloat() / AppConstants.MAX_MM_ON_V3D
                    it.v1.z = v2.z.toFloat() / AppConstants.MAX_MM_ON_V3D
                    it.v1.color = 1f

                    it.v2.y = -1f
                    it.v2.x = v2.x.toFloat() / AppConstants.MAX_MM_ON_V3D
                    it.v2.z = v2.z.toFloat() / AppConstants.MAX_MM_ON_V3D
                    it.v2.color = 1f
                }
            }
        }
    }
}