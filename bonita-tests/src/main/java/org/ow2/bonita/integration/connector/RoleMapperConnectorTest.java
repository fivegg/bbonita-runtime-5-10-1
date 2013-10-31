/**
 * Copyright (C) 2009  BonitaSoft S.A..
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
package org.ow2.bonita.integration.connector;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;

import javax.security.auth.login.LoginException;

import junit.framework.Assert;

import org.ow2.bonita.APITestCase;
import org.ow2.bonita.facade.exception.InstanceNotFoundException;
import org.ow2.bonita.facade.runtime.ActivityState;
import org.ow2.bonita.facade.runtime.TaskInstance;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;

/**
 * @author Matthieu Chaffotte
 *
 */
public class RoleMapperConnectorTest extends APITestCase {

	protected void checkAdminAndCustomerTasks(ProcessInstanceUUID instanceUUID) throws LoginException, InstanceNotFoundException {
    loginAs("admin", "bpm");
    Collection<TaskInstance> taskActivities = 
      getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.READY);
    Assert.assertEquals(0, taskActivities.size());
    loginAs("john", "bpm");
    taskActivities = 
      getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.READY);
    Assert.assertEquals(1, taskActivities.size());
  }

	protected File prepareFile(String xpdlFile, String fileName) {
    StringBuilder pathBuilder = new StringBuilder();
    String tmpDir = System.getProperty("java.io.tmpdir");
    pathBuilder.append(tmpDir);
    if (!(tmpDir.endsWith("/") || tmpDir.endsWith("\\"))) {
      pathBuilder.append(File.separator);
    }
    pathBuilder.append(xpdlFile);

    URL xpdlUrl = getClass().getResource(xpdlFile);
    URL ldap = getClass().getResource(fileName);

    File file = new File(pathBuilder.toString());
    try {
      BufferedReader reader =
        new BufferedReader(new FileReader(xpdlUrl.getPath()));
      BufferedWriter writer =
        new BufferedWriter(new FileWriter(file));
      String line = reader.readLine();
      while (line != null) {
        if (line.contains("@PATH_TO_PROPERTIES_FILE@")) {
          line = line.replace("@PATH_TO_PROPERTIES_FILE@", ldap.getPath());
        }
        writer.write(line);
        writer.newLine();
        line = reader.readLine();
      }
      reader.close();
      writer.close();
    } catch (FileNotFoundException e1) {
      e1.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return file;
  }
}
