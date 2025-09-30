package com.project.pr13;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class PR130Main {
    public static void main(String[] args) {
        try {
            File xmlFile = new File("persones.xml");
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(xmlFile);
            doc.getDocumentElement().normalize();

            NodeList persones = doc.getElementsByTagName("persona");

            // Imprimir cabecera con formato de columnas alineadas
            System.out.printf("%-8s %-14s %-5s %-9s%n", "Nom", "Cognom", "Edat", "Ciutat");
            System.out.printf("%-8s %-14s %-5s %-9s%n", "--------", "--------------", "-----", "---------");

            for (int i = 0; i < persones.getLength(); i++) {
                Node node = persones.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element persona = (Element) node;
                    String nom = persona.getElementsByTagName("nom").item(0).getTextContent();
                    String cognom = persona.getElementsByTagName("cognom").item(0).getTextContent();
                    String edat = persona.getElementsByTagName("edat").item(0).getTextContent();
                    String ciutat = persona.getElementsByTagName("ciutat").item(0).getTextContent();

                    System.out.printf("%-8s %-14s %-5s %-9s%n", nom, cognom, edat, ciutat);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
