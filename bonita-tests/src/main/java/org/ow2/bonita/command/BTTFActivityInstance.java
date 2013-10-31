/**
 * Copyright (C) 2010  BonitaSoft S.A.
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
package org.ow2.bonita.command;

import java.util.Date;

import org.ow2.bonita.env.Environment;
import org.ow2.bonita.facade.runtime.ProcessInstance;
import org.ow2.bonita.facade.runtime.impl.InternalActivityInstance;
import org.ow2.bonita.facade.uuid.ActivityInstanceUUID;
import org.ow2.bonita.util.Command;
import org.ow2.bonita.util.EnvTool;

/**
 * 
 * @author Matthieu Chaffotte
 *
 */
public class BTTFActivityInstance implements Command<ProcessInstance> {

  private static final long serialVersionUID = 737710033782104879L;
  private ActivityInstanceUUID activityUUID;
  private Date start;
  private Date end;

  public BTTFActivityInstance(ActivityInstanceUUID activityUUID, Date end) {
    this.activityUUID = activityUUID;
    this.start = null;
    this.end = new Date(end.getTime() - 300000);
  }
  
  public BTTFActivityInstance(ActivityInstanceUUID activityUUID, Date start, Date end) {
    this.activityUUID = activityUUID;
    this.start = start;
    this.end = end;
  }

  public ProcessInstance execute(Environment environment) throws Exception {
    InternalActivityInstance activity = (InternalActivityInstance) EnvTool.getAllQueriers().getActivityInstance(activityUUID);
    if (start == null) {
      activity.setExpectedEndDate(end);
    } else {
      activity.setReadyDate(start);
      activity.setEndedDate(end);
    }
    return null;
  }
}
