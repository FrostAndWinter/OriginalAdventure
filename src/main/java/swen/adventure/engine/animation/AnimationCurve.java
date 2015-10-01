package swen.adventure.engine.animation;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 2/10/15.
 */
public enum AnimationCurve {
    Linear,
    Sine;

    public double progressForPercentage(double percentage) {
        switch (this) {
            case Linear:
                return percentage;
            case Sine:
                return Math.sin(percentage * Math.PI/2.0);
            default:
                return percentage;
        }
    }
}