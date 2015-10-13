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
 *
 * A MeshNode represents an object mesh and its associated materials.
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

    public final Event<SceneNode, Player> eventMeshLookedAt = new Event<>("eventMeshLookedAt", this);

    /**
     * Loads a mesh from the specified location and parents it to parent. The id will be set to mesh{fileName}
     * @param directory The directory, relative to the root resources directory, in which to look for the mesh.
     * @param fileName The mesh's file name (e.g. mesh.obj)
     * @param parent The transform node to parent the node to.
     */
    public MeshNode(final String directory, final String fileName, final TransformNode parent) {
        this("mesh" + fileName, directory, fileName, parent);
    }

    /**
     * Loads a mesh from the specified location and parents it to parent.
     * @param id The id the MeshNode is to have in the scene graph.
     * @param directory The directory, relative to the root resources directory, in which to look for the mesh.
     * @param fileName The mesh's file name (e.g. mesh.obj)
     * @param parent The transform node to parent the node to.
     */
    public MeshNode(String id, final String directory, final String fileName, final TransformNode parent) {
        super(id, parent, false);

        _fileName = fileName;
        _directory = directory;

        _mesh = MeshNode.meshWithFileName(directory, fileName);
        _localSpaceBoundingBox = _mesh.boundingBox();

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

    /**
     * A material override ignores the default per-face material specified for an object in its GLMesh, and instead applies this material for the entire object.
     * @return the material override, if it's present.
     */
    public Optional<Material> materialOverride() {
        return _materialOverride;
    }

    /**
     * A material override ignores the default per-face material specified for an object in its GLMesh, and instead applies this material for the entire object.
     * @param materialOverride The material override to set.
     */
    public void setMaterialOverride(final Material materialOverride) {
        _materialOverride = Optional.of(materialOverride);
    }

    /**
     * @return The bounding box of this mesh in local space.
     */
    public BoundingBox boundingBox() {
        return _localSpaceBoundingBox;
    }

    /**
     * Sets whether the mesh is collidable. If collidable is true, it will set this MeshNode's collision node to a new collision node.
     * @param collidable whether the mesh is collidable.
     */
    public void setCollidable(boolean collidable) {
        if (collidable && !_collisionNode.isPresent()) {
            _collisionNode = Optional.of(new CollisionNode(this));
        } else if (!collidable) {
            _collisionNode.ifPresent(collisionNode -> {
                collisionNode.setParent(null);
            });
            _collisionNode = Optional.empty();
        }
    }

    /**
     * Renders the mesh, applying its own materials to the shader. If a material override is set, then that override is used.
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

    /** The texture repeat is how much the textures should be scaled in each axis on this mesh. A scale of 30, 1, 1 will tile the textures 30 times horizontally. */
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

    /**
     * Renders using the currently bound shader and materials, and passing only those vertex attributes specified by the vertex array object.
     * @param vertexArrayObject An enum value specifying which vertex attributes to pass to the shader.
     */
    public void render(GLMesh.VertexArrayObject vertexArrayObject) {
        _mesh.render(vertexArrayObject);
    }

    private static Map<String, GLMesh<Float>> _loadedMeshes = new HashMap<>();

    /**
     * Loads a mesh with a given directory and file name. Will return a cached mesh if it has already been loaded.
     * @param directory The directory the mesh is in.
     * @param fileName the name of the mesh.
     * @return The GLMesh object for the mesh with that name and directory
     */
    public static GLMesh<Float> meshWithFileName(String directory, String fileName) {
        GLMesh<Float> mesh = _loadedMeshes.get(fileName);

        if (mesh == null) {
            String[] fileNameComponents = fileName.split("\\.");
            String extension = fileNameComponents[fileNameComponents.length - 1];

            if (extension.equalsIgnoreCase("obj")) {
                try {
                    mesh = ObjMesh.loadMesh(directory, fileName);
                } catch (FileNotFoundException e) {
                    throw new RuntimeException("Could not load mesh file in directory " + directory + " named " + fileName);
                }
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
