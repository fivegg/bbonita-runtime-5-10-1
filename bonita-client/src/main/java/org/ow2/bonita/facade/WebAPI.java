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
package org.ow2.bonita.facade;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
 * Internal use only.
 */
public interface WebAPI {

  @Deprecated
  List<Label> getSystemLabels(String ownerName);

  @Deprecated
  List<Label> getUserCustomLabels(String ownerName);

  Label getLabel(String ownerName, String labelName);

  Set<Label> getLabels(String ownerName);

  void addLabel(String labelName, String ownerName, String editableCSSStyleName, String readonlyCSSStyleName, 
      String previewCSSStyleName, boolean isVisible, boolean hasToBeDisplayed, String iconCSSStyle, 
      Set<ProcessInstanceUUID> caseList, int displayOrder, boolean isSystemLabel);

  void removeLabel(String ownerName, String labelName);

  void removeLabels(String ownerName, Collection<String> labelNames);

  void addCasesToLabel(String ownerName, String labelName, Set<ProcessInstanceUUID> caseList);

  void removeCasesFromLabel(String ownerName, String labelName, Set<ProcessInstanceUUID> caseList);

  void updateLabelCSS(String ownerName, String labelName, String aEditableCSSStyle, String aPreviewCSSStyle, String aReadOnlyCSSStyle);

  void updateLabelVisibility(String ownerName, String labelName, boolean isVisible);

  void updateLabelVisibility(String ownerName, Map<String, Boolean> labelvisibilities);

  void updateLabelName(String ownerName, String labelName, String newName);

  Set<Label> getCaseLabels(String ownerName, ProcessInstanceUUID case_);

  Map<ProcessInstanceUUID, Set<Label>> getCasesLabels(String ownerName, Set<ProcessInstanceUUID> cases);

  void removeAllCasesFromLabels(Set<ProcessInstanceUUID> caseList);

  Set<Label> getLabels(String ownerName, Set<String> labelsName);
  void removeAllLabelsExcept(Set<String> labelNames);
  void removeLabels(Set<String> labelNames);

  Map<String, Integer> getCasesNumber(String owner, Collection<String> labelNames, int limit);
  Map<String, Integer> getCasesNumber(String owner, Collection<String> labelNames);
  Map<String, Integer> getCasesNumber(String owner, String labelName, Collection<String> labelNames, int limit);

  Set<ProcessInstanceUUID> getCases(String ownerName, Set<String> labels);

  List<LightProcessInstance> getLightProcessInstances(String ownerName, Set<String> theLabelsName, int startingIndex, int maxElementCount);

  void deletePhantomCases();
  void deleteAllCases();

  String generateTemporaryToken(String identityKey);
  String getIdentityKeyFromTemporaryToken(String token);

  Set<Category> getAllCategories();
  Set<Category> getCategories(Set<String> names);
  Set<Category> getCategoriesByUUIDs(Set<CategoryUUID> uuids);
  Set<Category> getAllCategoriesByUUIDExcept(Set<CategoryUUID> uuids);
  void deleteCategoriesByUUIDs(Set<CategoryUUID> uuids);
  void deleteCategories(Set<String> categoryNames);

  void executeConnectorAndSetVariables(String connectorClassName, Map<String, Object[]> parameters, ActivityInstance activityInstance, Map<String, Object> context) throws Exception;
  Map<String, Object> executeConnectorAndGetVariablesToSet(String connectorClassName, Map<String, Object[]> parameters, ProcessDefinitionUUID processDefinitionUUID, Map<String, Object> context) throws Exception;

  LightProcessDefinition setProcessCategories(ProcessDefinitionUUID aUUID, Set<String> aCategoriesName) throws ProcessNotFoundException;

  void addCategory(String name, String iconCSSStyle, String previewCSSStyleName, String cssStyleName) throws CategoryAlreadyExistsException;

  Category updateCategoryByUUID(String value, String name, String iconCSSStyle, String previewCSSStyleName, String cssStyleName) throws CategoryNotFoundException, CategoryAlreadyExistsException;

  Document addProcessDocumentTemplate(String name, ProcessDefinitionUUID processDefinitionUUID, String fileName, String mimeType, byte[] content)
  throws ProcessNotFoundException, DocumentAlreadyExistsException, DocumentationCreationException;

  List<Document> getProcessDocumentTemplates(ProcessDefinitionUUID processDefinitionUUID) throws ProcessNotFoundException, DocumentationCreationException;
}
