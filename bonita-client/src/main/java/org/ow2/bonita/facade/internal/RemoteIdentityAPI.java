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
package org.ow2.bonita.facade.internal;

import java.rmi.RemoteException;
import java.util.Collection;
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

/**
 * @author Anthony Birembaut, Matthieu Chaffotte, Elias Ricken de Medeiros
 *
 */
public interface RemoteIdentityAPI extends AbstractRemoteIdentityAPI {

	void removeProfileMetadata(
      Collection<String> profileMetadataUUIDs, 
  		final Map<String, String> options)
	throws RemoteException, MetadataNotFoundException;

	void removeRoles(
      Collection<String> roleUUIDs,
      final Map<String, String> options) 
	throws RemoteException, RoleNotFoundException;

	void removeGroups(
      Collection<String> groupUUIDs,
      final Map<String, String> options) 
	throws RemoteException, GroupNotFoundException;

	void addMembershipsToUser(
      String userUUID,
      Collection<String> membershipUUIDs, 
  		final Map<String, String> options)
	throws RemoteException, UserNotFoundException, MembershipNotFoundException;

	void removeMembershipsFromUser(
      String userUUID,
      Collection<String> membershipUUIDs, 
  		final Map<String, String> options)
	throws RemoteException, UserNotFoundException, MembershipNotFoundException;

	void setUserMemberships(
      String userUUID,
      Collection<String> membershipUUIDs, 
  		final Map<String, String> options)
	throws RemoteException, UserNotFoundException, MembershipNotFoundException;

	void removeUsers(
      Collection<String> userUUIDs,
      final Map<String, String> options)
	throws RemoteException, UserNotFoundException;

	List<User> getUsersByUUIDs(
      Collection<String> userUUIDs,
      final Map<String, String> options) 
  throws RemoteException, UserNotFoundException;

  List<Role> getRolesByUUIDs(
      Collection<String> roleUUIDs,
      final Map<String, String> options) 
  throws RemoteException, RoleNotFoundException;

	List<Group> getGroupsByUUIDs(
      Collection<String> groupUUIDs,
      final Map<String, String> options) 
  throws RemoteException, GroupNotFoundException;

	List<Membership> getMembershipsByUUIDs(
      Collection<String> membershipUUIDs, 
  		final Map<String, String> options)
  throws RemoteException, MembershipNotFoundException;

}
