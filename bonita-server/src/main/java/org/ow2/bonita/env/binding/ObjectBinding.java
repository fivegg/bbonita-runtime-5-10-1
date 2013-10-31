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

import org.ow2.bonita.env.Descriptor;
import org.ow2.bonita.env.descriptor.ArgDescriptor;
import org.ow2.bonita.env.descriptor.ObjectDescriptor;
import org.ow2.bonita.env.operation.Operation;
import org.ow2.bonita.env.xml.WireParser;
import org.ow2.bonita.util.xml.Parse;
import org.ow2.bonita.util.xml.Parser;
import org.ow2.bonita.util.xml.XmlUtil;
import org.w3c.dom.Element;

/**
 * parses a descriptor for creating a java object through reflection.
 * 
 * See schema docs for more details.
 * 
 * @author Tom Baeyens
 * @author Guillaume Porcher (documentation)
 */
public class ObjectBinding extends WireDescriptorBinding {

  public ObjectBinding() {
    super("object");
  }

  public static boolean isObjectDescriptor(Element element) {
    if (XmlUtil.attribute(element, "class") != null) {
      return true;
    }
    if (XmlUtil.attribute(element, "factory") != null) {
      return true;
    }
    if (XmlUtil.element(element, "factory") != null) {
      return true;
    }
    return false;
  }

  public Object parse(Element element, Parse parse, Parser parser) {
    ObjectDescriptor descriptor = new ObjectDescriptor();

    WireParser wireParser = (WireParser) parser;
    String className = XmlUtil.attribute(element, "class");
    String factoryObjectName = XmlUtil.attribute(element, "factory");
    Element factoryElement = XmlUtil.element(element, "factory");

    if (className != null) {
      descriptor.setClassName(className);
      if (factoryObjectName != null) {
        parse
            .addProblem("attribute 'factory' is specified together with attribute 'class' in element 'object': "
                + XmlUtil.toString(element));
      }
      if (factoryElement != null) {
        parse
            .addProblem("element 'factory' is specified together with attribute 'class' in element 'object': "
                + XmlUtil.toString(element));
      }

      Element constructorElement = XmlUtil.element(element, "constructor");
      if (constructorElement != null) {
        List<Element> argElements = XmlUtil.elements(constructorElement, "arg");
        List<ArgDescriptor> argDescriptors = wireParser.parseArgs(argElements,
            parse);
        descriptor.setArgDescriptors(argDescriptors);

        if (element.hasAttribute("method")) {
          parse
              .addProblem("attributes 'class' and 'method' indicate static method and also a 'constructor' element is specified for element 'object': "
                  + XmlUtil.toString(element));
        }
      }

    } else if (factoryObjectName != null) {
      descriptor.setFactoryObjectName(factoryObjectName);
      if (factoryElement != null) {
        parse
            .addProblem("element 'factory' is specified together with attribute 'factory' in element 'object': "
                + XmlUtil.toString(element));
      }

    } else if (factoryElement != null) {
      Element factoryDescriptorElement = XmlUtil.element(factoryElement);
      Descriptor factoryDescriptor = (Descriptor) parser.parseElement(
          factoryDescriptorElement, parse, WireParser.CATEGORY_DESCRIPTOR);
      descriptor.setFactoryDescriptor(factoryDescriptor);

    } else {
      parse
          .addProblem("element 'object' must have one of {attribute 'class', attribute 'factory' or element 'factory'}: "
              + XmlUtil.toString(element));
    }

    // method
    if (element.hasAttribute("method")) {
      descriptor.setMethodName(element.getAttribute("method"));

      List<Element> argElements = XmlUtil.elements(element, "arg");
      List<ArgDescriptor> argDescriptors = wireParser.parseArgs(argElements,
          parse);
      descriptor.setArgDescriptors(argDescriptors);
    } else if (className == null) {
      parse
          .addProblem("element 'object' with a element 'factory' or a attribute 'factory' must have a attribute 'method': "
              + XmlUtil.toString(element));
    }

    if ((className == null)
        && (XmlUtil.element(element, "constructor") != null)) {
      parse
          .addProblem("element 'object' with a element 'factory' or a attribute 'factory' can't have a 'constructor' element: "
              + XmlUtil.toString(element));
    }

    // read the operations elements
    List<Operation> operations = null;
    List<Element> elements = XmlUtil.elements(element);
    if (elements != null) {
      for (Element childElement : elements) {
        if (!childElement.getTagName().equals("constructor")
            && !childElement.getTagName().equals("factory")
            && !childElement.getTagName().equals("arg")) {
          Operation operation = (Operation) parser.parseElement(childElement,
              parse, WireParser.CATEGORY_OPERATION);
          if (operation != null) {
            if (operations == null) {
              operations = new ArrayList<Operation>();
            }
            operations.add(operation);
          } else {
            parse
                .addProblem("element 'object' can only have 'factory', 'arg', 'constructor' elements or an operation element ("
                    + parser.getBindings().getTagNames(
                        WireParser.CATEGORY_OPERATION)
                    + ")."
                    + " Invalid element '"
                    + childElement.getTagName()
                    + "' in: " + XmlUtil.toString(element));
          }
        }
      }
    }
    descriptor.setOperations(operations);

    // autowiring
    Boolean isAutoWireEnabled = XmlUtil.attributeBoolean(element, "auto-wire",
        false, parse);
    if (isAutoWireEnabled != null) {
      descriptor.setAutoWireEnabled(isAutoWireEnabled.booleanValue());
    }
    return descriptor;
  }
}