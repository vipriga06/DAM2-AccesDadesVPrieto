package com.project.pr13;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class PR131Main {

    private File dataDir;

    // Constructor que rep el directori base on crear el fitxer
    public PR131Main(File dataDir) {
        this.dataDir = dataDir;
    }

    // Getter del directori base
    public File getDataDir() {
        return dataDir;
    }

    // Setter del directori base
    public void setDataDir(File dataDir) {
        this.dataDir = dataDir;
    }

    // Mètode per crear i escriure el fitxer XML amb el nom passat
    public void processarFitxerXML(String nomFitxer) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.newDocument();

            // Crear arrel <biblioteca>
            Element biblioteca = doc.createElement("biblioteca");
            doc.appendChild(biblioteca);

            // Crear un llibre amb les dades que demana el test
            Element llibre = doc.createElement("llibre");
            llibre.setAttribute("id", "001");

            afegirElement(doc, llibre, "titol", "El viatge dels venturons");
            afegirElement(doc, llibre, "autor", "Joan Pla");
            afegirElement(doc, llibre, "anyPublicacio", "1998");
            afegirElement(doc, llibre, "editorial", "Edicions Mar");
            afegirElement(doc, llibre, "genere", "Aventura");
            afegirElement(doc, llibre, "pagines", "320");
            afegirElement(doc, llibre, "disponible", "true");

            biblioteca.appendChild(llibre);

            // Escriure el document a fitxer dins dataDir
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();

            // Configurar sortida amb indentació
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

            DOMSource source = new DOMSource(doc);
            File fitxerSortida = new File(dataDir, nomFitxer);
            StreamResult result = new StreamResult(fitxerSortida);

            transformer.transform(source, result);

            System.out.println("Fitxer " + nomFitxer + " creat correctament a " + dataDir.getAbsolutePath());

        } catch (ParserConfigurationException | TransformerException e) {
            e.printStackTrace();
        }
    }

    // Mètode auxiliar per afegir un element fill amb text
    private void afegirElement(Document doc, Element pare, String nomElement, String text) {
        Element elem = doc.createElement(nomElement);
        elem.appendChild(doc.createTextNode(text));
        pare.appendChild(elem);
    }

    // Main per proves manuals (opcional)
    public static void main(String[] args) {
        try {
            File directori = new File(".");
            PR131Main app = new PR131Main(directori);
            app.processarFitxerXML("biblioteca.xml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
