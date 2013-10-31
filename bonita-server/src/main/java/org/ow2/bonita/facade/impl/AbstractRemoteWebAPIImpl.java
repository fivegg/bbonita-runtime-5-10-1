/**
 * Copyright (C) 2010-2012 BonitaSoft S.A.
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ow2.bonita.facade.APIAccessor;
import org.ow2.bonita.facade.WebAPI;
import org.ow2.bonita.facade.exception.CategoryAlreadyExistsException;
import org.ow2.bonita.facade.exception.CategoryNotFoundException;
import org.ow2.bonita.facade.exception.DocumentAlreadyExistsException;
import org.ow2.bonita.facade.exception.DocumentationCreationException;
import org.ow2.bonita.facade.exception.ProcessNotFoundException;
import org.ow2.bonita.facade.internal.AbstractRemoteWebAPI;
import org.ow2.bonita.facade.runtime.ActivityInstance;
import org.ow2.bonita.facade.runtime.Category;
import org.ow2.bonita.facade.runtime.Document;
import org.ow2.bonita.facade.runtime.Label;
import org.ow2.bonita.facade.uuid.CategoryUUID;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.identity.auth.DomainOwner;
import org.ow2.bonita.identity.auth.PasswordOwner;
import org.ow2.bonita.identity.auth.RESTUserOwner;
import org.ow2.bonita.identity.auth.UserOwner;
import org.ow2.bonita.light.LightProcessDefinition;
import org.ow2.bonita.light.LightProcessInstance;

/**
 * 
 * @author Elias Ricken de Medeiros, Matthieu Chaffotte
 * 
 */
public class AbstractRemoteWebAPIImpl implements AbstractRemoteWebAPI {

  protected Map<String, WebAPI> apis = new HashMap<String, WebAPI>();

  protected WebAPI getAPI(final Map<String, String> options) {
    if (options == null) {
      throw new IllegalArgumentException("The options are null or not well set.");
    }
    final String queryList = options.get(APIAccessor.QUERYLIST_OPTION);
    final String user = options.get(APIAccessor.USER_OPTION);
    final String domain = options.get(APIAccessor.DOMAIN_OPTION);
    UserOwner.setUser(user);
    DomainOwner.setDomain(domain);

    final String restUser = options.get(APIAccessor.REST_USER_OPTION);
    if (restUser != null) {
      RESTUserOwner.setUser(restUser);
      final String restPswd = options.get(APIAccessor.PASSWORD_HASH_OPTION);
      PasswordOwner.setPassword(restPswd);
    }

    if (!apis.containsKey(queryList)) {
      apis.put(queryList, new StandardAPIAccessorImpl().getWebAPI(queryList));
    }
    return apis.get(queryList);
  }

  @Override
  public void deletePhantomCases(final Map<String, String> options) throws RemoteException {
    getAPI(options).deletePhantomCases();
  }

  @Override
  public void addCasesToLabel(final String ownerName, final String labelName, final Set<ProcessInstanceUUID> caseList,
      final Map<String, String> options) throws RemoteException {
    getAPI(options).addCasesToLabel(ownerName, labelName, caseList);
  }

  @Override
  public void addLabel(final String labelName, final String ownerName, final String editableCSSStyleName,
      final String readonlyCSSStyleName, final String previewCSSStyleName, final boolean isVisible,
      final boolean hasToBeDisplayed, final String iconCSSStyle, final Set<ProcessInstanceUUID> caseList,
      final int displayOrder, final boolean isSystemLabel, final Map<String, String> options) throws RemoteException {
    getAPI(options).addLabel(labelName, ownerName, editableCSSStyleName, readonlyCSSStyleName, previewCSSStyleName,
        isVisible, hasToBeDisplayed, iconCSSStyle, caseList, displayOrder, isSystemLabel);
  }

  @Override
  public Label getLabel(final String ownerName, final String labelName, final Map<String, String> options)
      throws RemoteException {
    return getAPI(options).getLabel(ownerName, labelName);
  }

  @Override
  public Set<Label> getLabels(final String ownerName, final Map<String, String> options) throws RemoteException {
    return getAPI(options).getLabels(ownerName);
  }

  @Override
  public List<Label> getSystemLabels(final String ownerName, final Map<String, String> options) throws RemoteException {
    return getAPI(options).getSystemLabels(ownerName);
  }

  @Override
  public List<Label> getUserCustomLabels(final String ownerName, final Map<String, String> options)
      throws RemoteException {
    return getAPI(options).getUserCustomLabels(ownerName);
  }

  @Override
  public Set<Category> getAllCategories(final Map<String, String> options) throws RemoteException {
    return getAPI(options).getAllCategories();
  }

  @Override
  public Set<Category> getAllCategoriesByUUIDExcept(final Set<CategoryUUID> uuids, final Map<String, String> options)
      throws RemoteException {
    return getAPI(options).getAllCategoriesByUUIDExcept(uuids);
  }

  @Override
  public Set<Category> getCategories(final Set<String> names, final Map<String, String> options) throws RemoteException {
    return getAPI(options).getCategories(names);
  }

  @Override
  public Set<Category> getCategoriesByUUIDs(final Set<CategoryUUID> uuids, final Map<String, String> options)
      throws RemoteException {
    return getAPI(options).getCategoriesByUUIDs(uuids);
  }

  @Override
  public void deleteCategories(final Set<String> categoryNames, final Map<String, String> options)
      throws RemoteException {
    getAPI(options).deleteCategories(categoryNames);
  }

  @Override
  public void deleteCategoriesByUUIDs(final Set<CategoryUUID> uuids, final Map<String, String> options)
      throws RemoteException {
    getAPI(options).deleteCategoriesByUUIDs(uuids);
  }

  @Override
  public List<LightProcessInstance> getLightProcessInstances(final String ownerName, final Set<String> labelNames,
      final int startingIndex, final int maxElementCount, final Map<String, String> options) throws RemoteException {
    return getAPI(options).getLightProcessInstances(ownerName, labelNames, startingIndex, maxElementCount);
  }

  @Override
  public Set<ProcessInstanceUUID> getCases(final String ownerName, final Set<String> labels,
      final Map<String, String> options) throws RemoteException {
    return getAPI(options).getCases(ownerName, labels);
  }

  @Override
  public void removeCasesFromLabel(final String ownerName, final String labelName,
      final Set<ProcessInstanceUUID> caseList, final Map<String, String> options) throws RemoteException {
    getAPI(options).removeCasesFromLabel(ownerName, labelName, caseList);
  }

  @Override
  public void removeLabel(final String ownerName, final String labelName, final Map<String, String> options)
      throws RemoteException {
    getAPI(options).removeLabel(ownerName, labelName);
  }

  @Override
  public void updateLabelCSS(final String ownerName, final String labelName, final String aEditableCSSStyle,
      final String aPreviewCSSStyle, final String aReadOnlyCSSStyle, final Map<String, String> options)
      throws RemoteException {
    getAPI(options).updateLabelCSS(ownerName, labelName, aEditableCSSStyle, aPreviewCSSStyle, aReadOnlyCSSStyle);
  }

  @Override
  public void updateLabelName(final String ownerName, final String labelName, final String newName,
      final Map<String, String> options) throws RemoteException {
    getAPI(options).updateLabelName(ownerName, labelName, newName);
  }

  @Override
  public Set<Label> getCaseLabels(final String ownerName, final ProcessInstanceUUID case_,
      final Map<String, String> options) throws RemoteException {
    return getAPI(options).getCaseLabels(ownerName, case_);
  }

  @Override
  public Map<ProcessInstanceUUID, Set<Label>> getCasesLabels(final String ownerName,
      final Set<ProcessInstanceUUID> cases, final Map<String, String> options) throws RemoteException {
    return getAPI(options).getCasesLabels(ownerName, cases);
  }

  @Override
  public void removeAllCasesFromLabels(final Set<ProcessInstanceUUID> caseList, final Map<String, String> options)
      throws RemoteException {
    getAPI(options).removeAllCasesFromLabels(caseList);
  }

  @Override
  public Set<Label> getLabels(final String ownerName, final Set<String> labelsName, final Map<String, String> options)
      throws RemoteException {
    return getAPI(options).getLabels(ownerName, labelsName);
  }

  @Override
  public void deleteAllCases(final Map<String, String> options) throws RemoteException {
    getAPI(options).deleteAllCases();
  }

  @Override
  public void updateLabelVisibility(final String ownerName, final String labelName, final boolean isVisible,
      final Map<String, String> options) throws RemoteException {
    getAPI(options).updateLabelVisibility(ownerName, labelName, isVisible);
  }

  @Override
  public void updateLabelVisibility(final String ownerName, final Map<String, Boolean> labelvisibilities,
      final Map<String, String> options) throws RemoteException {
    getAPI(options).updateLabelVisibility(ownerName, labelvisibilities);
  }

  @Override
  public String generateTemporaryToken(final String identityKey, final Map<String, String> options)
      throws RemoteException {
    return getAPI(options).generateTemporaryToken(identityKey);
  }

  @Override
  public String getIdentityKeyFromTemporaryToken(final String token, final Map<String, String> options)
      throws RemoteException {
    return getAPI(options).getIdentityKeyFromTemporaryToken(token);
  }

  @Override
  public void removeAllLabelsExcept(final Set<String> labelNames, final Map<String, String> options)
      throws RemoteException {
    getAPI(options).removeAllLabelsExcept(labelNames);
  }

  @Override
  public void removeLabels(final Set<String> labelNames, final Map<String, String> options) throws RemoteException {
    getAPI(options).removeLabels(labelNames);
  }

  @Override
  public void executeConnectorAndSetVariables(final String connectorClassName, final Map<String, Object[]> parameters,
      final ActivityInstance activityInstance, final Map<String, Object> context, final Map<String, String> options)
      throws RemoteException, Exception {
    getAPI(options).executeConnectorAndSetVariables(connectorClassName, parameters, activityInstance, context);
  }

  @Override
  public Map<String, Object> executeConnectorAndGetVariablesToSet(final String connectorClassName,
      final Map<String, Object[]> parameters, final ProcessDefinitionUUID processDefinitionUUID,
      final Map<String, Object> context, final Map<String, String> options) throws RemoteException, Exception {
    return getAPI(options).executeConnectorAndGetVariablesToSet(connectorClassName, parameters, processDefinitionUUID,
        context);
  }

  @Override
  public LightProcessDefinition setProcessCategories(final ProcessDefinitionUUID processUUID,
      final Set<String> categoryNames, final Map<String, String> options) throws RemoteException,
      ProcessNotFoundException {
    return getAPI(options).setProcessCategories(processUUID, categoryNames);
  }

  @Override
  public void addCategory(final String name, final String iconCSSStyle, final String previewCSSStyleName,
      final String cssStyleName, final Map<String, String> options) throws RemoteException,
      CategoryAlreadyExistsException {
    getAPI(options).addCategory(name, iconCSSStyle, previewCSSStyleName, cssStyleName);
  }

  @Override
  public Category updateCategoryByUUID(final String value, final String name, final String iconCSSStyle,
      final String previewCSSStyleName, final String cssStyleName, final Map<String, String> options)
      throws RemoteException, CategoryNotFoundException, CategoryAlreadyExistsException {
    return getAPI(options).updateCategoryByUUID(value, name, iconCSSStyle, previewCSSStyleName, cssStyleName);
  }

  @Override
  public Document addProcessDocumentTemplate(final String name, final ProcessDefinitionUUID processDefinitionUUID,
      final String fileName, final String mimeType, final byte[] content, final Map<String, String> options)
      throws RemoteException, ProcessNotFoundException, DocumentAlreadyExistsException, DocumentationCreationException {
    return getAPI(options).addProcessDocumentTemplate(name, processDefinitionUUID, fileName, mimeType, content);
  }

  @Override
  public List<Document> getProcessDocumentTemplates(final ProcessDefinitionUUID processDefinitionUUID,
      final Map<String, String> options) throws RemoteException, ProcessNotFoundException,
      DocumentationCreationException {
    return getAPI(options).getProcessDocumentTemplates(processDefinitionUUID);
  }

}
