package era.app.utils.writers

import clouds.Cloud
import era.app.AppConstants
import vectors.V2Vertical
import vectors.Vector3Childed
import vectors.Vector3Float
import viewdata.elementlist.ElementList
import viewdata.elementlist.elements.Cube
import viewdata.elementlist.elements.Line
import viewdata.elementlist.elements.Point

object WriterCloud {
    fun writeCubes(vectors: MutableList<Vector3Childed>, toView: ElementList<Cube>) {
        var v: Vector3Childed? = null
        vectors.forEach{
            if(it.vecs.size > 0) {
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

    fun writeCubeWithChild(vector: Vector3Childed, toView: ElementList<Cube>) {
        toView.add {
            it.color = 0f
            it.y = vector.y.toFloat() / AppConstants.MAX_MM_ON_V3D
            it.x = vector.x.toFloat() / AppConstants.MAX_MM_ON_V3D
            it.z = vector.z.toFloat() / AppConstants.MAX_MM_ON_V3D
        }

        vector.neighbours.forEach { vector ->
            toView.add {
                it.color = 1f
                it.y = vector.y.toFloat() / AppConstants.MAX_MM_ON_V3D
                it.x = vector.x.toFloat() / AppConstants.MAX_MM_ON_V3D
                it.z = vector.z.toFloat() / AppConstants.MAX_MM_ON_V3D
            }
        }
    }

    private fun isThisNeighbours(vectors: ArrayList<Vector3Float>, v: Vector3Childed): Boolean {
        vectors.forEach {
            if(v.equals(it))
                return true
        }
        return false
    }

    fun writePoint(cloud: Cloud, toView: ElementList<Point>) {
        cloud.forEach { x, y, z ->
            toView.add {
                it.z = z.toFloat()/ AppConstants.MAX_MM_ON_V3D
                it.x = x.toFloat()/ AppConstants.MAX_MM_ON_V3D
                it.y = y.toFloat()/ AppConstants.MAX_MM_ON_V3D
            }
        }
    }

    fun viewNullVector(v3: Vector3Float, toView: ElementList<Line>, color: Float) {
            toView.add {
                it.v1.y = v3.y.toFloat() / AppConstants.MAX_MM_ON_V3D
                it.v1.x = v3.x.toFloat() / AppConstants.MAX_MM_ON_V3D
                it.v1.z = v3.z.toFloat() / AppConstants.MAX_MM_ON_V3D
                it.v1.color = color

                it.v2.y = 0f / AppConstants.MAX_MM_ON_V3D
                it.v2.x = 0f / AppConstants.MAX_MM_ON_V3D
                it.v2.z = 0f / AppConstants.MAX_MM_ON_V3D
                it.v2.color = color
            }
    }

    fun viewPerpendicular(v3: Vector3Childed, toView: ElementList<Line>) {
        v3.vecs.forEach { pv ->
            toView.add {
                it.v1.y = v3.y.toFloat() / AppConstants.MAX_MM_ON_V3D
                it.v1.x = v3.x.toFloat() / AppConstants.MAX_MM_ON_V3D
                it.v1.z = v3.z.toFloat() / AppConstants.MAX_MM_ON_V3D
                it.v1.color = 1f

                it.v2.y = (v3.y + pv.y).toFloat() / AppConstants.MAX_MM_ON_V3D
                it.v2.x = (v3.x + pv.x).toFloat() / AppConstants.MAX_MM_ON_V3D
                it.v2.z = (v3.z + pv.z).toFloat() / AppConstants.MAX_MM_ON_V3D
                it.v2.color = 1f
            }
        }
    }

    fun viewVerticalVectors(cloud: List<V2Vertical>, toView: ElementList<Line>) {
        for (v2 in cloud) {
            toView.add {
                it.v1.y = 2f
                it.v1.x = v2.x.toFloat() / AppConstants.MAX_MM_ON_V3D
                it.v1.z = v2.z.toFloat() / AppConstants.MAX_MM_ON_V3D
                it.v1.color = 1f

                it.v2.y = -6f
                it.v2.x = v2.x.toFloat() / AppConstants.MAX_MM_ON_V3D
                it.v2.z = v2.z.toFloat() / AppConstants.MAX_MM_ON_V3D
                it.v2.color = 1f
            }
        }
    }

    fun viewVerticalVector(v: V2Vertical, toView: ElementList<Line>) {
        toView.add {
            it.v1.y = 2f
            it.v1.x = v.x.toFloat() / AppConstants.MAX_MM_ON_V3D
            it.v1.z = v.z.toFloat() / AppConstants.MAX_MM_ON_V3D
            it.v1.color = 1f

            it.v2.y = -6f
            it.v2.x = v.x.toFloat() / AppConstants.MAX_MM_ON_V3D
            it.v2.z = v.z.toFloat() / AppConstants.MAX_MM_ON_V3D
            it.v2.color = 1f
        }
    }
}