package viewdata.elementlist.elements

import viewdata.elementlist.IElement

data class Point(
    var z: Float,
    var y: Float,
    var x: Float,
    var color: Float
): IElement {
    fun copyOf(point: Point) {
        this.z = point.z
        this.x = point.x
        this.y = point.y
        this.color = point.color
    }

    companion object {
        fun createEmpty(): Point {
            return Point(0f, 0f, 0f, 0f)
        }
    }
}