package swen.adventure.engine;

import swen.adventure.engine.rendering.maths.BoundingBox;
import swen.adventure.engine.rendering.maths.Matrix4;
import swen.adventure.engine.scenegraph.MeshNode;
import swen.adventure.engine.scenegraph.SceneNode;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 7/10/15.
 *
 * DepthSorter sorts bounding boxes in a particular coordinate space by their z values.
 */
public class DepthSorter {

    static class SortableMesh implements Comparable<SortableMesh> {
        public final BoundingBox boundingBox;
        public final MeshNode meshNode;

        public SortableMesh(MeshNode meshNode, Matrix4 worldToCameraMatrix) {
            this.meshNode = meshNode;
            this.boundingBox = meshNode.boundingBox()
                    .axisAlignedBoundingBoxInSpace(
                            worldToCameraMatrix.multiply(meshNode.nodeToWorldSpaceTransform())
                    );
        }

        /**
         * Compares this sortable mesh with the other sortable mesh based upon depth.
         * @param other The object to compare with
         * @return -1 if this object's maximum z is closer to the camera than other's, 0 if they are equal, or 1 if it is further from the camera.
         */
        public int compareTo(SortableMesh other) {
            return (int)(other.boundingBox.maxPoint.z - this.boundingBox.maxPoint.z);
        }
    }

    /**
     * Takes a scene graph and a transformation to convert from world space to camera space and produces a list of mesh nodes sorted such that
     * @param sceneGraph The scene graph in which to find the mesh nodes.
     * @param worldToCameraMatrix A matrix to convert from world space to camera space.
     * @return A list of the nodes in the scene graph that are in front of the camera, sorted such that ones closer to the camera are earlier in the list.
     */
    public static List<MeshNode> sortedMeshNodesByZ(SceneNode sceneGraph, Matrix4 worldToCameraMatrix) {
        return sceneGraph.allNodesOfType(MeshNode.class)
                .parallelStream()
                .filter(SceneNode::isEnabled)
                .map(meshNode -> new SortableMesh(meshNode, worldToCameraMatrix))
                .filter(sortableMesh -> sortableMesh.boundingBox.minPoint.z < 0)
                .sorted()
                .map(sortableMesh -> sortableMesh.meshNode)
                .collect(Collectors.toList());
    }
}
