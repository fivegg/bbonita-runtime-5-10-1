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
 * This class implements the UUID for {@link org.ow2.bonita.facade.def.majorElement.TransitionDefinition}
 */
public class TransitionDefinitionUUID extends AbstractUUID {

  private static final long serialVersionUID = 7467868251623220453L;

  protected TransitionDefinitionUUID() {
    super();
  }

  public TransitionDefinitionUUID(final TransitionDefinitionUUID src) {
    super(src);
  }

  public TransitionDefinitionUUID(final String value) {
    super(value);
  }

  public TransitionDefinitionUUID(final ProcessDefinitionUUID processUUID, final String transitionName) {
    this(processUUID + SEPARATOR + transitionName);
  }

  @Deprecated
  public ProcessDefinitionUUID getProcessUUID() {
    final String processUUID = value.substring(0, value.lastIndexOf(SEPARATOR));
    return new ProcessDefinitionUUID(processUUID);
  }

  @Deprecated
  public String getTransitionName() {
    return value.substring(value.lastIndexOf(SEPARATOR) + SEPARATOR.length());
  }

}
