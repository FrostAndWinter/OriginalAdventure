package swen.adventure.rendering.shaders;

import swen.adventure.rendering.ShaderProgram;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL32.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 19/09/15.
 */
public class DirectionalLightShader extends ShaderProgram {
    public int modelToCameraMatrixUniformRef;
    public int cameraToClipMatrixUniformRef;
    public int normalModelToCameraMatrixUniformRef;
    public int colourUniformRef;

    public int cameraSpaceLightPositionUniformRef;
    public int lightIntensityUniformRef;
    public int ambientLightUniformRef;

    public DirectionalLightShader(String vertexShaderText, String fragmentShaderText) {
        super(vertexShaderText, fragmentShaderText);

        this.modelToCameraMatrixUniformRef = glGetUniformLocation(this.glProgramRef, "modelToCameraMatrixUniform");
        this.colourUniformRef = glGetUniformLocation(this.glProgramRef, "colour");

        this.cameraToClipMatrixUniformRef = glGetUniformLocation(this.glProgramRef, "cameraToClipMatrixUniform");
        this.normalModelToCameraMatrixUniformRef = glGetUniformLocation(this.glProgramRef, "normalModelToCameraMatrixUniform");

        this.cameraSpaceLightPositionUniformRef = glGetUniformLocation(this.glProgramRef, "cameraSpaceLightPosition");
        this.lightIntensityUniformRef = glGetUniformLocation(this.glProgramRef, "lightIntensity");
        this.ambientLightUniformRef = glGetUniformLocation(this.glProgramRef, "ambientIntensity");
    }
}
