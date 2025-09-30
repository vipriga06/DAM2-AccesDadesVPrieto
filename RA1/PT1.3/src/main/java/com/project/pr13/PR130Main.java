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

    private File dataDir;
    private Document document;
    private XPath xpath;

    // Constructor que rep el directori base on buscar el fitxer XML
    public PR130Main(File dataDir) throws Exception {
        this.dataDir = dataDir;
        XPathFactory xpathFactory = XPathFactory.newInstance();
        xpath = xpathFactory.newXPath();
    }

    // Mètode principal per executar la classe
    public static void main(String[] args) {
        try {
            // Obtenir el directori de l'usuari i apuntar a la carpeta de dades
            String userDir = System.getProperty("user.dir");
            File dataDir = new File(userDir, "data/pr13");

            // Crear una instància de la classe i processar el fitxer
            PR130Main app = new PR130Main(dataDir);
            app.processarFitxerXML("persones.xml");
        } catch (Exception e) {
            System.err.println("Hi ha hagut un error en inicialitzar l'aplicació.");
            e.printStackTrace();
        }
    }

    // Getter i setter per al directori base
    public File getDataDir() {
        return dataDir;
    }

    public void setDataDir(File dataDir) {
        this.dataDir = dataDir;
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

    // Mètode per processar un fitxer XML donat el seu nom (dins dataDir)
    public void processarFitxerXML(String nomFitxer) {
        try {
            File fitxer = new File(dataDir, nomFitxer);
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
}
