/**
 * Copyright (C) 2009  BonitaSoft S.A.
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
package org.ow2.bonita.facade.runtime.command;

import java.util.List;

import org.ow2.bonita.env.Environment;
import org.ow2.bonita.facade.APIAccessor;
import org.ow2.bonita.facade.QueryRuntimeAPI;
import org.ow2.bonita.facade.impl.StandardAPIAccessorImpl;
import org.ow2.bonita.light.LightProcessInstance;
import org.ow2.bonita.util.AccessorUtil;
import org.ow2.bonita.util.Command;

public class WebGetLightParentProcessInstances implements Command<List<LightProcessInstance>> {

	private static final long serialVersionUID = 4223637974438770807L;
	private int fromIndex;
	private int pageSize;
	private boolean searchInHistory;

	/**
	 * Default constructor.
	 * 
	 * @param userId
	 * @param fromIndex
	 * @param pageSize
	 */
	public WebGetLightParentProcessInstances(int fromIndex, int pageSize, boolean searchInHistory) {
		this.fromIndex = fromIndex;
		this.pageSize = pageSize;
		this.searchInHistory = searchInHistory;
	}

	public List<LightProcessInstance> execute(Environment environment) throws Exception {
		final APIAccessor accessor = new StandardAPIAccessorImpl();
		final QueryRuntimeAPI queryRuntimeAPI;
		if(this.searchInHistory) {
			queryRuntimeAPI = accessor.getQueryRuntimeAPI(AccessorUtil.QUERYLIST_HISTORY_KEY);
		} else {
			queryRuntimeAPI = accessor.getQueryRuntimeAPI(AccessorUtil.QUERYLIST_JOURNAL_KEY);
		}
		
		List<LightProcessInstance> result;
		result = queryRuntimeAPI.getLightParentProcessInstances(fromIndex, pageSize);
		return result;
	}

}
