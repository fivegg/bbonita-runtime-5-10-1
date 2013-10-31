/**
 * Copyright (C) 2012  BonitaSoft S.A.
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
package org.ow2.bonita.attachment;

import java.util.Collections;

import org.ow2.bonita.APITestCase;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.runtime.command.WebDeleteDocumentsOfProcessInstancesCommand;
import org.ow2.bonita.facade.runtime.command.WebDeleteProcessInstancesCommand;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.search.DocumentResult;
import org.ow2.bonita.search.DocumentSearchBuilder;
import org.ow2.bonita.search.index.DocumentIndex;
import org.ow2.bonita.util.ProcessBuilder;

/**
 * @author Elias Ricken de Medeiros
 *
 */
public class DeleteDocOfProcInstCommandTest extends APITestCase {
  public void testCanDeleteProcessInstanceContaintingADocument() throws Exception {
    final ProcessDefinition definition = ProcessBuilder.createProcess("doc", "1.0")
        .addAttachment("name")
        .addHuman(getLogin())
        .addHumanTask("step1", getLogin())
        .done();
    
    getManagementAPI().deploy(getBusinessArchive(definition));
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(definition.getUUID());
    executeTask(instanceUUID, "step1");
    
    final DocumentSearchBuilder documentSearchBuilder = new DocumentSearchBuilder();
    documentSearchBuilder.criterion(DocumentIndex.PROCESS_INSTANCE_UUID).equalsTo(instanceUUID.getValue());
    
    DocumentResult documentResult = getQueryRuntimeAPI().searchDocuments(documentSearchBuilder, 0, 10);
    assertEquals(1, documentResult.getCount());
    
    getCommandAPI().execute(new WebDeleteDocumentsOfProcessInstancesCommand(Collections.singleton(instanceUUID)));
    getCommandAPI().execute(new WebDeleteProcessInstancesCommand(Collections.singleton(instanceUUID)));
    
    documentResult = getQueryRuntimeAPI().searchDocuments(documentSearchBuilder, 0, 10);
    assertEquals(0, documentResult.getCount());
    
    getManagementAPI().deleteProcess(definition.getUUID());
  }
}
