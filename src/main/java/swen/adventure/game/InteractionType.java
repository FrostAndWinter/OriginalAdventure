/* Contributor List  */ 
 /* Thomas Roughton (roughtthom) (300313924) */ 
 /* David Barnett (barnetdavi) (300313764) */ 
 package swen.adventure.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;

/**
 * Created by drb on 13/10/15.
 */
public enum InteractionType {
    PickUp(Interaction.ActionType.Primary),
    PlaceIn(Interaction.ActionType.Secondary),
    Open(Interaction.ActionType.Primary),
    Close(Interaction.ActionType.Primary),
    Give(Interaction.ActionType.Primary),
    Pull(Interaction.ActionType.Primary),
    DisplayName(Interaction.ActionType.Primary);

    public final Interaction.ActionType actionType;

    InteractionType(Interaction.ActionType actionType) {
        this.actionType = actionType;
    }

    private static EnumMap<Interaction.ActionType, List<InteractionType>> _actionTypesToInteractionTypes = new EnumMap<>(Interaction.ActionType.class);
    static {
        for (InteractionType interactionType : InteractionType.values()) {
            List<InteractionType> interactionTypes = _actionTypesToInteractionTypes.get(interactionType.actionType);
            if (interactionTypes == null) { interactionTypes = new ArrayList<>(); _actionTypesToInteractionTypes.put(interactionType.actionType, interactionTypes); }
            interactionTypes.add(interactionType);
        }

    }

    public static List<InteractionType> typesForActionType(Interaction.ActionType actionType) {
        return _actionTypesToInteractionTypes.getOrDefault(actionType, Collections.emptyList());
    }
}