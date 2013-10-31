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
package org.ow2.bonita.facade.internal;

import java.rmi.RemoteException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.ow2.bonita.facade.exception.ActivityNotFoundException;
import org.ow2.bonita.facade.exception.InstanceNotFoundException;
import org.ow2.bonita.facade.exception.ProcessNotFoundException;
import org.ow2.bonita.facade.exception.VariableNotFoundException;
import org.ow2.bonita.facade.runtime.InitialAttachment;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;

/**
 * 
 * @author Elias Ricken de Medeiros
 *
 */
@Path("/API/repairAPI/")
@Produces({"text/*","application/xml"})
@Consumes({"application/x-www-form-urlencoded","text/*", "application/xml" })
public interface RESTRemoteRepairAPI extends AbstractRemoteRepairAPI {

	/**
	 * Create a new instance based on an existing one, being able to set its variables values. The activities located before the current activities are not executed.
   * @param instanceUUID the process instance UUID
   * @param processVariables the process variables in this Map will override the current process variable values
   * @param attachments the attachments in this Collection override the current attachments values
	 * @param options the options map (domain, queryList, user)
	 * @return
	 * @throws RemoteException
	 * @throws InstanceNotFoundException
	 * @throws VariableNotFoundException
	 */
	@POST	@Path("copyProcessInstance/{instanceUUID}")
  ProcessInstanceUUID copyProcessInstance(
      @PathParam("instanceUUID") ProcessInstanceUUID instanceUUID, 
  		@FormParam("processVariables") Map<String, Object> processVariables,
  		@FormParam("attachments") List<InitialAttachment> attachments, 
  		@FormParam("options") final Map<String, String> options)
	throws RemoteException, InstanceNotFoundException, VariableNotFoundException;

  /**
   * Create a new instance based on an existing one, being able to set its variables values. The activities located before the current activities are not executed.
   * @param instanceUUID the process instance UUID
   * @param processVariables the process variables in this Map will override the current process variable values
   * @param attachments the attachments in this Collection override the current attachments values
   * @param restoreVariableValuesAtDate restore the process variables and attachment values at this date
   * @param options the options map (domain, queryList, user)
   * @return the {@link ProcessInstanceUUID} of the copy
   * @throws RemoteException
   * @throws InstanceNotFoundException
   * @throws VariableNotFoundException
   */
  @POST	@Path("copyProcessInstanceByDate/{instanceUUID}")
  ProcessInstanceUUID copyProcessInstance(
      @PathParam("instanceUUID") ProcessInstanceUUID instanceUUID, 
  		@FormParam("processVariables") Map<String, Object> processVariables,
  		@FormParam("attachments") List<InitialAttachment> attachments, 
  		@FormParam("restoreVariableValuesAtDate") Date restoreVariableValuesAtDate,
  		@FormParam("options") final Map<String, String> options) 
  throws RemoteException, InstanceNotFoundException, VariableNotFoundException;

  /**
   * Starts a new process instance, positioned at specific activities, with a map of instance's variable values. The activities located before the start activities are not executed.
   * All connectors of specified activities will be executed normally (include connectors OnReady).
   * @param processDefinitionUUID the process definition UUID
   * @param processVariables the process variables in this Map will override the current process variable values
   * @param attachments the attachments in this Collection override the current attachments values
   * @param startActivitiesNames the list of activities on which to start an execution
   * @param options the options map (domain, queryList, user)
   * @return the {@link ProcessInstanceUUID} of the started instance
   * @throws RemoteException
   * @throws ProcessNotFoundException
   * @throws VariableNotFoundException
   * @throws ActivityNotFoundException
   */
  @POST	@Path("instantiateProcess/{processDefinitionUUID}")
  ProcessInstanceUUID instantiateProcess(
      @PathParam("processDefinitionUUID") ProcessDefinitionUUID processDefinitionUUID, 
  		@FormParam("processVariables") Map<String, Object> processVariables,
  		@FormParam("attachments") List<InitialAttachment> attachments, 
  		@FormParam("startActivitiesNames") List<String> startActivitiesNames,
  		@FormParam("options") final Map<String, String> options) 
  throws RemoteException, ProcessNotFoundException, VariableNotFoundException, ActivityNotFoundException;

  /**
   * Starts a new process instance, positioned at specific activities, with a map of instance's variable values. The activities located before the start activities are not executed.
   * All connectors of specified activities will be executed normally (include connectors OnReady).
   * @param processDefinitionUUID the process definition UUID
   * @param processVariables the process variables in this Map will override the current process variable values
   * @param attachments the attachments in this Collection override the current attachments values
   * @param startActivitiesNames the list of activities on which to start an execution
   * @param instanceInitiator the instance initiator of the process
   * @param options the options map (domain, queryList, user)
   * @return the {@link ProcessInstanceUUID} of the started instance
   * @throws RemoteException
   * @throws ProcessNotFoundException
   * @throws VariableNotFoundException
   * @throws ActivityNotFoundException
   */
  @POST @Path("instantiateProcessWithInstanceIniciator/{processDefinitionUUID}")
  ProcessInstanceUUID instantiateProcess(
      @PathParam("processDefinitionUUID") ProcessDefinitionUUID processDefinitionUUID, 
  		@FormParam("processVariables") Map<String, Object> processVariables,
  		@FormParam("attachments") List<InitialAttachment> attachments, 
  		@FormParam("startActivitiesNames") List<String> startActivitiesNames,
  		@FormParam("instanceInitiator") String instanceInitiator, 
  		@FormParam("options") final Map<String, String> options) 
  throws RemoteException, ProcessNotFoundException, VariableNotFoundException, ActivityNotFoundException;

}
