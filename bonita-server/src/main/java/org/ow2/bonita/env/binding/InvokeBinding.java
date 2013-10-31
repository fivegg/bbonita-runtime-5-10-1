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

import java.util.List;

import org.ow2.bonita.env.descriptor.ArgDescriptor;
import org.ow2.bonita.env.operation.InvokeOperation;
import org.ow2.bonita.env.xml.WireParser;
import org.ow2.bonita.util.xml.Parse;
import org.ow2.bonita.util.xml.Parser;
import org.ow2.bonita.util.xml.XmlUtil;
import org.w3c.dom.Element;

/**
 * parses a descriptor for creating an {@link InvokeOperation invoke operation}.
 * 
 * See schema docs for more details.
 * 
 * @author Tom Baeyens
 * @author Guillaume Porcher (documentation)
 */
public class InvokeBinding extends WireOperationBinding {

  public InvokeBinding() {
    super("invoke");
  }

  public Object parse(Element element, Parse parse, Parser parser) {
    InvokeOperation invokeOperation = new InvokeOperation();
    if (element.hasAttribute("method")) {
      invokeOperation.setMethodName(element.getAttribute("method"));
    } else {
      parse
          .addProblem("invoke must have method : " + XmlUtil.toString(element));
    }
    List<Element> argElements = XmlUtil.elements(element, "arg");
    WireParser wireParser = (WireParser) parser;
    List<ArgDescriptor> argDescriptors = wireParser.parseArgs(argElements,
        parse);
    invokeOperation.setArgDescriptors(argDescriptors);
    return invokeOperation;
  }
}