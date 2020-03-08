package study;

import lumutator.Configuration;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.*;

/**
 * Class that sets up the configuration before calling MuRa.
 */
public class ConfigurationSetup {

    /**
     * Add PITest as a dependency in the pom file of a project.
     *
     * @param pom The pom file.
     */
    public static void addPITest(File pom) throws IOException, SAXException, ParserConfigurationException, TransformerException, XPathExpressionException {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document doc = documentBuilder.parse(pom);

        XPath xPath = XPathFactory.newInstance().newXPath();
        Node node = (Node) xPath.compile("/project/build/plugins").evaluate(doc, XPathConstants.NODE);

        // It was easier to just add this dependency as a raw string then creating new XML nodes...
        final String pitest = "<plugin>\n" +
                "    <groupId>org.pitest</groupId>\n" +
                "    <artifactId>pitest-maven</artifactId>\n" +
                "    <version>1.4.7</version>\n" +
                "    <configuration>\n" +
                "        <outputFormats>XML</outputFormats>\n" +
                "        <threads>4</threads>\n" +
                "    </configuration>\n" +
                "    <executions>\n" +
                "        <execution>\n" +
                "            <id>pitest</id>\n" +
                "            <goals>\n" +
                "                <goal>mutationCoverage</goal>\n" +
                "            </goals>\n" +
                "        </execution>\n" +
                "    </executions>\n" +
                "</plugin>\n\n";

        Document doc2 = documentBuilder.parse(new ByteArrayInputStream(pitest.getBytes()));

        Node pitestNode = doc.importNode(doc2.getDocumentElement(), true);
        node.appendChild(pitestNode);

        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        Result output = new StreamResult(pom);
        Source input = new DOMSource(doc);

        transformer.transform(input, output);
    }

    /**
     * Add the classpath of the current project version to the configuration.
     *
     * @param config The configuration to be used for MuRa.
     */
    public static void addClassPath(Configuration config) throws IOException, InterruptedException {
        String newClassPath = null;

        // Get the classpath from Maven
        Process process = Runtime.getRuntime().exec("mvn dependency:build-classpath", null, new File(config.get("projectDir")));
        process.waitFor();

        BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        boolean foundClassPath = false;
        while ((line = in.readLine()) != null) {
            if (foundClassPath) {
                newClassPath = line;
                break;
            }
            if (line.startsWith("[INFO] Dependencies classpath:")) {
                foundClassPath = true;
            }
        }
        in.close();

        // Add the classpath to the config for MuRa
        if (foundClassPath) {
            config.set("classPath", newClassPath + ":" + config.get("classPath"));
        } else {
            throw new RuntimeException("Did not find the classpath from Maven");
        }
    }
}
