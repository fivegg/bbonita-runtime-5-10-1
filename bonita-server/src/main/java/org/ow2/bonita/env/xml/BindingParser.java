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
package org.ow2.bonita.env.xml;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ow2.bonita.util.ReflectUtil;
import org.ow2.bonita.util.xml.Binding;
import org.ow2.bonita.util.xml.Bindings;
import org.ow2.bonita.util.xml.Parse;
import org.ow2.bonita.util.xml.Parser;
import org.ow2.bonita.util.xml.XmlUtil;
import org.w3c.dom.Element;

public class BindingParser extends Parser {

	static final Logger LOG = Logger.getLogger(BindingParser.class.getName());

  public Object parseDocumentElement(Element documentElement, Parse parse) {
    List<Element> elements = XmlUtil.elements(documentElement, "binding");
    if (elements != null) {
      for (Element bindingElement : elements) {
        String bindingClassName = XmlUtil.attribute(bindingElement, "class");

        if (LOG.isLoggable(Level.FINE)) {
          LOG.fine("adding wire binding for " + bindingClassName);
        }

        Binding binding = null;
        if (bindingClassName != null) {
          try {
            Class<?> bindingClass = ReflectUtil.loadClass(classLoader,
                bindingClassName);
            binding = (Binding) bindingClass.newInstance();
          } catch (Exception e) {
            parse.addProblem(
                "couldn't instantiate binding " + bindingClassName, e);
          }
        } else {
          parse.addProblem("class is a required attribute in a binding "
              + XmlUtil.toString(bindingElement));
        }

        if (binding != null) {
          Bindings bindings = parse.findObject(Bindings.class);
          bindings.addBinding(binding);
        } else {
        	if (LOG.isLoggable(Level.INFO)) {
            LOG.info("WARNING: binding for " + bindingClassName
              + " could not be parsed. See parsing problems for more details.");
        	}
        }
      }
    }

    return null;
  }
}
