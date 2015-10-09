package swen.adventure.game.ui.components;

import processing.core.PGraphics;
import swen.adventure.engine.Action;
import swen.adventure.engine.animation.AnimableProperty;
import swen.adventure.engine.rendering.GLRenderer;
import swen.adventure.engine.rendering.maths.Matrix4;
import swen.adventure.engine.rendering.maths.Quaternion;
import swen.adventure.engine.rendering.maths.Vector3;
import swen.adventure.engine.scenegraph.Light;
import swen.adventure.engine.scenegraph.MeshNode;
import swen.adventure.engine.scenegraph.SceneNode;
import swen.adventure.engine.scenegraph.TransformNode;
import swen.adventure.engine.ui.components.UIComponent;
import swen.adventure.engine.ui.layoutmanagers.LayoutManager;
import swen.adventure.game.Interaction;
import swen.adventure.game.scenenodes.Inventory;
import swen.adventure.game.scenenodes.Item;
import swen.adventure.game.scenenodes.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Created by danielbraithwt on 9/18/15.
 * Modified by Thomas Roughton, Student ID 300313924
 */
public class InventoryComponent extends UIComponent {
    private static final int BOX_SIZE = 30;
    private static final int INNER_PADDING = 6;

    private int boxSize;

    private int selectedItem = 0;
    private boolean showItem;

    private final Inventory _inventory;
    private final TransformNode _rootSceneNode = new TransformNode("root", Vector3.zero, new Quaternion(), Vector3.one);
    private final TransformNode _toScreenTransform = new TransformNode("toScreenTransform", _rootSceneNode, true, new Vector3(0.f, 0.f, -1.f), new Quaternion(), Vector3.one);
    private final Light _modelLight = Light.createDirectionalLight("directionalLight", _rootSceneNode, Vector3.one.normalise(), 0.5f, new Vector3(0.5f, 0.5f, 0.5f));

    private final Matrix4 _projectionMatrix = Matrix4.makeOrtho(-1.f, 1.f, -1.f, 1.f, 1.f, 1000.f);

    private final List<Interaction> _interactionsForStep = new ArrayList<>();

    public InventoryComponent(Inventory inventory, int x, int y) {
        super(x, y, Inventory.Capacity * BOX_SIZE, BOX_SIZE);

        _inventory = inventory;

        boxSize = BOX_SIZE;
    }

    public static final Action<Inventory, Player, InventoryComponent> actionSelectSlot =
            (playerInventory, triggeringObject, inventoryView, data) -> {
                Integer item = (Integer) data.get(Inventory.SelectedSlot);
                inventoryView.setSelectedItem(item);
            };

    public static final Action<Inventory, Player, InventoryComponent> actionProvideInteraction =
            (playerInventory, triggeringObject, inventoryView, data) -> {
                Integer item = (Integer) data.get(Inventory.SelectedSlot);
                inventoryView.setSelectedItem(item);
            };

    public void setBoxSize(int boxSize) {
        this.boxSize = boxSize;
    }

    public void setSelectedItem(int s) {
        selectedItem = s;
    }

    public int getSelectedItem() {
        return selectedItem;
    }

    public boolean getShowItem() {
        return showItem;
    }

    public void setShowItem(boolean showItem) {
        if (_inventory.getSelectedSlot() >= _inventory.items().size()) {
            return;
        }

        this.showItem = showItem;
    }

    @Override
    public void setLayoutManager(LayoutManager lm) {
        throw new UnsupportedOperationException("Inventory cant use a layout manager");
    }

    @Override
    public void addChild(UIComponent c) {
        throw new UnsupportedOperationException("Inventory cant contain child ui elements");
    }

    @Override
    public void removeChild(UIComponent c) {
        throw new UnsupportedOperationException("Inventory cant contain child ui elements");
    }

    @Override
    protected void drawComponent(PGraphics g, float scaleX, float scaleY) {
        int currentX = x;
        int currentY = y;

        if (showItem) {
            g.fill(255);
            g.text(_inventory.items().get(_inventory.getSelectedSlot()).getDescription(), x * scaleX, y * scaleY + (height * scaleY)/2);
        }

        for (int i = 0; i < Inventory.Capacity; i++) {
            g.fill(0);
            if (i == selectedItem) {
                g.fill(180);
            }

            if (showItem) {
                g.fill(0,0,0,0);
            }

            g.rect(currentX * scaleX, currentY * scaleY, boxSize * scaleX, boxSize * scaleY);

            if (!showItem) {
                g.fill(34, 50, 90);
            }
            g.rect((currentX + (INNER_PADDING/2)) * scaleX, (currentY + (INNER_PADDING/2)) * scaleY, (boxSize - INNER_PADDING) * scaleX, (boxSize - INNER_PADDING) * scaleY);
            currentX += boxSize;
        }
    }

    /**
     *
     * @param renderer
     * @param scaleX
     * @param scaleY
     * @param width
     * @param height
     */
    public void drawItems(GLRenderer renderer, float scaleX, float scaleY, float dx, float dy, float width, float height) {

        _toScreenTransform.setScale(new Vector3(2.f / width, 2.f / height, 1.f));
        _toScreenTransform.setTranslation(new Vector3(-1.f, -1.f, -1.f));

        float currentX = this.x;

        List<MeshNode> nodesToRender = new ArrayList<>();

        for (int i = 0; i < _inventory.items().size(); i++) {
                Item item = _inventory.items().get(i);

                final int current = i;
                final float finalCurrentX = currentX;

                item.mesh().ifPresent(itemMesh -> {

                    Optional<SceneNode> meshNodeOpt = _toScreenTransform.nodeWithID(itemMesh.id);
                    MeshNode meshNode;
                    if (meshNodeOpt.isPresent()) {
                        meshNode = (MeshNode) meshNodeOpt.get();
                    } else {
                        TransformNode transformNode = new TransformNode(itemMesh.id + "Transform", _toScreenTransform, true, Vector3.zero, new Quaternion(), Vector3.one);
                        meshNode = new MeshNode(itemMesh.id, itemMesh.getDirectory(), itemMesh.getFileName(), transformNode);
                        itemMesh.materialOverride().ifPresent(meshNode::setMaterialOverride);
                    }

                    meshNode.setEnabled(true);

                    float meshMaxDimension = Math.max(meshNode.boundingBox().width(), meshNode.boundingBox().height());

                    TransformNode transformNode = meshNode.parent().get();

                    if (current == _inventory.getSelectedSlot() && showItem) {
                        int w = (int) (width - 2 * dx);
                        int h = (int) (height - 2 * dy - this.height * scaleY * 3);

                        float xScale = (w) / meshMaxDimension;
                        float yScale = (h) / meshMaxDimension;
                        float scale = Math.min(xScale, yScale);

                        transformNode.setTranslation(new Vector3(dx + w/2, dy + h/2 + this.height * scaleY* 3, 0.f));
                        transformNode.setScale(new Vector3(scale, scale, 1.f));
                    } else {
                        float xScale = (boxSize) * scaleX / meshMaxDimension;
                        float yScale = (boxSize) * scaleY / meshMaxDimension;

                        transformNode.setTranslation(new Vector3(dx + (finalCurrentX + boxSize / 2) * scaleX, dy + (height) - (this.y + boxSize / 2) * scaleY, 0.f));
                        transformNode.setScale(new Vector3(xScale, yScale, 1.f));
                    }

                    nodesToRender.add(meshNode);
                });

            currentX += boxSize;
        }

        renderer.render(nodesToRender, Collections.singletonList(_modelLight), Matrix4.identity, _projectionMatrix, 1.5f);
    }


    @Override
    public boolean withinBounds(int x, int y) {
        return (x > this.x && y > this.y) && (x < this.x + this.width && y < this.y + this.height);
    }

    @Override
    protected void componentClicked(int x, int y) {

    }
}
