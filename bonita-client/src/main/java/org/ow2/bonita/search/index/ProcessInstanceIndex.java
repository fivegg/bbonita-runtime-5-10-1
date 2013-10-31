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

import org.ow2.bonita.light.LightProcessInstance;
import org.ow2.bonita.search.SearchQueryBuilder;

/**
 * 
 * @author Matthieu Chaffotte
 *
 */
public class ProcessInstanceIndex implements Index {

  private static final long serialVersionUID = -6371070576258169695L;
  public static final String DBID = "dbid";
  public static final String UUID = "uuid";
  public static final String NB = "nb";
  public static final String STARTED_BY = "startedBy";
  public static final String ENDED_BY = "endedBy";
  public static final String STARTED_DATE = "startedDate";
  public static final String ENDED_DATE = "endedDate";
  public static final String LAST_UPDATE = "lastUpdate";
  /**
   * @Deprecated Use ActivityInstanceIndex.USERID combined to ActivityInstanceIndex.CANDIDATE using ProcessInstanceIndex.ACTIVITY
   * 
   * {@code final SearchQueryBuilder query = new SearchQueryBuilder(new ProcessInstanceIndex())
	    .leftParenthesis()
	    .criterion(ProcessInstanceIndex.ACTIVITY + ActivityInstanceIndex.USERID)
	    .startsWith(getLogin())
	    .rightParenthesis()
	    .or()
	    .leftParenthesis()
	    .criterion(ProcessInstanceIndex.ACTIVITY + ActivityInstanceIndex.CANDIDATE)
	    .startsWith(getLogin())
	    .rightParenthesis();
	    }
   */
  public static final String ACTIVE_USER = "activeUser";
  public static final String INVOLVED_USER = "involvedUser";
  public static final String COMMENT = "commentFeed.";
  public static final String ACTIVITY = "activities.";
  public static final String VARIABLE_NAME = "variable_name";
  public static final String VARIABLE_VALUE = "variable_value";
  public static final String PROCESS_DEFINITION_UUID = "processDefUUID";
  public static final String PROCESS_INSTANCE_UUID = "instanceUUID";
  public static final String PROCESS_ROOT_INSTANCE_UUID = "rootInstanceUUID";

  public static final List<String> FIELDS;
  public static final List<String> SUB_FIELDS;

  static {
    final List<String> tmp = new ArrayList<String>();
    tmp.add(UUID);
    tmp.add(PROCESS_DEFINITION_UUID);
    tmp.add(NB);
    tmp.add(STARTED_BY);
    tmp.add(ENDED_BY);
    tmp.add(STARTED_DATE);
    tmp.add(ENDED_DATE);
    tmp.add(LAST_UPDATE);
    tmp.add(ACTIVE_USER);
    tmp.add(INVOLVED_USER);
    FIELDS = Collections.unmodifiableList(tmp);
    
    final List<String> sub = new ArrayList<String>();
    sub.add(COMMENT);
    sub.add(ACTIVITY);
    sub.add(VARIABLE_NAME);
    sub.add(VARIABLE_VALUE);
    SUB_FIELDS = Collections.unmodifiableList(sub);
  }

  public List<String> getAllFields() {
    List<String> fields = new ArrayList<String>(getFields());
    fields.addAll(getSubFields());
    return Collections.unmodifiableList(fields);
  }

  public String getDefaultField() {
    return STARTED_BY;
  }

  public List<String> getFields() {
    return FIELDS;
  }

  public List<String> getSubFields() {
    return SUB_FIELDS;
  }

  public Class<?> getResultClass() {
    return LightProcessInstance.class;
  }

}
