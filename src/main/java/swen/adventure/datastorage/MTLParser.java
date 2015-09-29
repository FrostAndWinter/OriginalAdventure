package swen.adventure.datastorage;

import swen.adventure.rendering.Material;
import swen.adventure.rendering.maths.Vector3;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 29/09/15.
 */
public class MTLParser {
    private enum Attribute {
        AmbientColour("Ka"),
        DiffuseColour("Kd"),
        SpecularColour("Ks"),
        Specularity("Ns"),
        Transparency("d", "Tr"),
        IlluminationMode("illum"),
        NewMaterial("newmtl"),
        AmbientMap("map_Ka"),
        DiffuseMap("map_Kd"),
        SpecularColourMap("map_Ks"),
        SpecularityMap("map_Ns");

        private String[] _tokens;

        Attribute(String... tokens) {
            _tokens = tokens;
        }

        private static Map<String, Attribute> _stringsToAttributes = new HashMap<>();
        static {
            for (Attribute attribute : Attribute.values()) {
                for (String token : attribute._tokens) {
                    _stringsToAttributes.put(token, attribute);
                }
            }
        }
    }

    public static Map<String, Material> parseMaterialFile(List<String> materialFile) {
        Map<String, Material> materialMap = new HashMap<>();

        String materialName = null;
        Vector3 ambientColour, diffuseColour, specularColour;

        return materialMap;
    }
}
