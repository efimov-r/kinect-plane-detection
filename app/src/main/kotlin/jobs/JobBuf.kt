package era.app.jobs

import bufferchanel.BufferedChannel
import bufferchanel.databuffer.BufferedChannelForView
import bufferchanel.databuffer.BufferedCloudOfSensor
import era.app.AppConstants
import era.app.jobs.JobAll.doJobAll
import era.app.utils.subclouders.CloudOfSensorCompressor
import era.utils.time.Span
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import viewdata.transferdata.ToViewText

object JobBuf {

    val span = Span()
    val span2 = Span()
    val span3 = Span()

    suspend fun execute(
        formSensorChannel: BufferedChannel<BufferedCloudOfSensor>,
        toViewChannel: BufferedChannel<BufferedChannelForView>,
        channelText: Channel<ToViewText>
    ) {
        coroutineScope {
            formSensorChannel.getFirstDataForRead()?.let { readCloud ->
                toViewChannel.getDataForWriteOrRewriteOld()?.let { writeCloud ->
                    span.resetTimer()


                    writeCloud.cubeData.clear()
                    writeCloud.lineData.clear()
                    writeCloud.pointData.clear()
                    writeCloud.triangleData.clear()


                    val text = "         min      max \n" +
                            "X:      ${readCloud.minX()}    ${readCloud.maxX()}\n" +
                            "Y:      ${readCloud.minY()}    ${readCloud.maxY()}\n" +
                            "Z:      ${readCloud.minZ()}    ${readCloud.maxZ()}"

                    channelText.send(ToViewText(text))

                    val fastCloud = CloudOfSensorCompressor.compressHexadrupleRare(readCloud)

                    doJobAll(fastCloud, writeCloud).join()

//                    val vectorP = doJobChank(fastCloud, writeCloud).await()
//
//                    if (vectorP != null) {
//                        WriterCloud.viewNullVector(vectorP, writeCloud.lineData, 0f)
//
//                        val matrix = rotationMatrixToY(Vector3Float(vectorP.x, vectorP.y, vectorP.z))
//                        val newP = rotateVector(vectorP, matrix)
//                        WriterCloud.viewNullVector(newP, writeCloud.lineData, 1f)
//
////                            readCloud.transformSIMD(matrix, 10)
//                    }
//                        writeCloud.pointData.ofCloud(readCloud, 0f, AppConstants.AD_MAX_MM_ON_V3D, 10)
                    writeCloud.pointData.ofCloud(fastCloud, 0f, AppConstants.AD_MAX_MM_ON_V3D, 10)
                    span.middleCircle(100, "All")
//                    delay(30)

                    toViewChannel.endWrite(writeCloud)
                }
                formSensorChannel.endRead(readCloud)
            }
        }
    }



}