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

import org.ow2.bonita.light.LightProcessDefinition;

/**
 * 
 * @author Matthieu Chaffotte
 *
 */
public class ProcessDefinitionIndex implements Index {

  private static final long serialVersionUID = -8873676089494406880L;
  public static final String DBID = "dbid";
  public static final String UUID = "uuid";
  public static final String NAME = "name";
  public static final String DESCRIPTION = "description";
  public static final String CATEGORY_NAME = "categoryName";

  public static final List<String> FIELDS;
  static {
    final List<String> tmp = new ArrayList<String>();
    tmp.add(UUID);
    tmp.add(NAME);
    tmp.add(DESCRIPTION);
    tmp.add(CATEGORY_NAME);
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
    return LightProcessDefinition.class;
  }

}
