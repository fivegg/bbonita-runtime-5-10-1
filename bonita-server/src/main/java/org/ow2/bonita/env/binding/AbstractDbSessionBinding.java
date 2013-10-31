/**
 * Copyright (C) 2009  BonitaSoft S.A.
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

import org.ow2.bonita.env.descriptor.AbstractDbSessionDescriptor;
import org.ow2.bonita.env.xml.WireParser;
import org.ow2.bonita.util.ExceptionManager;
import org.ow2.bonita.util.xml.Parse;
import org.ow2.bonita.util.xml.Parser;
import org.ow2.bonita.util.xml.TagBinding;
import org.w3c.dom.Element;

/**
 *
 * @author Charles Souillard
 */
public abstract class AbstractDbSessionBinding extends TagBinding {
  
  public AbstractDbSessionBinding(final String tagName) {
    super(tagName, null, WireParser.CATEGORY_DESCRIPTOR);
  }
  
  abstract AbstractDbSessionDescriptor getDescriptor();
  
  public Object parse(Element element, Parse parse, Parser parser) {
    AbstractDbSessionDescriptor descriptor = getDescriptor();
    
    if (element.hasAttribute("session")) {
      descriptor.setSessionName(element.getAttribute("session"));
    } else {
    	String message = ExceptionManager.getInstance().getFullMessage("bse_QDSB_1", getTagName());
      throw new RuntimeException(message);
    }
    
    if (element.hasAttribute("name")) {
      descriptor.setName(element.getAttribute("name"));
    } else {
    	String message = ExceptionManager.getInstance().getFullMessage("bse_QDSB_2", getTagName());
      throw new RuntimeException(message);
    }
    return descriptor;
  }

}
