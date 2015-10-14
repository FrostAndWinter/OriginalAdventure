/* Contributor List  */ 
 /* Liam O'Niell (oneilliam) (300312734) */ 
 /* Liam O'Niell (oneilliam) (300312734) */ 
 /* Joseph Bennett (bennetjose) (300319773) */ 
 /* Daniel Braithwaite (braithdani) (300313770) */ 
 package swen.adventure.engine.datastorage;

import org.w3c.dom.*;
import swen.adventure.engine.Utilities;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Liam O'Neill, Student ID 300312734, on 18/09/15.
 *
 * A BundleSerializer instance manages converting BundleSerializable objects to their xml representation and back.
 */
public class BundleSerializer {

    /**
     * Serialize a bundleSerializable object into a string containing its xml representation.
     *
     * @param bundleSerializable object to serialize.
     * @return the xml representation of the given object.
     */
    public String toXml(BundleSerializable bundleSerializable) {
        BundleObject bundle = bundleSerializable.toBundle();

        Document document = Utilities.createDocument();
        writeBundleObject(document, document, bundle);

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        Utilities.writeOutDocument(document, os);

        return new String(os.toByteArray(), StandardCharsets.UTF_8);
    }

    /**
     * Convert the bundleSerializable object into a file containing its xml representation.
     *
     * @param bundleSerializable object to serialize.
     * @param file file to save into.
     * @throws FileNotFoundException if the file doesn't exist.
     */
    public void toXmlFile(BundleSerializable bundleSerializable, File file) throws FileNotFoundException {
        String xml = toXml(bundleSerializable);
        try (PrintStream ps = new PrintStream(file)) {
            ps.println(xml);
            ps.flush();
        }
    }

    /**
     * Deserialize a bundleObject by parsing its xml representation and calling the static factory method "createFromBundle".
     *
     * @param file file to read from.
     * @param class0 type of the object.
     * @return the deserialized object.
     * @throws IOException if the file doesn't exist.
     */
    public <T> T loadObjectFromBundle(File file, Class<T> class0) throws IOException {
        BundleObject bundleObject = fromXml(file);
        try {

            Method factory = class0.getDeclaredMethod("createFromBundle", BundleObject.class);
            factory.setAccessible(true);
            Object result = factory.invoke(null, bundleObject);
            return class0.cast(result);

        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Can't invoke static factory method \"createFromBundle\"", e);
        } catch (ClassCastException e) {
            throw new RuntimeException("Static factory method \"createFromBundle\" should return type " + class0.getSimpleName(), e);
        }
    }

    /**
     * Deserialize xml into a bundle object.
     *
     * @param xml string containing a xml representation of a bundle object.
     * @return the deserialized bundle object.
     */
    public BundleObject fromXml(String xml) {
        InputStream is = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
        return fromXml(is);
    }

    /**
     * Deserialize xml into a bundle object.
     *
     * @param file file containing a xml representation of a bundle object.
     * @return the deserialized bundle object.
     */
    public BundleObject fromXml(File file) throws FileNotFoundException {
        InputStream is = new FileInputStream(file);
        return fromXml(is);
    }

    /**
     * Deserialize xml into a bundle object.
     *
     * @param inputStream input stream containing a xml representation of a bundle object.
     * @return the deserialized bundle object.
     */
    public BundleObject fromXml(InputStream inputStream) {
        Document document = Utilities.loadExistingXmlDocument(inputStream);
        document.getDocumentElement().normalize();
        return readBundleObject(document.getFirstChild());
    }

    /**
     * Construct a bundle object by recursively parsing a xml node.
     *
     * @param node xml node
     * @return the deserialized bundle object
     */
    private static BundleObject readBundleObject(Node node) {
        Map<String, BundleProperty> storedValues = new HashMap<>();

        NodeList nList = node.getChildNodes();
        for (int i = 0; i < nList.getLength(); i++) {
            BundleProperty property = loadProperty(nList.item(i));
            storedValues.put(property.name, property);
        }

        return new BundleObject(storedValues);
    }

    /**
     * Deserialize a single property into a bundle property.
     * Note: This method may involve recursively parsing other properties if this property is a bundle object / array.
     *
     * @param node the xml node to parse.
     * @return the deserialized property.
     */
    private static BundleProperty loadProperty(Node node) {
        NamedNodeMap propertyNodeMap = node.getAttributes();
        String name = propertyNodeMap.getNamedItem("name").getNodeValue();

        Node valueNode = node.getChildNodes().item(0);
        NamedNodeMap valueTypeNodeMap = valueNode.getAttributes();
        String type = valueTypeNodeMap.getNamedItem("type").getNodeValue();

        Node value = valueNode.getChildNodes().item(0);
        try {
            Class<?> class0 = Class.forName(type);

            Object instance;
            if(class0 == BundleObject.class)
                instance = readBundleObject(value);
            else if(class0 == BundleArray.class)
                instance = readBundleArray(value);
            else
                instance = ParserManager.convertFromString(value.getTextContent(), class0);

            return new BundleProperty(name, instance, class0);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Construct a bundle array by recursively parsing a xml node.
     *
     * @param node xml node
     * @return the deserialized bundle array
     */
    private static BundleArray readBundleArray(Node node) {
        NodeList nList = node.getChildNodes();
        List<BundleProperty> storedValues = new ArrayList<>();
        for (int i = 0; i < nList.getLength(); i++) {
            BundleProperty property = loadProperty(nList.item(i));
            storedValues.add(property);
        }
        return new BundleArray(storedValues);
    }

    /**
     * Write out the bundleObject into the given xml document.
     *
     * @param parent the parent xml node.
     * @param document the xml document.
     * @param bundleObject the bundle object to serialize.
     */
    private static void writeBundleObject(Node parent, Document document, BundleObject bundleObject){
        Element root = document.createElement("BundleObject");
        parent.appendChild(root);

        bundleObject.getProperties()
                .forEach(property -> writeProperty(property, document, root));
    }

    /**
     * Write out the bundle array into the given xml document.
     *
     * @param parent the parent xml node.
     * @param document the xml document.
     * @param bundleArray the bundle array to serialize.
     */
    private static void writeBundleArray(Node parent, Document document, BundleArray bundleArray) {
        Element root = document.createElement("BundleArray");
        parent.appendChild(root);

        bundleArray.getProperties()
                .forEach(property -> writeProperty(property, document, root));
    }

    /**
     * Write the xml representation of a property into the given xml document.
     * This method will create a new xml node which will be parented by the parent element passed in.
     *
     * @param property the property instance to serialize.
     * @param document the document to write into
     * @param parent the node which will parent all nodes created by this method.
     */
    private static void writeProperty(BundleProperty property, Document document, Element parent){
        Element propertyElem = document.createElement("property");
        parent.appendChild(propertyElem);
        propertyElem.setAttribute("name", property.name);

        Element valueElem = document.createElement("value");
        propertyElem.appendChild(valueElem);
        valueElem.setAttribute("type", property.class0.getCanonicalName());

        if(property.class0 == BundleObject.class) {
            writeBundleObject(valueElem, document, (BundleObject)property.value);
        } else if(property.class0 == BundleArray.class) {
            writeBundleArray(valueElem, document, (BundleArray) property.value);
        } else {
            // this unchecked cast is safe by the guarantee given by the BundleProperty class that property.value.getClass() == property.class0.
            @SuppressWarnings("unchecked")
            Node valueText = document.createTextNode(ParserManager.convertToString(property.value, (Class) property.class0));
            valueElem.appendChild(valueText);
        }
    }

}