package cloud

import cloud.entities.Glob
import cloud.entities.forview.ElementArray

class FreeCloud {
    private var data: ElementArray<Glob> = ElementArray{Glob.createEmpty()}
    private var minX: Short = Short.MAX_VALUE
    private var maxX: Short = Short.MIN_VALUE
    private var minY: Short = Short.MAX_VALUE
    private var maxY: Short = Short.MIN_VALUE
    private var minZ: Short = Short.MAX_VALUE
    private var maxZ: Short = Short.MIN_VALUE

    fun clear() {
        data.clear()
        minX = Short.MAX_VALUE
        maxX = Short.MIN_VALUE
        minY = Short.MAX_VALUE
        maxY = Short.MIN_VALUE
        minZ = Short.MAX_VALUE
        maxZ = Short.MIN_VALUE
    }

    fun add(glob: Glob) {
        data.add {
            it.z = glob.z
            it.y = glob.y
            it.x = glob.x
        }
        if(glob.x < minX) {
            minX = glob.x
        } else if(glob.x > maxX) {
            maxX = glob.x
        }

        if(glob.y < minY) {
            minY = glob.y
        } else if(glob.y > maxY) {
            maxY = glob.y
        }

        if(glob.z < minZ) {
            minZ = glob.z
        } else if(glob.z > maxZ) {
            maxZ = glob.z
        }
    }

    fun forEach(callback: (dat: Glob, i: Int) -> Unit) {
        data.forEach(callback)
    }

    fun getMinX(): Short? {
        if(minX == Short.MAX_VALUE) {
            return null
        }
        return minX
    }

    fun getMaxX(): Short? {
        if(maxX == Short.MIN_VALUE) {
            return null
        }
        return maxX
    }

    fun getMinY(): Short? {
        if(minY == Short.MAX_VALUE) {
            return null
        }
        return minY
    }

    fun getMaxY(): Short? {
        if(maxY == Short.MIN_VALUE) {
            return null
        }
        return maxY
    }

    fun getMinZ(): Short? {
        if(minZ == Short.MAX_VALUE) {
            return null
        }
        return minZ
    }

    fun getMaxZ(): Short? {
        if(maxZ == Short.MIN_VALUE) {
            return null
        }
        return maxZ
    }
}