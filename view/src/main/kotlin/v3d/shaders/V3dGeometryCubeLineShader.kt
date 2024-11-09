package v3d.shaders

import com.jogamp.opengl.GL3
import org.jocl.Sizeof
import v3d.utils.V3dUtils.perspective

class V3dGeometryCubeLineShader(private val halfSize: Float) : Shader {
    private var shaderProgramID = 0
    var projectionMatrix: FloatArray? = FloatArray(16)

    override fun initShader(gl: GL3) {
        val vertexShaderID = gl.glCreateShader(GL3.GL_VERTEX_SHADER)
        gl.glShaderSource(
            vertexShaderID, 1,
            arrayOf<String>(vertexShaderSource), null
        )
        gl.glCompileShader(vertexShaderID)

        val geometryShaderId = gl.glCreateShader(GL3.GL_GEOMETRY_SHADER)
        gl.glShaderSource(
            geometryShaderId, 1,
            arrayOf<String>(geometryShaderSource), null
        )
        gl.glCompileShader(geometryShaderId)

        val fragmentShaderID = gl.glCreateShader(GL3.GL_FRAGMENT_SHADER)
        gl.glShaderSource(
            fragmentShaderID, 1,
            arrayOf<String>(fragmentShaderSource), null
        )
        gl.glCompileShader(fragmentShaderID)

        shaderProgramID = gl.glCreateProgram()
        gl.glAttachShader(shaderProgramID, vertexShaderID)
        gl.glAttachShader(shaderProgramID, geometryShaderId)
        gl.glAttachShader(shaderProgramID, fragmentShaderID)
        gl.glLinkProgram(shaderProgramID)
    }

    override fun setShader(gl: GL3, modelviewMatrix: FloatArray) {
        gl.glUseProgram(shaderProgramID)

        val projectionMatrixLocation = gl.glGetUniformLocation(shaderProgramID, "projectionMatrix")
        gl.glUniformMatrix4fv(projectionMatrixLocation, 1, false, projectionMatrix, 0)

        val modelviewMatrixLocation = gl.glGetUniformLocation(shaderProgramID, "modelviewMatrix")
        gl.glUniformMatrix4fv(modelviewMatrixLocation, 1, false, modelviewMatrix, 0)

        val halfSizeLocation = gl.glGetUniformLocation(shaderProgramID, "halfSize")
        gl.glUniform1f(halfSizeLocation, this.halfSize)
    }

    override fun initAttributes(gl: GL3) {
        val location = gl.glGetAttribLocation(shaderProgramID, "inVertex")
        gl.glVertexAttribPointer(location, 3, GL3.GL_FLOAT, false, 4 * Sizeof.cl_float, (0 * Sizeof.cl_float).toLong())
        gl.glEnableVertexAttribArray(location)


        val colorLocation = gl.glGetAttribLocation(shaderProgramID, "inColor")
        gl.glVertexAttribPointer(
            colorLocation,
            1,
            GL3.GL_FLOAT,
            false,
            4 * Sizeof.cl_float,
            (3 * Sizeof.cl_float).toLong()
        )
        gl.glEnableVertexAttribArray(colorLocation)
    }

    override fun setupView(aspect: Float) {
        projectionMatrix = perspective(50f, aspect, 0.1f, 100.0f)
    }

    companion object {
        private val vertexShaderSource = "#version 150 core" + "\n" +
                "in  vec3 inVertex;" + "\n" +
                "in  float inColor;" + "\n" +
                "out float ourColor;" + "\n" +
                "uniform mat4 modelviewMatrix;" + "\n" +
                "uniform mat4 projectionMatrix;" + "\n" +
                "void main(void)" + "\n" +
                "{" + "\n" +
                "    ourColor = inColor;" + "\n" +
                "    vec4 inVertexF = vec4(inVertex, 1.0);" + "\n" +
                "    gl_Position = " + "\n" +
                "        inVertexF;" + "\n" +
                "}"

        private val geometryShaderSource = "#version 150 core" + "\n" +
                "layout (points) in;\n" +
                "layout (line_strip, max_vertices = 17) out;\n" +
                "uniform mat4 modelviewMatrix;" + "\n" +
                "uniform mat4 projectionMatrix;" + "\n" +
                "uniform float halfSize;" + "\n" +
                "in float ourColor[];" + "\n" +
                "out float fColor;\n" +
                "\n" +
                "void main() {    \n" +
                "    fColor = ourColor[0];" +
                "    gl_Position = projectionMatrix * modelviewMatrix * (gl_in[0].gl_Position + vec4(-halfSize,  -halfSize, halfSize, 0.0));  //1   \n" +
                "    EmitVertex();\n" +
                "    gl_Position = projectionMatrix * modelviewMatrix * (gl_in[0].gl_Position + vec4(-halfSize,  halfSize, halfSize, 0.0));   //2  \n" +
                "    EmitVertex();\n" +
                "    gl_Position = projectionMatrix * modelviewMatrix * (gl_in[0].gl_Position + vec4(halfSize,  halfSize, halfSize, 0.0));    //3 \n" +
                "    EmitVertex();\n" +
                "    gl_Position = projectionMatrix * modelviewMatrix * (gl_in[0].gl_Position + vec4(halfSize,  -halfSize, halfSize, 0.0));   //4  \n" +
                "    EmitVertex();\n" +
                "    gl_Position = projectionMatrix * modelviewMatrix * (gl_in[0].gl_Position + vec4(halfSize,  -halfSize, -halfSize, 0.0));   //5 \n" +
                "    EmitVertex();\n" +
                "    gl_Position = projectionMatrix * modelviewMatrix * (gl_in[0].gl_Position + vec4(halfSize,  halfSize, -halfSize, 0.0));   //6 \n" +
                "    EmitVertex();\n" +
                "    gl_Position = projectionMatrix * modelviewMatrix * (gl_in[0].gl_Position + vec4(-halfSize,  halfSize, -halfSize, 0.0));    //7 \n" +
                "    EmitVertex();\n" +
                "    gl_Position = projectionMatrix * modelviewMatrix * (gl_in[0].gl_Position + vec4(-halfSize,  -halfSize, -halfSize, 0.0));   //8  \n" +
                "    EmitVertex();\n" +
                "    gl_Position = projectionMatrix * modelviewMatrix * (gl_in[0].gl_Position + vec4(-halfSize,  -halfSize, halfSize, 0.0));   //9  \n" +
                "    EmitVertex();\n" +
                "    gl_Position = projectionMatrix * modelviewMatrix * (gl_in[0].gl_Position + vec4(halfSize,  -halfSize, halfSize, 0.0));    //10 \n" +
                "    EmitVertex();\n" +
                "    gl_Position = projectionMatrix * modelviewMatrix * (gl_in[0].gl_Position + vec4(halfSize,  halfSize, halfSize, 0.0));   //11  \n" +
                "    EmitVertex();\n" +
                "    gl_Position = projectionMatrix * modelviewMatrix * (gl_in[0].gl_Position + vec4(halfSize,  halfSize, -halfSize, 0.0));   //12  \n" +
                "    EmitVertex();\n" +
                "    gl_Position = projectionMatrix * modelviewMatrix * (gl_in[0].gl_Position + vec4(halfSize,  -halfSize, -halfSize, 0.0));    //13 \n" +
                "    EmitVertex();\n" +
                "    gl_Position = projectionMatrix * modelviewMatrix * (gl_in[0].gl_Position + vec4(-halfSize,  -halfSize, -halfSize, 0.0));    //14 \n" +
                "    EmitVertex();\n" +
                "    gl_Position = projectionMatrix * modelviewMatrix * (gl_in[0].gl_Position + vec4(-halfSize,  halfSize, -halfSize, 0.0));    //15 \n" +
                "    EmitVertex();\n" +
                "    gl_Position = projectionMatrix * modelviewMatrix * (gl_in[0].gl_Position + vec4(-halfSize,  halfSize, halfSize, 0.0));  //16   \n" +
                "    EmitVertex();\n" +
                "    gl_Position = projectionMatrix * modelviewMatrix * (gl_in[0].gl_Position + vec4(-halfSize,  -halfSize, halfSize, 0.0));    //17 \n" +
                "    EmitVertex();\n" +
                "    EndPrimitive();" +
                "}  "

        private val fragmentShaderSource = "#version 150 core" + "\n" +
                "in float fColor;" + "\n" +
                "out vec4 outColor;" + "\n" +
                "void main(void)" + "\n" +
                "{" + "\n" +
                "    outColor = vec4((0), 0, 1.0, 1.0);" + "\n" +
                "}"
    }
}
