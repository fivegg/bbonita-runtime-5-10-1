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
package org.ow2.bonita.facade.impl;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.ow2.bonita.facade.exception.ActivityNotFoundException;
import org.ow2.bonita.facade.exception.InstanceNotFoundException;
import org.ow2.bonita.facade.exception.ProcessNotFoundException;
import org.ow2.bonita.facade.exception.VariableNotFoundException;
import org.ow2.bonita.facade.internal.RemoteRepairAPI;
import org.ow2.bonita.facade.runtime.InitialAttachment;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;

/**
 * 
 * @author Elias Ricken de Medeiros
 * 
 */
public class RemoteRepairAPIImpl extends AbstractRemoteRepairAPIImpl implements RemoteRepairAPI {

  @Override
  public ProcessInstanceUUID copyProcessInstance(final ProcessInstanceUUID instanceUUID,
      final Map<String, Object> processVariables, final Collection<InitialAttachment> attachments,
      final Map<String, String> options) throws RemoteException, InstanceNotFoundException, VariableNotFoundException {
    return getAPI(options).copyProcessInstance(instanceUUID, processVariables, attachments);
  }

  @Override
  public ProcessInstanceUUID copyProcessInstance(final ProcessInstanceUUID instanceUUID,
      final Map<String, Object> processVariables, final Collection<InitialAttachment> attachments,
      final Date restoreVariableValuesAtDate, final Map<String, String> options) throws RemoteException,
      InstanceNotFoundException, VariableNotFoundException {
    return getAPI(options)
        .copyProcessInstance(instanceUUID, processVariables, attachments, restoreVariableValuesAtDate);
  }

  @Override
  public ProcessInstanceUUID instantiateProcess(final ProcessDefinitionUUID processDefinitionUUID,
      final Map<String, Object> processVariables, final Collection<InitialAttachment> attachments,
      final List<String> startActivitiesNames, final Map<String, String> options) throws RemoteException,
      ProcessNotFoundException, VariableNotFoundException, ActivityNotFoundException {
    return getAPI(options).instantiateProcess(processDefinitionUUID, processVariables, attachments,
        startActivitiesNames);
  }

  @Override
  public ProcessInstanceUUID instantiateProcess(final ProcessDefinitionUUID processDefinitionUUID,
      final Map<String, Object> processVariables, final Collection<InitialAttachment> attachments,
      final List<String> startActivitiesNames, final String instanceInitiator, final Map<String, String> options)
      throws RemoteException, ProcessNotFoundException, VariableNotFoundException, ActivityNotFoundException {
    return getAPI(options).instantiateProcess(processDefinitionUUID, processVariables, attachments,
        startActivitiesNames, instanceInitiator);
  }

}
