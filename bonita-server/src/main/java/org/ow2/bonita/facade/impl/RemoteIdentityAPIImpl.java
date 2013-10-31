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
package org.ow2.bonita.facade.impl;

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
import org.ow2.bonita.facade.internal.RemoteIdentityAPI;

/**
 * 
 * @author Elias Ricken de Medeiros, Matthieu Chaffotte
 *
 */
public class RemoteIdentityAPIImpl extends AbstractRemoteIdentityAPIImpl implements RemoteIdentityAPI {

  public void addMembershipsToUser(String userUUID, Collection<String> membershipUUIDs, final Map<String, String> options)
  throws RemoteException,
  UserNotFoundException, MembershipNotFoundException {
    getAPI(options).addMembershipsToUser(userUUID, membershipUUIDs);
  }

  public void removeMembershipsFromUser(String userUUID, Collection<String> membershipUUIDs, final Map<String, String> options)
  throws RemoteException,
  UserNotFoundException, MembershipNotFoundException {
    getAPI(options).removeMembershipsFromUser(userUUID, membershipUUIDs);
  }

  public void setUserMemberships(String userUUID, Collection<String> membershipUUIDs, final Map<String, String> options)
  throws RemoteException, UserNotFoundException, MembershipNotFoundException {
    getAPI(options).setUserMemberships(userUUID, membershipUUIDs);
  }

  public void removeGroups(Collection<String> groupUUIDs, final Map<String, String> options)
  throws RemoteException, GroupNotFoundException {
    getAPI(options).removeGroups(groupUUIDs);
  }

  public void removeRoles(Collection<String> roleUUIDs, final Map<String, String> options)
  throws RemoteException, RoleNotFoundException {
    getAPI(options).removeRoles(roleUUIDs);
  }

  public void removeUsers(Collection<String> userUUIDs, final Map<String, String> options)
  throws RemoteException, UserNotFoundException {
    getAPI(options).removeUsers(userUUIDs);
  }

  public void removeProfileMetadata(Collection<String> profileMetadataUUIDs, final Map<String, String> options)
  throws RemoteException, MetadataNotFoundException {
    getAPI(options).removeProfileMetadata(profileMetadataUUIDs);
  }

  public List<Group> getGroupsByUUIDs(Collection<String> groupUUIDs, Map<String, String> options)
  throws RemoteException, GroupNotFoundException {
    return getAPI(options).getGroupsByUUIDs(groupUUIDs);
  }

  public List<Membership> getMembershipsByUUIDs(Collection<String> membershipUUIDs, Map<String, String> options)
  throws RemoteException, MembershipNotFoundException {
    return getAPI(options).getMembershipsByUUIDs(membershipUUIDs);
  }

  public List<Role> getRolesByUUIDs(Collection<String> roleUUIDs, Map<String, String> options)
  throws RemoteException, RoleNotFoundException {
    return getAPI(options).getRolesByUUIDs(roleUUIDs);
  }

  public List<User> getUsersByUUIDs(Collection<String> userUUIDs, Map<String, String> options)
  throws RemoteException, UserNotFoundException {
    return getAPI(options).getUsersByUUIDs(userUUIDs);
  }

}
