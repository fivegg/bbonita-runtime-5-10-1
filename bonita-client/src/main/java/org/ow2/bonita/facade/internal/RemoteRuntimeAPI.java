/**
 * Copyright (C) 2007  Bull S. A. S.
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
 * 
 * Modified by Matthieu Chaffotte, Elias Ricken de Medeiros - BonitaSoft S.A.
 **/
package org.ow2.bonita.facade.internal;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Map;

import org.ow2.bonita.facade.exception.EventNotFoundException;
import org.ow2.bonita.facade.exception.InstanceNotFoundException;
import org.ow2.bonita.facade.exception.ProcessNotFoundException;
import org.ow2.bonita.facade.exception.UncancellableInstanceException;
import org.ow2.bonita.facade.exception.UndeletableInstanceException;
import org.ow2.bonita.facade.exception.VariableNotFoundException;
import org.ow2.bonita.facade.runtime.InitialAttachment;
import org.ow2.bonita.facade.uuid.CatchingEventUUID;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;

/**
 * For internal use only.
 */
public interface RemoteRuntimeAPI extends AbstractRemoteRuntimeAPI {

	void cancelProcessInstances(
			Collection<ProcessInstanceUUID> instanceUUIDs,
			final Map<String, String> options)
	throws InstanceNotFoundException, UncancellableInstanceException, RemoteException;

	ProcessInstanceUUID instantiateProcess(
      ProcessDefinitionUUID processUUID,
      Map<String, Object> variables,
  		Collection<InitialAttachment> attachments,
  		final Map<String, String> options)
	throws ProcessNotFoundException, RemoteException, VariableNotFoundException;

	void deleteProcessInstances(
      Collection<ProcessInstanceUUID> instanceUUIDs,
  		final Map<String, String> options)
	throws InstanceNotFoundException, UndeletableInstanceException, RemoteException;

  void deleteAllProcessInstances(
      Collection<ProcessDefinitionUUID> processUUIDs,
  		final Map<String, String> options)
  throws ProcessNotFoundException,UndeletableInstanceException, RemoteException;

  public void deleteEvents(
      Collection<CatchingEventUUID> eventUUIDs,
      final Map<String, String> options)
  throws EventNotFoundException, RemoteException;
}
