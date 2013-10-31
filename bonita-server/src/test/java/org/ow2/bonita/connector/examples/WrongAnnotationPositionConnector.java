/**
 * Copyright (C) 2006  Bull S. A. S.
 * Bull, Rue Jean Jaures, B.P.68, 78340, Les Clayes-sous-Bois
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
package org.ow2.bonita.connector.examples;

import java.util.List;

import org.ow2.bonita.connector.core.Connector;
import org.ow2.bonita.connector.core.ConnectorError;

/**
 * This Connector cannot be a right Connector because of wrong position
 * of annotations. So this connector will not never be executed.
 * @author chaffotm
 *
 */
public class WrongAnnotationPositionConnector extends Connector {

  private String name;
  private String surname;
  private int age;
  private String address;
  private boolean male;

  /**
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * @return the surname
   */
  public String getSurname() {
    return surname;
  }

  /**
   * @return the age
   */
  public int getAge() {
    return age;
  }

  /**
   * @return the address
   */
  public String getAddress() {
    return address;
  }

  /**
   * @return the male
   */
  public boolean isMale() {
    return male;
  }

  /**
   * @param name the name to set
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * @param surname the surname to set
   */
  public void setSurname(String surname) {
    this.surname = surname;
  }

  /**
   * @param age the age to set
   */
  public void setAge(int age) {
    this.age = age;
  }

  /**
   * @param address the address to set
   */
  public void setAddress(String address) {
    this.address = address;
  }

  /**
   * @param male the male to set
   */
  public void setMale(boolean male) {
    this.male = male;
  }

  @Override
  protected void executeConnector() throws Exception {
  }

  @Override
  protected List<ConnectorError> validateValues() {
    getAddress();
    getAge();
    getName();
    getSurname();
    isMale();
    return null;
  }
}
