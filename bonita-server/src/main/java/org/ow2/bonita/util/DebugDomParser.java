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
package org.ow2.bonita.util;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.ow2.bonita.util.xml.Parse;
import org.ow2.bonita.util.xml.Parser;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

/**
 * @author Tom Baeyens
 */
public class DebugDomParser extends Parser {

  protected DebugDomBuilder debugDomBuilder = new DebugDomBuilder();

  protected Document buildDom(Parse parse) {
    DocumentBuilder documentBuilder = createDocumentBuilder(parse);
    Document document = documentBuilder.newDocument();
    debugDomBuilder.setDocument(document);

    InputSource inputSource = getInputSource(parse);

    try {
      SAXParserFactory factory = SAXParserFactory.newInstance();
      SAXParser saxParser = factory.newSAXParser();
      saxParser.parse(inputSource, debugDomBuilder);

    } catch (Exception e) {
      parse.addProblem("couldn't build DOM with DebugDomBuilder", e);
    }

    return document;
  }

  /** exposed for setting of its configuration properties */
  public DebugDomBuilder getDebugDomBuilder() {
    return debugDomBuilder;
  }

  public void setDebugDomBuilder(DebugDomBuilder debugDomBuilder) {
    this.debugDomBuilder = debugDomBuilder;
  }
}
