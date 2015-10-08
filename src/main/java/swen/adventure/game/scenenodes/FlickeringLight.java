package swen.adventure.game.scenenodes;

import swen.adventure.engine.Action;
import swen.adventure.engine.Event;
import swen.adventure.engine.animation.AnimableProperty;
import swen.adventure.engine.animation.Animation;
import swen.adventure.engine.rendering.Material;
import swen.adventure.engine.rendering.maths.Vector3;
import swen.adventure.engine.scenegraph.*;

import java.util.Collections;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 5/10/15.
 */
public class FlickeringLight extends GameObject {

    private static final float LightAnimationTime = 0.3f;

    private boolean _isOn = true;
    private boolean _isAnimatingToggle = false;

    private float _baseIntensity;
    private float _intensityVariation = 0.f;

    private AnimableProperty _lightIntensity;

    private final Material _lightMaterial;

    public final Event<FlickeringLight, Player> eventLightToggled = new Event<>("eventLightToggled", this);

    public static final Action<SceneNode, Player, FlickeringLight> actionToggleLight = (eventObject, player, light, data) -> {
        light._isAnimatingToggle = true;
        if (light.isOn()) {
            Animation animation = new Animation(light._lightIntensity, LightAnimationTime, 0.f);
            light._isOn = false;
            animation.eventAnimationDidComplete.addAction(light, (animation1, animation2, light1, data1) -> {
                light.mesh().ifPresent(meshNode -> meshNode.setEnabled(false));
                light._isAnimatingToggle = false;
            });

        } else {
            light.mesh().ifPresent(flameMesh -> flameMesh.setEnabled(true));
            Animation animation = new Animation(light._lightIntensity, LightAnimationTime, light._baseIntensity);
            animation.eventAnimationDidComplete.addAction(light, (animation1, triggeringObject, light1, data1) -> {
                light.setIntensity(light._baseIntensity);
                light._isAnimatingToggle = false;
            });
            light._isOn = true;
        }
        light.eventLightToggled.trigger(player, Collections.emptyMap());
    };

    public FlickeringLight(final String id, final TransformNode parent,
                           final String meshName, final String meshDirectory,
                           final Vector3 colour, final float intensity, final Light.LightFalloff falloff) {
        super(id, parent);

        _baseIntensity = intensity;
        _lightIntensity = new AnimableProperty(intensity);
        _lightMaterial = this.setupMaterial(colour, intensity);

        final String lightID = id + "Light";
        Light light = parent.findNodeWithIdOrCreate(lightID, () -> Light.createPointLight(lightID, parent, colour, intensity, falloff));

        this.setLight(light);

        final String meshID = id + "Mesh";

        MeshNode mesh = parent.findNodeWithIdOrCreate(meshID, () -> new MeshNode(meshID, meshDirectory, meshName, parent));
        mesh.setMaterialOverride(_lightMaterial);
        this.setMesh(mesh);

        _lightIntensity.eventValueChanged.addAction(this,  (animableProperty, triggeringObject, listener, data) -> {
            this.light().ifPresent(lightNode -> {
                lightNode.setIntensity(animableProperty.value());
                this.setMaterialColour(_lightMaterial, lightNode.colour(), animableProperty.value());
            });
        });
    }

    private void setMaterialColour(Material material, final Vector3 colour, final float intensity) {
        material.setDiffuseColour(colour.multiplyScalar(0.4f * intensity));
        material.setAmbientColour(colour.multiplyScalar(0.6f * intensity));
        if (this._isAnimatingToggle) {
            material.setOpacity(intensity / _baseIntensity);
        }
    }

    private Material setupMaterial(final Vector3 colour, final float intensity) {
        Material material = new Material();
        this.setMaterialColour(material, colour, intensity);
        material.setSpecularColour(Vector3.zero);
        material.setUseAmbient(true);
        return material;
    }

    public void setIntensity(float intensity) {
        _baseIntensity = intensity;
        this.setIntensityVariation(_intensityVariation);
    }

    public void setColour(final Vector3 colour) {
        this.light().ifPresent(light -> light.setColour(colour));
        this.setMaterialColour(_lightMaterial, colour, _lightIntensity.value());
    }

    public void setIntensityVariation(float intensityVariation) {
        _intensityVariation = intensityVariation;
        float lowIntensity = _baseIntensity - intensityVariation/2.f;
        float highIntensity = _baseIntensity + intensityVariation/2.f;

        _lightIntensity.stopAnimating();
        _lightIntensity.setValue(lowIntensity);
        new Animation(_lightIntensity, highIntensity);
    }

    public boolean isOn() {
        return _isOn;
    }

    public Vector3 getColour() {
        return light().get().getColour();
    }

    public Float getIntensity() {
        return _baseIntensity;
    }

    public Light.LightFalloff getFalloff() {
        return light().get().getFalloff();
    }

    public Float getIntensityVariation() {
        return _intensityVariation;
    }
}
