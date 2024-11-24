package cloud.databuffer

import cloud.entities.forview.*

class CloudForView(val id: Int,
                   cubeSize: Int = 100,
                    lineSize: Int = 100,
                    pointSize: Int = 100,
                    triangleSize: Int = 100) : DataForBufferedChannel {

    val cubeData: ForViewElementArray<Cube> =
        ForViewElementArray(initialCapacity = cubeSize) {Cube.createEmpty()}

    val lineData: ForViewElementArray<Line> =
        ForViewElementArray(initialCapacity = lineSize) {Line.createEmpty()}

    val pointData: ForViewElementArray<Point> =
        ForViewElementArray(initialCapacity = pointSize) {Point.createEmpty()}

    val triangleData: ForViewElementArray<Triangle> =
        ForViewElementArray(initialCapacity = triangleSize) {Triangle.createEmpty()}

    override fun getIdData(): Int {
        return id
    }
}