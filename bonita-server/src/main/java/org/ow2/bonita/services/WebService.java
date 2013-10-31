/**
 * Copyright (C) 2010-2013 BonitaSoft S.A.
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
package org.ow2.bonita.services;

import java.util.List;
import java.util.Set;

import org.ow2.bonita.facade.runtime.Category;
import org.ow2.bonita.facade.runtime.impl.CaseImpl;
import org.ow2.bonita.facade.runtime.impl.LabelImpl;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;

/**
 * 
 * @author Matthieu Chaffotte
 * 
 */
public interface WebService {

  void addLabel(LabelImpl label);

  void removeLabel(String ownerName, String labelName);

  void addCase(CaseImpl caseImpl);

  void removeCase(ProcessInstanceUUID caseUUID, String ownerName, String labelName);

  void removeCase(ProcessInstanceUUID instanceUUID);

  void removeCasesFromLabel(String ownerName, String labelName, Set<ProcessInstanceUUID> caseList);

  void updateLabelName(String ownerName, String labelName, String newName);

  void deleteAllCases();

  void deleteCases(Set<ProcessInstanceUUID> webCases);

  void removeCase(CaseImpl caseImpl);

  void removeCategories(Set<Category> categories);

  int getCasesNumber(String ownerName, String labelName);

  int getCasesNumber(String ownerName, String label1Name, String label2Name);

  Set<CaseImpl> getCases(String ownerName, String labelName, int limit);

  Set<CaseImpl> getCases(String ownerName, String label1Name, String label2Name, int limit);

  LabelImpl getLabel(String ownerName, String labelName);

  Set<LabelImpl> getLabels(String ownerName);

  List<LabelImpl> getSystemLabels(String ownerName);

  List<LabelImpl> getUserCustomLabels(String ownerName);

  Set<LabelImpl> getCaseLabels(String ownerName, ProcessInstanceUUID instanceUUID);

  Set<LabelImpl> getLabels(String ownerName, Set<String> labelsName);

  Set<LabelImpl> getLabelsByNameExcept(Set<String> labelNames);

  Set<LabelImpl> getLabelsByName(Set<String> labelNames);

  Set<ProcessInstanceUUID> getCases(String ownerName, Set<String> theLabelsName);

  Set<ProcessInstanceUUID> getAllCases();

  Set<CaseImpl> getCases(Set<ProcessInstanceUUID> caseUUIDs);

  CaseImpl getCase(ProcessInstanceUUID caseUUID, String ownerName, String labelName);

  List<ProcessInstanceUUID> getLabelsCaseUUIDs(String ownerName, Set<String> labelNames, int fromIndex, int pageSize);

  Set<ProcessInstanceUUID> getLabelCases(String labelName, Set<ProcessInstanceUUID> caseUUIDs);

  Set<CaseImpl> getLabelCases(String ownerName, Set<String> labelNames, Set<ProcessInstanceUUID> caseUUIDs);

}
