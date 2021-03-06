/* Contributor List  */ 
 /* Thomas Roughton (roughtthom) (300313924) */ 
 /* Joseph Bennett (bennetjose) (300319773) */ 
 /* Daniel Braithwaite (braithdani) (300313770) */ 
 package swen.adventure.game.scenenodes;

import swen.adventure.engine.datastorage.MaterialLibrary;
import swen.adventure.engine.scenegraph.MeshNode;
import swen.adventure.engine.scenegraph.TransformNode;

/**
 * Created by josephbennett on 6/10/15
 */
public class Note extends Item {

    private static final String NoteDescription = "A mysterious note found hidden in a chest.";

    public Note(String id, TransformNode parent) {
        super(id, parent, "note", NoteDescription);

        final String meshID = id + "Mesh";

        MeshNode noteMesh = parent.findNodeWithIdOrCreate(meshID, () -> new MeshNode(meshID, null, "Plane.obj", parent));
        noteMesh.setMaterialOverride(MaterialLibrary.libraryWithName("Note", "MoonlightNote.mtl").materialWithName("Note"));
        this.setMesh(noteMesh);
    }

}