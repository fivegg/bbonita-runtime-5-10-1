/**
 * Copyright (C) 2006  Bull S. A. S.
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
 * Modified by Charles Souillard - BonitaSoft S.A.
 * Modified by Matthieu Chaffotte - BonitaSoft S.A.
 **/
package org.ow2.bonita.services.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ow2.bonita.facade.runtime.Category;
import org.ow2.bonita.facade.runtime.impl.CaseImpl;
import org.ow2.bonita.facade.runtime.impl.LabelImpl;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.persistence.WebDbSession;
import org.ow2.bonita.services.WebService;
import org.ow2.bonita.util.EnvTool;

public class DbWebService implements WebService {

  private String persistenceServiceName;

  protected DbWebService() {
  }

  public DbWebService(final String persistenceServiceName) {
    super();
    this.persistenceServiceName = persistenceServiceName;
  }

  protected WebDbSession getDbSession() {
    return EnvTool.getWebServiceDbSession(persistenceServiceName);
  }

  @Override
  public Set<ProcessInstanceUUID> getAllCases() {
    return getDbSession().getAllCases();
  }

  @Override
  public void deleteAllCases() {
    getDbSession().deleteAllCases();
  }

  @Override
  public void addLabel(final LabelImpl label) {
    getDbSession().save(label);
  }

  @Override
  public void addCase(final CaseImpl caseImpl) {
    getDbSession().save(caseImpl);
  }

  @Override
  public void removeCase(final CaseImpl caseImpl) {
    getDbSession().delete(caseImpl);
  }

  @Override
  public void removeCase(final ProcessInstanceUUID caseUUID, final String ownerName, final String labelName) {
    final WebDbSession webSession = getDbSession();
    final Set<ProcessInstanceUUID> caseList = new HashSet<ProcessInstanceUUID>();
    caseList.add(caseUUID);
    final Set<CaseImpl> cases = webSession.getCases(ownerName, labelName, caseList);
    if (cases != null) {
      for (final CaseImpl caseImpl : cases) {
        webSession.delete(caseImpl);
      }
    }
  }

  @Override
  public List<ProcessInstanceUUID> getLabelsCaseUUIDs(final String ownerName, final Set<String> labelNames,
      final int fromIndex, final int pageSize) {
    return getDbSession().getLabelsCaseUUIDs(ownerName, labelNames, fromIndex, pageSize);
  }

  @Override
  public Set<ProcessInstanceUUID> getLabelCases(final String labelName, final Set<ProcessInstanceUUID> caseUUIDs) {
    return getDbSession().getLabelCases(labelName, caseUUIDs);
  }

  @Override
  public Set<CaseImpl> getLabelCases(final String ownerName, final Set<String> labelNames,
      final Set<ProcessInstanceUUID> caseUUIDs) {
    return getDbSession().getLabelCases(ownerName, labelNames, caseUUIDs);
  }

  @Override
  public void removeLabel(final String ownerName, final String labelName) {
    final WebDbSession webSession = getDbSession();
    final LabelImpl label = webSession.getLabel(ownerName, labelName);
    if (label != null) {
      webSession.delete(label);
      for (final CaseImpl caseImpl : webSession.getCases(ownerName, labelName)) {
        webSession.delete(caseImpl);
      }
    }
  }

  @Override
  public void removeCategories(final Set<Category> categories) {
    final WebDbSession webSession = getDbSession();
    for (final Category category : categories) {
      webSession.delete(category);
    }
  }

  @Override
  public LabelImpl getLabel(final String ownerName, final String labelName) {
    return getDbSession().getLabel(ownerName, labelName);
  }

  @Override
  public Set<CaseImpl> getCases(final Set<ProcessInstanceUUID> caseUUIDs) {
    return getDbSession().getCases(caseUUIDs);
  }

  @Override
  public Set<ProcessInstanceUUID> getCases(final String ownerName, final Set<String> theLabelsName) {
    return getDbSession().getCases(ownerName, theLabelsName);
  }

  @Override
  public Set<LabelImpl> getLabels(final String ownerName) {
    return getDbSession().getLabels(ownerName);
  }

  @Override
  public List<LabelImpl> getSystemLabels(final String ownerName) {
    return getDbSession().getSystemLabels(ownerName);
  }

  @Override
  public List<LabelImpl> getUserCustomLabels(final String ownerName) {
    return getDbSession().getUserCustomLabels(ownerName);
  }

  @Override
  public Set<LabelImpl> getCaseLabels(final String ownerName, final ProcessInstanceUUID instanceUUID) {
    return getDbSession().getCaseLabels(ownerName, instanceUUID);
  }

  @Override
  public CaseImpl getCase(final ProcessInstanceUUID caseUUID, final String ownerName, final String labelName) {
    return getDbSession().getCase(caseUUID, ownerName, labelName);
  }

  @Override
  public void removeCase(final ProcessInstanceUUID instanceUUID) {
    final WebDbSession webSession = getDbSession();
    final Set<CaseImpl> cases = webSession.getCases(instanceUUID);
    if (cases != null) {
      for (final CaseImpl caseImpl : cases) {
        webSession.delete(caseImpl);
      }
    }
  }

  @Override
  public void deleteCases(final Set<ProcessInstanceUUID> webCases) {
    getDbSession().deleteCases(webCases);
  }

  @Override
  public void removeCasesFromLabel(final String ownerName, final String labelName,
      final Set<ProcessInstanceUUID> caseList) {

    if (caseList == null || caseList.isEmpty()) {
      return;
    }
    final WebDbSession webSession = getDbSession();
    final Set<CaseImpl> cases = webSession.getCases(ownerName, labelName, caseList);
    if (cases != null) {
      for (final CaseImpl caseImpl : cases) {
        webSession.delete(caseImpl);
      }
    }
  }

  @Override
  public Set<LabelImpl> getLabels(final String ownerName, final Set<String> labelsName) {
    return getDbSession().getLabels(ownerName, labelsName);
  }

  @Override
  public int getCasesNumber(final String ownerName, final String labelName) {
    return getDbSession().getCasesNumber(ownerName, labelName);
  }

  @Override
  public int getCasesNumber(final String ownerName, final String label1Name, final String label2Name) {
    return getDbSession().getCasesNumberWithTwoLabels(ownerName, label1Name, label2Name);
  }

  @Override
  public Set<CaseImpl> getCases(final String ownerName, final String labelName, final int limit) {
    return getDbSession().getCases(ownerName, labelName, limit);
  }

  @Override
  public Set<CaseImpl> getCases(final String ownerName, final String label1Name, final String label2Name,
      final int limit) {
    return getDbSession().getCasesWithTwoLabels(ownerName, label1Name, label2Name, limit);
  }

  @Override
  public void updateLabelName(final String ownerName, final String labelName, final String newName) {
    final LabelImpl label = EnvTool.getWebService().getLabel(ownerName, labelName);
    label.setName(newName);
    final Set<CaseImpl> cases = getDbSession().getCases(ownerName, labelName);
    if (cases != null && !cases.isEmpty()) {
      for (final CaseImpl case1 : cases) {
        case1.setLabelName(newName);
      }
    }
  }

  @Override
  public Set<LabelImpl> getLabelsByName(final Set<String> labelNames) {
    return getDbSession().getLabels(labelNames);
  }

  @Override
  public Set<LabelImpl> getLabelsByNameExcept(final Set<String> labelNames) {
    return getDbSession().getLabelsByNameExcept(labelNames);
  }

}
