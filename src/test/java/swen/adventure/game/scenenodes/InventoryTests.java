/* Contributor List  */ 
 /* Thomas Roughton (roughtthom) (300313924) */ 
 /* Liam O'Niell (oneilliam) (300312734) */ 
 /* Joseph Bennett (bennetjose) (300319773) */ 
 /* David Barnett (barnetdavi) (300313764) */ 
 /* Daniel Braithwaite (braithdani) (300313770) */ 
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
 * Moderfied by Daniel Braithwaite ID: 300313770
 */
public class InventoryTests {

    private TransformNode playerTransform;
    private TransformNode rootTransform;
    private TransformNode itemTransform;
    private Player player;

    @Before
    public void setup() {
        rootTransform = new TransformNode("rootTransform", Vector3.zero, new Quaternion(), Vector3.one);
        itemTransform = new TransformNode("itemTransform", rootTransform, true, Vector3.zero, new Quaternion(), Vector3.one);
        playerTransform = new TransformNode("playerTransform", rootTransform, true, Vector3.zero, new Quaternion(), Vector3.one);

        player = mock(Player.class);
        when(player.parent()).then(invocation -> Optional.of(playerTransform));
    }

    @Test
    public void testCanSelectSlot() {
        Inventory inventory = createInventory();

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
        Inventory inventory = createInventory();
        inventory.selectSlot(inventory.capacity() + 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void cannotSelectNegativeSlot() {
        Inventory inventory = createInventory();
        inventory.selectSlot(-1);
    }

    @Test
    public void selectingSlotUpdatesSelectedSlot() {
        Inventory inventory = createInventory();
        assertEquals(0, inventory.selectedSlot());

        inventory.selectSlot(1);
        assertEquals(1, inventory.selectedSlot());
    }

    private Item createMockItem() {
        Item testItem = mock(Item.class);
        when(testItem.parent()).then(invocation -> Optional.of(itemTransform));
        return testItem;
    }

    private Inventory createInventory() {
        return new Inventory(player.id, player.parent().get());
    }

}