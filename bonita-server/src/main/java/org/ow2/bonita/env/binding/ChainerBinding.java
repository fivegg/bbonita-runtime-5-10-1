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

import org.ow2.bonita.env.descriptor.ArgDescriptor;
import org.ow2.bonita.env.descriptor.ListDescriptor;
import org.ow2.bonita.env.descriptor.ObjectDescriptor;
import org.ow2.bonita.env.xml.WireParser;
import org.ow2.bonita.util.Misc;
import org.ow2.bonita.util.xml.Parse;
import org.ow2.bonita.util.xml.Parser;
import org.ow2.bonita.util.xml.TagBinding;
import org.w3c.dom.Element;

/**
 *
 * @author Marc Blachon, Guillaume Porcher, Charles Souillard, Miguel Valdes, Pierre Vigneras
 */
public class ChainerBinding extends TagBinding {

  public ChainerBinding() {
    super("chainer", null, WireParser.CATEGORY_DESCRIPTOR);
  }
  
  public Object parse(final Element element, final Parse parse, final Parser parser) {
    final ObjectDescriptor chainDescriptor = new ObjectDescriptor();

    final ListDescriptor listDescriptor = (ListDescriptor) new ListBinding().parse(element, parse, parser);
    final ArgDescriptor argDescriptor = new ArgDescriptor();
    argDescriptor.setDescriptor(listDescriptor);

    chainDescriptor.setClassName(Misc.class.getName());
    chainDescriptor.setMethodName("getChainOf");
    chainDescriptor.addArgDescriptor(argDescriptor);

    return chainDescriptor;
  }

}
