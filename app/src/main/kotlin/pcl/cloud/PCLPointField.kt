package era.app.pcl.cloud

data class PCLPointField(
    var name: String,
    var offset: Int = 0,
    var datatype: PointFieldTypes,
    var count: Int = 0
){
    enum class PointFieldTypes{
        BOOL, SHORT, INT, LONG, FLOAT, DOUBLE
    }

    override fun toString(): String {
        return "name: $name\n" +
                "offset: $offset\n" +
                "datatype: $datatype\n" +
                "count: $count"
    }


}
