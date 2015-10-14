/* Contributor List  */ 
 /* Thomas Roughton (roughtthom) (300313924) */ 
 package swen.adventure.engine.rendering.shaders;

import swen.adventure.engine.Utilities;
import swen.adventure.engine.rendering.TextureUnit;
import swen.adventure.engine.rendering.maths.Matrix4;

import java.io.IOException;

import static org.lwjgl.opengl.GL20.*;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 3/10/15.
 * This class describes a shader that, in addition to the functionality provided by PerObjectMaterialShader, also allows per-material normal maps.
 */
public class GaussianMaterialsNormalMapsShader extends PerObjectMaterialShader {

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
            return Utilities.readFile(Utilities.pathForResource("GaussianMaterialsNormalMap", "frag"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public GaussianMaterialsNormalMapsShader(String vertexShader, String fragmentShader) {
        super(vertexShader, fragmentShader);

        final int normalMapSamplerRef = glGetUniformLocation(this.glProgramRef(), "normalMapSampler");
        _nodeToCamera3x3MatrixUniformRef = glGetUniformLocation(this.glProgramRef(), "nodeToCamera3x3MatrixUniform");

        this.useProgram();
        glUniform1i(normalMapSamplerRef, TextureUnit.NormalMapUnit.glUnit);
        this.endUseProgram();
    }

    public GaussianMaterialsNormalMapsShader() {
        this(vertexShaderText(), fragmentShaderText());
    }

    @Override
    public void setModelToCameraMatrix(Matrix4 matrix) {
        super.setModelToCameraMatrix(matrix);

        glUniformMatrix3fv(_nodeToCamera3x3MatrixUniformRef, false, matrix.getMatrix3().toFloatBuffer());
    }
}