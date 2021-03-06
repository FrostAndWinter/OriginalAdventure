/* Contributor List  */ 
 /* Thomas Roughton (roughtthom) (300313924) */ 
 /* Liam O'Niell (oneilliam) (300312734) */ 
 /* Joseph Bennett (bennetjose) (300319773) */ 
 /* David Barnett (barnetdavi) (300313764) */ 
 /* Daniel Braithwaite (braithdani) (300313770) */ 
 package swen.adventure.game.scenenodes;

import swen.adventure.engine.Action;
import swen.adventure.engine.Event;
import swen.adventure.engine.animation.AnimableProperty;
import swen.adventure.engine.animation.Animation;
import swen.adventure.engine.rendering.Material;
import swen.adventure.engine.rendering.maths.Vector3;
import swen.adventure.engine.scenegraph.Light;
import swen.adventure.engine.scenegraph.MeshNode;
import swen.adventure.engine.scenegraph.SceneNode;
import swen.adventure.engine.scenegraph.TransformNode;

import java.util.Collections;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 5/10/15.
 */
public class FlickeringLight extends AdventureGameObject {

    private static final float LightAnimationTime = 0.3f;

    private boolean _isOn = true;
    private boolean _isAnimatingToggle = false;

    private float _baseIntensity;
    private float _intensityVariation = 0.f;

    private AnimableProperty _lightIntensity;

    private final Material _lightMaterial;

    public final Event<FlickeringLight, Player> eventLightTurnedOn = new Event<>("LightTurnedOn", this);
    public final Event<FlickeringLight, Player> eventLightTurnedOff = new Event<>("LightTurnedOff", this);

    public static final Action<SceneNode, Player, FlickeringLight> actionTurnLightOn = (eventObject, player, light, data) -> {
        if  (!light.isOn()) {
            light._isAnimatingToggle = true;
            Animation animation = new Animation(light._lightIntensity, LightAnimationTime, light._baseIntensity);
            animation.eventAnimationDidComplete.addAction(light, (animation1, triggeringObject, light1, data1) -> {
                light.setIntensity(light._baseIntensity);
                light._isAnimatingToggle = false;
            });
            light._isOn = true;

            light.mesh().ifPresent(flameMesh -> flameMesh.setEnabled(true));
            light.eventLightTurnedOn.trigger(player, Collections.emptyMap());
        }
    };

    public static final Action<SceneNode, Player, FlickeringLight> actionTurnLightOff = (eventObject, player, light, data) -> {
        if (light.isOn()) {
            light._isAnimatingToggle = true;
            Animation animation = new Animation(light._lightIntensity, LightAnimationTime, 0.f);
            light._isOn = false;
            animation.eventAnimationDidComplete.addAction(light, (animation1, animation2, light1, data1) -> {
                light.mesh().ifPresent(meshNode -> meshNode.setEnabled(false));
                light._isAnimatingToggle = false;
            });
            light.eventLightTurnedOff.trigger(player, Collections.emptyMap());
        }
    };

    public FlickeringLight(final String id, final TransformNode parent,
                           final String meshName, final String meshDirectory,
                           final Vector3 colour, final float intensity, final Light.LightFalloff falloff) {
        super(id, parent, id);

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
        material.setDiffuseColour(colour.multiplyScalar(0.25f));
        material.setAmbientColour(colour.multiplyScalar(intensity));
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

    /**
     * @param intensity intensity of the light
     */
    public void setIntensity(float intensity) {
        _baseIntensity = intensity;
        this.setIntensityVariation(_intensityVariation);
    }

    /**
     * @param colour color of the light as a vector
     */
    public void setColour(final Vector3 colour) {
        this.light().ifPresent(light -> light.setColour(colour));
        this.setMaterialColour(_lightMaterial, colour, _lightIntensity.value());
    }

    /**
     * @param intensityVariation how much the intensity should change (i.e. flicker)
     */
    public void setIntensityVariation(float intensityVariation) {
        _intensityVariation = intensityVariation;

        if (_isOn) {
            float lowIntensity = _baseIntensity - intensityVariation / 2.f;
            float highIntensity = _baseIntensity + intensityVariation / 2.f;

            _lightIntensity.stopAnimating();
            _lightIntensity.setValue(lowIntensity);
            new Animation(_lightIntensity, highIntensity);
        }
    }

    /**
     * @param isOn true if the light should be set to on
     */
    public void setOn(boolean isOn) {
        _isOn = isOn;
        if (isOn) {
            this.setIntensity(_baseIntensity);
            this.mesh().ifPresent(flameMesh -> flameMesh.setEnabled(true));
        } else {
            _lightIntensity.stopAnimating();
            _lightIntensity.setValue(0.f);
            this.mesh().ifPresent(meshNode -> meshNode.setEnabled(false));
        }
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