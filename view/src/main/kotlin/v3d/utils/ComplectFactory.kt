package v3d.utils

import v3d.eventlistener.V3dGlMultiEventListener.V3dComplect
import v3d.runs.V3dRunner
import v3d.shaders.Shader
import v3d.shaders.V3dGeometryCubeLineShader
import v3d.shaders.V3dGeometryCubeShader
import v3d.shaders.V3dPointsShader
import v3d.vbos.VBOPointsHelper
import v3d.vbos.VBOTriangleHelper

object ComplectFactory {
    fun createCubeComplect(runner: V3dRunner?, sizeBuffer: Int, cubeSize: Float): V3dComplect {
        val res = V3dComplect()
        res.runner = runner
        res.vboHelper = VBOPointsHelper(sizeBuffer)
        res.shaders = ArrayList<Shader>()
        val halfSize = cubeSize / 2
        res.shaders!!.add(V3dGeometryCubeShader(halfSize))
        res.shaders!!.add(V3dGeometryCubeLineShader(halfSize))
        return res
    }

    fun createPointComplect(runner: V3dRunner?, sizeBuffer: Int): V3dComplect {
        val res = V3dComplect()
        res.runner = runner
        res.vboHelper = VBOPointsHelper(sizeBuffer)
        res.shaders = ArrayList<Shader>()
        res.shaders!!.add(V3dPointsShader())
        return res
    }

    fun createTrianglesComplect(runner: V3dRunner?, sizeBuffer: Int): V3dComplect {
        val res = V3dComplect()
        res.runner = runner
        res.vboHelper = VBOTriangleHelper(sizeBuffer)
        res.shaders = ArrayList<Shader>()
        res.shaders!!.add(V3dPointsShader())
        return res
    }
}
