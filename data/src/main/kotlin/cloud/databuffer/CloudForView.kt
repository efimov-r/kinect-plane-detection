package cloud.databuffer

import cloud.entities.forview.*

class CloudForView(val id: Int,
                   cubeSize: Int = 100,
                    lineSize: Int = 100,
                    pointSize: Int = 100,
                    triangleSize: Int = 100) : DataForBufferedChannel {

    val cubeData: ElementArray<Cube> =
        ElementArray(initialCapacity = cubeSize) {Cube.createEmpty()}

    val lineData: ElementArray<Line> =
        ElementArray(initialCapacity = lineSize) {Line.createEmpty()}

    val pointData: ElementArray<Point> =
        ElementArray(initialCapacity = pointSize) {Point.createEmpty()}

    val triangleData: ElementArray<Triangle> =
        ElementArray(initialCapacity = triangleSize) {Triangle.createEmpty()}

    override fun getIdData(): Int {
        return id
    }
}