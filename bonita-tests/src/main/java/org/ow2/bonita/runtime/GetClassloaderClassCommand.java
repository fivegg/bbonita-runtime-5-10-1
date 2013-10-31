/**
 * Copyright (C) 2010  BonitaSoft S.A..
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
package org.ow2.bonita.runtime;

import org.ow2.bonita.env.Environment;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.util.Command;
import org.ow2.bonita.util.EnvTool;

/**
 * 
 * @author Matthieu Chaffotte
 *
 */
public class GetClassloaderClassCommand implements Command<String> {

  private static final long serialVersionUID = 1L;
  private ProcessDefinitionUUID processUUID;
  private String className;

  public GetClassloaderClassCommand(ProcessDefinitionUUID definitionUUID, String className) {
    this.processUUID = definitionUUID;
    this.className = className;
  }

  public String execute(Environment environment) throws Exception {
    Class<?> classe = EnvTool.getClassDataLoader().getClass(processUUID, className);
    return classe.getClassLoader().getClass().getName();
  }

}