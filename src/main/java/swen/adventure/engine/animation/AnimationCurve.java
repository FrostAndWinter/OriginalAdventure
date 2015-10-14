/* Contributor List  */ 
 /* Thomas Roughton (roughtthom) (300313924) */ 
 package swen.adventure.engine.animation;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 2/10/15.
 */
public enum AnimationCurve {
    Linear,
    Sine,
    Random;

    /**
     * Given a percentage progress, this will compute the output progress.
     * @param percentage A percentage progress in the range [0, 1].
     * @return A scaled progress in the range [0, 1].
     */
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