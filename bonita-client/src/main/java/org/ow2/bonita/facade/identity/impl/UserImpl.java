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
package org.ow2.bonita.facade.identity.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;

import org.ow2.bonita.facade.identity.ContactInfo;
import org.ow2.bonita.facade.identity.Membership;
import org.ow2.bonita.facade.identity.ProfileMetadata;
import org.ow2.bonita.facade.identity.User;
import org.ow2.bonita.util.Misc;

/**
 * @author Anthony Birembaut
 *
 */
public class UserImpl implements User {

  private static final long serialVersionUID = 5357217264648978573L;

  protected long dbid;
  protected String uuid;
  protected String firstName;
  protected String lastName;
  protected String password;
  protected String username;
  protected String manager;
  protected String delegee;
  protected String title;
  protected String jobTitle;
  protected ContactInfo professionalContactInfo;
  protected ContactInfo personalContactInfo;
  protected Map<ProfileMetadata, String> metadata;
  protected Set<Membership> memberships;
  
  protected UserImpl() {}
  
  public UserImpl(String username, String password) {
    Misc.checkArgsNotNull(username, password);
    this.uuid = UUID.randomUUID().toString();
    this.username = username;
    this.password = password;
  }
  
  /**
   * Constructor for user import
   * @param uuid the user UUID (should be URL compliant)
   * @param username the username
   * @param password the user's password
   */
  public UserImpl(String uuid, String username, String password) {
    Misc.checkArgsNotNull(uuid, username, password);
    this.uuid = uuid;
    this.username = username;
    this.password = password;
  }
  
  public UserImpl(UserImpl src) {
    Misc.checkArgsNotNull(src);
    this.uuid = src.getUUID();
    this.firstName = src.getFirstName();
    this.lastName = src.getLastName();
    this.password = src.getPassword();
    this.username = src.getUsername();
    this.title = src.getTitle();
    this.jobTitle = src.getJobTitle();
    this.manager = src.getManagerUUID();
    this.delegee = src.getDelegeeUUID();
    if (src.getPersonalContactInfo() != null) {
      this.personalContactInfo = new ContactInfoImpl((ContactInfoImpl)src.getPersonalContactInfo());
    }
    if (src.getProfessionalContactInfo() != null) {
      this.professionalContactInfo = new ContactInfoImpl((ContactInfoImpl)src.getProfessionalContactInfo());
    }
    this.memberships = new HashSet<Membership>();
    final Set<Membership> memberships = src.getMemberships();
    for (Membership membership : memberships) {
      this.memberships.add(new MembershipImpl((MembershipImpl)membership));
    }

    this.metadata = new HashMap<ProfileMetadata, String>();
    final Map<ProfileMetadata, String> metadata = src.getMetadata();
    for (Entry<ProfileMetadata, String> entry : metadata.entrySet()) {
      this.metadata.put(new ProfileMetadataImpl((ProfileMetadataImpl)entry.getKey()), entry.getValue());
    }
  }
  
  public String getUUID() {
    return uuid;
  }

  public String getFirstName() {
    return firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public String getPassword() {
    return password;
  }

  public String getUsername() {
    return username;
  }

  public String getTitle() {
    return title;
  }

  public String getJobTitle() {
    return jobTitle;
  }

  public String getManagerUUID() {
    return manager;
  }

  public String getDelegeeUUID() {
    return delegee;
  }

  @Deprecated
  public String getEmail() {
    if (professionalContactInfo != null) {
      return professionalContactInfo.getEmail();
    }
    return null;
  }

  public ContactInfo getPersonalContactInfo() {
    return personalContactInfo;
  }

  public ContactInfo getProfessionalContactInfo() {
    return professionalContactInfo;
  }

  public Map<ProfileMetadata, String> getMetadata() {
    if (metadata == null) {
      metadata = new HashMap<ProfileMetadata, String>();
    }
    return metadata;
  }

  public Set<Membership> getMemberships() {
    if (memberships == null) {
      memberships = new HashSet<Membership>();
    }
    return memberships;
  }

  public void setEmail(String email) {
    if (professionalContactInfo == null) {
      professionalContactInfo = new ContactInfoImpl();
    }
    ((ContactInfoImpl)professionalContactInfo).setEmail(email);
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public void setPersonalContactInfo(ContactInfo personalContactInfo) {
    this.personalContactInfo = personalContactInfo;
  }

  public void setProfessionalContactInfo(ContactInfo professionalContactInfo) {
    this.professionalContactInfo = professionalContactInfo;
  }

  public void setMetadata(Map<ProfileMetadata, String> metadata) {
    this.metadata = metadata;
  }
  
  public void setMemberships(Set<Membership> memberships) {
    this.memberships = memberships;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public void setJobTitle(String jobTitle) {
    this.jobTitle = jobTitle;
  }

  public void setManagerUUID(String manager) {
    this.manager = manager;
  }

  public void setDelegeeUUID(String delegee) {
    this.delegee = delegee;
  }
  
}
