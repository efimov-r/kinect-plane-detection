package era.kinect.cloud

import kinect.Constant.KINECT_DEPTH_COLS
import kinect.Constant.KINECT_DEPTH_ROWS

class Cloud {

    val gg: Array<Array<Glob?>> =
        Array<Array<Glob?>>(KINECT_DEPTH_ROWS) { arrayOfNulls<Glob>(KINECT_DEPTH_COLS) }

    fun setGlob(x: Int, y: Int, glob: Glob) {
        gg[y][x] = glob
    }

}