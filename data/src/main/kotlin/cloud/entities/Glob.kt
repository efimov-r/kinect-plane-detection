package cloud.entities

data class Glob (
    var z: Short,
    var y: Short,
    var x: Short
) {
    fun copyOf(glob: Glob) {
        this.x = glob.x
        this.y = glob.y
        this.z = glob.z
    }

    companion object {
        fun createEmpty(): Glob {
            return Glob(0, 0, 0)
        }
    }
}