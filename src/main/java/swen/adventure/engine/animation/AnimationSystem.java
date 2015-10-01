package swen.adventure.engine.animation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 2/10/15.
 */
public class AnimationSystem {

    private static List<Animation> _animations = new ArrayList<>();

    protected static double currentTime() {
        return System.currentTimeMillis()/1000.0;
    }

    protected static void addAnimation(Animation animation) {
        _animations.add(animation);
    }

    protected static void removeAnimation(Animation animation) {
        _animations.remove(animation);
    }

    /**
     * Updates the animation system, changing the values of all animatable
     */
    public static void update() {
        double currentTime = AnimationSystem.currentTime();
        Iterator<Animation> iterator = _animations.iterator();

        while (iterator.hasNext()) {
            Animation animation = iterator.next();
            if (animation.isComplete()) {
                animation.destroy();
                iterator.remove();
                continue;
            }
            animation.update(currentTime);
        }
    }

}
