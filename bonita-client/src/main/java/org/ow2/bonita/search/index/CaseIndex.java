/**
 * Copyright (C) 2011 BonitaSoft S.A.
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

import org.ow2.bonita.facade.runtime.impl.CaseImpl;

/**
 * 
 * @author Matthieu Chaffotte
 *
 */
public class CaseIndex implements Index {

  private static final long serialVersionUID = -6004843808427009665L;

  public static final String DBID = "dbid";
  public static final String OWNER_NAME = "ownerName";
  public static final String LABEL_NAME = "labelName";
  public static final String PROCESS_INSTANCE_UUID = "uuid";

  public static final List<String> FIELDS;
  static {
    final List<String> tmp = new ArrayList<String>();
    tmp.add(OWNER_NAME);
    tmp.add(LABEL_NAME);
    tmp.add(PROCESS_INSTANCE_UUID);
    FIELDS = Collections.unmodifiableList(tmp);
  }

  @Override
  public String getDefaultField() {
    return LABEL_NAME;
  }

  @Override
  public List<String> getFields() {
    return FIELDS;
  }

  @Override
  public List<String> getSubFields() {
    return Collections.emptyList();
  }

  @Override
  public List<String> getAllFields() {
    return getFields();
  }

  @Override
  public Class<?> getResultClass() {
    return CaseImpl.class;
  }

}
