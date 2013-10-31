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
package org.ow2.bonita.services;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.ow2.bonita.facade.identity.impl.GroupImpl;
import org.ow2.bonita.facade.identity.impl.MembershipImpl;
import org.ow2.bonita.facade.identity.impl.ProfileMetadataImpl;
import org.ow2.bonita.facade.identity.impl.RoleImpl;
import org.ow2.bonita.facade.identity.impl.UserImpl;
import org.ow2.bonita.facade.paging.GroupCriterion;
import org.ow2.bonita.facade.paging.RoleCriterion;
import org.ow2.bonita.facade.paging.UserCriterion;

/**
 * @author Anthony Birembaut
 *
 */
public interface IdentityService {
  
  UserImpl findUserByUsername(String username);
  RoleImpl findRoleByName(String roleName);
  Set<GroupImpl> findGroupsByName(String groupName);
  ProfileMetadataImpl findProfileMetadataByName(String metadataName);
  
  UserImpl getUser(String userUUID);
  RoleImpl getRole(String roleUUID);
  GroupImpl getGroup(String groupUUID);
  MembershipImpl getMembership(String membershipUUID);
  ProfileMetadataImpl getProfileMetadata(String profileMetadataUUID);
   
  MembershipImpl findMembershipByRoleAndGroup(String roleUUID, String groupUUID);
  
  List<RoleImpl> getAllRoles();
  List<RoleImpl> getRoles(int fromIndex, int numberOfRoles);
  List<RoleImpl> getRoles(int fromIndex, int numberOfRoles, RoleCriterion pagingCriterion);
  int getNumberOfRoles();
  
  List<GroupImpl> getAllGroups();
  List<GroupImpl> getGroupChildren(String parentGroupUUID);
  List<GroupImpl> getGroups(int fromIndex, int numberOfGroups);
  List<GroupImpl> getGroups(int fromIndex, int numberOfGroups, GroupCriterion pagingCriterion);
  int getNumberOfGroups();
  List<GroupImpl> getGroupChildren(String parentGroupUUID, int fromIndex, int numberOfGroups);
  List<GroupImpl> getGroupChildren(String parentGroupUUID, int fromIndex, int numberOfGroups, GroupCriterion pagingCriterion);
  int getNumberOfGroupChildren(String parentGroupUUID);
  
  List<ProfileMetadataImpl> getAllProfileMetadata();
  List<ProfileMetadataImpl> getProfileMetadata(int fromIndex, int numberOfMetadata);
  int getNumberOfProfileMetadata();
  
  List<UserImpl> getAllUsers();
  List<UserImpl> getUsersByRole(String roleUUID);
  List<UserImpl> getUsersByGroup(String groupUUID);
  List<UserImpl> getUsersByMembership(String membershipUUID);
  List<UserImpl> getUsers(int fromIndex, int numberOfUsers);
  List<UserImpl> getUsers(int fromIndex, int numberOfUsers, UserCriterion pagingCriterion);
  int getNumberOfUsers();
  List<UserImpl> getUsersByGroup(String groupUUID, int fromIndex, int numberOfUsers);
  List<UserImpl> getUsersByGroup(String groupUUID, int fromIndex, int numberOfUsers, UserCriterion pagingCriterion);
  int getNumberOfUsersByGroup(String groupUUID);
  List<UserImpl> getUsersByRole(String roleUUID, int fromIndex, int numberOfUsers);
  List<UserImpl> getUsersByRole(String roleUUID, int fromIndex, int numberOfUsers, UserCriterion pagingCriterion);
  int getNumberOfUsersByRole(String roleUUID);
  List<UserImpl> getUsersByManager(String managerUUID);
  List<UserImpl> getUsersByDelegee(String delegateUUID);
  
  Set<MembershipImpl> getAllMemberships();
  Set<MembershipImpl> getMembershipsByGroup(String groupUUID);
  Set<MembershipImpl> getMembershipsByRole(String roleUUID);
  
  void addUser(UserImpl user);
  void importUser(UserImpl user);
  void updateUser(UserImpl user);
  void updateUserPassword(UserImpl user, String password);
  void addProfileMetadata(ProfileMetadataImpl metadata);
  void updateProfileMetadata(ProfileMetadataImpl metadata);
  void addRole(RoleImpl role);
  void updateRole(RoleImpl role);
  void addGroup(GroupImpl group);
  void updateGroup(GroupImpl group);
  void addMembership(MembershipImpl membership);
  
  void deleteUser(UserImpl user);
  void deleteProfileMetadata(ProfileMetadataImpl metadata);
  void deleteRole(RoleImpl role);
  void deleteGroup(GroupImpl group);
  void deleteMembership(MembershipImpl membership);
  
  void addMembershipToUser(UserImpl user, MembershipImpl membership);
  void removeMembershipFromUser(UserImpl user, MembershipImpl membership);
  void setUserMemberships(UserImpl user, Set<MembershipImpl> memberships);
  
  List<UserImpl> getUsers(Collection<String> userUUIDs);
  List<RoleImpl> getRoles(Collection<String> roleUUIDs);
  List<GroupImpl> getGroups(Collection<String> groupUUIDs);
  List<MembershipImpl> getMemberships(Collection<String> membershipUUIDs);

}
