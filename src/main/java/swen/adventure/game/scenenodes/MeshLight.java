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

    public MeshLight(final String id, final TransformNode parent, boolean isDynamic,
                     final String meshName, final String meshDirectory,
                     final Vector3 colour, final float intensity, final LightFalloff falloff) {
        super(id, parent, isDynamic, LightType.Point, colour, intensity, Optional.empty(), falloff);

        _baseIntensity = intensity;
        _lightIntensity = new AnimableProperty(intensity);
        _lightMaterial = this.setupMaterial(colour, intensity);

        new MeshNode(id, meshDirectory, meshName, parent).setMaterialOverride(_lightMaterial);

        _lightIntensity.eventValueChanged.addAction(this, (animableProperty, triggeringObject, listener, data) ->  {
            super.setIntensity(animableProperty.value());
            _lightMaterial.setAmbientColour(colour.multiplyScalar(intensity));
        });
    }

    private Material setupMaterial(final Vector3 colour, final float intensity) {
        Material material = new Material();
        material.setDiffuseColour(Vector3.zero);
        material.setAmbientColour(colour.multiplyScalar(intensity));
        material.setSpecularColour(Vector3.zero);
        material.setUseAmbient(true);
        material.setOpacity(0.3f);
        return material;
    }

    @Override
    public void setIntensity(float intensity) {
        super.setIntensity(intensity);
        _baseIntensity = intensity;
        this.setIntensityVariation(_intensityVariation);

    }

    public void setIntensityVariation(float intensityVariation) {
        _intensityVariation = intensityVariation;
        float lowIntensity = this.intensity() - intensityVariation/2.f;
        float highIntensity = this.intensity() + intensityVariation/2.f;

        _lightIntensity.stopAnimating();
        _lightIntensity.setValue(lowIntensity);
        new Animation(_lightIntensity, highIntensity);
    }
}
