/* Contributor List  */ 
 /* Thomas Roughton (roughtthom) (300313924) */ 
 /* Joseph Bennett (bennetjose) (300319773) */ 
 package swen.adventure.game.input;

import swen.adventure.engine.Event;
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

    public final Event<MouseInput, MouseInput> eventMousePrimaryAction = new Event<>("MousePrimaryAction", this);
    public final Event<MouseInput, MouseInput> eventMouseSecondaryAction = new Event<>("MouseSecondaryAction", this);
    public final Event<MouseInput, MouseInput> eventMousePrimaryActionEnded = new Event<>("MousePrimaryActionEnded", this);
    public final Event<MouseInput, MouseInput> eventMouseSecondaryActionEnded = new Event<>("MouseSecondaryActionEnded", this);
}