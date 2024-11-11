import models.KinectModel
import org.junit.jupiter.api.Test
import runs.RunSinRGB
import runs.RunTrianglesRGB
import v2d.ViewFrame
import v3d.eventlistener.V3dGlMultiEventListener.V3dComplect
import v3d.utils.ComplectFactory.createCubeComplect
import v3d.utils.ComplectFactory.createModelComplect
import v3d.utils.ComplectFactory.createPointComplect
import v3d.utils.ComplectFactory.createTrianglesComplect
import v3d.utils.GLCapabilitiesCreator
import javax.swing.SwingUtilities
import kotlin.test.assertEquals

internal class v3dTest {

    val MESH_WIDTH: Int = 8 * 64
    val MESH_HEIGHT: Int = 8 * 64

    @Test
    fun testKinnectModel() {
//        var isFirst = true
//        var bufSize = 0
//        val kinectModel = KinectModel(0.01f)
//        start(createTrianglesComplect(V3dRunner{ buffer: FloatBuffer ->
//            if (isFirst) {
//                bufSize = kinectModel.buildModel(buffer)
//                isFirst = false
//            }
//            TimeUnit.MILLISECONDS.sleep(20)
//            return@V3dRunner bufSize
//        } , kinectModel.getSizeBuf()))

        start(createModelComplect(KinectModel(0.01f)))

        Thread.sleep(1000*60*60)
        assertEquals(1, 1)
    }

    @Test
    fun testCube() {
        start(createCubeComplect(RunSinRGB(0.0f,
            MESH_WIDTH,
            MESH_HEIGHT
        ), MESH_WIDTH * MESH_HEIGHT, 0.02f))

        Thread.sleep(1000*60*60)
        assertEquals(1, 1)
    }

    @Test
    fun testPoint() {
        start(createPointComplect(RunSinRGB(0.0f,
            MESH_WIDTH,
            MESH_HEIGHT
        ), MESH_WIDTH * MESH_HEIGHT
        ))

        Thread.sleep(1000*60*60)
        assertEquals(1, 1)
    }

    @Test
    fun testTriangle() {
        start(createTrianglesComplect(RunTrianglesRGB(), 60))

        Thread.sleep(1000*60*60)
        assertEquals(1, 1)
    }

    private fun start(complect: V3dComplect) {
        val creator = GLCapabilitiesCreator()

        val v3dComplects: MutableList<V3dComplect> = ArrayList<V3dComplect>()
        v3dComplects.add(complect)
        SwingUtilities.invokeLater(object : Runnable {
            override fun run() {
                ViewFrame(creator, v3dComplects)
            }
        })
    }
}