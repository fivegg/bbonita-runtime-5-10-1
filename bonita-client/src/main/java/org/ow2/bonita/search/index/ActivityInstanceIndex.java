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

import org.ow2.bonita.light.LightActivityInstance;

/**
 * 
 * @author Matthieu Chaffotte
 *
 */
public class ActivityInstanceIndex implements Index {

  private static final long serialVersionUID = 7820607576165471166L;
  public static final String DBID = "dbid";
  public static final String UUID = "uuid";
  public static final String NAME = "name";
  public static final String DESCRIPTION = "description";
  public static final String PRIORITY = "priority";
  public static final String STATE = "state";
  public static final String LAST_UPDATE = "lastUpdate";
  public static final String EXPECTED_END_DATE = "expectedEndDate";
  public static final String CANDIDATE = "candidate";
  public static final String USERID = "userId";
  public static final String VARIABLE_NAME = "variable_name";
  public static final String VARIABLE_VALUE = "variable_value";
  public static final String PROCESS_DEFINITION_UUID = "processDefUUID";
  public static final String PROCESS_INSTANCE_UUID = "instanceUUID";
  public static final String PROCESS_ROOT_INSTANCE_UUID = "rootInstanceUUID";

  public static final List<String> FIELDS;
  static {
    final List<String> tmp = new ArrayList<String>();
    tmp.add(UUID);
    tmp.add(PROCESS_DEFINITION_UUID);
    tmp.add(PROCESS_INSTANCE_UUID);
    tmp.add(PROCESS_ROOT_INSTANCE_UUID);
    tmp.add(NAME);
    tmp.add(DESCRIPTION);
    tmp.add(PRIORITY);
    tmp.add(STATE);
    tmp.add(EXPECTED_END_DATE);
    tmp.add(LAST_UPDATE);
    tmp.add(CANDIDATE);
    tmp.add(USERID);
    tmp.add(VARIABLE_NAME);
    tmp.add(VARIABLE_VALUE);
    FIELDS = Collections.unmodifiableList(tmp);
  }

  public List<String> getAllFields() {
    return getFields();
  }

  public String getDefaultField() {
    return NAME;
  }

  public List<String> getFields() {
    return FIELDS;
  }

  public List<String> getSubFields() {
    return Collections.emptyList();
  }

  public Class<?> getResultClass() {
    return LightActivityInstance.class;
  }

}
