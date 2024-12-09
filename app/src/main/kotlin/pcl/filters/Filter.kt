package era.app.pcl.filters

import era.app.pcl.cloud.PCLPointCloud2

abstract class Filter(protected val input_: PCLPointCloud2) {

    abstract fun applyFilter(output: PCLPointCloud2)

    fun filter(output: PCLPointCloud2) {
        if(input_.equals(output)) {

        } else {
            output.header = input_.header
            applyFilter(output)
        }
    }
}