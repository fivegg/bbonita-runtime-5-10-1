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
 * 
 * Modified by Matthieu Chaffotte - BonitaSoft S.A.
 **/
package org.ow2.bonita.env.descriptor;

import org.ow2.bonita.env.Descriptor;
import org.ow2.bonita.env.WireContext;

/**
 * <p>
 * This {@link Descriptor} specifies a reference to an object. The object
 * referenced should be declared somewhere else in the wireContext.
 * </p>
 * 
 * <p>
 * The constructed object is the referenced object.
 * </p>
 * 
 * <p>
 * The {@link #init} field can be used to force initialization of the referenced
 * object.
 * </p>
 * 
 * @author Tom Baeyens
 * @author Guillaume Porcher (documentation)
 */
public class ReferenceDescriptor extends AbstractDescriptor implements Descriptor {

  private static final long serialVersionUID = 1L;

  String text = null;

  // TODO add a refExpression that is evaluated with el
  // the base referenced descriptor always should have delayedInitialization =
  // false;
  public ReferenceDescriptor() {
    super();
  }

  public ReferenceDescriptor(final String objectName) {
    super();
    setValue(objectName);
  }

  @Override
  public Object construct(final WireContext wireContext) {
    return wireContext.get(text, isDelayedInitializationAllowed());
  }

  public boolean isDelayedInitializationAllowed() {
    return init == INIT_EAGER || init == INIT_LAZY;
  }

  public void setValue(final String objectName) {
    text = objectName;
  }

}
