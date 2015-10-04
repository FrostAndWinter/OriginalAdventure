package swen.adventure.engine.animation;

import swen.adventure.engine.Action;
import swen.adventure.engine.Event;

import java.util.Collections;
import java.util.Optional;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 2/10/15.
 */
public class AnimableProperty {
    private float _value;

    private Optional<Animation> _currentAnimation = Optional.empty();

    public final Event<AnimableProperty> eventValueChanged = new Event<>("eventValueChanged", this);

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

    public void stopAnimating() {
        _currentAnimation.ifPresent(animation -> {
            _currentAnimation = Optional.empty();
            animation.destroy();
        });

    }

    protected void setValueFromAnimation(float value) {
        _value = value;
        eventValueChanged.trigger(_currentAnimation.get(), Collections.emptyMap());
    }

    public void setValue(float value) {
        if (_currentAnimation.isPresent()) {
            System.err.println("WARNING: Tried to modify value on " + this + " to " + value + " when there is already a value assigned.");
            return;
        }
        _value = value;
    }

    public float value() {
        return _value;
    }
}
