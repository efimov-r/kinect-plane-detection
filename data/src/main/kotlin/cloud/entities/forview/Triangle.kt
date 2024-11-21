package cloud.entities.forview

data class Triangle(
    val v1: Point,
    val v2: Point,
    val v3: Point
) {
    fun copyOf(triangle: Triangle) {
        this.v1.copyOf(triangle.v1)
        this.v2.copyOf(triangle.v2)
        this.v3.copyOf(triangle.v3)
    }

    companion object{
        fun createEmpty(): Triangle {
            return Triangle(
                Point.createEmpty(),
                Point.createEmpty(),
                Point.createEmpty()
            )
        }
    }
}