/**
 * Copyright (C) 2010-2012 BonitaSoft S.A.
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ow2.bonita.facade.APIAccessor;
import org.ow2.bonita.facade.IdentityAPI;
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
import org.ow2.bonita.facade.internal.AbstractRemoteIdentityAPI;
import org.ow2.bonita.facade.paging.GroupCriterion;
import org.ow2.bonita.facade.paging.RoleCriterion;
import org.ow2.bonita.facade.paging.UserCriterion;
import org.ow2.bonita.identity.auth.DomainOwner;
import org.ow2.bonita.identity.auth.PasswordOwner;
import org.ow2.bonita.identity.auth.RESTUserOwner;
import org.ow2.bonita.identity.auth.UserOwner;

/**
 * 
 * @author Elias Ricken de Medeiros, Matthieu Chaffotte
 * 
 */
public class AbstractRemoteIdentityAPIImpl implements AbstractRemoteIdentityAPI {

  protected Map<String, IdentityAPI> apis = new HashMap<String, IdentityAPI>();

  protected IdentityAPI getAPI(final Map<String, String> options) {
    if (options == null) {
      throw new IllegalArgumentException("The options are null or not well set.");
    }
    final String queryList = options.get(APIAccessor.QUERYLIST_OPTION);
    final String user = options.get(APIAccessor.USER_OPTION);
    final String domain = options.get(APIAccessor.DOMAIN_OPTION);
    UserOwner.setUser(user);
    DomainOwner.setDomain(domain);

    final String restUser = options.get(APIAccessor.REST_USER_OPTION);
    if (restUser != null) {
      RESTUserOwner.setUser(restUser);
      final String restPswd = options.get(APIAccessor.PASSWORD_HASH_OPTION);
      PasswordOwner.setPassword(restPswd);
    }

    if (!apis.containsKey(queryList)) {
      apis.put(queryList, new StandardAPIAccessorImpl().getIdentityAPI(queryList));
    }
    return apis.get(queryList);
  }

  @Override
  public Role addRole(final String name, final Map<String, String> options) throws RemoteException,
      RoleAlreadyExistsException {
    return getAPI(options).addRole(name);
  }

  @Override
  public Role addRole(final String name, final String label, final String description, final Map<String, String> options)
      throws RemoteException, RoleAlreadyExistsException {
    return getAPI(options).addRole(name, label, description);
  }

  @Override
  @Deprecated
  public void addRoleToUser(final String roleName, final String username, final Map<String, String> options)
      throws RemoteException, UserNotFoundException, RoleNotFoundException {
    getAPI(options).addRoleToUser(roleName, username);
  }

  @Override
  @Deprecated
  public void setUserRoles(final String username, final Set<String> roleNames, final Map<String, String> options)
      throws RemoteException, UserNotFoundException, RoleNotFoundException {
    getAPI(options).setUserRoles(username, roleNames);
  }

  @Override
  public User addUser(final String username, final String password, final Map<String, String> options)
      throws RemoteException, UserAlreadyExistsException {
    return getAPI(options).addUser(username, password);
  }

  @Override
  @Deprecated
  public User addUser(final String username, final String password, final String firstName, final String lastName,
      final String email, final Map<String, String> options) throws RemoteException, UserAlreadyExistsException {
    return getAPI(options).addUser(username, password, firstName, lastName, email);
  }

  @Override
  @Deprecated
  public Role getRole(final String name, final Map<String, String> options) throws RemoteException,
      RoleNotFoundException {
    return getAPI(options).getRole(name);
  }

  @Override
  @Deprecated
  public Set<Role> getRoles(final Map<String, String> options) throws RemoteException {
    return getAPI(options).getRoles();
  }

  @Override
  @Deprecated
  public User getUser(final String username, final Map<String, String> options) throws RemoteException,
      UserNotFoundException {
    return getAPI(options).getUser(username);
  }

  @Override
  @Deprecated
  public Set<Role> getUserRoles(final String username, final Map<String, String> options) throws RemoteException,
      UserNotFoundException {
    return getAPI(options).getUserRoles(username);
  }

  @Override
  @Deprecated
  public Set<User> getUsers(final Map<String, String> options) throws RemoteException {
    return getAPI(options).getUsers();
  }

  @Override
  @Deprecated
  public Set<User> getUsersInRole(final String name, final Map<String, String> options) throws RemoteException,
      RoleNotFoundException {
    return getAPI(options).getUsersInRole(name);
  }

  @Override
  @Deprecated
  public void removeRole(final String name, final Map<String, String> options) throws RemoteException,
      RoleNotFoundException {
    getAPI(options).removeRole(name);
  }

  @Override
  @Deprecated
  public void removeRoleFromUser(final String roleName, final String username, final Map<String, String> options)
      throws RemoteException, UserNotFoundException, RoleNotFoundException {
    getAPI(options).removeRoleFromUser(roleName, username);
  }

  @Override
  @Deprecated
  public void removeUser(final String username, final Map<String, String> options) throws RemoteException,
      UserNotFoundException {
    getAPI(options).removeUser(username);
  }

  @Override
  @Deprecated
  public Role updateRole(final String oldName, final String name, final String label, final String description,
      final Map<String, String> options) throws RemoteException, RoleNotFoundException, RoleAlreadyExistsException {
    return getAPI(options).updateRole(oldName, name, label, description);
  }

  @Override
  @Deprecated
  public User updateUser(final String oldUsername, final String username, final String password,
      final String firstName, final String lastName, final String email, final Map<String, String> options)
      throws RemoteException, UserNotFoundException, UserAlreadyExistsException {
    return getAPI(options).updateUser(oldUsername, username, password, firstName, lastName, email);
  }

  @Override
  public User updateUserPassword(final String userUUID, final String password, final Map<String, String> options)
      throws RemoteException, UserNotFoundException {
    return getAPI(options).updateUserPassword(userUUID, password);
  }

  @Override
  public Group addGroup(final String name, final String parentGroupUUID, final Map<String, String> options)
      throws RemoteException, GroupAlreadyExistsException, GroupNotFoundException {
    return getAPI(options).addGroup(name, parentGroupUUID);
  }

  @Override
  public Group addGroup(final String name, final String label, final String description, final String parentGroupUUID,
      final Map<String, String> options) throws RemoteException, GroupAlreadyExistsException, GroupNotFoundException {
    return getAPI(options).addGroup(name, label, description, parentGroupUUID);
  }

  @Override
  public void addMembershipToUser(final String userUUID, final String membershipUUID, final Map<String, String> options)
      throws RemoteException, UserNotFoundException, MembershipNotFoundException {
    getAPI(options).addMembershipToUser(userUUID, membershipUUID);
  }

  @Override
  public ProfileMetadata addProfileMetadata(final String name, final Map<String, String> options)
      throws RemoteException, MetadataAlreadyExistsException {
    return getAPI(options).addProfileMetadata(name);
  }

  @Override
  public ProfileMetadata addProfileMetadata(final String name, final String label, final Map<String, String> options)
      throws RemoteException, MetadataAlreadyExistsException {
    return getAPI(options).addProfileMetadata(name, label);
  }

  @Override
  public User addUser(final String username, final String password, final String firstName, final String lastName,
      final String title, final String jobTitle, final String managerUserUUID,
      final Map<String, String> profileMetadata, final Map<String, String> options) throws RemoteException,
      UserAlreadyExistsException, UserNotFoundException, MetadataNotFoundException {
    return getAPI(options).addUser(username, password, firstName, lastName, title, jobTitle, managerUserUUID,
        profileMetadata);
  }

  @Override
  public ProfileMetadata findProfileMetadataByName(final String metadataName, final Map<String, String> options)
      throws RemoteException, MetadataNotFoundException {
    return getAPI(options).findProfileMetadataByName(metadataName);
  }

  @Override
  public Role findRoleByName(final String name, final Map<String, String> options) throws RemoteException,
      RoleNotFoundException {
    return getAPI(options).findRoleByName(name);
  }

  @Override
  public User findUserByUserName(final String username, final Map<String, String> options) throws RemoteException,
      UserNotFoundException {
    return getAPI(options).findUserByUserName(username);
  }

  @Override
  public List<Group> getAllGroups(final Map<String, String> options) throws RemoteException {
    return getAPI(options).getAllGroups();
  }

  @Override
  public List<ProfileMetadata> getAllProfileMetadata(final Map<String, String> options) throws RemoteException {
    return getAPI(options).getAllProfileMetadata();
  }

  @Override
  public List<Role> getAllRoles(final Map<String, String> options) throws RemoteException {
    return getAPI(options).getAllRoles();
  }

  @Override
  public List<User> getAllUsers(final Map<String, String> options) throws RemoteException {
    return getAPI(options).getAllUsers();
  }

  @Override
  public List<User> getAllUsersInGroup(final String groupUUID, final Map<String, String> options)
      throws RemoteException, GroupNotFoundException {
    return getAPI(options).getAllUsersInGroup(groupUUID);
  }

  @Override
  public List<User> getAllUsersInMembership(final String membershipUUID, final Map<String, String> options)
      throws RemoteException, MembershipNotFoundException {
    return getAPI(options).getAllUsersInMembership(membershipUUID);
  }

  @Override
  public List<User> getAllUsersInRole(final String roleUUID, final Map<String, String> options) throws RemoteException,
      RoleNotFoundException {
    return getAPI(options).getAllUsersInRole(roleUUID);
  }

  @Override
  public List<User> getAllUsersInRoleAndGroup(final String roleUUID, final String groupUUID,
      final Map<String, String> options) throws RemoteException, RoleNotFoundException, GroupNotFoundException {
    return getAPI(options).getAllUsersInRoleAndGroup(roleUUID, groupUUID);
  }

  @Override
  public List<Group> getChildrenGroupsByUUID(final String groupUUID, final Map<String, String> options)
      throws RemoteException {
    return getAPI(options).getChildrenGroupsByUUID(groupUUID);
  }

  @Override
  public List<Group> getChildrenGroups(final String groupUUID, final int fromIndex, final int numberOfGroups,
      final Map<String, String> options) throws RemoteException, GroupNotFoundException {
    return getAPI(options).getChildrenGroups(groupUUID, fromIndex, numberOfGroups);
  }

  @Override
  public List<Group> getChildrenGroups(final String groupUUID, final int fromIndex, final int numberOfGroups,
      final GroupCriterion pagingCriterion, final Map<String, String> options) throws GroupNotFoundException,
      RemoteException {
    return getAPI(options).getChildrenGroups(groupUUID, fromIndex, numberOfGroups, pagingCriterion);
  }

  @Override
  public int getNumberOfChildrenGroups(final String groupUUID, final Map<String, String> options)
      throws RemoteException, GroupNotFoundException {
    return getAPI(options).getNumberOfChildrenGroups(groupUUID);
  }

  @Override
  public Group getGroupByUUID(final String groupUUID, final Map<String, String> options) throws RemoteException,
      GroupNotFoundException {
    return getAPI(options).getGroupByUUID(groupUUID);
  }

  @Override
  public List<Group> getGroups(final int fromIndex, final int numberOfGroups, final Map<String, String> options)
      throws RemoteException {
    return getAPI(options).getGroups(fromIndex, numberOfGroups);
  }

  @Override
  public List<Group> getGroups(final int fromIndex, final int numberOfGroups, final GroupCriterion pagingCriterion,
      final Map<String, String> options) throws RemoteException {
    return getAPI(options).getGroups(fromIndex, numberOfGroups, pagingCriterion);
  }

  @Override
  public Membership getMembershipByUUID(final String membershipUUID, final Map<String, String> options)
      throws RemoteException, MembershipNotFoundException {
    return getAPI(options).getMembershipByUUID(membershipUUID);
  }

  @Override
  public Membership getMembershipForRoleAndGroup(final String roleUUID, final String groupUUID,
      final Map<String, String> options) throws RemoteException, RoleNotFoundException, GroupNotFoundException {
    return getAPI(options).getMembershipForRoleAndGroup(roleUUID, groupUUID);
  }

  @Override
  public int getNumberOfGroups(final Map<String, String> options) throws RemoteException {
    return getAPI(options).getNumberOfGroups();
  }

  @Override
  public int getNumberOfRoles(final Map<String, String> options) throws RemoteException {
    return getAPI(options).getNumberOfRoles();
  }

  @Override
  public int getNumberOfUsers(final Map<String, String> options) throws RemoteException {
    return getAPI(options).getNumberOfUsers();
  }

  @Override
  public int getNumberOfUsersInGroup(final String groupUUID, final Map<String, String> options) throws RemoteException {
    return getAPI(options).getNumberOfUsersInGroup(groupUUID);
  }

  @Override
  public int getNumberOfUsersInRole(final String roleUUID, final Map<String, String> options) throws RemoteException {
    return getAPI(options).getNumberOfUsersInRole(roleUUID);
  }

  @Override
  public ProfileMetadata getProfileMetadataByUUID(final String metadataUUID, final Map<String, String> options)
      throws RemoteException, MetadataNotFoundException {
    return getAPI(options).getProfileMetadataByUUID(metadataUUID);
  }

  @Override
  public List<ProfileMetadata> getProfileMetadata(final int fromIndex, final int numberOfMetadata,
      final Map<String, String> options) throws RemoteException {
    return getAPI(options).getProfileMetadata(fromIndex, numberOfMetadata);
  }

  @Override
  public int getNumberOfProfileMetadata(final Map<String, String> options) throws RemoteException {
    return getAPI(options).getNumberOfProfileMetadata();
  }

  @Override
  public Role getRoleByUUID(final String roleUUID, final Map<String, String> options) throws RemoteException,
      RoleNotFoundException {
    return getAPI(options).getRoleByUUID(roleUUID);
  }

  @Override
  public List<Role> getRoles(final int fromIndex, final int numberOfRoles, final Map<String, String> options)
      throws RemoteException {
    return getAPI(options).getRoles(fromIndex, numberOfRoles);
  }

  @Override
  public List<Role> getRoles(final int fromIndex, final int numberOfRoles, final RoleCriterion pagingCriterion,
      final Map<String, String> options) throws RemoteException {
    return getAPI(options).getRoles(fromIndex, numberOfRoles, pagingCriterion);
  }

  @Override
  public User getUserByUUID(final String userUUID, final Map<String, String> options) throws RemoteException,
      UserNotFoundException {
    return getAPI(options).getUserByUUID(userUUID);
  }

  @Override
  public List<User> getUsers(final int fromIndex, final int numberOfUsers, final Map<String, String> options)
      throws RemoteException {
    return getAPI(options).getUsers(fromIndex, numberOfUsers);
  }

  @Override
  public List<User> getUsers(final int fromIndex, final int numberOfUsers, final UserCriterion pagingCriterion,
      final Map<String, String> options) throws RemoteException {
    return getAPI(options).getUsers(fromIndex, numberOfUsers, pagingCriterion);
  }

  @Override
  public List<User> getUsersInGroup(final String groupUUID, final int fromIndex, final int numberOfUsers,
      final Map<String, String> options) throws RemoteException, GroupNotFoundException {
    return getAPI(options).getUsersInGroup(groupUUID, fromIndex, numberOfUsers);
  }

  @Override
  public List<User> getUsersInGroup(final String groupUUID, final int fromIndex, final int numberOfUsers,
      final UserCriterion pagingCriterion, final Map<String, String> options) throws GroupNotFoundException,
      RemoteException {
    return getAPI(options).getUsersInGroup(groupUUID, fromIndex, numberOfUsers, pagingCriterion);
  }

  @Override
  public List<User> getUsersInRole(final String roleUUID, final int fromIndex, final int numberOfUsers,
      final Map<String, String> options) throws RemoteException, RoleNotFoundException {
    return getAPI(options).getUsersInRole(roleUUID, fromIndex, numberOfUsers);
  }

  @Override
  public List<User> getUsersInRole(final String roleUUID, final int fromIndex, final int numberOfUsers,
      final UserCriterion pagingCriterion, final Map<String, String> options) throws RoleNotFoundException,
      RemoteException {
    return getAPI(options).getUsersInRole(roleUUID, fromIndex, numberOfUsers, pagingCriterion);
  }

  @Override
  public void removeGroupByUUID(final String groupUUID, final Map<String, String> options) throws RemoteException,
      GroupNotFoundException {
    getAPI(options).removeGroupByUUID(groupUUID);
  }

  @Override
  public void removeMembershipFromUser(final String userUUID, final String membershipUUID,
      final Map<String, String> options) throws RemoteException, UserNotFoundException, MembershipNotFoundException {
    getAPI(options).removeMembershipFromUser(userUUID, membershipUUID);
  }

  @Override
  public void removeProfileMetadataByUUID(final String profileMetadataUUID, final Map<String, String> options)
      throws RemoteException, MetadataNotFoundException {
    getAPI(options).removeProfileMetadataByUUID(profileMetadataUUID);
  }

  @Override
  public void removeRoleByUUID(final String roleUUID, final Map<String, String> options) throws RemoteException,
      RoleNotFoundException {
    getAPI(options).removeRoleByUUID(roleUUID);
  }

  @Override
  public void removeUserByUUID(final String userUUID, final Map<String, String> options) throws RemoteException,
      UserNotFoundException {
    getAPI(options).removeUserByUUID(userUUID);
  }

  @Override
  public Group updateGroupByUUID(final String groupUUID, final String name, final String label,
      final String description, final String parentGroupUUID, final Map<String, String> options)
      throws RemoteException, GroupNotFoundException, GroupAlreadyExistsException {
    return getAPI(options).updateGroupByUUID(groupUUID, name, label, description, parentGroupUUID);
  }

  @Override
  public ProfileMetadata updateProfileMetadataByUUID(final String profileMetadataUUID, final String name,
      final String label, final Map<String, String> options) throws RemoteException, MetadataNotFoundException,
      MetadataAlreadyExistsException {
    return getAPI(options).updateProfileMetadataByUUID(profileMetadataUUID, name, label);
  }

  @Override
  public Role updateRoleByUUID(final String roleUUID, final String name, final String label, final String description,
      final Map<String, String> options) throws RemoteException, RoleNotFoundException, RoleAlreadyExistsException {
    return getAPI(options).updateRoleByUUID(roleUUID, name, label, description);
  }

  @Override
  public User updateUserByUUID(final String userUUID, final String username, final String firstName,
      final String lastName, final String title, final String jobTitle, final String managerUserUUID,
      final Map<String, String> profileMetadata, final Map<String, String> options) throws RemoteException,
      UserNotFoundException, UserAlreadyExistsException, MetadataNotFoundException {
    return getAPI(options).updateUserByUUID(userUUID, username, firstName, lastName, title, jobTitle, managerUserUUID,
        profileMetadata);
  }

  @Override
  public void updateUserDelegee(final String userUUID, final String delegeeUserUUID, final Map<String, String> options)
      throws RemoteException, UserNotFoundException {
    getAPI(options).updateUserDelegee(userUUID, delegeeUserUUID);
  }

  @Override
  public void updateUserPersonalContactInfo(final String userUUID, final String email, final String phoneNumber,
      final String mobileNumber, final String faxNumber, final String building, final String room,
      final String address, final String zipCode, final String city, final String state, final String country,
      final String website, final Map<String, String> options) throws RemoteException, UserNotFoundException {
    getAPI(options).updateUserPersonalContactInfo(userUUID, email, phoneNumber, mobileNumber, faxNumber, building,
        room, address, zipCode, city, state, country, website);
  }

  @Override
  public void updateUserProfessionalContactInfo(final String userUUID, final String email, final String phoneNumber,
      final String mobileNumber, final String faxNumber, final String building, final String room,
      final String address, final String zipCode, final String city, final String state, final String country,
      final String website, final Map<String, String> options) throws RemoteException, UserNotFoundException {
    getAPI(options).updateUserProfessionalContactInfo(userUUID, email, phoneNumber, mobileNumber, faxNumber, building,
        room, address, zipCode, city, state, country, website);
  }

  @Override
  public List<User> getUsersByManagerUUID(final String managerUUID, final Map<String, String> options)
      throws RemoteException, UserNotFoundException {
    return getAPI(options).getUsersByManagerUUID(managerUUID);
  }

  @Override
  public User importUser(final String userUUID, final String username, final String passwordHash,
      final String firstName, final String lastName, final String title, final String jobTitle,
      final String managerUserUUID, final Map<String, String> profileMetadata, final Map<String, String> options)
      throws RemoteException, UserAlreadyExistsException, MetadataNotFoundException {
    return getAPI(options).importUser(userUUID, username, passwordHash, firstName, lastName, title, jobTitle,
        managerUserUUID, profileMetadata);
  }

  @Override
  public Group importGroup(final String uuid, final String name, final String label, final String description,
      final String parentGroupUUID, final Map<String, String> options) throws RemoteException,
      GroupAlreadyExistsException, GroupNotFoundException {
    return getAPI(options).importGroup(uuid, name, label, description, parentGroupUUID);
  }

  @Override
  public Role importRole(final String uuid, final String name, final String label, final String description,
      final Map<String, String> options) throws RemoteException, RoleAlreadyExistsException {
    return getAPI(options).importRole(uuid, name, label, description);
  }

  @Override
  public Boolean groupExists(final String groupUUID, final Map<String, String> options) throws RemoteException {
    return getAPI(options).groupExists(groupUUID);
  }

  @Override
  public Group getGroupUsingPath(final List<String> path, final Map<String, String> options) throws RemoteException {
    return getAPI(options).getGroupUsingPath(path);
  }

}
