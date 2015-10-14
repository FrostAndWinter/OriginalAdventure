/* Contributor List  */ 
 /* Joseph Bennett (bennetjose) (300319773) */ 
 package swen.adventure.game.scenenodes;

import org.junit.Before;
import org.junit.Test;
import swen.adventure.engine.rendering.maths.Quaternion;
import swen.adventure.engine.rendering.maths.Vector3;
import swen.adventure.engine.scenegraph.TransformNode;

import java.util.Optional;

import static junit.framework.Assert.assertNotSame;
import static junit.framework.Assert.assertTrue;
import static junit.framework.TestCase.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by josephbennett on 11/10/15
 */
public class ContainerTests {

    private TransformNode rootTransform;
    private TransformNode containerTransform;
    private TransformNode itemTransform;

    @Before
    public void setup() {
        rootTransform = new TransformNode("rootTransform", Vector3.zero, new Quaternion(), Vector3.one);
        containerTransform = new TransformNode("containerTransform", rootTransform, false, Vector3.zero, new Quaternion(), Vector3.one);
        itemTransform = new TransformNode("itemTransform", rootTransform, true, Vector3.zero, new Quaternion(), Vector3.one);
    }

    @Test
    public void testContainerHasCorrectCapacity() {
        Container container = new Container("test", containerTransform, 10);
        assertEquals(10, container.capacity());
    }

    @Test
    public void testPushingItemIntoContainer() {
        Container container = new Container("test", containerTransform, 10);

        Item testItem = createMockItem();

        // the item's transform should be parented to the root node before it is pushed to the container
        assertSame(rootTransform, itemTransform.parent().get());
        assertNotSame(containerTransform, itemTransform.parent().get());

        container.push(testItem);

        // make sure the item was adopted (?) by the container's parent
        assertSame(containerTransform, itemTransform.parent().get());
        assertNotSame(rootTransform, itemTransform.parent().get());

        assertEquals(1, container.itemCount());
    }

    @Test
    public void testCannotFillContainerPastCapacity() {
        int capacity = 10;
        Container container = new Container("test", containerTransform, capacity);

        // fill the container up to its capacity
        for (int i = 0; i < capacity; i++) {
            boolean success = container.push(createMockItem());
            assertTrue(success);
        }

        // try add one more item than what the container can hold, it should fail.
        boolean success = container.push(createMockItem());
        assertFalse(success);

        // there should be 'capacity' amount of items left in the container
        assertEquals(container.itemCount(), capacity);

        // the container should be full
        assertTrue(container.isFull());
    }


    @Test
    public void testPeekingAtEmptyContainerReturnsEmptyOptional() {
        Container container = new Container("test", containerTransform, 10);
        assertEquals(Optional.empty(), container.peek());
    }

    @Test
    public void testPeekingAtContainerReturnsLastItemPutIn() {
        Container container = new Container("test", containerTransform, 10);

        container.push(createMockItem());
        Item lastPutIn = createMockItem();
        container.push(lastPutIn);

        assertEquals(Optional.of(lastPutIn), container.peek());
    }

    @Test
    public void testPoppingOffAnEmptyContainerReturnsEmptyOptional() {
        Container container = new Container("test", containerTransform, 10);
        assertEquals(Optional.empty(), container.pop());
    }

    @Test
    public void testPoppingOffReturnsAndRemovesLastItemPutIn() {
        Container container = new Container("test", containerTransform, 10);

        // push two items into the container
        Item firstPutIn = createMockItem();
        container.push(firstPutIn);
        Item lastPutIn = createMockItem();
        container.push(lastPutIn);

        // should pop last pushed item and remove it
        Optional<Item> firstPopped = container.pop();
        assertEquals(lastPutIn, firstPopped.get());
        assertEquals(1, container.itemCount());

        // should pop first pushed item and remove it
        Optional<Item> secondPopped = container.pop();
        assertEquals(firstPutIn, secondPopped.get());
        assertEquals(0, container.itemCount());

        // should be nothing left for the next pop
        assertEquals(Optional.empty(), container.pop());
    }

    @Test
    public void canGetItemThatIsAtSpecifiedIndexIfSomethingIsInIt() {
        Container container = new Container("Test", containerTransform, 10);

        Item shouldBeAtIndex0 = createMockItem();
        Item shouldBeAtIndex1 = createMockItem();
        Item shouldBeAtIndex2 = createMockItem();

        container.push(shouldBeAtIndex0);
        container.push(shouldBeAtIndex1);
        container.push(shouldBeAtIndex2);

        assertEquals(shouldBeAtIndex0, container.itemAtIndex(0).get());
        assertEquals(shouldBeAtIndex1, container.itemAtIndex(1).get());
        assertEquals(shouldBeAtIndex2, container.itemAtIndex(2).get());
    }

    @Test(expected = RuntimeException.class)
    public void indexOutsideOfContainerCapacityThrowsRuntimeException() {
        Container container = new Container("Test", containerTransform, 10);
        container.itemAtIndex(11);
    }

    @Test
    public void emptyOptionalIsReturnedIfNothingIsAtValidGivenIndex() {
        Container container = new Container("Test", containerTransform, 10);
        assertEquals(Optional.empty(), container.itemAtIndex(4));
    }

    @Test
    public void allItemsInContainerAreDisabledFromSceneGraphWhenPutInContainerIfShowNotShowingTopItem() {
        Container container = new Container("Test", containerTransform, 10);
        container.setShowTopItem(false);

        Item one = new Item("one", itemTransform, "test item", "test description");
        Item two = new Item("two", itemTransform, "test item", "test description");
        Item three = new Item("three", itemTransform, "test item", "test description");

        container.push(one);
        container.push(two);
        container.push(three);

        assertFalse(one.isEnabled());
        assertFalse(two.isEnabled());
        assertFalse(three.isEnabled());
    }

    @Test
    public void topItemInContainerIsEnabledInSceneGraphWhenPutInContainerIfShowTopItemIsEnabled() {
        Container container = new Container("Test", containerTransform, 10);
        container.setShowTopItem(true);

        Item one = new Item("item", itemTransform, "test item", "test description");
        container.push(one);
        assertTrue(one.isEnabled());

        Item two = new Item("two", itemTransform, "test item", "test description");
        container.push(two);
        assertTrue(two.isEnabled());
        assertFalse(one.isEnabled());

        Item three = new Item("three", itemTransform, "test item", "test description");
        container.push(three);
        assertTrue(three.isEnabled());
        assertFalse(two.isEnabled());
        assertFalse(one.isEnabled());
    }

    private Item createMockItem() {
        Item testItem = mock(Item.class);
        when(testItem.parent()).then(invocation -> Optional.of(itemTransform));
        return testItem;
    }


}