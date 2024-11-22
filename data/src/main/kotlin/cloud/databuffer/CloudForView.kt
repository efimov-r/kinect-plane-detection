package cloud.databuffer

import cloud.entities.forview.*

class CloudForView(cubeSize: Int,
    lineSize: Int,
    pointSize: Int,
    triangleSize: Int,
    val id: Int) : DataForBufferedChannel {

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