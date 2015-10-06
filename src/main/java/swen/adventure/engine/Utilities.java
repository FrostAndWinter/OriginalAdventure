package swen.adventure.engine;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import swen.adventure.engine.scenegraph.SceneNode;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 19/09/15.
 */
public class Utilities {

    public static final boolean isHeadlessMode = Boolean.getBoolean("swen.adventure.HeadlessMode");

    private static final String BasePath;
    static {
        URI path = null;
        try {
            path = Utilities.class.getProtectionDomain().getCodeSource().getLocation().toURI();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        BasePath = new File(path).getPath().replaceFirst("classes" + File.separator + "main", "resources" + File.separator + "main");
    }

    public static String pathForResource(String resourceName, String extension) {
        return Utilities.pathForResource(null, resourceName, extension);
    }

    public static String pathForResource(String directory, String resourceName, String extension) {
        String modifiedDirectory = directory != null ? directory : "";
        if (!modifiedDirectory.startsWith(File.separator)) {
            modifiedDirectory = File.separator + modifiedDirectory;
        }
        if (!modifiedDirectory.endsWith(File.separator)) {
            modifiedDirectory = modifiedDirectory + File.separator;
        }

        String pathString = BasePath + modifiedDirectory + resourceName + (extension == null ? "" : "." + extension);
        return pathString.replaceAll("[\\r\\n]", ""); //remove any newline characters.
    }

    public static List<String> readLinesFromFile(String filePath) throws IOException {
        return Files.readAllLines(new File(filePath).toPath());
    }

    public static String readFile(String filePath) throws IOException {
        List<String> lines = Utilities.readLinesFromFile(filePath);
        return lines.stream().collect(Collectors.joining("\n"));
    }

    public static InputStream stringToInputStream(String input){
        return new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
    }

    public static InputStream fileToInputStream(File inputFile) throws FileNotFoundException {
        return new FileInputStream(inputFile);
    }

    public static float[] toPrimitiveArray(List<Float> list) {
        float[] out = new float[list.size()];
        for (int i = 0; i < list.size(); i++) {
            out[i] = list.get(i);
        }
        return out;
    }

    public static Document loadExistingXmlDocument(String input){
        return loadExistingXmlDocument(stringToInputStream(input));
    }

    public static Document loadExistingXmlDocument(File inputFile) throws FileNotFoundException {
        return loadExistingXmlDocument(fileToInputStream(inputFile));
    }

    public static Document loadExistingXmlDocument(InputStream is) {
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            return docBuilder.parse(is);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Document createDocument() {
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            return docBuilder.newDocument();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    public static void writeOutDocument(Document doc, OutputStream os) {
        writeOutDocument(doc, os, false);
    }

    public static void writeOutDocument(Document doc, OutputStream os, boolean neat) {
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();

            if(neat)
                transformerFactory.setAttribute("indent-number", 2);

            Transformer transformer = transformerFactory.newTransformer();
            if(neat)
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");

            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(os);
            transformer.transform(source, result);
        } catch (TransformerException e) {
            throw new RuntimeException(e);
        }
    }
}
