package com.example.madt_lab5;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class Parser {


    //Parses the XML input stream to extract currency rates based on USD.

    public static Map<String, String> getCurrencyRatesBaseUsd(InputStream stream) throws IOException {
        Map<String, String> currencyRates = new HashMap<>(); // Initialize a map to hold currency rates

        try {
            // Initialize XML parser
            DocumentBuilderFactory xmlDocFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder xmlDocBuilder = xmlDocFactory.newDocumentBuilder();
            Document doc = xmlDocBuilder.parse(stream); // Parse the XML input stream

            // Extract currency rates from the XML
            NodeList rateNodes = doc.getElementsByTagName("item");
            for (int i = 0; i < rateNodes.getLength(); ++i) {
                Element rateNode = (Element) rateNodes.item(i);

                // Extract targetCurrency and exchangeRate with null checks
                String currencyName = getElementValue(rateNode, "targetCurrency");
                String rate = getElementValue(rateNode, "exchangeRate");

                if (currencyName != null && rate != null) { // Only add if both values are present
                    currencyRates.put(currencyName, rate); // Store in the map
                }
            }
        } catch (ParserConfigurationException | SAXException e) {
            e.printStackTrace(); // Print stack trace in case of XML parsing errors
        } finally {
            if (stream != null) {
                stream.close(); // Ensure InputStream is closed to avoid resource leaks
            }
        }

        return currencyRates; // Return the map of currency rates
    }


     //Helper method to safely get the value of an element by tag name.

    private static String getElementValue(Element parentElement, String tagName) {
        NodeList nodes = parentElement.getElementsByTagName(tagName);
        if (nodes.getLength() > 0 && nodes.item(0).getFirstChild() != null) {
            return nodes.item(0).getFirstChild().getNodeValue(); // Return the value of the first child node
        }
        return null; // Return null if not found or no child node exists
    }
}