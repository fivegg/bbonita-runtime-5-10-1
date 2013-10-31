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

import java.io.IOException;
import java.io.NotSerializableException;
import java.net.URL;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.bonitasoft.connectors.bonita.SetVarConnector;
import org.ow2.bonita.APITestCase;
import org.ow2.bonita.facade.def.majorElement.DataFieldDefinition;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.runtime.ActivityInstance;
import org.ow2.bonita.facade.runtime.ActivityState;
import org.ow2.bonita.facade.runtime.InstanceState;
import org.ow2.bonita.facade.runtime.ProcessInstance;
import org.ow2.bonita.facade.runtime.impl.ObjectVariable;
import org.ow2.bonita.facade.uuid.ActivityInstanceUUID;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.light.LightTaskInstance;
import org.ow2.bonita.util.BonitaException;
import org.ow2.bonita.util.BonitaRuntimeException;
import org.ow2.bonita.util.DateUtil;
import org.ow2.bonita.util.ProcessBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * @author Guillaume Porcher
 *
 */
public class VariableBasicTypeTest extends APITestCase {

  public void testStringVariable() throws Exception {
    URL xpdlUrl = ActivityVariableTest.class.getResource("basicTypeVariables.xpdl");
    ProcessDefinition process = getManagementAPI().deploy(getBusinessArchiveFromXpdl(xpdlUrl));

    ProcessDefinitionUUID processUUID = process.getUUID();

    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    // initial value
    String str = (String) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "string");
    assertNotNull(str);
    assertEquals("initial value", str);

    // set  with valid value
    getRuntimeAPI().setProcessInstanceVariable(instanceUUID, "string", "new value");

    str = (String) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "string");
    assertNotNull(str);
    assertEquals("new value", str);

    // check that the value has not changed 
    str = (String) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "string");
    assertNotNull(str);
    assertEquals("new value", str);


    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testBooleanVariable() throws Exception {
    URL xpdlUrl = ActivityVariableTest.class.getResource("basicTypeVariables.xpdl");
    ProcessDefinition process = getManagementAPI().deploy(getBusinessArchiveFromXpdl(xpdlUrl));

    ProcessDefinitionUUID processUUID = process.getUUID();

    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    // initial value
    Boolean bool = (Boolean) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "bool");
    assertNotNull(bool);
    assertTrue(bool);

    // set  with valid value
    getRuntimeAPI().setProcessInstanceVariable(instanceUUID, "bool", Boolean.FALSE);

    bool = (Boolean) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "bool");
    assertNotNull(bool);
    assertFalse(bool);

    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testFloatVariable() throws Exception {
    URL xpdlUrl = ActivityVariableTest.class.getResource("basicTypeVariables.xpdl");
    ProcessDefinition process = getManagementAPI().deploy(getBusinessArchiveFromXpdl(xpdlUrl));

    ProcessDefinitionUUID processUUID = process.getUUID();

    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    // initial value
    Double floatVar = (Double) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "float_var");
    assertNotNull(floatVar);
    assertEquals(42.42, floatVar);

    // set with valid value
    // don't use Math.PI because some db have a lower precision than java) 
    double testValue = 3.14159265358979;
    getRuntimeAPI().setProcessInstanceVariable(instanceUUID, "float_var", testValue);

    floatVar = (Double) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "float_var");

    assertNotNull(floatVar);
    assertEquals(testValue, floatVar);

    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testIntegerVariable() throws Exception {
    URL xpdlUrl = ActivityVariableTest.class.getResource("basicTypeVariables.xpdl");
    ProcessDefinition process = getManagementAPI().deploy(getBusinessArchiveFromXpdl(xpdlUrl));

    ProcessDefinitionUUID processUUID = process.getUUID();

    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    // initial value
    Long intVar = (Long) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "integer");
    assertNotNull(intVar);
    assertEquals(42L, intVar.intValue());

    // set  with valid value
    getRuntimeAPI().setProcessInstanceVariable(instanceUUID, "integer", 28L);

    intVar = (Long) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "integer");
    assertNotNull(intVar);
    assertEquals(28L, intVar.intValue());

    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testDatetimeVariable() throws Exception {
    URL xpdlUrl = ActivityVariableTest.class.getResource("basicTypeVariables.xpdl");
    ProcessDefinition process = getManagementAPI().deploy(getBusinessArchiveFromXpdl(xpdlUrl));

    ProcessDefinitionUUID processUUID = process.getUUID();

    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    // initial value
    Date dateVar = (Date) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "datetime");
    assertNotNull(dateVar);
    assertEquals(DateUtil.parseDate("2008/07/31/14/00/00"), dateVar);

    // set  with valid value
    Date newDate = new Date();
    getRuntimeAPI().setProcessInstanceVariable(instanceUUID, "datetime", newDate);

    dateVar = (Date) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "datetime");
    assertNotNull(dateVar);
    assertEquals(newDate, dateVar);

    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testPerformerVariable() throws Exception {
    URL xpdlUrl = ActivityVariableTest.class.getResource("basicTypeVariables.xpdl");
    ProcessDefinition process = getManagementAPI().deploy(getBusinessArchiveFromXpdl(xpdlUrl));

    ProcessDefinitionUUID processUUID = process.getUUID();

    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    // initial value
    String performer = (String) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "performer");
    assertNotNull(performer);
    assertEquals("user", performer);

    // set  with valid value
    getRuntimeAPI().setProcessInstanceVariable(instanceUUID, "performer", "user2");

    performer = (String) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "performer");
    assertNotNull(performer);
    assertEquals("user2", performer);

    // check that the value has not changed 
    performer = (String) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "performer");
    assertNotNull(performer);
    assertEquals("user2", performer);


    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testVariableTypes() throws BonitaException, NumberFormatException, NotSerializableException, IOException, ClassNotFoundException, ParserConfigurationException {
    //all types, conversion, long string, wrong enum

    Document initialDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
    Element el = initialDoc.createElement("el");
    el.setAttribute("myAttribute", "myValue");
    initialDoc.appendChild(el);

    Document newDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
    Element newEl = newDoc.createElement("el");
    newEl.setAttribute("myAttribute", "myNewValue");
    newDoc.appendChild(newEl);

    Set<String> enumeariontValues = new HashSet<String>();
    enumeariontValues.add("1");
    enumeariontValues.add("2");

    MyObject myObject = new MyObject("test");
    Date date = new Date();

    String str254 = generateString(254, 'a');
    String str254_2 = generateString(254, 'z');
    String str255 = generateString(255, 'b');
    String str255_2 = generateString(255, 'y');
    String str256 = generateString(256, 'c');
    String str256_2 = generateString(256, 'x');
    String strMax = generateString(65520, 'd');
    String strMax_2 = generateString(65520, 'w');


    ProcessDefinition p = ProcessBuilder.createProcess("p", "1.0")
    .addBooleanData("bool", true)
    .addCharData("char", 'c')
    .addDateData("date", date)
    .addDoubleData("double", 2.0)
    .addFloatData("float", new Float(1.0))
    .addIntegerData("integer", 3)
    .addLongData("long", 28L)
    .addShortData("short", new Short("1"))
    .addStringData("string", "s")
    .addStringData("str254", str254)
    .addStringData("str255", str255)
    .addStringData("str256", str256)
    .addStringData("strMax", strMax)
    .addEnumData("enum", enumeariontValues, "1")
    .addObjectData("doc", Document.class.getName())
    .addObjectData("doc2", Document.class.getName(), initialDoc)
    .addObjectData("myObject", ObjectVariable.class.getName())
    .addObjectData("myObject2", ObjectVariable.class.getName(), new ObjectVariable(myObject))
    .addHuman("admin")
    .addHumanTask("task", "admin")
    .done();

    ProcessDefinition process = getManagementAPI().deploy(getBusinessArchive(p, null, MyObject.class));
    ProcessDefinitionUUID processUUID = process.getUUID();

    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    checkVariable(instanceUUID, "bool", true, false, Boolean.class);
    checkVariable(instanceUUID, "char", 'c', 'd', Character.class);
    checkVariable(instanceUUID, "date", date, new Date(), Date.class);
    checkVariable(instanceUUID, "double", 2.0, 3.0, Double.class);
    checkVariable(instanceUUID, "float", new Float(1.0), new Float(2.0), Float.class);
    checkVariable(instanceUUID, "integer", 3, 4, Integer.class);
    checkVariable(instanceUUID, "long", 28L, 29L, Long.class);
    checkVariable(instanceUUID, "short", new Short("1"), new Short("2"), Short.class);
    checkVariable(instanceUUID, "string", "s", "t", String.class);
    checkVariable(instanceUUID, "str254", str254, str254_2, String.class);
    checkVariable(instanceUUID, "str255", str255, str255_2, String.class);
    checkVariable(instanceUUID, "str256", str256, str256_2, String.class);
    checkVariable(instanceUUID, "strMax", strMax, strMax_2, String.class);
    checkVariable(instanceUUID, "enum", "1", "2", String.class);
    checkVariable(instanceUUID, "myObject", null, new ObjectVariable(new MyObject("abc")), ObjectVariable.class);
    checkVariable(instanceUUID, "myObject2", new ObjectVariable(myObject), new ObjectVariable(new MyObject("def")), ObjectVariable.class);
    checkVariable(instanceUUID, "doc", null, newDoc, Document.class);
    checkVariable(instanceUUID, "doc2", initialDoc, newDoc, Document.class);

    getManagementAPI().deleteProcess(processUUID);

  }

  public void testEnumerationValues() throws BonitaException {
    Set<String> enumeariontValues = new HashSet<String>();
    enumeariontValues.add("1");
    enumeariontValues.add("2");

    ProcessDefinition p = ProcessBuilder.createProcess("p", "1.0")
    .addEnumData("enum", enumeariontValues, "1")
    .addHuman("admin")
    .addHumanTask("task", "admin")
    .done();

    ProcessDefinition process = getManagementAPI().deploy(getBusinessArchive(p, null, MyObject.class));
    ProcessDefinitionUUID processUUID = process.getUUID();

    Set<DataFieldDefinition> datafields = process.getDataFields();
    assertNotNull(datafields);
    assertEquals(1, datafields.size());

    DataFieldDefinition dataFieldDefinition = datafields.iterator().next();
    assertEquals("enum", dataFieldDefinition.getName());
    assertEquals(String.class.getName(), dataFieldDefinition.getDataTypeClassName());
    assertTrue(dataFieldDefinition.isEnumeration());
    assertEquals(enumeariontValues, dataFieldDefinition.getEnumerationValues());

    getManagementAPI().deleteProcess(processUUID);
  }

  public void testHugeEnumerationValues() throws BonitaException {
    Set<String> berries = new HashSet<String>();
    berries.add("Barberries");
    berries.add("Bearberries");
    berries.add("Blackberries");
    berries.add("Blueberries");
    berries.add("Boysenberries");
    berries.add("Chokeberries");
    berries.add("Cloudberries");
    berries.add("Cranberries");
    berries.add("Elderberries");
    berries.add("Gooseberries");
    berries.add("Huckleberries");
    berries.add("Juneberries");
    berries.add("Juniperberries");
    berries.add("Lingonberries");
    berries.add("Loganberries");
    berries.add("Marionberries");
    berries.add("Mulberries");
    berries.add("Nannyberries");
    berries.add("Ollaliberries");
    berries.add("Salmonberries");
    berries.add("Strawberries");
    berries.add("Raspberries");
    berries.add("Tayberries");
    

    ProcessDefinition p = ProcessBuilder.createProcess("p", "1.0")
    .addEnumData("enum", berries, "1")
    .addHuman("admin")
    .addHumanTask("task", "admin")
    .done();

    ProcessDefinition process = getManagementAPI().deploy(getBusinessArchive(p, null, MyObject.class));
    ProcessDefinitionUUID processUUID = process.getUUID();

    Set<DataFieldDefinition> datafields = process.getDataFields();
    assertNotNull(datafields);
    assertEquals(1, datafields.size());

    DataFieldDefinition dataFieldDefinition = datafields.iterator().next();
    assertEquals("enum", dataFieldDefinition.getName());
    assertEquals(String.class.getName(), dataFieldDefinition.getDataTypeClassName());
    assertTrue(dataFieldDefinition.isEnumeration());
    assertEquals(berries, dataFieldDefinition.getEnumerationValues());

    getManagementAPI().deleteProcess(processUUID);
  }

  public void testObjectVariableType() throws Exception {
    Employee initialEmployee = new Employee("john", "doe");
    Address initialAddress = new Address("2", "John Kennedy", 98765);
    initialEmployee.setAddress(initialAddress);

    Exception o1 = new Exception("e");
    RuntimeException o2 = new RuntimeException("re");
    if (o1.equals(o2)) {
      fail("o1 and o2 are equals, the test is not well witten");
    }

    ProcessDefinition p = ProcessBuilder.createProcess("objectProcess", "1.0")
    .addStringData("test", "test")
    .addObjectData("employee", ObjectVariable.class.getName(), new ObjectVariable(initialEmployee))
    .addObjectData("nullEmployee", ObjectVariable.class.getName())
    .addObjectData("object", Object.class.getName(), o1)
    .addHuman("admin")
    .addHumanTask("task", "admin")
    .addStringData("test", "test")
    .addObjectData("employee", ObjectVariable.class.getName(), new ObjectVariable(initialEmployee))
    .done();

    ProcessDefinition process = getManagementAPI().deploy(getBusinessArchive(p, null, Employee.class, Address.class));
    ProcessDefinitionUUID processUUID = process.getUUID();

    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    Object employee = getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "employee");
    assertEmployeesEquals(initialEmployee, employee);

    assertEquals(o1.getMessage(), ((Exception)getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "object")).getMessage());
    getRuntimeAPI().setProcessInstanceVariable(instanceUUID, "object", o2);
    assertEquals(o2.getMessage(), ((Exception)getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "object")).getMessage());

    ActivityInstanceUUID activityUUID = getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.READY).iterator().next().getUUID();
    employee = getQueryRuntimeAPI().getActivityInstanceVariable(activityUUID, "employee");

    assertEmployeesEquals(initialEmployee, employee);

    Employee newEmployee = new Employee("a", "b");
    Address newAddress = new Address("12", "street", 234);
    newEmployee.setAddress(newAddress);

    getRuntimeAPI().setProcessInstanceVariable(instanceUUID, "employee", new ObjectVariable(newEmployee));
    employee = getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "employee");
    assertEmployeesEquals(newEmployee, employee);

    getRuntimeAPI().setActivityInstanceVariable(activityUUID, "employee", new ObjectVariable(newEmployee));
    employee = getQueryRuntimeAPI().getActivityInstanceVariable(activityUUID, "employee");
    assertEmployeesEquals(newEmployee, employee);

    newEmployee.setResponsabilities(getResponsabilities(5000));

    getRuntimeAPI().setProcessInstanceVariable(instanceUUID, "employee", new ObjectVariable(newEmployee));
    employee = getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "employee");
    assertEmployeesEquals(newEmployee, employee);

    getRuntimeAPI().setActivityInstanceVariable(activityUUID, "employee", new ObjectVariable(newEmployee));
    employee = getQueryRuntimeAPI().getActivityInstanceVariable(activityUUID, "employee");
    assertEmployeesEquals(newEmployee, employee);

    assertEquals(newEmployee.getFirstName(), getRuntimeAPI().evaluateGroovyExpression("${employee.firstName}", instanceUUID, true));
    assertEquals(newEmployee.getAddress().getZipCode(), getRuntimeAPI().evaluateGroovyExpression("${employee.address.zipCode}", instanceUUID, true));

    assertEquals(newEmployee.getFirstName(), getRuntimeAPI().evaluateGroovyExpression("${employee.firstName}", activityUUID, true, true));
    assertEquals(newEmployee.getAddress().getZipCode(), getRuntimeAPI().evaluateGroovyExpression("${employee.address.zipCode}", activityUUID, true, true));

    getRuntimeAPI().evaluateGroovyExpression("${boolean b=test==null;test=\"new\"}", instanceUUID, true);
    assertEquals("new", getRuntimeAPI().evaluateGroovyExpression("${test}", instanceUUID, true));

    getRuntimeAPI().evaluateGroovyExpression("${boolean b=test==null;test=\"new\"}", activityUUID, true, true);
    assertEquals("new", getRuntimeAPI().evaluateGroovyExpression("${test}", activityUUID, true, true));

    assertEquals(234, getRuntimeAPI().evaluateGroovyExpression("${employee.address.zipCode}", activityUUID, true, true));

    assertEquals(234, getRuntimeAPI().evaluateGroovyExpression("${employee.address.zipCode}", instanceUUID, true));
    getRuntimeAPI().evaluateGroovyExpression("${employee.address.setZipCode(999)}", instanceUUID, true);
    assertEquals(999, getRuntimeAPI().evaluateGroovyExpression("${employee.address.zipCode}", instanceUUID, true));

    assertEquals(234, getRuntimeAPI().evaluateGroovyExpression("${employee.address.zipCode}", activityUUID, true, true));
    getRuntimeAPI().evaluateGroovyExpression("${employee.address.setZipCode(999)}", activityUUID, true, true);
    assertEquals(999, getRuntimeAPI().evaluateGroovyExpression("${employee.address.zipCode}", activityUUID, true, true));

    getRuntimeAPI().evaluateGroovyExpression("${employee = null}", instanceUUID, true);
    assertNull(getRuntimeAPI().evaluateGroovyExpression("${employee}", instanceUUID, true));

    getRuntimeAPI().evaluateGroovyExpression("${employee = null}", activityUUID, true, true);
    assertNull(getRuntimeAPI().evaluateGroovyExpression("${employee}", activityUUID, true, true));

    getRuntimeAPI().evaluateGroovyExpression("${nullEmployee = new " + Employee.class.getName() + "(\"john\", \"doe\");}", instanceUUID, true);
    assertEquals("john", getRuntimeAPI().evaluateGroovyExpression("${nullEmployee.firstName}", instanceUUID, true));

    getManagementAPI().deleteProcess(processUUID);

  }

  public void testBigInitialValue() throws Exception {
  	//skip if REST
  	if (isREST()){
  		return;
  	}
    Employee employee = new Employee("john", "doe");
    employee.setResponsabilities(getResponsabilities(20000));

    Employee employee2 = new Employee("john", "doe");
    employee2.setResponsabilities(getResponsabilities(20000));

    ProcessDefinition process = ProcessBuilder.createProcess("p", "1.0")
    .addObjectData("employee", ObjectVariable.class.getName(), new ObjectVariable(employee))
    .addHuman(getLogin())
    .addHumanTask("task", getLogin())
    .addObjectData("employee2", ObjectVariable.class.getName(), new ObjectVariable(employee2))
    .addHumanTask("task2", getLogin())
    .addObjectData("employee", ObjectVariable.class.getName(), new ObjectVariable(employee))
    .addTransition("task", "task2")
    .done();

    assertEmployeesEquals(employee, process.getDataFields().iterator().next().getInitialValue());
    assertEmployeesEquals(employee2, process.getActivity("task").getDataFields().iterator().next().getInitialValue());
    assertEmployeesEquals(employee, process.getActivity("task2").getDataFields().iterator().next().getInitialValue());

    process = getManagementAPI().deploy(getBusinessArchive(process, null, Employee.class, Address.class));
    ProcessDefinitionUUID processUUID = process.getUUID();

    assertEmployeesEquals(employee, process.getDataFields().iterator().next().getInitialValue());
    assertEmployeesEquals(employee2, process.getActivity("task").getDataFields().iterator().next().getInitialValue());
    assertEmployeesEquals(employee, process.getActivity("task2").getDataFields().iterator().next().getInitialValue());

    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    ActivityInstanceUUID activityUUID = getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.READY).iterator().next().getUUID();
    assertNotNull(activityUUID);

    assertEmployeesEquals(employee, getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "employee"));
    assertEmployeesEquals(employee2, getQueryRuntimeAPI().getActivityInstanceVariable(activityUUID, "employee2"));

    getRuntimeAPI().executeTask(activityUUID, true);
    activityUUID = getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.READY).iterator().next().getUUID();
    assertNotNull(activityUUID);

    assertEmployeesEquals(employee, getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "employee"));
    assertEmployeesEquals(employee, getQueryRuntimeAPI().getActivityInstanceVariable(activityUUID, "employee"));


    getManagementAPI().deleteProcess(processUUID);
  }

  public void testInstantiateWithCustomClass() throws Exception {
    ProcessDefinition process = ProcessBuilder.createProcess("p", "d")
    .addHuman(getLogin())
    .addObjectData("var", ObjectVariable.class.getName())
    .addSystemTask("auto")
    .done();
    
    process = getManagementAPI().deploy(getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = process.getUUID();
    
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("var", new ObjectVariable(new CustomClass(1, "hello")));
    
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID, variables);
    
    assertEquals(InstanceState.FINISHED, getQueryRuntimeAPI().getProcessInstance(instanceUUID).getInstanceState());
    final Object var = getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "var");
    assertNotNull(var);
    assertEquals(ObjectVariable.class.getName(), var.getClass().getName());
    
    final CustomClass custom = (CustomClass) ((ObjectVariable)var).getValue();
    assertEquals(1, custom.getId());
    assertEquals("hello", custom.getName());
    
    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().deleteProcess(processUUID);
  }
  
  /*
  This test doe not work in EJB mode as RMI classloader are not enabled
  public void testInstantiateWithCustomClassWithoutObjectVariable() throws Exception {
    ProcessDefinition process = ProcessBuilder.createProcess("p", "d")
    .addHuman(getLogin())
    .addObjectData("var", CustomClass.class.getName())
    .addSystemTask("auto")
    .done();
    
    getManagementAPI().deployJar("custom.jar", Misc.generateJar(CustomClass.class));
    
    process = getManagementAPI().deploy(getBusinessArchive(process, null, CustomClass.class));
    final ProcessDefinitionUUID processUUID = process.getUUID();
    
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("var", new CustomClass(1, "hello"));
    
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID, variables);
    
    assertEquals(InstanceState.FINISHED, getQueryRuntimeAPI().getProcessInstance(instanceUUID).getInstanceState());
    final Object var = getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "var");
    assertNotNull(var);
    assertEquals(CustomClass.class.getName(), var.getClass().getName());
    
    final CustomClass custom = (CustomClass)var;
    assertEquals(1, custom.getId());
    assertEquals("hello", custom.getName());
    
    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().deleteProcess(processUUID);
    getManagementAPI().removeJar("custom.jar");
  }
  */
  
  public void testInstantiateWithBonitaClass() throws Exception {
    ProcessDefinition process = ProcessBuilder.createProcess("p", "d")
    .addHuman(getLogin())
    .addObjectData("var", Exception.class.getName())
    .addSystemTask("auto")
    .done();
    
    process = getManagementAPI().deploy(getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = process.getUUID();
    
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("var", new BonitaRuntimeException("Exception message"));
    
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID, variables);
    
    assertEquals(InstanceState.FINISHED, getQueryRuntimeAPI().getProcessInstance(instanceUUID).getInstanceState());
    final Object var = getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "var");
    assertNotNull(var);
    assertEquals(BonitaRuntimeException.class.getName(), var.getClass().getName());
    
    final BonitaRuntimeException exception = (BonitaRuntimeException)var;
    assertEquals("Exception message", exception.getMessage());
    
    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().deleteProcess(processUUID);
  }
  
  private Collection<String> getResponsabilities(int nb) {
    Collection<String> responsabilities = new HashSet<String>();
    for (int i = 0 ; i < nb ; i++) {
      responsabilities.add("resp" + i);
    }
    return responsabilities;
  }

  private void assertEmployeesEquals(Employee expected, Object o) throws IOException, ClassNotFoundException {
    assertNotNull(o);
    assertEquals(ObjectVariable.class, o.getClass());

    Employee current = (Employee) ((ObjectVariable) o).getValue();
    assertEquals(expected.getFirstName(), current.getFirstName());
    assertEquals(expected.getLastName(), current.getLastName());

    if (expected.getResponsabilities() == null) {
      assertNull(current.getResponsabilities());
    } else {
      assertEquals(expected.getResponsabilities(), current.getResponsabilities());
    }
    if (expected.getAddress() == null) {
      assertNull(current.getAddress());
    } else {
      assertNotNull(current.getAddress());
      assertEquals(expected.getAddress().getStreetNumber(), current.getAddress().getStreetNumber());
      assertEquals(expected.getAddress().getStreetName(), current.getAddress().getStreetName());
      assertEquals(expected.getAddress().getZipCode(), current.getAddress().getZipCode());
    }
  }

  private void checkVariable(ProcessInstanceUUID instanceUUID, String variableName, Object initValue, Object newValue, Class< ? > expectedClazz) throws BonitaException {
    Object v = getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, variableName);
    ProcessInstance instance = getQueryRuntimeAPI().getProcessInstance(instanceUUID);
    Map<String, Object> variables = instance.getLastKnownVariableValues();
    assertNotNull(variables);
    if (initValue != null) {
      assertNotNull(v);
      assertTrue(expectedClazz.isAssignableFrom(v.getClass()));
      assertNotNull(variables.get(variableName));
      assertTrue(expectedClazz.isAssignableFrom(variables.get(variableName).getClass()));
    } else {
      assertNull(v);
      assertNull(variables.get(variableName));
    }
    if (v instanceof Document) {
      checkDocuments(initValue, v);
      checkDocuments(initValue, variables.get(variableName));
    } else {
      assertEquals(initValue, v);
      assertEquals(initValue, variables.get(variableName));
    }

    getRuntimeAPI().setProcessInstanceVariable(instanceUUID, variableName, newValue);
    v = getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, variableName);
    assertNotNull(v);
    assertTrue(expectedClazz.isAssignableFrom(v.getClass()));
    if (v instanceof Document) {
      checkDocuments(v, newValue);
    } else {
      assertEquals(newValue, v);
    }

    instance = getQueryRuntimeAPI().getProcessInstance(instanceUUID);
    variables = instance.getLastKnownVariableValues();
    assertNotNull(variables);
    assertNotNull(variables.get(variableName));

    if (newValue instanceof Document) {
      checkDocuments(variables.get(variableName), newValue);
    } else {
      assertEquals(newValue, variables.get(variableName));
    }
  }

  private void checkDocuments(Object base, Object d) {
    Document expected = (Document) base;
    Document current = (Document) d;

    Element expectedDocElement = expected.getDocumentElement();
    Element currentDocElement = current.getDocumentElement();

    assertEquals(expectedDocElement.getNodeName(), currentDocElement.getNodeName());
    assertEquals("el", currentDocElement.getNodeName());

    assertEquals(expectedDocElement.getAttribute("myAttribute"), currentDocElement.getAttribute("myAttribute"));

  }

  String generateString(final int number, final char c) {
    StringBuilder text = new StringBuilder();
    for (int i=0; i<number; i++) {
      text.append(c);
    };
    return text.toString();
  }
  
  public void testSetVariableWithClientType() throws Exception {
    Employee employee = new Employee("john", "doe");
    employee.setResponsabilities(getResponsabilities(20000));

    ProcessDefinition process = ProcessBuilder.createProcess("p", "1.0")
    .addHuman(getLogin())
    .addHumanTask("task", getLogin())
    .addObjectData("employee", ObjectVariable.class.getName(), new ObjectVariable(employee))
    .done();

    process = getManagementAPI().deploy(getBusinessArchive(process, null, Employee.class, Address.class));
    ProcessDefinitionUUID processUUID = process.getUUID();

    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    ActivityInstanceUUID activityUUID = getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.READY).iterator().next().getUUID();
    assertNotNull(activityUUID);

    assertEmployeesEquals(employee, getQueryRuntimeAPI().getActivityInstanceVariable(activityUUID, "employee"));
    
    Employee newEmployee = new Employee("jack", "doe");
    getRuntimeAPI().setVariable(activityUUID, "employee", new ObjectVariable(newEmployee));    
    
    assertEmployeesEquals(newEmployee, getQueryRuntimeAPI().getActivityInstanceVariable(activityUUID, "employee"));

    getManagementAPI().deleteProcess(processUUID);
  }
  
  public void testEvaluateGroovyExpressionWhenAnActivityVariableIsNotSet() throws Exception {
    ProcessDefinition process = ProcessBuilder.createProcess("nullVar", "2.5")
    .addHuman(getLogin())
    .addHumanTask("step", getLogin())
      .addStringData("myVar")
    .done();

    getManagementAPI().deploy(getBusinessArchive(process));

    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(process.getUUID());
    Set<LightTaskInstance> tasks = getQueryRuntimeAPI().getLightTasks(instanceUUID);
    assertEquals(1, tasks.size());
    LightTaskInstance task = tasks.iterator().next();
    String var = (String) getRuntimeAPI().evaluateGroovyExpression("${myVar}", task.getUUID(), new HashMap<String, Object>(), false, true);
    assertNull(var);

    getManagementAPI().deleteProcess(process.getUUID());
  }

  public void testEvaluateGroovyExpressionWhenAProcessVariableIsNotSet() throws Exception {
    ProcessDefinition process = ProcessBuilder.createProcess("nullVar", "2.5")
    .addHuman(getLogin())
    .addStringData("myVar")
    .addHumanTask("step", getLogin())
    .done();

    getManagementAPI().deploy(getBusinessArchive(process));

    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(process.getUUID());
    Set<LightTaskInstance> tasks = getQueryRuntimeAPI().getLightTasks(instanceUUID);
    assertEquals(1, tasks.size());
    LightTaskInstance task = tasks.iterator().next();
    String var = (String) getRuntimeAPI().evaluateGroovyExpression("${myVar}", task.getUUID(), new HashMap<String, Object>(), false, true);
    assertNull(var);
    var = (String) getRuntimeAPI().evaluateGroovyExpression("${myVar}", instanceUUID, new HashMap<String, Object>(), false, true);
    assertNull(var);

    getManagementAPI().deleteProcess(process.getUUID());
  }
  
  
  public void testStringVarWithBooleanValue() throws Exception {

    getManagementAPI().deleteAllProcesses();

    ProcessDefinition process = ProcessBuilder.createProcess("p", "1.0")
    .addHuman(getLogin())
    .addHumanTask("task", getLogin())
    .addStringData("var", "text")
    .done();

    process = getManagementAPI().deploy(getBusinessArchive(process, null, SetVarConnector.class, Employee.class));

    final ProcessDefinitionUUID processUUID = process.getUUID();
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    //check instance is not finished
    assertEquals(InstanceState.STARTED, getQueryRuntimeAPI().getProcessInstance(instanceUUID).getInstanceState());
    Set<ActivityInstance> activities = getQueryRuntimeAPI().getActivityInstances(instanceUUID, "task");
    assertEquals(1, activities.size());

    final ActivityInstance activity = activities.iterator().next();
    final ActivityInstanceUUID activityUUID = activity.getUUID();

    getRuntimeAPI().setVariable(activityUUID, "var", "true");

    assertEquals("true", getQueryRuntimeAPI().getVariable(activityUUID, "var"));

    getManagementAPI().deleteAllProcesses();

    }

}
