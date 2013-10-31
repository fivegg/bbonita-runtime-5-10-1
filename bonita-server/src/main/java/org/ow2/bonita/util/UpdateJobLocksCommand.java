/**
 * Copyright (C) 2013  BonitaSoft S.A.
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
package org.ow2.bonita.util;

import java.util.List;

import org.ow2.bonita.env.Environment;
import org.ow2.bonita.facade.def.InternalProcessDefinition;
import org.ow2.bonita.facade.runtime.impl.InternalProcessInstance;
import org.ow2.bonita.services.EventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Elias Ricken de Medeiros
 *
 */
public class UpdateJobLocksCommand  implements Command<Void> {

  private static final long serialVersionUID = -7175668322844266834L;
  
  private static final int MAX_RESULTS = 1000;
  
  private static final Logger LOG = LoggerFactory.getLogger(UpdateJobLocksCommand.class);

  @Override
  public Void execute(final Environment environment) throws Exception {
    createProcDefJobLocks();
    createRootProcInstJobLocks();
    return null;
  }

  private void createProcessDefinitionJobLocks(final List<InternalProcessDefinition> processes) {
    final EventService eventService = EnvTool.getEventService();
    for (final InternalProcessDefinition internalProcessDefinition : processes) {
      eventService.lockProcessDefinition(internalProcessDefinition.getUUID());
    }
  }

  private void createRootInstanceJobLocks(final List<InternalProcessInstance> procInsts) {
    final EventService eventService = EnvTool.getEventService();
    for (final InternalProcessInstance procInst : procInsts) {
      eventService.lockRootInstance(procInst.getUUID());
    }
  }
  
  private void createProcDefJobLocks() {
    LOG.info("updating job locks for process definitions ...");
    int fromIndex = 0;
    List<InternalProcessDefinition> processes = null;
    do {      
      processes = EnvTool.getJournal().getProcesses(fromIndex, MAX_RESULTS);
      createProcessDefinitionJobLocks(processes);
      fromIndex += MAX_RESULTS;
    } while (processes != null && !processes.isEmpty());
  }

  private void createRootProcInstJobLocks() {
    LOG.info("updating job locks for process instances ...");
    int fromIndex = 0;
    List<InternalProcessInstance> procInsts = null;
    do {      
      procInsts = EnvTool.getJournal().getParentProcessInstances(fromIndex, MAX_RESULTS);
      createRootInstanceJobLocks(procInsts);
      fromIndex += MAX_RESULTS;
    } while (procInsts != null && !procInsts.isEmpty());
  }
  
}
