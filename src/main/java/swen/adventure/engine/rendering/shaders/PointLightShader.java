package swen.adventure.engine.rendering.shaders;

import swen.adventure.engine.rendering.ShaderProgram;

import static org.lwjgl.opengl.GL20.glGetUniformLocation;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 19/09/15.
 * A PointLightShader is a simple shader that lights objects based on a single point light and ambient light.
 * It takes a single colour parameter, and does not allow textures.
 */
public class PointLightShader extends ShaderProgram {
    public final int modelToCameraMatrixUniformRef;
    public final int cameraToClipMatrixUniformRef;
    public final int normalModelToCameraMatrixUniformRef;
    public final int colourUniformRef;

    public final int cameraSpaceLightPositionUniformRef;
    public final int lightIntensityUniformRef;
    public final int ambientLightUniformRef;

    public PointLightShader(String vertexShaderText, String fragmentShaderText) {
        super(vertexShaderText, fragmentShaderText);

        this.modelToCameraMatrixUniformRef = glGetUniformLocation(this.glProgramRef(), "modelToCameraMatrixUniform");
        this.colourUniformRef = glGetUniformLocation(this.glProgramRef(), "colour");
        this.cameraToClipMatrixUniformRef = glGetUniformLocation(this.glProgramRef(), "cameraToClipMatrixUniform");
        this.normalModelToCameraMatrixUniformRef = glGetUniformLocation(this.glProgramRef(), "normalModelToCameraMatrixUniform");

        this.cameraSpaceLightPositionUniformRef = glGetUniformLocation(this.glProgramRef(), "cameraSpaceLightPosition");
        this.lightIntensityUniformRef = glGetUniformLocation(this.glProgramRef(), "lightIntensity");
        this.ambientLightUniformRef = glGetUniformLocation(this.glProgramRef(), "ambientIntensity");
    }
}
