package swen.adventure.engine.rendering.octree;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 15/09/15.
 */
public enum Direction {
    FrontUpLeft(0),
    FrontUpRight(1),
    BackUpLeft(2),
    BackUpRight(3),
    FrontDownLeft(4),
    FrontDownRight(5),
    BackDownLeft(6),
    BackDownRight(7);

    public final int index;

    Direction(int index) {
        this.index = index;
    }
}