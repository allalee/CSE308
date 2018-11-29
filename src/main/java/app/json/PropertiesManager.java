package app.json;

import app.enums.Property;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Yixiu Liu on 11/29/2018.
 */
public class PropertiesManager{

    private final static String ERROR_FAIL_INIT = "Failed to initialize manager";
    private final static String ERROR_DUPLICATE_TAG = "Duplicate name found";
    private final static String VALUE_ATTR = "value";
    private static Map<String, String> propertyMap;
    private final static String IGNORE_TAG = "root";

    public static void init(String url){
        propertyMap = new HashMap<>();
        try {
            File file = new File(url);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = null;
            builder = factory.newDocumentBuilder();
            Document doc = builder.parse(file);

            // normalize, no newline or spaces considered
            doc.getDocumentElement().normalize();

            NodeList allNodes = doc.getElementsByTagName("*");
            for(int i=0; i < allNodes.getLength(); i++){
                Element element = (Element) allNodes.item(i);
                String tagName = element.getTagName();
                if(IGNORE_TAG.equals(tagName))
                    continue;

                if(propertyMap.containsKey(tagName) ){
                    throw new Exception(ERROR_DUPLICATE_TAG);
                }
                propertyMap.put(tagName, element.getAttribute(VALUE_ATTR));
            }

        }catch (ParserConfigurationException e) {
            System.out.println(ERROR_FAIL_INIT);
            e.printStackTrace();
        }catch (IOException e) {
            System.out.println(ERROR_FAIL_INIT);
            e.printStackTrace();
        } catch (SAXException e) {
            System.out.println(ERROR_FAIL_INIT);
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println(VALUE_ATTR);
            e.printStackTrace();
        }
    }

    public static final String get(Property prop){
        return propertyMap.get(prop.toString());
    }
}
