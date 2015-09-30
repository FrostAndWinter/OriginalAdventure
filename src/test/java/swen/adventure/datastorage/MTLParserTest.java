package swen.adventure.datastorage;

import org.junit.Test;
import swen.adventure.Utilities;
import swen.adventure.rendering.Material;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 30/09/15.
 */
public class MTLParserTest {

    @Test
    public void testLoadingRocketMaterialFile() throws IOException {
        File file = new File(Utilities.pathForResource("rocket", "mtl"));
        Map<String, Material> result = MTLParser.parse(file);
        System.out.println(result);
    }
}
