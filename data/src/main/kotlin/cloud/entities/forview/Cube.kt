package cloud.entities.forview

data class Cube(
    var z: Float,
    var y: Float,
    var x: Float,
    var color: Float
): IElement {
    fun copyOf(cube: Cube) {
        this.z = cube.z
        this.x = cube.x
        this.y = cube.y
        this.color = cube.color
    }

    companion object {
        fun createEmpty(): Cube {
            return Cube(0f, 0f, 0f, 0f)
        }
    }
}