/**
 * Copyright (C) 2007  Bull S. A. S.
 * Bull, Rue Jean Jaures, B.P.68, 78340, Les Clayes-sous-Bois
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

import java.util.List;

import org.ow2.bonita.env.descriptor.ArgDescriptor;
import org.ow2.bonita.env.descriptor.ObjectDescriptor;
import org.ow2.bonita.env.xml.WireParser;
import org.ow2.bonita.util.xml.Parse;
import org.ow2.bonita.util.xml.Parser;
import org.ow2.bonita.util.xml.TagBinding;
import org.ow2.bonita.util.xml.XmlUtil;
import org.w3c.dom.Element;

/**
 *
 * @author Marc Blachon, Guillaume Porcher, Charles Souillard, Miguel Valdes, Pierre Vigneras
 */
public class ImplementationBinding extends TagBinding {

  public ImplementationBinding(String tagName) {
    super(tagName, null, WireParser.CATEGORY_DESCRIPTOR);
  }
  
  public Object parse(Element element, Parse parse, Parser parser) {
    ObjectDescriptor descriptor = new ObjectDescriptor();
    if (!element.hasAttribute("class")) {
      parse.addProblem("element '" + XmlUtil.getTagLocalName(element)
          + " must have a 'class' attribute");
    }
    String type = element.getAttribute("class");
    descriptor.setClassName(type);
    
    List<Element> argElements = XmlUtil.elements(element, "arg");
    List<ArgDescriptor> argDescriptors = ((WireParser) parser).parseArgs(argElements, parse);
    descriptor.setArgDescriptors(argDescriptors);
    
    return descriptor;
  }

}
