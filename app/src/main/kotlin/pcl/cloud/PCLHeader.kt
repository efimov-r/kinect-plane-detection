package era.app.pcl.cloud

data class PCLHeader(
    var seq: Int = 0, //краткий id
    var stamp: Long = 0, //временная метка
    var frameId: String = "", //кртакий id системы координат
) {
    override fun toString(): String {
        return "seq: $seq\n" +
                "stamp: $stamp\n" +
                "frame_id: $frameId\n"
    }

    override fun equals(other: Any?): Boolean {
        other?.let {
            if(other is PCLHeader) {
                return (seq == other.seq) && (stamp == other.stamp) && (frameId.equals(other.frameId))
            } else {
                return false
            }
        }
        return false
    }
}
