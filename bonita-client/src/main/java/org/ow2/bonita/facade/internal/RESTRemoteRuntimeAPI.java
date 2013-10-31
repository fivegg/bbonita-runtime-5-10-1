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
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.ow2.bonita.facade.exception.DocumentationCreationException;
import org.ow2.bonita.facade.exception.EventNotFoundException;
import org.ow2.bonita.facade.exception.InstanceNotFoundException;
import org.ow2.bonita.facade.exception.ProcessNotFoundException;
import org.ow2.bonita.facade.exception.UncancellableInstanceException;
import org.ow2.bonita.facade.exception.UndeletableInstanceException;
import org.ow2.bonita.facade.exception.VariableNotFoundException;
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
@Path("/API/runtimeAPI/")
@Produces({"text/*","application/xml"})
@Consumes({"application/x-www-form-urlencoded", "application/xml" })
public interface RESTRemoteRuntimeAPI extends AbstractRemoteRuntimeAPI {

	/**
	 * Cancels for each  given instance UUID, the process instance.
   * If the instance represented by the given instanceUUID has a parentInstance,
   * then UncancellableInstanceException is thrown.
   * @param instanceUUIDs the instance UUIDs.
	 * @param options the options map (domain, queryList, user)
	 * @throws InstanceNotFoundException
	 * @throws UncancellableInstanceException
	 * @throws RemoteException
	 */
	@POST @Path("cancelProcessInstances")
	void cancelProcessInstances(
			@FormParam("instanceUUIDs") List<ProcessInstanceUUID> instanceUUIDs,
			@FormParam("options") final Map<String, String> options)
	throws InstanceNotFoundException, UncancellableInstanceException, RemoteException;	
	
	/**
	 * Creates an instance of the specified process with the added variable map, the default attachments
   * and start the execution.
   * returned instance has STARTED state.
   * If the first activity has StartMode=manual then a task has been created.
   * If the first activity has StartMode=automatic then the automatic behavior
   * of the activity has been started.
   * @param processUUID the process UUID.
   * @param variables variables added to the variables already set within the process definition
   * the variable object can be: a plain {@link String}, a {@link Boolean}, a {@link Date}, a
   * {@link Long} or a {@link Double}.
   * @param attachments the attachments
	 * @param options the options map (domain, queryList, user)
	 * @return the UUID of the created instance.
	 * @throws ProcessNotFoundException
	 * @throws RemoteException
	 * @throws VariableNotFoundException
	 */
	@POST @Path("instantiateProcessWithVariablesAndAttachements/{processUUID}")
  ProcessInstanceUUID instantiateProcess(
      @PathParam("processUUID") ProcessDefinitionUUID processUUID,
      @FormParam("variables") Map<String, Object> variables,
  		@FormParam("attachments") List<InitialAttachment> attachments,
  		@FormParam("options") final Map<String, String> options)
	throws ProcessNotFoundException, RemoteException, VariableNotFoundException;

	/**
	 * Deletes for each given instance UUID,  all runtime objects
   * @param instanceUUIDs the instance UUIDs.
	 * @param options the options map (domain, queryList, user)
	 * @throws InstanceNotFoundException
	 * @throws UndeletableInstanceException
	 * @throws RemoteException
	 */
	@POST @Path("deleteProcessInstances") 
  void deleteProcessInstances(
      @FormParam("instanceUUIDs") List<ProcessInstanceUUID> instanceUUIDs,
  		@FormParam("options") final Map<String, String> options)
	throws InstanceNotFoundException, UndeletableInstanceException, RemoteException;

  /**
   * Deletes all runtime objects for all instances created with the given process UUIDs collection
   * and delete also all there recorded data from the journal.
   * If instances some instances of this process were not found in the journal,
   * then the archived instances are deleted from history.
   * @param processUUIDs the collection of process UUIDs.
   * @param options the options map (domain, queryList, user)
   * @throws ProcessNotFoundException
   * @throws UndeletableInstanceException
   * @throws RemoteException
   */
  @POST @Path("deleteAllProcessInstances") 
  void deleteAllProcessInstances(
      @FormParam("processUUIDs") List<ProcessDefinitionUUID> processUUIDs,
  		@FormParam("options") final Map<String, String> options)
  throws ProcessNotFoundException,UndeletableInstanceException, RemoteException;

  /**
   * Deletes events
   * @param eventUUIDs the eventUUIDs of events to delete
   * @throws EventNotFoundException if an event does not exist
   */
  @POST @Path("deleteCatchingEvents")
  public void deleteEvents(
      @FormParam("eventUUIDs") List<CatchingEventUUID> eventUUIDs,
      @FormParam("options") final Map<String, String> options)
  throws EventNotFoundException, RemoteException;
  
  /**
   * Create a document associated with a process definition
   * @param name the name of the document
   * @param processDefinitionUUID the {@link ProcessDefinitionUUID}
   * @param fileName the filename
   * @param mimeType the mime type of the file
   * @param content the content of the file
   * @return a {@link Document}
   * @throws DocumentationCreationException
   * @throws InstanceNotFoundException
   */
  @Consumes("application/octet-stream")
  @POST @Path("createProcessDocumentOctetStream/{processUUID}")
  Document createDocumentOctetStream(
      @QueryParam("name") final String name, 
      @PathParam("processUUID") final ProcessDefinitionUUID processDefinitionUUID, 
      @QueryParam("fileName") final String fileName, 
      @QueryParam("mimeType") final String mimeType,
      final byte[] content,
      @HeaderParam("options") final Map<String, String> options)
  throws RemoteException, DocumentationCreationException, ProcessNotFoundException;
  
  
  /**
   * Create a document associated with a process instance
   * @param name the name of the document
   * @param instanceUUID the {@link ProcessInstanceUUID}
   * @param fileName the filename
   * @param mimeType the mime type of the file
   * @param content the content of the file
   * @return a {@link Document}
   * @throws DocumentationCreationException
   * @throws InstanceNotFoundException
   */
  @Consumes("application/octet-stream")
  @POST @Path("createDocumentOctetStream/{instanceUUID}")
  Document createDocumentOctetStream(
      @QueryParam("name") final String name,
      @PathParam("instanceUUID") final ProcessInstanceUUID instanceUUID,
      @QueryParam("fileName") final String fileName,
      @QueryParam("mimeType") final String mimeType,
      final byte[] content,
      @HeaderParam("options") final Map<String, String> options)
  throws RemoteException, DocumentationCreationException, InstanceNotFoundException;

  /**
   * add a document version
   * @param documentUUID the document UUID
   * @param isMajorVersion indicate if the document is a major version
   * @param fileName the filename
   * @param mimeType the mime type of the file
   * @param content the content of the file
   * @return a {@link Document}
   * @throws DocumentationCreationException
   * @throws InstanceNotFoundException
   */
  @Consumes("application/octet-stream")
  @POST @Path("addDocumentVersionOctetStream/{documentUUID}")
  Document addDocumentVersionOctetStream(
      @PathParam("documentUUID") final DocumentUUID documentUUID,
      @QueryParam("isMajorVersion") final boolean isMajorVersion,
      @QueryParam("fileName") final String fileName,
      @QueryParam("mimeType") final String mimeType,
      final byte[] content,
      @HeaderParam("options") final Map<String, String> options)
  throws RemoteException, DocumentationCreationException;
  
  
  /**
   * Add an attachment to a process instance. If you are using REST, use addAttachmentOctetStream instead.
   * @param instanceUUID the process instance UUID
   * @param name the attachment name
   * @param fileName the file name
   * @param value the content of the attachment
   * @param options the options map (domain, queryList, user)
   * @throws RemoteException
   */
  @Deprecated
  @Consumes("application/octet-stream")
  @POST @Path("addAttachmentOctetStream/{instanceUUID}") 
  void addAttachmentOctetStream(
      @PathParam("instanceUUID") ProcessInstanceUUID instanceUUID,
      @QueryParam("name") String name,
      @QueryParam("fileName") String fileName, 
      byte[] value,
      @HeaderParam("options") final Map<String, String> options)
  throws RemoteException;

}
