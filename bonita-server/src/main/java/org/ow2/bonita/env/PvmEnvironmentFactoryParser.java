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
package org.ow2.bonita.env;

import org.ow2.bonita.env.xml.WireParser;
import org.ow2.bonita.util.Misc;
import org.ow2.bonita.util.stream.StreamSource;
import org.ow2.bonita.util.xml.Parse;
import org.ow2.bonita.util.xml.Parser;
import org.ow2.bonita.util.xml.XmlUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * main parser. It parses the <code>contexts</code>, which is assumed the
 * document element and can contain the {@link EnvironmentFactoryXmlParser
 * environment-factory} context and the {@link EnvironmentXmlParser environment}
 * context.
 * 
 * See {@link Parser} for usage instructions.
 * 
 * @author Tom Baeyens
 */
public class PvmEnvironmentFactoryParser extends Parser {

  WireParser environmentFactoryXmlParser = new WireParser();
  WireParser environmentXmlParser = new WireParser();

  protected static final PvmEnvironmentFactoryParser INSTANCE = new PvmEnvironmentFactoryParser();

  public static PvmEnvironmentFactoryParser getInstance() {
    return INSTANCE;
  }

  /**
   * parses the given stream source with {@link #getInstance() the current
   * parser}.
   */
  static EnvironmentFactory parse(final StreamSource streamSource) {
    final Parse parse = getInstance().createParse().setStreamSource(streamSource).execute();
    Misc.showProblems(parse.getProblems(), "environment configuration " + streamSource);
    return (EnvironmentFactory) parse.getDocumentObject();
  }

  @Override
  public Object parseDocument(final Document document, final Parse parse) {
    final Element documentElement = document.getDocumentElement();

    // if the default environment factory was already set in the parse
    PvmEnvironmentFactory pvmEnvironmentFactory = (PvmEnvironmentFactory) parse.getDocumentObject();
    if (pvmEnvironmentFactory == null) {
      pvmEnvironmentFactory = new PvmEnvironmentFactory();
      parse.setDocumentObject(pvmEnvironmentFactory);
    }

    final WireDefinition environmentFactoryWireDefinition = getApplicationWireDefinition(documentElement, parse);
    final WireDefinition environmentWireDefinition = getBlockWireDefinition(documentElement, parse);

    // create the application wire context from the definition
    final WireContext environmentFactoryWireContext = new WireContext(environmentFactoryWireDefinition,
        Context.CONTEXTNAME_ENVIRONMENT_FACTORY);
    // propagate the parser classloader to the application context
    environmentFactoryWireContext.setClassLoader(classLoader);

    // configure the default environment factory
    pvmEnvironmentFactory.setEnvironmentFactoryCtxWireContext(environmentFactoryWireContext);
    pvmEnvironmentFactory.setEnvironmentCtxWireDefinition(environmentWireDefinition);

    parse.setDocumentObject(pvmEnvironmentFactory);

    return pvmEnvironmentFactory;
  }

  WireDefinition getApplicationWireDefinition(final Element documentElement, final Parse parse) {
    final Element applicationElement = XmlUtil.element(documentElement, Context.CONTEXTNAME_ENVIRONMENT_FACTORY);
    if (applicationElement != null) {
      return (WireDefinition) environmentFactoryXmlParser.parseDocumentElement(applicationElement, parse);
    }
    return null;
  }

  WireDefinition getBlockWireDefinition(final Element documentElement, final Parse parse) {
    final Element blockElement = XmlUtil.element(documentElement, Context.CONTEXTNAME_ENVIRONMENT);
    if (blockElement != null) {
      return (WireDefinition) environmentXmlParser.parseDocumentElement(blockElement, parse);
    }
    return null;
  }

  public WireParser getEnvironmentFactoryXmlParser() {
    return environmentFactoryXmlParser;
  }

  public void setEnvironmentFactoryXmlParser(final WireParser applicationWireXmlParser) {
    environmentFactoryXmlParser = applicationWireXmlParser;
  }

  public WireParser getEnvironmentXmlParser() {
    return environmentXmlParser;
  }

  public void setEnvironmentXmlParser(final WireParser blockWireXmlParser) {
    environmentXmlParser = blockWireXmlParser;
  }
}
