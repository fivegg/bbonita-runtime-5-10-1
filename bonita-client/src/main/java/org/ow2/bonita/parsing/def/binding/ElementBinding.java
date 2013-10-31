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
package org.ow2.bonita.parsing.def.binding;

import java.util.List;

import org.ow2.bonita.parsing.def.XmlDefParser;
import org.ow2.bonita.util.BonitaRuntimeException;
import org.ow2.bonita.util.ExceptionManager;
import org.ow2.bonita.util.Misc;
import org.ow2.bonita.util.xml.Parse;
import org.ow2.bonita.util.xml.Parser;
import org.ow2.bonita.util.xml.TagBinding;
import org.ow2.bonita.util.xml.XmlUtil;
import org.w3c.dom.Element;

/**
 * @author Anthony Birembaut
 */
public abstract class ElementBinding extends TagBinding {
  
  protected ElementBinding(String tagName) {
    super(tagName, "", XmlDefParser.CATEGORY_MAJOR_ELT);
  }

  protected <T extends Object> T getObject(Class<T> clazz, Parse parse) {
    return parse.findObject(clazz);
  }

  protected String getChildTextContent(Element fatherElement, String childTagName) {
    if (fatherElement == null) {
      return null;
    }
    Element childElement = XmlUtil.element(fatherElement, childTagName);
    if (childElement != null) {
      return childElement.getTextContent().trim();
    }
    return null;
  }

  protected <T extends Enum<T> > T getEnumValue(Class< T > enumType, String valueAsString, T defaultValue) {
    if (valueAsString == null) {
      return defaultValue;
    }
    try {
      return Misc.stringToEnum(enumType, valueAsString);
    } catch (IllegalArgumentException e) {
      String message = ExceptionManager.getInstance().getMessage(
          "bpx_EB_1", valueAsString, enumType.getName());
      throw new BonitaRuntimeException(message, e);
    }
  }
  
  protected void parseElementList(Element parentElement,
      String listName, String elementName, Parse parse, Parser parser) {

    Element listElement = XmlUtil.element(parentElement, listName);
    if (listElement != null) {
      List<Element> subElements = XmlUtil.elements(listElement, elementName);
      if (subElements != null) {
        for (Element subElement : subElements) {
          parser.parseElement(subElement, parse, XmlDefParser.CATEGORY_MAJOR_ELT);
        }
      }
    }
  }
  
}
