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
package org.ow2.bonita.env.binding;

import org.ow2.bonita.env.descriptor.AbstractDescriptor;
import org.ow2.bonita.util.xml.Parse;
import org.ow2.bonita.util.xml.Parser;
import org.ow2.bonita.util.xml.XmlUtil;
import org.w3c.dom.Element;

public abstract class BasicTypeBinding extends WireDescriptorBinding {

  public BasicTypeBinding(String tagName) {
    super(tagName);
  }

  public Object parse(Element element, Parse parse, Parser parser) {
    AbstractDescriptor descriptor = null;

    if (element.hasAttribute("value")) {
      String value = element.getAttribute("value");
      descriptor = createDescriptor(value, element, parse);
    } else {
      parse.addProblem("attribute 'value' is required in element '"
          + XmlUtil.getTagLocalName(element) + "': "
          + XmlUtil.toString(element));
    }

    return descriptor;
  }

  public String createValueExceptionMessage(String message, Element element) {
    return XmlUtil.getTagLocalName(element)
        + " has invalid formatted value attribute: "
        + (message != null ? message + ": " : "") + XmlUtil.toString(element);
  }

  /**
   * subclasses can be sure that the value is not null. subclasses should use
   * {@link #createValueExceptionMessage(String, Element) for reporting format
   * problems in the parse.
   */
  protected abstract AbstractDescriptor createDescriptor(String value,
      Element element, Parse parse);
}