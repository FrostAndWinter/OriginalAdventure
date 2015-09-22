package swen.adventure.datastorage;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Created by Liam O'Neill, Student ID 300312734, on 18/09/15.
 */
class ParserManager {

    private static final Map<Class<?>, Parser<?>> PARSERS;
    static {
        Map<Class<?>, Parser<?>> parsers = new HashMap<>();

        addParser(String.class, new Parser<>(Function.identity(), Function.identity()), parsers);
        addParser(Integer.class, new Parser<>(Object::toString, Integer::parseInt), parsers);
        addParser(Float.class, new Parser<>(Object::toString, Float::parseFloat), parsers);
        addParser(Long.class, new Parser<>(Object::toString, Long::parseLong), parsers);

        PARSERS = Collections.unmodifiableMap(parsers);
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

    <T> Function<T, String> getToStringFunction(Class<T> class0){
        return getParser(class0).toStringFunction;
    }

    <T> Function<String, T> getFromStringFunction(Class<T> class0){
        return getParser(class0).fromStringFunction;
    }

    <T> String convertToString(T t, Class<T> class0) {
        return getParser(class0).convertToString(t);
    }

    <T> T convertFromString(String str, Class<T> class0) {
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
