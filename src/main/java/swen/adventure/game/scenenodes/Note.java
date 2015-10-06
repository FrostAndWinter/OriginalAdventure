package swen.adventure.game.scenenodes;

import swen.adventure.engine.datastorage.MaterialLibrary;
import swen.adventure.engine.scenegraph.MeshNode;
import swen.adventure.engine.scenegraph.TransformNode;

/**
 * Created by josephbennett on 6/10/15
 */
public class Note extends Item {

    public Note(String id, TransformNode parent) {
        super(id, parent);

        MeshNode noteMesh = new MeshNode(id + "Mesh", null, "Plane.obj", parent);
        noteMesh.setMaterialOverride(MaterialLibrary.libraryWithName("Note", "MoonlightNote.mtl").materialWithName("Note"));
        this.setMesh(noteMesh);
    }

}