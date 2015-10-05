package swen.adventure.engine.datastorage;

import swen.adventure.engine.Utilities;
import swen.adventure.engine.rendering.Material;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 5/10/15.
 */
public class MaterialLibrary {

    private static final Map<String, MaterialLibrary> _materialLibraries = new HashMap<>();

    private final Map<String, Material> _materials;

    private MaterialLibrary(Map<String, Material> materialMap) {
        _materials = materialMap;
    }

    public Material materialWithName(String name) {
        return _materials.get(name);
    }

    public static MaterialLibrary libraryWithName(String fileName) {
        return MaterialLibrary.libraryWithName(null, fileName);
    }

    public static MaterialLibrary libraryWithName(String directory, String fileName) {
        String libraryName = directory + File.separator + fileName;
        MaterialLibrary library = _materialLibraries.get(libraryName);

        if (library == null) {
            String path = Utilities.pathForResource(directory, fileName, null);
            try {
                Map<String, Material> libraryMap = MTLParser.parse(new File(path), directory);
                library = new MaterialLibrary(libraryMap);
                _materialLibraries.put(libraryName, library);
            } catch (FileNotFoundException e) {
                System.err.println("Error loading material library: " + e);
            }
        }
        return library;
    }
}
