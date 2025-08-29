package vectors

data class PlanePolygon(
    val a: Float, val b: Float, val c: Float, val d: Float,
    val vertices: List<Vector3Float>
)