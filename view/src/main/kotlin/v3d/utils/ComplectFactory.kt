package v3d.utils

import v3d.models.V3dModel
import v3d.eventlistener.V3dComplect
import v3d.eventlistener.V3dModelComplect
import v3d.runners.*
import v3d.shaders.V3dGeometryCubeLineShader
import v3d.shaders.V3dGeometryCubeShader
import v3d.shaders.V3dPointsShader
import v3d.vbos.model.VBOModelImpl
import v3d.vbos.other.VBOLine
import v3d.vbos.other.VBOPoints
import v3d.vbos.other.VBOTriangle

object ComplectFactory {
    fun createAllComplects(): List<V3dComplect> {
        val v3dComplects: MutableList<V3dComplect> = ArrayList()
        v3dComplects.add(createPointComplect())
        v3dComplects.add(createCubeComplect())
        v3dComplects.add(createTrianglesComplect())
        v3dComplects.add(createLineComplect())
        return v3dComplects
    }

    fun createAllModelComplects(models: List<V3dModel>): List<V3dModelComplect> {
        val v3dComplects: MutableList<V3dModelComplect> = ArrayList()
        models.forEach { v3dComplects.add(V3dModelComplect(VBOModelImpl(it), V3dPointsShader())) }
        return v3dComplects
    }

    private fun createCubeComplect(startSizeBuffer: Int = 100, cubeSize: Float = 0.02f): V3dComplect {
        val res =  V3dComplect(V3dCubeRunnerImpl(), VBOPoints(), currentSizeBuff = startSizeBuffer)
        res.shaders.add(V3dGeometryCubeShader(cubeSize / 2))
        res.shaders.add(V3dGeometryCubeLineShader(cubeSize / 2))

        return res
    }

    private fun createPointComplect(startSizeBuffer: Int = 100): V3dComplect {
        val res = V3dComplect(V3dPointRunnerImpl(), VBOPoints(), currentSizeBuff = startSizeBuffer)
        res.shaders.add(V3dPointsShader())
        return res

    }

    private fun createTrianglesComplect(startSizeBuffer: Int = 100): V3dComplect {
        val res = V3dComplect(V3dTriangleRunnerImpl(), VBOTriangle(), currentSizeBuff = startSizeBuffer)
        res.shaders.add(V3dPointsShader())
        return res
    }

    private fun createLineComplect(startSizeBuffer: Int = 100): V3dComplect {
        val res = V3dComplect(V3dLineRunnerImpl(), VBOLine(), currentSizeBuff = startSizeBuffer)
        res.shaders.add(V3dPointsShader())
        return res
    }
}
