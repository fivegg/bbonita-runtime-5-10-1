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

import java.util.Collection;
import java.util.Map;

import org.ow2.bonita.facade.exception.InstanceNotFoundException;
import org.ow2.bonita.facade.exception.ProcessNotFoundException;
import org.ow2.bonita.facade.exception.UncancellableInstanceException;
import org.ow2.bonita.facade.exception.UndeletableInstanceException;
import org.ow2.bonita.facade.exception.VariableNotFoundException;
import org.ow2.bonita.facade.internal.RemoteRuntimeAPI;
import org.ow2.bonita.facade.runtime.InitialAttachment;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;

/**
 * 
 * @author Elias Ricken de Medeiros
 * 
 */
public class RemoteRuntimeAPIImpl extends AbstractRemoteRuntimeAPIImpl implements RemoteRuntimeAPI {

  @Override
  public void deleteAllProcessInstances(final Collection<ProcessDefinitionUUID> processUUIDs,
      final Map<String, String> options) throws ProcessNotFoundException, UndeletableInstanceException {
    getAPI(options).deleteAllProcessInstances(processUUIDs);
  }

  @Override
  public void deleteProcessInstances(final Collection<ProcessInstanceUUID> instanceUUIDs,
      final Map<String, String> options) throws InstanceNotFoundException, UndeletableInstanceException {
    getAPI(options).deleteProcessInstances(instanceUUIDs);
  }

  @Override
  public void cancelProcessInstances(final Collection<ProcessInstanceUUID> instanceUUIDs,
      final Map<String, String> options) throws InstanceNotFoundException, UncancellableInstanceException {
    getAPI(options).cancelProcessInstances(instanceUUIDs);
  }

  @Override
  public ProcessInstanceUUID instantiateProcess(final ProcessDefinitionUUID processUUID,
      final Map<String, Object> variables, final Collection<InitialAttachment> attachments,
      final Map<String, String> options) throws ProcessNotFoundException, VariableNotFoundException {
    return getAPI(options).instantiateProcess(processUUID, variables, attachments);
  }

}
