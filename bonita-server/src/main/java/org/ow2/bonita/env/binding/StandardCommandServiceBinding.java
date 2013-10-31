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

import org.ow2.bonita.env.Descriptor;
import org.ow2.bonita.env.descriptor.CommandServiceDescriptor;
import org.ow2.bonita.env.xml.WireParser;
import org.ow2.bonita.services.CommandService;
import org.ow2.bonita.services.impl.DefaultCommandService;
import org.ow2.bonita.util.xml.Parse;
import org.ow2.bonita.util.xml.Parser;
import org.ow2.bonita.util.xml.XmlUtil;
import org.w3c.dom.Element;

/**
 * parses a descriptor for a creating {@link DefaultCommandService}.
 * 
 * See schema docs for more details.
 * 
 * @author Tom Baeyens
 */
public class StandardCommandServiceBinding extends WireDescriptorBinding {

  public StandardCommandServiceBinding() {
    super("command-service");
  }

  protected StandardCommandServiceBinding(String tagName) {
    super(tagName);
  }

  public Object parse(Element element, Parse parse, Parser parser) {
    CommandServiceDescriptor commandServiceDescriptor = new CommandServiceDescriptor();

    CommandService commandService = getCommandService(element, parse, parser);
    commandServiceDescriptor.setCommandService(commandService);

    List<Element> interceptorElements = XmlUtil.elements(element);
    if (interceptorElements != null) {
      for (Element interceptorElement : interceptorElements) {
        Descriptor interceptorDescriptor = (Descriptor) parser.parseElement(interceptorElement, parse, WireParser.CATEGORY_INTERCEPTOR);
        commandServiceDescriptor.addInterceptorDescriptor(interceptorDescriptor);
      }
    }

    return commandServiceDescriptor;
  }

  protected CommandService getCommandService(Element element, Parse parse,
      Parser parser) {
    return new DefaultCommandService();
  }

}
