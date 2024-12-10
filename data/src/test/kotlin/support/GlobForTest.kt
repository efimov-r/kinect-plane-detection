package support

import viewdata.elementlist.IElement
import java.io.Serializable

data class GlobForTest (
    var z: Short,
    var y: Short,
    var x: Short
) : IElement, Serializable{
    fun copyOf(glob: GlobForTest) {
        this.x = glob.x
        this.y = glob.y
        this.z = glob.z
    }

    companion object {
        fun createEmpty(): GlobForTest {
            return GlobForTest(0, 0, 0)
        }
    }
}