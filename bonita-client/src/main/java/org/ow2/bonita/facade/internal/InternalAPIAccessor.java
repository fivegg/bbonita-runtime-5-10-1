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
package org.ow2.bonita.facade.internal;

import org.ow2.bonita.facade.CommandAPI;
import org.ow2.bonita.facade.IdentityAPI;
import org.ow2.bonita.facade.ManagementAPI;
import org.ow2.bonita.facade.RepairAPI;
import org.ow2.bonita.facade.RuntimeAPI;
import org.ow2.bonita.facade.WebAPI;

/**
 *  Helper class giving access to {@link CommandAPI}, {@link IdentityAPI}, {@link ManagementAPI}, 
 *  {@link RepairAPI}, {@link RuntimeAPI} and {@link WebAPI} interfaces.
 */
public interface InternalAPIAccessor extends InternalQueryAPIAccessor {

	public final String QUERYLIST_OPTION = "queryList";
	
  RuntimeAPI getRuntimeAPI(final String queryList);

  ManagementAPI getManagementAPI(final String queryList);

  CommandAPI getCommandAPI(final String queryList);

  WebAPI getWebAPI(final String queryList);

  IdentityAPI getIdentityAPI(final String queryList);

  RepairAPI getRepairAPI(final String queryList);

}
