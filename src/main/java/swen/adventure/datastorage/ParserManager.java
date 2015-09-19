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

    private static Parser<?> getParser(Class<?> class0) {
        Parser<?> parser = PARSERS.get(class0);

        if(parser == null)
            throw new ClassCastException(class0.getSimpleName() + " doesn't have a parser.");

        return parser;
    }

    String convertToString(Object object, Class<?> class0) {
        return getParser(class0).convertToString(object);
    }

    Object convertFromString(String str, Class<?> class0) {
        return getParser(class0).convertToInstance(str);
    }

    private static class Parser<T> {
        private final Function<Object, String> toStringFunction;
        private final Function<String, Object> fromStringFunction;

        @SuppressWarnings("unchecked")
        public Parser(Function<T, String> toStringFunction, Function<String, T> fromStringFunction) {
            this.toStringFunction = (Function<Object, String>)toStringFunction;
            this.fromStringFunction = (Function<String, Object>)fromStringFunction;
        }

        String convertToString(Object object) {
            return toStringFunction.apply(object);
        }

        Object convertToInstance(String str) {
            return fromStringFunction.apply(str);
        }
    }
}
