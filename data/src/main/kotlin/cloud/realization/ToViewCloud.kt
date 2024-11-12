package cloud.realization

import cloud.Cloud
import era.kinect.cloud.Glob

class ToViewCloud (val size: Int): Cloud{

    val globs: Array<Glob?> = arrayOfNulls<Glob>(size)

    fun setGlob(i: Int, glob: Glob) {
        globs[i] = glob
    }

}