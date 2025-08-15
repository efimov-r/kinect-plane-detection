package bufferchanel.databuffer

import viewdata.buffers.CubeBuffer
import viewdata.buffers.LineBuffer
import viewdata.buffers.PointBuffer
import viewdata.buffers.TriangleBuffer

class BufferedChannelForView(val id: Int,
                             cubeSize: Int = 100,
                             lineSize: Int = 100,
                             pointSize: Int = 600_000, //todo &?
                             triangleSize: Int = 100) : DataForBufferedChannel {

    val cubeData: CubeBuffer = CubeBuffer(cubeSize)
    val lineData: LineBuffer = LineBuffer(lineSize)
    val pointData: PointBuffer = PointBuffer(pointSize)
    val triangleData: TriangleBuffer = TriangleBuffer(triangleSize)

    override fun getIdData(): Int {
        return id
    }

    override fun validate(): Boolean {
        return true
    }
}