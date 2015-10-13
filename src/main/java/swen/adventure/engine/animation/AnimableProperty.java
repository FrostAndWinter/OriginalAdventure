package swen.adventure.engine.animation;

import swen.adventure.engine.Action;
import swen.adventure.engine.Event;

import java.util.Collections;
import java.util.Optional;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 2/10/15.
 * An AnimableProperty is a floating point number that can have its value vary over time according to an animation.
 * Instantiate an Animation with an AnimableProperty as an argument to start that animation on that property.
 */
public class AnimableProperty {
    private float _value;

    private Optional<Animation> _currentAnimation = Optional.empty();

    /**
     * The ValueChanged event triggers whenever the
     */
    public final Event<AnimableProperty, Animation> eventValueChanged = new Event<>("ValueChanged", this);

    public static final Action<Animation, Animation, AnimableProperty> actionAnimationDidFinish = (animation, triggeringObject, animatableProperty, data) -> {
        animatableProperty._currentAnimation.ifPresent(propertyAnimation -> {
            if (propertyAnimation.equals(animation)) {
                animatableProperty.setAnimation(null);
            }
        });
    };

    public AnimableProperty(float value) {
        this.setValue(value);
    }

    /**
     * Assigns an animation to this object, setting up listeners for its completion.
     * @param animation The animation which is attached to this property.
     */
    protected void setAnimation(Animation animation) {
        if (_currentAnimation.isPresent() && animation != _currentAnimation.get()) {
            this.stopAnimating();
        }

        _currentAnimation = Optional.ofNullable(animation);
        if (animation != null) {
            animation.eventAnimationDidComplete.addAction(this, AnimableProperty.actionAnimationDidFinish);
        }
    }

    /**
     * @return Whether the property's value is currently being altered by an animation.
     */
    public boolean isAnimating() {
        return _currentAnimation.isPresent();
    }

    /** Cancels an animation, causing it to stop immediately and be destroyed. */
    public void stopAnimating() {
        _currentAnimation.ifPresent(animation -> {
            _currentAnimation = Optional.empty();
            animation.cancel();
        });

    }

    protected void setValueFromAnimation(Animation animation, float value) {
        _value = value;
        this.eventValueChanged.trigger(animation, Collections.emptyMap());
    }

    /**
     * Directly sets the value of this property. Will print a warning and perform a no-op if the property is currently being affected by an animation.
     * @param value The value to set it to.
     */
    public void setValue(float value) {
        if (_currentAnimation.isPresent()) {
            System.err.println("WARNING: Tried to modify value on " + this + " to " + value + " when there is already a value assigned.");
            return;
        }
        _value = value;
        this.eventValueChanged.trigger(null, Collections.emptyMap());
    }

    public float value() {
        return _value;
    }
}
