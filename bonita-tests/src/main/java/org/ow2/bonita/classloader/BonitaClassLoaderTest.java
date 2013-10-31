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
package org.ow2.bonita.classloader;

import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarInputStream;

import org.ow2.bonita.APITestCase;
import org.ow2.bonita.facade.def.element.BusinessArchive;
import org.ow2.bonita.facade.def.element.HookDefinition.Event;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.runtime.TaskInstance;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.runtime.CommonClassLoader;
import org.ow2.bonita.runtime.GetClassloaderClassCommand;
import org.ow2.bonita.runtime.ProcessClassLoader;
import org.ow2.bonita.util.Misc;
import org.ow2.bonita.util.ProcessBuilder;

/**
 * 
 * @author Matthieu Chaffotte
 *
 */
public class BonitaClassLoaderTest extends APITestCase {

  public void testJavaClassInAJarInABar() throws Exception{
    setJavaClassInABar(true);
  }

  public void testJavaClassInABar() throws Exception {
    setJavaClassInABar(false);
  }

  private void setJavaClassInABar(boolean inJar) throws Exception {
    String className = "org.bonitasoft.misc.MyJavaClass";
    String jarName = "MyJavaClass.jar";
    String commandJar = "commands.jar";

    InputStream is = this.getClass().getResourceAsStream(jarName);
    Map<String, byte[]> resources = new HashMap<String, byte[]>();
    if(inJar) {
      byte[] jarFile = Misc.getAllContentFrom(is);
      resources.put(jarName, jarFile);
    } else {
      JarInputStream input = new JarInputStream(is);
      resources = Misc.getJarEntries(input, jarName);
      input.close();
    }
    is.close();

    ProcessDefinition process =
      ProcessBuilder.createProcess("myProcess1", "1.0")
      .addSystemTask("myTask")
      .addHumanTask("human", "john")
      .addHuman("john")
      .done();

    BusinessArchive archive = getBusinessArchive(process, resources);
    process = getManagementAPI().deploy(archive);

    getManagementAPI().deployJar(commandJar, Misc.generateJar(GetClassloaderClassCommand.class));
    GetClassloaderClassCommand command = new GetClassloaderClassCommand(process.getUUID(), className);
    String classloaderClassName = getCommandAPI().execute(command);
    assertEquals(ProcessClassLoader.class.getName(), classloaderClassName);

    getManagementAPI().removeJar(commandJar);
    getManagementAPI().deleteProcess(process.getUUID());
  }

  public void testDeployAJavaClassFromAJar() throws Exception {
    String className = "org.bonitasoft.misc.MyJavaClass";
    String jarName = "MyJavaClass.jar";
    String commandJar = "commands.jar";

    InputStream is = this.getClass().getResourceAsStream(jarName);
    byte[] jar = Misc.getAllContentFrom(is);
    getManagementAPI().deployJar(jarName, jar);
    is.close();
    
    ProcessDefinition process =
      ProcessBuilder.createProcess("myProcess2", "1.0")
      .addSystemTask("myTask")
      .addHumanTask("human", "john")
      .addHuman("john")
      .done();

    BusinessArchive archive = getBusinessArchive(process);
    process = getManagementAPI().deploy(archive);

    getManagementAPI().deployJar(commandJar, Misc.generateJar(GetClassloaderClassCommand.class));
    GetClassloaderClassCommand command = new GetClassloaderClassCommand(process.getUUID(), className);
    String classloaderClassName = getCommandAPI().execute(command);
    assertEquals(CommonClassLoader.class.getName(), classloaderClassName);

    getManagementAPI().removeJar(jarName);
    getManagementAPI().removeJar(commandJar);
    getManagementAPI().deleteProcess(process.getUUID());
  }

  public void testRoleResolverInABar() throws Exception {
    Set<String> expected = new HashSet<String>();
    expected.add("Matti");
    expected.add("Hannu");
    expected.add("Tiina");

    String className = "org.bonitasoft.roleresolver.MyRoleResolver";
    String jarName = "MyRoleResolver.jar";
    String commandJar = "commands.jar";

    InputStream is = this.getClass().getResourceAsStream(jarName);
    Map<String, byte[]> resources = new HashMap<String, byte[]>();
    byte[] jarFile = Misc.getAllContentFrom(is);
    resources.put(jarName, jarFile);
    is.close();

    ProcessDefinition process =
      ProcessBuilder.createProcess("myProcess3", "1.0")
      .addGroup("MyGroup")
        .addGroupResolver(className)
      .addHumanTask("myTask", "MyGroup")
      .done();

    BusinessArchive archive = getBusinessArchive(process, resources);
    process = getManagementAPI().deploy(archive);
    ProcessDefinitionUUID definitionUUID = process.getUUID();

    getManagementAPI().deployJar(commandJar, Misc.generateJar(GetClassloaderClassCommand.class));
    GetClassloaderClassCommand command = new GetClassloaderClassCommand(process.getUUID(), className);
    String classloaderClassName = getCommandAPI().execute(command);
    assertEquals(ProcessClassLoader.class.getName(), classloaderClassName);

    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(definitionUUID);

    Set<TaskInstance> tasks = getQueryRuntimeAPI().getTasks(instanceUUID);
    TaskInstance task = tasks.iterator().next();
    Set<String> actual = task.getTaskCandidates();
    assertEquals(expected, actual);

    getManagementAPI().removeJar(commandJar);
    getManagementAPI().deleteProcess(definitionUUID);
  }

  public void testEmployeeRoleResolverInABar() throws Exception {
    Set<String> expected = new HashSet<String>();
    expected.add("john");
    expected.add("james");
    expected.add("joe");

    String className = "org.bonitasoft.roleresolver.EmployeRoleResolver";
    String employeeClassName = "org.bonitasoft.model.Employee";
    String jarName = "MyEmployeeRoleResolver.jar";
    String employeeJarName = "MyEmployee.jar";
    String commandJar = "commands.jar";

    InputStream is = this.getClass().getResourceAsStream(jarName);
    Map<String, byte[]> resources = new HashMap<String, byte[]>();
    byte[] jarFile = Misc.getAllContentFrom(is);
    resources.put(jarName, jarFile);
    is = this.getClass().getResourceAsStream(employeeJarName);
    jarFile = Misc.getAllContentFrom(is);
    resources.put(employeeJarName, jarFile);
    is.close();

    ProcessDefinition process =
      ProcessBuilder.createProcess("myProcess4", "1.0")
      .addGroup("MyGroup")
        .addGroupResolver(className)
      .addHumanTask("myTask", "MyGroup")
      .done();

    BusinessArchive archive = getBusinessArchive(process, resources);
    process = getManagementAPI().deploy(archive);
    ProcessDefinitionUUID definitionUUID = process.getUUID();

    getManagementAPI().deployJar(commandJar, Misc.generateJar(GetClassloaderClassCommand.class));
    GetClassloaderClassCommand command = new GetClassloaderClassCommand(process.getUUID(), className);
    String classloaderClassName = getCommandAPI().execute(command);
    assertEquals(ProcessClassLoader.class.getName(), classloaderClassName);
    command = new GetClassloaderClassCommand(process.getUUID(), employeeClassName);
    classloaderClassName = getCommandAPI().execute(command);
    assertEquals(ProcessClassLoader.class.getName(), classloaderClassName);

    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(definitionUUID);
    Set<TaskInstance> tasks = getQueryRuntimeAPI().getTasks(instanceUUID);
    TaskInstance task = tasks.iterator().next();
    Set<String> actual = task.getTaskCandidates();
    assertEquals(expected, actual);

    getManagementAPI().removeJar(commandJar);
    getManagementAPI().deleteProcess(definitionUUID);
  }

  public void testAddressEmployeeRoleResolverInABar() throws Exception {
    Set<String> expected = new HashSet<String>();
    expected.add("john");
    expected.add("james");
    expected.add("joe");

    String className = "org.bonitasoft.roleresolver.EmployeRoleResolver";
    String addressClassName = "org.bonitasoft.misc.Address";
    String EmployeeClassName = "org.bonitasoft.model.Employee";
    String jarName = "AddressEmployeeRoleResolver.jar";
    String employeeJarName = "AddressEmployee.jar";
    String addressJarName = "Address.jar";
    String commandJar = "commands.jar";

    InputStream is = this.getClass().getResourceAsStream(jarName);
    Map<String, byte[]> resources = new HashMap<String, byte[]>();
    byte[] jarFile = Misc.getAllContentFrom(is);
    resources.put(jarName, jarFile);
    is = this.getClass().getResourceAsStream(employeeJarName);
    jarFile = Misc.getAllContentFrom(is);
    resources.put(employeeJarName, jarFile);
    is = this.getClass().getResourceAsStream(addressJarName);
    jarFile = Misc.getAllContentFrom(is);
    resources.put(addressJarName, jarFile);
    is.close();

    ProcessDefinition process =
      ProcessBuilder.createProcess("myProcess5", "1.0")
      .addGroup("MyGroup")
        .addGroupResolver(className)
      .addHumanTask("myTask", "MyGroup")
      .done();

    BusinessArchive archive = getBusinessArchive(process, resources);
    process = getManagementAPI().deploy(archive);
    ProcessDefinitionUUID definitionUUID = process.getUUID();

    getManagementAPI().deployJar(commandJar, Misc.generateJar(GetClassloaderClassCommand.class));
    GetClassloaderClassCommand command = new GetClassloaderClassCommand(process.getUUID(), className);
    String classloaderClassName = getCommandAPI().execute(command);
    assertEquals(ProcessClassLoader.class.getName(), classloaderClassName);
    command = new GetClassloaderClassCommand(process.getUUID(), addressClassName);
    classloaderClassName = getCommandAPI().execute(command);
    assertEquals(ProcessClassLoader.class.getName(), classloaderClassName);
    command = new GetClassloaderClassCommand(process.getUUID(), EmployeeClassName);
    classloaderClassName = getCommandAPI().execute(command);
    assertEquals(ProcessClassLoader.class.getName(), classloaderClassName);

    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(definitionUUID);

    Set<TaskInstance> tasks = getQueryRuntimeAPI().getTasks(instanceUUID);
    TaskInstance task = tasks.iterator().next();
    Set<String> actual = task.getTaskCandidates();
    assertEquals(expected, actual);

    getManagementAPI().removeJar(commandJar);
    getManagementAPI().deleteProcess(definitionUUID);
  }

  public void testAddressEmployeeRoleResolverInABarInAJar() throws Exception {
    Set<String> expected = new HashSet<String>();
    expected.add("john");
    expected.add("james");
    expected.add("joe");

    String className = "org.bonitasoft.roleresolver.EmployeRoleResolver";
    String addressClassName = "org.bonitasoft.misc.Address";
    String EmployeeClassName = "org.bonitasoft.model.Employee";
    String jarName = "AddressEmployeeRoleResolver.jar";
    String employeeJarName = "AddressEmployee.jar";
    String addressJarName = "Address.jar";
    String commandJar = "commands.jar";

    InputStream is = this.getClass().getResourceAsStream(jarName);
    Map<String, byte[]> resources = new HashMap<String, byte[]>();
    byte[] jarFile = Misc.getAllContentFrom(is);
    resources.put(jarName, jarFile);
    is = this.getClass().getResourceAsStream(employeeJarName);
    jarFile = Misc.getAllContentFrom(is);
    resources.put(employeeJarName, jarFile);
    is = this.getClass().getResourceAsStream(addressJarName);
    jarFile = Misc.getAllContentFrom(is);
    is.close();
    getManagementAPI().deployJar(addressJarName, jarFile);

    ProcessDefinition process =
      ProcessBuilder.createProcess("myProcess6", "1.0")
      .addGroup("MyGroup")
        .addGroupResolver(className)
      .addHumanTask("myTask", "MyGroup")
      .done();

    BusinessArchive archive = getBusinessArchive(process, resources);
    process = getManagementAPI().deploy(archive);
    ProcessDefinitionUUID definitionUUID = process.getUUID();

    getManagementAPI().deployJar(commandJar, Misc.generateJar(
        GetClassloaderClassCommand.class, GetClassloaderClassCommand.class));
    GetClassloaderClassCommand command = new GetClassloaderClassCommand(process.getUUID(), className);
    String classloaderClassName = getCommandAPI().execute(command);
    assertEquals(ProcessClassLoader.class.getName(), classloaderClassName);
    command = new GetClassloaderClassCommand(process.getUUID(), EmployeeClassName);
    classloaderClassName = getCommandAPI().execute(command);
    assertEquals(ProcessClassLoader.class.getName(), classloaderClassName);
    GetClassloaderClassCommand commonCommand = new GetClassloaderClassCommand(process.getUUID(), addressClassName);
    classloaderClassName = getCommandAPI().execute(commonCommand);
    assertEquals(CommonClassLoader.class.getName(), classloaderClassName);

    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(definitionUUID);
    Set<TaskInstance> tasks = getQueryRuntimeAPI().getTasks(instanceUUID);
    TaskInstance task = tasks.iterator().next();
    Set<String> actual = task.getTaskCandidates();
    assertEquals(expected, actual);

    getManagementAPI().removeJar(commandJar);
    getManagementAPI().deleteProcess(definitionUUID);
    getManagementAPI().removeJar(addressJarName);
  }

  public void testAddressEmployeeInBarAndInJar() throws Exception {
    String className = "org.bonitasoft.model.Employee";
    String employeeJarName = "AddressEmployee.jar";
    String addressJarName = "Address.jar";
    String commandJar = "commands.jar";

    InputStream is = this.getClass().getResourceAsStream(addressJarName);
    byte[] jarFile = Misc.getAllContentFrom(is);
    getManagementAPI().deployJar(addressJarName, jarFile);
    is = this.getClass().getResourceAsStream(employeeJarName);
    jarFile = Misc.getAllContentFrom(is);
    Map<String, byte[]> resources = new HashMap<String, byte[]>();
    resources.put(employeeJarName, jarFile);
    is.close();

    ProcessDefinition process =
      ProcessBuilder.createProcess("myProcess7", "1.0")
      .addGroup("MyGroup")
        .addGroupResolver(className)
      .addHumanTask("myTask", "MyGroup")
      .done();

    BusinessArchive archive = getBusinessArchive(process, resources);
    process = getManagementAPI().deploy(archive);
    ProcessDefinitionUUID definitionUUID = process.getUUID();

    getManagementAPI().deployJar(commandJar, Misc.generateJar(GetClassloaderClassCommand.class));
    GetClassloaderClassCommand command = new GetClassloaderClassCommand(process.getUUID(), className);
    String classloaderClassName = getCommandAPI().execute(command);
    assertEquals(ProcessClassLoader.class.getName(), classloaderClassName);

    getManagementAPI().removeJar(commandJar);
    getManagementAPI().deleteProcess(definitionUUID);
    getManagementAPI().removeJar(addressJarName);
  }

  public void testAddressEmployeeInJars() throws Exception {
    String className = "org.bonitasoft.model.Employee";
    String employeeJarName = "AddressEmployee.jar";
    String addressJarName = "Address.jar";
    String commandJar = "commands.jar";

    InputStream is = this.getClass().getResourceAsStream(addressJarName);
    byte[] jarFile = Misc.getAllContentFrom(is);
    getManagementAPI().deployJar(addressJarName, jarFile);
    is = this.getClass().getResourceAsStream(employeeJarName);
    jarFile = Misc.getAllContentFrom(is);
    getManagementAPI().deployJar(employeeJarName, jarFile);
    is.close();

    ProcessDefinition process =
      ProcessBuilder.createProcess("myProcess8", "1.0")
      .addHuman("john")
      .addHumanTask("myTask", "john")
      .done();

    BusinessArchive archive = getBusinessArchive(process);
    process = getManagementAPI().deploy(archive);
    ProcessDefinitionUUID definitionUUID = process.getUUID();

    getManagementAPI().deployJar(commandJar, Misc.generateJar(GetClassloaderClassCommand.class));
    GetClassloaderClassCommand command = new GetClassloaderClassCommand(process.getUUID(), className);
    String classloaderClassName = getCommandAPI().execute(command);
    assertEquals(CommonClassLoader.class.getName(), classloaderClassName);

    getManagementAPI().removeJar(commandJar);
    getManagementAPI().deleteProcess(definitionUUID);
    getManagementAPI().removeJar(employeeJarName);
    getManagementAPI().removeJar(addressJarName);
  }

  public void testResource() throws Exception {
    String className = "org.bonitasoft.connector.ResourceConnector";
    String resourceJarName = "Resource.jar";
    String commandJar = "commands.jar";

    InputStream is = this.getClass().getResourceAsStream(resourceJarName);
    byte[] jarFile = Misc.getAllContentFrom(is);
    getManagementAPI().deployJar(resourceJarName, jarFile);
    is.close();

    ProcessDefinition process =
      ProcessBuilder.createProcess("myProcess9", "1.0")
      .addObjectData("value", URL.class.getName())
      .addHuman("john")
      .addHumanTask("myTask", "john")
        .addConnector(Event.taskOnReady, className, true)
          .addInputParameter("resourcePath", "bonita.i18n")
          .addOutputParameter("${uRL}", "value")
      .done();

    BusinessArchive archive = getBusinessArchive(process);
    process = getManagementAPI().deploy(archive);
    ProcessDefinitionUUID definitionUUID = process.getUUID();

    getManagementAPI().deployJar(commandJar, Misc.generateJar(GetClassloaderClassCommand.class));
    GetClassloaderClassCommand command = new GetClassloaderClassCommand(process.getUUID(), className);
    String classloaderClassName = getCommandAPI().execute(command);
    assertEquals(CommonClassLoader.class.getName(), classloaderClassName);
    
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(process.getUUID());
    URL actual = (URL) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "value");
    assertNotNull(actual);

    getManagementAPI().removeJar(commandJar);
    getManagementAPI().deleteProcess(definitionUUID);
    getManagementAPI().removeJar(resourceJarName);
  }

  public void testResourceInAnotherJar() throws Exception {
    String className = "org.bonitasoft.connector.ResourceConnector";
    String resourceJarName = "Resource.jar";
    String otherResourceJarName = "OtherResource.jar";
    String commandJar = "commands.jar";

    InputStream is = this.getClass().getResourceAsStream(resourceJarName);
    byte[] jarFile = Misc.getAllContentFrom(is);
    getManagementAPI().deployJar(resourceJarName, jarFile);
    is = this.getClass().getResourceAsStream(otherResourceJarName);
    jarFile = Misc.getAllContentFrom(is);
    getManagementAPI().deployJar(otherResourceJarName, jarFile);
    is.close();

    ProcessDefinition process =
      ProcessBuilder.createProcess("myProcess10", "1.0")
      .addObjectData("value", URL.class.getName())
      .addHuman("john")
      .addHumanTask("myTask", "john")
        .addConnector(Event.taskOnReady, className, true)
          .addInputParameter("resourcePath", "/org/bonitasoft/other/Other.i18n")
          .addOutputParameter("${uRL}", "value")
      .done();

    BusinessArchive archive = getBusinessArchive(process);
    process = getManagementAPI().deploy(archive);
    ProcessDefinitionUUID definitionUUID = process.getUUID();

    getManagementAPI().deployJar(commandJar, Misc.generateJar(GetClassloaderClassCommand.class));
    GetClassloaderClassCommand command = new GetClassloaderClassCommand(process.getUUID(), className);
    String classloaderClassName = getCommandAPI().execute(command);
    assertEquals(CommonClassLoader.class.getName(), classloaderClassName);

    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(process.getUUID());
    URL actual = (URL) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "value");
    assertNotNull(actual);

    getManagementAPI().removeJar(commandJar);
    getManagementAPI().deleteProcess(definitionUUID);
    getManagementAPI().removeJar(resourceJarName);
    getManagementAPI().removeJar(otherResourceJarName);
  }
 
}
