package era.app

import bufferchanel.BufferedChannel
import bufferchanel.databuffer.BufferedChannelForView
import bufferchanel.databuffer.BufferedCloudOfSensor
import clouds.ChankCloudJ
import clouds.CloudJ
import era.app.AppConstants.COS_GROUP_PERPENDIC
import era.app.utils.neighbour.NeighbourHelper
import era.app.utils.subclouders.Chanker
import era.app.utils.subclouders.CloudOfSensorCompressor
import era.app.utils.writers.WriterCloud
import era.utils.files.KiLogger
import era.utils.time.Span
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import vectors.V2Vertical
import vectors.Vector3
import vectors.Vector3ChildedShort
import viewdata.transferdata.ToViewText
import java.util.Date

class JobBuf {
    companion object{

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
                                "X:      ${readCloud.minX}    ${readCloud.maxX}\n" +
                                "Y:      ${readCloud.minY}    ${readCloud.maxY}\n" +
                                "Z:      ${readCloud.minZ}    ${readCloud.maxZ}"
                        channelText.send(ToViewText(text))


                        val fastCloud = CloudOfSensorCompressor.compressHexadrupleRare(readCloud)

                        listOf(
                            doJobPointToView(fastCloud, writeCloud),
                            doJobCube(fastCloud, writeCloud)
                        ).joinAll()
                        span.middleCircle(1000, "All")

                        toViewChannel.endWrite(writeCloud)
                    }
                    formSensorChannel.endRead(readCloud)
                }
            }
        }

        fun CoroutineScope.doJobCube(
            fastCloud: CloudJ, writeCloud: BufferedChannelForView
        ):Job = launch(Dispatchers.IO) {
//            span.resetTimer()
            span3.resetTimer()
            val verticalVectors = createVerticalVectors(fastCloud)

//            WriterCloud.viewVerticalVectors(verticalVectors, writeCloud.lineData)
//            writeCloud.lineData.clear()

            val v3List = verticalVectors.map { verticalVector ->
                async(Dispatchers.IO) {
                    val chank = Chanker.getChank(fastCloud, verticalVector, (AppConstants.STEP_CREATE_VERTICAL_VECTORS / 3).toShort())
                    findNearestPointCentrSrYInChank(chank)?.let {
                        Vector3ChildedShort(it.x, it.y, it.z)
                    }

                }
            }.awaitAll().filterNotNull().toMutableList()
            span3.middleCircle(1000, "chank")

            NeighbourHelper.findAndFilter(v3List)

            span2.resetTimer()
            v3List.forEach { v ->
                v.createPerpendicularVectors()
                v.filterPerpendicularVectors()
                v.groupPerpendicularVectors(COS_GROUP_PERPENDIC)
                WriterCloud.viewPerpendicular(v, writeCloud.lineData)
            }
            span2.middleCircle(1000, "v3List")



//            span.middleCircle(100, "asdsa")

//            WriterCloud.writeCubes(v3List, writeCloud.cubeData)
//            WriterCloud.writeCubeWithChild(v3List.get(10), writeCloud.cubeData)
        }

        fun findNearestPointCentrSrYInChank(chank: ChankCloudJ): Vector3<Short>? {
            var minLength = Int.MAX_VALUE
            var length: Int
            var result:Vector3<Short>? = null

            chank.forEach{x, y, z, i ->
                val srY = chank.maxY - chank.minY
                length = ((x-chank.centrX)*(x-chank.centrX)) + ((y-srY) * (y-srY)) + ((z-chank.centrZ) * (z-chank.centrZ))
                if(length < minLength) {
                    minLength = length
                    result = Vector3(x, y, z)
                }
            }

            return result
        }

        fun CoroutineScope.doJobPointToView(fastCloud: CloudJ,
                                            outp: BufferedChannelForView
        ): Job = launch(Dispatchers.IO) {
            WriterCloud.writePoint(fastCloud, outp.pointData)
        }

        private fun createVerticalVectors(inp: CloudJ): List<V2Vertical> {
            if((inp.maxX ?: Short.MIN_VALUE).toInt() - (inp.minX ?: Short.MAX_VALUE).toInt() < AppConstants.MIN_SIZE_CREATE_VERTICAL_VECTORS) { //todo kostili
                return emptyList()
            }
            if((inp.maxZ ?: Short.MIN_VALUE).toInt() - (inp.minZ ?: Short.MAX_VALUE).toInt() < AppConstants.MIN_SIZE_CREATE_VERTICAL_VECTORS) {
                return emptyList()
            }

            val res = ArrayList<V2Vertical>()
            for (curX in inp.minX!!..inp.maxX!! step AppConstants.STEP_CREATE_VERTICAL_VECTORS.toInt()) {
                for (curZ in inp.minZ!!..inp.maxZ!! step AppConstants.STEP_CREATE_VERTICAL_VECTORS.toInt()) {
                    res.add(V2Vertical(curX.toShort(), curZ.toShort()))
                }
            }

            return res
        }
    }
}