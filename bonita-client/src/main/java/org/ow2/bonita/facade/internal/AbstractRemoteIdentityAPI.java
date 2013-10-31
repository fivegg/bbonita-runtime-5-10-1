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
package org.ow2.bonita.facade.internal;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

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
import org.ow2.bonita.facade.paging.GroupCriterion;
import org.ow2.bonita.facade.paging.RoleCriterion;
import org.ow2.bonita.facade.paging.UserCriterion;

/**
 * 
 * @author Elias Ricken de Medeiros, Matthieu Chaffotte
 *
 */
@Path("/API/identityAPI/")
@Produces({"text/*","application/xml"})
@Consumes({"application/x-www-form-urlencoded","text/*", "application/xml"})
public interface AbstractRemoteIdentityAPI extends Remote {
	
	/**
	 * Retrieve a ProfileMetadata from it's UUID
   * @param metadataUUID the profile metadata UUID
   * @param options the options map (domain, queryList, user)
   * @return a ProfileMetadata
	 */
	@POST	@Path("getProfileMetadataByUUID/{metadataUUID}")
  ProfileMetadata getProfileMetadataByUUID(
  		@PathParam("metadataUUID")String metadataUUID, 
  		@FormParam("options") final Map<String, String> options) 
	throws RemoteException, MetadataNotFoundException;
  
	/**
	 * Retrieve a ProfileMetadata from it's name
   * @param metadataName the profile metadata name
   * @param options the options map (domain, queryList, user)
   * @return a ProfileMetadata
	 */
	@POST	@Path("findProfileMetadataByName/{metadataName}")
  ProfileMetadata findProfileMetadataByName(
  		@PathParam("metadataName") String metadataName, 
  		@FormParam("options") final Map<String, String> options) 
	throws RemoteException, MetadataNotFoundException;
  
	/**
	 * Retrieve all the metadata
	 * @param options the options map (domain, queryList, user)
	 * @return a List of ProfileMetadata 
	 */
	@POST	@Path("getAllProfileMetadata")
  List<ProfileMetadata> getAllProfileMetadata(
  		@FormParam("options") final Map<String, String> options)
  throws RemoteException;
  
	/**
	 * Retrieve a limited number of ProfileMetadata (for paginated UI)
	 * @param fromIndex start index
	 * @param numberOfMetadata maximum number of metadata retrieved
	 * @param options the options map (domain, queryList, user) 
	 * @return a {@link List} of {@link ProfileMetadata}
	 * @throws RemoteException
	 */
	@POST	@Path("getProfileMetadata")
  List<ProfileMetadata> getProfileMetadata(
      @QueryParam("fromIndex") int fromIndex,
      @QueryParam("numberOfMetadata") int numberOfMetadata, 
  		@FormParam("options") final Map<String, String> options)
  throws RemoteException;

	/**
	 * Get the profile metadata count
	 * @param options the options map (domain, queryList, user)
	 * @return the number of profile metadata defined
	 * @throws RemoteException
	 */
	@POST	@Path("getNumberOfProfileMetadata")
  int getNumberOfProfileMetadata(
      @FormParam("options") final Map<String, String> options)
	throws RemoteException;
  
	/**
	 * Create a new profile metadata
	 * @param name the name of the metadata
	 * @param options the options map (domain, queryList, user)
	 * @return the ProfileMetadata created
	 */
	@POST	@Path("addProfileMetadata/{name}")
  ProfileMetadata addProfileMetadata(
      @PathParam("name") String name,
      @FormParam("options") final Map<String, String> options) 
	throws RemoteException, MetadataAlreadyExistsException;
  
	/**
	 * Create a new profile metadata
	 * @param name the name of the metadata
   * @param label the label of the metadata
	 * @param options the options map (domain, queryList, user)
	 * @return the {@link ProfileMetadata} created
	 */
	@POST	@Path("addProfileMetadata/{name}/{label}")
  ProfileMetadata addProfileMetadata(
      @PathParam("name") String name,
      @PathParam("label") String label,
      @FormParam("options") final Map<String, String> options) 
	throws RemoteException, MetadataAlreadyExistsException;

	/**
	 * Update a profile metadata
	 * @param profileMetadataUUID the UUID of the profile metadata to update
   * @param name the new name of the metadata
   * @param label the new label of the metadata
	 * @param options the options map (domain, queryList, user)
	 * @return the updated {@link ProfileMetadata}
	 */
	@POST	@Path("updateProfileMetadataByUUID/{profileMetadataUUID}/{name}/{label}")
  ProfileMetadata updateProfileMetadataByUUID(
  		@PathParam("profileMetadataUUID") String profileMetadataUUID, 
  		@PathParam("name") String name, 
  		@PathParam("label") String label, 
  		@FormParam("options") final Map<String, String> options) 
	throws RemoteException, MetadataNotFoundException, MetadataAlreadyExistsException;
  
	/**
	 * Permanently remove a profile metadata
	 * @param profileMetadataUUID the profile metadata to remove
	 * @param options the options map (domain, queryList, user)
	 */
	@POST	@Path("removeProfileMetadataByUUID/{profileMetadataUUID}")
  void removeProfileMetadataByUUID(
  		@PathParam("profileMetadataUUID") String profileMetadataUUID, 
  		@FormParam("options") final Map<String, String> options) 
	throws RemoteException, MetadataNotFoundException;
  
	/**
	 * Retrieve a user from its username
	 * @param username the user username
	 * @param options the options map (domain, queryList, user)
	 * @return a {@link User}
	 */
	@POST	@Path("getUser/{username}")
  @Deprecated
  User getUser(
  		@PathParam("username") String username, 
  		@FormParam("options") final Map<String, String> options)
	throws RemoteException, UserNotFoundException;
  
	/**
	 * Retrieve a user from its username
	 * @param username username the user username
	 * @param options the options map (domain, queryList, user)
	 * @return a {@link User}
	 * @throws RemoteException
	 * @throws UserNotFoundException
	 */
	@POST	@Path("findUserByUserName/{username}")
  User findUserByUserName(
  		@PathParam("username") String username, 
  		@FormParam("options") final Map<String, String> options) 
	throws RemoteException, UserNotFoundException;
  
	/**
	 * Retrieve a user from its UUID
	 * @param userUUID the user UIID
	 * @param options the options map (domain, queryList, user)
	 * @return a {@link User}
	 * @throws RemoteException
	 * @throws UserNotFoundException
	 */
	@POST	@Path("getUserByUUID/{userUUID}")
  User getUserByUUID(
  		@PathParam("userUUID") String userUUID, 
  		@FormParam("options") final Map<String, String> options) 
	throws RemoteException, UserNotFoundException;
  
	/**
	 * Retrieve all the users
	 * @param options the options map (domain, queryList, user)
	 * @return a {@link Set} of {@link User}
	 * @throws RemoteException
	 */
	@POST	@Path("getUsers")
  @Deprecated
  Set<User> getUsers(
      @FormParam("options") final Map<String, String> options)
  throws RemoteException;
  
	/**
	 * Retrieve all the users
	 * @param options the options map (domain, queryList, user)
	 * @return a {@link List} of {@link User}
	 * @throws RemoteException
	 */
	@POST	@Path("getAllUsers")
  List<User> getAllUsers(
      @FormParam("options") final Map<String, String> options)
  throws RemoteException;
  
	/**
	 * Retrieve a limited number of users (for paginated UI)
	 * @param fromIndex start index
	 * @param numberOfUsers maximum number of users retrieved
	 * @param options the options map (domain, queryList, user)
	 * @return numberOfUsers maximum number of users retrieved
	 * @throws RemoteException
	 */
	@POST	@Path("getUsersByIndexAndNumberOfUsers")
  List<User> getUsers(
      @QueryParam("fromIndex") int fromIndex,
      @QueryParam("numberOfUsers") int numberOfUsers, 
  		@FormParam("options") final Map<String, String> options)
  throws RemoteException;
	
	/**
   * Retrieve a limited number of users (for paginated UI) order by the given pagingCriterion
   * @param fromIndex start index
   * @param numberOfUsers maximum number of users retrieved
   * @param pagingCriterion the criterion used to sort the retried users
   * @param options the options map (domain, queryList, user)
   * @return a {@link List} of {@link User}
   */
	@POST	@Path("getUsersByIndexAndNumberOfUsersWithPagingCriterion")
  List<User> getUsers(
  		@QueryParam("fromIndex") int fromIndex, 
  		@QueryParam("numberOfUsers") int numberOfUsers, 
  		@QueryParam("pagingCriterion") UserCriterion pagingCriterion,
  		@FormParam("options") final Map<String, String> options)
  throws RemoteException;
  
	/**
	 * get the user count
	 * @param options the options map (domain, queryList, user)
	 * @return the numbers of users
	 * @throws RemoteException
	 */
	@POST	@Path("getNumberOfUsers")
  int getNumberOfUsers(
      @FormParam("options") final Map<String, String> options)
	throws RemoteException;

	/**
	 * Create a new a user
	 * @param username the user's username
   * @param password the user's password
	 * @param options the options map (domain, queryList, user)
	 * @return the {@link User} created
	 * @throws RemoteException
	 * @throws UserAlreadyExistsException
	 */
	@POST @Path("addUser")
  User addUser(
      @FormParam("username") String username,
      @FormParam("password") String password,
      @FormParam("options") final Map<String, String> options) 
	throws RemoteException, UserAlreadyExistsException;

	/**
	 * Create a new a user
   * @param username the user's username
   * @param password the user's password
   * @param firstName the user's first name
   * @param lastName the user's last name
   * @param email the user's email
	 * @param options the options map (domain, queryList, user)
	 * @return
	 * @throws RemoteException
	 * @throws UserAlreadyExistsException
	 */
	@POST	@Path("addUserWithEmail")
  @Deprecated
  User addUser(
      @FormParam("username") String username,
      @FormParam("password") String password,
      @FormParam("firstName") String firstName, 
  		@FormParam("lastName") String lastName,
  		@FormParam("email") String email,
  		@FormParam("options") final Map<String, String> options) 
	throws RemoteException, UserAlreadyExistsException;
  
	/**
	 * Create a new user
	 * @param username the user's username
   * @param password the user's password
   * @param firstName the user's first name
   * @param lastName the user's last name
   * @param title the user's title
   * @param jobTitle the user's job title
   * @param managerUserUUID the user's manager's UUID
   * @param profileMetadata the user's profile metadata where the key of the map should be the name of one of the defined profile metadata
	 * @param options the options map (domain, queryList, user)
	 * @return the {@link User} created
	 * @throws RemoteException
	 * @throws UserAlreadyExistsException
	 * @throws UserNotFoundException
	 * @throws MetadataNotFoundException
	 */
	@POST	@Path("addUserFull")
  User addUser(
      @FormParam("username") String username,
      @FormParam("password") String password,
      @FormParam("firstName") String firstName, 
  		@FormParam("lastName") String lastName,
  		@FormParam("title") String title,
  		@FormParam("jobTitle") String jobTitle, 
  		@FormParam("managerUserUUID") String managerUserUUID,
  		@FormParam("profileMetadata") Map<String, String> profileMetadata, 
  		@FormParam("options") final Map<String, String> options)
	throws RemoteException, UserAlreadyExistsException, UserNotFoundException, MetadataNotFoundException;

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
	 * @param options the options map (domain, queryList, user)
	 * @return the {@link User} created
	 * @throws RemoteException
	 * @throws UserAlreadyExistsException
	 * @throws MetadataNotFoundException
	 */
	@POST	@Path("importUser")
  User importUser(
      @FormParam("userUUID") String userUUID,
      @FormParam("username") String username,
      @FormParam("password") String passwordHash,
  		@FormParam("firstName") String firstName,
  		@FormParam("lastName") String lastName,
  		@FormParam("title") String title, 
  		@FormParam("jobTitle") String jobTitle,
  		@FormParam("managerUserUUID") String managerUserUUID, 
  		@FormParam("profileMetadata") Map<String, String> profileMetadata, 
  		@FormParam("options") final Map<String, String> options)
	throws RemoteException, UserAlreadyExistsException, MetadataNotFoundException;

	/**
	Set the manager of a user
   * @param userUUID the user UUID
   * @param delegateUserUUID the delegate user UUID (null to remove the delegate)
	 * @param options the options map (domain, queryList, user)
	 * @throws RemoteException
	 * @throws UserNotFoundException
	 */
	@POST	@Path("updateUserDelegate/{userUUID}/{delegateUserUUID}")
  void updateUserDelegee(
      @PathParam("userUUID") String userUUID,
      @PathParam("delegateUserUUID") String delegateUserUUID, 
  		@FormParam("options") final Map<String, String> options)
	throws RemoteException, UserNotFoundException;

	/**
	 * update a user's personal contact info
	 * @param userUUID
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
	 * @param options the options map (domain, queryList, user)
	 * @throws RemoteException
	 * @throws UserNotFoundException
	 */
	@POST	@Path("updateUserPersonalContactInfo")
  void updateUserPersonalContactInfo(
      @FormParam("userUUID") String userUUID,
      @FormParam("email")String email,
      @FormParam("phoneNumber")String phoneNumber, 
  		@FormParam("mobileNumber")String mobileNumber,
  		@FormParam("faxNumber")String faxNumber,
  		@FormParam("building")String building, 
  		@FormParam("room")String room,
  		@FormParam("address")String address,
  		@FormParam("zipCode")String zipCode,
  		@FormParam("city")String city, 
  		@FormParam("state")String state,
  		@FormParam("country")String country,
  		@FormParam("website")String website, 
  		@FormParam("options") final Map<String, String> options)
	throws RemoteException, UserNotFoundException;

	/**
	 * update a user's personal contact info
	 * @param userUUID
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
	 * @param options the options map (domain, queryList, user)
	 * @throws RemoteException
	 * @throws UserNotFoundException
	 */
	@POST	@Path("updateUserProfessionalContactInfo")
  void updateUserProfessionalContactInfo(
      @FormParam("userUUID") String userUUID,
      @FormParam("email")String email,
      @FormParam("phoneNumber")String phoneNumber, 
  		@FormParam("mobileNumber")String mobileNumber,
  		@FormParam("faxNumber")String faxNumber,
  		@FormParam("building")String building, 
  		@FormParam("room")String room,
  		@FormParam("address")String address,
  		@FormParam("zipCode")String zipCode,
  		@FormParam("city")String city, 
  		@FormParam("state")String state,
  		@FormParam("country")String country,
  		@FormParam("website")String website, 
  		@FormParam("options") final Map<String, String> options)
	throws RemoteException, UserNotFoundException;

	/**
	 * Update a user
   * @param oldUsername the actual username of the user to update
   * @param username the user's username
   * @param password the user's password
   * @param firstName the user's first name
   * @param lastName the user's last name
   * @param email the user's email
	 * @param options the options map (domain, queryList, user)
	 * @return the updated {@link User}
	 * @throws RemoteException
	 * @throws UserNotFoundException
	 * @throws UserAlreadyExistsException
	 */
	@POST	@Path("updateUser")
  @Deprecated
  User updateUser(
      @FormParam("oldUsername") String oldUsername,
      @FormParam("username") String username,
      @FormParam("password") String password, 
  		@FormParam("firstName")String firstName,
  		@FormParam("lastName")String lastName,
  		@FormParam("email")String email, 
  		@FormParam("options") final Map<String, String> options)
	throws RemoteException, UserNotFoundException, UserAlreadyExistsException;
  
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
	 * @param profileMetadata
	 * @param options the options map (domain, queryList, user)
	 * @return the updated {@link User}
	 * @throws RemoteException
	 * @throws UserNotFoundException
	 * @throws UserAlreadyExistsException
	 * @throws MetadataNotFoundException
	 */
	@POST	@Path("updateUserByUUID")
  User updateUserByUUID(
      @FormParam("userUUID") String userUUID,
      @FormParam("username") String username,
      @FormParam("firstName")String firstName, 
  		@FormParam("lastName")String lastName,
  		@FormParam("title")String title,
  		@FormParam("jobTitle")String jobTitle, 
  		@FormParam("managerUserUUID")String managerUserUUID,
  		@FormParam("profileMetadata")Map<String, String> profileMetadata, 
  		@FormParam("options") final Map<String, String> options) 
	throws RemoteException, UserNotFoundException, UserAlreadyExistsException, MetadataNotFoundException;
  
	/**
	 * Update the user's password.
   * @param userUUID the user UUID
   * @param password the user new password
   * @param options the options map (domain, queryList, user)
	 * @return the updated user
	 * @throws RemoteException
	 * @throws UserNotFoundException
	 */
	@POST	@Path("updateUserPassword")
  User updateUserPassword(
      @FormParam("userUUID") String userUUID,
      @FormParam("password") String password, 
  		@FormParam("options") final Map<String, String> options)
	throws RemoteException, UserNotFoundException;  
  
	/**
	 * Retrieve all the users having a given role
   * @param roleName the role name
	 * @return a {@link Set} of {@link User}
	 * @throws RemoteException
	 * @throws RoleNotFoundException
	 */
	@POST	@Path("getUsersInRole/{roleName}")
  @Deprecated
  Set<User> getUsersInRole(
      @PathParam("roleName") String roleName,
      @FormParam("options") final Map<String, String> options) 
  throws RemoteException, RoleNotFoundException;
  
	/**
   * Retrieve all the users in a role
   * @param roleUUID the role UUID
	 * @param options the options map (domain, queryList, user)
	 * @return a {@link List} of {@link User}
	 * @throws RemoteException
	 * @throws RoleNotFoundException
	 */
	@POST	@Path("getAllUsersInRole/{roleUUID}")
  List<User>getAllUsersInRole(
      @PathParam("roleUUID") String roleUUID,
      @FormParam("options") final Map<String, String> options)
  throws RemoteException, RoleNotFoundException;
  
	/**
	 * Retrieve a limited number of the users in a role (for paginated UI)
   * @param roleUUID the role UUID
   * @param fromIndex start index
   * @param numberOfUsers maximum number of users retrieved
	 * @param options the options map (domain, queryList, user)
	 * @return a {@link List} of {@link User}
	 * @throws RemoteException
	 * @throws RoleNotFoundException
	 */
	@POST	@Path("getUsersInRoleByIndexAndnumberOfUsers/{roleUUID}")
  List<User> getUsersInRole(
      @PathParam("roleUUID") String roleUUID,
      @QueryParam("fromIndex") int fromIndex,
      @QueryParam("numberOfUsers") int numberOfUsers, 
  		@FormParam("options") final Map<String, String> options)
  throws RemoteException, RoleNotFoundException;
	
	/**
   * Retrieve a limited number of the users in a role (for paginated UI) order by the given pagingCriterion 
   * @param roleUUID the role UUID
   * @param fromIndex start index
   * @param numberOfUsers maximum number of users retrieved
   * @param pagingCriterion the criterion used to sort the retrieved users
   * @param options the options map (domain, queryList, user)
   * @return a {@link List} of {@link User}
   */
	@POST  @Path("getUsersInRoleByIndexAndnumberOfUsersWithPagingCriterion/{roleUUID}")
  List<User> getUsersInRole(
      @PathParam("roleUUID") String roleUUID, 
      @QueryParam("fromIndex") int fromIndex, 
      @QueryParam("numberOfUsers") int numberOfUsers, 
      @QueryParam("pagingCriterion") UserCriterion pagingCriterion, 
      @FormParam("options") final Map<String, String> options) 
  throws RoleNotFoundException, RemoteException;
  
	/**
	 * Retrieve the number of users inside a group
   * @param roleUUID the role UUID
	 * @param options the options map (domain, queryList, user)
	 * @return the number of users inside the the number of users inside the group
	 * @throws RemoteException
	 */
	@POST	@Path("getNumberOfUsersInRole/{roleUUID}")
  int getNumberOfUsersInRole(
      @PathParam("roleUUID") String roleUUID,
      @FormParam("options") final Map<String, String> options)
	throws RemoteException;

	/**
	 * Retrieve all the users of a group
	 * @param groupUUID
	 * @param options the options map (domain, queryList, user)
	 * @return a {@link List} of {@link User}
	 * @throws RemoteException
	 * @throws GroupNotFoundException
	 */
	@POST	@Path("getAllUsersInGroup/{groupUUID}")
  List<User>getAllUsersInGroup(
      @PathParam("groupUUID") String groupUUID,
      @FormParam("options") final Map<String, String> options) 
  throws RemoteException, GroupNotFoundException;
  
	/**
	 * Retrieve a limited number of a group's users (for paginated UI)
   * @param groupUUID the group UUID
   * @param fromIndex start index
   * @param numberOfUsers maximum number of users retrieved from the group
	 * @param options the options map (domain, queryList, user)
	 * @return a {@link List} of {@link User}
	 * @throws RemoteException
	 * @throws GroupNotFoundException
	 */
	@POST	@Path("getUsersInGroup/{groupUUID}")
  List<User> getUsersInGroup(
      @PathParam("groupUUID") String groupUUID,
      @QueryParam("fromIndex") int fromIndex, 
  		@QueryParam("numberOfUsers") int numberOfUsers,
  		@FormParam("options") final Map<String, String> options)
  throws RemoteException, GroupNotFoundException;
	
	/**
   * Retrieve a limited number of a group's users (for paginated UI) oder by the given criterion
   * @param groupUUID the group UUID
   * @param fromIndex start index
   * @param numberOfUsers maximum number of users retrieved from the group
   * @param pagingCriterion the criterion used to sort the retrieved users
   * @return a {@link List} of {@link User}
   */
	@POST  @Path("getUsersInGroupWithPagingCriterion/{groupUUID}")
  List<User> getUsersInGroup(
      @PathParam("groupUUID") String groupUUID, 
      @QueryParam("fromIndex") int fromIndex, 
      @QueryParam("numberOfUsers") int numberOfUsers, 
      @QueryParam("pagingCriterion") UserCriterion pagingCriterion,
      @FormParam("options") final Map<String, String> options) 
  throws GroupNotFoundException, RemoteException;
  
	/**
	 * Retrieve the number of users inside a group
   * @param groupUUID the group UUID
	 * @param options the options map (domain, queryList, user)
	 * @return the number of users inside the group
	 * @throws RemoteException
	 */
	@POST	@Path("getNumberOfUsersInGroup/{groupUUID}")
  int getNumberOfUsersInGroup(
      @PathParam("groupUUID") String groupUUID,
      @FormParam("options") final Map<String, String> options)
	throws RemoteException;
  
	/**
	 * Retrieve all the users in the membership
   * @param MembershipUUID the membership UUID
	 * @param options the options map (domain, queryList, user)
	 * @return @return a {@link Set} of users
	 * @throws RemoteException
	 * @throws MembershipNotFoundException
	 */
	@POST	@Path("getAllUsersInMembership/{membershipUUID}")
  List<User> getAllUsersInMembership(
      @PathParam("membershipUUID") String membershipUUID,
      @FormParam("options") final Map<String, String> options) 
  throws RemoteException, MembershipNotFoundException;
  
	/**
	* Retrieve all the users in a given role inside a group. This method return the same result as getAllUsersInMembership except you don't need to know the membership UUID for the role-group association
   * @param roleUUID the role UUID
   * @param groupUUID the group UUID
	 * @param options the options map (domain, queryList, user)
	 * @return a {@link Set} of users
	 * @throws RemoteException
	 * @throws RoleNotFoundException
	 * @throws GroupNotFoundException
	 */
	@POST	@Path("getAllUsersInRoleAndGroup/{roleUUID}/{groupUUID}")
  List<User> getAllUsersInRoleAndGroup(
      @PathParam("roleUUID") String roleUUID,
      @PathParam("groupUUID") String groupUUID, 
  		@FormParam("options") final Map<String, String> options)
  throws RemoteException, RoleNotFoundException, GroupNotFoundException;
  
	/**
	 * Retrieve all the users having the same manager
   * @param managerUUID the user UUID of the manager
	 * @param options the options map (domain, queryList, user)
	 * @return a {@link Set} of users
	 * @throws RemoteException
	 * @throws UserNotFoundException
	 */
	@POST	@Path("getUsersByManagerUUID/{managerUUID}")
  List<User> getUsersByManagerUUID(
      @PathParam("managerUUID") String managerUUID,
      @FormParam("options") final Map<String, String> options)
  throws RemoteException, UserNotFoundException;
  
	/**
	 * Permanently remove a user
   * @param userUUID the user UUID
	 * @param options the options map (domain, queryList, user)
	 * @throws RemoteException
	 * @throws UserNotFoundException
	 */
	@POST	@Path("removeUserByUUID/{userUUID}")
  void removeUserByUUID(
      @PathParam("userUUID") String userUUID,
      @FormParam("options") final Map<String, String> options)
	throws RemoteException, UserNotFoundException;
  
	/**
	 * Permanently remove a user
   * @param username the user's username
	 * @param options the options map (domain, queryList, user)
	 * @throws RemoteException
	 * @throws UserNotFoundException
	 */
	@POST	@Path("removeUser/{username}")
  @Deprecated
  void removeUser(
      @PathParam("username") String username,
      @FormParam("options") final Map<String, String> options)
	throws RemoteException, UserNotFoundException;
  
	/**
	 * Retrieve a role from its name
   * @param name the role's name
	 * @param options the options map (domain, queryList, user)
	 * @return a {@link Role}
	 * @throws RemoteException
	 * @throws RoleNotFoundException
	 */
	@POST	@Path("getRole/{name}")
  @Deprecated
  Role getRole(
      @PathParam("name") String name,
      @FormParam("options") final Map<String, String> options)
	throws RemoteException, RoleNotFoundException;
  
	/**
	 * Retrieve a role from its name
   * @param name the role's name
	 * @param options the options map (domain, queryList, user)
	 * @return a {@link Role}
	 * @throws RemoteException
	 * @throws RoleNotFoundException
	 */
	@POST	@Path("findRoleByName/{name}")
  Role findRoleByName(
      @PathParam("name") String name,
      @FormParam("options") final Map<String, String> options)
	throws RemoteException, RoleNotFoundException;
  
	/**
	 * Retrieve a role from its UUID
   * @param roleUUID the role's UUID
	 * @param options the options map (domain, queryList, user)
	 * @return a {@link Role}
	 * @throws RemoteException
	 * @throws RoleNotFoundException
	 */
	@POST	@Path("getRoleByUUID/{roleUUID}")
  Role getRoleByUUID(
      @PathParam("roleUUID") String roleUUID,
      @FormParam("options") final Map<String, String> options)
	throws RemoteException, RoleNotFoundException;
  
	/**
	 * a {@link Role}
	 * @param options the options map (domain, queryList, user)
	 * @return a {@link Set} of {@link Role}
	 * @throws RemoteException
	 */
	@POST	@Path("getRoles")
  @Deprecated
  Set<Role> getRoles(
      @FormParam("options") final Map<String, String> options)
  throws RemoteException;

	/**
	 * Retrieve all the roles
	 * @param options the options map (domain, queryList, user)
	 * @return a {@link List} of {@link Role}
	 * @throws RemoteException
	 */
	@POST	@Path("getAllRoles")
  List<Role> getAllRoles(
      @FormParam("options") final Map<String, String> options)
  throws RemoteException;

	/**
	 * Retrieve a limited number of roles (for paginated UI)
   * @param fromIndex start index
   * @param numberOfRoles maximum number of roles retrieved
	 * @param options the options map (domain, queryList, user)
	 * @return
	 * @throws RemoteException
	 */
	@POST	@Path("getRolesByIndexAndNumberOfUsers")
  List<Role> getRoles(
      @QueryParam("fromIndex") int fromIndex,
      @QueryParam("numberOfRoles") int numberOfRoles, 
  		@FormParam("options") final Map<String, String> options)
  throws RemoteException;
	
	/**
   * Retrieve a limited number of roles (for paginated UI) order by the given criterion
   * @param fromIndex start index
   * @param numberOfRoles maximum number of roles retrieved
   * @param pagingCriterion the criteion used to sort the retried roles
   * @param options the options map (domain, queryList, user)
   * @return a {@link List} of {@link Role}
   */
	@POST  @Path("getRolesByIndexAndNumberOfUsersWithPagingCriterion")
  List<Role> getRoles(
      @QueryParam("fromIndex") int fromIndex, 
      @QueryParam("numberOfRoles") int numberOfRoles, 
      @QueryParam("pagingCriterion") RoleCriterion pagingCriterion, 
      @FormParam("options") final Map<String, String> options) 
  throws RemoteException;

	/**
	 * get the role count
	 * @param options the options map (domain, queryList, user)
	 * @return the number of roles
	 * @throws RemoteException
	 */
	@POST	@Path("getNumberOfRoles")
  int getNumberOfRoles(
      @FormParam("options") final Map<String, String> options)
	throws RemoteException;

	/**
	 * Retrieve a user's roles
   * @param username the user name
	 * @param options the options map (domain, queryList, user)
	 * @return
	 * @throws RemoteException
	 * @throws UserNotFoundException
	 */
	@POST	@Path("getUserRoles/{username}")
  @Deprecated
  Set<Role> getUserRoles(
      @PathParam("username") String username,
      @FormParam("options") final Map<String, String> options)
  throws RemoteException, UserNotFoundException;

	/**
	  * Create a new role
   * @param name the role's name
	 * @param options the options map (domain, queryList, user)
	 * @return
	 * @throws RemoteException
	 * @throws RoleAlreadyExistsException
	 */
	@POST	@Path("addRole/{name}")
  Role addRole(
      @PathParam("name") String name,
      @FormParam("options") final Map<String, String> options)
	throws RemoteException, RoleAlreadyExistsException;
  
	/**
	 * Create a new role
   * @param name the role's name
   * @param label the role's label
   * @param description the role's description
	 * @param options the options map (domain, queryList, user)
	 * @return
	 * @throws RemoteException
	 * @throws RoleAlreadyExistsException
	 */
	@POST	@Path("addRole/{name}/{label}")
  Role addRole(
      @PathParam("name") String name,
      @PathParam("label") String label,
      @FormParam("description")String description, 
  		@FormParam("options") final Map<String, String> options)
	throws RemoteException, RoleAlreadyExistsException;

	/**
	 * Import a role
   * @param uuid the role's uuid
   * @param name the role's name
   * @param label the role's label
   * @param description the role's description
	 * @param options the options map (domain, queryList, user)
	 * @return the {@link Role} created
	 * @throws RemoteException
	 * @throws RoleAlreadyExistsException
	 */
	@POST	@Path("importRole/{uuid}/{name}")
  Role importRole(
      @PathParam("uuid") String uuid,
      @PathParam("name") String name,
      @QueryParam("label") String label,
      @FormParam("description")String description, 
  		@FormParam("options") final Map<String, String> options)
	throws RemoteException, RoleAlreadyExistsException;
  
	/**
	 * Update a role
   * @param roleUUID the role UUID of the role to update
   * @param name the role's name
   * @param label the role's label
   * @param description the role's description
	 * @param options the options map (domain, queryList, user)
	 * @return
	 * @throws RemoteException
	 * @throws RoleNotFoundException
	 * @throws RoleAlreadyExistsException
	 */
	@POST	@Path("updateRoleByUUID/{roleUUID}/{name}")
  Role updateRoleByUUID(
      @PathParam("roleUUID") String roleUUID,
      @PathParam("name") String name,
      @QueryParam("label") String label, 
  		@FormParam("description")String description,
  		@FormParam("options") final Map<String, String> options) 
	throws RemoteException, RoleNotFoundException, RoleAlreadyExistsException;
  
	/**
	 * Update a role
   * @param oldName the actual role name of the role to update
   * @param name the role's name
   * @param label the role's label
   * @param description the role's description
	 * @param options the options map (domain, queryList, user)
	 * @return
	 * @throws RemoteException
	 * @throws RoleNotFoundException
	 * @throws RoleAlreadyExistsException
	 */
	@POST	@Path("updateRole")
  @Deprecated
  Role updateRole(
      @FormParam("oldName")String oldName,
      @FormParam("name") String name,
      @FormParam("label") String label, 
  		@FormParam("description")String description,
  		@FormParam("options") final Map<String, String> options) 
	throws RemoteException, RoleNotFoundException, RoleAlreadyExistsException;

	/**
	 * Permanently remove a role
   * @param roleUUID the role UUID
	 * @param options the options map (domain, queryList, user)
	 * @throws RemoteException
	 * @throws RoleNotFoundException
	 */
	@POST	@Path("removeRoleByUUID/{roleUUID}")
  void removeRoleByUUID(
      @PathParam("roleUUID") String roleUUID,
      @FormParam("options") final Map<String, String> options) 
	throws RemoteException, RoleNotFoundException;

	/**
	 * Permanently remove a role
   * @param name the role's name
	 * @param options the options map (domain, queryList, user)
	 * @throws RemoteException
	 * @throws RoleNotFoundException
	 */
	@POST	@Path("removeRole/{name}")
  @Deprecated
  void removeRole(
      @PathParam("name") String name,
      @FormParam("options") final Map<String, String> options)
	throws RemoteException, RoleNotFoundException;
  
	/**
	 * Retrieve a Group from it's UUID
   * @param groupUUID the group UUID
	 * @param options the options map (domain, queryList, user)
	 * @return a {@link Group}
	 * @throws RemoteException
	 * @throws GroupNotFoundException
	 */
	@POST	@Path("getGroupByUUID/{groupUUID}")
  Group getGroupByUUID(
      @PathParam("groupUUID") String groupUUID,
      @FormParam("options") final Map<String, String> options) 
	throws RemoteException, GroupNotFoundException;
  
	/**
	 * Retrieve all the groups
	 * @param options the options map (domain, queryList, user)
	 * @return
	 * @throws RemoteException
	 */
	@POST	@Path("getAllGroups")
  List<Group> getAllGroups(
      @FormParam("options") final Map<String, String> options)
  throws RemoteException;
  
	/**
	* Retrieve a limited number of groups (for paginated UI)
   * @param fromIndex start index
   * @param numberOfGroups maximum number of groups retrieved
	 * @param options the options map (domain, queryList, user)
	 * @return a {@link List} of {@link Group}
	 * @throws RemoteException
	 */
	@POST	@Path("getGroups")
  List<Group> getGroups(
      @QueryParam("fromIndex") int fromIndex,
      @QueryParam("numberOfGroups") int numberOfGroups, 
  		@FormParam("options") final Map<String, String> options)
  throws RemoteException;
	
	/**
   * Retrieve a limited number of groups (for paginated UI) order by the given criterion
   * @param fromIndex start index
   * @param numberOfGroups maximum number of groups retrieved
   * @param pagingCriterion the criterion used to sort the retrieved groups
   * @param options the options map (domain, queryList, user)
   * @return a {@link List} of {@link Group}
   */
	@POST  @Path("getGroupsWithPagingCriterion")
  List<Group> getGroups(
      @QueryParam("fromIndex") int fromIndex, 
      @QueryParam("numberOfGroups") int numberOfGroups, 
      @QueryParam("pagingCriterion") GroupCriterion pagingCriterion, 
      @FormParam("options") final Map<String, String> options)
  throws RemoteException;
  
	/**
	 * get the group count
	 * @param options the options map (domain, queryList, user)
	 * @return the number of groups
	 * @throws RemoteException
	 */
	@POST	@Path("getNumberOfGroups")
  int getNumberOfGroups(
      @FormParam("options") final Map<String, String> options)
	throws RemoteException;
  
	/**
	* Retrieve the direct children groups of a group
   * @param groupUUID the group UUID
	 * @param options the options map (domain, queryList, user)
	 * @return @return a {@link List} of {@link Group}
	 * @throws RemoteException
	 */
	@POST	@Path("getChildrenGroupsByUUID")
  List<Group> getChildrenGroupsByUUID(
      @QueryParam("groupUUID") String groupUUID,
      @FormParam("options") final Map<String, String> options)
  throws RemoteException;

	/**
	 * Retrieve a limited number of groups (for paginated UI)
   * @param groupUUID the group UUID
   * @param fromIndex start index
   * @param numberOfGroups maximum number of groups retrieved
	 * @param options the options map (domain, queryList, user)
	 * @return a {@link List} of {@link Group}
	 * @throws RemoteException
	 * @throws GroupNotFoundException
	 */
	@POST	@Path("getChildrenGroups/{groupUUID}")
  List<Group> getChildrenGroups(
      @PathParam("groupUUID") String groupUUID,
      @QueryParam("fromIndex") int fromIndex, 
      @QueryParam ("numberOfGroups") int numberOfGroups, 
  		@FormParam("options") final Map<String, String> options)
  throws RemoteException, GroupNotFoundException;
	
	/**
   * Retrieve a limited number of groups (for paginated UI) order by the given criterion
   * @param groupUUID the group UUID
   * @param fromIndex start index
   * @param numberOfGroups maximum number of groups retrieved
   * @param pagingCriterion the criterion used to sort the retrieved groups
   * @param options the options map (domain, queryList, user)
   * @return a {@link List} of {@link Group}
   */
	@POST  @Path("getChildrenGroupsWithPagingCriterion/{groupUUID}")
  List<Group> getChildrenGroups(
      @PathParam("groupUUID") String groupUUID, 
      @QueryParam("fromIndex") int fromIndex, 
      @QueryParam("numberOfGroups") int numberOfGroups, 
      @QueryParam("pagingCriterion") GroupCriterion pagingCriterion, 
      @FormParam("options") final Map<String, String> options) 
  throws GroupNotFoundException, RemoteException;
  
	/**
	 * Get the group count
	 * @param groupUUID the group UUID
	 * @param options the options map (domain, queryList, user)
	 * @return the number of groups
	 * @throws RemoteException
	 * @throws GroupNotFoundException
	 */
	@POST	@Path("getNumberOfChildrenGroups/{groupUUID}")
  int getNumberOfChildrenGroups(
      @PathParam("groupUUID") String groupUUID,
      @FormParam("options") final Map<String, String> options) 
	throws RemoteException, GroupNotFoundException;
  
	/**
	 * Create a new group
   * @param name the group's name
   * @param parentGroupUUID the parent group UUID (null if the group should be created at the root)
	 * @param options the options map (domain, queryList, user)
	 * @return the {@link Group} created
	 * @throws RemoteException
	 * @throws GroupAlreadyExistsException
	 * @throws GroupNotFoundException
	 */
	@POST	@Path("addGroup/{name}")
  Group addGroup(
      @PathParam("name") String name,
      @QueryParam("parentGroupUUID") String parentGroupUUID, 
  		@FormParam("options") final Map<String, String> options)
	throws RemoteException, GroupAlreadyExistsException, GroupNotFoundException;
  
	/**
	 * Create a new group
   * @param name the group's name
   * @param label the group's label
   * @param description the group's description
   * @param parentGroupUUID the parent group UUID (null if the group should be created at the root)
	 * @param options the options map (domain, queryList, user)
	 * @return the {@link Group} created
	 * @throws RemoteException
	 * @throws GroupAlreadyExistsException
	 * @throws GroupNotFoundException
	 */
	@POST	@Path("addGroupUsingLabelAndDescription/{name}")
  Group addGroup(
      @PathParam("name") String name,
      @QueryParam("label") String label,
      @FormParam("description")String description, 
  		@QueryParam("parentGroupUUID") String parentGroupUUID,
  		@FormParam("options") final Map<String, String> options)
	throws RemoteException, GroupAlreadyExistsException, GroupNotFoundException;

	/**
	 * Check if a group exists
   * @param groupUUID the group UUID
	 * @param options the options map (domain, queryList, user)
	 * @return
	 * @throws RemoteException
	 */
	@POST	@Path("groupExists/{groupUUID}")
  Boolean groupExists(
      @PathParam("groupUUID") String groupUUID,
      @FormParam("options") final Map<String, String> options)
	throws RemoteException;
  
	/**
	 * Import a group
   * @param uuid the group's UUID
   * @param name the group's name
   * @param label the group's label
   * @param description the group's description
   * @param parentGroupUUID the parent group UUID (null if the group should be created at the root)
	 * @param options the options map (domain, queryList, user)
	 * @return the {@link Group} created
	 * @throws RemoteException
	 * @throws GroupAlreadyExistsException
	 * @throws GroupNotFoundException
	 */
	@POST	@Path("importGroup/{uuid}/{name}")
  Group importGroup(
      @PathParam("uuid") String uuid,
      @PathParam("name") String name,
      @QueryParam("label") String label,
      @FormParam("description")String description,
      @QueryParam("parentGroupUUID") String parentGroupUUID,
      @FormParam("options") final Map<String, String> options)
	throws RemoteException, GroupAlreadyExistsException, GroupNotFoundException;

	/**
	 * Update a group
   * @param groupUUID the actual group UUID of the group to update
   * @param name the group's name
   * @param label the group's label
   * @param description the group's description
   * @param parentGroupUUID the parent group UUID
	 * @param options the options map (domain, queryList, user)
	 * @return the updated {@link Group}
	 * @throws RemoteException
	 * @throws GroupNotFoundException
	 * @throws GroupAlreadyExistsException
	 */
	@POST	@Path("updateGroupByUUID/{groupUUID}/{name}")
  Group updateGroupByUUID(
      @PathParam("groupUUID") String groupUUID,
      @PathParam("name") String name,
      @QueryParam("label") String label, 
  		@FormParam("description")String description,
  		@QueryParam("parentGroupUUID")String parentGroupUUID, 
  		@FormParam("options") final Map<String, String> options) 
	throws RemoteException, GroupNotFoundException, GroupAlreadyExistsException;

	/**
	 * Permanently remove a group (and its children)
   * @param groupUUID the group's UUID
	 * @param options the options map (domain, queryList, user)
	 * @throws RemoteException
	 * @throws GroupNotFoundException
	 */
	@POST	@Path("removeGroupByUUID/{groupUUID}")
  void removeGroupByUUID(
      @PathParam("groupUUID") String groupUUID,
      @FormParam("options") final Map<String, String> options) 
	throws RemoteException, GroupNotFoundException;
  
	/**
	 * Retrieve a membership from it's UUID
   * @param membershipUUID the membership UUID
	 * @param options the options map (domain, queryList, user)
	 * @return a {@link Membership}
	 * @throws RemoteException
	 * @throws MembershipNotFoundException
	 */
	@POST	@Path("getMembershipByUUID/{membershipUUID}")
  Membership getMembershipByUUID(
      @PathParam("membershipUUID") String membershipUUID,
      @FormParam("options") final Map<String, String> options) 
	throws RemoteException, MembershipNotFoundException;
  
	/**
	 * Obtain the {@link Membership} for a role and a group (either it's an existing membership or it's created if the role and group exist)
   * @param roleUUID the role UUID
   * @param groupUUID the group UUID
	 * @param options the options map (domain, queryList, user)
	 * @return the {@link Membership} for the role and the group
	 * @throws RemoteException
	 * @throws RoleNotFoundException
	 * @throws GroupNotFoundException
	 */
	@POST	@Path("getMembershipForRoleAndGroup/{roleUUID}/{groupUUID}")
  Membership getMembershipForRoleAndGroup(
      @PathParam("roleUUID") String roleUUID,
      @PathParam("groupUUID") String groupUUID, 
  		@FormParam("options") final Map<String, String> options)
	throws RemoteException, RoleNotFoundException, GroupNotFoundException;
  
	/**
	 * Add a membership to a user
   * @param userUUID the user UUID
   * @param membershipUUID the membership UUID
	 * @param options the options map (domain, queryList, user)
	 * @throws RemoteException
	 * @throws UserNotFoundException
	 * @throws MembershipNotFoundException
	 */
	@POST	@Path("addMembershipToUser/{userUUID}/{membershipUUID}")
  void addMembershipToUser(
      @PathParam("userUUID") String userUUID,
      @PathParam("membershipUUID") String membershipUUID, 
  		@FormParam("options") final Map<String, String> options)
	throws RemoteException, UserNotFoundException, MembershipNotFoundException;

	/**
	 * Remove a membership from a user
   * @param userUUID the user UUID
	 * @param options the options map (domain, queryList, user)
	 * @throws RemoteException
	 * @throws UserNotFoundException
	 * @throws MembershipNotFoundException
	 */
	@POST	@Path("removeMembershipFromUser/{userUUID}/{membershipUUID}")
  void removeMembershipFromUser(
      @PathParam("userUUID") String userUUID,
      @PathParam("membershipUUID") String membershipUUID, 
  		@FormParam("options") final Map<String, String> options)
	throws RemoteException, UserNotFoundException, MembershipNotFoundException;

	/**
	* Add a role to a user
   * @param roleName the role's name
   * @param username the user's username
	 * @param options the options map (domain, queryList, user)
	 * @throws RemoteException
	 * @throws UserNotFoundException
	 * @throws RoleNotFoundException
	 */
	@POST	@Path("addRoleToUser/{roleName}/{username}")
  @Deprecated
  void addRoleToUser(
      @PathParam("roleName") String roleName,
      @PathParam("username") String username,
      @FormParam("options") final Map<String, String> options) 
	throws RemoteException, UserNotFoundException, RoleNotFoundException;

	/**
	 * @param username the user's username
   * @param roleNames the user's roles to set
	 * @param options the options map (domain, queryList, user)
	 * @throws RemoteException
	 * @throws UserNotFoundException
	 * @throws RoleNotFoundException
	 * 
	 * @deprecated use {@link IdentityAPI#setUserMemberships(String, Collection)} instead with memberships obtained with {@link IdentityAPI#getMembershipForRoleAndGroup(String, String)} and the default group {@link IdentityAPI#DEFAULT_GROUP_NAME}
	 */
	@POST	@Path("setUserRoles/{username}")
  @Deprecated
  void setUserRoles(
      @PathParam("username") String username,
      @FormParam("roleNames") Set<String>roleNames, 
  		@FormParam("options") final Map<String, String> options)
	throws RemoteException, UserNotFoundException, RoleNotFoundException;

	/**
	 * Remove role from a user
   * @param roleName the role's name
   * @param username the user's username
	 * @param options the options map (domain, queryList, user)
	 * @throws RemoteException
	 * @throws UserNotFoundException
	 * @throws RoleNotFoundException
	 */
	@POST	@Path("removeRoleFromUser/{roleName}/{username}")
  @Deprecated
  void removeRoleFromUser(
      @PathParam("roleName") String roleName,
      @PathParam("username") String username, 
  		@FormParam("options") final Map<String, String> options)
	throws RemoteException, UserNotFoundException, RoleNotFoundException;

	/**
	 * Retrieves the group according to the group path. 
   * @param path the group path (the first element is the root groupName, the second element is the childName of the root group, ...)
	 * @param options the options map (domain, queryList, user)
	 * @return the group corresponding to the group path; null otherwise
	 * @throws RemoteException
	 */
	@POST	@Path("getGroupUsingPath")
	Group getGroupUsingPath(
	    @FormParam("hierarchy") final List<String> path,
	    @FormParam("options") final Map<String, String> options)
	throws RemoteException;

}
