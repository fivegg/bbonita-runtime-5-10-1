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
 * 
 **/
package org.ow2.bonita.facade.runtime.command;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ow2.bonita.env.Environment;
import org.ow2.bonita.facade.APIAccessor;
import org.ow2.bonita.facade.QueryRuntimeAPI;
import org.ow2.bonita.facade.RuntimeAPI;
import org.ow2.bonita.facade.WebAPI;
import org.ow2.bonita.facade.exception.DocumentNotFoundException;
import org.ow2.bonita.facade.impl.StandardAPIAccessorImpl;
import org.ow2.bonita.facade.runtime.Document;
import org.ow2.bonita.facade.uuid.DocumentUUID;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.light.LightProcessInstance;
import org.ow2.bonita.search.DocumentResult;
import org.ow2.bonita.search.DocumentSearchBuilder;
import org.ow2.bonita.search.index.DocumentIndex;
import org.ow2.bonita.util.Command;

/**
 * 
 * @author Nicolas Chabanoles
 * 
 */
public class WebDeleteAllProcessInstancesCommand implements Command<Void> {

	private static final long serialVersionUID = 1L;

	private Set<ProcessDefinitionUUID> processUUIDs;
	private boolean deleteAttachments;
	

    public WebDeleteAllProcessInstancesCommand(Set<ProcessDefinitionUUID> processUUIDs) {
        super();
        this.processUUIDs = processUUIDs;
        this.deleteAttachments = false;
    }
	
	public WebDeleteAllProcessInstancesCommand(Set<ProcessDefinitionUUID> processUUIDs, boolean deleteAttachments) {
		super();
		this.processUUIDs = processUUIDs;
		this.deleteAttachments = deleteAttachments;
	}

	public Void execute(Environment environment) throws Exception {
	  final APIAccessor accessor = new StandardAPIAccessorImpl();
		if (processUUIDs != null && !processUUIDs.isEmpty()) {
			// get all parent instances that are of process with given UUID
			final QueryRuntimeAPI queryRuntimeAPI = accessor.getQueryRuntimeAPI();
			Set<LightProcessInstance> instances = queryRuntimeAPI.getLightWeightProcessInstances(processUUIDs);
			// delete all cases of these instances (by group of 100 for perfs ?)
			final WebAPI webAPI = accessor.getWebAPI();
			Set<ProcessInstanceUUID> uuids = new HashSet<ProcessInstanceUUID>();
			for (LightProcessInstance instance : instances) {
				uuids.add(instance.getUUID());
				deleteAttachments(instance.getProcessDefinitionUUID());
				if (uuids.size() == 100) {
					webAPI.removeAllCasesFromLabels(uuids);
					uuids.clear();
				}
			}
			webAPI.removeAllCasesFromLabels(uuids);
			final RuntimeAPI runtimeAPI = accessor.getRuntimeAPI();
			runtimeAPI.deleteAllProcessInstances(processUUIDs);
		}
		return null;
	}
	
	  /**
	   * @param uuids
	   * @throws DocumentNotFoundException
	   */
	  private void deleteAttachments(ProcessDefinitionUUID uuid) throws DocumentNotFoundException {
	    
	      final DocumentSearchBuilder documentSearchBuilder = new DocumentSearchBuilder();
	      documentSearchBuilder.criterion(deleteAttachments?DocumentIndex.PROCESS_DEFINITION_UUID:DocumentIndex.PROCESS_DEFINITION_UUID_WITHOUT_INSTANCES).equalsTo(uuid.getValue());
	      final APIAccessor accessor = new StandardAPIAccessorImpl();
	      final QueryRuntimeAPI queryRuntimeAPI = accessor.getQueryRuntimeAPI();
	      final RuntimeAPI runtimeAPI = accessor.getRuntimeAPI();
	      DocumentResult searchResult;
	      List<Document> documentsFound;
	      DocumentUUID[] documentsToDelete;
	      int i;
	      do {
	        searchResult = queryRuntimeAPI.searchDocuments(documentSearchBuilder, 0, 100);
	        documentsFound = searchResult.getDocuments();
	        documentsToDelete = new DocumentUUID[documentsFound.size()];
	        i = 0;
	        for (Document document : documentsFound) {
	          documentsToDelete[i] = document.getUUID();
	          i++;
	        }
	        runtimeAPI.deleteDocuments(true, documentsToDelete);
	      } while (searchResult.getCount() > 0);

	  }
	

}
