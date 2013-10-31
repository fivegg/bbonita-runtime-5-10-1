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
package org.ow2.bonita.facade.identity.impl;

import org.ow2.bonita.facade.identity.ContactInfo;

/**
 * @author Anthony Birembaut
 *
 */
public class ContactInfoImpl implements ContactInfo {

  /**
   * UID
   */
  private static final long serialVersionUID = 7538697709415948019L;
  
  protected String email;
  protected String phoneNumber;
  protected String mobileNumber;
  protected String faxNumber;
  protected String building;
  protected String room;
  protected String address;
  protected String zipCode;
  protected String city;
  protected String state;
  protected String country;
  protected String website;
  
  public ContactInfoImpl() {}
  
  public ContactInfoImpl(ContactInfoImpl src) {
    this.email = src.getEmail();
    this.phoneNumber = src.getPhoneNumber();
    this.mobileNumber = src.getMobileNumber();
    this.faxNumber = src.getFaxNumber();
    this.building = src.getBuilding();
    this.room = src.getRoom();
    this.address = src.getAddress();
    this.zipCode = src.getZipCode();
    this.city = src.getCity();
    this.state = src.getState();
    this.country = src.getCountry();
    this.website = src.getWebsite();
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPhoneNumber() {
    return phoneNumber;
  }

  public void setPhoneNumber(String phoneNumber) {
    this.phoneNumber = phoneNumber;
  }

  public String getMobileNumber() {
    return mobileNumber;
  }

  public void setMobileNumber(String mobileNumber) {
    this.mobileNumber = mobileNumber;
  }

  public String getFaxNumber() {
    return faxNumber;
  }

  public void setFaxNumber(String faxNumber) {
    this.faxNumber = faxNumber;
  }

  public String getBuilding() {
    return building;
  }

  public void setBuilding(String building) {
    this.building = building;
  }

  public String getRoom() {
    return room;
  }

  public void setRoom(String room) {
    this.room = room;
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public String getZipCode() {
    return zipCode;
  }

  public void setZipCode(String zipCode) {
    this.zipCode = zipCode;
  }

  public String getCity() {
    return city;
  }

  public void setCity(String city) {
    this.city = city;
  }

  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }

  public String getCountry() {
    return country;
  }

  public void setCountry(String country) {
    this.country = country;
  }

  public String getWebsite() {
    return website;
  }

  public void setWebsite(String website) {
    this.website = website;
  }

}
