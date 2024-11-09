import models.KinnectModelCreator
import models.KinnectModelCreator.Companion.sizeBuf
import v2d.ViewFrame
import v3d.eventlistener.V3dGlMultiEventListener.V3dComplect
import v3d.runs.RunSinRGB
import v3d.runs.RunTrianglesRGB
import v3d.utils.ComplectFactory.createCubeComplect
import v3d.utils.ComplectFactory.createPointComplect
import v3d.utils.ComplectFactory.createTrianglesComplect
import v3d.utils.GLCapabilitiesCreator
import javax.swing.SwingUtilities

object ViewStarter {
    val MESH_WIDTH: Int = 8 * 64
    val MESH_HEIGHT: Int = 8 * 64

    fun start() {
        val creator = GLCapabilitiesCreator()

        val v3dComplects: MutableList<V3dComplect> = ArrayList<V3dComplect>()
        val cubeComplect = createCubeComplect(RunSinRGB(0.0f, MESH_WIDTH, MESH_HEIGHT), MESH_WIDTH * MESH_HEIGHT, 0.02f)
        val pointComplect = createPointComplect(RunSinRGB(0.0f, MESH_WIDTH, MESH_HEIGHT), MESH_WIDTH * MESH_HEIGHT)
        val trianglesComplect = createTrianglesComplect(RunTrianglesRGB(), 60)
        val kinnectComplect = createTrianglesComplect(KinnectModelCreator(0.01f), sizeBuf)
        //        v3dComplects.add(cubeComplect);
//        v3dComplects.add(pointComplect);
//        v3dComplects.add(trianglesComplect);
        v3dComplects.add(kinnectComplect)
        SwingUtilities.invokeLater(object : Runnable {
            override fun run() {
                ViewFrame(creator, v3dComplects)
            }
        })
    }
}
