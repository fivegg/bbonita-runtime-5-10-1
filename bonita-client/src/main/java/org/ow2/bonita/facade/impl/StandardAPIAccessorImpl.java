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
package org.ow2.bonita.facade.impl;

import org.ow2.bonita.facade.APIAccessor;
import org.ow2.bonita.facade.CommandAPI;
import org.ow2.bonita.facade.IdentityAPI;
import org.ow2.bonita.facade.ManagementAPI;
import org.ow2.bonita.facade.RepairAPI;
import org.ow2.bonita.facade.RuntimeAPI;
import org.ow2.bonita.facade.WebAPI;
import org.ow2.bonita.facade.internal.InternalAPIAccessor;
import org.ow2.bonita.util.AccessorUtil;

public class StandardAPIAccessorImpl extends StandardQueryAPIAccessorImpl implements APIAccessor {

  private final InternalAPIAccessor serverAccessor = LocalAPIAccessorFactory.getStandardServerAPIAccessor();

  public RuntimeAPI getRuntimeAPI() {
  	return getRuntimeAPI(AccessorUtil.QUERYLIST_DEFAULT_KEY);
  }
  
  public RuntimeAPI getRuntimeAPI(final String queryList) {
  	return this.serverAccessor.getRuntimeAPI(queryList);
  }

  public ManagementAPI getManagementAPI() {
    return getManagementAPI(AccessorUtil.QUERYLIST_DEFAULT_KEY);
  }

  public ManagementAPI getManagementAPI(final String queryList) {
    return this.serverAccessor.getManagementAPI(queryList);
  }
  
  public CommandAPI getCommandAPI() {
  	return getCommandAPI(AccessorUtil.QUERYLIST_DEFAULT_KEY);
  }

  public CommandAPI getCommandAPI(final String queryList) {
  	return this.serverAccessor.getCommandAPI(queryList);
  }
  
  public WebAPI getWebAPI() {
  	return getWebAPI(AccessorUtil.QUERYLIST_DEFAULT_KEY);
  }

  public WebAPI getWebAPI(final String queryList) {
  	return this.serverAccessor.getWebAPI(queryList);
  }
  
  public IdentityAPI getIdentityAPI() {
  	return getIdentityAPI(AccessorUtil.QUERYLIST_DEFAULT_KEY);
  }
  
  public IdentityAPI getIdentityAPI(final String queryList) {
  	return this.serverAccessor.getIdentityAPI(queryList);
  }
  
  public RepairAPI getRepairAPI() {
  	return getRepairAPI(AccessorUtil.QUERYLIST_DEFAULT_KEY);
  }
  
  public RepairAPI getRepairAPI(final String queryList) {
  	return this.serverAccessor.getRepairAPI(queryList);
  }

}
