package swen.adventure.engine.datastorage;

import swen.adventure.game.scenenodes.Chest;

import java.util.*;

/**
 * Created by liam on 5/10/15.
 */
public class SceneGraphPropertyRegister {

    // this class shouldn't be instantiable
    private SceneGraphPropertyRegister(){
    }

    private static final Map<Class<?>, Set<Property<?,?>>> PROPERTY_REGISTER;

    static {
        Map<Class<?>, Set<Property<?,?>>> propertyRegister = new HashMap<>();

        // important: adding properties will overwrite all previous properties for that class

        addProperties(propertyRegister, Chest.class,
                new Property<>(Chest::isOpen, Chest::setOpen)
        );

        PROPERTY_REGISTER = Collections.unmodifiableMap(propertyRegister);
    }

    /**
     * This method ensures that the type of each pair in the map is correct i.e Class<T> -> Property<T, ?>.
     *
     * @param propertyRegistry
     * @param class0
     * @param properties
     * @param <T>
     */
    private static <T> void addProperties(Map<Class<?>, Set<Property<?, ?>>> propertyRegistry,
                                          Class<T> class0, Property<T, ?>... properties) {
        propertyRegistry.put(class0, new HashSet<>(Arrays.asList(properties)));
    }
}
