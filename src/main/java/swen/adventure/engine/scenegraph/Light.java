package swen.adventure.engine.scenegraph;

import org.lwjgl.BufferUtils;
import swen.adventure.engine.datastorage.BundleObject;
import swen.adventure.engine.rendering.maths.Matrix4;
import swen.adventure.engine.rendering.maths.Vector3;
import swen.adventure.engine.rendering.maths.Vector4;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 26/09/15.
 *
 * A Light is a light source in the scene. Currently, point, directional, and ambient variants are supported.
 * A variety of different attributes can be set on the lights.
 */
public final class Light extends SceneNode {

    public enum LightType {
        Ambient,
        Directional,
        Point
    }

    public enum LightFalloff {
        None(0),
        Linear(1),
        Quadratic(2);

        public final float glValue;

        LightFalloff(float glValue) {
            this.glValue = glValue;
        }

        public static LightFalloff fromString(String s) {
            switch (s) {
                case "None":
                    return LightFalloff.None;
                case "Linear":
                    return LightFalloff.Linear;
                case "Quadratic":
                    return LightFalloff.Quadratic;
                default:
                    throw new RuntimeException(s + " is not a valid LightFalloff.");
            }
        }
    }

    private static final int MaxLights = 32;
    private static final int PerLightDataSize = 32;
    private static final float LightAttenuationFactor = 0.00006f;

    public static final int BufferSizeInBytes = Vector4.sizeInBytes + //ambient light
            4 + //num dynamic lights
            4 +  //light attenuation factor
            4 * 2 + //padding
            PerLightDataSize * MaxLights;

    public final LightType type;

    private Vector3 _colour;
    private float _intensity;
    private boolean _on;
    public final Optional<Vector3> direction;
    public final LightFalloff falloff;

    private Light(final String id, final TransformNode parent, final boolean isDynamic,
                  final LightType type, final Vector3 colour, final float intensity,
                  final Optional<Vector3> direction, final LightFalloff falloff) {
        super(id, parent, isDynamic);

        float colourMagnitude = colour.length();

        this.type = type;
        _intensity = intensity * colourMagnitude;
        _on = true;
        _colour = colour.normalise();
        this.direction = direction.map(Vector3::normalise);
        this.falloff = falloff;
    }

    @Override
    public BundleObject toBundle() {
        return super.toBundle()
                .put("colour", _colour)
                .put("intensity", _intensity)
                // convert the enum values to their names so the valueOf() method can return their instance
                .put("type", type.toString())
                .put("falloff", falloff.toString());
    }

    private static Light createSceneNodeFromBundle(BundleObject bundle,
                                                           Function<String, TransformNode> findParentFunction) { //FIXME move out of the Light class.
        String parentId = bundle.getString("parentId");
        TransformNode parent = findParentFunction.apply(parentId);

        String id = bundle.getString("id");
        float intensity = bundle.getFloat("intensity");
        boolean isDynamic = bundle.getBoolean("isDynamic");
        Vector3 colour = bundle.getVector3("colour");
        LightType lightType = LightType.valueOf(bundle.getString("type"));
        LightFalloff lightFalloff = LightFalloff.valueOf(bundle.getString("falloff"));

        Optional<Vector3> direction;
        if(bundle.hasProperty("direction"))
            direction = Optional.of(bundle.getVector3("direction"));
        else
            direction = Optional.empty();

        return new Light(id, parent, isDynamic, lightType, colour, intensity, direction, lightFalloff);
    }

    public static Light createAmbientLight(final String id, final TransformNode parent, final Vector3 colour, final float intensity) {
        return new Light(id, parent, false, LightType.Ambient, colour, intensity, Optional.empty(), LightFalloff.None);
    }

    public static Light createDirectionalLight(final String id, final TransformNode parent,
                                               final Vector3 colour, final float intensity,
                                               final Vector3 fromDirection) {
        return new Light(id, parent, false, LightType.Directional, colour, intensity, Optional.of(fromDirection), LightFalloff.None);
    }

    public static Light createPointLight(final String id, final TransformNode parent,
                                               final Vector3 colour, final float intensity,
                                               final LightFalloff falloff) {
        return new Light(id, parent, false, LightType.Point, colour, intensity, Optional.empty(), falloff);
    }

    /** The light's intensity. */
    public float intensity() {
        return _intensity;
    }

    public void setIntensity(float intensity) {
        _intensity = intensity;
    }

    /** The light's colour as a unit vector. */
    public Vector3 colour() {
        return _colour;
    }

    public void setColour(final Vector3 colour) {
        _colour = colour;
    }

    /** Returns whether the light is currently contributing to the scene. */
    public boolean isOn() {
        return _on;
    }

    public void setOn(boolean isOn) {
        _on = isOn;
    }

    /** @return this light's colour multiplied by its intensity. */
    public Vector3 colourVector() {
        return this._colour.multiplyScalar(_intensity);
    }

    private void addLightDataToBuffer(ByteBuffer buffer, Matrix4 worldToCameraMatrix) {
        if (this.type == LightType.Ambient) {
            throw new RuntimeException("Ambient light with id " + id + " should not be converted to a ByteBuffer.\n " +
                    "Use method Light.toLightBlock instead.");
        }

        Vector4 localSpacePosition;
        if (this.type == LightType.Directional) {
            localSpacePosition = new Vector4(this.direction.get(), 0.f);
        } else { //this.type == LightType.Point
            localSpacePosition = new Vector4(0.f, 0.f, 0.f, 1.f);
        }

        //The position vector will have a 0 w component if it's directional.
        Vector4 positionInCameraSpace = worldToCameraMatrix.multiply(this.nodeToWorldSpaceTransform().multiply(localSpacePosition));
        Vector3 intensity = this.colourVector();

//        Structure:
//        struct PerLightData {
//            Vector4 positionInCameraSpace; //16 bytes
//            Vector4 intensity; //where xyz are the intensity colour vectors and w is the falloff; 16 bytes
//        }

        for (int i = 0; i < 4; i++) {
            buffer.putFloat(positionInCameraSpace.v[i]);
        }
        for (int i = 0; i < 3; i++) {

            if (_on) {
                buffer.putFloat(intensity.v[i]);
            } else {
                buffer.putFloat(0);
            }
        }

        buffer.putFloat(this.falloff.glValue);
    }

    /**
     * Converts a set of lights to a ByteBuffer that can be passed as a uniform block to the shader program.
     * @param lights The set of lights in the scene.
     * @param worldToCameraMatrix A transformation to convert a world position to a camera space position.
     * @return A byte buffer representing the GL uniform block.
     */
    public static ByteBuffer toLightBlock(List<Light> lights, Matrix4 worldToCameraMatrix) {
        Vector3 ambientIntensity = lights.stream()
                .filter((light) -> light.type == LightType.Ambient)
                .map(Light::colourVector)
                .reduce(Vector3::add)
                .orElse(new Vector3(0.f, 0.f, 0.f));

        List<Light> otherLights = lights.stream()
                .filter(light -> light.type != LightType.Ambient)
                .collect(Collectors.toList());

        if (otherLights.size() > MaxLights) {
            throw new RuntimeException("The scene has " + lights.size() + " dynamic lights, which is greater than the maximum of " + MaxLights);
        }

//        Structure:
//        struct LightBlock {
//            Vector4 ambientIntensity;
//            int numDynamicLights;
//            int padding1;
//            float lightAttenuationFactor;
//            float padding2;
//            PerLightData lights[MaxLights];
//        }


        ByteBuffer buffer = BufferUtils.createByteBuffer(BufferSizeInBytes);

        buffer.putFloat(ambientIntensity.x);
        buffer.putFloat(ambientIntensity.y);
        buffer.putFloat(ambientIntensity.z);
        buffer.putFloat(1.f);
        buffer.putInt(otherLights.size());
        buffer.putInt(0); //padding
        buffer.putFloat(LightAttenuationFactor);
        buffer.putFloat(0.f); //padding2

        for (Light dynamicLight : otherLights) {
            dynamicLight.addLightDataToBuffer(buffer, worldToCameraMatrix);
        }

        buffer.rewind();
        return buffer;
    }

    public LightType getType() {
        return type;
    }

    public Vector3 getColour() {
        return _colour;
    }

    public Float getIntensity() {
        return _intensity;
    }

    public Optional<Vector3> getDirection() {
        return direction;
    }

    public LightFalloff getFalloff() {
        return falloff;
    }
}
