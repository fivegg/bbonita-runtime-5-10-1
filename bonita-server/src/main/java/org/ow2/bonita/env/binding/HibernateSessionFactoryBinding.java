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
import org.ow2.bonita.env.descriptor.HibernateSessionFactoryDescriptor;
import org.ow2.bonita.util.xml.Parse;
import org.ow2.bonita.util.xml.Parser;
import org.ow2.bonita.util.xml.XmlUtil;
import org.w3c.dom.Element;

/**
 * parses a descriptor for creating a hibernate SessionFactory.
 * 
 * See schema docs for more details.
 * 
 * @author Tom Baeyens
 */
public class HibernateSessionFactoryBinding extends
    HibernateConfigurationBinding {

  public HibernateSessionFactoryBinding() {
    super("hibernate-session-factory");
  }

  public Object parse(Element element, Parse parse, Parser parser) {
    HibernateSessionFactoryDescriptor descriptor = new HibernateSessionFactoryDescriptor();

    if (element.hasAttribute("configuration")) {
      descriptor.setConfigurationName(element.getAttribute("configuration"));

      // if hibernate session factory binding has sub elements, we assume that
      // it is an inline configuration declaration
    } else if (XmlUtil.elements(element) != null) {
      Descriptor configurationDescriptor = (Descriptor) super.parse(element,
          parse, parser);
      descriptor.setConfigurationDescriptor(configurationDescriptor);
    }

    return descriptor;
  }
}
