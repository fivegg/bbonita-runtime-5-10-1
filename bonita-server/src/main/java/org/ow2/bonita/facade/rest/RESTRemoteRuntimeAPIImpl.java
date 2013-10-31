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

import java.lang.reflect.Proxy;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

import org.ow2.bonita.facade.APIAccessor;
import org.ow2.bonita.facade.RuntimeAPI;
import org.ow2.bonita.facade.exception.DocumentationCreationException;
import org.ow2.bonita.facade.exception.EventNotFoundException;
import org.ow2.bonita.facade.exception.InstanceNotFoundException;
import org.ow2.bonita.facade.exception.ProcessNotFoundException;
import org.ow2.bonita.facade.exception.UncancellableInstanceException;
import org.ow2.bonita.facade.exception.UndeletableInstanceException;
import org.ow2.bonita.facade.exception.VariableNotFoundException;
import org.ow2.bonita.facade.impl.AbstractRemoteRuntimeAPIImpl;
import org.ow2.bonita.facade.impl.StandardAPIAccessorImpl;
import org.ow2.bonita.facade.internal.RESTRemoteRuntimeAPI;
import org.ow2.bonita.facade.runtime.Document;
import org.ow2.bonita.facade.runtime.InitialAttachment;
import org.ow2.bonita.facade.uuid.CatchingEventUUID;
import org.ow2.bonita.facade.uuid.DocumentUUID;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;

/**
 * 
 * @author Elias Ricken de Medeiros, Matthieu Chaffotte
 *
 */
public class RESTRemoteRuntimeAPIImpl extends AbstractRemoteRuntimeAPIImpl implements RESTRemoteRuntimeAPI {

	protected void putAPI(String queryList) {
		RuntimeAPI runtimeAPI = new StandardAPIAccessorImpl().getRuntimeAPI(queryList);
		// add REST interceptor
		final RESTServerAPIInterceptor restInterceptor = new RESTServerAPIInterceptor(runtimeAPI);
		Class<RuntimeAPI> clazz = RuntimeAPI.class;
		runtimeAPI = clazz.cast(Proxy.newProxyInstance(APIAccessor.class.getClassLoader(), new Class[] { clazz }, restInterceptor));
		apis.put(queryList, runtimeAPI);
	}

	public void cancelProcessInstances(List<ProcessInstanceUUID> instanceUUIDs, Map<String, String> options)
	throws InstanceNotFoundException, UncancellableInstanceException, RemoteException {
		getAPI(options).cancelProcessInstances(instanceUUIDs);		
	}

	public void deleteAllProcessInstances(List<ProcessDefinitionUUID> processUUIDs, Map<String, String> options)
	throws ProcessNotFoundException, UndeletableInstanceException, RemoteException {
		getAPI(options).deleteAllProcessInstances(processUUIDs);
	}

	public void deleteProcessInstances(List<ProcessInstanceUUID> instanceUUIDs, Map<String, String> options)
	throws InstanceNotFoundException, UndeletableInstanceException, RemoteException {
		getAPI(options).deleteProcessInstances(instanceUUIDs);
	}

	public ProcessInstanceUUID instantiateProcess(ProcessDefinitionUUID processUUID, Map<String, Object> variables,
			List<InitialAttachment> attachments, Map<String, String> options)
	throws ProcessNotFoundException, RemoteException, VariableNotFoundException {
		return getAPI(options).instantiateProcess(processUUID, variables, attachments);
	}

  public void deleteEvents(List<CatchingEventUUID> eventUUIDs, Map<String, String> options)
  throws EventNotFoundException, RemoteException {
    getAPI(options).deleteEvents(eventUUIDs);
  }

  @Override
  public Document createDocumentOctetStream(String name,
      ProcessDefinitionUUID processDefinitionUUID, String fileName,
      String mimeType, byte[] content, Map<String, String> options)
      throws RemoteException, DocumentationCreationException,
      ProcessNotFoundException {
    return getAPI(options).createDocument(name, processDefinitionUUID, fileName, mimeType, content);
  }

  @Override
  public Document createDocumentOctetStream(String name,
      ProcessInstanceUUID instanceUUID, String fileName, String mimeType,
      byte[] content, Map<String, String> options) throws RemoteException,
      DocumentationCreationException, InstanceNotFoundException {
    return getAPI(options).createDocument(name, instanceUUID, fileName, mimeType, content);
  }

  @Override
  public Document addDocumentVersionOctetStream(DocumentUUID documentUUID,
      boolean isMajorVersion, String fileName, String mimeType, byte[] content,
      Map<String, String> options) throws RemoteException,
      DocumentationCreationException {
    return getAPI(options).addDocumentVersion(documentUUID, isMajorVersion, fileName, mimeType, content);
  }

  @SuppressWarnings("deprecation")
  @Override
  public void addAttachmentOctetStream(ProcessInstanceUUID instanceUUID,
      String name, String fileName, byte[] value, Map<String, String> options)
      throws RemoteException {
    getAPI(options).addAttachment(instanceUUID, name, fileName, value);
  }

}
