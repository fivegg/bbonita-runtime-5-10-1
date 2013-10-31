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
package org.ow2.bonita.env.binding;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ow2.bonita.env.Descriptor;
import org.ow2.bonita.env.descriptor.MapDescriptor;
import org.ow2.bonita.env.xml.WireParser;
import org.ow2.bonita.util.xml.Parse;
import org.ow2.bonita.util.xml.Parser;
import org.ow2.bonita.util.xml.XmlUtil;
import org.w3c.dom.Element;

/**
 * parses a descriptor for creating a {@link java.util.Map}.
 * 
 * See schema docs for more details.
 * 
 * @author Tom Baeyens
 * @author Guillaume Porcher (documentation)
 */
public class MapBinding extends WireDescriptorBinding {

  public MapBinding() {
    super("map");
  }

  public Object parse(Element element, Parse parse, Parser parser) {
    MapDescriptor descriptor = new MapDescriptor();
    String className = XmlUtil.attribute(element, "class");
    if (AbstractCollectionBinding.verify(className, Map.class, parse, parser)) {
      descriptor.setClassName(className);
    }

    Boolean isSynchronized = XmlUtil.attributeBoolean(element, "synchronized",
        false, parse);
    if (isSynchronized != null) {
      descriptor.setSynchronized(isSynchronized.booleanValue());
    }

    List<Descriptor> keyDescriptors = new ArrayList<Descriptor>();
    List<Descriptor> valueDescriptors = new ArrayList<Descriptor>();
    List<Element> elements = XmlUtil.elements(element);
    if (elements != null) {
      for (Element entryElement : elements) {
        if ("entry".equals(XmlUtil.getTagLocalName(entryElement))) {
          // key
          Element keyElement = XmlUtil.element(entryElement, "key");
          Element keyDescriptorElement = (keyElement != null ? XmlUtil
              .element(keyElement) : null);
          Descriptor keyDescriptor = (Descriptor) parser.parseElement(
              keyDescriptorElement, parse, WireParser.CATEGORY_DESCRIPTOR);
          // value
          Element valueElement = XmlUtil.element(entryElement, "value");
          Element valueDescriptorElement = (valueElement != null ? XmlUtil
              .element(valueElement) : null);
          Descriptor valueDescriptor = (Descriptor) parser.parseElement(
              valueDescriptorElement, parse, WireParser.CATEGORY_DESCRIPTOR);

          if ((keyDescriptor != null) && (valueDescriptor != null)) {
            keyDescriptors.add(keyDescriptor);
            valueDescriptors.add(valueDescriptor);
          } else {
            parse
                .addProblem("entry must have key and value element with a single descriptor as contents: "
                    + XmlUtil.toString(entryElement));
          }
        } else {
          parse.addProblem("map can only contain entry elements: "
              + XmlUtil.toString(entryElement));
        }

      }
    }
    descriptor.setKeyDescriptors(keyDescriptors);
    descriptor.setValueDescriptors(valueDescriptors);
    return descriptor;
  }
}