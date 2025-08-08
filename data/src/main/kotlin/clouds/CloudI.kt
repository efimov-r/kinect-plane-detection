package clouds

class CloudI(initialCapacity: Int = 1000, name: String = "") : Cloud(initialCapacity, name) {
    private var minX = Float.MAX_VALUE
    private var minY = Float.MAX_VALUE
    private var minZ = Float.MAX_VALUE
    private var maxX = Float.MIN_VALUE
    private var maxY = Float.MIN_VALUE
    private var maxZ = Float.MIN_VALUE

    private fun calckI(x: Float, y: Float, z: Float) {
        if(x < minX) minX = x
        if(y < minY) minY = y
        if(z < minZ) minZ = z
        if(x > maxX) maxX = x
        if(y > maxY) maxY = y
        if(z > maxZ) maxZ = z
    }

    override fun add(x: Float, y: Float, z: Float) {
        super.add(x, y, z)
        calckI(x, y, z)
    }

    //foreach override?

    override fun minX(): Float? = if(minX == Float.MAX_VALUE) null else minX
    override fun minY(): Float? = if(minY == Float.MAX_VALUE) null else minY
    override fun minZ(): Float? = if(minZ == Float.MAX_VALUE) null else minZ

    override fun maxX(): Float? = if(maxX == Float.MIN_VALUE) null else maxX
    override fun maxY(): Float? = if(maxY == Float.MIN_VALUE) null else maxY
    override fun maxZ(): Float? = if(maxZ == Float.MIN_VALUE) null else maxZ
}