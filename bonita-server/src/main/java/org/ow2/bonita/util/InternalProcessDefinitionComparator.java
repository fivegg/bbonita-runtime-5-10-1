/**
 * Copyright (C) 2010-2012 BonitaSoft S.A.
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
package org.ow2.bonita.util;

import java.util.Comparator;

import org.ow2.bonita.facade.def.InternalProcessDefinition;
import org.ow2.bonita.facade.paging.ProcessDefinitionCriterion;

/**
 * @author Elias Ricken de Medeiros
 * 
 */
public class InternalProcessDefinitionComparator implements Comparator<InternalProcessDefinition> {

  private final ProcessDefinitionCriterion pagingCriterion;

  public InternalProcessDefinitionComparator(final ProcessDefinitionCriterion pagingCriterion) {
    this.pagingCriterion = pagingCriterion;
  }

  @Override
  public int compare(final InternalProcessDefinition o1, final InternalProcessDefinition o2) {
    switch (this.pagingCriterion) {
    case NAME_ASC:
      return o1.getName().compareTo(o2.getName());

    case LABEL_ASC:
      if (o1.getLabel() == null) {
        if (o2.getLabel() == null) {
          return 0;
        } else {
          return -1;
        }
      }
      return o1.getLabel().compareTo(o2.getLabel());

    case VERSION_ASC:
      return o1.getVersion().compareTo(o2.getVersion());

    case STATE_ASC:
      return o1.getState().toString().compareTo(o2.getState().toString());

    case NAME_DESC:
      return o2.getName().compareTo(o1.getName());

    case LABEL_DESC:
      if (o2.getLabel() == null) {
        if (o1.getLabel() == null) {
          return 0;
        } else {
          return -1;
        }
      }
      return o2.getLabel().compareTo(o1.getLabel());

    case VERSION_DESC:
      return o2.getVersion().compareTo(o1.getVersion());

    case STATE_DESC:
      return o2.getState().toString().compareTo(o1.getState().toString());

    case DEFAULT:
      return o1.getLabelOrName().compareTo(o2.getLabelOrName());
    }
    return 0;
  }

}
