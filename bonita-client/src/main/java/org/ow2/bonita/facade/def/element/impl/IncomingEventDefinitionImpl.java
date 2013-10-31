/**
 * Copyright (C) 2009-2012 BonitaSoft S.A.
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
package org.ow2.bonita.facade.def.element.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.ow2.bonita.facade.def.element.ConnectorDefinition;
import org.ow2.bonita.facade.def.element.HookDefinition;
import org.ow2.bonita.facade.def.element.IncomingEventDefinition;

/**
 * 
 * @author Matthieu Chaffotte
 *
 */
public class IncomingEventDefinitionImpl extends EventDefinitionImpl implements IncomingEventDefinition {

  private static final long serialVersionUID = -8085232103955646778L;

  protected long dbid;

	protected List<ConnectorDefinition> connectors;

	protected String expression;

	public IncomingEventDefinitionImpl() {
	  super();
	}

	public IncomingEventDefinitionImpl(final String name, final String expression) {
	  super(name);
		this.expression = expression;
	}

	public IncomingEventDefinitionImpl(final IncomingEventDefinition src) {
	  super(src);
		List<ConnectorDefinition> srcConnectors = src.getConnectors();
		if (srcConnectors != null) {
			this.connectors = new ArrayList<ConnectorDefinition>();
			for (ConnectorDefinition connector : srcConnectors) {
				this.connectors.add(new ConnectorDefinitionImpl(connector));
			}
		}
		this.expression = src.getExpression();
	}

	public List<ConnectorDefinition> getConnectors() {
	  if (this.connectors == null) {
	    return Collections.emptyList();
	  }
		return connectors;
	}

	public String getExpression() {
	  return expression;
	}

	public void addConnector(final HookDefinition connector) {
		if (connectors == null) {
      connectors = new ArrayList<ConnectorDefinition>();
    }
    this.connectors.add(connector);
	}

}
