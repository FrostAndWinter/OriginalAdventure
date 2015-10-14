/* Contributor List  */ 
 /* Thomas Roughton (roughtthom) (300313924) */ 
 package swen.adventure.engine.datastorage;

import swen.adventure.engine.Utilities;
import swen.adventure.engine.rendering.Material;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 5/10/15.
 * MaterialLibrary is an abstraction around a material library (.mtllib) file, and provides easy ways to access the materials in those files.
 */
public class MaterialLibrary {

    private static final Map<String, MaterialLibrary> _materialLibraries = new HashMap<>();

    private final Map<String, Material> _materials;

    private MaterialLibrary(Map<String, Material> materialMap) {
        _materials = materialMap;
    }

    /**
     * @return a mapping from material names to the materials.
     */
    public Map<String, Material> materials() {
        return _materials;
    }

    /**
     * Returns the material with a particular name.
     * @param name The name of the material
     * @return The material.
     */
    public Material materialWithName(String name) {
        return _materials.get(name);
    }

    /**
     * Returns the material library with a particular name.
     * @param fileName The name of the mttlib file, including its extension
     * @return The material library, or null if it can't be found.
     */
    public static MaterialLibrary libraryWithName(String fileName) {
        return MaterialLibrary.libraryWithName(null, fileName);
    }

    /**
     * Returns the material library with a particular name and directory.
     * @param directory The directory in resources in which the mtllib file is located
     * @param fileName The name of the mttlib file, including its extension
     * @return The material library, or null if it can't be found.
     */
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