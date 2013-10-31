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
import org.ow2.bonita.facade.runtime.ProcessInstance;
import org.ow2.bonita.facade.runtime.impl.InternalProcessInstance;
import org.ow2.bonita.facade.uuid.IdFactory;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.services.CommandService;
import org.ow2.bonita.services.Querier;
import org.ow2.bonita.services.Recorder;
import org.ow2.bonita.util.BonitaConstants;
import org.ow2.bonita.util.Command;
import org.ow2.bonita.util.EnvTool;

/**
 * @author Pierre Vigneras
 */
public class JournalTest extends APITestCase {

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

  public void testRecordNullProcessInstance() {
    try {
      recordInstanceStarted(null, null);
      fail("Should not accept null arguments!");
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

  private void recordInstanceStarted(final InternalProcessInstance instance, final String userId) {
    this.commandService.execute(new RecordInstanceStarted(instance, userId));
  }

  private ProcessInstance getProcessInstance(final ProcessInstanceUUID instanceUUID) {
    return this.commandService.execute(new GetProcessInstance(instanceUUID));
  }

  private Set<InternalProcessInstance> getProcessInstances(final ProcessDefinitionUUID processDefUUID) {
    return this.commandService.execute(new GetProcessInstances(processDefUUID));
  }

  // Commands

  private static class RecordInstanceStarted implements Command<InternalProcessInstance> {

    /**
     * 
     */
    private static final long serialVersionUID = -3767825497405799247L;
    private final InternalProcessInstance instance;
    private final String userId;

    public RecordInstanceStarted(final InternalProcessInstance instance, final String userId) {
      this.instance = instance;
      this.userId = userId;
    }

    public InternalProcessInstance execute(final Environment environment) throws Exception {
      final Recorder recorder = environment.get(Recorder.class);
      recorder.recordInstanceStarted(this.instance != null ? this.instance : null, this.userId);
      return this.instance;
    }
  }

  private static class GetProcessInstance implements Command<ProcessInstance> {

    /**
     * 
     */
    private static final long serialVersionUID = -1799787779339645974L;
    private final ProcessInstanceUUID instanceUUID;

    public GetProcessInstance(final ProcessInstanceUUID instanceUUID) {
      this.instanceUUID = instanceUUID;
    }

    public ProcessInstance execute(final Environment environment) throws Exception {
      final Querier journal = EnvTool.getJournalQueriers();
      return journal.getProcessInstance(this.instanceUUID);
    }
  }

  private static class GetProcessInstances implements Command<Set<InternalProcessInstance>> {

    /**
     * 
     */
    private static final long serialVersionUID = -5267798564001096606L;
    private final ProcessDefinitionUUID processDefUUID;

    public GetProcessInstances(final ProcessDefinitionUUID processDefUUID) {
      this.processDefUUID = processDefUUID;
    }

    public Set<InternalProcessInstance> execute(final Environment environment) throws Exception {
      final Querier journal = EnvTool.getJournalQueriers();
      return journal.getProcessInstances(this.processDefUUID);
    }
  }
}
