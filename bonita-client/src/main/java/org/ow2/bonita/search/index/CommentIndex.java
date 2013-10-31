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

import org.ow2.bonita.facade.runtime.Comment;

/**
 * 
 * @author Matthieu Chaffotte
 *
 */
public class CommentIndex implements Index {

  private static final long serialVersionUID = 4480531193668403252L;
  public static final String DBID = "dbid";
  public static final String MESSAGE = "message";
  public static final String AUTHOR = "author";
  public static final String DATE = "date";

  public static final List<String> FIELDS;
  static {
    final List<String> tmp = new ArrayList<String>();
    tmp.add(MESSAGE);
    tmp.add(AUTHOR);
    tmp.add(DATE);
    FIELDS = Collections.unmodifiableList(tmp);
  }

  public List<String> getAllFields() {
    return getFields();
  }

  public String getDefaultField() {
    return MESSAGE;
  }

  public List<String> getFields() {
    return FIELDS;
  }

  public Class<?> getResultClass() {
    return Comment.class;
  }

  public List<String> getSubFields() {
    return Collections.emptyList();
  }

}
