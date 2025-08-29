package era.app.jobs

import bufferchanel.BufferedChannel
import bufferchanel.databuffer.BufferedChannelForView
import bufferchanel.databuffer.BufferedCloudOfSensor
import era.app.jobs.Plainer.create
import era.app.utils.subclouders.CloudOfSensorCompressor
import era.utils.time.Span
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
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


                    create(readCloud, writeCloud)

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
//                    writeCloud.pointData.ofCloud(fastCloud, 0f, AppConstants.AD_MAX_MM_ON_V3D, 10)
                    span.middleCircle(100, "All")
//                    delay(30)

                    toViewChannel.endWrite(writeCloud)
                }
                formSensorChannel.endRead(readCloud)
            }
        }
    }



}