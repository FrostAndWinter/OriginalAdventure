package swen.adventure.rendering;

import com.jogamp.opengl.GL3;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 19/09/15.
 */
public class ShaderProgram {
    public int glProgramRef;
    public int modelToCameraMatrixUniformRef;
    public int cameraToClipMatrixUniformRef;
    public int normalModelToCameraMatrixUniformRef;
    public int colourUniformRef;

    public int cameraSpaceLightPositionUniformRef;
    public int lightIntensityUniformRef;
    public int ambientLightUniformRef;

    public ShaderProgram(GL3 gl, String vertexShaderText, String fragmentShaderText) {
        List<Integer> shaders = new ArrayList<>(2);

        shaders.add(ShaderProgram.createShader(gl, GL3.GL_VERTEX_SHADER, vertexShaderText));
        shaders.add(ShaderProgram.createShader(gl, GL3.GL_FRAGMENT_SHADER, fragmentShaderText));

        this.glProgramRef = ShaderProgram.createProgram(gl, shaders);

        this.modelToCameraMatrixUniformRef = gl.glGetUniformLocation(this.glProgramRef, "modelToCameraMatrixUniform");
        this.colourUniformRef = gl.glGetUniformLocation(this.glProgramRef, "colour");

        this.cameraToClipMatrixUniformRef = gl.glGetUniformLocation(this.glProgramRef, "cameraToClipMatrixUniform");
        this.normalModelToCameraMatrixUniformRef = gl.glGetUniformLocation(this.glProgramRef, "normalModelToCameraMatrixUniform");

        this.cameraSpaceLightPositionUniformRef = gl.glGetUniformLocation(this.glProgramRef, "cameraSpaceLightPosition");
        this.lightIntensityUniformRef = gl.glGetUniformLocation(this.glProgramRef, "lightIntensity");
        this.ambientLightUniformRef = gl.glGetUniformLocation(this.glProgramRef, "ambientIntensity");
    }

    public static int createProgram(GL3 gl, List<Integer> shaderList){
        int program = gl.glCreateProgram();

        for (int shader : shaderList) {
            gl.glAttachShader(program, shader);
        }

        gl.glLinkProgram(program);

        IntBuffer statusBuffer = IntBuffer.allocate(1);
        gl.glGetProgramiv (program, GL3.GL_LINK_STATUS, statusBuffer);
        if (statusBuffer.get(0) == GL3.GL_FALSE) {
            IntBuffer infoLogLengthBuffer = IntBuffer.allocate(1);
            gl.glGetProgramiv(program, GL3.GL_INFO_LOG_LENGTH, infoLogLengthBuffer);

            ByteBuffer strInfoLog = ByteBuffer.allocate(infoLogLengthBuffer.get(0) + 1);
            gl.glGetProgramInfoLog(program, infoLogLengthBuffer.get(0), null, strInfoLog);
            System.err.printf("Linker failure: %s\n", new String(strInfoLog.array(), Charset.defaultCharset()));
        }

        for (int shader : shaderList) {
            gl.glDetachShader(program, shader);
        }

        return program;
    }

    public static int createShader(GL3 gl, int shaderType, String shaderText) {
        int shader = gl.glCreateShader(shaderType);
        gl.glShaderSource(shader, 1, new String[]{shaderText}, null);
        gl.glCompileShader(shader);

        IntBuffer statusBuffer = IntBuffer.allocate(1);
        gl.glGetShaderiv(shader, GL3.GL_COMPILE_STATUS, statusBuffer);
        if (statusBuffer.get(0) == GL3.GL_FALSE) {
            IntBuffer infoLogLengthBuffer = IntBuffer.allocate(1);
            gl.glGetShaderiv(shader, GL3.GL_INFO_LOG_LENGTH, infoLogLengthBuffer);
            int infoLogLength = infoLogLengthBuffer.get(0);

            byte[] strInfoLog = new byte[infoLogLength + 1];
            ByteBuffer byteBuffer = ByteBuffer.wrap(strInfoLog);
            gl.glGetShaderInfoLog(shader, infoLogLength, null, byteBuffer);

            String strShaderType = null;
            switch (shaderType) {
                case GL3.GL_VERTEX_SHADER: strShaderType = "vertex"; break;
                case GL3.GL_GEOMETRY_SHADER: strShaderType = "geometry"; break;
                case GL3.GL_FRAGMENT_SHADER: strShaderType = "fragment"; break;
            }

            System.err.printf("Compile failure in %s shader:\n%s\n", strShaderType, new String(byteBuffer.array(), Charset.defaultCharset()));
        }
        return shader;
    }
}
