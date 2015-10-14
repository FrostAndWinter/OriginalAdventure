package swen.adventure.engine.datastorage;

import swen.adventure.engine.rendering.maths.BoundingBox;
import swen.adventure.engine.rendering.maths.Quaternion;
import swen.adventure.engine.rendering.maths.Vector3;
import swen.adventure.engine.rendering.maths.Vector4;
import swen.adventure.game.InteractionType;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * Created by Liam O'Neill, Student ID 300312734, on 18/09/15.
 *
 * ParserManager holds all implemented parsers and exposes toString and fromString functions for each type.
 */
public class ParserManager {

    // Map of classes to the parser for that class.
    // While java's type system isn't powerful enough to express this
    //      but the type param of the class and the parser are guaranteed to match for every entry.
    // To enforce this guarantee only the addParser method should mutate this map.
    private static final Map<Class<?>, Parser<?>> PARSERS;

    // static initializer to load all parsers
    static {
        Map<Class<?>, Parser<?>> parsers = new HashMap<>();

        addParser(String.class, new Parser<>(Function.identity(), Function.identity()), parsers);
        addParser(Integer.class, new Parser<>(Object::toString, Integer::parseInt), parsers);
        addParser(Float.class, new Parser<>(Object::toString, Float::parseFloat), parsers);
        addParser(Long.class, new Parser<>(Object::toString, Long::parseLong), parsers);
        addParser(Boolean.class, new Parser<>(Object::toString, Boolean::parseBoolean), parsers);

        addParser(Vector3.class, new Parser<>(
                v -> toCsvString(Arrays.asList(v.x, v.y, v.z), Float.class),
                s -> {
                    List<Float> xyz = fromCsvString(s, Float.class);
                    return new Vector3(xyz.get(0), xyz.get(1), xyz.get(2));
                }), parsers);

        addParser(Vector4.class, new Parser<>(
                v -> toCsvString(Arrays.asList(v.x, v.y, v.z), Float.class),
                s -> {
                    List<Float> xyzw = fromCsvString(s, Float.class);
                    return new Vector4(xyzw.get(0), xyzw.get(1), xyzw.get(2), xyzw.get(3));
                }), parsers);

        addParser(BoundingBox.class, new Parser<>(
                b -> toCsvString(Arrays.asList(b.minPoint.x, b.minPoint.y, b.minPoint.z, b.maxPoint.x, b.maxPoint.y, b.maxPoint.z), Float.class),
                s -> {
                    List<Float> xyzxyz = fromCsvString(s, Float.class);
                    return new BoundingBox(new Vector3(xyzxyz.get(0), xyzxyz.get(1), xyzxyz.get(2)), new Vector3(xyzxyz.get(3), xyzxyz.get(4), xyzxyz.get(5)));
                }), parsers);

        addParser(Quaternion.class, new Parser<>(
                q -> toCsvString(Arrays.asList(q.x, q.y, q.z, q.w), Float.class),
                s -> {
                    List<Float> xyzw = fromCsvString(s, Float.class);
                    return new Quaternion(xyzw.get(0), xyzw.get(1), xyzw.get(2), xyzw.get(3));
                }), parsers);

        addParser(InteractionType.class, new Parser<>(
                i -> Integer.toString(i.ordinal()),
                s -> InteractionType.values()[Integer.parseInt(s)]), parsers);

        addParser(String[].class, new Parser<>(
                a -> toCsvString(Arrays.asList(a), String.class),
                s -> {
                    List<String> strings = fromCsvString(s, String.class);
                    return strings.toArray(new String[strings.size()]);
                }), parsers);

        PARSERS = Collections.unmodifiableMap(parsers);
    }

    /**
     * Helper method to convert a list of elements into a string with commas separating the elements (a csv string).
     *
     * @param elements list of elements to convert into a csv string
     * @return a csv string formed from the elements
     */
    private static <T> String toCsvString(List<T> elements, Class<T> class0) {
        Function<T, String> toStringFunction = getToStringFunction(class0);
        return elements.stream()
                .map(toStringFunction::apply)
                .collect(Collectors.joining(", "));
    }

    /**
     * Parse a csv string into its component elements.
     * @param csv comma separated values
     * @param class0 class object which matches the final elements
     * @return list of parsed elements
     */
    private static <T> List<T> fromCsvString(String csv, Class<T> class0){
        Parser<T> parser = getParser(class0);
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .map(parser::convertToInstance)
                .collect(Collectors.toList());
    }

    /**
     * This makes sure the guarantee on the parser map holds.
     * It makes sure the type params for the class and the parser are the same.
     *
     * @param class0 the class of the objects to parse
     * @param parser the parser instance holding the convert to and from string functions
     * @param parsers the map to add to
     */
    private static <T> void addParser(Class<T> class0, Parser<T> parser, Map<Class<?>, Parser<?>> parsers) {
        parsers.put(class0, parser);
    }

    /**
     * Get the parser for this class
     * @param class0 the class to get the parser for
     * @throws ClassCastException if there isn't a parser associated with this class
     * @return parser associated with this class
     */
    private static <T> Parser<T> getParser(Class<T> class0) {
        Parser<?> unknownParser = PARSERS.get(class0);

        if(unknownParser == null)
            throw new ClassCastException(class0.getSimpleName() + " doesn't have a parser.");

        // this is guaranteed to be correct as the map guarantees the type params for each entry match
        @SuppressWarnings("unchecked")
        Parser<T> uncheckedParser = (Parser<T>)unknownParser;
        return uncheckedParser;
    }

    /**
     * Get the toString function for a class
     * @param class0 class to retrieve the to string function for
     * @return a function from T to String
     */
    static <T> Function<T, String> getToStringFunction(Class<T> class0){
        return getParser(class0).toStringFunction;
    }

    /**
     * Get the fromString function for a class
     * @param class0 class to retrieve the from string function for
     * @return a function from String to T
     */
    static <T> Function<String, T> getFromStringFunction(Class<T> class0){
        return getParser(class0).fromStringFunction;
    }

    /**
     * Convert a object to a string by using its toString parser function.
     *
     * @param t object to convert to a string
     * @param class0 class of the object
     * @return a string outputted by that class's toString function
     */
    public static <T> String convertToString(T t, Class<T> class0) {
        return getParser(class0).convertToString(t);
    }

    /**
     * Convert a string into a object of the passed class
     *
     * @param str string to convert into an instance of the given class
     * @param class0 target class of the object
     * @return the object outputted by that class's fromString function
     */
    public static <T> T convertFromString(String str, Class<T> class0) {
        return getParser(class0).convertToInstance(str);
    }

    /**
     * Private class which wraps up the toString and the fromString function for a given type.
     */
    private static class Parser<T> {
        private final Function<T, String> toStringFunction;
        private final Function<String, T> fromStringFunction;

        public Parser(Function<T, String> toStringFunction, Function<String, T> fromStringFunction) {
            this.toStringFunction = toStringFunction;
            this.fromStringFunction = fromStringFunction;
        }

        String convertToString(T t) {
            return toStringFunction.apply(t);
        }

        T convertToInstance(String str) {
            return fromStringFunction.apply(str);
        }
    }
}
