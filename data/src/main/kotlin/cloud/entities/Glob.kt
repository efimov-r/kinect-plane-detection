package cloud.entities

import cloud.entities.forview.IElement
import java.io.Serializable

data class Glob (
    var z: Short,
    var y: Short,
    var x: Short
) : IElement, Serializable{
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