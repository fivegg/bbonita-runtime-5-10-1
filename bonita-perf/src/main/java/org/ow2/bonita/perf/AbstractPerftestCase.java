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
package org.ow2.bonita.perf;

import org.ow2.bonita.facade.ManagementAPI;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.util.AccessorUtil;
import org.ow2.bonita.util.BonitaRuntimeException;


public abstract class AbstractPerftestCase implements PerfTestCase {

  protected long id;

  public long getId() {
    return this.id;
  }
  public void setId(final long id) {
    this.id = id;
  }
  
  public void deploy() {
    final ManagementAPI managementAPI = AccessorUtil.getAPIAccessor().getManagementAPI();
    try {
      managementAPI.deploy(getBusinessArchive());
    } catch (final Exception e) {
      throw new BonitaRuntimeException("Error during deployment", e);
    }
  }

  public void undeploy() {
    try {
      final ProcessDefinitionUUID processUUID = AccessorUtil.getAPIAccessor().getQueryDefinitionAPI().getLastProcess(getProcessName()).getUUID();
      final ManagementAPI managementAPI = AccessorUtil.getAPIAccessor().getManagementAPI();
      managementAPI.deleteProcess(processUUID);
    } catch (final Exception e) {
      throw new BonitaRuntimeException("Error during undeployment", e);
    }
  }

}


