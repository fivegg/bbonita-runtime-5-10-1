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
package org.ow2.bonita.multitenancy;

import java.util.HashMap;
import java.util.Map;

import javax.security.auth.login.LoginContext;

import junit.framework.Assert;

import org.ow2.bonita.facade.ManagementAPI;
import org.ow2.bonita.facade.QueryDefinitionAPI;
import org.ow2.bonita.facade.QueryRuntimeAPI;
import org.ow2.bonita.facade.RuntimeAPI;
import org.ow2.bonita.facade.def.element.BusinessArchive;
import org.ow2.bonita.facade.runtime.InstanceState;
import org.ow2.bonita.facade.runtime.ProcessInstance;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.util.AccessorUtil;

/**
 * @author Alejandro Guizar
 */
public class MultiTenantThread extends Thread {

  private String tenantID;
  private Throwable t;
  private boolean finished = false;

  public MultiTenantThread(final String tenantID) {
    this.tenantID = tenantID;
  }

  public void run() {
    try {
      //get all used APIs
      final ManagementAPI managementAPI = AccessorUtil.getManagementAPI();
      final RuntimeAPI runtimeAPI = AccessorUtil.getRuntimeAPI();
      final QueryRuntimeAPI queryRuntimeAPI = AccessorUtil.getQueryRuntimeAPI();
      final QueryDefinitionAPI queryDefinitionAPI = AccessorUtil.getQueryDefinitionAPI();

      final BusinessArchive businessArchive = MultiTenancyTest.getBusinessArchive();
      final ProcessDefinitionUUID processUUID = businessArchive.getProcessUUID();

      final Map<String, Object> variables = new HashMap<String, Object>();
      variables.put("newValue", this.tenantID);

      LoginContext loginContext = MultiTenancyTest.login(tenantID);

      managementAPI.deploy(businessArchive);
      Assert.assertNotNull(queryDefinitionAPI.getLightProcess(processUUID));
      final ProcessInstanceUUID instance1UUID = runtimeAPI.instantiateProcess(processUUID, variables);
      final ProcessInstance instance1 = queryRuntimeAPI.getProcessInstance(instance1UUID);
      Assert.assertEquals(1, instance1.getNb());

      MultiTenancyTest.waitForInstanceEnd(5000, 200, instance1UUID);
      Assert.assertEquals(InstanceState.FINISHED, queryRuntimeAPI.getLightProcessInstance(instance1UUID).getInstanceState());

      final String dataValue = (String) queryRuntimeAPI.getProcessInstanceVariable(instance1UUID, "data");
      Assert.assertEquals(this.tenantID, dataValue);

      managementAPI.deleteProcess(processUUID);

      loginContext.logout();
    } catch (Throwable t) {
      this.t = t;
      t.printStackTrace();
    } finally {
      finished = true;
    }
  }

  public boolean isFinished() {
    return finished;
  }

  public Throwable getThrowable() {
    return t;
  }
  
}
