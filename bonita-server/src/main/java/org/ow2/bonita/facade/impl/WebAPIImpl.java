/**
 * Copyright (C) 2009-2013 BonitaSoft S.A.
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.ow2.bonita.connector.core.Connector;
import org.ow2.bonita.connector.core.Mapper;
import org.ow2.bonita.definition.activity.ConnectorExecutor;
import org.ow2.bonita.facade.WebAPI;
import org.ow2.bonita.facade.def.InternalProcessDefinition;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.exception.CategoryAlreadyExistsException;
import org.ow2.bonita.facade.exception.CategoryNotFoundException;
import org.ow2.bonita.facade.exception.DocumentAlreadyExistsException;
import org.ow2.bonita.facade.exception.DocumentationCreationException;
import org.ow2.bonita.facade.exception.ProcessNotFoundException;
import org.ow2.bonita.facade.runtime.ActivityInstance;
import org.ow2.bonita.facade.runtime.Category;
import org.ow2.bonita.facade.runtime.Document;
import org.ow2.bonita.facade.runtime.Label;
import org.ow2.bonita.facade.runtime.WebTemporaryToken;
import org.ow2.bonita.facade.runtime.impl.CaseImpl;
import org.ow2.bonita.facade.runtime.impl.CategoryImpl;
import org.ow2.bonita.facade.runtime.impl.InternalProcessInstance;
import org.ow2.bonita.facade.runtime.impl.LabelImpl;
import org.ow2.bonita.facade.runtime.impl.WebTemporaryTokenImpl;
import org.ow2.bonita.facade.uuid.CategoryUUID;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.light.LightProcessDefinition;
import org.ow2.bonita.light.LightProcessInstance;
import org.ow2.bonita.light.impl.LightProcessDefinitionImpl;
import org.ow2.bonita.light.impl.LightProcessInstanceImpl;
import org.ow2.bonita.services.DocumentationManager;
import org.ow2.bonita.services.WebService;
import org.ow2.bonita.services.WebTokenManagementService;
import org.ow2.bonita.util.BonitaConstants;
import org.ow2.bonita.util.DocumentService;
import org.ow2.bonita.util.EnvTool;
import org.ow2.bonita.util.Misc;

/**
 * @author Charles Souillard, Matthieu Chaffotte, Nicolas Chabanoles, Christophe
 *         Leroy
 */
public class WebAPIImpl implements WebAPI {

  protected WebAPIImpl(final String queryList) {
  }

  @Override
  public void deletePhantomCases() {
    final Set<ProcessInstanceUUID> webCases = EnvTool.getWebService().getAllCases();
    final Set<ProcessInstanceUUID> runtimeCases = EnvTool.getAllQueriers().getParentInstancesUUIDs();

    webCases.removeAll(runtimeCases);
    if (!webCases.isEmpty()) {
      EnvTool.getWebService().deleteCases(webCases);
    }
  }

  @Override
  public void addLabel(final String labelName, final String ownerName, final String editableCSSStyleName,
      final String readonlyCSSStyleName, final String previewCSSStyleName, final boolean isVisible,
      final boolean hasToBeDisplayed, final String iconCSSStyle, final Set<ProcessInstanceUUID> caseList,
      final int displayOrder, final boolean isSystemLabel) {
    final WebService webService = EnvTool.getWebService();
    final LabelImpl label = new LabelImpl(labelName, ownerName, editableCSSStyleName, readonlyCSSStyleName,
        previewCSSStyleName, isVisible, hasToBeDisplayed, iconCSSStyle, displayOrder, isSystemLabel);
    webService.addLabel(label);

    addCasesToLabel(ownerName, labelName, caseList);
  }

  @Override
  public void removeLabel(final String ownerName, final String labelName) {
    EnvTool.getWebService().removeLabel(ownerName, labelName);
  }

  @Override
  public void removeCasesFromLabel(final String ownerName, final String labelName,
      final Set<ProcessInstanceUUID> caseList) {
    final Set<InternalProcessInstance> instances = EnvTool.getAllQueriers().getProcessInstances(caseList);
    final WebService webService = EnvTool.getWebService();
    for (final InternalProcessInstance parentNewOrUpdatedInstance : instances) {
      webService.removeCase(parentNewOrUpdatedInstance.getUUID(), ownerName, labelName);
    }
    EnvTool.getWebService().removeCasesFromLabel(ownerName, labelName, caseList);
  }

  @Override
  public void deleteAllCases() {
    EnvTool.getWebService().deleteAllCases();
  }

  @Override
  public void updateLabelCSS(final String ownerName, final String labelName, final String aEditableCSSStyle,
      final String aPreviewCSSStyle, final String aReadOnlyCSSStyle) {
    final LabelImpl label = EnvTool.getWebService().getLabel(ownerName, labelName);
    label.setEditableCSSStyleName(aEditableCSSStyle);
    label.setPreviewCSSStyleName(aPreviewCSSStyle);
    label.setReadonlyCSSStyleName(aReadOnlyCSSStyle);
  }

  @Override
  public void addCasesToLabel(final String ownerName, final String labelName, final Set<ProcessInstanceUUID> caseList) {
    final Set<InternalProcessInstance> instances = EnvTool.getAllQueriers().getProcessInstances(caseList);
    final WebService webService = EnvTool.getWebService();
    for (final InternalProcessInstance internalProcessInstance : instances) {
      if (webService.getCase(internalProcessInstance.getUUID(), ownerName, labelName) == null) {
        webService.addCase(new CaseImpl(internalProcessInstance.getUUID(), ownerName, labelName));
      }
    }
  }

  @Override
  public Label getLabel(final String ownerName, final String labelName) {
    final LabelImpl label = EnvTool.getWebService().getLabel(ownerName, labelName);
    if (label == null) {
      return null;
    }
    return new LabelImpl(label);
  }

  @Override
  public Set<Label> getLabels(final String ownerName) {
    final Set<LabelImpl> labels = EnvTool.getWebService().getLabels(ownerName);
    return getCopy(labels);
  }

  @Override
  public List<Label> getSystemLabels(final String ownerName) {
    final List<LabelImpl> labels = EnvTool.getWebService().getSystemLabels(ownerName);
    return getCopy(labels);
  }

  @Override
  public List<Label> getUserCustomLabels(final String ownerName) {
    final List<LabelImpl> labels = EnvTool.getWebService().getUserCustomLabels(ownerName);
    return getCopy(labels);
  }

  @Override
  public Set<Label> getCaseLabels(final String ownerName, final ProcessInstanceUUID case_) {
    final Set<LabelImpl> labels = EnvTool.getWebService().getCaseLabels(ownerName, case_);
    return getCopy(labels);
  }

  @Override
  public Map<ProcessInstanceUUID, Set<Label>> getCasesLabels(final String ownerName,
      final Set<ProcessInstanceUUID> cases) {
    if (cases == null || cases.isEmpty()) {
      return Collections.emptyMap();
    }
    final Map<ProcessInstanceUUID, Set<Label>> result = new HashMap<ProcessInstanceUUID, Set<Label>>();
    for (final ProcessInstanceUUID case_ : cases) {
      result.put(case_, getCaseLabels(ownerName, case_));
    }
    return result;
  }

  @Override
  public void removeAllCasesFromLabels(final Set<ProcessInstanceUUID> caseList) {
    if (caseList != null && !caseList.isEmpty()) {
      final WebService webService = EnvTool.getWebService();
      for (final ProcessInstanceUUID case_ : caseList) {
        webService.removeCase(case_);
      }
    }
  }

  @Override
  public Set<ProcessInstanceUUID> getCases(final String ownerName, final Set<String> labels) {
    if (ownerName != null && labels != null && !labels.isEmpty()) {
      final WebService webService = EnvTool.getWebService();
      final Set<ProcessInstanceUUID> cases = webService.getCases(ownerName, labels);
      if (cases == null) {
        return Collections.emptySet();
      }
      final Set<ProcessInstanceUUID> result = new HashSet<ProcessInstanceUUID>();
      for (final ProcessInstanceUUID instanceUUID : cases) {
        result.add(new ProcessInstanceUUID(instanceUUID));
      }
      return result;
    }
    return Collections.emptySet();
  }

  @Override
  public List<LightProcessInstance> getLightProcessInstances(final String ownerName, final Set<String> theLabelsName,
      final int fromIndex, final int pageSize) {
    if (ownerName == null || theLabelsName == null || theLabelsName.isEmpty()) {
      return Collections.emptyList();
    }

    final List<ProcessInstanceUUID> caseUUIDs = EnvTool.getWebService().getLabelsCaseUUIDs(ownerName, theLabelsName,
        fromIndex, pageSize);
    final Set<InternalProcessInstance> internalList = EnvTool.getAllQueriers().getProcessInstances(caseUUIDs);

    if (internalList == null || internalList.isEmpty()) {
      return Collections.emptyList();
    }
    final Comparator<InternalProcessInstance> comparator = new Comparator<InternalProcessInstance>() {
      @Override
      public int compare(final InternalProcessInstance o1, final InternalProcessInstance o2) {
        return o2.getLastUpdate().compareTo(o1.getLastUpdate());
      }
    };
    final List<InternalProcessInstance> sortedInstances = new ArrayList<InternalProcessInstance>(internalList);
    Collections.sort(sortedInstances, comparator);

    final List<LightProcessInstance> list = new ArrayList<LightProcessInstance>();
    for (final InternalProcessInstance internalProcessInstance : sortedInstances) {
      list.add(new LightProcessInstanceImpl(internalProcessInstance));
    }
    return list;
  }

  @Override
  public Set<Label> getLabels(final String ownerName, final Set<String> labelsName) {
    final Set<LabelImpl> labels = EnvTool.getWebService().getLabels(ownerName, labelsName);
    return getCopy(labels);
  }

  private Set<Label> getCopy(final Set<LabelImpl> labels) {
    if (labels == null || labels.isEmpty()) {
      return Collections.emptySet();
    }
    final Set<Label> result = new HashSet<Label>();
    for (final Label label : labels) {
      result.add(new LabelImpl(label));
    }
    return result;
  }

  private List<Label> getCopy(final List<LabelImpl> labels) {
    if (labels == null || labels.isEmpty()) {
      return Collections.emptyList();
    }
    final List<Label> result = new ArrayList<Label>();
    for (final Label label : labels) {
      result.add(new LabelImpl(label));
    }
    return result;
  }

  @Override
  public void removeLabels(final String ownerName, final Collection<String> labelNames) {
    for (final String labelName : labelNames) {
      removeLabel(ownerName, labelName);
    }
  }

  @Override
  public void updateLabelName(final String ownerName, final String labelName, final String newName) {
    EnvTool.getWebService().updateLabelName(ownerName, labelName, newName);
  }

  @Override
  public void updateLabelVisibility(final String ownerName, final String labelName, final boolean isVisible) {
    final LabelImpl label = EnvTool.getWebService().getLabel(ownerName, labelName);
    label.setVisible(isVisible);
  }

  @Override
  public void updateLabelVisibility(final String ownerName, final Map<String, Boolean> labelvisibilities) {
    for (final Entry<String, Boolean> labelVisibility : labelvisibilities.entrySet()) {
      updateLabelVisibility(ownerName, labelVisibility.getKey(), labelVisibility.getValue());
    }
  }

  @Override
  public Map<String, Integer> getCasesNumber(final String ownerName, final Collection<String> labelNames,
      final int limit) {
    final Map<String, Integer> result = new HashMap<String, Integer>();
    final WebService webService = EnvTool.getWebService();
    for (final String label : labelNames) {
      int number = 0;
      if (limit <= BonitaConstants.MAX_LIST_SIZE) {
        final Set<CaseImpl> cases = webService.getCases(ownerName, label, limit + 1);
        number = cases.size();
        if (number > limit) {
          number = -limit;
        }
      } else {
        number = webService.getCasesNumber(ownerName, label);
      }
      result.put(label, number);
    }
    return result;
  }

  @Override
  public Map<String, Integer> getCasesNumber(final String ownerName, final Collection<String> labelNames) {
    final Map<String, Integer> result = new HashMap<String, Integer>();
    final WebService webService = EnvTool.getWebService();
    for (final String label : labelNames) {
      final int number = webService.getCasesNumber(ownerName, label);
      result.put(label, number);
    }
    return result;
  }

  @Override
  public Map<String, Integer> getCasesNumber(final String ownerName, final String labelName,
      final Collection<String> labelNames, final int limit) {
    final Map<String, Integer> result = new HashMap<String, Integer>();
    final WebService webService = EnvTool.getWebService();
    for (final String label : labelNames) {
      int number = 0;
      if (limit <= BonitaConstants.MAX_LIST_SIZE) {
        final Set<CaseImpl> cases = webService.getCases(ownerName, labelName, label, limit + 1);
        number = cases.size();
        if (number > limit) {
          number = -limit;
        }
      } else {
        number = webService.getCasesNumber(ownerName, labelName, label);
      }
      result.put(label, number);
    }
    return result;
  }

  @Override
  public String generateTemporaryToken(final String identityKey) {
    FacadeUtil.checkArgsNotNull(identityKey);
    if (identityKey.length() == 0) {
      throw new IllegalArgumentException();
    }
    final WebTokenManagementService webTokenManagementService = EnvTool.getWebTokenManagementService();
    final String tokenKey = Misc.getUniqueId("");
    // Token will expire in 1 hour.
    final long expirationDate = new Date().getTime() + 1000 * 60 * 60;
    final WebTemporaryTokenImpl temporaryToken = new WebTemporaryTokenImpl(tokenKey, expirationDate, identityKey);
    webTokenManagementService.addTemporaryToken(temporaryToken);
    return temporaryToken.getToken();
  }

  @Override
  public String getIdentityKeyFromTemporaryToken(final String token) {
    final WebTokenManagementService webTokenManagementService = EnvTool.getWebTokenManagementService();
    final WebTemporaryToken temporaryToken = webTokenManagementService.getToken(token);
    if (temporaryToken == null) {
      return null;
    }
    // Token can be used only once.
    webTokenManagementService.deleteToken((WebTemporaryTokenImpl) temporaryToken);

    // Clean all expired tokens
    final Set<WebTemporaryToken> expiredTokens = webTokenManagementService.getExpiredTokens();
    for (final WebTemporaryToken expiredToken : expiredTokens) {
      webTokenManagementService.deleteToken((WebTemporaryTokenImpl) expiredToken);
    }

    return temporaryToken.getIdentityKey();
  }

  @Override
  public Set<Category> getAllCategories() {
    final Set<Category> categories = EnvTool.getJournalQueriers().getAllCategories();
    final Set<Category> result = new HashSet<Category>();
    for (final Category category : categories) {
      result.add(new CategoryImpl(category));
    }
    return result;
  }

  @Override
  public Set<Category> getCategories(final Set<String> names) {
    final Set<Category> categories = EnvTool.getJournalQueriers().getCategories(names);
    final Set<Category> result = new HashSet<Category>();
    for (final Category category : categories) {
      result.add(new CategoryImpl(category));
    }
    return result;
  }

  @Override
  public void deleteCategoriesByUUIDs(final Set<CategoryUUID> uuids) {
    final Set<CategoryImpl> categories = EnvTool.getJournalQueriers().getCategoriesByUUIDs(uuids);
    EnvTool.getWebService().removeCategories(new HashSet<Category>(categories));
  }

  @Override
  public Set<Category> getCategoriesByUUIDs(final Set<CategoryUUID> uuids) {
    final Set<CategoryImpl> categories = EnvTool.getJournalQueriers().getCategoriesByUUIDs(uuids);
    final Set<Category> result = new HashSet<Category>();
    for (final Category category : categories) {
      result.add(new CategoryImpl(category));
    }
    return result;
  }

  @Override
  public void removeAllLabelsExcept(final Set<String> labelNames) {
    final Set<LabelImpl> labels = EnvTool.getWebService().getLabelsByNameExcept(labelNames);
    for (final Label label : labels) {
      removeLabel(label.getOwnerName(), label.getName());
    }
  }

  @Override
  public void removeLabels(final Set<String> labelNames) {
    final Set<LabelImpl> labels = EnvTool.getWebService().getLabelsByName(labelNames);
    for (final Label label : labels) {
      removeLabel(label.getOwnerName(), label.getName());
    }
  }

  @Override
  public void deleteCategories(final Set<String> categoryNames) {
    final Set<Category> categories = EnvTool.getJournalQueriers().getCategories(categoryNames);
    EnvTool.getWebService().removeCategories(categories);
  }

  @Override
  public void executeConnectorAndSetVariables(final String connectorClassName, final Map<String, Object[]> parameters,
      final ActivityInstance activityInstance, final Map<String, Object> context) throws Exception {
    final ProcessDefinitionUUID processDefinitionUUID = activityInstance.getProcessDefinitionUUID();
    final Connector connector = (Connector) EnvTool.getClassDataLoader().getInstance(processDefinitionUUID,
        connectorClassName);
    if (connector instanceof Mapper) {
      throw new IllegalAccessException(connectorClassName + " is a instance of RoleResolver or Filter");
    }
    ConnectorExecutor.executeConnector(connector, parameters, activityInstance.getProcessInstanceUUID(),
        activityInstance, context);
  }

  @Override
  public Map<String, Object> executeConnectorAndGetVariablesToSet(final String connectorClassName,
      final Map<String, Object[]> parameters, final ProcessDefinitionUUID processDefinitionUUID,
      final Map<String, Object> context) throws Exception {
    final Connector connector = (Connector) EnvTool.getClassDataLoader().getInstance(processDefinitionUUID,
        connectorClassName);
    if (connector instanceof Mapper) {
      throw new IllegalAccessException(connectorClassName + " is a instance of RoleResolver or Filter");
    }
    return ConnectorExecutor.executeConnector(connector, parameters, processDefinitionUUID, context);
  }

  @Override
  public LightProcessDefinition setProcessCategories(final ProcessDefinitionUUID aProcessUUID,
      final Set<String> aCategoriesName) throws ProcessNotFoundException {
    FacadeUtil.checkArgsNotNull(aProcessUUID);
    final InternalProcessDefinition internalProcess = EnvTool.getAllQueriers().getProcess(aProcessUUID);
    if (internalProcess == null) {
      throw new ProcessNotFoundException("web_1", aProcessUUID);
    }
    internalProcess.setCategories(aCategoriesName);
    return new LightProcessDefinitionImpl(internalProcess);
  }

  @Override
  public void addCategory(final String name, final String iconCSSStyle, final String previewCSSStyleName,
      final String cssStyleName) throws CategoryAlreadyExistsException {
    FacadeUtil.checkArgsNotNull(name);
    final Set<Category> categories = EnvTool.getJournalQueriers().getCategories(Arrays.asList(name));
    if (categories != null && !categories.isEmpty()) {
      throw new CategoryAlreadyExistsException(name);
    }
    final CategoryImpl newCategory = new CategoryImpl(name);
    newCategory.setIconCSSStyle(iconCSSStyle);
    newCategory.setPreviewCSSStyleName(previewCSSStyleName);
    newCategory.setReadonlyCSSStyleName(cssStyleName);
    EnvTool.getRecorder().recordNewCategory(newCategory);
  }

  @Override
  public Category updateCategoryByUUID(final String uuid, final String name, final String iconCSSStyle,
      final String previewCSSStyleName, final String cssStyleName) throws CategoryNotFoundException,
      CategoryAlreadyExistsException {
    FacadeUtil.checkArgsNotNull(uuid, name);
    final CategoryImpl category = EnvTool.getJournalQueriers().getCategoryByUUID(uuid);
    if (category == null) {
      throw new CategoryNotFoundException(name);
    }
    final Set<Category> categories = EnvTool.getJournalQueriers().getCategories(Arrays.asList(name));
    if (categories != null && !categories.isEmpty()) {
      if (!categories.iterator().next().getUUID().equals(uuid)) {
        throw new CategoryAlreadyExistsException(name);
      }
    }
    category.setIconCSSStyle(iconCSSStyle);
    category.setPreviewCSSStyleName(previewCSSStyleName);
    category.setReadonlyCSSStyleName(cssStyleName);
    category.setName(name);
    return new CategoryImpl(category);
  }

  @Override
  public Set<Category> getAllCategoriesByUUIDExcept(final Set<CategoryUUID> uuids) {

    final Set<String> ids = new HashSet<String>();
    for (final CategoryUUID categoryUUID : uuids) {
      ids.add(categoryUUID.getValue());
    }
    final Set<Category> categories = EnvTool.getJournalQueriers().getAllCategoriesExcept(ids);
    final Set<Category> result = new HashSet<Category>();
    for (final Category category : categories) {
      result.add(new CategoryImpl(category));
    }
    return result;
  }

  @Override
  public Document addProcessDocumentTemplate(final String name, final ProcessDefinitionUUID processDefinitionUUID,
      final String fileName, final String mimeType, final byte[] content) throws ProcessNotFoundException,
      DocumentAlreadyExistsException, DocumentationCreationException {
    final ProcessDefinition process = EnvTool.getAllQueriers().getProcess(processDefinitionUUID);
    if (process == null) {
      throw new ProcessNotFoundException("bai_RAPII_29", processDefinitionUUID);
    } else if (content != null && (fileName == null || mimeType == null)) {
      new DocumentationCreationException("");
    }
    final DocumentationManager manager = EnvTool.getDocumentationManager();
    return DocumentService.getClientDocument(manager,
        manager.createMetaDocument(processDefinitionUUID, name, fileName, mimeType, content));
  }

  @Override
  public List<Document> getProcessDocumentTemplates(final ProcessDefinitionUUID processDefinitionUUID)
      throws ProcessNotFoundException, DocumentationCreationException {
    final ProcessDefinition process = EnvTool.getAllQueriers().getProcess(processDefinitionUUID);
    if (process == null) {
      throw new ProcessNotFoundException("bai_RAPII_29", processDefinitionUUID);
    }
    final DocumentationManager manager = EnvTool.getDocumentationManager();
    return DocumentService.getClientDocuments(manager, manager.getMetaDocuments(processDefinitionUUID));
  }

}
