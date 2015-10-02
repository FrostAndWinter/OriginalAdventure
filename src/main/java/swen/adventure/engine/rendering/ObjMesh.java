package swen.adventure.engine.rendering;

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

    class VertexData {
        final Vector vertexPosition;
        final Optional<Vector3> vertexNormal;
        final Optional<Vector3> textureCoordinate;

        public VertexData(Vector vertexPosition, Optional<Vector3> vertexNormal, Optional<Vector3> textureCoordinate) {
            this.vertexPosition = vertexPosition;
            this.vertexNormal = vertexNormal;
            this.textureCoordinate = textureCoordinate;
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

    private static final String VAOPositions = "vaoPositions";
    private static final String VAOPositionsAndNormals = "vaoPositionsAndNormals";
    private static final String VAOPositionsAndTexCoords = "vaoPositionsAndTexCoords";
    private static final String VAOPositionsNormalsTexCoords = "vaoPositionsNormalsTexCoords";

    private boolean _hasNormals = false;
    private boolean _hasTextureCoordinates = false;
    private boolean _hasFourComponentGeoVectors = false;
    private List<VertexData> _vertices = new ArrayList<>();
    private Map<Material, List<Integer>> _triIndices = new HashMap<>();

    private final BoundingBox _boundingBox;

    public static ObjMesh loadMesh(String fileName) throws FileNotFoundException {
        File file = new File(Utilities.pathForResource(fileName, null));
        WavefrontParser.Result result = WavefrontParser.parse(file);
        return new ObjMesh(fileName, result);
    }

    public ObjMesh(String fileName, WavefrontParser.Result parsedFile) {
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
                this.addIndices(polygonFace.material, Arrays.asList(vertexIndices.get(0), vertexIndices.get(1), vertexIndices.get(3))); //convert to triangles
                this.addIndices(polygonFace.material, Arrays.asList(vertexIndices.get(1), vertexIndices.get(2), vertexIndices.get(3)));
            } else {
                this.addIndices(polygonFace.material, vertexIndices);
            }
        }

        List<Float> vertexGeometry = new ArrayList<>();
        List<Float> vertexNormals = new ArrayList<>();
        List<Float> textureCoordinates = new ArrayList<>();

        for (VertexData vertex : _vertices) {
            Vector geometricPosition = vertex.vertexPosition;
            if (_hasFourComponentGeoVectors && geometricPosition instanceof Vector3) {
                Vector3 vec3 = (Vector3)geometricPosition;
               geometricPosition = new Vector4(vec3, 1.f);
            }

            this.addVectorToList(geometricPosition, vertexGeometry);

            if (_hasNormals) {
                this.addVectorToList(vertex.vertexNormal.isPresent() ? vertex.vertexNormal.get() : new Vector3(1.f, 0.f, 0.f), vertexNormals);
                if (!vertex.vertexNormal.isPresent()) { System.err.println("Warning: mesh with name " + fileName + " has missing normals for vertex at " + geometricPosition); }
            }
            if (_hasTextureCoordinates) {
                this.addVectorToList(vertex.textureCoordinate.isPresent() ? vertex.textureCoordinate.get() : new Vector3(1.f, 0.f, 0.f), textureCoordinates);
                if (!vertex.textureCoordinate.isPresent()) { System.err.println("Warning: mesh with name " + fileName + " has missing texture coordinates for vertex at " + geometricPosition); }
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
        }

        _boundingBox = this.computeBoundingBox();

        super.initialise(attributes, indexData, namedVAOs, renderCommands);
    }

    /** Adds the indices to the list of vertices for a particular material, correcting the winding order if necessary. */
    private void addIndices(Material material, List<Integer> indices) {
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
