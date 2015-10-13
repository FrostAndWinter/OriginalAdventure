package swen.adventure.engine.rendering.shaders.deferredrendering;

import swen.adventure.engine.Utilities;
import swen.adventure.engine.rendering.TextureUnit;
import swen.adventure.engine.rendering.maths.Matrix4;
import swen.adventure.engine.rendering.shaders.PerObjectMaterialShader;

import java.io.IOException;

import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glUniform1i;
import static org.lwjgl.opengl.GL20.glUniformMatrix3fv;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 11/10/15.
 */
public class GeometryPassShader extends PerObjectMaterialShader {
    private final int _nodeToCamera3x3MatrixUniformRef;

    private static String vertexShaderText() {
        try {
            return Utilities.readFile(Utilities.pathForResource("VertexShaderMaterialNormalMap", "vert"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String fragmentShaderText() {
        try {
            return Utilities.readFile(Utilities.pathForResource("deferred-shading", "GeometryPass", "frag"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public GeometryPassShader(String vertexShader, String fragmentShader) {
        super(vertexShader, fragmentShader);

        final int normalMapSamplerRef = glGetUniformLocation(this.glProgramRef(), "normalMapSampler");
        _nodeToCamera3x3MatrixUniformRef = glGetUniformLocation(this.glProgramRef(), "nodeToCamera3x3MatrixUniform");

        this.useProgram();
        glUniform1i(normalMapSamplerRef, TextureUnit.NormalMapUnit.glUnit);
        this.endUseProgram();
    }

    public GeometryPassShader() {
        this(vertexShaderText(), fragmentShaderText());
    }

    @Override
    public void setModelToCameraMatrix(Matrix4 matrix) {
        super.setModelToCameraMatrix(matrix);

        glUniformMatrix3fv(_nodeToCamera3x3MatrixUniformRef, false, matrix.getMatrix3().toFloatBuffer());
    }
}
