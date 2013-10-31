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
package org.ow2.bonita.definition;

import java.util.List;

import org.ow2.bonita.util.ExceptionManager;
import org.ow2.bonita.util.Misc;

/**
 * Class returned by the MultiInstantiator interface and therefore by the classes implementing
 * it written for the need of the end user application.
 * @author Guillaume Porcher
 *
 */
public class MultiInstantiatorDescriptor {

  protected List<Object> variableValues;
  protected int joinNumber;

  protected MultiInstantiatorDescriptor() { }

  public MultiInstantiatorDescriptor(final int joinNumber, final List<Object> variableValues) {
    Misc.checkArgsNotNull(variableValues);
    String message = ExceptionManager.getInstance().getFullMessage("ba_MID_1");
    Misc.badStateIfTrue(joinNumber <= 0, message);
    message = ExceptionManager.getInstance().getFullMessage("ba_MID_2");
    Misc.badStateIfTrue(joinNumber > variableValues.size(), message);
    this.joinNumber = joinNumber;
    this.variableValues = variableValues;
  }

  public List<Object> getVariableValues() {
    return variableValues;
  }

  public int getJoinNumber() {
    return joinNumber;
  }

}
