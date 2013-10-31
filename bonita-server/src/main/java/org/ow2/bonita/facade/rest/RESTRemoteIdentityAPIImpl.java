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
package org.ow2.bonita.facade.rest;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

import org.ow2.bonita.facade.exception.GroupNotFoundException;
import org.ow2.bonita.facade.exception.MembershipNotFoundException;
import org.ow2.bonita.facade.exception.MetadataNotFoundException;
import org.ow2.bonita.facade.exception.RoleNotFoundException;
import org.ow2.bonita.facade.exception.UserNotFoundException;
import org.ow2.bonita.facade.identity.Group;
import org.ow2.bonita.facade.identity.Membership;
import org.ow2.bonita.facade.identity.Role;
import org.ow2.bonita.facade.identity.User;
import org.ow2.bonita.facade.impl.AbstractRemoteIdentityAPIImpl;
import org.ow2.bonita.facade.internal.RESTRemoteIdentityAPI;

/**
 * 
 * @author Elias Ricken de Medeiros
 *
 */
public class RESTRemoteIdentityAPIImpl extends AbstractRemoteIdentityAPIImpl
		implements RESTRemoteIdentityAPI {
		
	public void addMembershipsToUser(String userUUID,
			List<String> membershipUUIDs, Map<String, String> options)
			throws RemoteException, UserNotFoundException,
			MembershipNotFoundException {		
		getAPI(options).addMembershipsToUser(userUUID, membershipUUIDs);
	}

	public List<Group> getGroupsByUUIDs(List<String> groupUUIDs,
			Map<String, String> options) throws RemoteException,
			GroupNotFoundException {
		return getAPI(options).getGroupsByUUIDs(groupUUIDs);
	}

	public List<Membership> getMembershipsByUUIDs(List<String> membershipUUIDs,
			Map<String, String> options) throws RemoteException,
			MembershipNotFoundException {
		return getAPI(options).getMembershipsByUUIDs(membershipUUIDs);
	}

	public List<Role> getRolesByUUIDs(List<String> roleUUIDs,
			Map<String, String> options) throws RemoteException,
			RoleNotFoundException {
		return getAPI(options).getRolesByUUIDs(roleUUIDs);
	}

	public List<User> getUsersByUUIDs(List<String> userUUIDs,
			Map<String, String> options) throws RemoteException,
			UserNotFoundException {
		return getAPI(options).getUsersByUUIDs(userUUIDs);
	}

	public void removeGroups(List<String> groupUUIDs, Map<String, String> options)
			throws RemoteException, GroupNotFoundException {
		getAPI(options).removeGroups(groupUUIDs);
	}

	public void removeMembershipsFromUser(String userUUID,
			List<String> membershipUUIDs, Map<String, String> options)
			throws RemoteException, UserNotFoundException,
			MembershipNotFoundException {
		getAPI(options).removeMembershipsFromUser(userUUID, membershipUUIDs);

	}

	public void removeProfileMetadata(List<String> profileMetadataUUIDs,
			Map<String, String> options) throws RemoteException,
			MetadataNotFoundException {
		getAPI(options).removeProfileMetadata(profileMetadataUUIDs);
	}

	public void removeRoles(List<String> roleUUIDs, Map<String, String> options)
			throws RemoteException, RoleNotFoundException {
		getAPI(options).removeRoles(roleUUIDs);
	}

	public void removeUsers(List<String> userUUIDs, Map<String, String> options)
			throws RemoteException, UserNotFoundException {
		getAPI(options).removeUsers(userUUIDs);
	}

	public void setUserMemberships(String userUUID, List<String> membershipUUIDs,
			Map<String, String> options) throws RemoteException,
			UserNotFoundException, MembershipNotFoundException {
		getAPI(options).setUserMemberships(userUUID, membershipUUIDs);
	}

}
