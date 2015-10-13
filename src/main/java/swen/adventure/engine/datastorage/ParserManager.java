package swen.adventure.engine.datastorage;

import swen.adventure.engine.Utilities;
import swen.adventure.engine.rendering.maths.*;
import swen.adventure.game.Interaction;
import swen.adventure.game.InteractionType;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by Liam O'Neill, Student ID 300312734, on 18/09/15.
 */
public class ParserManager {

    private static final Map<Class<?>, Parser<?>> PARSERS;
    static {
        Map<Class<?>, Parser<?>> parsers = new HashMap<>();

        addParser(String.class, new Parser<>(Function.identity(), Function.identity()), parsers);
        addParser(Integer.class, new Parser<>(Object::toString, Integer::parseInt), parsers);
        addParser(Float.class, new Parser<>(Object::toString, Float::parseFloat), parsers);
        addParser(Long.class, new Parser<>(Object::toString, Long::parseLong), parsers);
        addParser(Boolean.class, new Parser<>(Object::toString, Boolean::parseBoolean), parsers);

        addParser(Vector3.class, new Parser<>(
                v -> toCsvString(Arrays.asList(v.x, v.y, v.z)),
                s -> {
                    List<Float> xyz = fromCsvString(s, Float.class);
                    return new Vector3(xyz.get(0), xyz.get(1), xyz.get(2));
                }), parsers);

        addParser(Vector4.class, new Parser<>(
                v -> toCsvString(Arrays.asList(v.x, v.y, v.z)),
                s -> {
                    List<Float> xyzw = fromCsvString(s, Float.class);
                    return new Vector4(xyzw.get(0), xyzw.get(1), xyzw.get(2), xyzw.get(3));
                }), parsers);

        addParser(BoundingBox.class, new Parser<>(
                b -> toCsvString(Arrays.asList(b.minPoint.x, b.minPoint.y, b.minPoint.z, b.maxPoint.x, b.maxPoint.y, b.maxPoint.z)),
                s -> {
                    List<Float> xyzxyz = fromCsvString(s, Float.class);
                    return new BoundingBox(new Vector3(xyzxyz.get(0), xyzxyz.get(1), xyzxyz.get(2)), new Vector3(xyzxyz.get(3), xyzxyz.get(4), xyzxyz.get(5)));
                }), parsers);

        addParser(Quaternion.class, new Parser<>(
                q -> toCsvString(Arrays.asList(q.x, q.y, q.z, q.w)),
                s -> {
                    List<Float> xyzw = fromCsvString(s, Float.class);
                    return new Quaternion(xyzw.get(0), xyzw.get(1), xyzw.get(2), xyzw.get(3));
                }), parsers);

        addParser(Matrix3.class, new Parser<>(
                m -> toCsvString(Arrays.asList(m)),
                s -> {
                    List<Float> entryValues = fromCsvString(s, Float.class);
                    return new Matrix3(Utilities.toPrimitiveArray(entryValues));
                }), parsers);

        addParser(Matrix4.class, new Parser<>(
                m -> toCsvString(Arrays.asList(m)),
                s -> {
                    List<Float> entryValues = fromCsvString(s, Float.class);
                    return new Matrix4(Utilities.toPrimitiveArray(entryValues));
                }), parsers);

        addParser(InteractionType.class, new Parser<>(
                i -> Integer.toString(i.ordinal()),
                s -> InteractionType.values()[Integer.parseInt(s)]), parsers);

        PARSERS = Collections.unmodifiableMap(parsers);
    }

    private static String toCsvString(List<?> elements) {
        return elements.stream()
                .map(Object::toString)
                .collect(Collectors.joining(", "));
    }

    private static <T> List<T> fromCsvString(String csv, Class<T> class0){
        Parser<T> parser = getParser(class0);
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .map(parser::convertToInstance)
                .collect(Collectors.toList());
    }

    private static <T> void addParser(Class<T> class0, Parser<T> parser, Map<Class<?>, Parser<?>> parsers) {
        parsers.put(class0, parser);
    }

    private static <T> Parser<T> getParser(Class<T> class0) {
        Parser<?> unknownParser = PARSERS.get(class0);

        if(unknownParser == null)
            throw new ClassCastException(class0.getSimpleName() + " doesn't have a parser.");

        @SuppressWarnings("unchecked")
        Parser<T> uncheckedParser = (Parser<T>)unknownParser;
        return uncheckedParser;
    }

    static <T> Function<T, String> getToStringFunction(Class<T> class0){
        return getParser(class0).toStringFunction;
    }

    static <T> Function<String, T> getFromStringFunction(Class<T> class0){
        return getParser(class0).fromStringFunction;
    }

    public static <T> String convertToString(T t, Class<T> class0) {
        return getParser(class0).convertToString(t);
    }

    public static <T> T convertFromString(String str, Class<T> class0) {
        return getParser(class0).convertToInstance(str);
    }

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
