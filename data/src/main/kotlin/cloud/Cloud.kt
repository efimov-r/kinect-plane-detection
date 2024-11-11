package era.kinect.cloud

class Cloud (val size: Int){

    val globs: Array<Glob?> = arrayOfNulls<Glob>(size)

    fun setGlob(i: Int, glob: Glob) {
        globs[i] = glob
    }

}