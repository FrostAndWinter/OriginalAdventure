package swen.adventure.rendering;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL32.GL_GEOMETRY_SHADER;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 19/09/15.
 */
public class ShaderProgram {
    private final int _glProgramRef;

    public ShaderProgram(String vertexShaderText, String fragmentShaderText) {
        List<Integer> shaders = new ArrayList<>(2);

        shaders.add(ShaderProgram.createShader(GL_VERTEX_SHADER, vertexShaderText));
        shaders.add(ShaderProgram.createShader(GL_FRAGMENT_SHADER, fragmentShaderText));

        _glProgramRef = ShaderProgram.createProgram(shaders);
    }

    protected int glProgramRef() {
        return _glProgramRef;
    }

    public void useProgram() {
        glUseProgram(_glProgramRef);
    }

    public void endUseProgram() {
        glUseProgram(0);
    }

    public static int createProgram(List<Integer> shaderList){
        int program = glCreateProgram();

        for (int shader : shaderList) {
            glAttachShader(program, shader);
        }

        glLinkProgram(program);

        int status = glGetProgrami(program, GL_LINK_STATUS);
        if (status == GL_FALSE) {
            int infoLogLength = glGetProgrami(program, GL_INFO_LOG_LENGTH);

            String error = glGetProgramInfoLog(program, infoLogLength);
            System.err.printf("Linker failure: %s\n", error);
        }

        for (int shader : shaderList) {
            glDetachShader(program, shader);
        }

        return program;
    }

    public static int createShader(int shaderType, String shaderText) {
        int shader = glCreateShader(shaderType);
        glShaderSource(shader, shaderText);
        glCompileShader(shader);

        int status = glGetShaderi(shader, GL_COMPILE_STATUS);
        if (status == GL_FALSE) {
            int infoLogLength = glGetShaderi(shader, GL_INFO_LOG_LENGTH);

            String info = glGetShaderInfoLog(shader, infoLogLength);

            String strShaderType = null;
            switch (shaderType) {
                case GL_VERTEX_SHADER: strShaderType = "vertex"; break;
                case GL_GEOMETRY_SHADER: strShaderType = "geometry"; break;
                case GL_FRAGMENT_SHADER: strShaderType = "fragment"; break;
            }

            System.err.printf("Compile failure in %s shader:\n%s\n", strShaderType, info);
        }
        return shader;
    }
}
