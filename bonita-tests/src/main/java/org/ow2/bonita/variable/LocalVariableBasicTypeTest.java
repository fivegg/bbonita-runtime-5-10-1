/**
 * Copyright (C) 2007  Bull S. A. S.
 * Bull, Rue Jean Jaures, B.P.68, 78340, Les Clayes-sous-Bois
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
package org.ow2.bonita.variable;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.ow2.bonita.APITestCase;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.util.Misc;
import org.ow2.bonita.util.ProcessBuilder;

/**
 * @author Guillaume Porcher
 *
 */
public class LocalVariableBasicTypeTest extends APITestCase {

  public void testObjectVariableType() throws Exception {
    final String myObjectClassName = "org.ow2.bonita.variable.MyBonitaBeanForVariables";

    ProcessDefinition p = ProcessBuilder.createProcess("objectProcess", "1.0")
    .addObjectData("bean", Object.class.getName(), "${new " + myObjectClassName + "(\"init\")}")
    .addHuman("admin")
    .addHumanTask("task", "admin")
    .addStringData("test", "test")
    .done();

    final String jarFileName = "MyBonitaBeanForVariables.jar";
    final byte[] jarContent =  Misc.getAllContentFrom(this.getClass().getResource(jarFileName));
    Map<String, byte[]> resources = new HashMap<String, byte[]>();
    resources.put(jarFileName,jarContent);

    ProcessDefinition process = getManagementAPI().deploy(getBusinessArchive(p, resources));
    ProcessDefinitionUUID processUUID = process.getUUID();

    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    File tmpFile = File.createTempFile(UUID.randomUUID().toString(), ".jar");
    Misc.write(tmpFile, Misc.getAllContentFrom(this.getClass().getResource(jarFileName)));
    
    final URL[] urls = new URL[1];
    urls[0] = tmpFile.toURI().toURL();
    
    URLClassLoader myClassloader = new URLClassLoader(urls, this.getClass().getClassLoader());
    final ClassLoader ori = Thread.currentThread().getContextClassLoader();
    try {
      Thread.currentThread().setContextClassLoader(myClassloader);
      
      final Class< ? > myObjectClass = myClassloader.loadClass(myObjectClassName);
      final Object myUpdatedObject = myObjectClass.getDeclaredConstructor(String.class).newInstance("updated");
      assertEquals("init", getRuntimeAPI().evaluateGroovyExpression("${bean.getStr()}", instanceUUID, false));
      getRuntimeAPI().setProcessInstanceVariable(instanceUUID, "bean", myUpdatedObject);
      assertEquals("updated", getRuntimeAPI().evaluateGroovyExpression("${bean.getStr()}", instanceUUID, false));
      final Object foundObject = getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "bean");
      assertEquals(myObjectClassName, foundObject.getClass().getName());
      final String str = (String) foundObject.getClass().getDeclaredMethod("getStr", (Class[])null).invoke(foundObject, (Object[])null);
      assertEquals("updated", str);

    } finally {
      tmpFile.delete();
      Thread.currentThread().setContextClassLoader(ori);
    }
    getManagementAPI().deleteProcess(processUUID);

  }
  
  
}
