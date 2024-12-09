package era.app.pcl.cloud

import era.utils.files.KiLogger
import java.lang.Math.max

data class PCLPointCloud2(
    var header: PCLHeader,
    var height: Int = 0,
    var width: Int = 0,
    var fields: Array<PCLPointField>,
    var is_bigendian: Bigendian = Bigendian.BOOST_ENDIAN_BIG_BYTE,
    var point_step: Int = 0,
    var row_step: Int = 0,
    var data: ByteArray,
    var is_dense: Boolean = false //byte in original
) {
    companion object {
        fun copy(destCloud: PCLPointCloud2, sourceCloud: PCLPointCloud2) {
            destCloud.header = sourceCloud.header
            destCloud.height = sourceCloud.height
            destCloud.width = sourceCloud.width
            destCloud.fields = sourceCloud.fields.copyOf()
            destCloud.is_bigendian = sourceCloud.is_bigendian
            destCloud.point_step = sourceCloud.point_step
            destCloud.row_step = sourceCloud.row_step
            destCloud.data = sourceCloud.data.copyOf()
            destCloud.is_dense = sourceCloud.is_dense
        }

        fun concatenate(cloud1: PCLPointCloud2, cloud2: PCLPointCloud2): Boolean {
            if(cloud1.is_bigendian != cloud2.is_bigendian) {
                KiLogger.err("[pcl::PCLPointCloud2::concatenate] Endianness of clouds does not match")
                return false
            }
            val size1 = cloud1.width * cloud1.height
            val size2 = cloud2.width * cloud2.height
            if ((size1 == 0) && (size2 != 0)) {
                copy(cloud1, cloud2)
            }

            if ((size1 == 0) || (size2 == 0)) {
                if(cloud1.header.stamp < cloud2.header.stamp) {
                    cloud1.header.stamp = cloud2.header.stamp
                }
                return true;
            }

            val simple_layout = fields_eq(cloud1.fields, cloud2.fields)
            val validFields: ArrayList<FieldDetails> = ArrayList(max(cloud1.fields.size, cloud2.fields.size))

            if(!simple_layout) {
                var i = 0
                var j = 0
                while(i < cloud1.fields.size && j < cloud2.fields.size) {
                    if("_".equals(cloud1.fields[i].name)) {
                        ++i
                        continue
                    }
                    if("_".equals(cloud2.fields[j].name)) {
                        ++j
                        continue
                    }
                    if(field_eq(cloud1.fields[i], cloud2.fields[j])){
                        validFields.add(FieldDetails(i, j, 0))
                        ++i
                        ++j
                        continue
                    }
                    KiLogger.err("[pcl::PCLPointCloud2::concatenate] Number of fields to copy in cloud1 ($i) != Number of fields to copy in cloud2 ($j)")
                    return false
                }
            }

            cloud1.header.stamp = max (cloud1.header.stamp, cloud2.header.stamp)
            cloud1.is_dense = cloud1.is_dense && cloud2.is_dense;
            cloud1.height = 1;
            cloud1.width = size1 + size2;
            cloud1.row_step = cloud1.width * cloud1.point_step;

            if (simple_layout){
                cloud1.data.plus(cloud2.data)
                return true
            }
            val data1Size = cloud1.data.size
//            cloud1.data.resize(data1Size + cloud2.data.size ())
            for(cp in 0..<size2) {
                validFields.forEach { fieldData->
                    val i = fieldData.idx1
                    val j = fieldData.idx2
                    val size = fieldData.size

                    val sourceOffset = cp * cloud2.point_step + cloud2.fields[j].offset
                    val destinationOffset = data1Size + cp * cloud1.point_step + cloud1.fields[i].offset

                    System.arraycopy(
                        cloud2.data,
                        sourceOffset,
                        cloud1.data,
                        destinationOffset,
                        cloud2.fields[j].count * size
                    )

                }
            }
            return true
        }

        private fun field_eq(field1: PCLPointField, field2: PCLPointField) : Boolean {
            return ((field1.name == field2.name) ||
                    (field1.name == "rgb" && field2.name == "rgba") ||
                    (field1.name == "rgba" && field2.name == "rgb"));
        }

        private fun fields_eq(fields1: Array<PCLPointField>, fields2: Array<PCLPointField>) : Boolean {
            fields1.forEach { f1 ->
                fields2.forEach { f2 ->
                    if(!field_eq(f1, f2)) return false
                }
            }
            return true
        }

        private data class FieldDetails(val idx1: Int, val idx2: Int, val size: Int)
    }

    fun at(point_index: Int, field_offset: Int): Byte {
        val position = point_index * point_step + field_offset
        return data[position]
    }

    override fun toString(): String {
        return "header:\n$header\n" +
                "height: $height\n" +
                "width: $width\n" +
                "fields[]:\n" +
                "$fields\n" +
                "is_bigendian: $is_bigendian" +
                "point_step: $point_step\n" +
                "row_step: $row_step\n" +
                "data[]: \n" +
                "${data.contentToString()}\n" +
                "is_dense: $is_dense\n"
    }
}
