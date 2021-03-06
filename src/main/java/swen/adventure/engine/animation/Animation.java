/* Contributor List  */ 
 /* Thomas Roughton (roughtthom) (300313924) */ 
 /* Joseph Bennett (bennetjose) (300319773) */ 
 package swen.adventure.engine.animation;

import swen.adventure.engine.Event;

import java.util.Collections;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 2/10/15.
 * An Animation, combined with an AnimableProperty, automatically adjusts the value of that property over time.
 */
public class Animation {

    private AnimableProperty _animableProperty;

    private double _startTime;

    private double _initialValue;
    private double _finalValue;
    private double _duration;
    private boolean _repeats;

    private boolean _shouldStopRepeating;
    private boolean _hasCompletedCycleSinceStoppingRepeating;

    private boolean _complete = false;
    private AnimationCurve _curve = AnimationCurve.Linear;

    public final Event<Animation, Animation> eventAnimationDidComplete = new Event<>("AnimationDidComplete", this);
    public final Event<Animation, Animation> eventAnimationDidCancel = new Event<>("AnimationDidCancel", this);

    public Animation(AnimableProperty animableProperty, AnimationCurve curve, double duration, double delay, double toValue, boolean repeats) {
        _animableProperty = animableProperty;
        _animableProperty.setAnimation(this);

        _initialValue = animableProperty.value();

        _finalValue = toValue;
        _startTime = AnimationSystem.currentTime() + delay;
        _duration = duration;
        _repeats = repeats;
        _curve = curve;

        AnimationSystem.addAnimation(this);
    }

    public Animation(AnimableProperty animableProperty, double duration, double toValue, boolean repeats) {
        this(animableProperty, AnimationCurve.Linear, duration, 0.0, toValue, repeats);
    }

    /**
     * Creates a new random animation that varies between the current value and the toValue.
     * @param animableProperty The property to animate
     * @param toValue The maximum value the property can take.
     */
    public Animation(AnimableProperty animableProperty, double toValue) {
        this(animableProperty, AnimationCurve.Random, Float.MAX_VALUE, 0.0, toValue, true);
    }

    public Animation(AnimableProperty animableProperty, double duration, double delay, double toValue) {
        this(animableProperty, AnimationCurve.Linear, duration, delay, toValue, false);
    }

    public Animation(AnimableProperty animableProperty, double duration, double toValue) {
        this(animableProperty, duration, toValue, false);
    }

    public Animation(AnimableProperty animableProperty, AnimationCurve curve, double duration, double toValue) {
        this(animableProperty, curve, duration, 0.0, toValue, false);
    }

    private double percentageComplete(double currentTime) {
        double elapsedTime = currentTime - _startTime;
        double percentage = elapsedTime/_duration;

        if (_shouldStopRepeating && percentage >= 1.0) {
            _hasCompletedCycleSinceStoppingRepeating = true;
        }

        double percentageThroughCycle = percentage - (int)percentage;
        if (_repeats && !_hasCompletedCycleSinceStoppingRepeating) {
            percentage = percentageThroughCycle;
        }
        percentage = Math.min(Math.max(0.0, percentage), 1.0);
        return percentage;
    }

    private double currentValue(double currentTime) {
        if (_curve == AnimationCurve.Random) {
            double value = Math.random() * (_finalValue - _initialValue) + _initialValue;
            return value;
        } else {
            double progress = _curve.progressForPercentage(this.percentageComplete(currentTime));
            return _initialValue + (_finalValue - _initialValue) * progress;
        }
    }

    protected void update(double currentTime) {
        double value = this.currentValue(currentTime);

        _animableProperty.setValueFromAnimation(this, (float)value);

        if (!_repeats && value == _finalValue) {
            this.setComplete(true);
            this.eventAnimationDidComplete.trigger(this, Collections.emptyMap());
        }
    }

    /** Cancels the animation so it ceases to update any properties. */
    public void cancel() {
        this.setComplete(true);
        this.eventAnimationDidCancel.trigger(this, Collections.emptyMap());
    }

    private void setComplete(boolean complete) {
        _complete = complete;
    }

    /** @return Whether the animation is still in progress. */
    public boolean isComplete() {
        return _complete;
    }

    /** Stops the animation from repeating again after this cycle. */
    public void stopRepeating() {
        _shouldStopRepeating = true;
    }

}