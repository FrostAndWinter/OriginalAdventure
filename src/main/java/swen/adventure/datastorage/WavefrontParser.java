package swen.adventure.datastorage;

import swen.adventure.rendering.maths.Vector3;
import swen.adventure.rendering.maths.Vector4;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * Created by Liam O'Neill, Student ID 300312734, on 19/09/15.
 */
public class WavefrontParser {

    private static final Pattern COMMENT_PAT = Pattern.compile("#.*");
    private static final Pattern GEOMETRIC_VERTEX_PAT = Pattern.compile("v");
    private static final Pattern TEXTURE_VERTEX_PAT = Pattern.compile("vt");
    private static final Pattern VERTEX_NORMAL_PAT = Pattern.compile("vn");
    private static final Pattern PARAMETER_SPACE_VERTEX_PAT = Pattern.compile("vp");
    private static final Pattern POLYGONAL_FACE_PAT = Pattern.compile("f");

    // f v1/vt1 v2/vt2 v3/vt3
    private static final Pattern FACE_VERTEX_TEXTURE_PAT = Pattern.compile("\\d+/\\d+");

    // f v1/vt1/vn1 v2/vt2/vn2 v3/vt3/vn3
    private static final Pattern FACE_VERTEX_TEXTURE_NORMAL_PAT = Pattern.compile("\\d+/\\d+/\\d+");

    // f v1//vn1 v2//vn2 v3//vn3
    private static final Pattern FACE_VERTEX_NORMAL_PAT = Pattern.compile("\\d+//\\d+");

    private final Scanner scan;

    private final List<Vector4> geometricVertices = new ArrayList<>();
    private final List<Vector3> textureVertices = new ArrayList<>();
    private final List<Vector3> vertexNormals = new ArrayList<>();
    private final List<List<Vector4>> polygonFaces = new ArrayList<>();

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
        this.scan = new Scanner(is);
        parse();
    }

    private void parse(){
        while(hasNext()){

            if(hasNext(COMMENT_PAT))
                scan.nextLine();

            else if(hasNext(GEOMETRIC_VERTEX_PAT))
                parseGeometricVertex();

            else if(hasNext(TEXTURE_VERTEX_PAT))
                parseTextureVertex();

            else if(hasNext(VERTEX_NORMAL_PAT))
                parseVertexNormal();

            else if(hasNext(PARAMETER_SPACE_VERTEX_PAT))
                parseParameterSpaceVertex();

            else if(hasNext(POLYGONAL_FACE_PAT))
                parsePolygonFace();
        }
    }

    private void parseGeometricVertex() {
        ensuredGobble(GEOMETRIC_VERTEX_PAT, "Geometric vertices should start with a 'v'");
        float x = scan.nextFloat();
        float y = scan.nextFloat();
        float z = scan.nextFloat();
        float w = scan.hasNextFloat() ? scan.nextFloat() : 1f;
        geometricVertices.add(new Vector4(x, y, z, w));
    }

    private void parseTextureVertex() {
        ensuredGobble(TEXTURE_VERTEX_PAT, "Texture vertices should start with a 'vt'");
        float u = scan.nextFloat();
        float v = scan.nextFloat();
        float w = scan.hasNextFloat() ? scan.nextFloat() : 0f;
        textureVertices.add(new Vector3(u, v, w));
    }

    private void parseVertexNormal() {
        ensuredGobble(VERTEX_NORMAL_PAT, "Vertex should start with a 'vn'");
        float x = scan.nextFloat();
        float y = scan.nextFloat();
        float z = scan.nextFloat();
        vertexNormals.add(new Vector3(x, y, z));
    }

    private void parseParameterSpaceVertex() {
        throw new UnsupportedOperationException("Parameter space vertices haven't been implemented yet");
    }

    private void parsePolygonFace() {
        ensuredGobble(POLYGONAL_FACE_PAT, "Polygons faces should start with 'f'");
        List<Vector4> face = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            face.add(parsePolygonVertex());
        }
        polygonFaces.add(face);
    }

    private Vector4 parsePolygonVertex() {
        // need to look into regex capturing groups.
        if(scan.hasNextInt()){
            int vetexIndex = scan.nextInt();
        } else if (hasNext(FACE_VERTEX_TEXTURE_PAT)) {

        } else if (hasNext(FACE_VERTEX_TEXTURE_NORMAL_PAT)){

        } else if (hasNext(FACE_VERTEX_NORMAL_PAT)){

        } else
            fail("Can't parse polygon vertex");

        throw new UnsupportedOperationException("parsePolygonVertex isn't implemented yet");
    }

    private boolean hasNext(){
        return scan.hasNext();
    }

    private boolean hasNext(Pattern p){
        return scan.hasNext(p);
    }

    private boolean gobble(Pattern p) {
        if (scan.hasNext(p)) {
            scan.next();
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
        for (int i = 0; i < 10 && scan.hasNext(); i++) {
            msg += " " + scan.next();
        }
        throw new RuntimeException(msg + "...");
    }

    // this class is temporary
    public static class Result {
        final List<Vector4> geometricVertices;
        final List<Vector3> textureVertices;
        final List<Vector3> vertexNormals;
        final List<List<Vector4>> polygonFaces;

        public Result(List<Vector4> geometricVertices, List<Vector3> textureVertices, List<Vector3> vertexNormals, List<List<Vector4>> polygonFaces) {
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
