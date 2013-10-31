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

import org.ow2.bonita.env.Descriptor;
import org.ow2.bonita.env.operation.PropertyOperation;
import org.ow2.bonita.env.xml.WireParser;
import org.ow2.bonita.util.xml.Binding;
import org.ow2.bonita.util.xml.Parse;
import org.ow2.bonita.util.xml.Parser;
import org.ow2.bonita.util.xml.XmlUtil;
import org.w3c.dom.Element;

/**
 * <p>
 * This {@link Binding} specifies {@link PropertyOperation} (for property
 * injection).
 * </p>
 * 
 * <p>
 * A property injection is defined by a <b><code>{@literal <property>}</code>
 * </b> xml element.
 * </p>
 * 
 * <p>
 * This element must have a attribute "name" or a attribute "setter".
 * <ul>
 * <li>the "name" attribute specifies the name of the property to inject. For a
 * property <code>foo</code>, a setter method named <code>setFoo</code> should
 * exist.</li>
 * <li>the "setter" attribute specifies the name of the setter method to use to
 * inject the property.</li>
 * </ul>
 * If both attributes are present, the setter method is used.
 * </p>
 * 
 * <p>
 * This element should contain a child element that defines a {@link Descriptor}
 * . This descriptor will be used to create the object that will be assigned to
 * the field
 * </p>
 * 
 * <h3>Example</h3>
 * 
 * Consider the following class:
 * 
 * <pre>
 * public class Foo {
 *   String bar;
 * 
 *   public void setBar(String bar) {
 *     this.bar = bar;
 *   }
 * }
 * </pre>
 * 
 * The following Xml declaration will create an object 'o' of class 'Foo' (see
 * {@link org.ow2.bonita.env.descriptor.ObjectDescriptor}), and
 * the value <code>hello</code> will be assigned to <code>o.bar</code> by
 * calling the <code>setBar</code> method.
 * 
 * <pre>
 * &lt;objects&gt;
 *   &lt;object name='o' class='Foo'&gt;
 *     &lt;property name='bar'&gt;
 *       &lt;string value='hello' /&gt;
 *     &lt;/property&gt;
 *   &lt;/object&gt;
 * &lt;/objects&gt;
 * </pre>
 * 
 * @author Tom Baeyens
 * @author Guillaume Porcher (documentation)
 * 
 * @see ObjectBinding
 * @see WireParser
 */
public class PropertyBinding extends WireOperationBinding {

  public PropertyBinding() {
    super("property");
  }

  public Object parse(Element element, Parse parse, Parser parser) {
    PropertyOperation propertyOperation = new PropertyOperation();
    if (element.hasAttribute("setter")) {
      propertyOperation.setSetterName(element.getAttribute("setter"));
    } else if (element.hasAttribute("name")) {
      propertyOperation.setPropertyName(element.getAttribute("name"));
    } else {
      parse.addProblem("property must have name or setter attribute: "
          + XmlUtil.toString(element));
    }
    Element descriptorElement = XmlUtil.element(element);
    if (descriptorElement != null) {
      Descriptor descriptor = (Descriptor) parser.parseElement(
          descriptorElement, parse, WireParser.CATEGORY_DESCRIPTOR);
      if (descriptor != null) {
        propertyOperation.setDescriptor(descriptor);
      } else {
        parse
            .addProblem("couldn't parse property content element as a value descriptor: "
                + XmlUtil.toString(element));
      }
    } else {
      parse.addProblem("property must have 1 descriptor element out of "
          + parser.getBindings().getTagNames(WireParser.CATEGORY_DESCRIPTOR)
          + " as content: " + XmlUtil.toString(element));
    }
    return propertyOperation;
  }
}