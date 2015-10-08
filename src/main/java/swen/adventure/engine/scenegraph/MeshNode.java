package swen.adventure.engine.scenegraph;

import swen.adventure.engine.Event;
import swen.adventure.engine.datastorage.BundleObject;
import swen.adventure.engine.rendering.GLMesh;
import swen.adventure.engine.rendering.Material;
import swen.adventure.engine.rendering.ObjMesh;
import swen.adventure.engine.rendering.maths.Vector3;
import swen.adventure.engine.rendering.shaders.MaterialShader;
import swen.adventure.engine.rendering.maths.BoundingBox;
import swen.adventure.game.scenenodes.Player;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 25/09/15.
 */
public final class MeshNode extends SceneNode {

    // needed for serializing this node into xml
    private final String _directory;
    private final String _fileName;

    private GLMesh<Float> _mesh;
    private Optional<Material> _materialOverride = Optional.empty();

    private BoundingBox _localSpaceBoundingBox;
    private Vector3 _textureRepeat = Vector3.one;
    private Optional<CollisionNode> _collisionNode = Optional.empty();

    public final Event<SceneNode, Player> eventMeshPressed = new Event<>("eventMeshPressed", this);
    public Event<SceneNode, Player> eventMeshReleased = new Event<>("eventMeshReleased", this);

    public MeshNode(final String directory, final String fileName, final TransformNode parent) {
        this("mesh" + fileName, directory, fileName, parent);
    }

    public MeshNode(String id, final String directory, final String fileName, final TransformNode parent) {
        super(id, parent, false);

        _fileName = fileName;
        _directory = directory;

        try {
            _mesh = MeshNode.loadMeshWithFileName(directory, fileName);
            _localSpaceBoundingBox = _mesh.boundingBox();
        } catch (FileNotFoundException e) {
            System.err.println("Could not load mesh file " + fileName + ": " + e);
        }
    }

    @Override
    public BundleObject toBundle() {
        String fileName = id.substring("mesh".length()); // the id must equal "mesh" + fileName
        return super.toBundle()
                .put("fileName", fileName);
    }

    private static MeshNode createSceneNodeFromBundle(BundleObject bundle, Function<String, TransformNode> findParentFunction) {
        String id = bundle.getString("id");
        String parentId = bundle.getString("parentId");
        TransformNode parent = findParentFunction.apply(parentId);
        String fileName = bundle.getString("fileName");
        return new MeshNode(id, fileName, parent);
    }

    public Optional<Material> materialOverride() {
        return _materialOverride;
    }

    public void setMaterialOverride(final Material materialOverride) {
        _materialOverride = Optional.of(materialOverride);
    }

    public BoundingBox boundingBox() {
        return _localSpaceBoundingBox;
    }

    public void setCollidable(boolean collidable) {
        if (collidable && !_collisionNode.isPresent()) {
            _collisionNode = Optional.of(new CollisionNode(this));
        } else {
            _collisionNode.ifPresent(collisionNode -> {
                collisionNode.setParent(null);
            });
            _collisionNode = Optional.empty();
        }
    }

    /**
     * Renders the mesh, applying its own material to the shader. If a material override is set, then that override is used.
     * @param shader The Material Shader on which to set the materials.
     */
    public void render(MaterialShader shader) {
        shader.setTextureRepeat(_textureRepeat);
        _materialOverride.ifPresent(material -> {
            shader.setMaterial(material.toBuffer());
            material.bindTextures();
            Material.bindSamplers();

            _mesh.render();

            Material.unbindSamplers();
            Material.unbindTextures();
        });
        if (!_materialOverride.isPresent()) {
            _mesh.render(shader);
        }
    }


    public void setTextureRepeat(Vector3 textureRepeat) {
        _textureRepeat = textureRepeat;
    }

    /** The texture repeat is how much the textures should be scaled in each axis on this mesh. A scale of 30, 1, 1 will tile the textures 30 times horizontally. */
    public Vector3 textureRepeat() {
        return _textureRepeat;
    }

    /**
     * Renders the mesh using the currently bound shader and materials.
     */
    public void render() {
        _mesh.render();
    }

    public void render(String vertexArrayObjectName) {
        _mesh.render(vertexArrayObjectName);
    }

    private static Map<String, GLMesh<Float>> _loadedMeshes = new HashMap<>();

    private static GLMesh<Float> loadMeshWithFileName(String directory, String fileName) throws FileNotFoundException {
        GLMesh<Float> mesh = _loadedMeshes.get(fileName);

        if (mesh == null) {
            String[] fileNameComponents = fileName.split("\\.");
            String extension = fileNameComponents[fileNameComponents.length - 1];

            if (extension.equalsIgnoreCase("obj")) {
                mesh = ObjMesh.loadMesh(directory, fileName);
            } else {
                throw new RuntimeException("The file format " + extension + " is not supported.");
            }
            _loadedMeshes.put(fileName, mesh);
        }

        return mesh;
    }

    public String getDirectory() {
        return _directory;
    }

    public String getFileName() {
        return _fileName;
    }

    public Vector3 getTextureRepeat() {
        return _textureRepeat;
    }

    public boolean isCollidable() {
        return _collisionNode.isPresent();
    }
}
