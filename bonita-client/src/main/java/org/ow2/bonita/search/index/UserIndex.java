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

import org.ow2.bonita.facade.identity.impl.UserImpl;

/**
 * 
 * @author Matthieu Chaffotte
 *
 */
public class UserIndex implements Index {

  private static final long serialVersionUID = 4619748548630433651L;
  public static final String DBID = "dbid";
  public static final String NAME = "name";
  public static final String FIRST_NAME = "firstName";
  public static final String LAST_NAME = "lastName";
  public static final String MANAGER = "manager";
  public static final String DELEGATE = "delegate";
  public static final String TITLE = "title";
  public static final String JOB_TITLE = "jobTitle";
  public static final String PROFESSIONAL_INFO = "professionalContactInfo.";
  public static final String PERSONNAL_INFO = "personnalContactInfo.";

  public static final List<String> FIELDS;
  public static final List<String> SUB_FIELDS;
  static {
    final List<String> tmp = new ArrayList<String>();
    tmp.add(NAME);
    tmp.add(FIRST_NAME);
    tmp.add(LAST_NAME);
    tmp.add(MANAGER);
    tmp.add(DELEGATE);
    tmp.add(TITLE);
    tmp.add(JOB_TITLE);
    FIELDS = Collections.unmodifiableList(tmp);
    
    final List<String> sub = new ArrayList<String>();
    sub.add(PROFESSIONAL_INFO);
    sub.add(PERSONNAL_INFO);
    SUB_FIELDS = Collections.unmodifiableList(sub);
  }

  public List<String> getAllFields() {
    List<String> fields = getFields();
    fields.addAll(getSubFields());
    return fields;
  }

  public String getDefaultField() {
    return NAME;
  }

  public List<String> getFields() {
    return FIELDS;
  }

  public List<String> getSubFields() {
    return SUB_FIELDS;
  }

  public Class<?> getResultClass() {
    return UserImpl.class;
  }

}
