package era.app.utils.writers

import clouds.CloudJ
import era.app.AppConstants
import vectors.V2Vertical
import vectors.Vector3ChildedShort
import vectors.Vector3Short
import viewdata.elementlist.ElementList
import viewdata.elementlist.elements.Cube
import viewdata.elementlist.elements.Line
import viewdata.elementlist.elements.Point

object WriterCloud {
    fun writeCube(vectors: ArrayList<Vector3ChildedShort>, toView: ElementList<Cube>) {
        toView.clear()

        var v: Vector3ChildedShort? = null
        vectors.forEach{
            if(it.neighboursPerpendicularVectors.size > 0) {
                v = it
            }
        }

        vectors.forEach { v3->
            toView.add {
                it.color = if(isThisNeighbours(v3.neighbours, v!!)) 0f else 1f
                it.y = v3.y.toFloat() / AppConstants.MAX_MM_ON_V3D
                it.x = v3.x.toFloat() / AppConstants.MAX_MM_ON_V3D
                it.z = v3.z.toFloat() / AppConstants.MAX_MM_ON_V3D
            }
        }
    }

    private fun isThisNeighbours(vectors: ArrayList<Vector3Short>, v: Vector3ChildedShort): Boolean {
        vectors.forEach {
            if(v.equals(it))
                return true
        }
        return false
    }

    fun writePoint(cloud: CloudJ, toView: ElementList<Point>) {
        toView.clear()
        cloud.forEach { x, y, z ->
            toView.add {
                it.z = z.toFloat()/ AppConstants.MAX_MM_ON_V3D
                it.x = x.toFloat()/ AppConstants.MAX_MM_ON_V3D
                it.y = y.toFloat()/ AppConstants.MAX_MM_ON_V3D
            }
        }
    }

    fun viewPerpendicular(cloud: List<Vector3ChildedShort>, toView: ElementList<Line>) {
        toView.clear()
        cloud.forEach { v3->
            if(v3.neighboursPerpendicularVectors.size !=0)
                println("count ch: ${v3.neighboursPerpendicularVectors.size}")
            v3.neighboursPerpendicularVectors.forEach { child ->
                toView.add {
                    it.v1.y = v3.y.toFloat() / AppConstants.MAX_MM_ON_V3D
                    it.v1.x = v3.x.toFloat() / AppConstants.MAX_MM_ON_V3D
                    it.v1.z = v3.z.toFloat() / AppConstants.MAX_MM_ON_V3D
                    it.v1.color = 1f

                    it.v2.y = (v3.y + child.y).toFloat() / AppConstants.MAX_MM_ON_V3D
                    it.v2.x = (v3.x + child.x).toFloat() / AppConstants.MAX_MM_ON_V3D
                    it.v2.z = (v3.z + child.z).toFloat() / AppConstants.MAX_MM_ON_V3D
                    it.v2.color = 1f
                }
            }

        }
    }

    fun viewVerticalVectors(cloud: ArrayList<V2Vertical>, toView: ElementList<Line>) {
        toView.clear()
        for (v2 in cloud) {
            toView.add {
                it.v1.y = 1f
                it.v1.x = v2.x.toFloat() / AppConstants.MAX_MM_ON_V3D
                it.v1.z = v2.z.toFloat() / AppConstants.MAX_MM_ON_V3D
                it.v1.color = 1f

                it.v2.y = -1f
                it.v2.x = v2.x.toFloat() / AppConstants.MAX_MM_ON_V3D
                it.v2.z = v2.z.toFloat() / AppConstants.MAX_MM_ON_V3D
                it.v2.color = 1f
            }
        }
    }
}