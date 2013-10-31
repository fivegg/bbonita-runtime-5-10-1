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
package org.ow2.bonita.parsing.connector.binding;

import org.ow2.bonita.connector.core.desc.Getter;
import org.ow2.bonita.parsing.def.binding.ElementBinding;
import org.ow2.bonita.util.xml.Parse;
import org.ow2.bonita.util.xml.Parser;
import org.w3c.dom.Element;

/**
 * @author Mickael Istria
 *
 */
public class PropertyBinding extends ElementBinding {

	public PropertyBinding() {
		super("property");
	}

	/* (non-Javadoc)
	 * @see org.ow2.bonita.util.xml.Binding#parse(org.w3c.dom.Element, org.ow2.bonita.util.xml.Parse, org.ow2.bonita.util.xml.Parser, java.lang.String)
	 */
	public Object parse(Element element, Parse parse, Parser parser) {
	    final Getter getter = parse.findObject(Getter.class);
	    getter.getMetadata().put(element.getAttribute("name"), element.getAttribute("value"));
	    return null;
	}

}
