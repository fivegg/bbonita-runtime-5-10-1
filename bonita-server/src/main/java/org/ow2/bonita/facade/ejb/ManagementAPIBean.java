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
 * 
 * Modified by Matthieu Chaffotte, Elias Ricken de Medeiros - BonitaSoft S.A.
 **/
package org.ow2.bonita.facade.ejb;

import java.rmi.RemoteException;

import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

import org.ow2.bonita.facade.impl.RemoteManagementAPIImpl;

public class ManagementAPIBean extends RemoteManagementAPIImpl implements SessionBean {

  private static final long serialVersionUID = 7816343722790053864L;

  protected SessionContext ctx = null;

  public void ejbCreate() {
  }

  public void ejbActivate() throws RemoteException {
  }

  public void ejbPassivate() throws RemoteException {
  }

  public void ejbRemove() throws RemoteException {
  }

  public void setSessionContext(final SessionContext arg0) throws RemoteException {
    this.ctx = arg0;
  }

}
