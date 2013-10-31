/**
 * Copyright (C) 2007  Bull S. A. S.
 * Bull, Rue Jean Jaures, B.P.68, 78340, Les Clayes-sous-Bois
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
package org.ow2.bonita.env.xml;

import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ow2.bonita.env.Descriptor;
import org.ow2.bonita.env.WireContext;
import org.ow2.bonita.env.WireDefinition;
import org.ow2.bonita.env.binding.BooleanBinding;
import org.ow2.bonita.env.binding.ByteBinding;
import org.ow2.bonita.env.binding.CharBinding;
import org.ow2.bonita.env.binding.ClassBinding;
import org.ow2.bonita.env.binding.DoubleBinding;
import org.ow2.bonita.env.binding.FloatBinding;
import org.ow2.bonita.env.binding.IntBinding;
import org.ow2.bonita.env.binding.ListBinding;
import org.ow2.bonita.env.binding.MapBinding;
import org.ow2.bonita.env.binding.NullBinding;
import org.ow2.bonita.env.binding.ObjectBinding;
import org.ow2.bonita.env.binding.RefBinding;
import org.ow2.bonita.env.binding.SetBinding;
import org.ow2.bonita.env.binding.StringBinding;
import org.ow2.bonita.env.descriptor.AbstractDescriptor;
import org.ow2.bonita.env.descriptor.ArgDescriptor;
import org.ow2.bonita.env.descriptor.ObjectDescriptor;
import org.ow2.bonita.util.Misc;
import org.ow2.bonita.util.ReflectUtil;
import org.ow2.bonita.util.xml.Bindings;
import org.ow2.bonita.util.xml.Parse;
import org.ow2.bonita.util.xml.Parser;
import org.ow2.bonita.util.xml.XmlUtil;
import org.w3c.dom.Element;

/**
 * parses object wiring xml and constructs a WireDefinition.
 * 
 * <p>
 * To learn the full XML syntax, check out the xsd docs
 * </p>
 * <ul>
 * <li>To describe an object:<br/>
 * <ul>
 * <li><b><code>&lt;object .../&gt;</code></b>: see {@link ObjectBinding}</li>
 * </ul>
 * </li>
 * <li>To describe basic types:<br/>
 * <ul>
 * <li><b><code>&lt;boolean .../&gt;</code>, <code>&lt;true /&gt;</code> and <code>&lt;false/&gt;</code></b>: see
 * {@link BooleanBinding}</li>
 * <li><b><code>&lt;byte .../&gt;</code></b>: see {@link ByteBinding}</li>
 * <li><b><code>&lt;char .../&gt;</code></b>: see {@link CharBinding}</li>
 * <li><b><code>&lt;double .../&gt;</code></b>: see {@link DoubleBinding}</li>
 * <li><b><code>&lt;float .../&gt;</code></b>: see {@link FloatBinding}</li>
 * <li><b><code>&lt;int .../&gt;</code></b>: see {@link IntBinding}</li>
 * <li><b><code>&lt;string .../&gt;</code></b>: see {@link StringBinding}</li>
 * </ul>
 * </li>
 * <li>To describe collections:<br/>
 * <ul>
 * <li><b><code>&lt;map .../&gt;</code></b>: see {@link MapBinding}</li>
 * <li><b><code>&lt;set .../&gt;</code></b>: see {@link SetBinding}</li>
 * <li><b><code>&lt;list .../&gt;</code></b>: see {@link ListBinding}</li>
 * </ul>
 * </li>
 * <li>Others:<br/>
 * <ul>
 * <li><b><code>&lt;class .../&gt;</code></b>: see {@link ClassBinding}</li>
 * <li><b><code>&lt;ref .../&gt;</code></b>: see {@link RefBinding}</li>
 * <li><b><code>&lt;null .../&gt;</code></b>: see {@link NullBinding}</li>
 * </ul>
 * </li>
 * </ul>
 * 
 * <h3>Bindings</h3>
 * 
 * <p>
 * The defaults bindings for the Wiring XML are divided in two categories:
 * </p>
 * 
 * <ul>
 * <li>Descriptors, registered with the {@link #CATEGORY_DESCRIPTOR} category</li>
 * <li>Operations, registered with the {@link #CATEGORY_OPERATION} category</li>
 * </ul>
 * 
 * <p>
 * Once a parser is created, bindings can be added, overwritten and removed to customize that parser instance.
 * </p>
 * 
 * <h3 id='args'>Describing arguments</h3>
 * 
 * <p>
 * An ArgDescriptor is defined by a <b><code>&lt;arg&gt;</code></b> xml element.
 * </p>
 * 
 * <p>
 * This element can have an attribute "type", which specifies name of the argument's type.
 * </p>
 * <p>
 * This element contains <b>one</b> child element that defines a {@link Descriptor}. This descriptor specifies the value
 * to give to the argument.
 * </p>
 * 
 * <h4>Example</h4>
 * 
 * Consider the following class:
 * 
 * <pre>
 * public class Hello {
 *   public static String sayHello(String name) {
 *     return &quot;Hello &quot; + name + &quot; !&quot;;
 *   }
 * }
 * </pre>
 * 
 * The following Xml declaration will create an object 's' of class 'String' (see {@link ObjectDescriptor}). This object
 * is created by invoking <code>Hello.sayHello</code> with the value <code>world</code> as a parameter.
 * 
 * <pre>
 * &lt;objects&gt;
 *   &lt;object name=&quot;s&quot; class='Hello' method='sayHello'&gt;
 *     &lt;arg&gt;
 *      &lt;string value='world' /&gt;
 *     &lt;/arg&gt;
 *   &lt;/object&gt;
 * &lt;/objects&gt;
 * </pre>
 * 
 * The created object 's' will be a String, containing "Hello world !".
 * 
 * <h3 id='init'>Initialization</h3>
 * 
 * <p>
 * The initialization method can be defined with the <code>init</code> attribute. For more details on how initialization
 * works, see section 'Initialization' of {@link WireContext}.
 * </p>
 * 
 * The <code>init</code> attribute can have these values:
 * <ul>
 * <li><code>lazy</code>: for lazy creation and delayed initialization</li>
 * <li><code>required</code>: for lazy creation and immediate initialization</li>
 * <li><code>eager</code>: for eager creation and delayed initialization</li>
 * <li><code>immediate</code>: for eager creation and immediate initialization</li>
 * </ul>
 * 
 * @author Tom Baeyens
 * @author Guillaume Porcher (documentation)
 */
public class WireParser extends Parser {

  public static final String PVM_WIRE_BINDINGS_RESOURCES = "bonita.wire.bindings.xml";

  private static final Logger LOG = Logger.getLogger(WireParser.class.getName());

  public static final String CATEGORY_DESCRIPTOR = "descriptor";
  public static final String CATEGORY_OPERATION = "operation";
  public static final String CATEGORY_INTERCEPTOR = "interceptor";

  /** static instance of WireParser */
  private static WireParser instance;
  /** default bindings for handling wiring xml elements. */
  private static Bindings defaultBindings; // initialized at the bottom of this
                                           // file

  /**
   * Constructs a new WireParser with the default bindings.
   */
  public WireParser() {
    super(defaultBindings);
  }

  /**
   * Default method to get an instance of the WireParser
   * 
   * @return the static instance of WireParser
   */
  public static synchronized WireParser getInstance() {
    if (instance == null) {
      instance = new WireParser();
    }
    return instance;
  }

  /**
   * Convenience method to parse a wiring xml.
   * 
   * @param xmlString
   *          the xml string to parse
   * @return the WireDefinition created by parsing the xml given in input.
   * @see #parseXmlString(String)
   */
  public static WireDefinition parseXmlString(final String xmlString) {
    final Parse parse = getInstance().createParse().setString(xmlString).execute();
    Misc.showProblems(parse.getProblems(), "wire definition xml string");

    return (WireDefinition) parse.getDocumentObject();
  }

  // document element parsing /////////////////////////////////////////////////

  /**
   * This method builds the WireDefinition from the DOM tree. This methods parses all child nodes of the documentElement
   * that correspond to a Descriptor definition.
   * 
   * @param documentElement
   *          the root element of the document
   * @param parse
   *          Parse object that contains all information for the current parse operation.
   * @return an instance of WireDefinition containing the resulting WireDefinition.
   * @see Parser#parseDocumentElement(Element, Parse)
   */
  @Override
  public Object parseDocumentElement(final Element documentElement, final Parse parse) {
    final List<Element> elements = XmlUtil.elements(documentElement);

    final WireDefinition wireDefinition = new WireDefinition();
    wireDefinition.setClassLoader(classLoader);

    if (elements != null) {
      parse.pushObject(wireDefinition);
      try {
        for (final Element descriptorElement : elements) {
          parseElement(descriptorElement, parse, CATEGORY_DESCRIPTOR);
        }
      } finally {
        parse.popObject();
      }
    }
    return wireDefinition;
  }

  /**
   * This method parses an arbitrary element in the document based on the bindings in the given category. This method
   * calls the {@link Parser#parseElement(Element, Parse, String)} method to build the resulting object. If the
   * resulting object is a subclass of {@link AbstractDescriptor}, the related fields are initialized.
   * 
   * @param element
   *          the element to parse
   * @param parse
   *          Parse object that contains all information for the current parse operation.
   * @param category
   *          is the category in which the tagName should be resolved to an ElementHandler. If category is null, all the
   *          categories will be scanned for an appropriate binding in random order.
   * @return the java object created from the DOM element
   */
  @Override
  public Object parseElement(final Element element, final Parse parse, final String category) {
    if (element == null) {
      return null;
    }
    final Object object = super.parseElement(element, parse, category);
    if (object instanceof Descriptor) {

      final Descriptor descriptor = (Descriptor) object;

      if (descriptor instanceof AbstractDescriptor) {
        final AbstractDescriptor abstractDescriptor = (AbstractDescriptor) descriptor;
        if (element.hasAttribute("name")) {
          final String name = element.getAttribute("name");
          // get the name
          abstractDescriptor.setName(name);
        }

        if (element.hasAttribute("init")) {
          // get the init
          final String initText = element.getAttribute("init");

          if ("eager".equalsIgnoreCase(initText)) {
            abstractDescriptor.setInit(AbstractDescriptor.INIT_EAGER);
          } else if ("immediate".equalsIgnoreCase(initText)) {
            abstractDescriptor.setInit(AbstractDescriptor.INIT_IMMEDIATE);
          } else if ("required".equalsIgnoreCase(initText)) {
            abstractDescriptor.setInit(AbstractDescriptor.INIT_REQUIRED);
          } else {
            // init='lazy' or default value
            abstractDescriptor.setInit(AbstractDescriptor.INIT_LAZY);
          }
        }
      }

      // add the descriptor
      final WireDefinition wireDefinition = parse.findObject(WireDefinition.class);
      wireDefinition.addDescriptor(descriptor);
    }
    return object;
  }

  // other methods ////////////////////////////////////////////////////////////

  /**
   * Parses the list of arguments of a method. This method creates a list of {@link ArgDescriptor} from the given list
   * of DOM elements
   * 
   * @param argElements
   *          the list of argument DOM elements
   * @param parse
   *          Parse object that contains all information for the current parse operation.
   * @return the list of ArgDescriptor created from the DOM elements
   * @see ArgDescriptor
   */
  public List<ArgDescriptor> parseArgs(final List<Element> argElements, final Parse parse) {
    List<ArgDescriptor> args = null;
    if (argElements != null) {
      if (argElements.size() > 0) {
        args = new ArrayList<ArgDescriptor>(argElements.size());
      }
      for (final Element argElement : argElements) {
        final ArgDescriptor argDescriptor = new ArgDescriptor();
        argDescriptor.setTypeName(XmlUtil.attribute(argElement, "type"));
        final Element descriptorElement = XmlUtil.element(argElement);
        if (descriptorElement == null) {
          parse.addProblem("arg must contain exactly one descriptor element out of "
              + bindings.getTagNames(CATEGORY_DESCRIPTOR) + " as contents:"
              + XmlUtil.toString((Element) argElement.getParentNode()));
        } else {
          final Descriptor descriptor = (Descriptor) parseElement(descriptorElement, parse, CATEGORY_DESCRIPTOR);
          argDescriptor.setDescriptor(descriptor);
        }
        args.add(argDescriptor);
      }
    }
    return args;
  }

  static {
    // default descriptor parsers
    // ///////////////////////////////////////////////
    defaultBindings = new Bindings();

    final BindingParser bindingParser = new BindingParser();

    final Enumeration<URL> enumeration = ReflectUtil.getResources(null, PVM_WIRE_BINDINGS_RESOURCES);
    if (enumeration != null) {
      while (enumeration.hasMoreElements()) {
        final URL url = enumeration.nextElement();
        if (LOG.isLoggable(Level.FINE)) {
          LOG.fine("parsing bindings from resource url: " + url);
        }

        final Parse parse = bindingParser.createParse().setUrl(url).pushObject(defaultBindings).execute();
        Misc.showProblems(parse.getProblems(), "default wire bindings");
      }
    }
  }
}