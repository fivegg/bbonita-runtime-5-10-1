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
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.ow2.bonita.facade.exception.CategoryAlreadyExistsException;
import org.ow2.bonita.facade.exception.CategoryNotFoundException;
import org.ow2.bonita.facade.exception.DocumentAlreadyExistsException;
import org.ow2.bonita.facade.exception.DocumentationCreationException;
import org.ow2.bonita.facade.exception.ProcessNotFoundException;
import org.ow2.bonita.facade.runtime.ActivityInstance;
import org.ow2.bonita.facade.runtime.Category;
import org.ow2.bonita.facade.runtime.Document;
import org.ow2.bonita.facade.runtime.Label;
import org.ow2.bonita.facade.uuid.CategoryUUID;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.light.LightProcessDefinition;
import org.ow2.bonita.light.LightProcessInstance;

/**
 * 
 * @author Elias Ricken de Medeiros
 *
 */
@Path("/API/webAPI/")
@Produces({"text/*","application/xml"})
@Consumes({"application/x-www-form-urlencoded","text/*", "application/xml"})
public interface AbstractRemoteWebAPI extends Remote {
  
	@POST @Path("getSystemLabels/{ownerName}")
  List<Label> getSystemLabels(
  		@PathParam("ownerName") String ownerName, 
  		@FormParam("options") final Map<String, String> options) 
  throws RemoteException;
  
	@POST @Path("getUserCustomLabels/{ownerName}")
  List<Label> getUserCustomLabels(
  		@PathParam("ownerName") String ownerName, 
  		@FormParam("options") final Map<String, String> options) 
  throws RemoteException;
  
	@POST @Path("getLabel/{ownerName}/{labelName}")
  Label getLabel(
  		@PathParam("ownerName") String ownerName, 
  		@PathParam("labelName") String labelName, 
  		@FormParam("options") final Map<String, String> options) 
	throws RemoteException;
  
	@POST @Path("getLabels/{ownerName}")
  Set<Label> getLabels(
  		@PathParam("ownerName") String ownerName, 
  		@FormParam("options") final Map<String, String> options) 
  throws RemoteException;
  
  @POST @Path("addLabel") 
  void addLabel(
  		@QueryParam("labelName") String labelName, 
  		@QueryParam("ownerName") String ownerName, 
  		@QueryParam("editableCSSStyleName") String editableCSSStyleName, 
  		@QueryParam("readonlyCSSStyleName") String readonlyCSSStyleName, 
  		@QueryParam("previewCSSStyleName") String previewCSSStyleName, 
  		@QueryParam("isVisible") boolean isVisible, 
  		@QueryParam("hasToBeDisplayed") boolean hasToBeDisplayed, 
  		@QueryParam("iconCSSStyle") String iconCSSStyle, 
      @FormParam("caseList") Set<ProcessInstanceUUID> caseList, 
      @QueryParam("displayOrder") int displayOrder, 
      @QueryParam("isSystemLabel") boolean isSystemLabel, 
      @FormParam("options") final Map<String, String> options) 
  throws RemoteException;

  @POST @Path("removeLabel/{ownerName}/{labelName}") 
  void removeLabel(
  		@PathParam("ownerName") String ownerName, 
  		@PathParam("labelName") String labelName, 
  		@FormParam("options") final Map<String, String> options) 
  throws RemoteException;
  
  @POST @Path("addCasesToLabel/{ownerName}/{labelName}") 
  void addCasesToLabel(
  		@PathParam("ownerName") String ownerName, 
  		@PathParam("labelName") String labelName, 
  		@FormParam("caseList") Set<ProcessInstanceUUID> caseList, 
  		@FormParam("options") final Map<String, String> options) 
  throws RemoteException;
  
  @POST @Path("removeCasesFromLabel/{ownerName}/{labelName}") 
  void removeCasesFromLabel(
  		@PathParam("ownerName") String ownerName, 
  		@PathParam("labelName") String labelName, 
  		@FormParam("caseList") Set<ProcessInstanceUUID> caseList, 
  		@FormParam("options") final Map<String, String> options) 
  throws RemoteException;
  
  @POST @Path("updateLabelCSS/{ownerName}/{labelName}") 
  void updateLabelCSS(
  		@PathParam("ownerName") String ownerName, 
  		@PathParam("labelName") String labelName, 
  		@QueryParam("aEditableCSSStyle") String aEditableCSSStyle, 
  		@QueryParam("aPreviewCSSStyle") String aPreviewCSSStyle, 
  		@QueryParam("aReadOnlyCSSStyle") String aReadOnlyCSSStyle, 
  		@FormParam("options") final Map<String, String> options) 
  throws RemoteException;

  @POST @Path("updateLabelVisibility/{ownerName}/{labelName}/{isVisible}") 
  void updateLabelVisibility(
  		@PathParam("ownerName") String ownerName, 
  		@PathParam("labelName") String labelName, 
  		@PathParam("isVisible") boolean isVisible, 
  		@FormParam("options") final Map<String, String> options) 
  throws RemoteException;
  
  @POST 
  @Path("updateLabelVisibility/{ownerName}") 
  void updateLabelVisibility(
  		@PathParam("ownerName") String ownerName, 
  		@FormParam("labelvisibilities") Map<String, Boolean> labelvisibilities, 
  		@FormParam("options") final Map<String, String> options) 
  throws RemoteException;

  @POST @Path("updateLabelName/{ownerName}/{labelName}/{newName}") 
  void updateLabelName(
  		@PathParam("ownerName") String ownerName, 
  		@PathParam("labelName") String labelName, 
  		@PathParam("newName") String newName, 
  		@FormParam("options") final Map<String, String> options) 
  throws RemoteException;
  
  @POST @Path("getCaseLabels/{ownerName}/{case_}")
  Set<Label> getCaseLabels(
  		@PathParam("ownerName") String ownerName, 
  		@PathParam("case_") ProcessInstanceUUID case_, 
  		@FormParam("options") final Map<String, String> options) 
  throws RemoteException;
  
  @POST @Path("getCasesLabels/{ownerName}")
  Map<ProcessInstanceUUID, Set<Label>> getCasesLabels(
  		@PathParam("ownerName") String ownerName, 
  		@FormParam("cases") Set<ProcessInstanceUUID> cases, 
  		@FormParam("options") final Map<String, String> options) 
  throws RemoteException;
  
  @POST @Path("removeAllCasesFromLabels") 
  void removeAllCasesFromLabels(
  		@FormParam("caseList") Set<ProcessInstanceUUID> caseList, 
  		@FormParam("options") final Map<String, String> options) 
  throws RemoteException;

  @POST @Path("removeAllLabelsExcept") 
  void removeAllLabelsExcept(
  		@FormParam("labelNames") Set<String> labelNames, 
  		@FormParam("options") final Map<String, String> options) 
  throws RemoteException;
  
  @POST @Path("removeLabels") 
  void removeLabels(
  		@FormParam("labelNames") Set<String> labelNames, 
  		@FormParam("options") final Map<String, String> options) 
  throws RemoteException;

  @POST @Path("getLabelsByLabelsName/{ownerName}")
  Set<Label> getLabels(
  		@PathParam("ownerName") String ownerName, 
  		@FormParam("labelsName") Set<String> labelsName, 
  		@FormParam("options") final Map<String, String> options) 
  throws RemoteException;
  
  @POST @Path("getAllCategories")
  Set<Category> getAllCategories(
  		@FormParam("options") final Map<String, String> options) 
  throws RemoteException;
  
  @POST @Path("getAllCategoriesByUUIDExcept")
  Set<Category> getAllCategoriesByUUIDExcept(@
  		FormParam("uuids") Set<CategoryUUID> uuids, 
  		@FormParam("options") final Map<String, String> options) 
  throws RemoteException;
  
  @POST @Path("getCategories")
  Set<Category> getCategories(
  		@FormParam("names") Set<String> names, 
  		@FormParam("options") final Map<String, String> options) 
  throws RemoteException;
  
  @POST	@Path("getCategoriesByUUIDs")
  Set<Category> getCategoriesByUUIDs(
  		@FormParam("uuids") Set<CategoryUUID> uuids, 
  		@FormParam("options") final Map<String, String> options) 
  throws RemoteException;
  
  @POST @Path("deleteCategories") 
  void deleteCategories(
  		@FormParam("categoryNames") final Set<String> categoryNames, 
  		@FormParam("options") final Map<String, String> options) 
  throws RemoteException;
  
  @POST @Path("deleteCategoriesByUUIDs")
  void deleteCategoriesByUUIDs(
  		@FormParam("uuids") Set<CategoryUUID> uuids, 
  		@FormParam("options") final Map<String, String> options) 
  throws RemoteException;

  @POST @Path("getLightProcessInstances/{ownerName}")
  List<LightProcessInstance> getLightProcessInstances(
  		@PathParam("ownerName") String ownerName, 
  		@FormParam("theLabelsName") Set<String> theLabelsName, 
  		@QueryParam("startingIndex") int startingIndex, 
  		@QueryParam("maxElementCount") int maxElementCount, 
  		@FormParam("options") final Map<String, String> options) 
  throws RemoteException;
  
  @POST @Path("getCases/{ownerName}")
  Set<ProcessInstanceUUID> getCases(
  		@PathParam("ownerName") String ownerName, 
  		@FormParam("labels") Set<String> labels, 
  		@FormParam("options") final Map<String, String> options) 
  throws RemoteException;

  @POST @Path("deleteAllCases") 
  void deleteAllCases(
  		@FormParam("options") final Map<String, String> options) 
  throws RemoteException;
  
  @POST @Path("deletePhantomCases") 
  void deletePhantomCases(
  		@FormParam("options") final Map<String, String> options) 
  throws RemoteException;

  @POST @Path("generateTemporaryToken")
  String generateTemporaryToken(
      @FormParam("identityKey")String identityKey, 
  		@FormParam("options") final Map<String, String> options) 
  throws RemoteException;
  
  @POST @Path("getIdentityKeyFromTemporaryToken/{token}")
  String getIdentityKeyFromTemporaryToken(
  		@PathParam("token")String token, 
  		@FormParam("options") final Map<String, String> options) 
  throws RemoteException;
  
  @POST @Path("executeConnectorAndSetVariables")
  void executeConnectorAndSetVariables(
  		@FormParam("connectorClassName") String connectorClassName, 
  		@FormParam("parameters") Map<String, Object[]> parameters, 
  		@PathParam("activityInstance") ActivityInstance activityInstance, 
  		@FormParam("context") Map<String, Object> context, 
  		@FormParam("options") final Map<String, String> options) 
  throws RemoteException, Exception;
  
  @POST @Path("executeConnectorAndGetVariablesToSet/{processDefinitionUUID}")
  Map<String, Object> executeConnectorAndGetVariablesToSet(
  		@FormParam("connectorClassName") String connectorClassName, 
  		@FormParam("parameters") Map<String, Object[]> parameters, 
  		@PathParam("processDefinitionUUID") ProcessDefinitionUUID processDefinitionUUID, 
  		@FormParam("context") Map<String, Object> context, 
  		@FormParam("options") final Map<String, String> options) 
  throws RemoteException, Exception;
  
  @POST @Path("setProcessCategories/{processUUID}")
  LightProcessDefinition setProcessCategories(
  		@PathParam("processUUID") ProcessDefinitionUUID processUUID, 
  		@FormParam("CategoryNames") Set<String> CategoryNames, 
  		@FormParam("options") final Map<String, String> options) 
  throws RemoteException, ProcessNotFoundException;
  
  @POST @Path("addCategory")
  void addCategory(
  		@QueryParam("name") String name, 
  		@QueryParam("iconCSSStyle") String iconCSSStyle, 
  		@QueryParam("previewCSSStyleName") String previewCSSStyleName, 
  		@QueryParam("cssStyleName") String cssStyleName, 
  		@FormParam("options") final Map<String, String> options) 
  throws RemoteException, CategoryAlreadyExistsException;

  @POST @Path("updateCategoryByUUID")
  Category updateCategoryByUUID(
  		@QueryParam("value") String value, 
  		@QueryParam("name") String name, 
  		@QueryParam("iconCSSStyle") String iconCSSStyle, 
  		@QueryParam("previewCSSStyleName") String previewCSSStyleName, 
  		@QueryParam("cssStyleName") String cssStyleName, 
  		@FormParam("options") final Map<String, String> options) 
  throws RemoteException, CategoryNotFoundException, CategoryAlreadyExistsException;

  @Consumes("application/octet-stream")
  @POST @Path("addProcessDocumentTemplate/{processUUID}")
  Document addProcessDocumentTemplate(
      @QueryParam("name") String name,
      @PathParam("processUUID") ProcessDefinitionUUID processDefinitionUUID,
      @QueryParam("fileName") String fileName,
      @QueryParam("mimeType") String mimeType,
      byte[] content, 
      @HeaderParam("options") final Map<String, String> options) 
  throws RemoteException, ProcessNotFoundException, DocumentAlreadyExistsException, DocumentationCreationException;

  @POST @Path("getProcessDocumentTemplates/{processUUID}")
  List<Document> getProcessDocumentTemplates(
      @PathParam("processUUID") ProcessDefinitionUUID processDefinitionUUID, 
      @FormParam("options") final Map<String, String> options) 
  throws RemoteException, ProcessNotFoundException, DocumentationCreationException;

}
