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
package org.ow2.bonita.services.impl;

import org.ow2.bonita.facade.def.InternalProcessDefinition;
import org.ow2.bonita.facade.runtime.impl.InternalProcessInstance;
import org.ow2.bonita.services.History;
import org.ow2.bonita.util.ExceptionManager;
import org.ow2.bonita.util.Misc;

/**
 * 
 * @author Thomas Gueze
 * 
 */
public class DbHistory extends AbstractDbQuerier implements History {

  public DbHistory(final String name) {
    super(name);
  }

  @Override
  public void archive(final InternalProcessDefinition process) {
    Misc.checkArgsNotNull(process);
    if (getProcess(process.getUUID()) == null) {
      getDbSession().save(process);
    } else {
      final String message = ExceptionManager.getInstance().getFullMessage("bsi_DH_1", process);
      throw new IllegalArgumentException(message);
    }
  }

  @Override
  public void archive(final InternalProcessInstance processInst) {
    Misc.checkArgsNotNull(processInst);
    if (getProcessInstance(processInst.getUUID()) == null) {
      getDbSession().save(processInst);
    } else {
      final String message = ExceptionManager.getInstance().getFullMessage("bsi_DH_2", processInst);
      throw new IllegalArgumentException(message);
    }
  }

  @Override
  public void remove(final InternalProcessDefinition process) {
    Misc.checkArgsNotNull(process);
    getDbSession().delete(process);
  }

  @Override
  public void remove(final InternalProcessInstance processInst) {
    Misc.checkArgsNotNull(processInst);
    getDbSession().delete(processInst);
  }

}
