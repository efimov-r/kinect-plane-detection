package era.app

import cloud.entities.ToViewText
import cloud.BufferedChannel
import cloud.databuffer.CloudForView
import cloud.databuffer.CloudOfSensor
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
            formSensorChannel: BufferedChannel<CloudOfSensor>,
            toViewChannel: BufferedChannel<CloudForView>,
            channelText: Channel<ToViewText>
        ) {
            coroutineScope {
                formSensorChannel.getFirstDataForRead()?.let { readCloud ->
                    toViewChannel.getDataForWriteOrRewriteOld()?.let { writeCloud ->

                        val launchPoint = doJobPointToView(readCloud, writeCloud, channelText)
//                        val launchCube = doJobCube(readCloud, writeCloud)

                        launchPoint.join()
//                        launchCube.join()

                        toViewChannel.endWrite(writeCloud)
                    }
                    formSensorChannel.endRead(readCloud)
                }
            }
        }

        fun CoroutineScope.doJobCube(
            readCloud: CloudOfSensor, writeCloud: CloudForView
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

        fun CoroutineScope.doJobPointToView(inp: CloudOfSensor,
                                             outp: CloudForView,
                                             channelText: Channel<ToViewText>
        ): Job = launch(Dispatchers.IO) {
            outp.pointData.clear()
            val text = "         min      max \n" +
                    "X:      ${inp.minX}    ${inp.maxX}\n" +
                    "Y:      ${inp.minY}    ${inp.maxY}\n" +
                    "Z:      ${inp.minZ}    ${inp.maxZ}"
            channelText.send(ToViewText(text))
            inp.editData { glob, id ->
                outp.pointData.add {
                    it.z = glob.z.toFloat()/ AppConstants.MAX_MM_ON_V3D
                    it.x = glob.x.toFloat()/ AppConstants.MAX_MM_ON_V3D
                    it.y = glob.y.toFloat()/ AppConstants.MAX_MM_ON_V3D
                }
            }
        }


        private fun createVerticalVectors(inp: CloudOfSensor): ArrayList<V2Vertical> {
            val res = ArrayList<V2Vertical>()
            if(inp.maxX - inp.minX < AppConstants.MIN_SIZE_CREATE_VERTICAL_VECTORS) {
                return res
            }
            if(inp.maxZ - inp.minZ < AppConstants.MIN_SIZE_CREATE_VERTICAL_VECTORS) {
                return res
            }
            var curX: Short = inp.minX
            while (curX <= inp.maxX) {
                var curZ: Short = inp.minZ
                while(curZ <= inp.maxZ) {
                    res.add(V2Vertical(curX, curZ))
                    curZ = (curZ + AppConstants.STEP_CREATE_VERTICAL_VECTORS).toShort()
                }
                curX = (curX + AppConstants.STEP_CREATE_VERTICAL_VECTORS).toShort()
            }
            return res
        }

        private fun viewVerticalVectors(outp: CloudForView, inp: ArrayList<V2Vertical>) {
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