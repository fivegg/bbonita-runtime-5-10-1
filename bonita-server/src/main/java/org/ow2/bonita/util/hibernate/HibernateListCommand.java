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
package org.ow2.bonita.util.hibernate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Query;
import org.hibernate.Session;
import org.ow2.bonita.env.Environment;
import org.ow2.bonita.util.Command;

public class HibernateListCommand<T> implements Command<List<T>> {

  private static final long serialVersionUID = -522051467458658712L;
  // "hibernate-session:core"
  private final String sessionName;
  private final String query;
  private Map<String, String> stringParameters;

  public HibernateListCommand(final String sessionName, final String query) {
    this.sessionName = sessionName;
    this.query = query;
  }

  protected void addStringParameter(final String key, final String value) {
    if (stringParameters == null) {
      stringParameters = new HashMap<String, String>();
    }
    stringParameters.put(key, value);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<T> execute(final Environment environment) throws Exception {
    final Session session = (Session) environment.get(this.sessionName);

    final Query query = session.createQuery(this.query);
    if (stringParameters != null) {
      for (final Map.Entry<String, String> stringParameter : stringParameters.entrySet()) {
        query.setString(stringParameter.getKey(), stringParameter.getValue());
      }
    }
    query.setCacheable(true);
    final List<T> result = new ArrayList<T>();
    result.addAll(query.list());
    return result;
  }
}
