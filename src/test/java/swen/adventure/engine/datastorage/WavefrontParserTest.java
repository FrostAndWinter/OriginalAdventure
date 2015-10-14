package swen.adventure.engine.datastorage;

import org.junit.Test;
import swen.adventure.engine.Utilities;
import swen.adventure.engine.rendering.maths.Vector;
import swen.adventure.engine.rendering.maths.Vector3;
import swen.adventure.engine.rendering.maths.Vector4;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * Created by Liam O'Neill, Student ID 300312734, on 20/09/15.
 */
public class WavefrontParserTest {

    @Test public void
    should_be_able_to_handle_comments() {
        String input =
                "# List of geometric vertices, with (x,y,z[,w]) coordinates, w is optional and defaults to 1.0.\n" +
                "v 0.123 0.234 0.345 1.0\n";

        WavefrontParser.Result result = parse(input);
        assertEquals(
                singletonList(new Vector3(0.123f, 0.234f, 0.345f)),
                result.geometricVertices
        );
    }

    @Test public void
    should_be_able_to_parse_geometric_vertices() {
        String input =
                "v 0.123 0.234 0.345 1.0\n" +
                "v 2.454 7.899 1.273 0.5";

        WavefrontParser.Result result = parse(input);
        assertEquals(
                asList(
                        new Vector3(0.123f, 0.234f, 0.345f),
                        new Vector4(2.454f, 7.899f, 1.273f, 0.5f)
                ),
                result.geometricVertices
        );
    }

    @Test public void
    should_be_able_to_parse_texture_coordinates() {
        String input =
                "vt 0.500 1 0\n" +
                "vt 0.700 1.2\n";

        WavefrontParser.Result result = parse(input);
        assertEquals(
                asList(
                        new Vector3(0.5f, 1f, 0f),
                        new Vector3(0.7f, 1.2f, 0f)
                ),
                result.textureVertices
        );
    }

    @Test public void
    should_be_able_to_parse_vertex_normals() {
        String input =
                "vn 0.707 0.000 0.707 \n" +
                "vn 0.700 1.2 0.80 \n";

        WavefrontParser.Result result = parse(input);
        assertEquals(
                asList(
                        new Vector3(0.707f, 0f, 0.707f),
                        new Vector3(0.7f, 1.2f, 0.8f)
                ),
                result.vertexNormals
        );
    }

    @Test public void
    should_be_able_to_parse_all_information() {
        String input =
                "# List of geometric vertices, with (x,y,z[,w]) coordinates, w is optional and defaults to 1.0.\n" +
                "v 0.123 0.234 0.345 1.0\n" +
                "# List of texture coordinates, in (u, v [,w]) coordinates, these will vary between 0 and 1, w is optional and defaults to 0.\n" +
                "vt 0.500 1 0\n" +
                "# List of vertex normals in (x,y,z) form; normals might not be unit vectors.\n" +
                "vn 0.707 0.000 0.707\n";

        WavefrontParser.Result result = parse(input);

        List<Vector> expectedGeometricResults = singletonList(new Vector3(0.123f, 0.234f, 0.345f));
        List<Vector3> expectedTextureCoordinates = singletonList(new Vector3(0.5f, 1f, 0f));
        List<Vector3> expectedVertexNormals = singletonList(new Vector3(0.707f, 0f, 0.707f));

        assertEquals(expectedGeometricResults, result.geometricVertices);
        assertEquals(expectedTextureCoordinates, result.textureVertices);
        assertEquals(expectedVertexNormals, result.vertexNormals);
    }

    private WavefrontParser.Result parse(String input) {
        try {
            return WavefrontParser.parse(input);
        } catch (Exception e) {
            fail();
        }
        return null; // dead code
    }

}
