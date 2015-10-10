package swen.adventure.game.input;

import swen.adventure.engine.Event;
import swen.adventure.engine.KeyInput;
import swen.adventure.engine.MouseInput;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 10/10/15.
 */
public class AdventureGameMouseInput extends MouseInput {

    public AdventureGameMouseInput() {
        super();

        this.onPressMappings.put(Button.Left, this.eventMousePrimaryAction);
        this.onPressMappings.put(Button.Right, this.eventMouseSecondaryAction);
        this.onReleasedMappings.put(Button.Left, this.eventMousePrimaryActionEnded);
        this.onReleasedMappings.put(Button.Right, this.eventMouseSecondaryActionEnded);
    }

    public final Event<MouseInput, MouseInput> eventMousePrimaryAction = new Event<>("eventMousePrimaryAction", this);
    public final Event<MouseInput, MouseInput> eventMouseSecondaryAction = new Event<>("eventMouseSecondaryAction", this);
    public final Event<MouseInput, MouseInput> eventMousePrimaryActionEnded = new Event<>("eventMousePrimaryActionEnded", this);
    public final Event<MouseInput, MouseInput> eventMouseSecondaryActionEnded = new Event<>("eventMouseSecondaryActionEnded", this);
}
