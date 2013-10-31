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

import org.ow2.bonita.env.Environment;
import org.ow2.bonita.facade.runtime.impl.InternalActivityInstance;
import org.ow2.bonita.facade.uuid.ActivityInstanceUUID;
import org.ow2.bonita.util.Command;
import org.ow2.bonita.util.EnvTool;

/**
 * @author Elias Ricken de Medeiros
 *
 */
public class MarkActivityAsFailed implements Command<Void> {

  private static final long serialVersionUID = 8711833108646113149L;
  private final ActivityInstanceUUID activityInstanceUUID;

  public MarkActivityAsFailed(final ActivityInstanceUUID activityInstanceUUID) {
    this.activityInstanceUUID = activityInstanceUUID;
  }
  
  @Override
  public Void execute(final Environment environment) throws Exception {
    final InternalActivityInstance activityInstance = EnvTool.getJournalQueriers().getActivityInstance(activityInstanceUUID);
    EnvTool.getRecorder().recordActivityFailed(activityInstance);
    return null;
  }

}
