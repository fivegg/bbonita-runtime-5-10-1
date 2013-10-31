/**
 * Copyright (C) 2009-2010  BonitaSoft S.A.
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
package org.ow2.bonita.facade;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import org.ow2.bonita.facade.paging.GroupCriterion;
import org.ow2.bonita.facade.paging.RoleCriterion;
import org.ow2.bonita.facade.paging.UserCriterion;

/**
 * Bonita Identity API. 
 * This API purpose is to manage the users referential.
 * @author Anthony Birembaut, Matthieu Chaffotte
 *
 */
public interface IdentityAPI {

  final String GROUP_PATH_SEPARATOR = "/";
  final String MEMBERSHIP_SEPARATOR = ":";
  final String DEFAULT_GROUP_NAME = "platform";
  final String DEFAULT_GROUP_LABEL = "Platform";
  final String DEFAULT_GROUP_DESCRIPTION = "The default group";
  final String USER_ROLE_NAME = "user";
  final String USER_ROLE_LABEL = "User";
  final String USER_ROLE_DESCRIPTION = "The user role";
  final String ADMIN_ROLE_NAME = "admin";
  final String ADMIN_ROLE_LABEL = "Admin";
  final String ADMIN_ROLE_DESCRIPTION = "The admin role";
  
  /**
   * Retrieve a {@link ProfileMetadata} from it's UUID
   * @param metadataUUID the profile metadata UUID
   * @return a {@link ProfileMetadata}
   * @throws MetadataNotFoundException
   */
  ProfileMetadata getProfileMetadataByUUID(String metadataUUID) throws MetadataNotFoundException;
  
  /**
   * Retrieve a {@link ProfileMetadata} from it's name
   * @param metadataName the profile metadata name
   * @return a {@link ProfileMetadata}
   * @throws MetadataNotFoundException
   */
  ProfileMetadata findProfileMetadataByName(String metadataName) throws MetadataNotFoundException;
  
  /**
   * Retrieve all the metadata
   * @return a {@link List} of {@link ProfileMetadata}
   */
  List<ProfileMetadata> getAllProfileMetadata();
  
  /**
   * Retrieve a limited number of ProfileMetadata (for paginated UI)
   * @param fromIndex start index
   * @param numberOfMetadata maximum number of metadata retrieved
   * @return a {@link List} of {@link ProfileMetadata}
   */
  List<ProfileMetadata> getProfileMetadata(int fromIndex, int numberOfMetadata);
  
  /**
   * Get the profile metadata count
   * @return the number of profile metadata defined
   */
  int getNumberOfProfileMetadata();
  
  /**
   * Create a new profile metadata
   * @param name the name of the metadata
   * @return the {@link ProfileMetadata} created
   * @throws MetadataAlreadyExistsException
   */
  ProfileMetadata addProfileMetadata(String name) throws MetadataAlreadyExistsException;
  
  /**
   * Create a new profile metadata
   * @param name the name of the metadata
   * @param label the label of the metadata
   * @return the {@link ProfileMetadata} created
   * @throws MetadataAlreadyExistsException
   */
  ProfileMetadata addProfileMetadata(String name, String label) throws MetadataAlreadyExistsException;
  
  /**
   * Update a profile metadata
   * @param profileMetadataUUID the UUID of the profile metadata to update
   * @param name the new name of the metadata
   * @param label the new label of the metadata
   * @return the updated {@link ProfileMetadata}
   * @throws MetadataNotFoundException
   * @throws MetadataAlreadyExistsException
   */
  ProfileMetadata updateProfileMetadataByUUID(String profileMetadataUUID, String name, String label) throws MetadataNotFoundException, MetadataAlreadyExistsException;
  
  /**
   * Permanently remove a profile metadata
   * @param profileMetadataUUID the profile metadata to remove
   * @throws MetadataNotFoundException
   */
  void removeProfileMetadataByUUID(String profileMetadataUUID) throws MetadataNotFoundException;
  
  /**
   * Permanently remove a collection of profile metadata
   * @param profileMetadataUUIDs the collection of profile metadata to remove
   * @throws MetadataNotFoundException
   */
  void removeProfileMetadata(Collection<String> profileMetadataUUIDs) throws MetadataNotFoundException;
  
  
  /**
   * Retrieve a user from its username
   * @param username the user username
   * @return a {@link User}
   * @throws UserNotFoundException
   * 
   * @deprecated use {@link IdentityAPI#findUserByUserName(String)} instead
   */
  @Deprecated
  User getUser(String username) throws UserNotFoundException;
  
  /**
   * Retrieve a user from its username
   * @param username the user username
   * @return a {@link User}
   * @throws UserNotFoundException
   */
  User findUserByUserName(String username) throws UserNotFoundException;
  
  /**
   * Retrieve a user from its UUID
   * @param userUUID the user username
   * @return a {@link User}
   * @throws UserNotFoundException
   */
  User getUserByUUID(String userUUID) throws UserNotFoundException;
  
  /**
   * Retrieve all the users
   * @return a {@link Set} of {@link User}
   * 
   * @deprecated use {@link IdentityAPI#getAllUsers()} instead
   */
  @Deprecated
  Set<User> getUsers();
  
  /**
   * Retrieve all the users
   * @return a {@link List} of {@link User}
   */
  List<User> getAllUsers();
  
  /**
   * Retrieve a limited number of users (for paginated UI)
   * @param fromIndex start index
   * @param numberOfUsers maximum number of users retrieved
   * @return a {@link List} of {@link User}
   */
  List<User> getUsers(int fromIndex, int numberOfUsers);
  
  /**
   * Retrieve a limited number of users (for paginated UI) order by the given pagingCriterion
   * @param fromIndex start index
   * @param numberOfUsers maximum number of users retrieved
   * @param pagingCriterion the criterion used to sort the retried users
   * @return a {@link List} of {@link User}
   */
  List<User> getUsers(int fromIndex, int numberOfUsers, UserCriterion pagingCriterion);
  
  /**
   * get the user count
   * @return the number of users
   */
  int getNumberOfUsers();
  
  /**
   * Create a new a user
   * @param username the user's username
   * @param password the user's password
   * @return the {@link User} created
   * @throws UserAlreadyExistsException
   */
  User addUser(String username, String password) throws UserAlreadyExistsException;
  
  /**
   * Create a new a user
   * @param username the user's username
   * @param password the user's password
   * @param firstName the user's first name
   * @param lastName the user's last name
   * @param email the user's email
   * @return the {@link User} created
   * @throws UserAlreadyExistsException
   * 
   * @deprecated use one of the other addUser methods
   */
  @Deprecated
  User addUser(String username, String password, String firstName, String lastName, String email) throws UserAlreadyExistsException;
  
  /**
   * Create a new a user
   * @param username the user's username
   * @param password the user's password
   * @param firstName the user's first name
   * @param lastName the user's last name
   * @param title the user's title
   * @param jobTitle the user's job title
   * @param managerUserUUID the user's manager's UUID
   * @param profileMetadata the user's profile metadata where the key of the map should be the name of one of the defined profile metadata
   * @return the {@link User} created
   * @throws UserAlreadyExistsException
   * @throws UserNotFoundException
   * @throws MetadataNotFoundException
   */
  User addUser(String username, String password, String firstName, String lastName, String title, String jobTitle, String managerUserUUID, Map<String, String> profileMetadata) throws UserAlreadyExistsException, UserNotFoundException, MetadataNotFoundException;
  
  /**
   * Import a user
   * @param userUUID the user UUID (should be URL compliant)
   * @param username the user's username
   * @param passwordHash the hash of the user's password
   * @param firstName the user's first name
   * @param lastName the user's last name
   * @param title the user's title
   * @param jobTitle the user's job title
   * @param managerUserUUID the user's manager's UUID
   * @param profileMetadata the user's profile metadata where the key of the map should be the name of one of the defined profile metadata
   * @return the {@link User} created
   * @throws UserAlreadyExistsException
   * @throws UserNotFoundException
   * @throws MetadataNotFoundException
   */
  User importUser(String userUUID, String username, String passwordHash, String firstName, String lastName, String title, String jobTitle, String managerUserUUID, Map<String, String> profileMetadata) throws UserAlreadyExistsException, MetadataNotFoundException;
  
  /**
   * Set the delegee of a user
   * @param userUUID the user UUID
   * @param delegeeUserUUID the delegee user UUID (null to remove the delegate)
   * @throws UserNotFoundException
   */
  void updateUserDelegee(String userUUID, String delegeeUserUUID) throws UserNotFoundException;
  
  /**
   * update a user's personal contact info
   * @param email
   * @param phoneNumber
   * @param mobileNumber
   * @param faxNumber
   * @param building
   * @param room
   * @param address
   * @param zipCode
   * @param city
   * @param state
   * @param country
   * @param website
   * @throws UserNotFoundException
   */
  void updateUserPersonalContactInfo(String userUUID, String email, String phoneNumber, String mobileNumber, String faxNumber, String building, String room, String address, String zipCode, String city, String state, String country, String website) throws UserNotFoundException;
  
  /**
   * update a user's professional contact info
   * @param email
   * @param phoneNumber
   * @param mobileNumber
   * @param faxNumber
   * @param building
   * @param room
   * @param address
   * @param zipCode
   * @param city
   * @param state
   * @param country
   * @param website
   * @throws UserNotFoundException
   */
  void updateUserProfessionalContactInfo(String userUUID, String email, String phoneNumber, String mobileNumber, String faxNumber, String building, String room, String address, String zipCode, String city, String state, String country, String website) throws UserNotFoundException;
  
  /**
   * Update a user
   * @param oldUsername the actual username of the user to update
   * @param username the user's username
   * @param password the user's password
   * @param firstName the user's first name
   * @param lastName the user's last name
   * @param email the user's email
   * @return the updated {@link User}
   * @throws UserNotFoundException
   * @throws UserAlreadyExistsException
   * 
   * @deprecated use {@link IdentityAPI#updateUserByUUID(String, String, String, String, String, String, String, Map)} to update the user's info and use {@link IdentityAPI#updateUserPassword(String, String)} to change the user's password.
   */
  @Deprecated
  User updateUser(String oldUsername, String username, String password, String firstName, String lastName, String email) throws UserNotFoundException, UserAlreadyExistsException;
  
  /**
   * Update a user
   * @param userUUID the user UUID of the user to update
   * @param username the user's username
   * @param firstName the user's first name
   * @param lastName the user's last name
   * @param title the user's title
   * @param jobTitle the user's job title
   * @param managerUserUUID the user's manager's UUID
   * @param profileMetadata the user's profile metadata where the key of the map should be the name of one of the defined profile metadata
   * @return the updated {@link User}
   * @throws UserNotFoundException
   * @throws UserAlreadyExistsException
   */
  User updateUserByUUID(String userUUID, String username, String firstName, String lastName, String title, String jobTitle, String managerUserUUID, Map<String, String> profileMetadata) throws UserNotFoundException, UserAlreadyExistsException, MetadataNotFoundException;
  
  /**
   * Update the user's password.
   * @param userUUID the user UUID
   * @param password the user new password
   * @return the updated user
   * @throws UserNotFoundException
   */
  User updateUserPassword(String userUUID, String password) throws UserNotFoundException;  
  
  /**
   * Retrieve all the users having a given role
   * @param roleName the role name
   * @return a {@link Set} of {@link User}
   * @throws RoleNotFoundException
   * 
   * @deprecated use {@link IdentityAPI#getAllUsersInRoleAndGroup(String, String)} instead with the default group {@link IdentityAPI#DEFAULT_GROUP_NAME}
   */
  @Deprecated
  Set<User> getUsersInRole(String roleName) throws RoleNotFoundException;
  
  /**
   * Retrieve all the users in a role
   * @param roleUUID the role UUID
   * @return a {@link List} of {@link User}
   * @throws RoleNotFoundException
   */
  List<User>getAllUsersInRole(String roleUUID) throws RoleNotFoundException;
  
  /**
   * Retrieve a limited number of the users in a role (for paginated UI)
   * @param roleUUID the role UUID
   * @param fromIndex start index
   * @param numberOfUsers maximum number of users retrieved
   * @return a {@link List} of {@link User}
   */
  List<User> getUsersInRole(String roleUUID, int fromIndex, int numberOfUsers) throws RoleNotFoundException;
  
  /**
   * Retrieve a limited number of the users in a role (for paginated UI)
   * @param roleUUID the role UUID
   * @param fromIndex start index
   * @param numberOfUsers maximum number of users retrieved
   * @param pagingCriterion the criterion used to sort the retrieved users
   * @return a {@link List} of {@link User}
   */
  List<User> getUsersInRole(String roleUUID, int fromIndex, int numberOfUsers, UserCriterion pagingCriterion) throws RoleNotFoundException;
  
  /**
   * Retrieve the number of users inside a group
   * @param roleUUID the role UUID
   * @return the number of users inside the group
   */
  int getNumberOfUsersInRole(String roleUUID);
  
  /**
   * Retrieve all the users of a group
   * @return a {@link List} of {@link User}
   */
  List<User>getAllUsersInGroup(String groupUUID) throws GroupNotFoundException;
  
  /**
   * Retrieve a limited number of a group's users (for paginated UI)
   * @param groupUUID the group UUID
   * @param fromIndex start index
   * @param numberOfUsers maximum number of users retrieved from the group
   * @return a {@link List} of {@link User}
   */
  List<User> getUsersInGroup(String groupUUID, int fromIndex, int numberOfUsers) throws GroupNotFoundException;
  
  /**
   * Retrieve a limited number of a group's users (for paginated UI) oder by the given criterion
   * @param groupUUID the group UUID
   * @param fromIndex start index
   * @param numberOfUsers maximum number of users retrieved from the group
   * @param pagingCriterion the criterion used to sort the retrieved users
   * @return a {@link List} of {@link User}
   */
  List<User> getUsersInGroup(String groupUUID, int fromIndex, int numberOfUsers, UserCriterion pagingCriterion) throws GroupNotFoundException;
  
  /**
   * Retrieve the number of users inside a group
   * @param groupUUID the group UUID
   * @return the number of users inside the group
   */
  int getNumberOfUsersInGroup(String groupUUID);
  
  /**
   * Retrieve all the users in the membership
   * @param MembershipUUID the membership UUID
   * @return a {@link Set} of users
   * @throws MembershipNotFoundException
   */
  List<User> getAllUsersInMembership(String membershipUUID) throws MembershipNotFoundException;
  
  /**
   * Retrieve all the users in a given role inside a group. This method return the same result as getAllUsersInMembership except you don't need to know the membership UUID for the role-group association
   * @param roleUUID the role UUID
   * @param groupUUID the group UUID
   * @return a {@link Set} of users
   * @throws RoleNotFoundException
   * @throws GroupNotFoundException
   */
  List<User> getAllUsersInRoleAndGroup(String roleUUID, String groupUUID) throws RoleNotFoundException, GroupNotFoundException;
  
  /**
   * Retrieve all the users having the same manager
   * @param managerUUID the user UUID of the manager
   * @return a {@link Set} of users
   * @throws UserNotFoundException
   */
  List<User> getUsersByManagerUUID(String managerUUID) throws UserNotFoundException;
  
  /**
   * Permanently remove a user
   * @param userUUID the user UUID
   * @throws UserNotFoundException
   */
  void removeUserByUUID(String userUUID) throws UserNotFoundException;
  
  /**
   * Permanently remove a collection of users
   * @param userUUIDs the users UUIDs
   * @throws UserNotFoundException
   */
  void removeUsers(Collection<String> userUUIDs) throws UserNotFoundException;
  
  /**
   * Permanently remove a user
   * @param username the user's username
   * @throws UserNotFoundException
   * 
   * @deprecated use {@link IdentityAPI#removeUserByUUID(String)} with a user UUID instead
   */
  @Deprecated
  void removeUser(String username) throws UserNotFoundException;
  
  
  /**
   * Retrieve a role from its name
   * @param name the role's name
   * @return a {@link Role}
   * @throws RoleNotFoundException
   * 
   * @deprecated use {@link IdentityAPI#findRoleByName(String)} instead
   */
  @Deprecated
  Role getRole(String name) throws RoleNotFoundException;

  /**
   * Retrieve a role from its name
   * @param name the role's name
   * @return a {@link Role}
   * @throws RoleNotFoundException
   */
  Role findRoleByName(String name) throws RoleNotFoundException;
  
  /**
   * Retrieve a role from its UUID
   * @param roleUUID the role's UUID
   * @return a {@link Role}
   * @throws RoleNotFoundException
   */
  Role getRoleByUUID(String roleUUID) throws RoleNotFoundException;
  
  /**
   * Retrieve all the roles
   * @return a {@link Set} of {@link Role}
   * 
   * @deprecated use {@link IdentityAPI#getAllRoles()} instead
   */
  @Deprecated
  Set<Role> getRoles();
  
  /**
   * Retrieve all the roles
   * @return a {@link List} of {@link Role}
   */
  List<Role> getAllRoles();
  
  /**
   * Retrieve a limited number of roles (for paginated UI)
   * @param fromIndex start index
   * @param numberOfRoles maximum number of roles retrieved
   * @return a {@link List} of {@link Role}
   */
  List<Role> getRoles(int fromIndex, int numberOfRoles);
  
  /**
   * Retrieve a limited number of roles (for paginated UI) order by the given criterion
   * @param fromIndex start index
   * @param numberOfRoles maximum number of roles retrieved
   * @param pagingCriterion the criteion used to sort the retried roles
   * @return a {@link List} of {@link Role}
   */
  List<Role> getRoles(int fromIndex, int numberOfRoles, RoleCriterion pagingCriterion);

  /**
   * get the role count
   * @return the number of roles
   */
  int getNumberOfRoles();
  
  /**
   * Retrieve a user's roles
   * @param username the user's username
   * @return a {@link Set} of {@link Role}
   * @throws UserNotFoundException
   * 
   * @deprecated use {@link User#getMemberships()} instead
   */
  @Deprecated
  Set<Role> getUserRoles(String username) throws UserNotFoundException;

  /**
   * Create a new role
   * @param name the role's name
   * @return the {@link Role} created
   * @throws RoleAlreadyExistsException
   */
  Role addRole(String name) throws RoleAlreadyExistsException;
  
  /**
   * Create a new role
   * @param name the role's name
   * @param label the role's label
   * @param description the role's description
   * @return the {@link Role} created
   * @throws RoleAlreadyExistsException
   */
  Role addRole(String name, String label, String description) throws RoleAlreadyExistsException;
  
  /**
   * Import a role
   * @param uuid the role's uuid
   * @param name the role's name
   * @param label the role's label
   * @param description the role's description
   * @return the {@link Role} created
   * @throws RoleAlreadyExistsException
   */
  Role importRole(String uuid, String name, String label, String description) throws RoleAlreadyExistsException;
  
  /**
   * Update a role
   * @param roleUUID the role UUID of the role to update
   * @param name the role's name
   * @param label the role's label
   * @param description the role's description
   * @return the updated {@link Role}
   * @throws RoleNotFoundException
   * @throws RoleAlreadyExistsException
   */
  Role updateRoleByUUID(String roleUUID, String name, String label, String description) throws RoleNotFoundException, RoleAlreadyExistsException;
  
  /**
   * Update a role
   * @param oldName the actual role name of the role to update
   * @param name the role's name
   * @param label the role's label
   * @param description the role's description
   * @return the updated {@link Role}
   * @throws RoleNotFoundException
   * @throws RoleAlreadyExistsException
   * 
   * @deprecated use {@link IdentityAPI#updateRoleByUUID(String, String, String, String)} instead
   */
  @Deprecated
  Role updateRole(String oldName, String name, String label, String description) throws RoleNotFoundException, RoleAlreadyExistsException;
  
  /**
   * Permanently remove a role
   * @param roleUUID the role UUID
   * @throws RoleNotFoundException
   */
  void removeRoleByUUID(String roleUUID) throws RoleNotFoundException;
  
  /**
   * Permanently remove a collection of roles
   * @param roleUUIDs the roles UUIDs
   * @throws RoleNotFoundException
   */
  void removeRoles(Collection<String> roleUUIDs) throws RoleNotFoundException;
  
  /**
   * Permanently remove a role
   * @param name the role's name
   * @throws RoleNotFoundException
   * 
   * @deprecated use {@link IdentityAPI#removeRoleByUUID(String)} instead
   */
  @Deprecated
  void removeRole(String name) throws RoleNotFoundException;
  

  /**
   * Retrieve a Group from it's UUID
   * @param groupUUID the group UUID
   * @return a {@link Group}
   * @throws GroupNotFoundException
   */
  Group getGroupByUUID(String groupUUID) throws GroupNotFoundException;
  
  /**
   * Retrieve all the groups
   * @return a {@link Set} of {@link Group}
   */
  List<Group> getAllGroups();

  /**
   * Retrieve a limited number of groups (for paginated UI)
   * @param fromIndex start index
   * @param numberOfGroups maximum number of groups retrieved
   * @return a {@link List} of {@link Group}
   */
  List<Group> getGroups(int fromIndex, int numberOfGroups);
  
  /**
   * Retrieve a limited number of groups (for paginated UI) order by the given criterion
   * @param fromIndex start index
   * @param numberOfGroups maximum number of groups retrieved
   * @param pagingCriterion the criterion used to sort the retrieved groups
   * @return a {@link List} of {@link Group}
   */
  List<Group> getGroups(int fromIndex, int numberOfGroups, GroupCriterion pagingCriterion);
  
  /**
   * get the group count
   * @return the number of groups
   */
  int getNumberOfGroups();
  
  /**
   * Retrieve the direct children groups of a group
   * @param groupUUID the group UUID
   * @return a {@link List} of {@link Group}
   */
  List<Group> getChildrenGroupsByUUID(String groupUUID);

  /**
   * Retrieve a limited number of groups (for paginated UI)
   * @param groupUUID the group UUID
   * @param fromIndex start index
   * @param numberOfGroups maximum number of groups retrieved
   * @return a {@link List} of {@link Group}
   */
  List<Group> getChildrenGroups(String groupUUID, int fromIndex, int numberOfGroups) throws GroupNotFoundException;
  
  /**
   * Retrieve a limited number of groups (for paginated UI) order by the given criterion
   * @param groupUUID the group UUID
   * @param fromIndex start index
   * @param numberOfGroups maximum number of groups retrieved
   * @param pagingCriterion the criterion used to sort the retrieved groups
   * @return a {@link List} of {@link Group}
   */
  List<Group> getChildrenGroups(String groupUUID, int fromIndex, int numberOfGroups, GroupCriterion pagingCriterion) throws GroupNotFoundException;
  
  /**
   * Get the group count
   * @param groupUUID the group UUID
   * @return the number of groups
   */
  int getNumberOfChildrenGroups(String groupUUID) throws GroupNotFoundException;
  
  /**
   * Create a new group
   * @param name the group's name
   * @param parentGroupUUID the parent group UUID (null if the group should be created at the root)
   * @return the {@link Group} created
   * @throws RoleAlreadyExistsException
   */
  Group addGroup(String name, String parentGroupUUID) throws GroupAlreadyExistsException, GroupNotFoundException;
  
  /**
   * Create a new group
   * @param name the group's name
   * @param label the group's label
   * @param description the group's description
   * @param parentGroupUUID the parent group UUID (null if the group should be created at the root)
   * @return the {@link Group} created
   * @throws GroupAlreadyExistsException
   */
  Group addGroup(String name, String label, String description, String parentGroupUUID) throws GroupAlreadyExistsException, GroupNotFoundException;
  
  /**
   * Check if a group exists
   * @param groupUUID the group UUID
   * @return true if the group exists, false otherwise
   */
  boolean groupExists(String groupUUID);
  
  /**
   * Import a group
   * @param uuid the group's UUID
   * @param name the group's name
   * @param label the group's label
   * @param description the group's description
   * @param parentGroupUUID the parent group UUID (null if the group should be created at the root)
   * @return the {@link Group} created
   * @throws GroupAlreadyExistsException
   */
  Group importGroup(String uuid, String name, String label, String description, String parentGroupUUID) throws GroupAlreadyExistsException, GroupNotFoundException;
  
  /**
   * Update a group
   * @param groupUUID the actual group UUID of the group to update
   * @param name the group's name
   * @param label the group's label
   * @param description the group's description
   * @param parentGroupUUID the parent group UUID
   * @return the updated {@link Group}
   * @throws GroupNotFoundException
   * @throws GroupAlreadyExistsException
   */
  Group updateGroupByUUID(String groupUUID, String name, String label, String description, String parentGroupUUID) throws GroupNotFoundException, GroupAlreadyExistsException;
  
  /**
   * Permanently remove a group (and its children)
   * @param groupUUID the group's UUID
   * @throws GroupNotFoundException
   */
  void removeGroupByUUID(String groupUUID) throws GroupNotFoundException;
  
  /**
   * Permanently remove a collection of groups (and their children)
   * @param groupUUIDs the group's UUIDs
   * @throws GroupNotFoundException
   */
  void removeGroups(Collection<String> groupUUIDs) throws GroupNotFoundException;
  
  
  /**
   * Retrieve a membership from it's UUID
   * @param membershipUUID the membership UUID
   * @return a {@link Membership}
   */
  Membership getMembershipByUUID(String membershipUUID) throws MembershipNotFoundException;
  
  /**
   * Obtain the {@link Membership} for a role and a group (either it's an existing membership or it's created if the role and group exist)
   * @param roleUUID the role UUID
   * @param groupUUID the group UUID
   * @return the {@link Membership} for the role and the group
   * @throws RoleNotFoundException
   * @throws GroupNotFoundException
   */
  Membership getMembershipForRoleAndGroup(String roleUUID, String groupUUID) throws RoleNotFoundException, GroupNotFoundException;
  
  /**
   * Add a membership to a user
   * @param userUUID the user UUID
   * @param membershipUUID the membership UUID
   * @throws UserNotFoundException
   * @throws MembershipNotFoundException
   */
  void addMembershipToUser(String userUUID, String membershipUUID) throws UserNotFoundException, MembershipNotFoundException;
  
  /**
   * Add a collection of memberships to a user
   * @param userUUID the user UUID
   * @param membershipUUIDs the collection of membership UUID
   * @throws UserNotFoundException
   * @throws MembershipNotFoundException
   */
  void addMembershipsToUser(String userUUID, Collection<String> membershipUUIDs) throws UserNotFoundException, MembershipNotFoundException;
  
  /**
   * Remove a membership from a user
   * @param userUUID the user UUID
   * @param membershipUUID the membership UUID
   * @throws UserNotFoundException
   * @throws MembershipNotFoundException
   */
  void removeMembershipFromUser(String userUUID, String membershipUUID) throws UserNotFoundException, MembershipNotFoundException;
  
  /**
   * Remove a collection of memberships from a user
   * @param userUUID the user UUID
   * @param membershipUUIDs the collection of membership UUID
   * @throws UserNotFoundException
   * @throws MembershipNotFoundException
   */
  void removeMembershipsFromUser(String userUUID, Collection<String> membershipUUIDs) throws UserNotFoundException, MembershipNotFoundException;
  
  /**
   * Set the memberships of a user
   * @param userUUID the user UUID
   * @param membershipUUIDs the collection of membership UUID
   * @throws UserNotFoundException
   * @throws MembershipNotFoundException
   */
  void setUserMemberships(String userUUID, Collection<String> membershipUUIDs) throws UserNotFoundException, MembershipNotFoundException;
  
  /**
   * Add a role to a user
   * @param roleName the role's name
   * @param username the user's username
   * @throws UserNotFoundException
   * @throws RoleNotFoundException
   * 
   * @deprecated use {@link IdentityAPI#addMembershipToUser(String, String)} instead with a membership obtained with {@link IdentityAPI#getMembershipForRoleAndGroup(String, String)} and the default group {@link IdentityAPI#DEFAULT_GROUP_NAME}
   */
  @Deprecated
  void addRoleToUser(String roleName, String username) throws UserNotFoundException, RoleNotFoundException;
  
  /**
   * @param username the user's username
   * @param roleNames the user's roles to set
   * @throws UserNotFoundException
   * @throws RoleNotFoundException
   * 
   * @deprecated use {@link IdentityAPI#setUserMemberships(String, Collection)} instead with memberships obtained with {@link IdentityAPI#getMembershipForRoleAndGroup(String, String)} and the default group {@link IdentityAPI#DEFAULT_GROUP_NAME}
   */
  @Deprecated
  void setUserRoles(String username, Set<String> roleNames) throws UserNotFoundException, RoleNotFoundException;
  
  /**
   * Remove role from a user
   * @param roleName the role's name
   * @param username the user's username
   * @throws UserNotFoundException
   * @throws RoleNotFoundException
   * 
   * @deprecated use {@link org.ow2.bonita.facade.IdentityAPI.removeMembershipFromUser} instead with a membership obtained with {@link IdentityAPI#getMembershipForRoleAndGroup(String, String)} and the default group {@link IdentityAPI#DEFAULT_GROUP_NAME}
   */
  @Deprecated
  void removeRoleFromUser(String roleName, String username) throws UserNotFoundException, RoleNotFoundException;
  
  /**
   * Retrieve a list of users
   * @param userUUIDs the UUIDs of the required users
   * @return a {@link List} of {@link User}
   * @throws UserNotFoundException
   */
  List<User> getUsersByUUIDs(Collection<String> userUUIDs) throws UserNotFoundException;
  
  /**
   * Retrieve a list of roles
   * @param roleUUIDs the UUIDs of the required roles
   * @return a {@link List} of {@link Role}
   * @throws RoleNotFoundException
   */
  List<Role> getRolesByUUIDs(Collection<String> roleUUIDs) throws RoleNotFoundException;
  
  /**
   * Retrieve a list of groups
   * @param groupUUIDs the UUIDs of the required groups
   * @return a {@link List} of {@link Group}
   * @throws GroupNotFoundException
   */
  List<Group> getGroupsByUUIDs(Collection<String> groupUUIDs) throws GroupNotFoundException;

  /**
   * Retrieve a list of memberships
   * @param membershipUUIDs the UUIDs of the required memberships
   * @return a {@link List} of {@link Membership}
   * @throws MembershipNotFoundException
   */
  List<Membership> getMembershipsByUUIDs(Collection<String> membershipUUIDs) throws MembershipNotFoundException;

  /**
   * Retrieves the group according to the group path. 
   * @param path the group path (the first element is the root groupName, the second element is the childName of the root group, ...)
   * @return the group corresponding to the group path; null otherwise
   */
  Group getGroupUsingPath(final List<String> path);

}
