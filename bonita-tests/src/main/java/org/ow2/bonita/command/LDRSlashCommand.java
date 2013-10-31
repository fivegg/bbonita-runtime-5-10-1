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

import java.io.InputStream;

import org.ow2.bonita.env.Environment;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.util.Command;
import org.ow2.bonita.util.EnvTool;

public class LDRSlashCommand implements Command<Boolean> {

  private static final long serialVersionUID = 8381222233145638608L;
  private String resourceName;
  private ProcessDefinitionUUID processUUID;

  public LDRSlashCommand(String resourceName, ProcessDefinitionUUID processUUID) {
    super();
    this.resourceName = resourceName;
    this.processUUID = processUUID;
  }

  public Boolean execute(Environment environment) throws Exception {
    ClassLoader cl = EnvTool.getClassDataLoader().getProcessClassLoader(processUUID);
    InputStream is = cl.getResourceAsStream(resourceName);
    Boolean exist = is != null;
    is.close();
    return exist;
  }

}
