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
package org.ow2.bonita.facade.runtime.impl;

import org.ow2.bonita.facade.runtime.RuntimeRecord;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.util.Misc;

/**
 * @author Pierre Vigneras
 */
public abstract class RuntimeRecordImpl implements RuntimeRecord {

  private static final long serialVersionUID = -5283156122103821954L;
  protected ProcessDefinitionUUID processUUID;
  protected ProcessInstanceUUID instanceUUID;
  protected ProcessInstanceUUID rootInstanceUUID;

  protected RuntimeRecordImpl() {
    super();
  }

  protected RuntimeRecordImpl(final ProcessDefinitionUUID processUUID, 
      final ProcessInstanceUUID instanceUUID, final ProcessInstanceUUID rootInstanceUUID) {
    Misc.checkArgsNotNull(processUUID, instanceUUID, rootInstanceUUID);
    this.processUUID = processUUID;
    this.instanceUUID = instanceUUID;
    this.rootInstanceUUID = rootInstanceUUID;
  }

  protected RuntimeRecordImpl(final RuntimeRecord src) {
    Misc.checkArgsNotNull(src);
    this.processUUID = new ProcessDefinitionUUID(src.getProcessDefinitionUUID());
    this.instanceUUID = new ProcessInstanceUUID(src.getProcessInstanceUUID());
    this.rootInstanceUUID = new ProcessInstanceUUID(src.getRootInstanceUUID());
  }

  public ProcessDefinitionUUID getProcessDefinitionUUID() {
    return this.processUUID;
  }

  public ProcessInstanceUUID getProcessInstanceUUID() {
    return this.instanceUUID;
  }

  public ProcessInstanceUUID getRootInstanceUUID() {
    return rootInstanceUUID;
  }

}
