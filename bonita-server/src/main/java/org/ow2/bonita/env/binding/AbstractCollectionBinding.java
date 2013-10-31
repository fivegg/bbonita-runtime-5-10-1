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

import java.util.ArrayList;
import java.util.List;

import org.ow2.bonita.env.Descriptor;
import org.ow2.bonita.env.descriptor.CollectionDescriptor;
import org.ow2.bonita.env.xml.WireParser;
import org.ow2.bonita.util.BonitaRuntimeException;
import org.ow2.bonita.util.ReflectUtil;
import org.ow2.bonita.util.xml.Parse;
import org.ow2.bonita.util.xml.Parser;
import org.ow2.bonita.util.xml.XmlUtil;
import org.w3c.dom.Element;

public abstract class AbstractCollectionBinding extends WireDescriptorBinding {

  public AbstractCollectionBinding(String tagName) {
    super(tagName);
  }

  public Object parse(Element element, Parse parse, Parser parser) {
    CollectionDescriptor descriptor = createDescriptor();

    String className = XmlUtil.attribute(element, "class");

    // verify if the given classname is specified and implements the collection
    // interface
    if (verify(className, getCollectionInterface(), parse, parser)) {
      descriptor.setClassName(className);
    }

    Boolean isSynchronized = XmlUtil.attributeBoolean(element, "synchronized",
        false, parse);
    if (isSynchronized != null) {
      descriptor.setSynchronized(isSynchronized.booleanValue());
    }

    List<Descriptor> valueDescriptors = new ArrayList<Descriptor>();
    List<Element> elements = XmlUtil.elements(element);
    if (elements != null) {
      for (Element valueElement : elements) {
        Descriptor valueDescriptor = (Descriptor) parser.parseElement(
            valueElement, parse, WireParser.CATEGORY_DESCRIPTOR);
        if (valueDescriptor != null) {
          valueDescriptors.add(valueDescriptor);
        }
      }
    }
    descriptor.setValueDescriptors(valueDescriptors);
    return descriptor;
  }

  /**
   * verifies if the given classname is specified and implements the collection
   * interface
   */
  public static boolean verify(String className, Class<?> collectionInterface,
      Parse parse, Parser parser) {
    if (className == null) {
      return false;
    }

    try {
      Class<?> collectionClass = ReflectUtil.loadClass(parse.getClassLoader(),
          className);

      if (collectionInterface.isAssignableFrom(collectionClass)) {
        return true;
      } else {
        parse.addProblem("class " + className + " is not a "
            + collectionInterface.getName());
      }
    } catch (BonitaRuntimeException e) {
      parse.addProblem("class " + className + " could not be found");
    }
    return false;
  }

  protected abstract Class<?> getCollectionInterface();

  protected abstract CollectionDescriptor createDescriptor();
}
