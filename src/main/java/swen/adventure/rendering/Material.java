package swen.adventure.rendering;

import swen.adventure.rendering.maths.Vector3;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 27/09/15.
 */
public class Material {

    /** The ambient colour for the material. When not illuminated, it will 'glow' with this colour. */
    public final Vector3 ambientColour;

    /** The colour of the material's diffuse reflection – this is the main factor for the material's appearance. */
    public final Vector3 diffuseColour;

    /** The colour of specular reflections. For most materials, this will be white.
     *  However, metals have a specular colour closer to their diffuse colour, and there are certain other exceptions. */
    public final Vector3 specularColour;

    /** Transparency is a value in the range [0, 1], and directly controls the output alpha of the material. */
    public final float transparency;

    /** A value for the gaussian specularity of the material in the range [0, 1], where 0 is perfectly smooth and 1 is very rough. */
    public final float specular;

    public Material(Vector3 ambientColour, Vector3 diffuseColour, Vector3 specularColour, float transparency, float specular) {
        this.ambientColour = ambientColour;
        this.diffuseColour = diffuseColour;
        this.specularColour = specularColour;
        this.transparency = transparency;
        this.specular = specular;
    }

}
