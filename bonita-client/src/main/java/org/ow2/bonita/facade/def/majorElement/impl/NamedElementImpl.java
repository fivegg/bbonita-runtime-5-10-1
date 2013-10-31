/**
 * Copyright (C) 2006  Bull S. A. S.
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
package org.ow2.bonita.facade.def.majorElement.impl;

import org.ow2.bonita.facade.def.majorElement.NamedElement;
import org.ow2.bonita.util.BonitaRuntimeException;
import org.ow2.bonita.util.Misc;


public abstract class NamedElementImpl extends DescriptionElementImpl implements NamedElement {

  private static final long serialVersionUID = -8929853717571177178L;
  private String name;
  private String label;

  protected NamedElementImpl() {
    super();
  }

  protected NamedElementImpl(final String name) {
    super();
    Misc.checkArgsNotNull(name);
    if (!Misc.isJavaIdentifier(name)) {
      throw new BonitaRuntimeException("Name: " + name + " is not a valid Java Identifier.");
    }
      
    this.name = name;
  }

  protected NamedElementImpl(NamedElement src) {
    super(src);
    Misc.checkArgsNotNull(src);
    this.name = src.getName();
    this.label = src.getLabel();
  }

  public void setLabel(final String label) {
    this.label = label;  
  }
  
  public String getName() {
    return this.name;
  }
  
  public String getLabel() {
    return this.label;
  }
}

