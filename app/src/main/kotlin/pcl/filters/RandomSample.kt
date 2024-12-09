package era.app.pcl.filters

import era.app.pcl.cloud.PCLPointCloud2

//brief False = remove points (default), true = redefine points, keep structure.
class RandomSample(input_: PCLPointCloud2, val keep_organized_: Boolean = false) : Filter(input_) {
    override fun applyFilter(output: PCLPointCloud2) {
        if(keep_organized_){

        } else {

        }
    }
}