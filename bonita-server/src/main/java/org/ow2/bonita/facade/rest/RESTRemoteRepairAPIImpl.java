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
package org.ow2.bonita.facade.rest;

import java.rmi.RemoteException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.ow2.bonita.facade.exception.ActivityNotFoundException;
import org.ow2.bonita.facade.exception.InstanceNotFoundException;
import org.ow2.bonita.facade.exception.ProcessNotFoundException;
import org.ow2.bonita.facade.exception.VariableNotFoundException;
import org.ow2.bonita.facade.impl.AbstractRemoteRepairAPIImpl;
import org.ow2.bonita.facade.internal.RESTRemoteRepairAPI;
import org.ow2.bonita.facade.runtime.InitialAttachment;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;

/**
 * 
 * @author Elias Ricken de Medeiros
 *
 */
public class RESTRemoteRepairAPIImpl extends AbstractRemoteRepairAPIImpl
		implements RESTRemoteRepairAPI {
	
	public ProcessInstanceUUID copyProcessInstance(
			ProcessInstanceUUID instanceUUID, Map<String, Object> processVariables,
			List<InitialAttachment> attachments, Map<String, String> options)
			throws RemoteException, InstanceNotFoundException,
			VariableNotFoundException {
		return getAPI(options).copyProcessInstance(instanceUUID, processVariables, attachments);
	}

	public ProcessInstanceUUID copyProcessInstance(
			ProcessInstanceUUID instanceUUID, Map<String, Object> processVariables,
			List<InitialAttachment> attachments, Date restoreVariableValuesAtDate,
			Map<String, String> options) throws RemoteException,
			InstanceNotFoundException, VariableNotFoundException {
		return getAPI(options).copyProcessInstance(instanceUUID, processVariables, attachments, restoreVariableValuesAtDate);
	}

	public ProcessInstanceUUID instantiateProcess(
			ProcessDefinitionUUID processDefinitionUUID,
			Map<String, Object> processVariables,
			List<InitialAttachment> attachments, List<String> startActivitiesNames,
			Map<String, String> options) throws RemoteException,
			ProcessNotFoundException, VariableNotFoundException,
			ActivityNotFoundException {
		return getAPI(options).instantiateProcess(processDefinitionUUID, processVariables, attachments, startActivitiesNames);
	}

	public ProcessInstanceUUID instantiateProcess(
			ProcessDefinitionUUID processDefinitionUUID,
			Map<String, Object> processVariables,
			List<InitialAttachment> attachments, List<String> startActivitiesNames,
			String instanceInitiator, Map<String, String> options)
			throws RemoteException, ProcessNotFoundException,
			VariableNotFoundException, ActivityNotFoundException {
		return getAPI(options).instantiateProcess(processDefinitionUUID, processVariables, attachments, startActivitiesNames, instanceInitiator);
	}

}
