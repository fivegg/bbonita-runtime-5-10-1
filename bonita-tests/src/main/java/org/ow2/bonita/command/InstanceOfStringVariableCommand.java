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
package org.ow2.bonita.command;

import org.ow2.bonita.env.Environment;
import org.ow2.bonita.type.Variable;
import org.ow2.bonita.type.variable.StringVariable;
import org.ow2.bonita.util.Command;
import org.ow2.bonita.util.VariableUtil;

/**
 * 
 * @author Matthieu Chaffotte
 *
 */
public class InstanceOfStringVariableCommand implements Command<Boolean> {

  private static final long serialVersionUID = 2119802426227646219L;

  public Boolean execute(Environment environment) throws Exception {
    final Variable v = VariableUtil.createVariable(null, "string", "string");
    return v instanceof StringVariable;
  }

}
