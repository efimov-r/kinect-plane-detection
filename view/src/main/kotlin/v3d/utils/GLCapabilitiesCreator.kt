package v3d.utils

import com.jogamp.opengl.GLCapabilities
import com.jogamp.opengl.GLProfile

class GLCapabilitiesCreator {
    val capabilities: GLCapabilities

    init {
        val profile = GLProfile.get(GLProfile.GL3)
        capabilities = GLCapabilities(profile)
    }
}
