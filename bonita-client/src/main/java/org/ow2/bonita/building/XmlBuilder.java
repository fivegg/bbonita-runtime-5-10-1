/**
 * Copyright (C) 2010  BonitaSoft S.A.
 * BonitaSoft, 31 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA  02110-1301, USA.
 **/
package org.ow2.bonita.building;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.ow2.bonita.util.Misc;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @author Anthony Birembaut
 *
 */
public class XmlBuilder {
  
  /**
   * Document builder factory
   */
  private DocumentBuilderFactory documentBuilderFactory;
  
  /**
   * Transformer factory
   */
  private TransformerFactory transformerFactory;
  
  /**
   * DOM representation of the XML file to create
   */
  private Document document;
  
  /**
   * the root node
   */
  private Node rootNode;
  
  /**
   * Constructor
   */
  public XmlBuilder(DocumentBuilderFactory documentBuilderFactory, TransformerFactory transformerFactory) {
    this.documentBuilderFactory = documentBuilderFactory;
    this.transformerFactory = transformerFactory;
    try {
      this.transformerFactory.setAttribute("indent-number", Integer.valueOf(2));
    } catch (Exception e) {
      // Nothing to do: indent-number is not supported
    }
  }

  /** 
   * Build a XML form definition file.
   * This is the last method to call once the form has been built.
   * @return a byte array
   * @throws InvalidFormDefinitionException if the generated document is not valid
   * @throws IOException 
   */
  public byte[] done() throws Exception {
    document.appendChild(rootNode);
    Source source = new DOMSource(document);

    Transformer transformer = transformerFactory.newTransformer();
    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
    transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    byte[] xmlContent = null;
    try {
      Result resultat = new StreamResult(new OutputStreamWriter(outputStream, "UTF-8"));
      transformer.transform(source, resultat);

      xmlContent = outputStream.toByteArray();
    } finally {
      outputStream.close();
    }
    return xmlContent;
  }
  
  /**
   * Create the document
   * @return the {@link XmlBuilder}
   * @throws ParserConfigurationException 
   */
  public XmlBuilder createDocument() throws ParserConfigurationException {
    DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
    document = documentBuilder.newDocument();
    document.setXmlVersion("1.0");
    document.setXmlStandalone(true);
    return this;
  }
  
  public Node createRootNode(String name) throws DOMException, IOException, ClassNotFoundException {
    return internalNodeCreation(null, name, null, null);
  }
  
  public Node createRootNode(String name, Map<String, Serializable> attributes) throws DOMException, IOException, ClassNotFoundException {
    return internalNodeCreation(null, name, null, attributes);
  }
  
  public Node createRootNode(String name, Serializable value) throws DOMException, IOException, ClassNotFoundException {
    if (value != null) {
      return internalNodeCreation(null, name, value, null);
    } else {
      return null;
    }
  }
  
  public Node createRootNode(String name, Serializable value, Map<String, Serializable> attributes) throws DOMException, IOException, ClassNotFoundException {
    if (value != null) {
      return internalNodeCreation(null, name, value, attributes);
    } else {
      return null;
    }
  }
  
  public Node createNode(Node parentNode, String name) throws DOMException, IOException, ClassNotFoundException {
    return internalNodeCreation(parentNode, name, null, null);
  }
  
  public Node createNode(Node parentNode, String name, Map<String, Serializable> attributes) throws DOMException, IOException, ClassNotFoundException {
    return internalNodeCreation(parentNode, name, null, attributes);
  }
  
  public Node createNode(Node parentNode, String name, Serializable value) throws DOMException, IOException, ClassNotFoundException {
    if (value != null) {
      return internalNodeCreation(parentNode, name, value, null);
    } else {
      return null;
    }
  }
  
  public Node createNode(Node parentNode, String name, Serializable value, Map<String, Serializable> attributes) throws DOMException, IOException, ClassNotFoundException {
    if (value != null) {
      return internalNodeCreation(parentNode, name, value, attributes);
    } else {
      return null;
    }
  }
  
  public Node internalNodeCreation(Node parentNode, String name, Serializable value, Map<String, Serializable> attributes) throws DOMException, IOException, ClassNotFoundException {
    Element element = document.createElement(name);
    if (value != null) {
      if (value.getClass().isEnum()) {
        element.setTextContent(((Enum<?>)value).name());
      } else if (value instanceof byte[]) {
        element.setTextContent(Misc.fragmentAndBase64Encode((byte[])value));
      } else if (value instanceof Date) {
        element.setTextContent(Long.toString(((Date) value).getTime()));
      } else {
        element.setTextContent(value.toString());
      }
    }
    if (attributes != null) {
      for (Entry<String, Serializable> attribute : attributes.entrySet()) {
        String attributeName = attribute.getKey();
        Serializable attributeValue = attribute.getValue();
        if (attributeValue.getClass().isEnum()) {
          element.setAttribute(attributeName, ((Enum<?>)attributeValue).name());
        } else if (attributeValue instanceof byte[]) {
          element.setAttribute(attributeName, Misc.fragmentAndBase64Encode((byte[])attributeValue));
        } else if (attributeValue instanceof Date) {
          element.setAttribute(attributeName, Long.toString(((Date) attributeValue).getTime()));
        } else {
          element.setAttribute(attributeName, attributeValue.toString());
        }
      }
    }
    if (parentNode == null) {
      rootNode = element;
      return element;
    } else {
      return parentNode.appendChild(element);
    }
  }

}
