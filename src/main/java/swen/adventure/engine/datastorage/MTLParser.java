package swen.adventure.engine.datastorage;

import swen.adventure.engine.rendering.Material;
import swen.adventure.engine.rendering.Texture;
import swen.adventure.engine.rendering.maths.Vector3;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 29/09/15.
 */
public class MTLParser {

    private static final Pattern PatternAmbientColour = Pattern.compile("Ka");
    private static final Pattern PatternDiffuseColour = Pattern.compile("Kd");
    private static final Pattern PatternSpecularColour = Pattern.compile("Ks");
    private static final Pattern PatternSpecularity = Pattern.compile("Ns");
    private static final Pattern PatternTransparency = Pattern.compile("d|Tr");
    private static final Pattern PatternIlluminationMode = Pattern.compile("illum");
    private static final Pattern PatternNewMaterial = Pattern.compile("newmtl");
    private static final Pattern PatternAmbientMap = Pattern.compile("map_Ka");
    private static final Pattern PatternDiffuseMap = Pattern.compile("map_Kd");
    private static final Pattern PatternSpecularColourMap = Pattern.compile("map_Ks");
    private static final Pattern PatternSpecularityMap = Pattern.compile("map_Ns");
    private static final Pattern PatternWhitespaceExceptNewLine = Pattern.compile("[^\\S\\r\\n]");
    private static final Pattern PatternWhitespace = Pattern.compile("\\s+");

    public static Map<String, Material> parse(File file) throws FileNotFoundException{
        InputStream is = new FileInputStream(file);
        return MTLParser.parseMaterialFile(new Scanner(is));
    }

    private static Map<String, Material> parseMaterialFile(Scanner scanner) {
        Map<String, Material> materialMap = new HashMap<>();

        Material material = null;

        while (scanner.hasNext()) {
            if (MTLParser.gobble(scanner, PatternNewMaterial)) {
                material = new Material();
                materialMap.put(scanner.next(), material);

            } else if (material != null) {
                if (MTLParser.gobble(scanner, PatternAmbientColour)) {
                    material.setAmbientColour(MTLParser.parseVector3(scanner));

                } else if (MTLParser.gobble(scanner, PatternDiffuseColour)) {
                    material.setDiffuseColour(MTLParser.parseVector3(scanner));

                } else if (MTLParser.gobble(scanner, PatternSpecularColour)) {
                    material.setSpecularColour(MTLParser.parseVector3(scanner));

                } else if (MTLParser.gobble(scanner, PatternSpecularity)) {
                    if (!scanner.hasNextFloat()) { MTLParser.fail(scanner, "Specularity requires a float value"); }
                    material.setSpecularity(Material.phongSpecularToGaussian(scanner.nextFloat()));

                } else if (MTLParser.gobble(scanner, PatternTransparency)) {
                    if (!scanner.hasNextFloat()) { MTLParser.fail(scanner, "Transparency requires a float value"); }
                    material.setTransparency(scanner.nextFloat());

                } else if (MTLParser.gobble(scanner, PatternIlluminationMode)) {
                    if (!scanner.hasNextInt()) { MTLParser.fail(scanner, "Illumination mode requires an integer value."); }
                    material.setUseAmbient(scanner.nextInt() > 1);

                } else if (MTLParser.gobble(scanner, PatternAmbientMap)) {
                    material.setAmbientMap(MTLParser.parseTexture(scanner));

                } else if (MTLParser.gobble(scanner, PatternDiffuseMap)) {
                    material.setDiffuseMap(MTLParser.parseTexture(scanner));

                } else if (MTLParser.gobble(scanner, PatternSpecularColourMap)) {
                    material.setSpecularColourMap(MTLParser.parseTexture(scanner));

                } else if (MTLParser.gobble(scanner, PatternSpecularityMap)) {
                    material.setSpecularityMap(MTLParser.parseTexture(scanner));

                } else if (scanner.hasNextLine()) {
                    scanner.nextLine();
                }

            } else if (scanner.hasNextLine()) {
                scanner.nextLine();
            }

        }

        return materialMap;
    }

    private static Vector3 parseVector3(Scanner scanner) {
        float[] vector = new float[3];
        for (int i = 0; i < 3; i++) {
            if (!scanner.hasNextFloat()) {
                MTLParser.fail(scanner, "Missing a float value for the vector");
            }
            vector[i] = scanner.nextFloat();
        }
        return new Vector3(vector);
    }

    private static Texture parseTexture(Scanner scanner) {
        scanner.useDelimiter(PatternWhitespaceExceptNewLine);

        List<String> args = new ArrayList<>();
        while (scanner.hasNext() && !scanner.hasNext("\n")) {
            args.add(scanner.next().replaceAll("\n", ""));
        }

        scanner.useDelimiter(PatternWhitespace);

        return Texture.loadTextureWithName(args.get(args.size() - 1));
    }

    private static boolean gobble(Scanner scanner, Pattern p) {
        if (scanner.hasNext(p)) {
            scanner.next();
            return true;
        } else {
            return false;
        }
    }

    /**
     * Gobble a token if it's there, otherwise fail
     *
     * @param p patten to gobble
     * @param errorMessage message to display if it fails
     */
    private static void ensuredGobble(Scanner scanner, Pattern p, String errorMessage) {
        if (!MTLParser.gobble(scanner, p)) {
            MTLParser.fail(scanner, errorMessage);
        }
    }

    /**
     * Report a failure in the parser.
     */
    private static void fail(Scanner scanner, String message) throws RuntimeException {
        String msg = message + "\n   @ ...";
        for (int i = 0; i < 10 && scanner.hasNext(); i++) {
            msg += " " + scanner.next();
        }
        throw new RuntimeException(msg + "...");
    }
}
