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
package org.ow2.bonita.facade.runtime.command;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.ow2.bonita.env.Environment;
import org.ow2.bonita.facade.APIAccessor;
import org.ow2.bonita.facade.IdentityAPI;
import org.ow2.bonita.facade.identity.User;
import org.ow2.bonita.facade.impl.StandardAPIAccessorImpl;
import org.ow2.bonita.util.Command;

/**
 * 
 * @author Nicolas Chabanoles
 *
 */
public class WebUpdateUserCommand implements Command<User> {

  private static final long serialVersionUID = -6178529043517754122L;
  
  private static final String WEBSITE_KEY = "website";
  private static final String COUNTRY_KEY = "country";
  private static final String STATE_KEY = "state";
  private static final String CITY_KEY = "city";
  private static final String ZIPCODE_KEY = "zip";
  private static final String ADDRESS_KEY = "address";
  private static final String ROOM_KEY = "room";
  private static final String BUILDING_KEY = "building";
  private static final String FAX_NUMBER_KEY = "fax";
  private static final String MOBILE_NUMBER_KEY = "mobile";
  private static final String PHONE_NUMBER_KEY = "phone";
  private static final String EMAIL_KEY = "email";
  
  private String username;
	private String firstName;
	private String lastName;
	private String password;
  private Map<String, Collection<String>> memberships;
  private Map<String, String> metadata;
  private boolean updatePassword;
  private String userUuid;
  private final String title;
  private final String jobTitle;
  private final String managerUuid;
  private final Map<String, String> personalContactInfo;
  private final Map<String, String> professionalContactInfo;
  private final String delegateUuid;

	/**
	 * Default constructor.
	 * Update the user definition. The user's password will not be updated unless the {@code updatePassword} is set to true.
	 * If {@code updatePassword} is false then the {@code password} should be null.
	 */
	public WebUpdateUserCommand(final String userUuid, final String username, final String firstName, final String lastName, final boolean needToUpdatePassword, final String password, final String title, final String jobTitle, final String managerUuid, final String delegateUuid, final Map<String, String> personalContactInfo, final Map<String,String> professionalContactInfo, final Map<String,Collection<String>> memberships, final Map<String,String> metadata) {
	  this.userUuid = userUuid;
		this.username = username;
		this.firstName = firstName;
		this.lastName = lastName;
		this.password = password;
		this.updatePassword = needToUpdatePassword;
    this.title = title;
    this.jobTitle = jobTitle;
    this.managerUuid = managerUuid;
    this.delegateUuid = delegateUuid;
    if(personalContactInfo != null) {
      this.personalContactInfo = new HashMap<String, String>(personalContactInfo);
    } else {
      this.personalContactInfo = new HashMap<String, String>();
    }
    
    if(professionalContactInfo != null) {
      this.professionalContactInfo = new HashMap<String, String>(professionalContactInfo);
    } else {
      this.professionalContactInfo = new HashMap<String, String>();
    }
		this.memberships = memberships;
		this.metadata = metadata;
	}

	public User execute(Environment environment) throws Exception {
		final APIAccessor accessor = new StandardAPIAccessorImpl();
		final IdentityAPI identityAPI = accessor.getIdentityAPI();
		
		User user;
		user = identityAPI.updateUserByUUID(userUuid, username, firstName, lastName, title, jobTitle, managerUuid, metadata);
		if(updatePassword){
		  identityAPI.updateUserPassword(userUuid, password);
		}
		identityAPI.updateUserDelegee(userUuid, delegateUuid);
		identityAPI.updateUserPersonalContactInfo(userUuid, personalContactInfo.get(EMAIL_KEY), personalContactInfo.get(PHONE_NUMBER_KEY), personalContactInfo.get(MOBILE_NUMBER_KEY), personalContactInfo.get(FAX_NUMBER_KEY), personalContactInfo.get(BUILDING_KEY), personalContactInfo.get(ROOM_KEY), personalContactInfo.get(ADDRESS_KEY), personalContactInfo.get(ZIPCODE_KEY), personalContactInfo.get(CITY_KEY), personalContactInfo.get(STATE_KEY), personalContactInfo.get(COUNTRY_KEY), personalContactInfo.get(WEBSITE_KEY));
		identityAPI.updateUserProfessionalContactInfo(userUuid, professionalContactInfo.get(EMAIL_KEY), professionalContactInfo.get(PHONE_NUMBER_KEY), professionalContactInfo.get(MOBILE_NUMBER_KEY), professionalContactInfo.get(FAX_NUMBER_KEY), professionalContactInfo.get(BUILDING_KEY), professionalContactInfo.get(ROOM_KEY), professionalContactInfo.get(ADDRESS_KEY), professionalContactInfo.get(ZIPCODE_KEY), professionalContactInfo.get(CITY_KEY), professionalContactInfo.get(STATE_KEY), professionalContactInfo.get(COUNTRY_KEY), professionalContactInfo.get(WEBSITE_KEY));
		final Collection<String> ms = new ArrayList<String>();
		String groupId;
		for (Entry<String,Collection<String>> entry : memberships.entrySet()) {
		  groupId = entry.getKey();
		  for (String roleId : entry.getValue()) {
		    ms.add(identityAPI.getMembershipForRoleAndGroup(roleId, groupId).getUUID());
      }
    }
		identityAPI.setUserMemberships(user.getUUID(), ms);
		
		return identityAPI.getUserByUUID(userUuid);
	}

}
