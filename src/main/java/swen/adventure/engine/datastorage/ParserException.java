package swen.adventure.engine.datastorage;

/**
 * Created by liam on 14/10/15.
 *
 * Checked exception which signifies that the scene graph parser has encountered an unrecoverable state.
 */
public class ParserException extends Exception {

    public ParserException(String message) {
        super(message);
    }

    public ParserException(String message, Throwable throwable) {
        super(message, throwable);
    }

}
