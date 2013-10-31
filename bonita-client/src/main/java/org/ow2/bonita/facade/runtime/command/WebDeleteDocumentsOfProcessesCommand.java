/**
 * Copyright (C) 2009 BonitaSoft S.A.
 * BonitaSoft, 31 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/
package org.ow2.bonita.facade.runtime.command;

import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ow2.bonita.env.Environment;
import org.ow2.bonita.facade.APIAccessor;
import org.ow2.bonita.facade.QueryRuntimeAPI;
import org.ow2.bonita.facade.RuntimeAPI;
import org.ow2.bonita.facade.exception.DocumentNotFoundException;
import org.ow2.bonita.facade.impl.StandardAPIAccessorImpl;
import org.ow2.bonita.facade.runtime.Document;
import org.ow2.bonita.facade.uuid.DocumentUUID;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.search.DocumentResult;
import org.ow2.bonita.search.DocumentSearchBuilder;
import org.ow2.bonita.search.index.DocumentIndex;
import org.ow2.bonita.util.Command;

public class WebDeleteDocumentsOfProcessesCommand implements Command<Void> {

    private static final long serialVersionUID = -4049500711900576134L;

    private final Collection<ProcessDefinitionUUID> processUUIDs;

    private final boolean deleteAttachments;

    private static final Logger LOGGER = Logger.getLogger(WebDeleteDocumentsOfProcessesCommand.class.getName());

    public WebDeleteDocumentsOfProcessesCommand(final Collection<ProcessDefinitionUUID> processUUIDs) {
        super();
        this.processUUIDs = processUUIDs;
        deleteAttachments = false;
        Logger.getLogger(WebDeleteDocumentsOfProcessesCommand.class.getName());
    }

    public WebDeleteDocumentsOfProcessesCommand(final Collection<ProcessDefinitionUUID> processUUIDs, final boolean deleteAttachments) {
        super();
        this.processUUIDs = processUUIDs;
        this.deleteAttachments = deleteAttachments;
    }

    @Override
    public Void execute(final Environment environment) throws Exception {
        for (final ProcessDefinitionUUID processUUID : processUUIDs) {
            deleteAttachments(processUUID);
        }
        if (deleteAttachments) {
            try {
                final Class<?> serverCommandClass = Class.forName("org.ow2.bonita.util.ServerWebDeleteDocumentsOfProcessesCommand");
                final Command<?> serverCommand = (Command<?>) serverCommandClass.getConstructor(Collection.class).newInstance(processUUIDs);
                serverCommand.execute(null);
            } catch (final Exception e) {
                LOGGER.log(Level.SEVERE, "Unable to delete folder of the process definition on document server", e);
            }
        }
        return null;
    }

    /**
     * @param uuids
     * @throws DocumentNotFoundException
     */
    private void deleteAttachments(final ProcessDefinitionUUID uuid) throws DocumentNotFoundException {

        final DocumentSearchBuilder documentSearchBuilder = new DocumentSearchBuilder();
        documentSearchBuilder.criterion(deleteAttachments ? DocumentIndex.PROCESS_DEFINITION_UUID : DocumentIndex.PROCESS_DEFINITION_UUID_WITHOUT_INSTANCES)
                .equalsTo(uuid.getValue());
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
