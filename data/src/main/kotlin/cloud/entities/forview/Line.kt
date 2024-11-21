package cloud.entities.forview

data class Line(
    val v1: Point,
    val v2: Point,
) {
    fun copyOf(line: Line) {
        this.v1.copyOf(line.v1)
        this.v2.copyOf(line.v2)
    }

    companion object{
        fun createEmpty(): Line {
            return Line(
                Point.createEmpty(),
                Point.createEmpty()
            )
        }
    }
}