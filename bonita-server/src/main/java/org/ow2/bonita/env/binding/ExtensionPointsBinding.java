/**
 * Copyright (C) 20012 BonitaSoft S.A.
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

import org.ow2.bonita.env.EnvConstants;
import org.ow2.bonita.env.descriptor.ExtensionPointsDescriptor;
import org.ow2.bonita.util.xml.Parse;
import org.ow2.bonita.util.xml.Parser;
import org.ow2.bonita.util.xml.XmlUtil;
import org.w3c.dom.Element;

/**
 * 
 * @author Elias Ricken de Medeiros
 */
public class ExtensionPointsBinding extends WireDescriptorBinding {

  public ExtensionPointsBinding() {
    super(EnvConstants.EXTENSION_POINTS_TAG);
  }

  @Override
  public Object parse(final Element element, final Parse parse, final Parser parser) {
    final ExtensionPointsDescriptor desc = new ExtensionPointsDescriptor();
    final boolean throwExceptionOnFail = XmlUtil.attributeBoolean(element, EnvConstants.THROW_EXCEPTION_ON_FAILURE_ATTRIBUTE, true, parse);
    desc.setThrowExceptionOnFail(throwExceptionOnFail);
    return desc;
  }

}
