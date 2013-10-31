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

import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

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
 * 
 * @author Elias Ricken de Medeiros
 *
 */
@Path("/API/identityAPI/")
@Produces({"text/*","application/xml"})
@Consumes({"application/x-www-form-urlencoded","text/*", "application/xml"})
public interface RESTRemoteIdentityAPI extends AbstractRemoteIdentityAPI {

	/**
	 * Permanently remove a collection of profile metadata
   * @param profileMetadataUUIDs the collection of profile metadata to remove
	 * @param options the options map (domain, queryList, user)
	 * @throws RemoteException
	 * @throws MetadataNotFoundException
	 */
	@POST	@Path("removeProfileMetadata")
  void removeProfileMetadata(
      @FormParam("profileMetadataUUIDs") List<String> profileMetadataUUIDs, 
  		@FormParam("options") final Map<String, String> options)
	throws RemoteException, MetadataNotFoundException;

	/**
	 * Permanently remove a collection of users
   * @param userUUIDs the users UUIDs
	 * @param options the options map (domain, queryList, user)
	 * @throws RemoteException
	 * @throws UserNotFoundException
	 */
	@POST	@Path("removeUsers")
  void removeUsers(
      @FormParam("userUUIDs") List<String> userUUIDs,
      @FormParam("options") final Map<String, String> options)
	throws RemoteException, UserNotFoundException;

	/**
	 * Permanently remove a collection of roles
   * @param roleUUIDs the roles UUIDs
	 * @param options the options map (domain, queryList, user)
	 * @throws RemoteException
	 * @throws RoleNotFoundException
	 */
	@POST	@Path("removeRoles")
  void removeRoles(
      @FormParam("roleUUIDs") List<String> roleUUIDs,
      @FormParam("options") final Map<String, String> options) 
	throws RemoteException, RoleNotFoundException;

	/**
	 * Permanently remove a collection of groups (and their children)
   * @param groupUUIDs the group's UUIDs
	 * @param options the options map (domain, queryList, user)
	 * @throws RemoteException
	 * @throws GroupNotFoundException
	 */
	@POST	@Path("removeGroups")
  void removeGroups(
      @FormParam("groupUUIDs") List<String> groupUUIDs,
      @FormParam("options") final Map<String, String> options) 
	throws RemoteException, GroupNotFoundException;

	/**
	 * Add a collection of memberships to a user
   * @param userUUID the user UUID
   * @param membershipUUIDs the collection of membership UUID
	 * @param options the options map (domain, queryList, user)
	 * @throws RemoteException
	 * @throws UserNotFoundException
	 * @throws MembershipNotFoundException
	 */
	@POST	@Path("addMembershipsToUser/{userUUID}")
  void addMembershipsToUser(
      @PathParam("userUUID") String userUUID,
      @FormParam("membershipUUIDs") List<String> membershipUUIDs, 
  		@FormParam("options") final Map<String, String> options)
	throws RemoteException, UserNotFoundException, MembershipNotFoundException;

	/**
	 * Remove a collection of memberships from a user
   * @param userUUID the user UUID
   * @param membershipUUIDs the collection of membership UUID
	 * @param options the options map (domain, queryList, user)
	 * @throws RemoteException
	 * @throws UserNotFoundException
	 * @throws MembershipNotFoundException
	 */
	@POST	@Path("removeMembershipsFromUser/{userUUID}")
  void removeMembershipsFromUser(
      @PathParam("userUUID") String userUUID,
      @FormParam("membershipUUIDs") List<String> membershipUUIDs, 
  		@FormParam("options") final Map<String, String> options)
	throws RemoteException, UserNotFoundException, MembershipNotFoundException;
  
	/**
	 * Set the memberships of a user
   * @param userUUID the user UUID
   * @param membershipUUIDs the collection of membership UUID
	 * @param options the options map (domain, queryList, user)
	 * @throws RemoteException
	 * @throws UserNotFoundException
	 * @throws MembershipNotFoundException
	 */
	@POST @Path("setUserMemberships/{userUUID}")
  void setUserMemberships(
      @PathParam("userUUID") String userUUID,
      @FormParam("membershipUUIDs") List<String> membershipUUIDs, 
  		@FormParam("options") final Map<String, String> options)
	throws RemoteException, UserNotFoundException, MembershipNotFoundException;

	/**
	 * Retrieve a list of users
   * @param userUUIDs the UUIDs of the required users
	 * @param options the options map (domain, queryList, user)
	 * @return a {@link List} of {@link User}
	 * @throws RemoteException
	 * @throws UserNotFoundException
	 */
	@POST	@Path("getUsersByUUIDs")
  List<User> getUsersByUUIDs(
      @FormParam("userUUIDs") List<String> userUUIDs,
      @FormParam("options") final Map<String, String> options) 
  throws RemoteException, UserNotFoundException;

	/**
	 * Retrieve a list of roles
   * @param roleUUIDs the UUIDs of the required roles
	 * @param options the options map (domain, queryList, user)
	 * @return a {@link List} of {@link Role}
	 * @throws RemoteException
	 * @throws RoleNotFoundException
	 */
	@POST @Path("getRolesByUUIDs")
  List<Role> getRolesByUUIDs(
      @FormParam("roleUUIDs") List<String> roleUUIDs,
      @FormParam("options") final Map<String, String> options) 
  throws RemoteException, RoleNotFoundException;

	/**
	 * Retrieve a list of groups
   * @param groupUUIDs the UUIDs of the required groups
	 * @param options the options map (domain, queryList, user)
	 * @return a {@link List} of {@link Group}
	 * @throws RemoteException
	 * @throws GroupNotFoundException
	 */
	@POST @Path("getGroupsByUUIDs")
  List<Group> getGroupsByUUIDs(
      @FormParam("groupUUIDs") List<String> groupUUIDs,
      @FormParam("options") final Map<String, String> options)
  throws RemoteException, GroupNotFoundException;
  
	/**
	 * Retrieve a list of memberships
   * @param membershipUUIDs the UUIDs of the required memberships
	 * @param options the options map (domain, queryList, user)
	 * @return a {@link List} of {@link Membership}
	 * @throws RemoteException
	 * @throws MembershipNotFoundException
	 */
	@POST	@Path("getMembershipsByUUIDs")
  List<Membership> getMembershipsByUUIDs(
      @FormParam("membershipUUIDs") List<String> membershipUUIDs, 
  		@FormParam("options") final Map<String, String> options)
  throws RemoteException, MembershipNotFoundException;

}
