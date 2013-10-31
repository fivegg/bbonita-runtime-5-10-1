/**
 * Copyright (C) 2007  Bull S. A. S.
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
 * Modified by Charles Souillard, BonitaSoft
 **/
package org.ow2.bonita.services.handlers.impl;

import org.ow2.bonita.env.binding.JournalBinding;
import org.ow2.bonita.facade.exception.BonitaWrapperException;
import org.ow2.bonita.facade.runtime.impl.InternalProcessInstance;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.services.Archiver;
import org.ow2.bonita.services.Querier;
import org.ow2.bonita.services.handlers.FinishedInstanceHandler;
import org.ow2.bonita.util.ArchiveTool;
import org.ow2.bonita.util.AtomicArchiveException;
import org.ow2.bonita.util.EnvTool;

/**
 * {@link FinishedInstanceHandler} that archive the records when the instance is finished
 * (removes records from the {@link JournalBinding} and gives them to the {@link Archiver}).
 *
 * @author Guillaume Porcher
 *
 */
public class ArchiveFinishedInstanceHandler implements FinishedInstanceHandler {

  public void handleFinishedInstance(final InternalProcessInstance instance) {
    final Querier journal  = EnvTool.getJournalQueriers();

    if (instance.getChildrenInstanceUUID() != null) {
      for (final ProcessInstanceUUID child : instance.getChildrenInstanceUUID()) {
        handleFinishedInstance(journal.getProcessInstance(child));
      }
    }

    try {
      ArchiveTool.atomicArchive(instance);
    } catch (final AtomicArchiveException e) {
      throw new BonitaWrapperException(e);
    }
     
  }
}
