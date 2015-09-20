package swen.adventure.datastorage;

import org.junit.Test;
import swen.adventure.Utilities;

import java.io.File;
import java.io.IOException;

/**
 * Created by Liam O'Neill, Student ID 300312734, on 20/09/15.
 */
public class WavefrontParserTest {

    @Test
    public void testVertexTextureObj() {
        String input =
                "# List of geometric vertices, with (x,y,z[,w]) coordinates, w is optional and defaults to 1.0.\n" +
                "v 0.123 0.234 0.345 1.0\n" +
                "# List of texture coordinates, in (u, v [,w]) coordinates, these will vary between 0 and 1, w is optional and defaults to 0.\n" +
                "vt 0.500 1 0\n" +
                "# List of vertex normals in (x,y,z) form; normals might not be unit vectors.\n" +
                "vn 0.707 0.000 0.707\n";

        WavefrontParser.Result result = WavefrontParser.parse(input);
        System.out.println(result);
    }

    @Test
    public void testLoadingTableFile() throws IOException {
        File file = new File(Utilities.pathForResource("Table", "obj"));
        WavefrontParser.Result result = WavefrontParser.parse(file);
        System.out.println(result);
    }
}
