package cloud.databuffer

import cloud.entities.forview.Cube
import cloud.entities.forview.Line
import cloud.entities.forview.Point
import cloud.entities.forview.Triangle

class CloudForView(cubeSize: Int,
    lineSize: Int,
    pointSize: Int,
    triangleSize: Int,
    val id: Int) : DataForBufferedChannel {

    private val cubeData: Array<Cube> = Array(cubeSize) {Cube.createEmpty()}
    private val lineData: Array<Line> = Array(lineSize) {Line.createEmpty()}
    private val pointData: Array<Point> = Array(pointSize) {Point.createEmpty()}
    private val triangleData: Array<Triangle> = Array(triangleSize) {Triangle.createEmpty()}

    fun getCubeData(callback: (dat: Cube) -> Unit) {
        for(i in 0..<cubeData.size) {
            callback(cubeData[i])
        }
    }

    fun getLineData(callback: (dat: Line) -> Unit) {
        for(i in 0..<lineData.size) {
            callback(lineData[i])
        }
    }

    fun getPointData(callback: (dat: Point, i: Int) -> Unit) {
        for(i in 0..<pointData.size) {
            callback(pointData[i], i)
        }
    }

    fun getTriangleData(callback: (dat: Triangle) -> Unit) {
        for(i in 0..<triangleData.size) {
            callback(triangleData[i])
        }
    }

    fun setDataGlob(i: Int, x: Float, y: Float, z: Float) {
        pointData[i].x = x
        pointData[i].y = y
        pointData[i].z = z
    }


    override fun getIdData(): Int {
        return id
    }
}