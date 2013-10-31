package org.ow2.bonita.connector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bonitasoft.connectors.bonita.AddDocumentVersion;
import org.bonitasoft.connectors.bonita.SetVarConnector;
import org.bonitasoft.connectors.bonita.filters.RandomMultipleFilter;
import org.bonitasoft.connectors.bonita.filters.UniqueRandomFilter;
import org.bonitasoft.connectors.bonita.resolvers.UserListRoleResolver;
import org.bonitasoft.connectors.scripting.GroovyConnector;
import org.ow2.bonita.APITestCase;
import org.ow2.bonita.facade.ErrorConnector;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.exception.BonitaInternalException;
import org.ow2.bonita.facade.runtime.ActivityInstance;
import org.ow2.bonita.facade.runtime.ActivityState;
import org.ow2.bonita.facade.runtime.ConnectorExecutionDescriptor;
import org.ow2.bonita.facade.runtime.Document;
import org.ow2.bonita.facade.uuid.ActivityInstanceUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.util.BonitaRuntimeException;
import org.ow2.bonita.util.Misc;
import org.ow2.bonita.util.ProcessBuilder;

public class ExecuteStandaloneConnectorTest extends APITestCase {

  public void testExecuteFilterWithNullParameters() throws Exception {
    final byte[] jar = Misc.generateJar(UniqueRandomFilter.class);
    getManagementAPI().deployJar("connect.jar", jar);
    final Set<String> members = new HashSet<String>();
    members.add("john");
    members.add("jack");
    members.add("jane");
    members.add("joe");
    final Set<String> candidates = getRuntimeAPI().executeFilter(UniqueRandomFilter.class.getName(), null, members);
    assertEquals(1, candidates.size());
    final String candidate = candidates.iterator().next();
    assertTrue(members.contains(candidate));

    getManagementAPI().removeJar("connect.jar");
  }

  public void testExecuteFilterWithNullMembers() throws Exception {
    final byte[] jar = Misc.generateJar(UniqueRandomFilter.class);
    getManagementAPI().deployJar("connect.jar", jar);
    try {
      getRuntimeAPI().executeFilter(UniqueRandomFilter.class.getName(), null, null);
      fail("Members cannot be null");
    } catch (final IllegalArgumentException e) {
    } finally {
      getManagementAPI().removeJar("connect.jar");
    }
  }

  public void testExecuteFilterWithBadParameters() throws Exception {
    final byte[] jar = Misc.generateJar(RandomMultipleFilter.class);
    getManagementAPI().deployJar("connect.jar", jar);

    final Set<String> members = new HashSet<String>();
    members.add("john");
    members.add("jack");
    members.add("jane");
    members.add("joe");

    final Map<String, Object[]> parameters = new HashMap<String, Object[]>();
    parameters.put("setMemberNumber", new Object[] { 2 });

    try {
      getRuntimeAPI().executeFilter(RandomMultipleFilter.class.getName(), parameters, members);
      fail("setMemberNumber is not a method of " + RandomMultipleFilter.class.getName());
    } catch (final BonitaRuntimeException e) {
    } finally {
      getManagementAPI().removeJar("connect.jar");
    }
  }

  public void testExecuteFilterWithParameters() throws Exception {
    final byte[] jar = Misc.generateJar(RandomMultipleFilter.class);
    getManagementAPI().deployJar("connect.jar", jar);

    final Set<String> members = new HashSet<String>();
    members.add("john");
    members.add("jack");
    members.add("jane");
    members.add("joe");

    final Map<String, Object[]> parameters = new HashMap<String, Object[]>();
    parameters.put("candidateNumber", new Object[] { 2 });

    final Set<String> candidates = getRuntimeAPI().executeFilter(RandomMultipleFilter.class.getName(), parameters,
        members);
    assertEquals(2, candidates.size());
    final Iterator<String> iterCandidates = candidates.iterator();
    final String candidate1 = iterCandidates.next();
    assertTrue(members.contains(candidate1));
    final String candidate2 = iterCandidates.next();
    assertTrue(members.contains(candidate2));
    assertNotSame(candidate1, candidate2);

    getManagementAPI().removeJar("connect.jar");
  }

  public void testExecuteRoleResolver() throws Exception {
    final byte[] jar = Misc.generateJar(UserListRoleResolver.class);
    getManagementAPI().deployJar("connect.jar", jar);

    final Map<String, Object[]> parameters = new HashMap<String, Object[]>();
    parameters
        .put(
            "setUsers",
            new Object[] { "${def users = ''; ['john', 'jack', 'joe'].each ({item -> users += item +  \", \"}); users += \"jane\" }" });

    final Set<String> members = getRuntimeAPI().executeRoleResolver(UserListRoleResolver.class.getName(), parameters);
    assertTrue(members.contains("john"));
    assertTrue(members.contains("jack"));
    assertTrue(members.contains("joe"));
    assertTrue(members.contains("jane"));

    getManagementAPI().removeJar("connect.jar");
  }

  public void testDoNotUseExecute() throws Exception {
    final byte[] jar = Misc.generateJar(UserListRoleResolver.class);
    getManagementAPI().deployJar("connect.jar", jar);
    final String name = UserListRoleResolver.class.getName();
    try {
      getRuntimeAPI().executeConnector(name, null);
      fail(name + "is a RoleResolver not a simple connector");
    } catch (final BonitaRuntimeException e) {
    } finally {
      getManagementAPI().removeJar("connect.jar");
    }
  }

  public void testExecuteConnectorNullParameters() throws Exception {
    final byte[] jar = Misc.generateJar(SetVarConnector.class);
    getManagementAPI().deployJar("connect.jar", jar);
    try {
      getRuntimeAPI().executeConnector(SetVarConnector.class.getName(), null);
      fail();
    } catch (final BonitaInternalException e) {
    } finally {
      getManagementAPI().removeJar("connect.jar");
    }
  }

  public void testExecuteConnectorEmptyParameters() throws Exception {
    final byte[] jar = Misc.generateJar(SetVarConnector.class);
    getManagementAPI().deployJar("connect.jar", jar);
    final Map<String, Object[]> parameters = new HashMap<String, Object[]>();
    try {
      getRuntimeAPI().executeConnector(SetVarConnector.class.getName(), parameters);
      fail("Parameters of SetVar Connector are missing. So an exception should be thorwn");
    } catch (final BonitaInternalException e) {
      e.printStackTrace();
    } finally {
      getManagementAPI().removeJar("connect.jar");
    }
  }

  public void testExecuteGroovyConnector() throws Exception {
    final byte[] jar = Misc.generateJar(GroovyConnector.class);
    getManagementAPI().deployJar("connect.jar", jar);
    final Map<String, Object[]> parameters = new HashMap<String, Object[]>();
    parameters.put("setThing", null);
    try {
      getRuntimeAPI().executeConnector(GroovyConnector.class.getName(), parameters);
      fail();
    } catch (final BonitaInternalException e) {
    } finally {
      getManagementAPI().removeJar("connect.jar");
    }
  }

  public void testExecuteConnector() throws Exception {
    final byte[] jar = Misc.generateJar(GroovyConnector.class);
    getManagementAPI().deployJar("connect.jar", jar);
    final Map<String, Object[]> parameters = new HashMap<String, Object[]>();
    parameters.put("setScript", null);
    try {
      getRuntimeAPI().executeConnector(GroovyConnector.class.getName(), parameters);
      fail("Script cannot be null");
    } catch (final BonitaInternalException e) {
    } finally {
      getManagementAPI().removeJar("connect.jar");
    }
  }

  public void testExecuteGoodGroovyConnector() throws Exception {
    final byte[] jar = Misc.generateJar(GroovyConnector.class);
    getManagementAPI().deployJar("connect.jar", jar);
    final Map<String, Object[]> parameters = new HashMap<String, Object[]>();
    parameters.put("setScript", new Object[] { "45" });
    final Map<String, Object> actual = getRuntimeAPI().executeConnector(GroovyConnector.class.getName(), parameters);
    final Map<String, Object> expected = new HashMap<String, Object>();
    expected.put("result", 45);
    assertEquals(expected, actual);
    getManagementAPI().removeJar("connect.jar");
  }

  public void testExecuteGoodGroovyConnector2() throws Exception {
    final byte[] jar = Misc.generateJar(GroovyConnector.class);
    getManagementAPI().deployJar("connect.jar", jar);
    final Map<String, Object[]> parameters = new HashMap<String, Object[]>();
    parameters.put("script", new Object[] { "45" });
    final Map<String, Object> actual = getRuntimeAPI().executeConnector(GroovyConnector.class.getName(), parameters);
    final Map<String, Object> expected = new HashMap<String, Object>();
    expected.put("result", 45);
    assertEquals(expected, actual);
    getManagementAPI().removeJar("connect.jar");
  }

  public void testExecuteGroovyConnectorWithAProcessDefinition() throws Exception {
    final Class<GroovyConnector> connectorClass = GroovyConnector.class;

    ProcessDefinition definition = ProcessBuilder.createProcess("kiitettävä", "1.0").addHuman(getLogin())
        .addHumanTask("yksi", getLogin()).done();
    definition = getManagementAPI().deploy(
        getBusinessArchive(definition, getResourcesFromConnector(connectorClass), connectorClass));

    final Map<String, Object[]> parameters = new HashMap<String, Object[]>();
    parameters.put("script", new Object[] { "45" });
    final Map<String, Object> actual = getRuntimeAPI().executeConnector(connectorClass.getName(), parameters,
        definition.getUUID());
    final Map<String, Object> expected = new HashMap<String, Object>();
    expected.put("result", 45);
    assertEquals(expected, actual);

    getManagementAPI().deleteProcess(definition.getUUID());
  }

  public void testExecuteGroovyConnectorWithAProcessDefinitionAndAContext() throws Exception {
    final Class<GroovyConnector> connectorClass = GroovyConnector.class;

    ProcessDefinition definition = ProcessBuilder.createProcess("kiitettävä", "1.0").addHuman(getLogin())
        .addHumanTask("yksi", getLogin()).done();
    definition = getManagementAPI().deploy(
        getBusinessArchive(definition, getResourcesFromConnector(connectorClass), connectorClass));

    final Map<String, Object> context = new HashMap<String, Object>();
    context.put("field1", "5 + ");

    final Map<String, Object[]> parameters = new HashMap<String, Object[]>();
    parameters.put("script", new Object[] { "${field1 + \"45\"}" });
    final Map<String, Object> actual = getRuntimeAPI().executeConnector(connectorClass.getName(), parameters,
        definition.getUUID(), context);
    final Map<String, Object> expected = new HashMap<String, Object>();
    expected.put("result", 50);
    assertEquals(expected, actual);

    getManagementAPI().deleteProcess(definition.getUUID());
  }

  @SuppressWarnings("unchecked")
  public void testExecuteConnectorsWithProcessDefinitionAndContext() throws Exception {
    final Class<GroovyConnector> connectorClass = GroovyConnector.class;

    ProcessDefinition definition = ProcessBuilder.createProcess("p1", "1.0").addHuman(getLogin())
        .addHumanTask("step1", getLogin()).done();
    definition = getManagementAPI().deploy(
        getBusinessArchive(definition, getResourcesFromConnector(connectorClass), connectorClass));

    final Map<String, Object> context = new HashMap<String, Object>();
    context.put("field1", "first element");
    context.put("field2", null);
    context.put("result", null);

    final ConnectorExecutionDescriptor connectorExecutionDescriptor1 = new ConnectorExecutionDescriptor(
        connectorClass.getName());
    connectorExecutionDescriptor1
        .addInputParameter(
            "script",
            "${\"List<String> l = new ArrayList<String>(); l.add(\\\"\" + field1 + \"\\\" + \\\" updated\\\"); l.add(\\\"second element\\\"); return l;\"}");
    connectorExecutionDescriptor1.addOutputParameter("field1", "${result.get(0)}");
    connectorExecutionDescriptor1.addOutputParameter("field2", "${result.get(1)}");

    final ConnectorExecutionDescriptor connectorExecutionDescriptor2 = new ConnectorExecutionDescriptor(
        connectorClass.getName());
    connectorExecutionDescriptor2
        .addInputParameter(
            "script",
            "${\"List<String> l = new ArrayList<String>(); l.add(\\\"\" + field1 + \"\\\"); l.add(\\\"\" + field2 + \"\\\"); l.add(\\\"third element\\\"); return l;\"}");
    connectorExecutionDescriptor2.addOutputParameter("result", "${result}");

    final List<ConnectorExecutionDescriptor> connectorsExecDesc = new ArrayList<ConnectorExecutionDescriptor>(2);
    connectorsExecDesc.add(connectorExecutionDescriptor1);
    connectorsExecDesc.add(connectorExecutionDescriptor2);

    final Map<String, Object> actual = getRuntimeAPI().executeConnectors(definition.getUUID(), connectorsExecDesc,
        context);
    final List<String> result = (List<String>) actual.get("result");
    assertNotNull(result);
    assertEquals(3, result.size());
    assertEquals("first element updated", result.get(0));
    assertEquals("second element", result.get(1));
    assertEquals("third element", result.get(2));

    assertEquals("first element updated", actual.get("field1"));
    assertEquals("second element", actual.get("field2"));

    getManagementAPI().deleteProcess(definition.getUUID());
  }

  @SuppressWarnings("unchecked")
  public void testExecuteConnectorsOptionDoesntThrowException() throws Exception {
    final Class<GroovyConnector> connectorClass = GroovyConnector.class;
    final Class<ErrorConnector> errorConnectorClass = ErrorConnector.class;

    ProcessDefinition definition = ProcessBuilder.createProcess("p1", "1.0").addHuman(getLogin())
        .addHumanTask("step1", getLogin()).done();
    definition = getManagementAPI().deploy(
        getBusinessArchive(definition, getResourcesFromConnector(connectorClass), connectorClass, errorConnectorClass));

    final Map<String, Object> context = new HashMap<String, Object>();
    context.put("field1", "first element");
    context.put("field2", null);
    context.put("result", null);

    final ConnectorExecutionDescriptor connectorExecutionDescriptor1 = new ConnectorExecutionDescriptor(
        connectorClass.getName());
    connectorExecutionDescriptor1
        .addInputParameter(
            "script",
            "${\"List<String> l = new ArrayList<String>(); l.add(\\\"\" + field1 + \"\\\" + \\\" updated\\\"); l.add(\\\"second element\\\"); return l;\"}");
    connectorExecutionDescriptor1.addOutputParameter("field1", "${result.get(0)}");
    connectorExecutionDescriptor1.addOutputParameter("field2", "${result.get(1)}");

    final ConnectorExecutionDescriptor connectorExecutionDescriptor2 = new ConnectorExecutionDescriptor(
        connectorClass.getName());
    connectorExecutionDescriptor2
        .addInputParameter(
            "script",
            "${\"List<String> l = new ArrayList<String>(); l.add(\\\"\" + field1 + \"\\\"); l.add(\\\"\" + field2 + \"\\\"); l.add(\\\"third element\\\"); return l;\"}");
    connectorExecutionDescriptor2.addOutputParameter("result", "${result}");

    final ConnectorExecutionDescriptor erroConnectorDesc = new ConnectorExecutionDescriptor(
        errorConnectorClass.getName());
    erroConnectorDesc.setThrowingException(false);

    final List<ConnectorExecutionDescriptor> connectorsExecDesc = new ArrayList<ConnectorExecutionDescriptor>(3);
    connectorsExecDesc.add(connectorExecutionDescriptor1);
    connectorsExecDesc.add(erroConnectorDesc);
    connectorsExecDesc.add(connectorExecutionDescriptor2);

    final Map<String, Object> actual = getRuntimeAPI().executeConnectors(definition.getUUID(), connectorsExecDesc,
        context);
    final List<String> result = (List<String>) actual.get("result");
    assertNotNull(result);
    assertEquals(3, result.size());
    assertEquals("first element updated", result.get(0));
    assertEquals("second element", result.get(1));
    assertEquals("third element", result.get(2));

    assertEquals("first element updated", actual.get("field1"));
    assertEquals("second element", actual.get("field2"));

    getManagementAPI().deleteProcess(definition.getUUID());
  }

  @SuppressWarnings("unchecked")
  public void testExecuteConnectorsWithJavaMethod() throws Exception {
    final Class<GroovyConnector> connectorClass = GroovyConnector.class;

    ProcessDefinition definition = ProcessBuilder.createProcess("p1", "1.0").addHuman(getLogin())
        .addHumanTask("step1", getLogin()).done();
    definition = getManagementAPI().deploy(
        getBusinessArchive(definition, getResourcesFromConnector(connectorClass), connectorClass));

    final List<String> additionalElements = new ArrayList<String>(2);
    additionalElements.add("n1");
    additionalElements.add("n2");

    final Map<String, Object> context = new HashMap<String, Object>();
    context.put("field1", "first element");
    context.put("field2", null);
    context.put("additionalElements", additionalElements);
    context.put("result", null);

    final ConnectorExecutionDescriptor connectorExecutionDescriptor1 = new ConnectorExecutionDescriptor(
        connectorClass.getName());
    connectorExecutionDescriptor1
        .addInputParameter(
            "script",
            "${\"List<String> l = new ArrayList<String>(); l.add(\\\"\" + field1 + \"\\\" + \\\" updated\\\"); l.add(\\\"second element\\\"); return l;\"}");
    connectorExecutionDescriptor1.addOutputParameter("field1", "${result.get(0)}");
    connectorExecutionDescriptor1.addOutputParameter("field2", "${result.get(1)}");

    final ConnectorExecutionDescriptor connectorExecutionDescriptor2 = new ConnectorExecutionDescriptor(
        connectorClass.getName());
    connectorExecutionDescriptor2
        .addInputParameter(
            "script",
            "${\"List<String> l = new ArrayList<String>(); l.add(\\\"\" + field1 + \"\\\"); l.add(\\\"\" + field2 + \"\\\"); l.add(\\\"third element\\\"); return l;\"}");
    connectorExecutionDescriptor2.addOutputParameter("additionalElements#additionalElements#addAll", "${result}");

    final List<ConnectorExecutionDescriptor> connectorsExecDesc = new ArrayList<ConnectorExecutionDescriptor>(2);
    connectorsExecDesc.add(connectorExecutionDescriptor1);
    connectorsExecDesc.add(connectorExecutionDescriptor2);

    final Map<String, Object> actual = getRuntimeAPI().executeConnectors(definition.getUUID(), connectorsExecDesc,
        context);
    final List<String> additionalElementsResult = (List<String>) actual.get("additionalElements");
    assertNotNull(additionalElementsResult);
    assertEquals(5, additionalElementsResult.size());
    assertEquals("n1", additionalElementsResult.get(0));
    assertEquals("n2", additionalElementsResult.get(1));
    assertEquals("first element updated", additionalElementsResult.get(2));
    assertEquals("second element", additionalElementsResult.get(3));
    assertEquals("third element", additionalElementsResult.get(4));

    getManagementAPI().deleteProcess(definition.getUUID());
  }

  @SuppressWarnings("unchecked")
  public void testExecuteConnectorsWithProcessInstanceAndContext() throws Exception {
    final Class<GroovyConnector> connectorClass = GroovyConnector.class;

    ProcessDefinition definition = ProcessBuilder.createProcess("p1", "1.0").addStringData("var1", "process variable")
        .addObjectData("list", ArrayList.class.getName(), new ArrayList<String>()).addHuman(getLogin())
        .addHumanTask("step1", getLogin()).done();
    definition = getManagementAPI().deploy(
        getBusinessArchive(definition, getResourcesFromConnector(connectorClass), connectorClass));

    final ProcessInstanceUUID processInstanceUUID = getRuntimeAPI().instantiateProcess(definition.getUUID());

    final Map<String, Object> context = new HashMap<String, Object>();
    context.put("field1", "first element");
    context.put("field2", null);

    final ConnectorExecutionDescriptor connectorExecutionDescriptor1 = new ConnectorExecutionDescriptor(
        connectorClass.getName());
    connectorExecutionDescriptor1
        .addInputParameter(
            "script",
            "${\"List<String> l = new ArrayList<String>(); l.add(\\\"\" + field1 + \"\\\" + \\\" updated\\\"); l.add(\\\"second element\\\"); return l;\"}");
    connectorExecutionDescriptor1.addOutputParameter("field1", "${result.get(0)}");
    connectorExecutionDescriptor1.addOutputParameter("field2", "${result.get(1)}");

    final ConnectorExecutionDescriptor connectorExecutionDescriptor2 = new ConnectorExecutionDescriptor(
        connectorClass.getName());
    connectorExecutionDescriptor2
        .addInputParameter(
            "script",
            "${\"List<String> l = new ArrayList<String>(); l.add(\\\"\" + field1 + \"\\\"); l.add(\\\"\" + field2 + \"\\\"); l.add(var1); list.addAll(l); return l;\"}");
    connectorExecutionDescriptor2.addOutputParameter("result", "${result}");

    final List<ConnectorExecutionDescriptor> connectorsExecDesc = new ArrayList<ConnectorExecutionDescriptor>(2);
    connectorsExecDesc.add(connectorExecutionDescriptor1);
    connectorsExecDesc.add(connectorExecutionDescriptor2);

    final Map<String, Object> actual = getRuntimeAPI().executeConnectors(processInstanceUUID, connectorsExecDesc,
        context, false);
    final List<String> result = (List<String>) actual.get("result");
    assertNotNull(result);
    assertEquals(3, result.size());
    assertEquals("first element updated", result.get(0));
    assertEquals("second element", result.get(1));
    assertEquals("process variable", result.get(2));

    final List<String> list = (List<String>) getQueryRuntimeAPI().getProcessInstanceVariable(processInstanceUUID,
        "list");
    assertEquals(3, list.size());

    getManagementAPI().deleteProcess(definition.getUUID());
  }

  @SuppressWarnings("unchecked")
  public void testExecuteConnectorsWithActivityInstanceAndContext() throws Exception {
    final Class<GroovyConnector> connectorClass = GroovyConnector.class;

    ProcessDefinition definition = ProcessBuilder.createProcess("p1", "1.0").addHuman(getLogin())
        .addHumanTask("step1", getLogin()).addObjectData("list", ArrayList.class.getName(), new ArrayList<String>())
        .addStringData("var1", "activity variable").done();
    definition = getManagementAPI().deploy(
        getBusinessArchive(definition, getResourcesFromConnector(connectorClass), connectorClass));

    final ProcessInstanceUUID processInstanceUUID = getRuntimeAPI().instantiateProcess(definition.getUUID());

    final Map<String, Object> context = new HashMap<String, Object>();
    context.put("field1", "first element");
    context.put("field2", null);

    final ConnectorExecutionDescriptor connectorExecutionDescriptor1 = new ConnectorExecutionDescriptor(
        connectorClass.getName());
    connectorExecutionDescriptor1
        .addInputParameter(
            "script",
            "${\"List<String> l = new ArrayList<String>(); l.add(\\\"\" + field1 + \"\\\" + \\\" updated\\\"); l.add(\\\"second element\\\"); return l;\"}");
    connectorExecutionDescriptor1.addOutputParameter("field1", "${result.get(0)}");
    connectorExecutionDescriptor1.addOutputParameter("field2", "${result.get(1)}");

    final ConnectorExecutionDescriptor connectorExecutionDescriptor2 = new ConnectorExecutionDescriptor(
        connectorClass.getName());
    connectorExecutionDescriptor2
        .addInputParameter(
            "script",
            "${\"List<String> l = new ArrayList<String>(); l.add(\\\"\" + field1 + \"\\\"); l.add(\\\"\" + field2 + \"\\\"); l.add(var1); list.addAll(l); return l;\"}");
    connectorExecutionDescriptor2.addOutputParameter("result", "${result}");

    final List<ConnectorExecutionDescriptor> connectorsExecDesc = new ArrayList<ConnectorExecutionDescriptor>(2);
    connectorsExecDesc.add(connectorExecutionDescriptor1);
    connectorsExecDesc.add(connectorExecutionDescriptor2);

    final Set<ActivityInstance> activityInstances = getQueryRuntimeAPI().getActivityInstances(processInstanceUUID,
        "step1");
    final ActivityInstance activityInstance = activityInstances.iterator().next();

    final Map<String, Object> actual = getRuntimeAPI().executeConnectors(activityInstance.getUUID(),
        connectorsExecDesc, context, false);
    final List<String> result = (List<String>) actual.get("result");
    assertNotNull(result);
    assertEquals(3, result.size());
    assertEquals("first element updated", result.get(0));
    assertEquals("second element", result.get(1));
    assertEquals("activity variable", result.get(2));

    final List<String> list = (List<String>) getQueryRuntimeAPI().getActivityInstanceVariable(
        activityInstance.getUUID(), "list");
    assertEquals(3, list.size());

    getManagementAPI().deleteProcess(definition.getUUID());
  }

  @SuppressWarnings("unchecked")
  public void testExecuteConnectorsWithActivityInstanceAndContextWithArchivedProcess() throws Exception {
    final Class<GroovyConnector> connectorClass = GroovyConnector.class;

    ProcessDefinition definition = ProcessBuilder.createProcess("p1", "1.0").addHuman(getLogin())
        .addObjectData("list", ArrayList.class.getName(), new ArrayList<String>())
        .addStringData("var1", "activity variable").addSystemTask("step1").done();
    definition = getManagementAPI().deploy(
        getBusinessArchive(definition, getResourcesFromConnector(connectorClass), connectorClass));

    final ProcessInstanceUUID processInstanceUUID = getRuntimeAPI().instantiateProcess(definition.getUUID());

    final Map<String, Object> context = new HashMap<String, Object>();
    context.put("field1", "first element");
    context.put("field2", null);

    final ConnectorExecutionDescriptor connectorExecutionDescriptor1 = new ConnectorExecutionDescriptor(
        connectorClass.getName());
    connectorExecutionDescriptor1
        .addInputParameter(
            "script",
            "${\"List<String> l = new ArrayList<String>(); l.add(\\\"\" + field1 + \"\\\" + \\\" updated\\\"); l.add(\\\"second element\\\"); return l;\"}");
    connectorExecutionDescriptor1.addOutputParameter("field1", "${result.get(0)}");
    connectorExecutionDescriptor1.addOutputParameter("field2", "${result.get(1)}");

    final ConnectorExecutionDescriptor connectorExecutionDescriptor2 = new ConnectorExecutionDescriptor(
        connectorClass.getName());
    connectorExecutionDescriptor2
        .addInputParameter(
            "script",
            "${\"List<String> l = new ArrayList<String>(); l.add(\\\"\" + field1 + \"\\\"); l.add(\\\"\" + field2 + \"\\\"); l.add(var1); return l;\"}");
    connectorExecutionDescriptor2.addOutputParameter("result", "${result}");

    final List<ConnectorExecutionDescriptor> connectorsExecDesc = new ArrayList<ConnectorExecutionDescriptor>(2);
    connectorsExecDesc.add(connectorExecutionDescriptor1);
    connectorsExecDesc.add(connectorExecutionDescriptor2);

    final Set<ActivityInstance> activityInstances = getQueryRuntimeAPI().getActivityInstances(processInstanceUUID,
        "step1");
    final ActivityInstance activityInstance = activityInstances.iterator().next();
    final Map<String, Object> actual = getRuntimeAPI().executeConnectors(activityInstance.getUUID(),
        connectorsExecDesc, context, false);
    final List<String> result = (List<String>) actual.get("result");
    assertNotNull(result);
    assertEquals(3, result.size());
    assertEquals("first element updated", result.get(0));
    assertEquals("second element", result.get(1));
    assertEquals("activity variable", result.get(2));

    getManagementAPI().deleteProcess(definition.getUUID());
  }

  public void testExecuteRoleResolverWithAProcessDefinition() throws Exception {
    final Class<UserListRoleResolver> connectorClass = UserListRoleResolver.class;
    ProcessDefinition definition = ProcessBuilder.createProcess("kiitettävä", "1.0").addHuman(getLogin())
        .addHumanTask("yksi", getLogin()).done();
    definition = getManagementAPI().deploy(
        getBusinessArchive(definition, getResourcesFromConnector(connectorClass), connectorClass));

    final Map<String, Object[]> parameters = new HashMap<String, Object[]>();
    parameters
        .put(
            "setUsers",
            new Object[] { "${def users = ''; ['john', 'jack', 'joe'].each ({item -> users += item +  \", \"}); users += \"jane\" }" });

    final Set<String> members = getRuntimeAPI().executeRoleResolver(connectorClass.getName(), parameters,
        definition.getUUID());
    assertTrue(members.contains("john"));
    assertTrue(members.contains("jack"));
    assertTrue(members.contains("joe"));
    assertTrue(members.contains("jane"));

    getManagementAPI().deleteProcess(definition.getUUID());
  }

  public void testExecuteFilterWithAProcessDefinition() throws Exception {
    final Class<UniqueRandomFilter> connectorClass = UniqueRandomFilter.class;
    ProcessDefinition definition = ProcessBuilder.createProcess("kiitettävä", "1.0").addHuman(getLogin())
        .addHumanTask("yksi", getLogin()).done();
    definition = getManagementAPI().deploy(
        getBusinessArchive(definition, getResourcesFromConnector(connectorClass), connectorClass));

    final Set<String> members = new HashSet<String>();
    members.add("john");
    members.add("jack");
    members.add("jane");
    members.add("joe");
    final Set<String> candidates = getRuntimeAPI().executeFilter(connectorClass.getName(), null, members,
        definition.getUUID());
    assertEquals(1, candidates.size());
    final String candidate = candidates.iterator().next();
    assertTrue(members.contains(candidate));

    getManagementAPI().deleteProcess(definition.getUUID());
  }

  public void testExecuteGroovyConnectorWithAProcessInstance() throws Exception {
    final Class<GroovyConnector> connectorClass = GroovyConnector.class;

    ProcessDefinition definition = ProcessBuilder.createProcess("simpleProcess", "1.0")
        .addIntegerData("processVar", 10).addHuman(getLogin()).addHumanTask("task1", getLogin())
        .addHumanTask("task2", getLogin()).addTransition("task1", "task2").done();
    definition = getManagementAPI().deploy(
        getBusinessArchive(definition, getResourcesFromConnector(connectorClass), connectorClass));

    final ProcessInstanceUUID processInstanceUUID = getRuntimeAPI().instantiateProcess(definition.getUUID());
    final ActivityInstanceUUID activityInstanceUUID = getQueryRuntimeAPI().getOneTask(ActivityState.READY);
    getRuntimeAPI().executeTask(activityInstanceUUID, true);
    getRuntimeAPI().setProcessInstanceVariable(processInstanceUUID, "processVar", 20);

    final Map<String, Object> context = new HashMap<String, Object>();
    context.put("field1", "5 + ");

    final Map<String, Object[]> parameters = new HashMap<String, Object[]>();
    parameters.put("script", new Object[] { "${field1 + \"45 + \" + processVar}" });
    final Map<String, Object> actual = getRuntimeAPI().executeConnector(connectorClass.getName(), parameters,
        processInstanceUUID, context, false);
    final Map<String, Object> expected = new HashMap<String, Object>();
    expected.put("result", 60);
    assertEquals(expected, actual);

    getManagementAPI().deleteProcess(definition.getUUID());
  }

  public void testExecuteGroovyConnectorWithAProcessInstanceUsingCurrentProcessValues() throws Exception {
    final Class<GroovyConnector> connectorClass = GroovyConnector.class;

    ProcessDefinition definition = ProcessBuilder.createProcess("simpleProcess", "1.0")
        .addIntegerData("processVar", 10).addHuman(getLogin()).addHumanTask("task1", getLogin())
        .addHumanTask("task2", getLogin()).addTransition("task1", "task2").done();
    definition = getManagementAPI().deploy(
        getBusinessArchive(definition, getResourcesFromConnector(connectorClass), connectorClass));

    final ProcessInstanceUUID processInstanceUUID = getRuntimeAPI().instantiateProcess(definition.getUUID());
    final ActivityInstanceUUID activityInstanceUUID = getQueryRuntimeAPI().getOneTask(ActivityState.READY);
    getRuntimeAPI().executeTask(activityInstanceUUID, true);
    getRuntimeAPI().setProcessInstanceVariable(processInstanceUUID, "processVar", 20);

    final Map<String, Object> context = new HashMap<String, Object>();
    context.put("field1", "5 + ");

    final Map<String, Object[]> parameters = new HashMap<String, Object[]>();
    parameters.put("script", new Object[] { "${field1 + \"45 + \" + processVar}" });
    final Map<String, Object> actual = getRuntimeAPI().executeConnector(connectorClass.getName(), parameters,
        processInstanceUUID, context, true);
    final Map<String, Object> expected = new HashMap<String, Object>();
    expected.put("result", 70);
    assertEquals(expected, actual);

    getManagementAPI().deleteProcess(definition.getUUID());
  }

  public void testExecuteGroovyConnectorWithAnActivityInstance() throws Exception {
    final Class<GroovyConnector> connectorClass = GroovyConnector.class;

    ProcessDefinition definition = ProcessBuilder.createProcess("simpleProcess", "1.0")
        .addIntegerData("processVar", 10).addHuman(getLogin()).addHumanTask("task1", getLogin())
        .addIntegerData("activityVar", 10).addHumanTask("task2", getLogin()).addTransition("task1", "task2").done();
    definition = getManagementAPI().deploy(
        getBusinessArchive(definition, getResourcesFromConnector(connectorClass), connectorClass));

    final ProcessInstanceUUID processInstanceUUID = getRuntimeAPI().instantiateProcess(definition.getUUID());
    final ActivityInstanceUUID activityInstanceUUID = getQueryRuntimeAPI().getOneTask(ActivityState.READY);
    getRuntimeAPI().setActivityInstanceVariable(activityInstanceUUID, "activityVar", 20);
    getRuntimeAPI().executeTask(activityInstanceUUID, true);
    getRuntimeAPI().setProcessInstanceVariable(processInstanceUUID, "processVar", 20);

    final Map<String, Object> context = new HashMap<String, Object>();
    context.put("field1", "5 + ");

    final Map<String, Object[]> parameters = new HashMap<String, Object[]>();
    parameters.put("script", new Object[] { "${field1 + \"45 + \" + processVar + \" + \" + activityVar}" });
    final Map<String, Object> actual = getRuntimeAPI().executeConnector(connectorClass.getName(), parameters,
        activityInstanceUUID, context, false);
    final Map<String, Object> expected = new HashMap<String, Object>();
    expected.put("result", 80);
    assertEquals(expected, actual);

    getManagementAPI().deleteProcess(definition.getUUID());
  }

  public void testExecuteGroovyConnectorWithAnActivityInstanceUsingCurrentProcessValues() throws Exception {
    final Class<GroovyConnector> connectorClass = GroovyConnector.class;

    ProcessDefinition definition = ProcessBuilder.createProcess("simpleProcess", "1.0")
        .addIntegerData("processVar", 10).addHuman(getLogin()).addHumanTask("task1", getLogin())
        .addIntegerData("activityVar", 10).addHumanTask("task2", getLogin()).addTransition("task1", "task2").done();
    definition = getManagementAPI().deploy(
        getBusinessArchive(definition, getResourcesFromConnector(connectorClass), connectorClass));

    final ProcessInstanceUUID processInstanceUUID = getRuntimeAPI().instantiateProcess(definition.getUUID());
    final ActivityInstanceUUID activityInstanceUUID = getQueryRuntimeAPI().getOneTask(ActivityState.READY);
    getRuntimeAPI().setActivityInstanceVariable(activityInstanceUUID, "activityVar", 20);
    getRuntimeAPI().executeTask(activityInstanceUUID, true);
    getRuntimeAPI().setProcessInstanceVariable(processInstanceUUID, "processVar", 20);

    final Map<String, Object> context = new HashMap<String, Object>();
    context.put("field1", "5 + ");

    final Map<String, Object[]> parameters = new HashMap<String, Object[]>();
    parameters.put("script", new Object[] { "${field1 + \"45 + \" + processVar + \" + \" + activityVar}" });
    final Map<String, Object> actual = getRuntimeAPI().executeConnector(connectorClass.getName(), parameters,
        activityInstanceUUID, context, true);
    final Map<String, Object> expected = new HashMap<String, Object>();
    expected.put("result", 90);
    assertEquals(expected, actual);

    getManagementAPI().deleteProcess(definition.getUUID());
  }

  public void testExecuteAddDocumentVersionConnector() throws Exception {
    final Class<AddDocumentVersion> connectorClass = AddDocumentVersion.class;

    ProcessDefinition definition = ProcessBuilder.createProcess("simpleProcess", "1.0").addHuman(getLogin())
        .addHumanTask("task1", getLogin()).done();
    definition = getManagementAPI().deploy(
        getBusinessArchive(definition, getResourcesFromConnector(connectorClass), connectorClass));

    final ProcessInstanceUUID instantiateUUID = getRuntimeAPI().instantiateProcess(definition.getUUID());
    final Document document = getRuntimeAPI().createDocument("myFile", instantiateUUID, "init.txt", "text/*",
        "initial".getBytes());

    final Map<String, Object[]> parameters = new HashMap<String, Object[]>();
    parameters.put("documentUUID", new Object[] { document.getUUID().getValue() });
    parameters.put("isMajorVersion", new Object[] { true });
    parameters.put("fileName", new Object[] { "update.txt" });
    parameters.put("content", new Object[] { "new content" });
    final Map<String, Object> context = new HashMap<String, Object>(1);
    final Map<String, Object> actual = getRuntimeAPI().executeConnector(connectorClass.getName(), parameters,
        instantiateUUID, context, true);
    final Document retrievedDocument = (Document) actual.get("document");
    assertNotNull(retrievedDocument);
    assertEquals("update.txt", retrievedDocument.getContentFileName());
    final byte[] documentContent = getQueryRuntimeAPI().getDocumentContent(retrievedDocument.getUUID());
    assertEquals("new content", new String(documentContent));

    getManagementAPI().deleteProcess(definition.getUUID());
  }

}
