package swen.adventure;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

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
        pathString = pathString + File.separator + resourceName + (extension == null ? "" : "." + extension);
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
