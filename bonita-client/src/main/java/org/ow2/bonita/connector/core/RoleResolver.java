/**
 * Copyright (C) 2009  BonitaSoft S.A.
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
package org.ow2.bonita.connector.core;

import java.util.List;
import java.util.Set;

import org.ow2.bonita.definition.RoleMapper;
import org.ow2.bonita.facade.QueryAPIAccessor;
import org.ow2.bonita.facade.impl.StandardAPIAccessorImpl;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;

/**
 * A Role Resolver searches and returns the possible members which can execute a task
 * @author Matthieu Chaffotte
 *
 */
public abstract class RoleResolver extends Mapper implements RoleMapper {

  private String roleId;

  public String getRoleId() {
    return roleId;
  }

  public void setRoleId(String roleId) {
    this.roleId = roleId;
  }

	@Override
  protected List<ConnectorError> validateValues() {
    List<ConnectorError> errors = super.validateValues();
    if (roleId.length() == 0) {
      errors.add(new ConnectorError("roleId",
          new IllegalArgumentException("cannot be emtpy")));
    }
    return errors;
  }

	protected abstract Set<String> getMembersSet(String roleId) throws Exception;

	@Override
	protected final void executeConnector() throws Exception {
		setMembers(getMembersSet(getRoleId()));
	}

  public final Set<String> searchMembers(QueryAPIAccessor accessor,
      ProcessInstanceUUID instanceUUID, String roleId) throws Exception {
    setRoleId(roleId);
    setProcessInstanceUUID(instanceUUID);
    setApiAccessor(new StandardAPIAccessorImpl());
    execute();
    return getMembers();
  }

}
