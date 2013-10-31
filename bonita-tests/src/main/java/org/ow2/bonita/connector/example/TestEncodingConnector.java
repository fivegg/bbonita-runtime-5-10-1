/**
 * Copyright (C) 2010 BonitaSoft S.A.
 * BonitaSoft, 31 rue Gustave Eiffel - 38000 Grenoble
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.ow2.bonita.connector.example;

import java.util.List;

import org.ow2.bonita.connector.core.Connector;
import org.ow2.bonita.connector.core.ConnectorError;

/**
 * @author Mickael Istria
 * An Empty connector intended to test the encoding of connector description
 */
public class TestEncodingConnector extends Connector {

	/* (non-Javadoc)
	 * @see org.ow2.bonita.connector.core.Connector#executeConnector()
	 */
	@Override
	protected void executeConnector() throws Exception {
	}

	/* (non-Javadoc)
	 * @see org.ow2.bonita.connector.core.Connector#validateValues()
	 */
	@Override
	protected List<ConnectorError> validateValues() {
		return null;
	}

}
