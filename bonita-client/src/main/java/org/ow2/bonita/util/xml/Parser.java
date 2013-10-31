/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.ow2.bonita.util.xml;

import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.ow2.bonita.util.BonitaConstants;
import org.ow2.bonita.util.stream.StreamSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

/**
 * makes typical usage of JAXP more convenient, adds a binding framework, entity
 * resolution and error handling.
 * 
 * <h2>Purpose</h2>
 * <p>
 * This is a base parser for the common pattern where first JAXP is used to
 * parse xml into a Document Object Model (DOM), and then, this DOM is examined
 * to build a domain model object. The main purpose of this parser is to serve
 * as a base class for implementing such parsers and to provide a more
 * convenient API for working with JAXP.
 * </p>
 * 
 * <p>
 * A {@link Parser} is a thread safe object. For each parse operation, a new
 * {@link Parse} object is created with method {@link #createParse()}. Then the
 * parse object is used to specify the input source, execute the parse operation
 * and extract the results.
 * </p>
 * 
 * <p>
 * {@link Binding}s capture parsing of a certain element type. This way, the
 * parser becomes more modular and customizable.
 * </p>
 * 
 * <p>
 * {@link Entity Entities} are schema's that specify the grammar of the XML files
 * that are parsed by the parser.
 * </p>
 * 
 * <h2>API Usage</h2>
 * <p>
 * Parsers can be customized by inheritance (that will be covered below), but a
 * parser can also be used as is:
 * </p>
 * 
 * <pre>
 * &lt;i&gt; 1 &lt;/i&gt;|   static Parser parser = new Parser();
 * i&gt; 2 &lt;/i&gt;| 
 * i&gt; 3 &lt;/i&gt;|   void someMethod() {
 * i&gt; 4 &lt;/i&gt;|     MyDomainObject mdo = (MyDomainObject) parser
 * i&gt; 5 &lt;/i&gt;|             .createParse()
 * i&gt; 6 &lt;/i&gt;|             .setString(myXmlString)
 * i&gt; 7 &lt;/i&gt;|             .execute()
 * i&gt; 8 &lt;/i&gt;|             .checkProblems()
 * i&gt; 9 &lt;/i&gt;|             .getDocumentObject();
 * i&gt;10 &lt;/i&gt;|   }
 * </pre>
 * 
 * <p>
 * <b>line 1</b> shows that a single parser can be used for all threads as the
 * parser is maintained in a static member field.
 * </p>
 * 
 * <p>
 * <b>line 5</b> shows that a new parse operation is always started with the
 * {@link #createParse()} operation. The {@link Parse} object that is returned
 * will maintain all data that is related to that single parse operation.
 * </p>
 * 
 * <p>
 * <b>line 6</b> shows how a simple XML string can be provided as the input
 * source for the parse operation. Alternative methods to specify the input
 * source are {@link Parse#setFile(java.io.File)},
 * {@link Parse#setInputStream(java.io.InputStream)},
 * {@link Parse#setInputSource(InputSource)}, {@link Parse#setUrl(java.net.URL)}
 * and {@link Parse#setStreamSource(StreamSource)}.
 * </p>
 * 
 * <p>
 * <b>line 7</b> shows how the execution of the parse is performed. The input
 * source will be read, the resulting Document Object Model (DOM) will be walked
 * and potentially problems are produced in the parse.
 * </p>
 * 
 * <p>
 * <b>line 8</b> shows how an exception can be thrown in case of an error. The
 * parse execution itself tries to keep parsing as much as possible to provide
 * the developer with as much feedback as possible in one parse cycle. The
 * {@link Parse#getProblems() problems} are silently captured in the parse
 * object. If an exception is thrown,
 * it will contain a report of all the parsing problems. Alternatively,
 * the {@link Parse#hasProblems() problems in the parse object} could be examined
 * directly without the need for an exception.
 * </p>
 * 
 * <p>
 * <b>line 9</b> shows how the result of the parse operation is extracted from
 * the parse object.
 * </p>
 * 
 * <h2 id="binding">Binding</h2>
 * <p>
 * Bindings are the link between a certain type of element in your XML document
 * and the corresponding java object in your domain model.
 * </p>
 * 
 * <p>
 * A parser can be configured with a set of {@link Binding}s. Each
 * {@link Binding} knows how to transform a dom element of a given tagName to
 * the corresponding Java object. {@link Bindings} has a notion of binding
 * categories. For example, nodes and actions can be seen as different
 * categories in jPDL.
 * </p>
 * 
 * <p>
 * The purpose of bindings is to make certain elements in the parsing
 * configurable. E.g. in jPDL, the main structure of the document is fixed. But
 * node types can be added dynamically.
 * </p>
 * 
 * <p>
 * The current {@link Bindings} implementation only supports matching of an
 * element with a {@link Binding} based on tagName. If you want to take other
 * things into account (e.g. when you want to differentiate between elements of
 * the same tagName with a different attribute value), you can create a
 * specialized {@link Bindings} class.
 * </p>
 * 
 * <p>
 * Bindings are added by tagName, but they have to be looked up by element. That
 * is to support more specialized bindings implementations that match an element
 * with a binding on more information then just the tagName. In that case, a
 * specialized subclass of {@link Binding} should be created and the method
 * {@link #getBinding(Element, String)} and constructor
 * {@link Bindings#Bindings(Bindings)} should be provided with the more
 * specialized matching behaviour.
 * </p>
 * 
 * <h2 id="objectstack">Object stack</h2>
 * <p>
 * When implementing {@link Binding}s, you might want to make use of the
 * contextual object stack that is provided on the {@link Parse}. The
 * {@link Binding} implementations can maintain Java objects on that stack that
 * are being created.
 * </p>
 * 
 * <p>
 * E.g. you could push the ProcessDefinition element onto the object stack while
 * it is being parsed like this:
 * </p>
 * 
 * <pre>
 * public class MyProcessBinding implements Binding {
 * 
 *   public Object parse(Element element, Parse parse, Parser parser) {
 *     &lt;i&gt;// instantiate the object for this binding&lt;/i&gt;
 *     MyProcess myProcess = new MyProcess();
 * 
 *     &lt;i&gt;// collect all the child elements of element&lt;/i&gt;
 *     List&lt;Element&gt; elements = XmlUtil.elements(element);
 * 
 *     &lt;i&gt;// push my processDefinition onto the object stack&lt;/i&gt;
 *     parse.pushObject(myProcess);
 *     try {
 * 
 *       for (Element nodeElement: elements) {
 *         // parse the child elements with the bindings in category &quot;node&quot;
 *         parseElement(nodeElement, parse, &quot;node&quot;);
 *       }
 *     } finally {
 *       // make sure my processDefinition is popped.
 *       parse.popObject();
 *     }
 *     return myProcess;
 *   }
 * }
 * </pre>
 * 
 * <p>
 * Then, node bindings might access the processDefinition like this:
 * </p>
 * 
 * <pre>
 * public class MyNodeBinding implements Binding {
 * 
 *   public Object parse(Element element, Parse parse, Parser parser) {
 *     &lt;i&gt;// instantiate the object for this binding&lt;/i&gt;
 *     MyNode myNode = new MyNode();
 * 
 *     &lt;i&gt;// add the node to the processDefinition&lt;/i&gt;
 *     MyProcess myProcess = parse.findObject(MyProcess.class);
 *     myProcess.addNode(myNode);
 *     myNode.setMyProcess(myProcess);
 * 
 *     return myNode;
 *   }
 * }
 * </pre>
 * 
 * <p>
 * A parser implementation will typically have a static Bindings object that is
 * leveraged in all parser objects. To customize bindings for a such a parser be
 * sure to make a deep copy with {@link Bindings#Bindings(Bindings)} before you
 * start adding more bindings to the specialized parser. Otherwise the base
 * parser's bindings will be updated as well.
 * </p>
 * 
 * <h2 id="buildingcustomparsers">Building custom parsers</h2>
 * 
 * <p>
 * This parser is build for inheritance. Overriding method
 * {@link #parseDocumentElement(Element, Parse)} can be an easy way to start
 * writing your own logic on walking the Document Object Model (DOM). Such
 * customizations can still be combined with the usage of <a
 * href="#binding">bindings</a>.
 * </p>
 * 
 * <h2 id="entityresolving">Entity resolving</h2>
 * <p>
 * A parser can be configured with a set of entities with the
 * {@link #addEntity(String, Entity)} method. The {@link UrlEntity} has a
 * convenience method to build entities from resources
 * {@link UrlEntity#UrlEntity(String, ClassLoader)}.
 * </p>
 * 
 * <p>
 * When a document builder is created, the default implementation of the
 * {@link #setEntityResolver(DocumentBuilder)} will set this parser as the
 * entity resolver. The implementation method of {@link EntityResolver} (
 * {@link #resolveEntity(String, String)} will use the added {@link Entity}s to
 * try and find a match based on the publicId. If one is found, the
 * {@link Entity} inputSource is returned, otherwise the systemId is used.
 * </p>
 * 
 * <p>
 * This class is intended to be used with aggregation as well as inheritance.
 * </p>
 * 
 * @author Tom Baeyens
 */
public class Parser implements EntityResolver {

	static final Logger LOG = Logger.getLogger(Parser.class.getName());

  protected DocumentBuilderFactory documentBuilderFactory = null;
  protected Map<String, Entity> entities = null;
  protected Bindings bindings = null;
  protected ClassLoader classLoader = null;

  /** the default parser */
  public Parser() {
  }

  /**
   * creates a new Parser with bindings that can be maintained statically in
   * specialized subclasses of Parser.
   */
  public Parser(Bindings bindings) {
    this.bindings = bindings;
  }

  /**
   * creates a new Parser with bindings and entities that can be maintained
   * statically in specialized subclasses of Parser.
   */
  public Parser(Bindings bindings, Map<String, Entity> entities) {
    this.bindings = bindings;
    this.entities = entities;
  }

  // document builder methods /////////////////////////////////////////////////

  /**
   * getter with lazy initialization of the document builder factory. If no
   * document builder factory was set previously with the
   * {@link #setDocumentBuilderFactory(DocumentBuilderFactory)} method,
   * {@link #newDocumentBuilderFactory()} will be called to create one.
   */
  public synchronized DocumentBuilderFactory getDocumentBuilderFactory() {
    if (documentBuilderFactory == null) {
      documentBuilderFactory = newDocumentBuilderFactory();
    }
    return documentBuilderFactory;
  }

  /** setter for the document builder factory */
  public void setDocumentBuilderFactory(DocumentBuilderFactory documentBuilderFactory) {
    this.documentBuilderFactory = documentBuilderFactory;
  }

  /**
   * factory method for {@link DocumentBuilderFactory} during lazy
   * initialization of the documentBuilderFactory. Can be overridden by
   * subclasses to change the DocumentBuilderFactory implementation or to apply
   * specific configurations.
   */
  protected DocumentBuilderFactory newDocumentBuilderFactory() {
    return DocumentBuilderFactory.newInstance();
  }

  // entities /////////////////////////////////////////////////////////////////

  /**
   * adds a resolver to the schema catalog. See also <a
   * href="#entityresolving">section 'Entity resolving'</a>.
   */
  public void addEntity(String publicId, Entity entity) {
    if (entities == null) {
      entities = new HashMap<String, Entity>();
    }
    entities.put(publicId, entity);
  }

  /**
   * makes sure that an {@link EntityResolver} is created based on the
   * {@link Entity}s in this parser. even when none of the
   * {@link #addEntity(String, Entity)} methods are called. This enables
   * addition of entities on a per-{@link Parse} basis when there are no
   * parser-level entities.
   */
  public void useParseEntityResolver() {
    if (entities == null) {
      entities = new HashMap<String, Entity>();
    }
  }

  /**
   * implementation of {@link EntityResolver} based on a map of {@link Entity}s.
   * See also <a href="entityresolving">section 'Entity resolving'</a>.
   * 
   * @see #addEntity(String, Entity)
   */
  public InputSource resolveEntity(String publicId, String systemId) {
    InputSource inputSource = null;
    if (entities != null) {
      Entity entity = entities.get(publicId);
      if (entity != null) {
        inputSource = entity.getInputSource();
      }
    }
    if (inputSource == null) {
      if (systemId != null) {
        // plan b: see if we can build an inputsource from the systemId
        inputSource = new InputSource(systemId);
      } else {
      	if (LOG.isLoggable(Level.SEVERE)) {
          LOG.severe("couldn't resolve entity with publicId " + publicId + " and systemId " + systemId);
      	}
      }
    }
    return inputSource;
  }

  // bindings /////////////////////////////////////////////////////////////////

  /** the handlers for specific element types */
  public Bindings getBindings() {
    return bindings;
  }

  /** set the handlers for specific element types */
  public void setBindings(Bindings bindings) {
    this.bindings = bindings;
  }

  /** the handler for the given element */
  public Binding getBinding(Element element) {
    return getBinding(element, null);
  }

  /** the handler for the given element limited to a given category */
  public Binding getBinding(Element element, String category) {
    return (bindings != null ? bindings.getBinding(element, category) : null);
  }

  // runtime parsing methods //////////////////////////////////////////////////

  /**
   * main method to start a new parse, check {@link Parse} for specifying input,
   * executing the parse and extracting the results.
   */
  public Parse createParse() {
    return new Parse(this);
  }

  /**
   * builds a dom from the importedStreamSource and appends the child elements
   * of the document element to the destination element. Problems are reported
   * in the importingParse.
   */
  public void importStream(StreamSource importedStreamSource,
      Element destination, Parse importingParse) {
    try {
      // build the dom of the imported document
      Parse importedParse = createParse();
      importedParse.setStreamSource(importedStreamSource);
      Document importedDocument = buildDom(importedParse);

      // loop over all the imported document elements
      Element importedDocumentElement = importedDocument.getDocumentElement();
      for (Element e : XmlUtil.elements(importedDocumentElement)) {
        // import the element into the destination element
        destination.appendChild(destination.getOwnerDocument().importNode(e,
            true));
      }

    } catch (Exception e) {
      importingParse.addProblem("couldn't import " + importedStreamSource, e);
    }
  }

  /** customizable parse execution */
  protected void execute(Parse parse) {
    try {
      if (parse.document == null) {
        parse.document = buildDom(parse);
      }

      // walk the dom tree
      if (parse.document != null) {
        try {
          // walk the dom tree
          parseDocument(parse.document, parse);

        } catch (Exception e) {
        	e.printStackTrace();
          parse.addProblem("couldn't interpret the dom model", e);
        }
      }

    } finally {
      if (parse.inputStream != null) {
        try {
          parse.inputStream.close();
        } catch (Exception e) {
          parse.addProblem("couldn't close input stream", e);
        }
      }
    }
  }

  /**
   * customizable DOM building. Parses {@link #getInputSource(Parse) the input
   * specified in the parse} with a {@link #createDocumentBuilder(Parse)
   * DocumentBuilder}.
   * 
   * @return a Document or null in case an exception occurs during XML parsing.
   */
  protected Document buildDom(Parse parse) {
    DocumentBuilder documentBuilder = createDocumentBuilder(parse);
    InputSource inputSource = getInputSource(parse);

    try {
      // create the dom tree
      parse.document = documentBuilder.parse(inputSource);

    } catch (Exception e) {
      parse.addProblem("couldn't parse xml document", e);
    }

    return parse.document;
  }

  /**
   * customizable creation of a new document builder. Used by
   * {@link #buildDom(Parse)}.
   */
  protected DocumentBuilder createDocumentBuilder(Parse parse) {
    DocumentBuilderFactory documentBuilderFactory = getDocumentBuilderFactory();
    try {
      parse.documentBuilder = documentBuilderFactory.newDocumentBuilder();
    } catch (Exception e) {
      parse.addProblem("couldn't get new document builder", e);
      return null;
    }
    parse.documentBuilder.setErrorHandler(parse);
    parse.documentBuilder.setEntityResolver(this);
    return parse.documentBuilder;
  }

  /**
   * customizable extraction of the inputSource from the given parse.
   * 
   * Returns null in case the parse doesn't have an inputSource or a
   * streamSource specified.
   * 
   * If an inputStream is created in this method, it is set in the parse so that
   * it can be closed at the end of the {@link #execute(Parse)}.
   */
  protected InputSource getInputSource(Parse parse) {
    if (parse.inputSource != null) {
      return parse.inputSource;
    }

    if (parse.streamSource != null) {
      parse.inputStream = parse.streamSource.openStream();
      try {
        InputStreamReader isr = new InputStreamReader(parse.inputStream, BonitaConstants.FILE_ENCONDING);
        return new InputSource(isr);
      } catch (UnsupportedEncodingException e) {
        parse.addProblem("Cannot encode source using " + BonitaConstants.FILE_ENCONDING);
        return null;
      }
    }

    parse.addProblem("no source specified to parse");
    return null;
  }

  // Document Object Model walking ////////////////////////////////////////////

  /**
   * start of the DOM walk.
   * 
   * This method is used as part of {@link #execute(Parse) the parse execution}.
   * 
   * This default implementation behaviour extracts the document element and
   * delegates to {@link #parseDocumentElement(Element, Parse)}.
   * 
   * This method can be overridden for customized behaviour.
   * 
   * @return the object that is the result from parsing this document.
   */
  public Object parseDocument(Document document, Parse parse) {
    Object object = parseDocumentElement(document.getDocumentElement(), parse);
    parse.documentObject = object;
    return object;
  }

  /**
   * parses the top level element in the document and produces the object that
   * is the result from the parsing.
   * 
   * @return the object that is the result from parsing this document element.
   */
  public Object parseDocumentElement(Element documentElement, Parse parse) {
    return parseElement(documentElement, parse);
  }

  /**
   * parses an arbitrary element in the document with the first matching binding
   * found using any of the categories.
   * 
   * @return the object that is the result from parsing this element.
   */
  public Object parseElement(Element element, Parse parse) {
    return parseElement(element, parse, null);
  }

  /**
   * parses an arbitrary element in the document based on the bindings in the
   * given category.
   * 
   * @param category
   *          is the category in which the tagName should be resolved to a
   *          {@link Binding}. If category is null, all the categories will be
   *          scanned for an appropriate binding in random order.
   * 
   * @return the object that is the result from parsing this element.
   */
  public Object parseElement(Element element, Parse parse, String category) {

    Object object = null;
    String tagName = XmlUtil.getTagLocalName(element);

    Binding binding = getBinding(element, category);

    if (binding != null) {
      object = binding.parse(element, parse, this);
    } else {
      parse.addProblem("no element parser for tag "
          + tagName
          + (category != null ? " in category " + category
              : " in the default category"));
    }

    return object;
  }

}
