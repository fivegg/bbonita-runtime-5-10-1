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
package org.ow2.bonita.facade.def.majorElement.impl;

import org.ow2.bonita.facade.def.element.RoleMapperDefinition;
import org.ow2.bonita.facade.def.element.impl.ConnectorDefinitionImpl;
import org.ow2.bonita.facade.def.majorElement.ParticipantDefinition;
import org.ow2.bonita.facade.uuid.ParticipantDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;

public class ParticipantDefinitionImpl extends ProcessElementImpl implements ParticipantDefinition {

  private static final long serialVersionUID = 72293066997775691L;

  protected ParticipantDefinitionUUID uuid;

  protected RoleMapperDefinition roleMapper;

  protected ParticipantDefinitionImpl() {
    super();
  }

  public ParticipantDefinitionImpl(final ProcessDefinitionUUID processUUID, final String name) {
    super(name, processUUID);
    uuid = new ParticipantDefinitionUUID(processUUID, name);
  }

  public ParticipantDefinitionImpl(final ParticipantDefinition src) {
    super(src);
    uuid = new ParticipantDefinitionUUID(src.getUUID());
    if (src.getRoleMapper() != null) {
      roleMapper = new ConnectorDefinitionImpl(src.getRoleMapper());
    }
  }

  @Override
  public RoleMapperDefinition getRoleMapper() {
    return roleMapper;
  }

  @Override
  public ParticipantDefinitionUUID getUUID() {
    return uuid;
  }

  public void setResolver(final RoleMapperDefinition resolver) {
    roleMapper = resolver;
  }

}