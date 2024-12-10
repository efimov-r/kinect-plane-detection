import bufferchanel.BufferedChannel
import bufferchanel.databuffer.BufferedChannelForView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import runs.RunSinRGB
import v2d.ViewFrame
import v3d.eventlistener.V3dComplect
import v3d.eventlistener.V3dGlMultiEventListener
import v3d.eventlistener.V3dModelComplect
import v3d.models.KinectModel
import v3d.runners.V3dPointRunnerImpl
import v3d.shaders.V3dGeometryCubeLineShader
import v3d.shaders.V3dGeometryCubeShader
import v3d.shaders.V3dPointsShader
import v3d.utils.ComplectFactory.createAllComplects
import v3d.utils.ComplectFactory.createAllModelComplects
import v3d.utils.GLCapabilitiesCreator
import v3d.vbos.model.VBOModelImpl
import v3d.vbos.other.VBOPoints
import javax.swing.SwingUtilities
import kotlin.math.cos
import kotlin.math.sin

internal class v3dTest {

    @Test
    fun testKinnectModel() { //тест только модельки
        runBlocking {
            launch(Dispatchers.IO) {
                delay(1000*60*60)
            }

            val mc = V3dModelComplect(VBOModelImpl(KinectModel(0.01f)), V3dPointsShader())
            start(null, mc, null)
        }
    }

    @Test
    fun testCubeWithoutChannel() { //тест работы промежуточных кадров, ок. 20fps должно давать
        runBlocking {
            val toViewChannel = BufferedChannel {
                return@BufferedChannel BufferedChannelForView(it)
            }

            launch(Dispatchers.IO) {
                while(true) {
                    toViewChannel.getDataForWriteOrRewriteOld()?.let {
                        it.pointData.clear()
                        it.pointData.add {  }
                        toViewChannel.endWrite(it)
                    }
                    delay(100)
                }
            }

            val runner = RunSinRGB(0f, 8 * 64, 8 * 64)
            val res =  V3dComplect(runner, VBOPoints(), currentSizeBuff = 1)
            res.shaders.add(V3dGeometryCubeShader(0.02f / 2))
            res.shaders.add(V3dGeometryCubeLineShader(0.02f / 2))

            start(res, null, toViewChannel)
        }
    }

    @Test
    fun testPointAndResizeBuff() { //тест ресайза облака и vbo. В полгига кучи влазит чуть более 5 000 000 точек.
        var mesh_width = 1
        var mesh_height = 1
        var animationState = 0f
        runBlocking {
            val toViewChannel = BufferedChannel {
                return@BufferedChannel BufferedChannelForView(it)
            }

            launch(Dispatchers.IO) {
                while(true) {
                    toViewChannel.getDataForWriteOrRewriteOld()?.let {
                        it.pointData.clear()

                        for (x in 0..<mesh_width) {
                            for (y in 0..<mesh_height) {
                                var u = x / mesh_width.toFloat()
                                var v = y / mesh_height.toFloat()

                                u = u * 2.0f - 1.0f
                                v = v * 2.0f - 1.0f

                                val freq = 4.0f
                                val w =
                                    sin((u * freq + animationState).toDouble()).toFloat() * cos((v * freq + animationState).toDouble()).toFloat() * 0.5f

                                it.pointData.add {
                                    it.z = u
                                    it.x = w
                                    it.y = v
                                }
                            }
                        }
                        animationState += 0.01f
                        mesh_width++
                        mesh_height++

                        it.pointData.add {  }
                        toViewChannel.endWrite(it)
                    }
                    delay(1)
                }
            }

            val cc = V3dComplect(V3dPointRunnerImpl(), VBOPoints(), currentSizeBuff = 100)
            cc.shaders.add(V3dPointsShader())

            start(cc, null, toViewChannel)
        }
    }

    @Test
    fun testAllObj() { //тест всех примитивов
        val mesh_width = 100
        val mesh_height = 100
        var animationState = 0f
        runBlocking {
            val toViewChannel = BufferedChannel {
                return@BufferedChannel BufferedChannelForView(it)
            }

            launch(Dispatchers.IO) {
                while(true) {
                    toViewChannel.getDataForWriteOrRewriteOld()?.let {
                        it.pointData.clear()
                        it.cubeData.clear()
                        it.triangleData.clear()
                        it.lineData.clear()

                        it.lineData.add {
                            it.v1.z = 0f
                            it.v1.x = 0f
                            it.v1.y = 0f
                            it.v1.color = 1f

                            it.v2.z = 1f
                            it.v2.x = 0f
                            it.v2.y = 0f
                            it.v2.color = 0f
                        }
                        it.lineData.add {
                            it.v1.z = 0.1f
                            it.v1.x = 0.1f
                            it.v1.y = 0.1f
                            it.v1.color = 1f

                            it.v2.z = 1.1f
                            it.v2.x = 0.1f
                            it.v2.y = 0.1f
                            it.v2.color = 0f
                        }
                        it.triangleData.add {
                            it.v1.z = -1f
                            it.v1.x = 1f
                            it.v1.y = 0f

                            it.v2.z = -1f
                            it.v2.x = -1f
                            it.v2.y = 0f

                            it.v3.z = 1f
                            it.v3.x = -1f
                            it.v3.y = 0f
                        }

                        for (x in 0..<mesh_width) {
                            for (y in 0..<mesh_height) {
                                var u = x / mesh_width.toFloat()
                                var v = y / mesh_height.toFloat()

                                u = u * 2.0f - 1.0f
                                v = v * 2.0f - 1.0f

                                val freq = 4.0f
                                val w =
                                    sin((u * freq + animationState).toDouble()).toFloat() * cos((v * freq + animationState).toDouble()).toFloat() * 0.5f

                                if(u>0 && v>0) {
                                    it.cubeData.add {
                                        it.z = u
                                        it.x = w
                                        it.y = v
                                        it.color = 1f
                                    }
                                } else {
                                    it.pointData.add {
                                        it.z = u
                                        it.x = w
                                        it.y = v
                                    }
                                }
                            }
                        }
                        animationState += 0.01f
                        toViewChannel.endWrite(it)
                    }
                    delay(10)
                }
            }

            val creator = GLCapabilitiesCreator()
            val eventListener = V3dGlMultiEventListener(
                createAllComplects(),
                createAllModelComplects(listOf(KinectModel(0.01f))),
                toViewChannel)
            SwingUtilities.invokeLater(object : Runnable {
                override fun run() {
                    ViewFrame(creator, eventListener)
                }
            })
        }
    }

    private fun start(v3dComplect: V3dComplect?,
                      v3dModelComplect: V3dModelComplect?,
                      channelToView: BufferedChannel<BufferedChannelForView>?) {

        val creator = GLCapabilitiesCreator()
        val v3dComplects: MutableList<V3dComplect> = ArrayList()
        v3dComplect?.let { v3dComplects.add(it) }
        val v3dModels: MutableList<V3dModelComplect> = ArrayList()
        v3dModelComplect?.let { v3dModels.add(it) }
        val eventListener = V3dGlMultiEventListener(v3dComplects, v3dModels, channelToView)
        SwingUtilities.invokeLater(object : Runnable {
            override fun run() {
                ViewFrame(creator, eventListener)
            }
        })
    }
}