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
package org.ow2.bonita.services.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ow2.bonita.facade.identity.Membership;
import org.ow2.bonita.facade.identity.ProfileMetadata;
import org.ow2.bonita.facade.identity.impl.GroupImpl;
import org.ow2.bonita.facade.identity.impl.MembershipImpl;
import org.ow2.bonita.facade.identity.impl.ProfileMetadataImpl;
import org.ow2.bonita.facade.identity.impl.RoleImpl;
import org.ow2.bonita.facade.identity.impl.UserImpl;
import org.ow2.bonita.facade.paging.GroupCriterion;
import org.ow2.bonita.facade.paging.RoleCriterion;
import org.ow2.bonita.facade.paging.UserCriterion;
import org.ow2.bonita.persistence.IdentityDbSession;
import org.ow2.bonita.persistence.db.HibernateDbSession;
import org.ow2.bonita.services.IdentityService;
import org.ow2.bonita.util.EnvTool;
import org.ow2.bonita.util.Misc;

/**
 * @author Anthony Birembaut, Matthieu Chaffotte
 *
 */
public class DbIdentity extends HibernateDbSession implements IdentityService {

  private String persistenceServiceName;

  public DbIdentity(String persistenceServiceName) {
    super();
    this.persistenceServiceName = persistenceServiceName;
  }

  protected IdentityDbSession getDbSession() {
    return EnvTool.getIdentityDbSession(persistenceServiceName);
  }

  public void addRole(RoleImpl role) {
    getDbSession().save(role);
  }

  public void addUser(UserImpl user) {
    String passwordHash = Misc.hash(user.getPassword());
    user.setPassword(passwordHash);
    getDbSession().save(user);
  }

  public void importUser(UserImpl user) {
    getDbSession().save(user);
  }

  public void deleteRole(RoleImpl role) {
    getDbSession().delete(role);
  }

  public void deleteUser(UserImpl user) {
    Map<ProfileMetadata, String> metadata = user.getMetadata();
    for (ProfileMetadata profileMetadata : metadata.keySet()) {
      ProfileMetadataImpl impl = (ProfileMetadataImpl) profileMetadata;
      impl.removeUser(user);
    }
    getDbSession().delete(user);
  }

  public void addGroup(GroupImpl group) {
    getDbSession().save(group);
  }

  public void addMembership(MembershipImpl membership) {
    getDbSession().save(membership);
  }

  public void addMembershipToUser(UserImpl user, MembershipImpl membership) {
    user.getMemberships().add(membership);
  }

  public void addProfileMetadata(ProfileMetadataImpl metadata) {
    getDbSession().save(metadata);
  }

  public void deleteGroup(GroupImpl group) {
    getDbSession().delete(group);
  }

  public void deleteMembership(MembershipImpl membership) {
    getDbSession().delete(membership);
  }

  public void deleteProfileMetadata(ProfileMetadataImpl metadata) {
    getDbSession().delete(metadata);
  }

  public List<GroupImpl> getAllGroups() {
    return getDbSession().getAllGroups();
  }

  public Set<MembershipImpl> getAllMemberships() {
    return getDbSession().getAllMemberships();
  }

  public List<ProfileMetadataImpl> getAllProfileMetadata() {
    return getDbSession().getAllProfileMetadata();
  }

  public List<RoleImpl> getAllRoles() {
    return getDbSession().getAllRoles();
  }

  public List<UserImpl> getAllUsers() {
    return getDbSession().getAllUsers();
  }

  public Set<GroupImpl> findGroupsByName(String name) {
    return getDbSession().findGroupsByName(name);
  }

  public List<GroupImpl> getGroupChildren(String parentGroupUUID) {
    return getDbSession().getGroupChildren(parentGroupUUID);
  }

  public List<GroupImpl> getGroupChildren(String parentGroupUUID, int fromIndex, int numberOfGroups) {
    return getDbSession().getGroupChildren(parentGroupUUID, fromIndex, numberOfGroups);
  }

  public List<GroupImpl> getGroupChildren(String parentGroupUUID,
      int fromIndex, int numberOfGroups, GroupCriterion pagingCriterion) {
    return getDbSession().getGroupChildren(parentGroupUUID, fromIndex, numberOfGroups, pagingCriterion);
  }

  public int getNumberOfGroupChildren(String parentGroupUUID) {
    return getDbSession().getNumberOfGroupChildren(parentGroupUUID);
  }

  public RoleImpl findRoleByName(String roleName) {
    return getDbSession().findRoleByName(roleName);
  }

  public UserImpl findUserByUsername(String username) {
    return getDbSession().findUserByUsername(username);
  }

  public ProfileMetadataImpl findProfileMetadataByName(String metadataName) {
    return getDbSession().findProfileMetadataByName(metadataName);
  }

  public void removeMembershipFromUser(UserImpl user, MembershipImpl membership) {
    user.getMemberships().remove(membership);
  }

  public void setUserMemberships(UserImpl user, Set<MembershipImpl> memberships) {
    Set<Membership> membershipsToSet = new HashSet<Membership>(memberships);
    user.setMemberships(membershipsToSet);
  }

  public void updateGroup(GroupImpl group) {
    //Nothing to do. Hibernate flush deals with the update
  }

  public void updateProfileMetadata(ProfileMetadataImpl profileMetadata) {
    //Nothing to do. Hibernate flush deals with the update
  }

  public void updateRole(RoleImpl role) {
    //Nothing to do. Hibernate flush deals with the update
  }

  public void updateUser(UserImpl user) {
    //Nothing to do. Hibernate flush deals with the update
  }

  public void updateUserPassword(UserImpl user, String password) {
    user.setPassword(Misc.hash(password));
    //Nothing else to do. Hibernate flush deals with the update
  }

  public GroupImpl getGroup(String groupUUID) {
    return getDbSession().getGroup(groupUUID);
  }

  public RoleImpl getRole(String roleUUID) {
    return getDbSession().getRole(roleUUID);
  }

  public UserImpl getUser(String userUUID) {
    return getDbSession().getUser(userUUID);
  }

  public MembershipImpl getMembership(String membershipUUID) {
    return getDbSession().getMembership(membershipUUID);
  }

  public MembershipImpl findMembershipByRoleAndGroup(String roleUUID, String groupUUID) {
    return getDbSession().findMembershipByRoleAndGroup(roleUUID, groupUUID);
  }

  public Set<MembershipImpl> getMembershipsByGroup(String groupUUID) {
    return getDbSession().getMembershipsByGroup(groupUUID);
  }

  public Set<MembershipImpl> getMembershipsByRole(String roleUUID) {
    return getDbSession().getMembershipsByRole(roleUUID);
  }

  public List<UserImpl> getUsersByGroup(String groupUUID) {
    return getDbSession().getUsersByGroup(groupUUID);
  }

  public List<UserImpl> getUsersByMembership(String membershipUUID) {
    return getDbSession().getUsersByMembership(membershipUUID);
  }

  public List<UserImpl> getUsersByRole(String roleUUID) {
    return getDbSession().getUsersByRole(roleUUID);
  }

  public List<UserImpl> getUsersByManager(String managerUUID) {
    return getDbSession().getUsersByManager(managerUUID);
  }

  public List<UserImpl> getUsersByDelegee(String delegeeUUID) {
    return getDbSession().getUsersByDelegee(delegeeUUID);
  }

  public ProfileMetadataImpl getProfileMetadata(String profileMetadataUUID) {
    return getDbSession().getProfileMetadata(profileMetadataUUID);
  }

  public List<GroupImpl> getGroups(int fromIndex, int numberOfGroups) {
    return getDbSession().getGroups(fromIndex, numberOfGroups);
  }

  public List<GroupImpl> getGroups(int fromIndex, int numberOfGroups,
      GroupCriterion pagingCriterion) {
    return getDbSession().getGroups(fromIndex, numberOfGroups, pagingCriterion);
  }

  public int getNumberOfGroups() {
    return getDbSession().getNumberOfGroups();
  }

  public int getNumberOfRoles() {
    return getDbSession().getNumberOfRoles();
  }

  public int getNumberOfUsers() {
    return getDbSession().getNumberOfUsers();
  }

  public int getNumberOfUsersByGroup(String groupUUID) {
    return getDbSession().getNumberOfUsersByGroup(groupUUID);
  }

  public int getNumberOfUsersByRole(String roleUUID) {
    return getDbSession().getNumberOfUsersByRole(roleUUID);
  }

  public List<RoleImpl> getRoles(int fromIndex, int numberOfRoles) {
    return getDbSession().getRoles(fromIndex, numberOfRoles);
  }

  public List<RoleImpl> getRoles(int fromIndex, int numberOfRoles,
      RoleCriterion pagingCriterion) {
    return getDbSession().getRoles(fromIndex, numberOfRoles, pagingCriterion);
  }

  public List<UserImpl> getUsers(int fromIndex, int numberOfUsers) {
    return getDbSession().getUsers(fromIndex, numberOfUsers);
  }

  public List<UserImpl> getUsers(int fromIndex, int numberOfUsers,
      UserCriterion pagingCriterion) {
    return getDbSession().getUsers(fromIndex, numberOfUsers, pagingCriterion);
  }

  public List<UserImpl> getUsersByGroup(String groupUUID, int fromIndex, int numberOfUsers) {
    return getDbSession().getUsersByGroup(groupUUID, fromIndex, numberOfUsers);
  }

  public List<UserImpl> getUsersByGroup(String groupUUID, int fromIndex,
      int numberOfUsers, UserCriterion pagingCriterion) {
    return getDbSession().getUsersByGroup(groupUUID, fromIndex, numberOfUsers, pagingCriterion);
  }

  public List<UserImpl> getUsersByRole(String roleUUID, int fromIndex, int numberOfUsers) {
    return getDbSession().getUsersByRole(roleUUID, fromIndex, numberOfUsers);
  }

  public List<UserImpl> getUsersByRole(String roleUUID, int fromIndex,
      int numberOfUsers, UserCriterion pagingCriterion) {
    return getDbSession().getUsersByRole(roleUUID, fromIndex, numberOfUsers, pagingCriterion);
  }

  public List<ProfileMetadataImpl> getProfileMetadata(int fromIndex, int numberOfMetadata) {
    return getDbSession().getProfileMetadata(fromIndex, numberOfMetadata);
  }

  public int getNumberOfProfileMetadata() {
    return getDbSession().getNumberOfProfileMetadata();
  }

  public List<GroupImpl> getGroups(Collection<String> groupUUIDs) {
    return getDbSession().getGroups(groupUUIDs);
  }

  public List<MembershipImpl> getMemberships(Collection<String> membershipUUIDs) {
    return getDbSession().getMemberships(membershipUUIDs);
  }

  public List<RoleImpl> getRoles(Collection<String> roleUUIDs) {
    return getDbSession().getRoles(roleUUIDs);
  }

  public List<UserImpl> getUsers(Collection<String> userUUIDs) {
    return getDbSession().getUsers(userUUIDs);
  }

}
