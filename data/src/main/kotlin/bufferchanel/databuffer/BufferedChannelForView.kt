package bufferchanel.databuffer

import viewdata.elementlist.ElementList
import viewdata.elementlist.elements.Cube
import viewdata.elementlist.elements.Line
import viewdata.elementlist.elements.Point
import viewdata.elementlist.elements.Triangle

class BufferedChannelForView(val id: Int,
                             cubeSize: Int = 100,
                             lineSize: Int = 100,
                             pointSize: Int = 600_000, //todo &?
                             triangleSize: Int = 100) : DataForBufferedChannel {

    val cubeData: ElementList<Cube> =
        ElementList(initialCapacity = cubeSize) { Cube.createEmpty()}

    val lineData: ElementList<Line> =
        ElementList(initialCapacity = lineSize) { Line.createEmpty()}

    val pointData: ElementList<Point> =
        ElementList(initialCapacity = pointSize) { Point.createEmpty()}

    val triangleData: ElementList<Triangle> =
        ElementList(initialCapacity = triangleSize) { Triangle.createEmpty()}

    override fun getIdData(): Int {
        return id
    }

    override fun validate(): Boolean {
        return true
    }
}