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
package org.ow2.bonita.env;

import java.io.InputStream;
import java.net.URL;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilderFactory;

import org.ow2.bonita.env.descriptor.ObjectDescriptor;
import org.ow2.bonita.env.xml.BindingParser;
import org.ow2.bonita.util.BonitaRuntimeException;
import org.ow2.bonita.util.ExceptionManager;
import org.ow2.bonita.util.Misc;
import org.ow2.bonita.util.ReflectUtil;
import org.ow2.bonita.util.xml.Bindings;
import org.ow2.bonita.util.xml.Parse;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

/**
  * @author Marc Blachon, Guillaume Porcher, Charles Souillard, Miguel Valdes, Pierre Vigneras
 */
public class BonitaEnvironmentParser extends PvmEnvironmentFactoryParser {

  public static final String CLASSPATH_URL = "classpath://";
  private static final Logger LOG = Logger.getLogger(BonitaEnvironmentParser.class.getName());

  private static PvmEnvironmentFactoryParser instance;

  public static final String BONITA_WIRE_BINDINGS_RESOURCES = "bonita.wire.bindings.xml";


  public static synchronized PvmEnvironmentFactoryParser getInstance() {
    if (instance == null) {
      instance = new BonitaEnvironmentParser();
      // Get bindings
      final Bindings bindings = instance.getEnvironmentXmlParser().getBindings();
      // Use the same bindings in block and application contexts
      instance.getEnvironmentFactoryXmlParser().setBindings(bindings);
      // Add bonita bindings
      final BindingParser bindingParser = new BindingParser();
      final URL url = ReflectUtil.getResource(null, BONITA_WIRE_BINDINGS_RESOURCES);
      if (url != null) {
        LOG.info("parsing bindings from resource url: " + url);

        Parse parse = bindingParser.createParse()
          .setUrl(url)
          .pushObject(bindings)
          .execute();
        
          Misc.showProblems(parse.getProblems(), "bonita wire bindings");
      }
    }
    return instance;
  }

  @Override
  public PvmEnvironmentFactory parseDocument(final Document document, final Parse parse) {
    // Default parsing
    final PvmEnvironmentFactory defaultEnvironmentFactory = (PvmEnvironmentFactory) super.parseDocument(document, parse);

    // Add authentication descriptor to application block
    final ObjectDescriptor authenticationDescriptor = new ObjectDescriptor();
    authenticationDescriptor.setClassName(Authentication.class.getName());
    defaultEnvironmentFactory.getEnvironmentFactoryCtxWireContext().getWireDefinition().addDescriptor(authenticationDescriptor);

    return defaultEnvironmentFactory;
  }

  public static EnvironmentFactory parseEnvironmentFactoryFromXmlString(
      final String xmlString) {
    final Parse parse = getInstance().createParse();
    parse.setString(xmlString);
    final EnvironmentFactory factory = (EnvironmentFactory) parse.execute().getDocumentObject();
    Misc.showProblems(parse.getProblems(), "environment");
    return factory;
  }

  @Override
  public synchronized DocumentBuilderFactory getDocumentBuilderFactory() {
    this.documentBuilderFactory = newDocumentBuilderFactory();
    this.documentBuilderFactory.setNamespaceAware(true);
    this.documentBuilderFactory.setValidating(true);
    // ignore white space can only be set if parser is validating
    this.documentBuilderFactory.setIgnoringElementContentWhitespace(true);
    // select xml schema as the schema language (a.o.t. DTD)
    this.documentBuilderFactory.setAttribute(
        "http://java.sun.com/xml/jaxp/properties/schemaLanguage",
        "http://www.w3.org/2001/XMLSchema");
    //  set schema sources
    final URL url = BonitaEnvironmentParser.class.getClassLoader().getResource("bonita-environment.xsd");
    if (url == null) {
    	String message = ExceptionManager.getInstance().getFullMessage("benv_XEP_1");
      throw new BonitaRuntimeException(message);
    }
    this.documentBuilderFactory.setAttribute(
        "http://java.sun.com/xml/jaxp/properties/schemaSource", url.toExternalForm());
    return this.documentBuilderFactory;
  }

  /* (non-Javadoc)
   * @see org.ow2.bonita.xml.Parser#resolveEntity(java.lang.String, java.lang.String)
   */
  @Override
  public InputSource resolveEntity(final String publicId, final String systemId) {
    if (systemId.startsWith(CLASSPATH_URL)) {
      final String localpart = systemId.substring(CLASSPATH_URL.length());
      final InputStream inStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(localpart);
      if (inStream != null) {
        return new InputSource(inStream);
      }
    }
    return super.resolveEntity(publicId, systemId);
  }

}
