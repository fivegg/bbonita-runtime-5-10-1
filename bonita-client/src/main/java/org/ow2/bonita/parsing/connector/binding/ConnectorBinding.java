/**
 * Copyright (C) 2010-2012 BonitaSoft S.A.
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
package org.ow2.bonita.parsing.connector.binding;

import org.ow2.bonita.connector.core.desc.ConnectorDescriptor;
import org.ow2.bonita.parsing.def.XmlDefParser;
import org.ow2.bonita.parsing.def.binding.ElementBinding;
import org.ow2.bonita.util.xml.Parse;
import org.ow2.bonita.util.xml.Parser;
import org.ow2.bonita.util.xml.XmlUtil;
import org.w3c.dom.Element;

/**
 * 
 * @author Matthieu Chaffotte
 * 
 */
public class ConnectorBinding extends ElementBinding {

  public ConnectorBinding() {
    super("connector");
  }

  @Override
  public Object parse(final Element connectorElement, final Parse parse, final Parser parser) {
    final String connectorId = getChildTextContent(connectorElement, "connectorId");
    final String version = getChildTextContent(connectorElement, "version");
    final String icon = getChildTextContent(connectorElement, "icon");

    final ConnectorDescriptor descriptor = new ConnectorDescriptor(connectorId, version, parse.getClassLoader());
    descriptor.setIcon(icon);

    parse.pushObject(descriptor);
    if (version == null) {
      final Element categoryElement = XmlUtil.element(connectorElement, "category");
      if (categoryElement != null) {
        parser.parseElement(categoryElement, parse, XmlDefParser.CATEGORY_MAJOR_ELT);
      }
    } else if ("5.0".equals(version)) {
      parseElementList(connectorElement, "categories", "category", parse, parser);
    }
    parseElementList(connectorElement, "inputs", "setter", parse, parser);
    parseElementList(connectorElement, "outputs", "getter", parse, parser);
    parseElementList(connectorElement, "pages", "page", parse, parser);
    parse.popObject();
    return descriptor;
  }

}
