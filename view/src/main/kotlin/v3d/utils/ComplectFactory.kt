package v3d.utils

import v3d.V3dModel
import v3d.V3dRunner
import v3d.eventlistener.V3dComplect
import v3d.eventlistener.V3dModelComplect
import v3d.shaders.V3dGeometryCubeLineShader
import v3d.shaders.V3dGeometryCubeShader
import v3d.shaders.V3dPointsShader
import v3d.vbos.VBOModelHelper
import v3d.vbos.VBOPointsHelper
import v3d.vbos.VBOTriangleHelper

object ComplectFactory {
    fun createCubeComplect(runner: V3dRunner, sizeBuffer: Int, cubeSize: Float): V3dComplect {
        val res =  V3dComplect(runner, VBOPointsHelper(sizeBuffer))
        res.shaders.add(V3dGeometryCubeShader(cubeSize / 2))
        res.shaders.add(V3dGeometryCubeLineShader(cubeSize / 2))

        return res
    }

    fun createPointComplect(runner: V3dRunner, sizeBuffer: Int): V3dComplect {
        val res = V3dComplect(runner, VBOPointsHelper(sizeBuffer))
        res.shaders.add(V3dPointsShader())
        return res

    }

    fun createTrianglesComplect(runner: V3dRunner, sizeBuffer: Int): V3dComplect {
        val res = V3dComplect(runner, VBOTriangleHelper(sizeBuffer))
        res.shaders.add(V3dPointsShader())
        return res
    }

    fun createModelComplect(model: V3dModel): V3dModelComplect {
        return V3dModelComplect(VBOModelHelper(model), V3dPointsShader())
    }
}
