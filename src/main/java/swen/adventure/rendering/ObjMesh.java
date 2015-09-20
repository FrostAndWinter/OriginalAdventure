package swen.adventure.rendering;

import com.jogamp.opengl.GL3;
import javafx.util.Pair;
import swen.adventure.Utilities;
import swen.adventure.datastorage.WavefrontParser;
import swen.adventure.rendering.maths.Vector;
import swen.adventure.rendering.maths.Vector3;
import swen.adventure.rendering.maths.Vector4;
import swen.adventure.scenegraph.SceneNode;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.stream.Collectors;

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

            if (!vertexPosition.equals(that.vertexPosition)) return false;
            if (!vertexNormal.equals(that.vertexNormal)) return false;
            return textureCoordinate.equals(that.textureCoordinate);

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
    private static final int VertexNormalAttributeIndex = 1;
    private static final int TextureCoordinateAttributeIndex = 2;

    private static final String VAOPositions = "vaoPositions";
    private static final String VAOPositionsAndNormals = "vaoPositionsAndNormals";
    private static final String VAOPositionsAndTexCoords = "vaoPositionsAndTexCoords";
    private static final String VAOPositionsNormalsTexCoords = "vaoPositionsNormalsTexCoords";

    private boolean _hasNormals = false;
    private boolean _hasTextureCoordinates = false;
    private boolean _hasFourComponentGeoVectors = false;
    private List<VertexData> _vertices = new ArrayList<>();
    private List<Short> _triIndices = new ArrayList<>();

    public static ObjMesh loadMesh(String id, SceneNode parent, GL3 gl, String fileName) throws FileNotFoundException {
        File file = new File(Utilities.pathForResource(fileName, "obj"));
        WavefrontParser.Result result = WavefrontParser.parse(file);
        return new ObjMesh(id, parent, gl, result);
    }

    public ObjMesh(String id, SceneNode parent, GL3 gl, WavefrontParser.Result parsedFile) {
        super(id, parent);

        Set<VertexData> vertexData = new LinkedHashSet<>(); //We use a LinkedHashSet to try and maintain ordering where possible (keep vertices in the same faces close together in memory).
        Map<WavefrontParser.IndexData, VertexData> objIndicesToVertices = new HashMap<>();

        for (List<WavefrontParser.IndexData> indices : parsedFile.polygonFaces) {
            for (WavefrontParser.IndexData indexData : indices) {
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

        for (List<WavefrontParser.IndexData> indices : parsedFile.polygonFaces) {
            List<Short> vertexIndices = indices.stream().map((data) -> {
                VertexData vertex = objIndicesToVertices.get(data);
                return (short)_vertices.indexOf(vertex);
            }).collect(Collectors.toList());

            if (vertexIndices.size() == 4) {
                _triIndices.addAll(Arrays.asList(vertexIndices.get(0), vertexIndices.get(1), vertexIndices.get(3))); //convert to triangles
                _triIndices.addAll(Arrays.asList(vertexIndices.get(1), vertexIndices.get(2), vertexIndices.get(3)));
            } else {
                _triIndices.addAll(vertexIndices);
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
                if (!vertex.vertexNormal.isPresent()) { System.err.println("Warning: mesh with id " + id + " has missing normals for vertex at " + geometricPosition); }
            }
            if (_hasTextureCoordinates) {
                this.addVectorToList(vertex.textureCoordinate.isPresent() ? vertex.textureCoordinate.get() : new Vector3(1.f, 0.f, 0.f), textureCoordinates);
                if (!vertex.textureCoordinate.isPresent()) { System.err.println("Warning: mesh with id " + id + " has missing texture coordinates for vertex at " + geometricPosition); }
            }
        }

        List<Attribute> attributes = new ArrayList<Attribute>();
        attributes.add(new Attribute(VertexGeometryAttributeIndex, _hasFourComponentGeoVectors ? 4 : 3, AttributeType.Float, false, vertexGeometry));
        if (_hasNormals) {
            attributes.add(new Attribute(VertexNormalAttributeIndex, 3, AttributeType.Float, false, vertexNormals));
        }
        if (_hasTextureCoordinates) {
            attributes.add(new Attribute(TextureCoordinateAttributeIndex, 3, AttributeType.Float, false, textureCoordinates));
        }

        List<RenderCommand> renderCommands = new ArrayList<>();
        List<IndexData<?>> indexData = new ArrayList<>();

        if (!_triIndices.isEmpty()) {
            renderCommands.add(new RenderCommand(GL3.GL_TRIANGLES, -1));
            indexData.add(new IndexData(_triIndices, AttributeType.UShort));
        }

        List<Pair<String, List<Integer>>> namedVAOs = new ArrayList<>();
        namedVAOs.add(new Pair<>(VAOPositions, Arrays.asList(VertexGeometryAttributeIndex)));
        if (_hasNormals) {
            namedVAOs.add(new Pair<>(VAOPositionsAndNormals, Arrays.asList(VertexGeometryAttributeIndex, VertexNormalAttributeIndex)));
        }
        if (_hasTextureCoordinates) {
            namedVAOs.add(new Pair<>(VAOPositionsAndTexCoords, Arrays.asList(VertexGeometryAttributeIndex, TextureCoordinateAttributeIndex)));
        }
        if (_hasNormals && _hasTextureCoordinates) {
            namedVAOs.add(new Pair<>(VAOPositionsNormalsTexCoords, Arrays.asList(VertexGeometryAttributeIndex, VertexNormalAttributeIndex, TextureCoordinateAttributeIndex)));
        }

        super.initialise(gl, attributes, indexData, namedVAOs, renderCommands);

    }

    private void addVectorToList(Vector vector, List<Float> list) {
        for (float component : vector.data()) {
            list.add(component);
        }
    }
}
