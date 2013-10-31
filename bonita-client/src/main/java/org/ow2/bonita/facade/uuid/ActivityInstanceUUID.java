/**
 * Copyright (C) 2006  Bull S. A. S.
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
 * Modified by Matthieu Chaffotte - BonitaSoft S.A.
 **/
package org.ow2.bonita.facade.uuid;

/**
 * This class implements the UUID for {@link org.ow2.bonita.facade.runtime.ActivityInstance}
 */
public class ActivityInstanceUUID extends AbstractUUID {

  private static final long serialVersionUID = 4202285835962832398L;

  protected ActivityInstanceUUID() {
    super();
  }

  public ActivityInstanceUUID(ActivityInstanceUUID src) {
    super(src);
  }

  public ActivityInstanceUUID(String value) {
    super(value);
  }

  public ActivityInstanceUUID(final ProcessInstanceUUID instanceUUID, final String activityName,
      final String iterationId, final String activityInstanceId, final String loopId) {
    this(instanceUUID + SEPARATOR + activityName + SEPARATOR + iterationId + SEPARATOR + activityInstanceId + SEPARATOR + loopId);
  }

  @Deprecated
  public ProcessInstanceUUID getProcessInstanceUUID() {
    String[] values = value.split(SEPARATOR);
    int max = values.length - 5;
    StringBuilder builder = new StringBuilder();
    for (int i = 0 ; i < max ; i++) {
      builder.append(values[i]).append(SEPARATOR);
    }
    builder.append(values[max]);
    String processInstanceUUID = builder.toString();
    return new ProcessInstanceUUID(processInstanceUUID);
  }

  @Deprecated
  public String getActivityName() {
    String[] values = value.split(SEPARATOR);
    return values[values.length - 4];
  }

  @Deprecated
  public String getIterationId() {
    String[] values = value.split(SEPARATOR);
    return values[values.length - 3];
  }

  @Deprecated
  public String getActivityInstanceId() {
    String[] values = value.split(SEPARATOR);
    return values[values.length - 2];
  }

  @Deprecated
  public String getLoopId() {
    String[] values = value.split(SEPARATOR);
    return values[values.length - 1];
  }

  @Deprecated
  public ActivityDefinitionUUID getActivityDefinitionUUID() {
    return new ActivityDefinitionUUID(getProcessInstanceUUID().getProcessDefinitionUUID(), getActivityName());
  }

}
