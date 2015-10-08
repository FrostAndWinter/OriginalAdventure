package swen.adventure.engine.rendering;

import org.lwjgl.Sys;
import swen.adventure.engine.Utilities;
import swen.adventure.engine.datastorage.WavefrontParser;
import swen.adventure.engine.rendering.maths.Vector;
import swen.adventure.engine.rendering.maths.Vector3;
import swen.adventure.engine.rendering.maths.Vector4;
import swen.adventure.engine.rendering.maths.BoundingBox;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.stream.Collectors;

import static org.lwjgl.opengl.GL11.GL_TRIANGLES;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 20/09/15.
 */
public class ObjMesh extends GLMesh<Float> {

    private class VertexData {
        final Vector vertexPosition;
        final Optional<Vector3> vertexNormal;
        final Optional<Vector3> textureCoordinate;

        private Optional<Vector> _tangent = Optional.empty();
        private Optional<Vector3> _bitangent = Optional.empty();

        public VertexData(Vector vertexPosition, Optional<Vector3> vertexNormal, Optional<Vector3> textureCoordinate) {
            this.vertexPosition = vertexPosition;
            this.vertexNormal = vertexNormal;
            this.textureCoordinate = textureCoordinate;
        }

        public void orthogonaliseTangent() {
            _tangent.ifPresent(t -> {
                Vector3 tangent = (Vector3) t;
                Vector3 normal = this.vertexNormal.get();
                Vector3 bitangent = _bitangent.get();

                // Gram-Schmidt orthogonalise
                tangent = (tangent.subtract(normal.multiplyScalar(normal.dotProduct(tangent)))).normalise();

                // Calculate handedness
                float w = normal.crossProduct(tangent).dotProduct(bitangent) < 0.f ? -1.f : 1.f;
                _tangent = Optional.of(new Vector4(tangent.x, tangent.y, tangent.z, w));
            });
        }

        /** The tangent is a Vector3 during calculation, but a Vector4 (with w indicating handedness) after orthogonalisation. */
        public Optional<Vector> tangent() {
            return _tangent;
        }

        /**
         * Adds the given bitangent to this vertex's tangent data if it is present, or sets it otherwise.
         * @param bitangent The bitangent to add (average with).
         */
        public void addBitangent(final Vector3 bitangent) {
            _bitangent = Optional.of(
                            _bitangent
                            .orElse(Vector3.zero)
                            .add(bitangent));
        }

        /**
         * Adds the given tangent to this vertex's tangent data if it is present, or sets it otherwise.
         * @param tangent The tangent to add (average with).
         */
        public void addTangent(final Vector3 tangent) {
            _tangent = Optional.of(
                    _tangent
                    .orElse(Vector3.zero)
                    .asVector3()
                            .add(tangent));
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            final VertexData that = (VertexData) o;

            return vertexPosition.equals(that.vertexPosition) && vertexNormal.equals(that.vertexNormal) && textureCoordinate.equals(that.textureCoordinate);

        }

        @Override
        public int hashCode() {
            int result = vertexPosition.hashCode();
            result = 31 * result + vertexNormal.hashCode();
            result = 31 * result + textureCoordinate.hashCode();
            return result;
        }
    }

    private static final int VertexGeometryAttributeIndex = 0;
    private static final int VertexNormalAttributeIndex = 2;
    private static final int TextureCoordinateAttributeIndex = 1;
    private static final int TangentAttributeIndex = 3;

    public static final String VAOPositions = "vaoPositions";
    public static final String VAOPositionsAndNormals = "vaoPositionsAndNormals";
    public static final String VAOPositionsAndTexCoords = "vaoPositionsAndTexCoords";
    public static final String VAOPositionsNormalsTexCoords = "vaoPositionsNormalsTexCoords";
    public static final String VAOPositionsNormalsTexCoordsTangents = "vaoPositionsNormalsTexCoordsTangents";

    private boolean _hasNormals = false;
    private boolean _hasTextureCoordinates = false;
    private boolean _hasFourComponentGeoVectors = false;
    private List<VertexData> _vertices = new ArrayList<>();
    private Map<Material, List<Integer>> _triIndices = new HashMap<>();

    private final BoundingBox _boundingBox;

    public static ObjMesh loadMesh(String fileName) throws FileNotFoundException {
        return ObjMesh.loadMesh(null, fileName);
    }

    public static ObjMesh loadMesh(String directory, String fileName) throws FileNotFoundException {
        File file = new File(Utilities.pathForResource(directory, fileName, null));
        WavefrontParser.Result result = WavefrontParser.parse(file, directory);
        return new ObjMesh(fileName, result);
    }

    public ObjMesh(String fileName, WavefrontParser.Result parsedFile) { //TODO While none of this functionality is repeated, it should probably still be either separated into methods or at least divided with comments.
        Set<VertexData> vertexData = new LinkedHashSet<>(); //We use a LinkedHashSet to try and maintain ordering where possible (keep vertices in the same faces close together in memory).
        Map<WavefrontParser.IndexData, VertexData> objIndicesToVertices = new HashMap<>();

        for (WavefrontParser.PolygonFace polygonFace : parsedFile.polygonFaces) {
            for (WavefrontParser.IndexData indexData : polygonFace.indices) {
                Vector geometricVertex = parsedFile.geometricVertices.get(indexData.vertexIndex - 1);
                Optional<Vector3> textureCoordinate = indexData.textureCoordinateIndex.isPresent() ? Optional.of(parsedFile.textureVertices.get(indexData.textureCoordinateIndex.get() - 1)) : Optional.empty();
                Optional<Vector3> vertexNormal = indexData.normalIndex.isPresent() ? Optional.of(parsedFile.vertexNormals.get(indexData.normalIndex.get() - 1)) : Optional.empty();

                _hasNormals = _hasNormals || vertexNormal.isPresent();
                _hasTextureCoordinates = _hasTextureCoordinates || textureCoordinate.isPresent();
                _hasFourComponentGeoVectors = _hasFourComponentGeoVectors || geometricVertex instanceof Vector4;

                VertexData vertex = new VertexData(geometricVertex, vertexNormal, textureCoordinate);
                vertexData.add(vertex); //Populate the vertex data.

                objIndicesToVertices.put(indexData, vertex);
            }
        }

        _vertices = vertexData.stream().collect(Collectors.toList());

        for (WavefrontParser.PolygonFace polygonFace : parsedFile.polygonFaces) {
            List<Integer> vertexIndices = polygonFace.indices.stream().map((data) -> {
                VertexData vertex = objIndicesToVertices.get(data);
                return _vertices.indexOf(vertex);
            }).collect(Collectors.toList());

            if (vertexIndices.size() == 4) {
                this.processAndAddFacesAtIndices(polygonFace.material, Arrays.asList(vertexIndices.get(0), vertexIndices.get(1), vertexIndices.get(3))); //convert to triangles
                this.processAndAddFacesAtIndices(polygonFace.material, Arrays.asList(vertexIndices.get(1), vertexIndices.get(2), vertexIndices.get(3)));
            } else {
                this.processAndAddFacesAtIndices(polygonFace.material, vertexIndices);
            }
        }

        if (_hasNormals && _hasTextureCoordinates) {
            for (List<Integer> indices : _triIndices.values()) {
                this.computeTangentsAndBiTangents(_vertices, indices);
            }
        }

        List<Float> vertexGeometry = new ArrayList<>();
        List<Float> vertexNormals = new ArrayList<>();
        List<Float> textureCoordinates = new ArrayList<>();
        List<Float> tangents = new ArrayList<>();

        for (VertexData vertex : _vertices) {
            Vector geometricPosition = vertex.vertexPosition;
            if (_hasFourComponentGeoVectors && geometricPosition instanceof Vector3) {
                Vector3 vec3 = (Vector3)geometricPosition;
               geometricPosition = new Vector4(vec3, 1.f);
            }

            this.addVectorToList(geometricPosition, vertexGeometry);

            if (_hasNormals) {
                this.addVectorToList(vertex.vertexNormal.orElse(new Vector3(1.f, 0.f, 0.f)), vertexNormals);
                if (!vertex.vertexNormal.isPresent()) { System.err.println("Warning: mesh with name " + fileName + " has missing normals for vertex at " + geometricPosition); }
            }
            if (_hasTextureCoordinates) {
                this.addVectorToList(vertex.textureCoordinate.orElse(new Vector3(1.f, 0.f, 0.f)), textureCoordinates);
                if (!vertex.textureCoordinate.isPresent()) {
                    System.err.println("Warning: mesh with name " + fileName + " has missing texture coordinates for vertex at " + geometricPosition);
                }
            }

            if (_hasNormals && _hasTextureCoordinates) {
                vertex.orthogonaliseTangent();
                this.addVectorToList(vertex.tangent().get(), tangents);
            }
        }

        List<Attribute> attributes = new ArrayList<>();
        attributes.add(new Attribute(VertexGeometryAttributeIndex, _hasFourComponentGeoVectors ? 4 : 3, AttributeType.Float, false, vertexGeometry));
        if (_hasNormals) {
            attributes.add(new Attribute(VertexNormalAttributeIndex, 3, AttributeType.Float, false, vertexNormals));
        }
        if (_hasTextureCoordinates) {
            attributes.add(new Attribute(TextureCoordinateAttributeIndex, 3, AttributeType.Float, false, textureCoordinates));
        }
        if (_hasNormals && _hasTextureCoordinates) {
            attributes.add(new Attribute(TangentAttributeIndex, 4, AttributeType.Float, false, tangents));
        }

        List<RenderCommand> renderCommands = new ArrayList<>();
        List<IndexData<?>> indexData = new ArrayList<>();

        for (Map.Entry<Material, List<Integer>> entry : _triIndices.entrySet()) {
            renderCommands.add(new RenderCommand(GL_TRIANGLES, -1, entry.getKey()));
            indexData.add(new IndexData<>(entry.getValue(), AttributeType.UInt));
        }

        List<NamedVertexArrayObject> namedVAOs = new ArrayList<>();
        namedVAOs.add(new NamedVertexArrayObject(VAOPositions, Collections.singletonList(VertexGeometryAttributeIndex)));
        if (_hasNormals) {
            namedVAOs.add(new NamedVertexArrayObject(VAOPositionsAndNormals, Arrays.asList(VertexGeometryAttributeIndex, VertexNormalAttributeIndex)));
        }
        if (_hasTextureCoordinates) {
            namedVAOs.add(new NamedVertexArrayObject(VAOPositionsAndTexCoords, Arrays.asList(VertexGeometryAttributeIndex, TextureCoordinateAttributeIndex)));
        }
        if (_hasNormals && _hasTextureCoordinates) {
            namedVAOs.add(new NamedVertexArrayObject(VAOPositionsNormalsTexCoords, Arrays.asList(VertexGeometryAttributeIndex, VertexNormalAttributeIndex, TextureCoordinateAttributeIndex)));
            namedVAOs.add(new NamedVertexArrayObject(VAOPositionsNormalsTexCoordsTangents, Arrays.asList(VertexGeometryAttributeIndex, VertexNormalAttributeIndex, TextureCoordinateAttributeIndex, TangentAttributeIndex)));
        }

        _boundingBox = this.computeBoundingBox();

        super.initialise(attributes, indexData, namedVAOs, renderCommands);
    }

    /**
     * Computes the tangents and bitangents for the given attribute lists, and adds them as attributes to attributeListToAddTo.
     * Assumes that the indices are for triangles and have the correct winding order.
     * Reference: http://www.terathon.com/code/tangent.html
     */
    private void computeTangentsAndBiTangents(List<VertexData> vertices, List<Integer> triangleIndices) {
        for (int i = 0; i < triangleIndices.size(); i+= 3) {
            int index1 = triangleIndices.get(i), index2 = triangleIndices.get(i + 1), index3 = triangleIndices.get(i + 2);

            VertexData v1 = vertices.get(index1), v2 = vertices.get(index2), v3 = vertices.get(index3);

            Vector3 pos1 = v1.vertexPosition.asVector3(), pos2 = v2.vertexPosition.asVector3(), pos3 = v3.vertexPosition.asVector3();
            Vector3 uv1 = v1.textureCoordinate.orElse(Vector3.zero), uv2 = v2.textureCoordinate.orElse(Vector3.zero), uv3 = v3.textureCoordinate.orElse(Vector3.zero);

            float x1 = pos2.x - pos1.x;
            float x2 = pos3.x - pos1.x;
            float y1 = pos2.y - pos1.y;
            float y2 = pos3.y - pos1.y;
            float z1 = pos2.z - pos1.z;
            float z2 = pos3.z - pos2.z;

            float s1 = uv2.x - uv1.x;
            float s2 = uv3.x - uv1.x;
            float t1 = uv2.y - uv1.y;
            float t2 = uv3.y - uv1.y;

            float r = 1.f / (s1 * t2 - s2 * t1);

            if (s1 * t2 - s2 * t1 == 0.f) {
                System.err.println("Warning: vertices share the same position or texture coordinate and therefore have incorrect tangents.");
                System.err.printf("Vertices are %s, %s, %s, and texture coordinates are %s, %s, %s.\n\n", pos1, pos2, pos3, uv1, uv2, uv3);
            }

            Vector3 sDirection = new Vector3(
                    (t2 * x1 - t1 * x2) * r,
                    (t2 * y1 - t1 * y2) * r,
                    (t2 * z1 - t1 * z2) * r
            );
            Vector3 tDirection = new Vector3(
                    (s1 * x2 - s2 * x1) * r,
                    (s1 * y2 - s2 * y1) * r,
                    (s1 * z2 - s2 * z1) * r
            );

            v1.addTangent(sDirection); v2.addTangent(sDirection); v3.addTangent(sDirection);
            v1.addBitangent(tDirection); v2.addBitangent(tDirection); v3.addBitangent(tDirection);
        }
    }

    /** Adds the indices to the list of vertices for a particular material, correcting the winding order if necessary. */
    private void processAndAddFacesAtIndices(Material material, List<Integer> indices) {
        assert indices.size() == 3;

        List<Integer> indicesForMaterial = _triIndices.get(material);
        if (indicesForMaterial == null) {
            indicesForMaterial = new ArrayList<>();
            _triIndices.put(material, indicesForMaterial);
        }

        if (_hasNormals) {
            //Check that the winding order of the vertices is counter-clockwise.
            VertexData vertexA = _vertices.get(indices.get(0));
            VertexData vertexB = _vertices.get(indices.get(1));
            VertexData vertexC = _vertices.get(indices.get(2));

            Vector3 averageNormal = vertexA.vertexNormal.get().add(vertexB.vertexNormal.get()).add(vertexC.vertexNormal.get()).divideScalar(3.f);
            Vector3 aToB = new Vector3(vertexB.vertexPosition.data()).subtract(new Vector3(vertexA.vertexPosition.data()));
            Vector3 bToC = new Vector3(vertexC.vertexPosition.data()).subtract(new Vector3(vertexB.vertexPosition.data()));

            Vector3 crossProduct = aToB.crossProduct(bToC);
            float dot = crossProduct.dotProduct(averageNormal);

            if (dot < 0.f) { //The winding order is wrong.
                indicesForMaterial.add(indices.get(2));
                indicesForMaterial.add(indices.get(1));
                indicesForMaterial.add(indices.get(0));
                return;
            }
        }
        indicesForMaterial.addAll(indices);
    }

    @Override
    public BoundingBox boundingBox() {
        return _boundingBox;
    }

    private BoundingBox computeBoundingBox() {
        float minX = Float.MAX_VALUE, minY = Float.MAX_VALUE, minZ = Float.MAX_VALUE;
        float maxX = -Float.MAX_VALUE, maxY = -Float.MAX_VALUE, maxZ = -Float.MAX_VALUE;

        for (VertexData vertex : _vertices) {
            float[] position = vertex.vertexPosition.data();
            float x = position[0];
            float y = position[1];
            float z = position[2];

            if (x < minX) { minX = x; }
            if (y < minY) { minY = y; }
            if (z < minZ) { minZ = z; }
            if (x > maxX) { maxX = x; }
            if (y > maxY) { maxY = y; }
            if (z > maxZ) { maxZ = z; }
        }
        return new BoundingBox(new Vector3(minX, minY, minZ), new Vector3(maxX, maxY, maxZ));
    }

    private void addVectorToList(Vector vector, List<Float> list) {
        for (float component : vector.data()) {
            list.add(component);
        }
    }
}
