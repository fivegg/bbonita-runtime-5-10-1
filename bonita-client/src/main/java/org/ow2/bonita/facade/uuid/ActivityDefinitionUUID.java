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
 * This class implements the UUID for {@link org.ow2.bonita.facade.def.majorElement.ActivityDefinition}
 */
public class ActivityDefinitionUUID extends AbstractUUID {

  private static final long serialVersionUID = 2993426242623621638L;

  protected ActivityDefinitionUUID() {
    super();
  }

  public ActivityDefinitionUUID(ActivityDefinitionUUID src) {
    super(src);
  }

  public ActivityDefinitionUUID(String value) {
    super(value);
  }

  public ActivityDefinitionUUID(final ProcessDefinitionUUID processUUID, final String activityName) {
    this(processUUID + SEPARATOR + activityName);
  }

  @Deprecated
  public ProcessDefinitionUUID getProcessUUID() {
    final String processUUID = value.substring(0, value.lastIndexOf(SEPARATOR));
    return new ProcessDefinitionUUID(processUUID);
  }

  @Deprecated
  public String getActivityName() {
    return value.substring(value.lastIndexOf(SEPARATOR) + SEPARATOR.length());
  }

}
