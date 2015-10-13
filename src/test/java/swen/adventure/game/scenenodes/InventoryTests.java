package swen.adventure.game.scenenodes;

import org.junit.Before;
import org.junit.Test;
import swen.adventure.engine.rendering.maths.Quaternion;
import swen.adventure.engine.rendering.maths.Vector3;
import swen.adventure.engine.scenegraph.TransformNode;

import java.util.Optional;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by josephbennett on 11/10/15
 */
public class InventoryTests {

    private TransformNode playerTransform;
    private Player player;

    @Before
    public void setup() {
        TransformNode rootTransform = new TransformNode("rootTransform", Vector3.zero, new Quaternion(), Vector3.one);
        playerTransform = new TransformNode("playerTransform", rootTransform, true, Vector3.zero, new Quaternion(), Vector3.one);

        player = mock(Player.class);
        when(player.parent()).then(invocation -> Optional.of(playerTransform));
    }

    @Test
    public void testCanSelectSlot() {
        Inventory inventory = new Inventory(player);

        Item slotZero = createMockItem();
        Item slotTwo = createMockItem();

        inventory.push(slotZero);
        inventory.push(slotTwo);

        inventory.selectSlot(0);
        assertEquals(slotZero, inventory.selectedItem().get());

        inventory.selectSlot(1);
        assertEquals(slotTwo, inventory.selectedItem().get());
    }

    @Test(expected = IllegalArgumentException.class)
    public void cannotSelectSlotGreaterThanCapacity() {
        Inventory inventory = new Inventory(player);
        inventory.selectSlot(inventory.capacity() + 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void cannotSelectNegativeSlot() {
        Inventory inventory = new Inventory(player);
        inventory.selectSlot(-1);
    }

    @Test
    public void selectingSlotUpdatesSelectedSlot() {
        Inventory inventory = new Inventory(player);
        assertEquals(0, inventory.selectedSlot());

        inventory.selectSlot(1);
        assertEquals(1, inventory.selectedSlot());
    }

    private Item createMockItem() {
        Item testItem = mock(Item.class);
        when(testItem.parent()).then(invocation -> Optional.of(playerTransform));
        return testItem;
    }

}
