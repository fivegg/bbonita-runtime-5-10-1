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

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.ow2.bonita.facade.exception.ActivityNotFoundException;
import org.ow2.bonita.facade.exception.InstanceNotFoundException;
import org.ow2.bonita.facade.exception.VariableNotFoundException;
import org.ow2.bonita.facade.uuid.ActivityInstanceUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;

/**
 * 
 * @author Elias Ricken de Medeiros
 *
 */
@Path("/API/repairAPI/")
@Produces({"text/*","application/xml"})
@Consumes({"application/x-www-form-urlencoded","text/*", "application/xml" })
public interface AbstractRemoteRepairAPI extends Remote {

	/**
	 * Starts a new execution in a process instance at a specific activity, with a map of instance’s variable values.
	 * All connectors of specified activity will be executed normally (include connectors OnReady).
   * @param instanceUUID the process instance UUID
   * @param activityName the activity to execute
	 * @param options the options map (domain, queryList, user)
	 * @return the {@link ActivityInstanceUUID} of the activity
	 * @throws RemoteException
	 * @throws InstanceNotFoundException
	 * @throws ActivityNotFoundException
	 * @throws VariableNotFoundException
	 */
	@POST @Path("startExecution/{instanceUUID}/{activityName}")
  ActivityInstanceUUID startExecution(
  		@PathParam("instanceUUID") ProcessInstanceUUID instanceUUID, 
  		@PathParam("activityName") String activityName, 
  		@FormParam("options") final Map<String, String> options) 
	throws RemoteException, InstanceNotFoundException, ActivityNotFoundException, VariableNotFoundException;
  
	/**
	 * Stops all the active executions for an activity of a given instance.
   * @param instanceUUID the process instance UUID
   * @param activityName the name of the activity whose executions should be stopped stops
	 * @param options the options map (domain, queryList, user)
	 * @throws RemoteException
	 * @throws InstanceNotFoundException
	 * @throws ActivityNotFoundException
	 */
	@POST @Path("stopExecution/{instanceUUID}/{activityName}")
  void stopExecution(
  		@PathParam("instanceUUID") ProcessInstanceUUID instanceUUID, 
  		@PathParam("activityName") String activityName, 
  		@FormParam("options") final Map<String, String> options) 
	throws RemoteException, InstanceNotFoundException, ActivityNotFoundException;

}
