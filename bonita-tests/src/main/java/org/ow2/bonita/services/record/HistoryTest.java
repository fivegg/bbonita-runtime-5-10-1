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
 **/
package org.ow2.bonita.services.record;

import java.util.Set;

import org.ow2.bonita.APITestCase;
import org.ow2.bonita.env.Environment;
import org.ow2.bonita.env.GlobalEnvironmentFactory;
import org.ow2.bonita.facade.def.InternalProcessDefinition;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.def.majorElement.impl.ProcessDefinitionImpl;
import org.ow2.bonita.facade.runtime.ActivityInstance;
import org.ow2.bonita.facade.runtime.ProcessInstance;
import org.ow2.bonita.facade.runtime.VariableUpdate;
import org.ow2.bonita.facade.runtime.impl.InternalActivityInstance;
import org.ow2.bonita.facade.runtime.impl.InternalProcessInstance;
import org.ow2.bonita.facade.uuid.ActivityInstanceUUID;
import org.ow2.bonita.facade.uuid.IdFactory;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.services.Archiver;
import org.ow2.bonita.services.CommandService;
import org.ow2.bonita.services.Querier;
import org.ow2.bonita.util.BonitaConstants;
import org.ow2.bonita.util.Command;
import org.ow2.bonita.util.EnvTool;

/**
 * @author Pierre Vigneras
 */
public class HistoryTest extends APITestCase {

  protected CommandService commandService;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    this.commandService = GlobalEnvironmentFactory.getEnvironmentFactory(BonitaConstants.DEFAULT_DOMAIN).get(CommandService.class);
  }

  @Override
  public void tearDown() throws Exception {
    this.commandService = null;
    super.tearDown();
  }

  public void testGetNonExistentProcess() {
    final ProcessDefinition record = getLastDeployedProcess(IdFactory.getNewProcessId(),
    		ProcessDefinition.ProcessState.DISABLED);
    assertNull(record);
    final Set<InternalProcessDefinition> records = getProcesses(IdFactory.getNewProcessId());
    assertNotNull(records);
    assertTrue(records.toString(), records.isEmpty());
  }

  public void testGetProcessNull() {
    try {
      getLastDeployedProcess(null, null);
      fail("Should not accept a null processId!");
    } catch (final IllegalArgumentException iae) {
      // ok
    }

    try {
      getProcesses((String) null);
      fail("Should not accept a null processId!");
    } catch (final IllegalArgumentException iae) {
      // ok
    }
  }

  public void testArchiveNullProcess() {
    try {
      archiveProcess((InternalProcessDefinition) null);
      fail("Should not accept a null processFullDef!");
    } catch (final IllegalArgumentException iae) {
      // ok
    }
  }

  public void testArchiveNullProcessInstance() {
    try {
      archiveInstance((InternalProcessInstance) null);
      fail("Should not accept a null processFullDef!");
    } catch (final IllegalArgumentException iae) {
      // ok
    }
  }

  public void testGetNonExistentProcessInstance() {
    final ProcessInstance record = getProcessInstance(IdFactory.getNewInstanceUUID());
    assertNull(record);

    final Set<InternalProcessInstance> records = getProcessInstances(IdFactory.getNewProcessUUID());
    assertNotNull(records);
    assertTrue(records.toString(), records.isEmpty());
  }

  public void testGetProcessInstanceNull() {
    try {
      getProcessInstance(null);
      fail("Should not accept a null processInstanceUUID!");
    } catch (final IllegalArgumentException iae) {
      // ok
    }
    try {
      getProcessInstances(null);
      fail("Should not accept a null processInstanceUUID!");
    } catch (final IllegalArgumentException iae) {
      // ok
    }
  }

  public void testGetNonExistentActivityInstance() {
    Set<InternalActivityInstance> records;
    ActivityInstance record;
    record = getActivityInstance(IdFactory.getNewActivityUUID());
    assertNull(record);
    records = getActivityInstances(IdFactory.getNewInstanceUUID());
    assertNotNull(records);
    assertTrue(records.toString(), records.isEmpty());
  }

  public void testGetActivityInstanceNull() {
    try {
      getActivityInstance(null);
      fail("Should not accept a null activityInstanceUUID!");
    } catch (final IllegalArgumentException iae) {
      // ok
    }
    try {
      getActivityInstances(null);
      fail("Should not accept a null activityInstanceUUID!");
    } catch (final IllegalArgumentException iae) {
      // ok
    }
  }


  private void archiveInstance(final InternalProcessInstance processInstance) {
    this.commandService.execute(new ArchiveCommand(processInstance));
  }

  private void archiveProcess(final InternalProcessDefinition processDef) {
    this.commandService.execute(new ArchiveCommand(processDef));
  }

  private Set<InternalProcessDefinition> getProcesses(final String processId) {
    return this.commandService.execute(new GetProcessesCommandWithId(processId));
  }

  private ProcessDefinition getLastDeployedProcess(final String processId, final ProcessDefinition.ProcessState processState) {
    return this.commandService.execute(new GetLastDeployedProcess(processId, processState));
  }

  private ActivityInstance getActivityInstance(final ActivityInstanceUUID instanceUUID) {
    return this.commandService.execute(new GetActivityInstanceOnlyUUID(instanceUUID));
  }

  private Set<InternalActivityInstance> getActivityInstances(final ProcessInstanceUUID processInstanceUUID) {
    return this.commandService.execute(new GetActivityInstances(processInstanceUUID));
  }

  private ProcessInstance getProcessInstance(final ProcessInstanceUUID processInstanceUUID) {
    return this.commandService.execute(new GetProcessInstance(processInstanceUUID));
  }

  private Set<InternalProcessInstance> getProcessInstances(final ProcessDefinitionUUID processDefUUID) {
    return this.commandService.execute(new GetProcessInstances(processDefUUID));
  }

  private Querier getHistory() {
    return EnvTool.getHistoryQueriers();
  }
  /* List of commands */

  class GetProcessWithUUID implements Command<ProcessDefinition> {
    /**
     * 
     */
    private static final long serialVersionUID = -3798444175939857662L;
    private final ProcessDefinitionUUID processDefUUID;

    public GetProcessWithUUID(final ProcessDefinitionUUID processDefUUID) {
      this.processDefUUID = processDefUUID;
    }
    public ProcessDefinition execute(final Environment environment) throws Exception {
      ProcessDefinition process = getHistory().getProcess(this.processDefUUID);
      return new ProcessDefinitionImpl(process);
    }
  }

  class GetProcessInstances implements Command<Set<InternalProcessInstance>> {
    /**
     * 
     */
    private static final long serialVersionUID = 8568074371790934617L;
    private final ProcessDefinitionUUID processDefUUID;

    public GetProcessInstances(final ProcessDefinitionUUID processDefUUID) {
      this.processDefUUID = processDefUUID;
    }
    public Set<InternalProcessInstance> execute(final Environment environment) throws Exception {
      return getHistory().getProcessInstances(this.processDefUUID);
    }
  }

  class GetProcessInstance implements Command<ProcessInstance> {
    /**
     * 
     */
    private static final long serialVersionUID = -1233442971215657292L;
    private final ProcessInstanceUUID processInstanceUUID;

    public GetProcessInstance(final ProcessInstanceUUID instanceUUID) {
      this.processInstanceUUID = instanceUUID;
    }
    public ProcessInstance execute(final Environment environment) throws Exception {
      final ProcessInstance processInstance = getHistory().getProcessInstance(this.processInstanceUUID);
      if (processInstance != null) {
        // Load process instance attributes to prevent LazyInitializationException
        for (final Object o : processInstance.getInitialVariableValues().values()) {
          o.toString();
        }
        for (final Object o : processInstance.getLastKnownVariableValues().values()) {
          o.toString();
        }
        for (final VariableUpdate o : processInstance.getVariableUpdates()) {
          o.toString();
        }
        for (final ActivityInstance activity : processInstance.getActivities()) {
          activity.toString();
        }
      }
      return processInstance;
    }

  }

  class GetActivityInstances implements Command<Set<InternalActivityInstance>> {
    /**
     * 
     */
    private static final long serialVersionUID = -2133035331171368188L;
    private final ProcessInstanceUUID processInstanceUUID;

    public GetActivityInstances(final ProcessInstanceUUID instanceUUID) {
      this.processInstanceUUID = instanceUUID;
    }
    public Set<InternalActivityInstance> execute(final Environment environment) throws Exception {
      return getHistory().getActivityInstances(this.processInstanceUUID);
    }

  }

  class GetActivityInstance implements Command<ActivityInstance> {

    private static final long serialVersionUID = -7665263370176516500L;
    private final ProcessInstanceUUID processInstanceUUID;
    private final String activityId;
    private final String iterationId;
    private final String activityInstanceId;
    private final String loopId;

    public GetActivityInstance(final ProcessInstanceUUID instanceUUID,
        final String activityId, final String iterationId, final String activityInstanceId, final String loopId) {
      this.processInstanceUUID = instanceUUID;
      this.activityId = activityId;
      this.iterationId = iterationId;
      this.activityInstanceId = activityInstanceId;
      this.loopId = loopId;
    }
    public ActivityInstance execute(final Environment environment) throws Exception {
      return getHistory().getActivityInstance(this.processInstanceUUID, this.activityId, this.iterationId, this.activityInstanceId, this.loopId);
    }

  }

  class GetActivityInstanceOnlyUUID implements Command<ActivityInstance> {
    /**
     * 
     */
    private static final long serialVersionUID = -7788725939578050007L;
    private final ActivityInstanceUUID actInstanceUUID;

    public GetActivityInstanceOnlyUUID(final ActivityInstanceUUID instanceUUID) {
      this.actInstanceUUID = instanceUUID;
    }
    public ActivityInstance execute(final Environment environment) throws Exception {
      return getHistory().getActivityInstance(this.actInstanceUUID);
    }

  }

  class GetLastDeployedProcess implements Command<ProcessDefinition> {

    /**
     * 
     */
    private static final long serialVersionUID = -1125399157847476928L;
    private final String processId;
    private final ProcessDefinition.ProcessState processState;

    public GetLastDeployedProcess(final String processId, final ProcessDefinition.ProcessState processState) {
      this.processId = processId;
      this.processState = processState;
    }
    public ProcessDefinition execute(final Environment environment) throws Exception {
      final ProcessDefinition process = getHistory().getLastDeployedProcess(this.processId, this.processState);
      return process;
    }
  }

  class GetProcessesCommand implements Command<Set<InternalProcessDefinition>> {

    /**
     * 
     */
    private static final long serialVersionUID = 5874326128608389947L;

    public Set<InternalProcessDefinition> execute(final Environment environment) throws Exception {
      final Set<InternalProcessDefinition> processes = getHistory().getProcesses();
      if (processes != null) {
        //Load processes to prevent LazyInitializationException
    	  processes.toString();
      }
      return processes;
    }
  }

  class GetProcessesCommandWithId implements Command<Set<InternalProcessDefinition>> {

    /**
     * 
     */
    private static final long serialVersionUID = 6559499951223096884L;
    private String processId = null;

    public GetProcessesCommandWithId(final String processId) {
      this.processId = processId;
    }

    public Set<InternalProcessDefinition> execute(final Environment environment) throws Exception {
      final Set<InternalProcessDefinition> processes = getHistory().getProcesses(this.processId);
      if (processes != null) {
        //Load processes to prevent LazyInitializationException
    	  processes.toString();
      }
      return processes;
    }
  }

  static class ArchiveCommand implements Command<Object> {

    /**
     * 
     */
    private static final long serialVersionUID = -285059260009674418L;
    private InternalProcessDefinition processDef = null;
    private InternalProcessInstance processInstance = null;

    public ArchiveCommand(final InternalProcessDefinition processDef) {
      this.processDef = processDef;
    }
    public ArchiveCommand(final InternalProcessInstance processInstance) {
      this.processInstance = processInstance;
    }

    public Object execute(final Environment environment) throws Exception {
      final Archiver archiver = EnvTool.getArchiver();
      if (this.processDef != null) {
        archiver.archive(this.processDef);
        return this.processDef;
      }
      archiver.archive(this.processInstance);
      return this.processInstance;
    }
  }
}
