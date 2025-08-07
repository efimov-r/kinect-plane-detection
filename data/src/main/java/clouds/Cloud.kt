package clouds

import elementlist.ShortList
import serialize.ShortArraySerialize

open class Cloud(initialCapacity: Int = 50, name: String?) : ShortList(initialCapacity * 3, name) {
    private var minXi = Short.MAX_VALUE
    val minX: Short?
        get() {
            if (minXi == Short.MAX_VALUE) return null
            return minXi
        }
    private var minYi = Short.MAX_VALUE
    val minY: Short?
        get() {
            if (minYi == Short.MAX_VALUE) return null
            return minYi
        }
    private var minZi = Short.MAX_VALUE
    val minZ: Short?
        get(){
            if (minZi == Short.MAX_VALUE) return null
            return minZi
        }

    private var maxXi = Short.MIN_VALUE
    val maxX: Short?
        get(){
            if (maxXi == Short.MIN_VALUE) return null
            return maxXi
        }
    private var maxYi = Short.MIN_VALUE
    val maxY: Short?
        get(){
            if (maxYi == Short.MIN_VALUE) return null
            return maxYi
        }
    private var maxZi = Short.MIN_VALUE
    val maxZ: Short?
        get(){
            if (maxZi == Short.MIN_VALUE) return null
            return maxZi
        }

    fun addGlob(x: Short, y: Short, z: Short) {
        super.add(x)
        if (x < minXi) minXi = x
        if (x > maxXi) maxXi = x
        super.add(y)
        if (y < minYi) minYi = y
        if (y > maxYi) maxYi = y
        super.add(z)
        if (z < minZi) minZi = z
        if (z > maxZi) maxZi = z
    }

    fun forEach(call: (x: Short, y: Short, z: Short) -> Unit) {
        for (i in 0..<(super.size() / 3)) {
            call(elementData[i * 3], elementData[i * 3 + 1], elementData[i * 3 + 2])
        }
    }

    fun copyOf(inp: Cloud) {
        super.copyOf(inp)
        this.maxXi = inp.maxXi
        this.maxYi = inp.maxYi
        this.maxZi = inp.maxZi
        this.minXi = inp.minXi
        this.minYi = inp.minYi
        this.minZi = inp.minZi
    }

    fun copyOf(inp: ShortArraySerialize?) {
        if (inp == null) {
            return
        }
        clear()
        super.ensureCapacity(inp.getSize())
        for (i in 0..<(inp.getSize() / 3)) {
            addGlob(inp.getData()[i * 3], inp.getData()[i * 3 + 1], inp.getData()[i * 3 + 2])
        }
    }

    override fun clear() {
        super.clear()
        minXi = Short.MAX_VALUE
        minYi = Short.MAX_VALUE
        minZi = Short.MAX_VALUE

        maxXi = Short.MIN_VALUE
        maxYi = Short.MIN_VALUE
        maxZi = Short.MIN_VALUE
    }
}
