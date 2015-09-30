package swen.adventure.datastorage;

import swen.adventure.Utilities;
import swen.adventure.rendering.Material;
import swen.adventure.rendering.maths.Vector;
import swen.adventure.rendering.maths.Vector3;
import swen.adventure.rendering.maths.Vector4;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by Liam O'Neill, Student ID 300312734, on 19/09/15.
 * Modified by Thomas Roughton, Student ID 300313924.
 */
public class WavefrontParser {

    public class IndexData {
        public final int vertexIndex;
        public final Optional<Integer> normalIndex;
        public final Optional<Integer> textureCoordinateIndex;

        public IndexData(int vertexIndex, Optional<Integer> textureCoordinateIndex, Optional<Integer> normalIndex) {
            this.vertexIndex = vertexIndex;
            this.textureCoordinateIndex = textureCoordinateIndex;
            this.normalIndex = normalIndex;
        }
    }

    private static final Pattern COMMENT_PAT = Pattern.compile("#.*");
    private static final Pattern GEOMETRIC_VERTEX_PAT = Pattern.compile("v");
    private static final Pattern TEXTURE_VERTEX_PAT = Pattern.compile("vt");
    private static final Pattern VERTEX_NORMAL_PAT = Pattern.compile("vn");
    private static final Pattern PARAMETER_SPACE_VERTEX_PAT = Pattern.compile("vp");
    private static final Pattern MATERIAL_LIBRARY_PAT = Pattern.compile("mtllib");
    private static final Pattern MATERIAL_PAT = Pattern.compile("usemtl");
    private static final Pattern POLYGONAL_FACE_PAT = Pattern.compile("f");
    private static final Pattern POLYGONAL_FACE_VERTEX_PATTERN = Pattern.compile("\\d+(/(\\d+)?)*");
   // private static final Pattern POLYGONAL_FACE_PAT = Pattern.compile("f\\s+(\\d+(/(\\d+)?(/\\d+)?)?\\s*){3,}");

    private static final Pattern FORWARD_SLASH_PATTERN = Pattern.compile("/");

    private final Scanner scanner;

    private final List<Vector> geometricVertices = new ArrayList<>();
    private final List<Vector3> textureVertices = new ArrayList<>();
    private final List<Vector3> vertexNormals = new ArrayList<>();
    private final List<PolygonFace> polygonFaces = new ArrayList<>();
    private final Map<String, Material> materials = new HashMap<>();

    private Material _currentMaterial = Material.DefaultMaterial;

    public static Result parse(File file) throws FileNotFoundException {
        InputStream is = new FileInputStream(file);
        return parse(is);
    }

    public static Result parse(String obj) {
        InputStream is = new ByteArrayInputStream(obj.getBytes(StandardCharsets.UTF_8));
        return parse(is);
    }

    private static Result parse(InputStream is) {
        WavefrontParser parser = new WavefrontParser(is);
        return new Result(parser.geometricVertices, parser.textureVertices, parser.vertexNormals, parser.polygonFaces);
    }

    private WavefrontParser(InputStream is) {
        this.scanner = new Scanner(is);
        parse();
    }

    private void parse(){
        while (hasNext()){

            if (hasNext(GEOMETRIC_VERTEX_PAT)) {
                parseGeometricVertex();

            } else if (hasNext(TEXTURE_VERTEX_PAT)) {
                parseTextureVertex();

            } else if (hasNext(VERTEX_NORMAL_PAT)) {
                parseVertexNormal();

            } else if (hasNext(PARAMETER_SPACE_VERTEX_PAT)) {
                parseParameterSpaceVertex();

            } else if (hasNext(POLYGONAL_FACE_PAT)) {
                parsePolygonFace();

            } else if (hasNext(MATERIAL_LIBRARY_PAT)) {
                parseMaterialLibrary();

            } else if (hasNext(MATERIAL_PAT)) {
                parseMaterial();

            } else {
                scanner.nextLine();
            }
        }
    }

    private void parseGeometricVertex() {
        ensuredGobble(GEOMETRIC_VERTEX_PAT, "Geometric vertices should start with a 'v'");
        float x = scanner.nextFloat();
        float y = scanner.nextFloat();
        float z = scanner.nextFloat();
        float w = scanner.hasNextFloat() ? scanner.nextFloat() : 1f;
        geometricVertices.add(w == 1.f ? new Vector3(x, y, z) : new Vector4(x, y, z, w));
    }

    private void parseTextureVertex() {
        ensuredGobble(TEXTURE_VERTEX_PAT, "Texture vertices should start with a 'vt'");
        float u = scanner.nextFloat();
        float v = scanner.nextFloat();
        float w = scanner.hasNextFloat() ? scanner.nextFloat() : 0f;
        textureVertices.add(new Vector3(u, v, w));
    }

    private void parseVertexNormal() {
        ensuredGobble(VERTEX_NORMAL_PAT, "Vertex should start with a 'vn'");
        float x = scanner.nextFloat();
        float y = scanner.nextFloat();
        float z = scanner.nextFloat();
        vertexNormals.add(new Vector3(x, y, z));
    }

    private void parseParameterSpaceVertex() {
        throw new UnsupportedOperationException("Parameter space vertices haven't been implemented yet");
    }

    private void parseMaterialLibrary() {
        ensuredGobble(MATERIAL_LIBRARY_PAT, "A material library starts with a mtllib command.");
        String libraryName = scanner.next();
        String fileName = Utilities.pathForResource(libraryName, null);
        Map<String, Material> libraryMaterials = null;
        try {
            libraryMaterials = MTLParser.parse(new File(fileName));
            this.materials.putAll(libraryMaterials);
        } catch (FileNotFoundException e) {
            fail("Invalid material library name caused a FileNotFoundException: " + e);
        }
    }

    private void parseMaterial() {
        ensuredGobble(MATERIAL_PAT, "A material usage definition starts with 'usemtl'.");
        Material material = this.materials.get(scanner.next());
        if (material == null) {
            fail("Material was not defined before it was used.");
        }
        _currentMaterial = material;
    }

    private void parsePolygonFace() {
        ensuredGobble(POLYGONAL_FACE_PAT, "Polygons faces should start with 'f'");
        List<IndexData> vertexIndices = new ArrayList<>();

        while (scanner.hasNext(POLYGONAL_FACE_VERTEX_PATTERN)) {
            String vertexToken = scanner.next();
            vertexIndices.add(parsePolygonVertex(vertexToken)); //we've parsed the string in a separate scanner, so move on.
        }
        polygonFaces.add(new PolygonFace(vertexIndices, _currentMaterial));
    }

    private IndexData parsePolygonVertex(String vertex) {

        String[] components = vertex.split("/", -1); //the -1 means to include empty strings.

        Optional<Integer> normalIndex = Optional.empty();
        Optional<Integer> textureCoordinateIndex = Optional.empty();

        int vertexIndex = Integer.parseInt(components[0]);

        if (components.length > 1 && components[1].length() > 0) {
            textureCoordinateIndex = Optional.of(Integer.parseInt(components[1]));
        }
        if (components.length > 2 && components[2].length() > 0) {
            normalIndex = Optional.of(Integer.parseInt(components[2]));
        }

        return new IndexData(vertexIndex, textureCoordinateIndex, normalIndex);

    }

    private boolean hasNext(){
        return scanner.hasNext();
    }

    private boolean hasNext(Pattern p){
        return scanner.hasNext(p);
    }

    private boolean gobble(Pattern p) {
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
    private void ensuredGobble(Pattern p, String errorMessage) {
        if (!gobble(p)) {
            fail(errorMessage);
        }
    }

    /**
     * Report a failure in the parser.
     */
    private void fail(String message) throws RuntimeException {
        String msg = message + "\n   @ ...";
        for (int i = 0; i < 10 && scanner.hasNext(); i++) {
            msg += " " + scanner.next();
        }
        throw new RuntimeException(msg + "...");
    }

    public static class PolygonFace {
        public final List<IndexData> indices;
        public final Material material;

        public PolygonFace(final List<IndexData> indices, final Material material) {
            this.indices = indices;
            this.material = material;
        }
    }

    // this class is temporary
    public static class Result {
        public final List<Vector> geometricVertices;
        public final List<Vector3> textureVertices;
        public final List<Vector3> vertexNormals;
        public final List<PolygonFace> polygonFaces;

        public Result(List<Vector> geometricVertices, List<Vector3> textureVertices, List<Vector3> vertexNormals, List<PolygonFace> polygonFaces) {
            this.geometricVertices = geometricVertices;
            this.textureVertices = textureVertices;
            this.vertexNormals = vertexNormals;
            this.polygonFaces = polygonFaces;
        }

        @Override
        public String toString() {
            return "Result{" +
                    "geometricVertices=" + geometricVertices +
                    ", textureVertices=" + textureVertices +
                    ", vertexNormals=" + vertexNormals +
                    ", polygonFaces=" + polygonFaces +
                    '}';
        }
    }
}
