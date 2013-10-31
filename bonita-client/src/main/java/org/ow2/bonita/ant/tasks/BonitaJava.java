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
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.types.CommandlineJava;
import org.apache.tools.ant.types.Environment;
import org.ow2.bonita.facade.Context;
import org.ow2.bonita.util.BonitaConstants;
import org.ow2.bonita.util.ExceptionManager;
import org.ow2.bonita.util.Misc;

public class BonitaJava extends Java {

  private String loginmode;
  private String apitype;
  private File jaas;
  private File env;
  private File log;
  private File bar;

  @Override
  public void execute() {
    if (bar == null) {
    	String message = ExceptionManager.getInstance().getFullMessage("bat_BJ_1");
      throw new BuildException(message);
    }

    CommandlineJava java = getCommandLine();
    
    java.createArgument().setValue("-bar=" + bar.getAbsolutePath());
    
    Environment.Variable testVar = new Environment.Variable();
    testVar.setKey(BonitaConstants.LOGIN_MODE_PROPERTY);
    testVar.setValue(loginmode);
    java.addSysproperty(testVar);

    if (env != null) {
      Environment.Variable var = new Environment.Variable();
      var.setKey(BonitaConstants.ENVIRONMENT_PROPERTY);
      var.setValue(env.getAbsolutePath());
      java.addSysproperty(var);
    }

    if (jaas != null) {
      Environment.Variable var = new Environment.Variable();
      var.setKey(BonitaConstants.JAAS_PROPERTY);
      var.setValue(jaas.getAbsolutePath());
      java.addSysproperty(var);
    }

    if (log != null) {
      Environment.Variable var = new Environment.Variable();
      var.setKey(BonitaConstants.LOGGING_PROPERTY);
      var.setValue(log.getAbsolutePath());
      java.addSysproperty(var);
    }

    if (apitype != null) {
      Environment.Variable var = new Environment.Variable();
      var.setKey(BonitaConstants.API_TYPE_PROPERTY);
      var.setValue(Misc.stringToEnum(Context.class, apitype).toString());
      java.addSysproperty(var);
    }
    
    try {
      super.execute();
    } catch (Throwable t) {
      t.printStackTrace();
      log(Misc.getStackTraceFrom(t), Project.MSG_ERR);
    }
  }

  public void setJaas(File jaas) {
    this.jaas = jaas;
  }

  public void setEnv(File env) {
    this.env = env;
  }

  public void setLog(File log) {
    this.log = log;
  }

  public void setApitype(String apitype) {
    this.apitype = apitype;
  }

  public void setLoginmode(String loginmode) {
    this.loginmode = loginmode;
  }

  public void setBar(File bar) {
    this.bar = bar;
  }

}
