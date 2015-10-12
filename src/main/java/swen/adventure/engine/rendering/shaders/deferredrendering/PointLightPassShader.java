package swen.adventure.engine.rendering.shaders.deferredrendering;

import swen.adventure.engine.Utilities;
import swen.adventure.engine.rendering.TextureUnit;
import swen.adventure.engine.scenegraph.Light;

import java.io.IOException;
import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL15.GL_DYNAMIC_DRAW;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glUniform1i;
import static org.lwjgl.opengl.GL30.glBindBufferRange;
import static org.lwjgl.opengl.GL31.GL_UNIFORM_BUFFER;
import static org.lwjgl.opengl.GL31.glGetUniformBlockIndex;
import static org.lwjgl.opengl.GL31.glUniformBlockBinding;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 12/10/15.
 */
public class PointLightPassShader extends LightPassShader {

    private static final int LightBlockIndex = 2;

    private int _lightUniformBufferRef;

    private static String vertexShaderText() {
        try {
            return Utilities.readFile(Utilities.pathForResource("deferred-shading", "PointLightPass", "vert"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String fragmentShaderText() {
        try {
            return Utilities.readFile(Utilities.pathForResource("deferred-shading", "PointLightPass", "frag"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public PointLightPassShader() {
        super(vertexShaderText(), fragmentShaderText());

        //Setup the uniform buffers
        int lightBlock = glGetUniformBlockIndex(this.glProgramRef(), "PointLight");

            glUniformBlockBinding(this.glProgramRef(), lightBlock, LightBlockIndex);
            _lightUniformBufferRef = glGenBuffers();
            glBindBuffer(GL_UNIFORM_BUFFER, _lightUniformBufferRef);
            glBufferData(GL_UNIFORM_BUFFER, Light.PointLightBufferSizeInBytes, GL_DYNAMIC_DRAW);

            //Bind the static buffer
            glBindBufferRange(GL_UNIFORM_BUFFER, LightBlockIndex, _lightUniformBufferRef, 0, Light.PointLightBufferSizeInBytes);

    }

    public void setPointLightData(ByteBuffer lightData) {
        glBindBuffer(GL_UNIFORM_BUFFER, _lightUniformBufferRef);
        glBufferSubData(GL_UNIFORM_BUFFER, 0, lightData);
        glBindBuffer(GL_UNIFORM_BUFFER, 0);
    }
}
