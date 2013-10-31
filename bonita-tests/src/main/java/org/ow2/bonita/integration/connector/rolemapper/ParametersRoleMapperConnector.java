/**
 * Copyright (C) 2009  BonitaSoft S.A..
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
package org.ow2.bonita.integration.connector.rolemapper;

import java.util.HashSet;
import java.util.Set;

import org.ow2.bonita.connector.core.RoleResolver;

/**
 * @author Matthieu Chaffotte
 *
 */
public class ParametersRoleMapperConnector extends RoleResolver {

	private String aUser;
	private String bUser;
	private String cUser;
	
	public String getaUser() {
  	return aUser;
  }

	public String getbUser() {
  	return bUser;
  }

	public String getcUser() {
  	return cUser;
  }

	public void setAUser(String aUser) {
  	this.aUser = aUser;
  }
	public void setBUser(String bUser) {
  	this.bUser = bUser;
  }

	public void setCUser(String cUser) {
  	this.cUser = cUser;
  }

	@Override
	protected Set<String> getMembersSet(String roleId) throws Exception {
		Set<String> team = new HashSet<String>();
		team.add(aUser);
		team.add(bUser);
		team.add(cUser);
		return team;
	}
}
