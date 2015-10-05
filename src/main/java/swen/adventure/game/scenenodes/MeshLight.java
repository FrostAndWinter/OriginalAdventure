package swen.adventure.game.scenenodes;

import swen.adventure.engine.Action;
import swen.adventure.engine.animation.AnimableProperty;
import swen.adventure.engine.animation.Animation;
import swen.adventure.engine.rendering.Material;
import swen.adventure.engine.rendering.maths.Quaternion;
import swen.adventure.engine.rendering.maths.Vector3;
import swen.adventure.engine.scenegraph.Light;
import swen.adventure.engine.scenegraph.MeshNode;
import swen.adventure.engine.scenegraph.SceneNode;
import swen.adventure.engine.scenegraph.TransformNode;

import java.util.Optional;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 5/10/15.
 */
public class MeshLight extends Light {

    private float _baseIntensity;
    private float _intensityVariation = 0.f;

    private AnimableProperty _lightIntensity;

    private MeshNode _meshNode;

    private final Material _lightMaterial;

    public MeshLight(final String id, final TransformNode parent,
                     final String meshName, final String meshDirectory,
                     final Vector3 colour, final float intensity, final LightFalloff falloff) {
        super(id, parent, false, LightType.Point, colour, intensity, Optional.empty(), falloff);

        _baseIntensity = intensity;
        _lightIntensity = new AnimableProperty(intensity);
        _lightMaterial = this.setupMaterial(colour, intensity);

        new MeshNode(id + "Mesh", meshDirectory, meshName, parent).setMaterialOverride(_lightMaterial);

        _lightIntensity.eventValueChanged.addAction(this, (animableProperty, triggeringObject, listener, data) ->  {
            super.setIntensity(animableProperty.value());
            this.setMaterialColour(_lightMaterial, this.colour(), animableProperty.value());
        });
    }

    private void setMaterialColour(Material material, final Vector3 colour, final float intensity) {
        material.setDiffuseColour(colour.multiplyScalar(0.4f * intensity));
        material.setAmbientColour(colour.multiplyScalar(0.6f * intensity));
    }

    private Material setupMaterial(final Vector3 colour, final float intensity) {
        Material material = new Material();
        this.setMaterialColour(material, colour, intensity);
        material.setSpecularColour(Vector3.zero);
        material.setUseAmbient(true);
        return material;
    }

    @Override
    public void setIntensity(float intensity) {
        super.setIntensity(intensity);
        _baseIntensity = intensity;
        this.setIntensityVariation(_intensityVariation);
    }

    @Override
    public void setColour(final Vector3 colour) {
        super.setColour(colour);
        _lightMaterial.setAmbientColour(colour);
    }

    public void setIntensityVariation(float intensityVariation) {
        _intensityVariation = intensityVariation;
        float lowIntensity = _baseIntensity - intensityVariation/2.f;
        float highIntensity = _baseIntensity + intensityVariation/2.f;

        _lightIntensity.stopAnimating();
        _lightIntensity.setValue(lowIntensity);
        new Animation(_lightIntensity, highIntensity);
    }
}
