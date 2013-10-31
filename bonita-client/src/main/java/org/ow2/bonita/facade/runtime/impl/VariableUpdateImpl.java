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
package org.ow2.bonita.facade.runtime.impl;

import java.io.Serializable;
import java.util.Date;

import org.ow2.bonita.facade.runtime.VariableUpdate;
import org.ow2.bonita.util.Misc;

/**
 * @author Guillaume Porcher
 *
 */
public class VariableUpdateImpl implements VariableUpdate {

  private static final long serialVersionUID = -2329602889979468039L;
  protected long date;
  protected String userId;
  protected String name;
  protected Serializable clientVariable;
  
  protected VariableUpdateImpl() { }

  public VariableUpdateImpl(VariableUpdate src) {
    this.date = Misc.getTime(src.getDate());
    this.userId = src.getUserId();
    this.name = src.getName();
    this.clientVariable = src.getValue();
  }
  
  public Date getDate() {
    return Misc.getDate(date);
  }

  public String getName() {
    return name;
  }

  public String getUserId() {
    return userId;
  }
  
  private Serializable getClientVariable() {
	  return this.clientVariable;
  }
  
  public Serializable getValue() {
    return getClientVariable();
  }
  
  public String toString() {
    String st = this.getClass().getName() + ": ";
    st += "name='" + name + "', ";
    st += "value='" + getValue() + "', ";
    st += "date='" + date + "', ";
    st += "userId='" + userId + "'";
    return st;
  }
}
