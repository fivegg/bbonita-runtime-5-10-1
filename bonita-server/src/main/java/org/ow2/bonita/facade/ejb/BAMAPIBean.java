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
package org.ow2.bonita.facade.ejb;

import java.rmi.RemoteException;

import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

import org.ow2.bonita.facade.impl.RemoteBAMAPIImpl;

public class BAMAPIBean extends RemoteBAMAPIImpl implements SessionBean {

  private static final long serialVersionUID = 8977547453867812345L;
  
  protected SessionContext ctx = null;

  public void ejbCreate() {
  }
  
  public void ejbActivate() throws RemoteException {

  }

  public void ejbPassivate() throws RemoteException {

  }

  public void ejbRemove() throws RemoteException {

  }

  public void setSessionContext(SessionContext arg0) throws RemoteException {
    this.ctx = arg0;
  }
}
