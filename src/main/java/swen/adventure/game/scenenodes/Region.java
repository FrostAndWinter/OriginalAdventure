package swen.adventure.game.scenenodes;

import swen.adventure.engine.Action;
import swen.adventure.engine.Event;
import swen.adventure.engine.rendering.maths.BoundingBox;
import swen.adventure.engine.rendering.maths.Vector3;
import swen.adventure.engine.scenegraph.SceneNode;
import swen.adventure.engine.scenegraph.TransformNode;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 14/10/15.
 */
public class Region extends SceneNode {

    public final String regionName;

    private Set<Player> _playersInRegion = new HashSet<>();

    public final Event<Region, Player> eventRegionEntered = new Event<>("RegionEntered", this);
    public final Event<Region, Player> eventRegionExited = new Event<>("RegionExited", this);

    private BoundingBox _boundingBox;

    public Region(String regionName, BoundingBox boundingBox, TransformNode parent) {
        super(regionName, parent, false);
        this.regionName = regionName;
        _boundingBox = boundingBox;

        Event.EventSet<Player, Player> playerMovedSet = (Event.EventSet<Player, Player>)Event.eventSetForName("PlayerMoved");
        playerMovedSet.addAction(this, Region.actionPlayerMoved);
    }

    /**
     * Called when any player moves.
     */
    public static final Action<Player, Player, Region> actionPlayerMoved = (player, ignored, region, data) -> {
      Vector3 playerLocation = player.nodeToWorldSpaceTransform().multiplyWithTranslation(Vector3.zero);
       region.playerMovedToLocation(player, playerLocation);
    };

    private void playerMovedToLocation(Player player, Vector3 location) {
        boolean isInRegion = _boundingBox.containsPoint(location);

        if (isInRegion && !_playersInRegion.contains(player)) {
            _playersInRegion.add(player);
            this.eventRegionEntered.trigger(player, Collections.emptyMap());
        } else if (!isInRegion && _playersInRegion.contains(player)) {
            _playersInRegion.remove(player);
            this.eventRegionExited.trigger(player, Collections.emptyMap());
        }
    }
}
