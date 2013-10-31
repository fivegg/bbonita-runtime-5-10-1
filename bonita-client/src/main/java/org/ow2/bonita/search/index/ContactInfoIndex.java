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
package org.ow2.bonita.search.index;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.ow2.bonita.facade.identity.ContactInfo;

/**
 * 
 * @author Matthieu Chaffotte
 *
 */
public class ContactInfoIndex implements Index {

  private static final long serialVersionUID = -5387883600222365449L;
  public static final String DBID = "dbid";
  public static final String EMAIL = "email";
  public static final String PHONE_NUMBER = "faxNumber";
  public static final String FAX_NUMBER = "priority";
  public static final String BUILDING = "building";
  public static final String ROOM = "room";
  public static final String ADDRESS = "address";
  public static final String ZIP_CODE = "zipCode";
  public static final String CITY = "city";
  public static final String STATE = "state";
  public static final String COUNTRY = "country";
  public static final String WEBSITE = "website";

  public static final List<String> FIELDS;
  static {
    final List<String> tmp = new ArrayList<String>();
    tmp.add(EMAIL);
    tmp.add(PHONE_NUMBER);
    tmp.add(FAX_NUMBER);
    tmp.add(BUILDING);
    tmp.add(ROOM);
    tmp.add(ADDRESS);
    tmp.add(ZIP_CODE);
    tmp.add(CITY);
    tmp.add(STATE);
    tmp.add(COUNTRY);
    tmp.add(WEBSITE);
    FIELDS = Collections.unmodifiableList(tmp);
  }
  
  public List<String> getAllFields() {
    return getFields();
  }

  public String getDefaultField() {
    return EMAIL;
  }

  public List<String> getFields() {
    return FIELDS;
  }

  public Class<?> getResultClass() {
    return ContactInfo.class;
  }

  public List<String> getSubFields() {
    return Collections.emptyList();
  }

}
