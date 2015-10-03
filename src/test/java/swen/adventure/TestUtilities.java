package swen.adventure;

import java.io.File;

/**
 * Created by Liam O'Neill, Student ID 300312734, on 03/10/15.
 */
public class TestUtilities {

    private static final String DIRECTORY = "/tmp/";

    public static File createTestFile(String fName) {
        return new File(DIRECTORY + fName);
    }
}
