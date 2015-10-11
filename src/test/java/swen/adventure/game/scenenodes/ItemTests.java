package swen.adventure.game.scenenodes;

import org.junit.Before;
import org.junit.Test;
import swen.adventure.engine.rendering.maths.Quaternion;
import swen.adventure.engine.rendering.maths.Vector3;
import swen.adventure.engine.scenegraph.MeshNode;
import swen.adventure.engine.scenegraph.TransformNode;
import swen.adventure.game.Interaction;

import java.util.List;
import java.util.Optional;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by josephbennett on 11/10/15
 */
public class ItemTests {

    private TransformNode rootTransform;
    private TransformNode itemTransform;
    private TransformNode containerTransform;

    private Container container;

    @Before
    public void setup() {
        rootTransform = new TransformNode("rootTransform", Vector3.zero, new Quaternion(), Vector3.one);
        itemTransform = new TransformNode("itemTransform", rootTransform, true, Vector3.zero, new Quaternion(), Vector3.one);

        containerTransform = new TransformNode("containerTransform", rootTransform, true, Vector3.zero, new Quaternion(), Vector3.one);
        container = new Container("container", containerTransform);
    }

    @Test
    public void itemIsNotInContainerWhenItIsCreated() {
        Item item  = new Item("item", itemTransform, "TestItem", "This is a test item. Yay.");
        assertEquals(Optional.empty(), item.containingContainer());
    }

    @Test
    public void itemIsMovedToAContainerCorrectly() {
        Item item  = new Item("item", itemTransform, "TestItem", "This is a test item. Yay.");
        item.moveToContainer(container);

        assertEquals(container, item.containingContainer().get());
        assertEquals(container.peek().get(), item);
    }

    @Test
    public void itemIsMovedOutOfPreviousContainerIfMovedToNewContainer() {
        Item item  = new Item("item", itemTransform, "TestItem", "This is a test item. Yay.");

        Container previousContainer = new Container("previousContainer", rootTransform);
        item.moveToContainer(previousContainer);

        Container newContainer = new Container("newContainer", rootTransform);
        item.moveToContainer(newContainer);

        // should not be in the previous container
        assertNotEquals(previousContainer, item.containingContainer().get());

        // it should be in the new container
        assertEquals(newContainer, item.containingContainer().get());
    }

    @Test(expected = NullPointerException.class)
    public void movingToNullContainerThrowsNullPointerException() {
        Item item  = new Item("item", itemTransform, "TestItem", "This is a test item. Yay.");
        item.moveToContainer(null);
    }

}
