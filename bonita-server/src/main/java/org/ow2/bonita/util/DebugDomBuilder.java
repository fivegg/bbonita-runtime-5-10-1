/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the  "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * $Id: DebugDomBuilder.java 1434 2008-07-01 10:32:10Z heiko.braun@jboss.com $
 */
package org.ow2.bonita.util;

import java.util.Stack;
import java.util.Vector;

import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.DefaultHandler;

/**
 * builds the dom model from SAX events, optionally adding the line and column number as attributes to every element.
 */
class DebugDomBuilder extends DefaultHandler implements ContentHandler, LexicalHandler {
  /** Root document */

  public Document document;

  protected String debugNamespace = null;
  protected String lineAttributeName = "line";
  protected String columnAttributeName = null;

  /** Current node */
  protected Node currentNode = null;

  /** The root node */
  protected Node root = null;

  /** The next sibling node */
  protected Node nextSibling = null;

  /** First node of document fragment or null if not a DocumentFragment */
  public DocumentFragment docFrag = null;

  /** Vector of element nodes */
  @SuppressWarnings("rawtypes")
  protected Stack elemStack = new Stack();

  /** Namespace support */
  @SuppressWarnings("rawtypes")
  protected Vector prefixMappings = new Vector();

  /** to obtain the line number information */
  protected Locator locator = null;

  /**
   * Get the root document or DocumentFragment of the DOM being created.
   * 
   * @return The root document or document fragment if not null
   */
  public Node getRootDocument() {
    return null != docFrag ? (Node) docFrag : (Node) document;
  }

  /**
   * Get the root node of the DOM tree.
   */
  public Node getRootNode() {
    return root;
  }

  /**
   * Get the node currently being processed.
   * 
   * @return the current node being processed
   */
  public Node getCurrentNode() {
    return currentNode;
  }

  /**
   * Set the next sibling node, which is where the result nodes should be inserted before.
   * 
   * @param nextSibling
   *          the next sibling node.
   */
  public void setNextSibling(final Node nextSibling) {
    this.nextSibling = nextSibling;
  }

  /**
   * Return the next sibling node.
   * 
   * @return the next sibling node.
   */
  public Node getNextSibling() {
    return nextSibling;
  }

  /**
   * Return null since there is no Writer for this class.
   * 
   * @return null
   */
  public java.io.Writer getWriter() {
    return null;
  }

  /**
   * Append a node to the current container.
   * 
   * @param newNode
   *          New node to append
   */
  protected void append(final Node newNode) throws SAXException {

    final Node currentNode = this.currentNode;

    if (null != currentNode) {
      if (currentNode == root && nextSibling != null) {
        currentNode.insertBefore(newNode, nextSibling);
      } else {
        currentNode.appendChild(newNode);
      }

    } else if (null != docFrag) {
      if (nextSibling != null) {
        docFrag.insertBefore(newNode, nextSibling);
      } else {
        docFrag.appendChild(newNode);
      }
    } else {
      boolean ok = true;
      final short type = newNode.getNodeType();

      if (type == Node.TEXT_NODE) {
        final String data = newNode.getNodeValue();

        if (null != data && data.trim().length() > 0) {
          final String message = ExceptionManager.getInstance().getFullMessage("bp_DDB_1");
          throw new SAXException(message);
        }

        ok = false;
      } else if (type == Node.ELEMENT_NODE) {
        if (document.getDocumentElement() != null) {
          ok = false;
          final String message = ExceptionManager.getInstance().getFullMessage("bp_DDB_2");
          throw new SAXException(message);
        }
      }

      if (ok) {
        if (nextSibling != null) {
          document.insertBefore(newNode, nextSibling);
        } else {
          document.appendChild(newNode);
        }
      }
    }
  }

  /**
   * Receive an object for locating the origin of SAX document events.
   * 
   * <p>
   * SAX parsers are strongly encouraged (though not absolutely required) to supply a locator: if it does so, it must
   * supply the locator to the application by invoking this method before invoking any of the other methods in the
   * ContentHandler interface.
   * </p>
   * 
   * <p>
   * The locator allows the application to determine the end position of any document-related event, even if the parser
   * is not reporting an error. Typically, the application will use this information for reporting its own errors (such
   * as character content that does not match an application's business rules). The information returned by the locator
   * is probably not sufficient for use with a search engine.
   * </p>
   * 
   * <p>
   * Note that the locator will return correct information only during the invocation of the events in this interface.
   * The application should not attempt to use it at any other time.
   * </p>
   * 
   * @param locator
   *          An object that can return the location of any SAX document event.
   * @see Locator
   */
  @Override
  public void setDocumentLocator(final Locator locator) {
    this.locator = locator;
    // No action for the moment.
  }

  /**
   * Receive notification of the beginning of a document.
   * 
   * <p>
   * The SAX parser will invoke this method only once, before any other methods in this interface or in DTDHandler
   * (except for setDocumentLocator).
   * </p>
   */
  @Override
  public void startDocument() throws SAXException {
    // No action for the moment.
  }

  /**
   * Receive notification of the end of a document.
   * 
   * <p>
   * The SAX parser will invoke this method only once, and it will be the last method invoked during the parse. The
   * parser shall not invoke this method until it has either abandoned parsing (because of an unrecoverable error) or
   * reached the end of input.
   * </p>
   */
  @Override
  public void endDocument() throws SAXException {
    // No action for the moment.
  }

  /**
   * Receive notification of the beginning of an element.
   * 
   * <p>
   * The Parser will invoke this method at the beginning of every element in the XML document; there will be a
   * corresponding endElement() event for every startElement() event (even when the element is empty). All of the
   * element's content will be reported, in order, before the corresponding endElement() event.
   * </p>
   * 
   * <p>
   * If the element name has a namespace prefix, the prefix will still be attached. Note that the attribute list
   * provided will contain only attributes with explicit values (specified or defaulted): #IMPLIED attributes will be
   * omitted.
   * </p>
   * 
   * 
   * @param ns
   *          The namespace of the node
   * @param localName
   *          The local part of the qualified name
   * @param name
   *          The element name.
   * @param atts
   *          The attributes attached to the element, if any.
   * @see #endElement
   * @see Attributes
   */
  @Override
  @SuppressWarnings("unchecked")
  public void startElement(final String ns, final String localName, final String name, final Attributes atts)
      throws SAXException {

    Element elem;

    // Note that the namespace-aware call must be used to correctly
    // construct a Level 2 DOM, even for non-namespaced nodes.
    if (null == ns || ns.length() == 0) {
      elem = document.createElementNS(null, name);
    } else {
      elem = document.createElementNS(ns, name);
    }
    append(elem);
    try {
      final int nAtts = atts.getLength();

      if (0 != nAtts) {
        for (int i = 0; i < nAtts; i++) {

          // First handle a possible ID attribute
          if (atts.getType(i).equalsIgnoreCase("ID")) {
            setIDAttribute(atts.getValue(i), elem);
          }
          String attrNS = atts.getURI(i);
          if ("".equals(attrNS)) {
            attrNS = null; // DOM represents no-namespace as null
          }
          // Crimson won't let us set an xmlns: attribute on the DOM.
          final String attrQName = atts.getQName(i);

          // In SAX, xmlns[:] attributes have an empty namespace, while in DOM
          // they
          // should have the xmlns namespace
          if (attrQName.startsWith("xmlns:") || "xmlns".equals(attrQName)) {
            attrNS = "http://www.w3.org/2000/xmlns/";
          }

          // ALWAYS use the DOM Level 2 call!
          elem.setAttributeNS(attrNS, attrQName, atts.getValue(i));
        }
      }

      if (locator != null) {
        final int lineNumber = locator.getLineNumber();
        final int columnNumber = locator.getColumnNumber();

        if (debugNamespace == null) {
          if (lineAttributeName != null) {
            elem.setAttribute(lineAttributeName, Integer.toString(lineNumber));
          }
          if (columnAttributeName != null) {
            elem.setAttribute(columnAttributeName, Integer.toString(columnNumber));
          }

        } else {
          if (lineAttributeName != null) {
            elem.setAttributeNS(debugNamespace, lineAttributeName, Integer.toString(lineNumber));
          }
          if (columnAttributeName != null) {
            elem.setAttributeNS(debugNamespace, columnAttributeName, Integer.toString(columnNumber));
          }

        }
      }

      /*
       * Adding namespace nodes to the DOM tree;
       */
      final int nDecls = prefixMappings.size();
      String prefix, declURL;
      for (int i = 0; i < nDecls; i += 2) {
        prefix = (String) prefixMappings.elementAt(i);
        if (prefix == null) {
          continue;
        }
        declURL = (String) prefixMappings.elementAt(i + 1);
        elem.setAttributeNS("http://www.w3.org/2000/xmlns/", prefix, declURL);
      }
      prefixMappings.clear();
      elemStack.push(elem);
      currentNode = elem;
    } catch (final Exception de) {
      throw new SAXException(de);
    }

  }

  /**
   * 
   * 
   * 
   * Receive notification of the end of an element.
   * 
   * <p>
   * The SAX parser will invoke this method at the end of every element in the XML document; there will be a
   * corresponding startElement() event for every endElement() event (even when the element is empty).
   * </p>
   * 
   * <p>
   * If the element name has a namespace prefix, the prefix will still be attached to the name.
   * </p>
   * 
   * 
   * @param ns
   *          the namespace of the element
   * @param localName
   *          The local part of the qualified name of the element
   * @param name
   *          The element name
   */
  @Override
  public void endElement(final String ns, final String localName, final String name) throws SAXException {
    elemStack.pop();
    currentNode = elemStack.isEmpty() ? null : (Node) elemStack.peek();
  }

  /**
   * Set an ID string to node association in the ID table.
   * 
   * @param id
   *          The ID string.
   * @param elem
   *          The associated ID.
   */
  public void setIDAttribute(final String id, final Element elem) {

    // Do nothing. This method is meant to be overiden.
  }

  /**
   * Receive notification of character data.
   * 
   * <p>
   * The Parser will call this method to report each chunk of character data. SAX parsers may return all contiguous
   * character data in a single chunk, or they may split it into several chunks; however, all of the characters in any
   * single event must come from the same external entity, so that the Locator provides useful information.
   * </p>
   * 
   * <p>
   * The application must not attempt to read from the array outside of the specified range.
   * </p>
   * 
   * <p>
   * Note that some parsers will report whitespace using the ignorableWhitespace() method rather than this one
   * (validating parsers must do so).
   * </p>
   * 
   * @param ch
   *          The characters from the XML document.
   * @param start
   *          The start position in the array.
   * @param length
   *          The number of characters to read from the array.
   * @see #ignorableWhitespace
   * @see Locator
   */
  @Override
  public void characters(final char ch[], final int start, final int length) throws SAXException {
    if (isOutsideDocElem() && isWhiteSpace(ch, start, length)) {
      return; // avoid DOM006 Hierarchy request error
    }

    if (inCData) {
      cdata(ch, start, length);

      return;
    }

    final String s = new String(ch, start, length);
    Node childNode;
    childNode = currentNode != null ? currentNode.getLastChild() : null;
    if (childNode != null && childNode.getNodeType() == Node.TEXT_NODE) {
      ((Text) childNode).appendData(s);
    } else {
      final Text text = document.createTextNode(s);
      append(text);
    }
  }

  /**
   * If available, when the disable-output-escaping attribute is used, output raw text without escaping. A PI will be
   * inserted in front of the node with the name "lotusxsl-next-is-raw" and a value of "formatter-to-dom".
   * 
   * @param ch
   *          Array containing the characters
   * @param start
   *          Index to start of characters in the array
   * @param length
   *          Number of characters in the array
   */
  public void charactersRaw(final char ch[], final int start, final int length) throws SAXException {
    if (isOutsideDocElem() && isWhiteSpace(ch, start, length)) {
      return; // avoid DOM006 Hierarchy request error
    }

    final String s = new String(ch, start, length);

    append(document.createProcessingInstruction("xslt-next-is-raw", "formatter-to-dom"));
    append(document.createTextNode(s));
  }

  /**
   * Report the beginning of an entity.
   * 
   * The start and end of the document entity are not reported. The start and end of the external DTD subset are
   * reported using the pseudo-name "[dtd]". All other events must be properly nested within start/end entity events.
   * 
   * @param name
   *          The name of the entity. If it is a parameter entity, the name will begin with '%'.
   * @see #endEntity
   * @see ext.DeclHandler#internalEntityDecl
   * @see ext.DeclHandler#externalEntityDecl
   */
  @Override
  public void startEntity(final String name) throws SAXException {

    // Almost certainly the wrong behavior...
    // entityReference(name);
  }

  /**
   * Report the end of an entity.
   * 
   * @param name
   *          The name of the entity that is ending.
   * @see #startEntity
   */
  @Override
  public void endEntity(final String name) throws SAXException {
  }

  /**
   * Receive notivication of a entityReference.
   * 
   * @param name
   *          name of the entity reference
   */
  public void entityReference(final String name) throws SAXException {
    append(document.createEntityReference(name));
  }

  /**
   * Receive notification of ignorable whitespace in element content.
   * 
   * <p>
   * Validating Parsers must use this method to report each chunk of ignorable whitespace (see the W3C XML 1.0
   * recommendation, section 2.10): non-validating parsers may also use this method if they are capable of parsing and
   * using content models.
   * </p>
   * 
   * <p>
   * SAX parsers may return all contiguous whitespace in a single chunk, or they may split it into several chunks;
   * however, all of the characters in any single event must come from the same external entity, so that the Locator
   * provides useful information.
   * </p>
   * 
   * <p>
   * The application must not attempt to read from the array outside of the specified range.
   * </p>
   * 
   * @param ch
   *          The characters from the XML document.
   * @param start
   *          The start position in the array.
   * @param length
   *          The number of characters to read from the array.
   * @see #characters
   */
  @Override
  public void ignorableWhitespace(final char ch[], final int start, final int length) throws SAXException {
    if (isOutsideDocElem()) {
      return; // avoid DOM006 Hierarchy request error
    }

    final String s = new String(ch, start, length);

    append(document.createTextNode(s));
  }

  /**
   * Tell if the current node is outside the document element.
   * 
   * @return true if the current node is outside the document element.
   */
  private boolean isOutsideDocElem() {
    return null == docFrag && elemStack.size() == 0
        && (null == currentNode || currentNode.getNodeType() == Node.DOCUMENT_NODE);
  }

  /**
   * Receive notification of a processing instruction.
   * 
   * <p>
   * The Parser will invoke this method once for each processing instruction found: note that processing instructions
   * may occur before or after the main document element.
   * </p>
   * 
   * <p>
   * A SAX parser should never report an XML declaration (XML 1.0, section 2.8) or a text declaration (XML 1.0, section
   * 4.3.1) using this method.
   * </p>
   * 
   * @param target
   *          The processing instruction target.
   * @param data
   *          The processing instruction data, or null if none was supplied.
   */
  @Override
  public void processingInstruction(final String target, final String data) throws SAXException {
    append(document.createProcessingInstruction(target, data));
  }

  /**
   * Report an XML comment anywhere in the document.
   * 
   * This callback will be used for comments inside or outside the document element, including comments in the external
   * DTD subset (if read).
   * 
   * @param ch
   *          An array holding the characters in the comment.
   * @param start
   *          The starting position in the array.
   * @param length
   *          The number of characters to use from the array.
   */
  @Override
  public void comment(final char ch[], final int start, final int length) throws SAXException {
    append(document.createComment(new String(ch, start, length)));
  }

  /** Flag indicating that we are processing a CData section */
  protected boolean inCData = false;

  /**
   * Report the start of a CDATA section.
   * 
   * @see #endCDATA
   */
  @Override
  public void startCDATA() throws SAXException {
    inCData = true;
    append(document.createCDATASection(""));
  }

  /**
   * Report the end of a CDATA section.
   * 
   * @see #startCDATA
   */
  @Override
  public void endCDATA() throws SAXException {
    inCData = false;
  }

  /**
   * Receive notification of cdata.
   * 
   * <p>
   * The Parser will call this method to report each chunk of character data. SAX parsers may return all contiguous
   * character data in a single chunk, or they may split it into several chunks; however, all of the characters in any
   * single event must come from the same external entity, so that the Locator provides useful information.
   * </p>
   * 
   * <p>
   * The application must not attempt to read from the array outside of the specified range.
   * </p>
   * 
   * <p>
   * Note that some parsers will report whitespace using the ignorableWhitespace() method rather than this one
   * (validating parsers must do so).
   * </p>
   * 
   * @param ch
   *          The characters from the XML document.
   * @param start
   *          The start position in the array.
   * @param length
   *          The number of characters to read from the array.
   * @see #ignorableWhitespace
   * @see Locator
   */
  public void cdata(final char ch[], final int start, final int length) throws SAXException {
    if (isOutsideDocElem() && isWhiteSpace(ch, start, length)) {
      return; // avoid DOM006 Hierarchy request error
    }

    final String s = new String(ch, start, length);

    final CDATASection section = (CDATASection) currentNode.getLastChild();
    section.appendData(s);
  }

  /**
   * Report the start of DTD declarations, if any.
   * 
   * Any declarations are assumed to be in the internal subset unless otherwise indicated.
   * 
   * @param name
   *          The document type name.
   * @param publicId
   *          The declared public identifier for the external DTD subset, or null if none was declared.
   * @param systemId
   *          The declared system identifier for the external DTD subset, or null if none was declared.
   * @see #endDTD
   * @see #startEntity
   */
  @Override
  public void startDTD(final String name, final String publicId, final String systemId) throws SAXException {

    // Do nothing for now.
  }

  /**
   * Report the end of DTD declarations.
   * 
   * @see #startDTD
   */
  @Override
  public void endDTD() throws SAXException {

    // Do nothing for now.
  }

  /**
   * Begin the scope of a prefix-URI Namespace mapping.
   * 
   * <p>
   * The information from this event is not necessary for normal Namespace processing: the SAX XML reader will
   * automatically replace prefixes for element and attribute names when the http://xml.org/sax/features/namespaces
   * feature is true (the default).
   * </p>
   * 
   * <p>
   * There are cases, however, when applications need to use prefixes in character data or in attribute values, where
   * they cannot safely be expanded automatically; the start/endPrefixMapping event supplies the information to the
   * application to expand prefixes in those contexts itself, if necessary.
   * </p>
   * 
   * <p>
   * Note that start/endPrefixMapping events are not guaranteed to be properly nested relative to each-other: all
   * startPrefixMapping events will occur before the corresponding startElement event, and all endPrefixMapping events
   * will occur after the corresponding endElement event, but their order is not guaranteed.
   * </p>
   * 
   * @param prefix
   *          The Namespace prefix being declared.
   * @param uri
   *          The Namespace URI the prefix is mapped to.
   * @see #endPrefixMapping
   * @see #startElement
   */
  @Override
  @SuppressWarnings("unchecked")
  public void startPrefixMapping(String prefix, final String uri) throws SAXException {
    if (null == prefix || prefix.equals("")) {
      prefix = "xmlns";
    } else {
      prefix = "xmlns:" + prefix;
    }
    prefixMappings.addElement(prefix);
    prefixMappings.addElement(uri);
  }

  /**
   * End the scope of a prefix-URI mapping.
   * 
   * <p>
   * See startPrefixMapping for details. This event will always occur after the corresponding endElement event, but the
   * order of endPrefixMapping events is not otherwise guaranteed.
   * </p>
   * 
   * @param prefix
   *          The prefix that was being mapping.
   * @see #startPrefixMapping
   * @see #endElement
   */
  @Override
  public void endPrefixMapping(final String prefix) throws SAXException {
  }

  /**
   * Receive notification of a skipped entity.
   * 
   * <p>
   * The Parser will invoke this method once for each entity skipped. Non-validating processors may skip entities if
   * they have not seen the declarations (because, for example, the entity was declared in an external DTD subset). All
   * processors may skip external entities, depending on the values of the
   * http://xml.org/sax/features/external-general-entities and the
   * http://xml.org/sax/features/external-parameter-entities properties.
   * </p>
   * 
   * @param name
   *          The name of the skipped entity. If it is a parameter entity, the name will begin with '%'.
   */
  @Override
  public void skippedEntity(final String name) throws SAXException {
  }

  /**
   * Returns whether the specified <var>ch</var> conforms to the XML 1.0 definition of whitespace. Refer to <A
   * href="http://www.w3.org/TR/1998/REC-xml-19980210#NT-S"> the definition of <CODE>S</CODE></A> for details.
   * 
   * @param ch
   *          Character to check as XML whitespace.
   * @return =true if <var>ch</var> is XML whitespace; otherwise =false.
   */
  public static boolean isWhiteSpace(final char ch) {
    return ch == 0x20 || ch == 0x09 || ch == 0xD || ch == 0xA;
  }

  /**
   * Tell if the string is whitespace.
   * 
   * @param ch
   *          Character array to check as XML whitespace.
   * @param start
   *          Start index of characters in the array
   * @param length
   *          Number of characters in the array
   * @return True if the characters in the array are XML whitespace; otherwise, false.
   */
  public static boolean isWhiteSpace(final char ch[], final int start, final int length) {

    final int end = start + length;

    for (int s = start; s < end; s++) {
      if (!isWhiteSpace(ch[s])) {
        return false;
      }
    }

    return true;
  }

  public void setDebugNamespace(final String debugNamespace) {
    this.debugNamespace = debugNamespace;
  }

  public void setLineAttributeName(final String lineAttributeName) {
    this.lineAttributeName = lineAttributeName;
  }

  public void setColumnAttributeName(final String columnAttributeName) {
    this.columnAttributeName = columnAttributeName;
  }

  public String getDebugNamespace() {
    return debugNamespace;
  }

  public String getLineAttributeName() {
    return lineAttributeName;
  }

  public String getColumnAttributeName() {
    return columnAttributeName;
  }

  public Document getDocument() {
    return document;
  }

  public void setDocument(final Document document) {
    this.document = document;
  }
}
