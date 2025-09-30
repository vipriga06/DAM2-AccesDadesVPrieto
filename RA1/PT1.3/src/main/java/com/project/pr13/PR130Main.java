package com.project.pr13;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.project.pr13.format.PersonaFormatter;

public class PR130Main {

    private File directoriBase;
    private Document document;
    private XPath xpath;

    // Constructor que rep el directori base on buscar els fitxers XML
    public PR130Main(File directoriBase) throws Exception {
        this.directoriBase = directoriBase;
        XPathFactory xpathFactory = XPathFactory.newInstance();
        xpath = xpathFactory.newXPath();
    }

    // Mètode estàtic per parsejar un fitxer XML i retornar el Document
    public static Document parseXML(File file) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(file);
            doc.getDocumentElement().normalize();
            return doc;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Mètode per processar un fitxer XML donat el seu nom (dins directoriBase)
    public void processarFitxerXML(String nomFitxer) {
        try {
            File fitxer = new File(directoriBase, nomFitxer);
            document = parseXML(fitxer);
            if (document == null) {
                System.out.println("No s'ha pogut carregar el fitxer XML.");
                return;
            }

            // Imprimir capçaleres
            System.out.println(PersonaFormatter.getCapçaleres());

            // Obtenir nodes persona amb XPath
            NodeList persones = (NodeList) xpath.evaluate("/persones/persona", document, XPathConstants.NODESET);

            // Recórrer i imprimir cada persona formatada
            for (int i = 0; i < persones.getLength(); i++) {
                Node personaNode = persones.item(i);
                String nom = xpath.evaluate("nom", personaNode);
                String cognom = xpath.evaluate("cognom", personaNode);
                String edat = xpath.evaluate("edat", personaNode);
                String ciutat = xpath.evaluate("ciutat", personaNode);

                System.out.println(PersonaFormatter.formatarPersona(nom, cognom, edat, ciutat));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Mètode main per proves manuals (opcional)
    public static void main(String[] args) {
        try {
            File directori = new File("."); // Directori actual
            PR130Main app = new PR130Main(directori);
            app.processarFitxerXML("persones.xml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
