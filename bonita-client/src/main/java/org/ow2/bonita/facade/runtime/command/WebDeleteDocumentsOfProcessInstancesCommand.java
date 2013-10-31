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
package org.ow2.bonita.facade.runtime.command;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ow2.bonita.env.Environment;
import org.ow2.bonita.facade.APIAccessor;
import org.ow2.bonita.facade.QueryRuntimeAPI;
import org.ow2.bonita.facade.RuntimeAPI;
import org.ow2.bonita.facade.exception.DocumentNotFoundException;
import org.ow2.bonita.facade.impl.StandardAPIAccessorImpl;
import org.ow2.bonita.facade.runtime.Document;
import org.ow2.bonita.facade.uuid.DocumentUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.search.DocumentResult;
import org.ow2.bonita.search.DocumentSearchBuilder;
import org.ow2.bonita.search.index.DocumentIndex;
import org.ow2.bonita.util.Command;

public class WebDeleteDocumentsOfProcessInstancesCommand implements Command<Void> {

  private static final long serialVersionUID = 1L;

  private final Set<ProcessInstanceUUID> instancesUUIDs;
  
  public WebDeleteDocumentsOfProcessInstancesCommand(final Set<ProcessInstanceUUID> instancesUUIDs) {
    super();
    this.instancesUUIDs = instancesUUIDs;
  }

  @Override
  public Void execute(final Environment environment) throws Exception {
    if (instancesUUIDs != null && !instancesUUIDs.isEmpty()) {
      final Set<ProcessInstanceUUID> uuids = new HashSet<ProcessInstanceUUID>();
      final Set<String> instanceUuidsAsString = new HashSet<String>();
      for (final ProcessInstanceUUID uuid : instancesUUIDs) {
        uuids.add(uuid);
        instanceUuidsAsString.add(uuid.getValue());
        if (uuids.size() == 100) {
          deleteDocumentsOfCases(instanceUuidsAsString);
          uuids.clear();
          instanceUuidsAsString.clear();
        }
               
      }
      deleteDocumentsOfCases(instanceUuidsAsString);
    }
    return null;
  }
  
  /**
   * @param uuids
   * @throws DocumentNotFoundException
   */
  private void deleteDocumentsOfCases(final Set<String> uuids) throws DocumentNotFoundException {
          final DocumentSearchBuilder documentSearchBuilder = new DocumentSearchBuilder();
          documentSearchBuilder.criterion(DocumentIndex.PROCESS_INSTANCE_UUID).in(uuids);
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
              for (final Document document : documentsFound) {
                  documentsToDelete[i] = document.getUUID();
                  i++;
              }
              runtimeAPI.deleteDocuments(true, documentsToDelete);
          } while (searchResult.getCount() > 0);

  }
}
