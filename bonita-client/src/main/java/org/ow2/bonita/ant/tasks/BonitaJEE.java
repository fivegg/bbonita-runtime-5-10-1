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
package org.ow2.bonita.ant.tasks;

import java.io.File;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.CommandlineJava;
import org.apache.tools.ant.types.Environment;
import org.ow2.bonita.util.BonitaConstants;
import org.ow2.bonita.util.ExceptionManager;

public class BonitaJEE extends BonitaJava {

  protected String initialcontextfactory;
  protected String providerurl;

  @Override
  public void execute() {
    if (initialcontextfactory == null) {
    	String message = ExceptionManager.getInstance().getFullMessage("bat_BJEE_1");
      throw new BuildException(message);
    }
    if (providerurl == null) {
    	String message = ExceptionManager.getInstance().getFullMessage("bat_BJEE_2");
      throw new BuildException(message);
    }
    CommandlineJava java = getCommandLine();
    
    if (initialcontextfactory != null) {
      Environment.Variable var = new Environment.Variable();
      var.setKey(BonitaConstants.INITIAL_CONTEXT_FACTORY_PROPERTY);
      var.setValue(initialcontextfactory);
      java.addSysproperty(var);
    }

    if (providerurl != null) {
      Environment.Variable var = new Environment.Variable();
      var.setKey(BonitaConstants.PROVIDER_URL_PROPERTY);
      var.setValue(providerurl);
      java.addSysproperty(var);
    }

    super.execute();
  }

  public void setInitialcontextfactory(String initialcontextfactory) {
    this.initialcontextfactory = initialcontextfactory;
  }

  public void setProviderurl(String providerurl) {
    this.providerurl = providerurl;
  }

  public void setEnv(File env) {
  	String message = ExceptionManager.getInstance().getFullMessage("bat_BJEE_3");
    throw new BuildException(message);
  }
}
