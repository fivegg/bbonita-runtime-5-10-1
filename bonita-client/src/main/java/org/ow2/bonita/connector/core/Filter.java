/**
 * Copyright (C) 2009  BonitaSoft S.A.
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
package org.ow2.bonita.connector.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * A Filter chooses candidates among members. Candidates can execute a task.
 * @author Matthieu Chaffotte
 *
 */
public abstract class Filter extends Mapper {

  private Set<String> candidates;

  public Set<String> getCandidates() {
		return candidates;
	}

	protected abstract Set<String> getCandidates(Set<String> members) throws Exception;

  @Override
  protected final void executeConnector() throws Exception {
  	candidates = getCandidates(getMembers());
  }

  @Override
  protected List<ConnectorError> validateValues() {
    List<ConnectorError> errors = new ArrayList<ConnectorError>();
    if (getMembers().isEmpty()) {
      errors.add(new ConnectorError("members",
          new IllegalArgumentException("cannot be empty")));
    }
    return errors;
  }
}
