package swen.adventure;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 19/09/15.
 */
public class Utilities {

    public static String pathForResource(String resourceName, String extension) {
        URI path = null;
        try {
            path = Utilities.class.getProtectionDomain().getCodeSource().getLocation().toURI();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        String pathString = new File(path).getPath().replaceFirst("classes" + File.separator + "main", "resources" + File.separator + "main");
        return pathString + File.separator + resourceName + "." + extension;
    }

    public static List<String> readLinesFromFile(String filePath) throws IOException {
        return Files.readAllLines(new File(filePath).toPath());
    }

    public static String readFile(String filePath) throws IOException {
        List<String> lines = Utilities.readLinesFromFile(filePath);
        return lines.stream().collect(Collectors.joining("\n"));
    }

}
