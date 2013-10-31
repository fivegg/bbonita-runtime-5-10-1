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
package org.ow2.bonita.facade.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.ow2.bonita.facade.IdentityAPI;
import org.ow2.bonita.facade.exception.BonitaWrapperException;
import org.ow2.bonita.facade.exception.GroupAlreadyExistsException;
import org.ow2.bonita.facade.exception.GroupNotFoundException;
import org.ow2.bonita.facade.exception.MembershipNotFoundException;
import org.ow2.bonita.facade.exception.MetadataAlreadyExistsException;
import org.ow2.bonita.facade.exception.MetadataNotFoundException;
import org.ow2.bonita.facade.exception.RoleAlreadyExistsException;
import org.ow2.bonita.facade.exception.RoleNotFoundException;
import org.ow2.bonita.facade.exception.UserAlreadyExistsException;
import org.ow2.bonita.facade.exception.UserNotFoundException;
import org.ow2.bonita.facade.identity.Group;
import org.ow2.bonita.facade.identity.Membership;
import org.ow2.bonita.facade.identity.ProfileMetadata;
import org.ow2.bonita.facade.identity.Role;
import org.ow2.bonita.facade.identity.User;
import org.ow2.bonita.facade.identity.impl.ContactInfoImpl;
import org.ow2.bonita.facade.identity.impl.GroupImpl;
import org.ow2.bonita.facade.identity.impl.MembershipImpl;
import org.ow2.bonita.facade.identity.impl.ProfileMetadataImpl;
import org.ow2.bonita.facade.identity.impl.RoleImpl;
import org.ow2.bonita.facade.identity.impl.UserImpl;
import org.ow2.bonita.facade.paging.GroupCriterion;
import org.ow2.bonita.facade.paging.RoleCriterion;
import org.ow2.bonita.facade.paging.UserCriterion;
import org.ow2.bonita.facade.privilege.Rule;
import org.ow2.bonita.facade.privilege.impl.RuleImpl;
import org.ow2.bonita.services.IdentityService;
import org.ow2.bonita.services.PrivilegeService;
import org.ow2.bonita.util.EnvTool;
import org.ow2.bonita.util.Misc;

/**
 * @author Anthony Birembaut, Matthieu Chaffotte
 * 
 */
public class IdentityAPIImpl implements IdentityAPI {

  protected IdentityAPIImpl(final String queryList) {
  }

  @Override
  public Role addRole(final String name) throws RoleAlreadyExistsException {
    FacadeUtil.checkArgsNotNull(name);
    final IdentityService identityService = EnvTool.getIdentityService();
    RoleImpl role = identityService.findRoleByName(name);
    if (role != null) {
      throw new RoleAlreadyExistsException("bai_IAPII_1", name);
    }
    role = new RoleImpl(name);
    identityService.addRole(role);
    return role;
  }

  @Override
  public Role addRole(final String name, final String label, final String description)
      throws RoleAlreadyExistsException {
    FacadeUtil.checkArgsNotNull(name);
    final IdentityService identityService = EnvTool.getIdentityService();
    RoleImpl role = identityService.findRoleByName(name);
    if (role != null) {
      throw new RoleAlreadyExistsException("bai_IAPII_1", name);
    }
    role = new RoleImpl(name);
    role.setLabel(label);
    role.setDescription(description);
    identityService.addRole(role);
    return role;
  }

  @Override
  public Role importRole(final String uuid, final String name, final String label, final String description)
      throws RoleAlreadyExistsException {
    FacadeUtil.checkArgsNotNull(name);
    final IdentityService identityService = EnvTool.getIdentityService();
    Role role = identityService.getRole(uuid);
    if (role != null) {
      throw new RoleAlreadyExistsException("bai_IAPII_1", uuid);
    }
    role = identityService.findRoleByName(name);
    if (role != null) {
      throw new RoleAlreadyExistsException("bai_IAPII_1", name);
    }
    final RoleImpl roleToImport = new RoleImpl(uuid, name);
    roleToImport.setLabel(label);
    roleToImport.setDescription(description);
    identityService.addRole(roleToImport);
    return role;
  }

  @Override
  @Deprecated
  public void addRoleToUser(final String roleName, final String username) throws UserNotFoundException,
      RoleNotFoundException {
    FacadeUtil.checkArgsNotNull(roleName, username);
    final IdentityService identityService = EnvTool.getIdentityService();
    final UserImpl user = identityService.findUserByUsername(username);
    if (user == null) {
      throw new UserNotFoundException("bai_IAPII_2", username);
    }
    final Set<GroupImpl> groups = identityService.findGroupsByName(IdentityAPI.DEFAULT_GROUP_NAME);
    GroupImpl defaultGroup = null;
    if (groups != null && !groups.isEmpty()) {
      defaultGroup = groups.iterator().next();
    }
    final RoleImpl role = identityService.findRoleByName(roleName);
    if (role == null || defaultGroup == null) {
      throw new RoleNotFoundException("bai_IAPII_3", roleName + IdentityAPI.MEMBERSHIP_SEPARATOR
          + IdentityAPI.GROUP_PATH_SEPARATOR + IdentityAPI.DEFAULT_GROUP_NAME);
    }
    MembershipImpl membership = identityService.findMembershipByRoleAndGroup(role.getUUID(), defaultGroup.getUUID());
    if (membership == null) {
      membership = new MembershipImpl();
      membership.setGroup(defaultGroup);
      membership.setRole(role);
      identityService.addMembership(membership);
    }
    identityService.addMembershipToUser(user, membership);
  }

  @Override
  @Deprecated
  public void setUserRoles(final String username, final Set<String> roleNames) throws UserNotFoundException,
      RoleNotFoundException {
    FacadeUtil.checkArgsNotNull(username);
    final IdentityService identityService = EnvTool.getIdentityService();
    final UserImpl user = identityService.findUserByUsername(username);
    if (user == null) {
      throw new UserNotFoundException("bai_IAPII_2", username);
    }
    final Set<GroupImpl> groups = identityService.findGroupsByName(IdentityAPI.DEFAULT_GROUP_NAME);
    GroupImpl defaultGroup = null;
    if (groups != null && !groups.isEmpty()) {
      defaultGroup = groups.iterator().next();
    }
    final Set<MembershipImpl> memberships = new HashSet<MembershipImpl>();
    if (roleNames != null) {
      for (final String roleName : roleNames) {
        final RoleImpl role = identityService.findRoleByName(roleName);
        if (role == null || defaultGroup == null) {
          throw new RoleNotFoundException("bai_IAPII_3", roleName + IdentityAPI.MEMBERSHIP_SEPARATOR
              + IdentityAPI.GROUP_PATH_SEPARATOR + IdentityAPI.DEFAULT_GROUP_NAME);
        }
        MembershipImpl membership = identityService
            .findMembershipByRoleAndGroup(role.getUUID(), defaultGroup.getUUID());
        if (membership == null) {
          membership = new MembershipImpl();
          membership.setGroup(defaultGroup);
          membership.setRole(role);
          identityService.addMembership(membership);
        }
        memberships.add(membership);
      }
    }

    identityService.setUserMemberships(user, memberships);
  }

  @Override
  public User addUser(final String username, final String password) throws UserAlreadyExistsException {
    FacadeUtil.checkArgsNotNull(username, password);
    if ("".equals(username.trim())) {
      throw new BonitaWrapperException(new IllegalArgumentException("The user name cannot be empty"));
    }
    final IdentityService identityService = EnvTool.getIdentityService();
    UserImpl user = identityService.findUserByUsername(username);
    if (user != null) {
      throw new UserAlreadyExistsException("bai_IAPII_4", username);
    }
    user = new UserImpl(username, password);
    identityService.addUser(user);
    return user;
  }

  @Override
  @Deprecated
  public User addUser(final String username, final String password, final String firstName, final String lastName,
      final String email) throws UserAlreadyExistsException {
    FacadeUtil.checkArgsNotNull(username, password);
    final IdentityService identityService = EnvTool.getIdentityService();
    UserImpl user = identityService.findUserByUsername(username);
    if (user != null) {
      throw new UserAlreadyExistsException("bai_IAPII_4", username);
    }
    user = new UserImpl(username, password);
    user.setEmail(email);
    user.setFirstName(firstName);
    user.setLastName(lastName);
    identityService.addUser(user);
    return user;
  }

  @Override
  @Deprecated
  public Set<Role> getRoles() {
    final IdentityService identityService = EnvTool.getIdentityService();
    final List<RoleImpl> roles = identityService.getAllRoles();
    final Set<Role> result = new HashSet<Role>();
    if (roles != null) {
      for (final RoleImpl role : roles) {
        result.add(new RoleImpl(role));
      }
    }
    return result;
  }

  @Override
  @Deprecated
  public Set<User> getUsers() {
    final IdentityService identityService = EnvTool.getIdentityService();
    final List<UserImpl> users = identityService.getAllUsers();
    final Set<User> result = new HashSet<User>();
    if (users != null) {
      for (final UserImpl user : users) {
        result.add(new UserImpl(user));
      }
    }
    return result;
  }

  @Override
  @Deprecated
  public Role getRole(final String name) throws RoleNotFoundException {
    FacadeUtil.checkArgsNotNull(name);
    final IdentityService identityService = EnvTool.getIdentityService();
    final RoleImpl role = identityService.findRoleByName(name);
    if (role == null) {
      throw new RoleNotFoundException("bai_IAPII_3", name);
    }
    return new RoleImpl(role);
  }

  @Override
  @Deprecated
  public User getUser(final String username) throws UserNotFoundException {
    FacadeUtil.checkArgsNotNull(username);
    final IdentityService identityService = EnvTool.getIdentityService();
    final UserImpl user = identityService.findUserByUsername(username);
    if (user == null) {
      throw new UserNotFoundException("bai_IAPII_2", username);
    }
    return new UserImpl(user);
  }

  @Override
  @Deprecated
  public void removeRole(final String name) throws RoleNotFoundException {
    FacadeUtil.checkArgsNotNull(name);
    final IdentityService identityService = EnvTool.getIdentityService();
    final RoleImpl role = identityService.findRoleByName(name);
    if (role == null) {
      throw new RoleNotFoundException("bai_IAPII_3", name);
    }
    final Set<MembershipImpl> memberships = identityService.getMembershipsByRole(role.getUUID());
    if (memberships != null) {
      final Set<String> membershipUUIDs = new HashSet<String>();
      for (final MembershipImpl membership : memberships) {
        final List<UserImpl> usersInMembership = identityService.getUsersByMembership(membership.getUUID());
        for (final UserImpl user : usersInMembership) {
          identityService.removeMembershipFromUser(user, membership);
        }
        identityService.deleteMembership(membership);
        membershipUUIDs.add(membership.getUUID());
      }
      removeMembershipsFromRules(membershipUUIDs);
    }
    identityService.deleteRole(role);

    final Set<String> roleUUIDs = new HashSet<String>();
    roleUUIDs.add(role.getUUID());
    removeRolesFromRules(roleUUIDs);
  }

  @Override
  @Deprecated
  public void removeUser(final String username) throws UserNotFoundException {
    FacadeUtil.checkArgsNotNull(username);
    final IdentityService identityService = EnvTool.getIdentityService();
    final UserImpl user = identityService.findUserByUsername(username);
    if (user == null) {
      throw new UserNotFoundException("bai_IAPII_2", username);
    }
    final List<UserImpl> usersOfManager = identityService.getUsersByManager(user.getUUID());
    for (final UserImpl userOfManager : usersOfManager) {
      userOfManager.setManagerUUID(null);
    }
    final List<UserImpl> delegeesUsers = identityService.getUsersByDelegee(user.getUUID());
    for (final UserImpl delegeesUser : delegeesUsers) {
      delegeesUser.setDelegeeUUID(null);
    }
    identityService.deleteUser(user);

    final Set<String> userUUIDs = new HashSet<String>();
    userUUIDs.add(user.getUUID());
    removeUsersFromRules(userUUIDs);
  }

  @Override
  @Deprecated
  public void removeRoleFromUser(final String roleName, final String username) throws UserNotFoundException,
      RoleNotFoundException {
    FacadeUtil.checkArgsNotNull(roleName, username);
    final IdentityService identityService = EnvTool.getIdentityService();
    final UserImpl user = identityService.findUserByUsername(username);
    if (user == null) {
      throw new UserNotFoundException("bai_IAPII_2", username);
    }
    final Set<GroupImpl> groups = identityService.findGroupsByName(IdentityAPI.DEFAULT_GROUP_NAME);
    GroupImpl defaultGroup = null;
    if (groups != null && !groups.isEmpty()) {
      defaultGroup = groups.iterator().next();
    }
    final RoleImpl role = identityService.findRoleByName(roleName);
    if (role == null || defaultGroup == null) {
      throw new RoleNotFoundException("bai_IAPII_3", roleName + IdentityAPI.MEMBERSHIP_SEPARATOR
          + IdentityAPI.GROUP_PATH_SEPARATOR + IdentityAPI.DEFAULT_GROUP_NAME);
    }
    final MembershipImpl membership = identityService.findMembershipByRoleAndGroup(role.getUUID(),
        defaultGroup.getUUID());
    if (membership != null) {
      identityService.removeMembershipFromUser(user, membership);
    }
  }

  @Override
  @Deprecated
  public Set<Role> getUserRoles(final String username) throws UserNotFoundException {
    FacadeUtil.checkArgsNotNull(username);
    final IdentityService identityService = EnvTool.getIdentityService();
    final UserImpl user = identityService.findUserByUsername(username);
    if (user == null) {
      throw new UserNotFoundException("bai_IAPII_2", username);
    }
    final Set<Role> roles = new HashSet<Role>();
    final Set<Membership> memberships = user.getMemberships();
    for (final Membership membership : memberships) {
      if (IdentityAPI.DEFAULT_GROUP_NAME.equals(membership.getGroup().getName())) {
        roles.add(new RoleImpl((RoleImpl) membership.getRole()));
      }
    }
    return roles;
  }

  @Override
  @Deprecated
  public Set<User> getUsersInRole(final String name) throws RoleNotFoundException {
    FacadeUtil.checkArgsNotNull(name);
    final IdentityService identityService = EnvTool.getIdentityService();
    final Set<GroupImpl> groups = identityService.findGroupsByName(IdentityAPI.DEFAULT_GROUP_NAME);
    GroupImpl defaultGroup = null;
    if (groups != null && !groups.isEmpty()) {
      defaultGroup = groups.iterator().next();
    }
    final RoleImpl role = identityService.findRoleByName(name);
    if (role == null || defaultGroup == null) {
      throw new RoleNotFoundException("bai_IAPII_3", name + IdentityAPI.MEMBERSHIP_SEPARATOR
          + IdentityAPI.GROUP_PATH_SEPARATOR + IdentityAPI.DEFAULT_GROUP_NAME);
    }
    final MembershipImpl membership = identityService.findMembershipByRoleAndGroup(role.getUUID(),
        defaultGroup.getUUID());
    final Set<User> users = new HashSet<User>();
    if (membership != null) {
      final List<UserImpl> usersInRole = identityService.getUsersByMembership(membership.getUUID());
      if (usersInRole != null) {
        for (final UserImpl user : usersInRole) {
          users.add(new UserImpl(user));
        }
      }
    }
    return users;
  }

  @Override
  @Deprecated
  public Role updateRole(final String oldName, final String name, final String label, final String description)
      throws RoleNotFoundException, RoleAlreadyExistsException {
    FacadeUtil.checkArgsNotNull(oldName, name);
    final IdentityService identityService = EnvTool.getIdentityService();
    final RoleImpl role = identityService.findRoleByName(oldName);
    if (role == null) {
      throw new RoleNotFoundException("bai_IAPII_3", oldName);
    }
    if (!name.equals(oldName) && identityService.findRoleByName(name) != null) {
      throw new RoleAlreadyExistsException("bai_IAPII_5", name);
    }
    role.setName(name);
    role.setLabel(label);
    role.setDescription(description);
    identityService.updateRole(role);
    return new RoleImpl(role);
  }

  @Override
  @Deprecated
  public User updateUser(final String oldUsername, final String username, final String password,
      final String firstName, final String lastName, final String email) throws UserNotFoundException,
      UserAlreadyExistsException {
    FacadeUtil.checkArgsNotNull(oldUsername, username);
    final IdentityService identityService = EnvTool.getIdentityService();
    final UserImpl user = identityService.findUserByUsername(oldUsername);
    if (user == null) {
      throw new UserNotFoundException("bai_IAPII_2", oldUsername);
    }
    if (!username.equals(oldUsername) && identityService.findUserByUsername(username) != null) {
      throw new UserAlreadyExistsException("bai_IAPII_6", username);
    }
    user.setUsername(username);
    user.setFirstName(firstName);
    user.setLastName(lastName);
    user.setEmail(email);
    identityService.updateUser(user);
    identityService.updateUserPassword(user, password);
    return new UserImpl(user);
  }

  @Override
  public User updateUserPassword(final String userUUID, final String password) throws UserNotFoundException {
    FacadeUtil.checkArgsNotNull(userUUID);
    final IdentityService identityService = EnvTool.getIdentityService();
    final UserImpl user = identityService.getUser(userUUID);
    if (user == null) {
      throw new UserNotFoundException("bai_IAPII_2", userUUID);
    }
    identityService.updateUserPassword(user, password);
    return new UserImpl(user);
  }

  @Override
  public Group addGroup(final String name, final String parentGroupUUID) throws GroupAlreadyExistsException,
      GroupNotFoundException {
    FacadeUtil.checkArgsNotNull(name);
    final IdentityService identityService = EnvTool.getIdentityService();
    final List<GroupImpl> childrenGroups = identityService.getGroupChildren(parentGroupUUID);
    for (final GroupImpl childGroup : childrenGroups) {
      if (name.equalsIgnoreCase(childGroup.getName())) {
        throw new GroupAlreadyExistsException("bai_IAPII_7", name);
      }
    }
    final GroupImpl group = new GroupImpl(name);
    if (parentGroupUUID != null) {
      final GroupImpl parentGroup = identityService.getGroup(parentGroupUUID);
      if (parentGroup == null) {
        throw new GroupNotFoundException("bai_IAPII_13", parentGroupUUID);
      }
      group.setParentGroup(parentGroup);
    }
    identityService.addGroup(group);
    return new GroupImpl(group);
  }

  @Override
  public Group addGroup(final String name, final String label, final String description, final String parentGroupUUID)
      throws GroupAlreadyExistsException, GroupNotFoundException {
    FacadeUtil.checkArgsNotNull(name);
    final IdentityService identityService = EnvTool.getIdentityService();
    final List<GroupImpl> childrenGroups = identityService.getGroupChildren(parentGroupUUID);
    for (final GroupImpl childGroup : childrenGroups) {
      if (name.equalsIgnoreCase(childGroup.getName())) {
        throw new GroupAlreadyExistsException("bai_IAPII_7", name);
      }
    }
    final GroupImpl group = new GroupImpl(name);
    group.setLabel(label);
    group.setDescription(description);
    identityService.addGroup(group);
    if (parentGroupUUID != null) {
      final GroupImpl parentGroup = identityService.getGroup(parentGroupUUID);
      if (parentGroup == null) {
        throw new GroupNotFoundException("bai_IAPII_13", parentGroupUUID);
      }
      group.setParentGroup(parentGroup);
    }
    return new GroupImpl(group);
  }

  @Override
  public Group importGroup(final String uuid, final String name, final String label, final String description,
      final String parentGroupUUID) throws GroupAlreadyExistsException, GroupNotFoundException {
    FacadeUtil.checkArgsNotNull(name);
    final IdentityService identityService = EnvTool.getIdentityService();
    final Group group = identityService.getGroup(uuid);
    if (group != null) {
      throw new GroupAlreadyExistsException("bai_IAPII_7", uuid);
    }
    final List<GroupImpl> childrenGroups = identityService.getGroupChildren(parentGroupUUID);
    for (final GroupImpl childGroup : childrenGroups) {
      if (name.equalsIgnoreCase(childGroup.getName())) {
        throw new GroupAlreadyExistsException("bai_IAPII_7", name);
      }
    }
    final GroupImpl groupToImport = new GroupImpl(uuid, name);
    groupToImport.setLabel(label);
    groupToImport.setDescription(description);
    identityService.addGroup(groupToImport);
    if (parentGroupUUID != null) {
      final GroupImpl parentGroup = identityService.getGroup(parentGroupUUID);
      if (parentGroup == null) {
        throw new GroupNotFoundException("bai_IAPII_13", parentGroupUUID);
      }
      groupToImport.setParentGroup(parentGroup);
    }
    return new GroupImpl(groupToImport);
  }

  @Override
  public void addMembershipToUser(final String userUUID, final String membershipUUID) throws UserNotFoundException,
      MembershipNotFoundException {
    FacadeUtil.checkArgsNotNull(userUUID, membershipUUID);
    final IdentityService identityService = EnvTool.getIdentityService();
    final UserImpl user = identityService.getUser(userUUID);
    if (user == null) {
      throw new UserNotFoundException("bai_IAPII_2", userUUID);
    }
    final MembershipImpl membership = identityService.getMembership(membershipUUID);
    if (membership == null) {
      throw new MembershipNotFoundException("bai_IAPII_8", membershipUUID);
    }
    identityService.addMembershipToUser(user, membership);
  }

  @Override
  public void addMembershipsToUser(final String userUUID, final Collection<String> membershipUUIDs)
      throws UserNotFoundException, MembershipNotFoundException {
    FacadeUtil.checkArgsNotNull(userUUID, membershipUUIDs);
    final IdentityService identityService = EnvTool.getIdentityService();
    final UserImpl user = identityService.getUser(userUUID);
    if (user == null) {
      throw new UserNotFoundException("bai_IAPII_2", userUUID);
    }
    for (final String membershipUUID : membershipUUIDs) {
      final MembershipImpl membership = identityService.getMembership(membershipUUID);
      if (membership == null) {
        throw new MembershipNotFoundException("bai_IAPII_8", membershipUUID);
      }
      identityService.addMembershipToUser(user, membership);
    }
  }

  @Override
  public ProfileMetadata addProfileMetadata(final String name) throws MetadataAlreadyExistsException {
    FacadeUtil.checkArgsNotNull(name);
    final IdentityService identityService = EnvTool.getIdentityService();
    final ProfileMetadataImpl metadata = identityService.findProfileMetadataByName(name);
    if (metadata != null) {
      throw new MetadataAlreadyExistsException("bai_IAPII_9", name);
    }
    final ProfileMetadataImpl profileMetadata = new ProfileMetadataImpl(name);
    identityService.addProfileMetadata(profileMetadata);
    return profileMetadata;
  }

  @Override
  public ProfileMetadata addProfileMetadata(final String name, final String label)
      throws MetadataAlreadyExistsException {
    FacadeUtil.checkArgsNotNull(name);
    final IdentityService identityService = EnvTool.getIdentityService();
    final ProfileMetadataImpl metadata = identityService.findProfileMetadataByName(name);
    if (metadata != null) {
      throw new MetadataAlreadyExistsException("bai_IAPII_9", name);
    }
    final ProfileMetadataImpl profileMetadata = new ProfileMetadataImpl(name);
    profileMetadata.setLabel(label);
    identityService.addProfileMetadata(profileMetadata);
    return profileMetadata;
  }

  @Override
  public User addUser(final String username, final String password, final String firstName, final String lastName,
      final String title, final String jobTitle, final String managerUserUUID, final Map<String, String> profileMetadata)
      throws UserAlreadyExistsException, UserNotFoundException, MetadataNotFoundException {
    FacadeUtil.checkArgsNotNull(username, password);
    if ("".equals(username.trim())) {
      throw new BonitaWrapperException(new IllegalArgumentException("The user name cannot be empty"));
    }
    final IdentityService identityService = EnvTool.getIdentityService();
    UserImpl user = identityService.findUserByUsername(username);
    if (user != null) {
      throw new UserAlreadyExistsException("bai_IAPII_4", username);
    }
    user = new UserImpl(username, password);
    user.setFirstName(firstName);
    user.setLastName(lastName);
    user.setJobTitle(jobTitle);
    user.setTitle(title);
    if (managerUserUUID != null && !managerUserUUID.isEmpty()) {
      final UserImpl manager = identityService.getUser(managerUserUUID);
      if (manager == null) {
        throw new UserNotFoundException("bai_IAPII_10", managerUserUUID);
      }
      user.setManagerUUID(managerUserUUID);
    }
    identityService.addUser(user);

    if (profileMetadata != null) {
      final Map<ProfileMetadata, String> userMetadata = new HashMap<ProfileMetadata, String>();
      for (final Entry<String, String> profileMetadataEntry : profileMetadata.entrySet()) {
        final ProfileMetadataImpl metadata = identityService.findProfileMetadataByName(profileMetadataEntry.getKey());
        if (metadata == null) {
          throw new MetadataNotFoundException("bai_IAPII_11", profileMetadataEntry.getKey());
        }
        metadata.getUsers().put(user, profileMetadataEntry.getValue());
        identityService.updateProfileMetadata(metadata);
        userMetadata.put(metadata, profileMetadataEntry.getValue());
      }
      user.setMetadata(userMetadata);
      identityService.updateUser(user);
    }
    return new UserImpl(user);
  }

  @Override
  public ProfileMetadata findProfileMetadataByName(final String metadataName) throws MetadataNotFoundException {
    FacadeUtil.checkArgsNotNull(metadataName);
    final IdentityService identityService = EnvTool.getIdentityService();
    final ProfileMetadataImpl metadata = identityService.findProfileMetadataByName(metadataName);
    if (metadata == null) {
      throw new MetadataNotFoundException("bai_IAPII_11", metadataName);
    }
    return new ProfileMetadataImpl(metadata);
  }

  @Override
  public Role findRoleByName(final String name) throws RoleNotFoundException {
    FacadeUtil.checkArgsNotNull(name);
    final IdentityService identityService = EnvTool.getIdentityService();
    final RoleImpl role = identityService.findRoleByName(name);
    if (role == null) {
      throw new RoleNotFoundException("bai_IAPII_3", name);
    }
    return new RoleImpl(role);
  }

  @Override
  public User findUserByUserName(final String username) throws UserNotFoundException {
    FacadeUtil.checkArgsNotNull(username);
    final IdentityService identityService = EnvTool.getIdentityService();
    final UserImpl user = identityService.findUserByUsername(username);
    if (user == null) {
      throw new UserNotFoundException("bai_IAPII_2", username);
    }
    return new UserImpl(user);
  }

  @Override
  public List<Group> getAllGroups() {
    final IdentityService identityService = EnvTool.getIdentityService();
    final List<GroupImpl> groups = identityService.getAllGroups();
    final List<Group> result = new ArrayList<Group>();
    if (groups != null) {
      for (final GroupImpl group : groups) {
        result.add(new GroupImpl(group));
      }
    }
    return result;
  }

  @Override
  public List<ProfileMetadata> getAllProfileMetadata() {
    final IdentityService identityService = EnvTool.getIdentityService();
    final List<ProfileMetadataImpl> profileMetadata = identityService.getAllProfileMetadata();
    final List<ProfileMetadata> result = new ArrayList<ProfileMetadata>();
    if (profileMetadata != null) {
      for (final ProfileMetadataImpl metadata : profileMetadata) {
        result.add(new ProfileMetadataImpl(metadata));
      }
    }
    return result;
  }

  @Override
  public List<ProfileMetadata> getProfileMetadata(final int fromIndex, final int numberOfMetadata) {
    final IdentityService identityService = EnvTool.getIdentityService();
    final List<ProfileMetadataImpl> profileMetadata = identityService.getProfileMetadata(fromIndex, numberOfMetadata);
    final List<ProfileMetadata> result = new ArrayList<ProfileMetadata>();
    if (profileMetadata != null) {
      for (final ProfileMetadataImpl metadata : profileMetadata) {
        result.add(new ProfileMetadataImpl(metadata));
      }
    }
    return result;
  }

  @Override
  public List<Role> getAllRoles() {
    final IdentityService identityService = EnvTool.getIdentityService();
    final List<RoleImpl> roles = identityService.getAllRoles();
    final List<Role> result = new ArrayList<Role>();
    if (roles != null) {
      for (final RoleImpl role : roles) {
        result.add(new RoleImpl(role));
      }
    }
    return result;
  }

  @Override
  public List<User> getAllUsers() {
    final IdentityService identityService = EnvTool.getIdentityService();
    final List<UserImpl> users = identityService.getAllUsers();
    final List<User> result = new ArrayList<User>();
    if (users != null) {
      for (final UserImpl user : users) {
        result.add(new UserImpl(user));
      }
    }
    return result;
  }

  @Override
  public List<User> getAllUsersInGroup(final String groupUUID) throws GroupNotFoundException {
    FacadeUtil.checkArgsNotNull(groupUUID);
    final IdentityService identityService = EnvTool.getIdentityService();
    final GroupImpl group = identityService.getGroup(groupUUID);
    if (group == null) {
      throw new GroupNotFoundException("bai_IAPII_12", groupUUID);
    }
    final List<UserImpl> users = identityService.getUsersByGroup(groupUUID);
    final List<User> result = new ArrayList<User>();
    if (users != null) {
      for (final UserImpl user : users) {
        result.add(new UserImpl(user));
      }
    }
    return result;
  }

  @Override
  public List<User> getAllUsersInMembership(final String membershipUUID) throws MembershipNotFoundException {
    FacadeUtil.checkArgsNotNull(membershipUUID);
    final IdentityService identityService = EnvTool.getIdentityService();
    final MembershipImpl membership = identityService.getMembership(membershipUUID);
    if (membership == null) {
      throw new MembershipNotFoundException("bai_IAPII_8", membershipUUID);
    }
    final List<UserImpl> users = identityService.getUsersByMembership(membershipUUID);
    final List<User> result = new ArrayList<User>();
    if (users != null) {
      for (final UserImpl user : users) {
        result.add(new UserImpl(user));
      }
    }
    return result;
  }

  @Override
  public List<User> getAllUsersInRole(final String roleUUID) throws RoleNotFoundException {
    FacadeUtil.checkArgsNotNull(roleUUID);
    final IdentityService identityService = EnvTool.getIdentityService();
    final RoleImpl role = identityService.getRole(roleUUID);
    if (role == null) {
      throw new RoleNotFoundException("bai_IAPII_3", roleUUID);
    }
    final List<UserImpl> users = identityService.getUsersByRole(roleUUID);
    final List<User> result = new ArrayList<User>();
    if (users != null) {
      for (final UserImpl user : users) {
        result.add(new UserImpl(user));
      }
    }
    return result;
  }

  @Override
  public List<User> getAllUsersInRoleAndGroup(final String roleUUID, final String groupUUID)
      throws RoleNotFoundException, GroupNotFoundException {
    FacadeUtil.checkArgsNotNull(roleUUID, groupUUID);
    final IdentityService identityService = EnvTool.getIdentityService();
    final RoleImpl role = identityService.getRole(roleUUID);
    if (role == null) {
      throw new RoleNotFoundException("bai_IAPII_3", roleUUID);
    }
    final GroupImpl group = identityService.getGroup(groupUUID);
    if (group == null) {
      throw new GroupNotFoundException("bai_IAPII_12", groupUUID);
    }
    final MembershipImpl membership = identityService.findMembershipByRoleAndGroup(roleUUID, groupUUID);
    final List<User> result = new ArrayList<User>();
    if (membership != null) {
      final List<UserImpl> users = identityService.getUsersByMembership(membership.getUUID());
      for (final UserImpl user : users) {
        result.add(new UserImpl(user));
      }
    }
    return result;
  }

  @Override
  public List<User> getUsersByManagerUUID(final String managerUUID) throws UserNotFoundException {
    FacadeUtil.checkArgsNotNull(managerUUID);
    final IdentityService identityService = EnvTool.getIdentityService();
    final UserImpl manager = identityService.getUser(managerUUID);
    if (manager == null) {
      throw new UserNotFoundException("bai_IAPII_2", managerUUID);
    }
    final List<UserImpl> users = identityService.getUsersByManager(managerUUID);
    final List<User> result = new ArrayList<User>();
    if (users != null) {
      for (final UserImpl user : users) {
        result.add(new UserImpl(user));
      }
    }
    return result;
  }

  @Override
  public List<Group> getChildrenGroupsByUUID(final String groupUUID) {
    final IdentityService identityService = EnvTool.getIdentityService();
    final List<GroupImpl> groups = identityService.getGroupChildren(groupUUID);
    final List<Group> result = new ArrayList<Group>();
    if (groups != null) {
      for (final GroupImpl group : groups) {
        result.add(new GroupImpl(group));
      }
    }
    return result;
  }

  @Override
  public List<Group> getChildrenGroups(final String groupUUID, final int fromIndex, final int numberOfGroups)
      throws GroupNotFoundException {
    final IdentityService identityService = EnvTool.getIdentityService();
    final List<GroupImpl> groups = identityService.getGroupChildren(groupUUID, fromIndex, numberOfGroups);
    final List<Group> result = new ArrayList<Group>();
    if (groups != null) {
      for (final GroupImpl group : groups) {
        result.add(new GroupImpl(group));
      }
    }
    return result;
  }

  @Override
  public List<Group> getChildrenGroups(final String groupUUID, final int fromIndex, final int numberOfGroups,
      final GroupCriterion pagingCriterion) throws GroupNotFoundException {
    final IdentityService identityService = EnvTool.getIdentityService();
    final List<GroupImpl> groups = identityService.getGroupChildren(groupUUID, fromIndex, numberOfGroups,
        pagingCriterion);
    final List<Group> result = new ArrayList<Group>();
    if (groups != null) {
      for (final GroupImpl group : groups) {
        result.add(new GroupImpl(group));
      }
    }
    return result;
  }

  @Override
  public int getNumberOfChildrenGroups(final String groupUUID) throws GroupNotFoundException {
    final IdentityService identityService = EnvTool.getIdentityService();
    return identityService.getNumberOfGroupChildren(groupUUID);
  }

  @Override
  public Group getGroupByUUID(final String groupUUID) throws GroupNotFoundException {
    FacadeUtil.checkArgsNotNull(groupUUID);
    final IdentityService identityService = EnvTool.getIdentityService();
    final GroupImpl group = identityService.getGroup(groupUUID);
    if (group == null) {
      throw new GroupNotFoundException("bai_IAPII_12", groupUUID);
    }
    return new GroupImpl(group);
  }

  @Override
  public List<Group> getGroupsByUUIDs(final Collection<String> groupUUIDs) throws GroupNotFoundException {
    FacadeUtil.checkArgsNotNull(groupUUIDs);
    final List<Group> result = new ArrayList<Group>();
    final IdentityService identityService = EnvTool.getIdentityService();
    if (groupUUIDs.size() > 0) {
      final List<GroupImpl> groups = identityService.getGroups(groupUUIDs);
      if (groupUUIDs.size() != groups.size()) {
        // The request tries to get an unknown group.
        final Set<String> storedGroupUUIDs = new HashSet<String>();
        for (final Group group : groups) {
          storedGroupUUIDs.add(group.getUUID());
        }
        for (final String groupUUID : groupUUIDs) {
          if (!storedGroupUUIDs.contains(groupUUID)) {
            throw new GroupNotFoundException("bai_IAPII_12", groupUUID);
          }
        }
      }
      for (final GroupImpl group : groups) {
        result.add(new GroupImpl(group));
      }
    }
    return result;
  }

  @Override
  public List<Group> getGroups(final int fromIndex, final int numberOfGroups) {
    final IdentityService identityService = EnvTool.getIdentityService();
    final List<GroupImpl> groups = identityService.getGroups(fromIndex, numberOfGroups);
    final List<Group> result = new ArrayList<Group>();
    if (groups != null) {
      for (final GroupImpl group : groups) {
        result.add(new GroupImpl(group));
      }
    }
    return result;
  }

  @Override
  public List<Group> getGroups(final int fromIndex, final int numberOfGroups, final GroupCriterion pagingCriterion) {
    final IdentityService identityService = EnvTool.getIdentityService();
    final List<GroupImpl> groups = identityService.getGroups(fromIndex, numberOfGroups, pagingCriterion);
    final List<Group> result = new ArrayList<Group>();
    if (groups != null) {
      for (final GroupImpl group : groups) {
        result.add(new GroupImpl(group));
      }
    }
    return result;
  }

  @Override
  public Membership getMembershipByUUID(final String membershipUUID) throws MembershipNotFoundException {
    FacadeUtil.checkArgsNotNull(membershipUUID);
    final IdentityService identityService = EnvTool.getIdentityService();
    final MembershipImpl membership = identityService.getMembership(membershipUUID);
    if (membership == null) {
      throw new MembershipNotFoundException("bai_IAPII_8", membershipUUID);
    }
    return new MembershipImpl(membership);
  }

  @Override
  public List<Membership> getMembershipsByUUIDs(final Collection<String> membershipUUIDs)
      throws MembershipNotFoundException {
    FacadeUtil.checkArgsNotNull(membershipUUIDs);
    final List<Membership> result = new ArrayList<Membership>();
    final IdentityService identityService = EnvTool.getIdentityService();
    if (membershipUUIDs.size() > 0) {
      final List<MembershipImpl> memberships = identityService.getMemberships(membershipUUIDs);
      if (membershipUUIDs.size() != memberships.size()) {
        // The request tries to get an unknown membership.
        final Set<String> storedMembershipUUIDs = new HashSet<String>();
        for (final Membership membership : memberships) {
          storedMembershipUUIDs.add(membership.getUUID());
        }
        for (final String membershipUUID : membershipUUIDs) {
          if (!storedMembershipUUIDs.contains(membershipUUID)) {
            throw new MembershipNotFoundException("bai_IAPII_8", membershipUUID);
          }
        }
      }
      for (final MembershipImpl membership : memberships) {
        result.add(new MembershipImpl(membership));
      }
    }
    return result;
  }

  @Override
  public Membership getMembershipForRoleAndGroup(final String roleUUID, final String groupUUID)
      throws RoleNotFoundException, GroupNotFoundException {
    FacadeUtil.checkArgsNotNull(roleUUID, groupUUID);
    final IdentityService identityService = EnvTool.getIdentityService();
    final RoleImpl role = identityService.getRole(roleUUID);
    if (role == null) {
      throw new RoleNotFoundException("bai_IAPII_3", roleUUID);
    }
    final GroupImpl group = identityService.getGroup(groupUUID);
    if (group == null) {
      throw new GroupNotFoundException("bai_IAPII_12", groupUUID);
    }
    MembershipImpl membership = identityService.findMembershipByRoleAndGroup(roleUUID, groupUUID);
    if (membership != null) {
      return new MembershipImpl(membership);
    } else {
      membership = new MembershipImpl();
      membership.setGroup(group);
      membership.setRole(role);
      identityService.addMembership(membership);
      return new MembershipImpl(membership);
    }
  }

  @Override
  public int getNumberOfGroups() {
    final IdentityService identityService = EnvTool.getIdentityService();
    return identityService.getNumberOfGroups();
  }

  @Override
  public int getNumberOfRoles() {
    final IdentityService identityService = EnvTool.getIdentityService();
    return identityService.getNumberOfRoles();
  }

  @Override
  public int getNumberOfUsers() {
    final IdentityService identityService = EnvTool.getIdentityService();
    return identityService.getNumberOfUsers();
  }

  @Override
  public int getNumberOfUsersInGroup(final String groupUUID) {
    FacadeUtil.checkArgsNotNull(groupUUID);
    final IdentityService identityService = EnvTool.getIdentityService();
    return identityService.getNumberOfUsersByGroup(groupUUID);
  }

  @Override
  public int getNumberOfUsersInRole(final String roleUUID) {
    FacadeUtil.checkArgsNotNull(roleUUID);
    final IdentityService identityService = EnvTool.getIdentityService();
    return identityService.getNumberOfUsersByRole(roleUUID);
  }

  @Override
  public ProfileMetadata getProfileMetadataByUUID(final String metadataUUID) throws MetadataNotFoundException {
    FacadeUtil.checkArgsNotNull(metadataUUID);
    final IdentityService identityService = EnvTool.getIdentityService();
    final ProfileMetadataImpl profileMetadata = identityService.getProfileMetadata(metadataUUID);
    if (profileMetadata == null) {
      throw new MetadataNotFoundException("bai_IAPII_11", metadataUUID);
    }
    return new ProfileMetadataImpl(profileMetadata);
  }

  @Override
  public Role getRoleByUUID(final String roleUUID) throws RoleNotFoundException {
    FacadeUtil.checkArgsNotNull(roleUUID);
    final IdentityService identityService = EnvTool.getIdentityService();
    final RoleImpl role = identityService.getRole(roleUUID);
    if (role == null) {
      throw new RoleNotFoundException("bai_IAPII_3", roleUUID);
    }
    return new RoleImpl(role);
  }

  @Override
  public List<Role> getRolesByUUIDs(final Collection<String> roleUUIDs) throws RoleNotFoundException {
    FacadeUtil.checkArgsNotNull(roleUUIDs);
    final List<Role> result = new ArrayList<Role>();
    final IdentityService identityService = EnvTool.getIdentityService();
    if (roleUUIDs.size() > 0) {
      final List<RoleImpl> roles = identityService.getRoles(roleUUIDs);
      if (roleUUIDs.size() != roles.size()) {
        // The request tries to get an unknown role.
        final Set<String> storedGroupUUIDs = new HashSet<String>();
        for (final Role group : roles) {
          storedGroupUUIDs.add(group.getUUID());
        }
        for (final String roleUUID : roleUUIDs) {
          if (!storedGroupUUIDs.contains(roleUUID)) {
            throw new RoleNotFoundException("bai_IAPII_3", roleUUID);
          }
        }
      }
      for (final RoleImpl role : roles) {
        result.add(new RoleImpl(role));
      }
    }
    return result;
  }

  @Override
  public List<Role> getRoles(final int fromIndex, final int numberOfRoles) {
    final IdentityService identityService = EnvTool.getIdentityService();
    final List<RoleImpl> roles = identityService.getRoles(fromIndex, numberOfRoles);
    final List<Role> result = new ArrayList<Role>();
    if (roles != null) {
      for (final RoleImpl role : roles) {
        result.add(new RoleImpl(role));
      }
    }
    return result;
  }

  @Override
  public List<Role> getRoles(final int fromIndex, final int numberOfRoles, final RoleCriterion pagingCriterion) {
    final IdentityService identityService = EnvTool.getIdentityService();
    final List<RoleImpl> roles = identityService.getRoles(fromIndex, numberOfRoles, pagingCriterion);
    final List<Role> result = new ArrayList<Role>();
    if (roles != null) {
      for (final RoleImpl role : roles) {
        result.add(new RoleImpl(role));
      }
    }
    return result;
  }

  @Override
  public User getUserByUUID(final String userUUID) throws UserNotFoundException {
    FacadeUtil.checkArgsNotNull(userUUID);
    final IdentityService identityService = EnvTool.getIdentityService();
    final UserImpl user = identityService.getUser(userUUID);
    if (user == null) {
      throw new UserNotFoundException("bai_IAPII_2", userUUID);
    }
    return new UserImpl(user);
  }

  @Override
  public List<User> getUsersByUUIDs(final Collection<String> userUUIDs) throws UserNotFoundException {
    FacadeUtil.checkArgsNotNull(userUUIDs);
    final List<User> result = new ArrayList<User>();
    final IdentityService identityService = EnvTool.getIdentityService();
    if (userUUIDs.size() > 0) {
      final List<UserImpl> users = identityService.getUsers(userUUIDs);
      if (userUUIDs.size() != users.size()) {
        // The request tries to get an unknown user.
        final Set<String> storedUserUUIDs = new HashSet<String>();
        for (final User user : users) {
          storedUserUUIDs.add(user.getUUID());
        }
        for (final String userUUID : userUUIDs) {
          if (!storedUserUUIDs.contains(userUUID)) {
            throw new UserNotFoundException("bai_IAPII_2", userUUID);
          }
        }
      }
      for (final UserImpl user : users) {
        result.add(new UserImpl(user));
      }
    }
    return result;
  }

  @Override
  public List<User> getUsers(final int fromIndex, final int numberOfUsers) {
    final IdentityService identityService = EnvTool.getIdentityService();
    final List<UserImpl> usersInGroup = identityService.getUsers(fromIndex, numberOfUsers);
    final List<User> users = new ArrayList<User>();
    for (final UserImpl userInGroup : usersInGroup) {
      users.add(new UserImpl(userInGroup));
    }
    return users;
  }

  @Override
  public List<User> getUsers(final int fromIndex, final int numberOfUsers, final UserCriterion pagingCriterion) {
    final IdentityService identityService = EnvTool.getIdentityService();
    final List<UserImpl> usersInGroup = identityService.getUsers(fromIndex, numberOfUsers, pagingCriterion);
    final List<User> users = new ArrayList<User>();
    for (final UserImpl userInGroup : usersInGroup) {
      users.add(new UserImpl(userInGroup));
    }
    return users;
  }

  @Override
  public List<User> getUsersInGroup(final String groupUUID, final int fromIndex, final int numberOfUsers)
      throws GroupNotFoundException {
    FacadeUtil.checkArgsNotNull(groupUUID);
    final IdentityService identityService = EnvTool.getIdentityService();
    final GroupImpl group = identityService.getGroup(groupUUID);
    if (group == null) {
      throw new GroupNotFoundException("bai_IAPII_12", groupUUID);
    }
    final List<UserImpl> usersInGroup = identityService.getUsersByGroup(groupUUID, fromIndex, numberOfUsers);
    final List<User> users = new ArrayList<User>();
    for (final UserImpl userInGroup : usersInGroup) {
      users.add(new UserImpl(userInGroup));
    }
    return users;
  }

  @Override
  public List<User> getUsersInGroup(final String groupUUID, final int fromIndex, final int numberOfUsers,
      final UserCriterion pagingCriterion) throws GroupNotFoundException {
    FacadeUtil.checkArgsNotNull(groupUUID);
    final IdentityService identityService = EnvTool.getIdentityService();
    final GroupImpl group = identityService.getGroup(groupUUID);
    if (group == null) {
      throw new GroupNotFoundException("bai_IAPII_12", groupUUID);
    }
    final List<UserImpl> usersInGroup = identityService.getUsersByGroup(groupUUID, fromIndex, numberOfUsers,
        pagingCriterion);
    final List<User> users = new ArrayList<User>();
    for (final UserImpl userInGroup : usersInGroup) {
      users.add(new UserImpl(userInGroup));
    }
    return users;
  }

  @Override
  public List<User> getUsersInRole(final String roleUUID, final int fromIndex, final int numberOfUsers)
      throws RoleNotFoundException {
    FacadeUtil.checkArgsNotNull(roleUUID);
    final IdentityService identityService = EnvTool.getIdentityService();
    final RoleImpl role = identityService.getRole(roleUUID);
    if (role == null) {
      throw new RoleNotFoundException("bai_IAPII_3", roleUUID);
    }
    final List<UserImpl> usersInGroup = identityService.getUsersByRole(roleUUID, fromIndex, numberOfUsers);
    final List<User> users = new ArrayList<User>();
    for (final UserImpl userInGroup : usersInGroup) {
      users.add(new UserImpl(userInGroup));
    }
    return users;
  }

  @Override
  public List<User> getUsersInRole(final String roleUUID, final int fromIndex, final int numberOfUsers,
      final UserCriterion pagingCriterion) throws RoleNotFoundException {
    FacadeUtil.checkArgsNotNull(roleUUID);
    final IdentityService identityService = EnvTool.getIdentityService();
    final RoleImpl role = identityService.getRole(roleUUID);
    if (role == null) {
      throw new RoleNotFoundException("bai_IAPII_3", roleUUID);
    }
    final List<UserImpl> usersInGroup = identityService.getUsersByRole(roleUUID, fromIndex, numberOfUsers,
        pagingCriterion);
    final List<User> users = new ArrayList<User>();
    for (final UserImpl userInGroup : usersInGroup) {
      users.add(new UserImpl(userInGroup));
    }
    return users;
  }

  @Override
  public void removeGroupByUUID(final String groupUUID) throws GroupNotFoundException {
    FacadeUtil.checkArgsNotNull(groupUUID);
    final IdentityService identityService = EnvTool.getIdentityService();
    final GroupImpl group = identityService.getGroup(groupUUID);
    if (group == null) {
      throw new GroupNotFoundException("bai_IAPII_12", groupUUID);
    }
    final List<GroupImpl> children = identityService.getGroupChildren(groupUUID);
    final List<String> childrenUUIDs = new ArrayList<String>();
    for (final GroupImpl child : children) {
      childrenUUIDs.add(child.getUUID());
    }
    if (!children.isEmpty()) {
      removeGroups(childrenUUIDs);
    }
    final Set<MembershipImpl> memberships = identityService.getMembershipsByGroup(group.getUUID());
    if (memberships != null) {
      final Set<String> membershipUUIDs = new HashSet<String>();
      for (final MembershipImpl membership : memberships) {
        final List<UserImpl> usersInMembership = identityService.getUsersByMembership(membership.getUUID());
        for (final UserImpl user : usersInMembership) {
          identityService.removeMembershipFromUser(user, membership);
        }
        identityService.deleteMembership(membership);
        membershipUUIDs.add(membership.getUUID());
      }
      removeMembershipsFromRules(membershipUUIDs);
    }
    identityService.deleteGroup(group);

    final Set<String> groupUUIDs = new HashSet<String>();
    groupUUIDs.add(groupUUID);
    removeGroupsFromRules(groupUUIDs);
  }

  @Override
  public void removeMembershipFromUser(final String userUUID, final String membershipUUID)
      throws UserNotFoundException, MembershipNotFoundException {
    FacadeUtil.checkArgsNotNull(userUUID, membershipUUID);
    final IdentityService identityService = EnvTool.getIdentityService();
    final UserImpl user = identityService.getUser(userUUID);
    if (user == null) {
      throw new UserNotFoundException("bai_IAPII_2", userUUID);
    }
    final MembershipImpl membership = identityService.getMembership(membershipUUID);
    if (membership == null) {
      throw new MembershipNotFoundException("bai_IAPII_8", membershipUUID);
    }
    identityService.removeMembershipFromUser(user, membership);
  }

  @Override
  public void removeMembershipsFromUser(final String userUUID, final Collection<String> membershipUUIDs)
      throws UserNotFoundException, MembershipNotFoundException {
    FacadeUtil.checkArgsNotNull(userUUID, membershipUUIDs);
    final IdentityService identityService = EnvTool.getIdentityService();
    final UserImpl user = identityService.getUser(userUUID);
    if (user == null) {
      throw new UserNotFoundException("bai_IAPII_2", userUUID);
    }
    for (final String membershipUUID : membershipUUIDs) {
      final MembershipImpl membership = identityService.getMembership(membershipUUID);
      if (membership == null) {
        throw new MembershipNotFoundException("bai_IAPII_8", membershipUUID);
      }
      identityService.removeMembershipFromUser(user, membership);
    }
  }

  @Override
  public void removeProfileMetadataByUUID(final String profileMetadataUUID) throws MetadataNotFoundException {
    Misc.checkArgsNotNull(profileMetadataUUID);
    final IdentityService identityService = EnvTool.getIdentityService();
    final ProfileMetadataImpl profileMetadata = identityService.getProfileMetadata(profileMetadataUUID);
    if (profileMetadata == null) {
      throw new MetadataNotFoundException("bai_IAPII_11", profileMetadataUUID);
    }
    identityService.deleteProfileMetadata(profileMetadata);
  }

  @Override
  public void removeRoleByUUID(final String roleUUID) throws RoleNotFoundException {
    Misc.checkArgsNotNull(roleUUID);
    final IdentityService identityService = EnvTool.getIdentityService();
    final RoleImpl role = identityService.getRole(roleUUID);
    if (role == null) {
      throw new RoleNotFoundException("bai_IAPII_3", roleUUID);
    }
    identityService.deleteRole(role);

    final Set<String> roleUUIDs = new HashSet<String>();
    roleUUIDs.add(roleUUID);
    removeRolesFromRules(roleUUIDs);
  }

  @Override
  public void removeUserByUUID(final String userUUID) throws UserNotFoundException {
    Misc.checkArgsNotNull(userUUID);
    final IdentityService identityService = EnvTool.getIdentityService();
    final UserImpl user = identityService.getUser(userUUID);
    if (user == null) {
      throw new UserNotFoundException("bai_IAPII_2", userUUID);
    }
    final List<UserImpl> usersOfManager = identityService.getUsersByManager(userUUID);
    for (final UserImpl userOfManager : usersOfManager) {
      userOfManager.setManagerUUID(null);
    }
    final List<UserImpl> delegeesUsers = identityService.getUsersByDelegee(userUUID);
    for (final UserImpl delegeesUser : delegeesUsers) {
      delegeesUser.setDelegeeUUID(null);
    }
    identityService.deleteUser(user);

    final Set<String> userUUIDs = new HashSet<String>();
    userUUIDs.add(userUUID);
    removeUsersFromRules(userUUIDs);
  }

  @Override
  public void setUserMemberships(final String userUUID, final Collection<String> membershipUUIDs)
      throws UserNotFoundException, MembershipNotFoundException {
    FacadeUtil.checkArgsNotNull(userUUID, membershipUUIDs);
    final IdentityService identityService = EnvTool.getIdentityService();
    final UserImpl user = identityService.getUser(userUUID);
    if (user == null) {
      throw new UserNotFoundException("bai_IAPII_2", userUUID);
    }
    final Set<MembershipImpl> userMemberships = new HashSet<MembershipImpl>();
    for (final String membershipUUID : membershipUUIDs) {
      final MembershipImpl membership = identityService.getMembership(membershipUUID);
      if (membership == null) {
        throw new MembershipNotFoundException("bai_IAPII_8", membershipUUID);
      }
      userMemberships.add(membership);
    }
    identityService.setUserMemberships(user, userMemberships);
  }

  @Override
  public boolean groupExists(final String groupUUID) {
    FacadeUtil.checkArgsNotNull(groupUUID);
    final IdentityService identityService = EnvTool.getIdentityService();
    if (identityService.getGroup(groupUUID) != null) {
      return true;
    }
    return false;
  }

  @Override
  public Group updateGroupByUUID(final String groupUUID, final String name, final String label,
      final String description, final String parentGroupUUID) throws GroupNotFoundException,
      GroupAlreadyExistsException {
    FacadeUtil.checkArgsNotNull(groupUUID, name);
    final IdentityService identityService = EnvTool.getIdentityService();
    final GroupImpl group = identityService.getGroup(groupUUID);
    if (group == null) {
      throw new GroupNotFoundException("bai_IAPII_12", groupUUID);
    }
    GroupImpl parentGroup = null;
    if (parentGroupUUID != null) {
      parentGroup = identityService.getGroup(parentGroupUUID);
      if (parentGroup == null) {
        throw new GroupNotFoundException("bai_IAPII_13", parentGroupUUID);
      }
    }
    if (!group.getName().equals(name)) {
      final List<GroupImpl> groupChildren = identityService.getGroupChildren(parentGroupUUID);
      for (final GroupImpl child : groupChildren) {
        if (child.getName().equals(name)) {
          throw new GroupAlreadyExistsException("bai_IAPII_7", name);
        }
      }
    }
    group.setParentGroup(parentGroup);
    group.setName(name);
    group.setLabel(label);
    group.setDescription(description);
    identityService.updateGroup(group);
    return new GroupImpl(group);
  }

  @Override
  public ProfileMetadata updateProfileMetadataByUUID(final String profileMetadataUUID, final String name,
      final String label) throws MetadataNotFoundException, MetadataAlreadyExistsException {
    FacadeUtil.checkArgsNotNull(profileMetadataUUID, name);
    final IdentityService identityService = EnvTool.getIdentityService();
    final ProfileMetadataImpl metadata = identityService.getProfileMetadata(profileMetadataUUID);
    if (metadata == null) {
      throw new MetadataNotFoundException("bai_IAPII_11", profileMetadataUUID);
    }
    if (!metadata.getName().equals(name) && identityService.findProfileMetadataByName(name) != null) {
      throw new MetadataAlreadyExistsException("bai_IAPII_14", name);
    }
    metadata.setName(name);
    metadata.setLabel(label);
    identityService.updateProfileMetadata(metadata);
    return new ProfileMetadataImpl(metadata);
  }

  @Override
  public Role updateRoleByUUID(final String roleUUID, final String name, final String label, final String description)
      throws RoleNotFoundException, RoleAlreadyExistsException {
    FacadeUtil.checkArgsNotNull(roleUUID, name);
    final IdentityService identityService = EnvTool.getIdentityService();
    final RoleImpl role = identityService.getRole(roleUUID);
    if (role == null) {
      throw new RoleNotFoundException("bai_IAPII_3", roleUUID);
    }
    if (!role.getName().equals(name) && identityService.findRoleByName(name) != null) {
      throw new RoleAlreadyExistsException("bai_IAPII_5", name);
    }
    role.setName(name);
    role.setLabel(label);
    role.setDescription(description);
    identityService.updateRole(role);
    return new RoleImpl(role);
  }

  @Override
  public User updateUserByUUID(final String userUUID, final String username, final String firstName,
      final String lastName, final String title, final String jobTitle, final String managerUserUUID,
      final Map<String, String> profileMetadata) throws UserNotFoundException, UserAlreadyExistsException,
      MetadataNotFoundException {
    FacadeUtil.checkArgsNotNull(userUUID, username);
    final IdentityService identityService = EnvTool.getIdentityService();
    final UserImpl user = identityService.getUser(userUUID);
    if (user == null) {
      throw new UserNotFoundException("bai_IAPII_2", userUUID);
    }
    if (!user.getUsername().equals(username) && identityService.findUserByUsername(username) != null) {
      throw new UserAlreadyExistsException("bai_IAPII_6", username);
    }
    user.setUsername(username);
    user.setFirstName(firstName);
    user.setLastName(lastName);
    user.setTitle(title);
    user.setJobTitle(jobTitle);
    if (managerUserUUID != null) {
      final UserImpl manager = identityService.getUser(managerUserUUID);
      if (manager == null) {
        throw new UserNotFoundException("bai_IAPII_10", managerUserUUID);
      }
    }
    user.setManagerUUID(managerUserUUID);
    if (profileMetadata != null) {
      final Map<ProfileMetadata, String> userMetadata = new HashMap<ProfileMetadata, String>();
      for (final Entry<String, String> profileMetadataEntry : profileMetadata.entrySet()) {
        final ProfileMetadataImpl metadata = identityService.findProfileMetadataByName(profileMetadataEntry.getKey());
        if (metadata == null) {
          throw new MetadataNotFoundException("bai_IAPII_11", profileMetadataEntry.getKey());
        }
        metadata.getUsers().put(user, profileMetadataEntry.getValue());
        identityService.updateProfileMetadata(metadata);
        userMetadata.put(metadata, profileMetadataEntry.getValue());
      }
      user.setMetadata(userMetadata);
      identityService.updateUser(user);
    }
    return new UserImpl(user);
  }

  @Override
  public void updateUserDelegee(final String userUUID, final String delegeeUserUUID) throws UserNotFoundException {
    FacadeUtil.checkArgsNotNull(userUUID);
    final IdentityService identityService = EnvTool.getIdentityService();
    final UserImpl user = identityService.getUser(userUUID);
    if (user == null) {
      throw new UserNotFoundException("bai_IAPII_2", userUUID);
    }
    if (delegeeUserUUID != null) {
      final UserImpl delegee = identityService.getUser(delegeeUserUUID);
      if (delegee == null) {
        throw new UserNotFoundException("bai_IAPII_15", delegeeUserUUID);
      }
    }
    user.setDelegeeUUID(delegeeUserUUID);
    identityService.updateUser(user);
  }

  @Override
  public void updateUserPersonalContactInfo(final String userUUID, final String email, final String phoneNumber,
      final String mobileNumber, final String faxNumber, final String building, final String room,
      final String address, final String zipCode, final String city, final String state, final String country,
      final String website) throws UserNotFoundException {
    FacadeUtil.checkArgsNotNull(userUUID);
    final IdentityService identityService = EnvTool.getIdentityService();
    final UserImpl user = identityService.getUser(userUUID);
    if (user == null) {
      throw new UserNotFoundException("bai_IAPII_2", userUUID);
    }
    final ContactInfoImpl personalContactInfo = createContactInfo(email, phoneNumber, mobileNumber, faxNumber,
        building, room, address, zipCode, city, state, country, website);
    user.setPersonalContactInfo(personalContactInfo);
    identityService.updateUser(user);
  }

  @Override
  public void updateUserProfessionalContactInfo(final String userUUID, final String email, final String phoneNumber,
      final String mobileNumber, final String faxNumber, final String building, final String room,
      final String address, final String zipCode, final String city, final String state, final String country,
      final String website) throws UserNotFoundException {
    FacadeUtil.checkArgsNotNull(userUUID);
    final IdentityService identityService = EnvTool.getIdentityService();
    final UserImpl user = identityService.getUser(userUUID);
    if (user == null) {
      throw new UserNotFoundException("bai_IAPII_2", userUUID);
    }
    final ContactInfoImpl professionalContactInfo = createContactInfo(email, phoneNumber, mobileNumber, faxNumber,
        building, room, address, zipCode, city, state, country, website);
    user.setProfessionalContactInfo(professionalContactInfo);
    identityService.updateUser(user);
  }

  private ContactInfoImpl createContactInfo(final String email, final String phoneNumber, final String mobileNumber,
      final String faxNumber, final String building, final String room, final String address, final String zipCode,
      final String city, final String state, final String country, final String website) {
    final ContactInfoImpl contactInfo = new ContactInfoImpl();
    contactInfo.setEmail(email);
    contactInfo.setPhoneNumber(phoneNumber);
    contactInfo.setMobileNumber(mobileNumber);
    contactInfo.setFaxNumber(faxNumber);
    contactInfo.setBuilding(building);
    contactInfo.setRoom(room);
    contactInfo.setAddress(address);
    contactInfo.setZipCode(zipCode);
    contactInfo.setCity(city);
    contactInfo.setState(state);
    contactInfo.setCountry(country);
    contactInfo.setWebsite(website);
    return contactInfo;
  }

  @Override
  public void removeGroups(final Collection<String> groupUUIDs) throws GroupNotFoundException {
    FacadeUtil.checkArgsNotNull(groupUUIDs);
    final IdentityService identityService = EnvTool.getIdentityService();
    for (final String groupUUID : groupUUIDs) {
      final GroupImpl group = identityService.getGroup(groupUUID);
      if (group != null) {
        final List<GroupImpl> children = identityService.getGroupChildren(groupUUID);
        final List<String> childrenUUIDs = new ArrayList<String>();
        for (final GroupImpl child : children) {
          childrenUUIDs.add(child.getUUID());
        }
        if (!children.isEmpty()) {
          removeGroups(childrenUUIDs);
        }
        final Set<MembershipImpl> memberships = identityService.getMembershipsByGroup(group.getUUID());
        final Set<String> membershipUUIDs = new HashSet<String>();
        for (final MembershipImpl membership : memberships) {
          final List<UserImpl> usersInMembership = identityService.getUsersByMembership(membership.getUUID());
          for (final UserImpl user : usersInMembership) {
            identityService.removeMembershipFromUser(user, membership);
          }
          identityService.deleteMembership(membership);
          membershipUUIDs.add(membership.getUUID());
        }
        removeMembershipsFromRules(membershipUUIDs);
        identityService.deleteGroup(group);
      }
    }
    removeGroupsFromRules(groupUUIDs);
  }

  @Override
  public void removeRoles(final Collection<String> roleUUIDs) throws RoleNotFoundException {
    FacadeUtil.checkArgsNotNull(roleUUIDs);
    final IdentityService identityService = EnvTool.getIdentityService();
    for (final String roleUUID : roleUUIDs) {
      final RoleImpl role = identityService.getRole(roleUUID);
      if (role == null) {
        throw new RoleNotFoundException("bai_IAPII_3", roleUUID);
      }
      final Set<MembershipImpl> memberships = identityService.getMembershipsByRole(role.getUUID());
      final Set<String> membershipUUIDs = new HashSet<String>();
      for (final MembershipImpl membership : memberships) {
        final List<UserImpl> usersInMembership = identityService.getUsersByMembership(membership.getUUID());
        for (final UserImpl user : usersInMembership) {
          identityService.removeMembershipFromUser(user, membership);
        }
        identityService.deleteMembership(membership);
        membershipUUIDs.add(membership.getUUID());
      }
      removeMembershipsFromRules(membershipUUIDs);
      identityService.deleteRole(role);
    }

    removeRolesFromRules(roleUUIDs);
  }

  @Override
  public void removeUsers(final Collection<String> userUUIDs) throws UserNotFoundException {
    FacadeUtil.checkArgsNotNull(userUUIDs);
    final IdentityService identityService = EnvTool.getIdentityService();
    for (final String userUUID : userUUIDs) {
      final UserImpl user = identityService.getUser(userUUID);
      if (user == null) {
        throw new UserNotFoundException("bai_IAPII_2", userUUID);
      }
      final List<UserImpl> usersOfManager = identityService.getUsersByManager(userUUID);
      for (final UserImpl userOfManager : usersOfManager) {
        userOfManager.setManagerUUID(null);
      }
      final List<UserImpl> delegeesUsers = identityService.getUsersByDelegee(userUUID);
      for (final UserImpl delegeesUser : delegeesUsers) {
        delegeesUser.setDelegeeUUID(null);
      }
      identityService.deleteUser(user);
    }
    removeUsersFromRules(userUUIDs);
  }

  @Override
  public void removeProfileMetadata(final Collection<String> profileMetadataUUIDs) throws MetadataNotFoundException {
    Misc.checkArgsNotNull(profileMetadataUUIDs);
    final IdentityService identityService = EnvTool.getIdentityService();
    for (final String profileMetadataUUID : profileMetadataUUIDs) {
      final ProfileMetadataImpl profileMetadata = identityService.getProfileMetadata(profileMetadataUUID);
      if (profileMetadata == null) {
        throw new MetadataNotFoundException("bai_IAPII_11", profileMetadataUUID);
      }
      identityService.deleteProfileMetadata(profileMetadata);
    }
  }

  @Override
  public int getNumberOfProfileMetadata() {
    final IdentityService identityService = EnvTool.getIdentityService();
    return identityService.getNumberOfProfileMetadata();
  }

  private void removeMembershipsFromRules(final Collection<String> membershipUUIDs) {
    final PrivilegeService privilegeService = EnvTool.getPrivilegeService();
    final List<Rule> rules = privilegeService.getAllApplicableRules(null, null, null, membershipUUIDs, null);
    for (final Rule rule : rules) {
      ((RuleImpl) rule).removeMemberships(membershipUUIDs);
      privilegeService.updateRule(rule);
    }
  }

  private void removeRolesFromRules(final Collection<String> roleUUIDs) {
    final PrivilegeService privilegeService = EnvTool.getPrivilegeService();
    final List<Rule> rules = privilegeService.getAllApplicableRules(null, roleUUIDs, null, null, null);
    for (final Rule rule : rules) {
      ((RuleImpl) rule).removeRoles(roleUUIDs);
      privilegeService.updateRule(rule);
    }
  }

  private void removeGroupsFromRules(final Collection<String> groupUUIDs) {
    final PrivilegeService privilegeService = EnvTool.getPrivilegeService();
    final List<Rule> rules = privilegeService.getAllApplicableRules(null, null, groupUUIDs, null, null);
    for (final Rule rule : rules) {
      ((RuleImpl) rule).removeGroups(groupUUIDs);
      privilegeService.updateRule(rule);
    }
  }

  private void removeUsersFromRules(final Collection<String> userUUIDs) {
    final PrivilegeService privilegeService = EnvTool.getPrivilegeService();
    for (final String userUUID : userUUIDs) {
      final List<Rule> rules = privilegeService.getAllApplicableRules(userUUID, null, null, null, null);
      final Set<String> userUUIDCollection = new HashSet<String>();
      userUUIDCollection.add(userUUID);
      for (final Rule rule : rules) {
        ((RuleImpl) rule).removeUsers(userUUIDCollection);
        privilegeService.updateRule(rule);
      }
    }
  }

  @Override
  public User importUser(final String uuid, final String username, final String passwordHash, final String firstName,
      final String lastName, final String title, final String jobTitle, final String managerUserUUID,
      final Map<String, String> profileMetadata) throws UserAlreadyExistsException, MetadataNotFoundException {
    FacadeUtil.checkArgsNotNull(uuid, username, passwordHash);
    final IdentityService identityService = EnvTool.getIdentityService();
    UserImpl user = identityService.getUser(uuid);
    if (user != null) {
      throw new UserAlreadyExistsException("bai_IAPII_4", uuid);
    }
    user = identityService.findUserByUsername(username);
    if (user != null) {
      throw new UserAlreadyExistsException("bai_IAPII_4", username);
    }
    final UserImpl userToImport = new UserImpl(uuid, username, passwordHash);
    userToImport.setFirstName(firstName);
    userToImport.setLastName(lastName);
    userToImport.setJobTitle(jobTitle);
    userToImport.setTitle(title);
    userToImport.setManagerUUID(managerUserUUID);
    identityService.importUser(userToImport);

    if (profileMetadata != null) {
      final Map<ProfileMetadata, String> userMetadata = new HashMap<ProfileMetadata, String>();
      for (final Entry<String, String> profileMetadataEntry : profileMetadata.entrySet()) {
        final ProfileMetadataImpl metadata = identityService.findProfileMetadataByName(profileMetadataEntry.getKey());
        if (metadata == null) {
          throw new MetadataNotFoundException("bai_IAPII_11", profileMetadataEntry.getKey());
        }
        metadata.getUsers().put(userToImport, profileMetadataEntry.getValue());
        identityService.updateProfileMetadata(metadata);
        userMetadata.put(metadata, profileMetadataEntry.getValue());
      }
      userToImport.setMetadata(userMetadata);
      identityService.updateUser(userToImport);
    }
    return new UserImpl(userToImport);
  }

  @Override
  public Group getGroupUsingPath(final List<String> path) {
    FacadeUtil.checkArgsNotNull(path);
    if (path.isEmpty()) {
      return null;
    }
    final String groupName = path.get(path.size() - 1);
    final IdentityService identityService = EnvTool.getIdentityService();
    final Set<GroupImpl> possibleGroups = identityService.findGroupsByName(groupName);
    GroupImpl group = null;
    for (final GroupImpl possibleGroup : possibleGroups) {
      if (isAValidGroupPath(possibleGroup, path)) {
        group = possibleGroup;
        break;
      }
    }
    if (group != null) {
      return new GroupImpl(group);
    } else {
      return null;
    }
  }

  private boolean isAValidGroupPath(final GroupImpl group, final List<String> hierarchy) {
    final String groupPath = getGroupPath(group);
    final String expectedPath = getListPath(hierarchy);
    return groupPath.equals(expectedPath);
  }

  private String getListPath(final List<String> hierarchy) {
    final StringBuilder builder = new StringBuilder();
    for (final String groupName : hierarchy) {
      builder.append("/").append(groupName);
    }
    return builder.toString();
  }

  private String getGroupPath(final GroupImpl group) {
    final StringBuilder builder = new StringBuilder();
    GroupImpl currentGroup = group;
    final IdentityService identityService = EnvTool.getIdentityService();
    while (currentGroup != null) {
      builder.insert(0, currentGroup.getName());
      builder.insert(0, "/");
      final Group parent = currentGroup.getParentGroup();
      if (parent != null) {
        currentGroup = identityService.getGroup(parent.getUUID());
      } else {
        currentGroup = null;
      }
    }
    return builder.toString();
  }

}
