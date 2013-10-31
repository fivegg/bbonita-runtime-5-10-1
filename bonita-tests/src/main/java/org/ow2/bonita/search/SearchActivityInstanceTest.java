package org.ow2.bonita.search;

import java.util.Date;
import java.util.List;

import org.bonitasoft.connectors.bonita.resolvers.UserListRoleResolver;
import org.ow2.bonita.APITestCase;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.runtime.ActivityState;
import org.ow2.bonita.facade.uuid.ActivityInstanceUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.light.LightActivityInstance;
import org.ow2.bonita.search.index.ActivityInstanceIndex;
import org.ow2.bonita.util.ProcessBuilder;

public class SearchActivityInstanceTest extends APITestCase {
  
  public void testSearchActivityUsingStateInLowerCase() throws Exception {
    ProcessDefinition first = ProcessBuilder.createProcess("first", "1.0")
      .addGroup("users")
        .addGroupResolver(UserListRoleResolver.class.getName())
          .addInputParameter("users", "john, jack, james")
      .addHumanTask("step1", "users")
        .addActivityPriority(2)
      .addSystemTask("step0")
      .addTransition("step0", "step1")
    .done();
    first = getManagementAPI().deploy(getBusinessArchive(first, getResourcesFromConnector(UserListRoleResolver.class), UserListRoleResolver.class));
    getRuntimeAPI().instantiateProcess(first.getUUID());

    SearchQueryBuilder query = new SearchQueryBuilder(new ActivityInstanceIndex());
    query.criterion(ActivityInstanceIndex.STATE).equalsTo("ready");
    List<LightActivityInstance> activities = getQueryRuntimeAPI().search(query, 0, 10);
    assertEquals(1, activities.size());
    LightActivityInstance activity = activities.iterator().next();
    assertEquals("step1", activity.getActivityName());

    query = new SearchQueryBuilder(new ActivityInstanceIndex());
    query.criterion(ActivityInstanceIndex.STATE).equalsTo("finished");
    activities = getQueryRuntimeAPI().search(query, 0, 10);
    assertEquals(1, activities.size());
    activity = activities.iterator().next();
    assertEquals("step0", activity.getActivityName());

    getManagementAPI().deleteProcess(first.getUUID());
  }

  public void testSearchActivityUsingStateInUpperCase() throws Exception {
    ProcessDefinition first = ProcessBuilder.createProcess("first", "1.0")
      .addGroup("users")
        .addGroupResolver(UserListRoleResolver.class.getName())
          .addInputParameter("users", "john, jack, james")
      .addHumanTask("step1", "users")
        .addActivityPriority(2)
      .addSystemTask("step0")
      .addTransition("step0", "step1")
    .done();
    first = getManagementAPI().deploy(getBusinessArchive(first, getResourcesFromConnector(UserListRoleResolver.class), UserListRoleResolver.class));
    getRuntimeAPI().instantiateProcess(first.getUUID());

    SearchQueryBuilder query = new SearchQueryBuilder(new ActivityInstanceIndex());
    query.criterion(ActivityInstanceIndex.STATE).equalsTo("READY");
    List<LightActivityInstance> activities = getQueryRuntimeAPI().search(query, 0, 10);
    assertEquals(1, activities.size());
    LightActivityInstance activity = activities.iterator().next();
    assertEquals("step1", activity.getActivityName());

    query = new SearchQueryBuilder(new ActivityInstanceIndex());
    query.criterion(ActivityInstanceIndex.STATE).equalsTo("FINISHED");
    activities = getQueryRuntimeAPI().search(query, 0, 10);
    assertEquals(1, activities.size());
    activity = activities.iterator().next();
    assertEquals("step0", activity.getActivityName());

    getManagementAPI().deleteProcess(first.getUUID());
  }

  public void testSearchActivityUsingStateUsingEnumValues() throws Exception {
    ProcessDefinition first = ProcessBuilder.createProcess("first", "1.0")
      .addGroup("users")
        .addGroupResolver(UserListRoleResolver.class.getName())
          .addInputParameter("users", "john, jack, james")
      .addHumanTask("step1", "users")
        .addActivityPriority(2)
      .addSystemTask("step0")
      .addTransition("step0", "step1")
    .done();
    first = getManagementAPI().deploy(getBusinessArchive(first, getResourcesFromConnector(UserListRoleResolver.class), UserListRoleResolver.class));
    getRuntimeAPI().instantiateProcess(first.getUUID());

    SearchQueryBuilder query = new SearchQueryBuilder(new ActivityInstanceIndex());
    query.criterion(ActivityInstanceIndex.STATE).equalsTo(ActivityState.READY.name());
    List<LightActivityInstance> activities = getQueryRuntimeAPI().search(query, 0, 10);
    assertEquals(1, activities.size());
    LightActivityInstance activity = activities.iterator().next();
    assertEquals("step1", activity.getActivityName());

    query = new SearchQueryBuilder(new ActivityInstanceIndex());
    query.criterion(ActivityInstanceIndex.STATE).equalsTo(ActivityState.FINISHED.name());
    activities = getQueryRuntimeAPI().search(query, 0, 10);
    assertEquals(1, activities.size());
    activity = activities.iterator().next();
    assertEquals("step0", activity.getActivityName());

    getManagementAPI().deleteProcess(first.getUUID());
  }

  public void testSearchCandidates() throws Exception {
    ProcessDefinition first = ProcessBuilder.createProcess("first", "1.0")
    .addGroup("users")
      .addGroupResolver(UserListRoleResolver.class.getName())
        .addInputParameter("users", "john, jack, james")
    .addHumanTask("step1", "users")
      .addActivityPriority(2)
    .addSystemTask("system")
    .addTransition("system", "step1")
    .done();
    first = getManagementAPI().deploy(getBusinessArchive(first, getResourcesFromConnector(UserListRoleResolver.class), UserListRoleResolver.class));
    getRuntimeAPI().instantiateProcess(first.getUUID());

    SearchQueryBuilder query = new SearchQueryBuilder(new ActivityInstanceIndex());
    query.criterion(ActivityInstanceIndex.CANDIDATE).equalsTo("james");
    List<LightActivityInstance> activities = getQueryRuntimeAPI().search(query, 0, 10);
    assertEquals(1, activities.size());

    getManagementAPI().deleteProcess(first.getUUID());
  }
  
  public void testSearchUserId() throws Exception {
	    ProcessDefinition first = ProcessBuilder.createProcess("first", "1.0")
	    .addHuman(getLogin())
	    .addHumanTask("step1", getLogin())
	      .addActivityPriority(2)
	    .addSystemTask("system")
	    .addTransition("system", "step1")
	    .done();
	    first = getManagementAPI().deploy(getBusinessArchive(first));
	    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(first.getUUID());
	    
	    final ActivityInstanceUUID activity = getQueryRuntimeAPI().getOneTask(instanceUUID, ActivityState.READY);
	    getRuntimeAPI().assignTask(activity, getLogin());

	    SearchQueryBuilder query = new SearchQueryBuilder(new ActivityInstanceIndex());
	    query.criterion(ActivityInstanceIndex.USERID).equalsTo(getLogin());
	    List<LightActivityInstance> activities = getQueryRuntimeAPI().search(query, 0, 10);
	    assertEquals(1, activities.size());

	    getManagementAPI().deleteProcess(first.getUUID());
	  }

  public void testSearchName() throws Exception {
    ProcessDefinition first = ProcessBuilder.createProcess("first", "1.0")
    .addGroup("users")
      .addGroupResolver(UserListRoleResolver.class.getName())
        .addInputParameter("users", "john, jack, james")
    .addHumanTask("step1", "users")
      .addActivityPriority(2)
    .addSystemTask("system")
    .addTransition("system", "step1")
    .done();
    first = getManagementAPI().deploy(getBusinessArchive(first, getResourcesFromConnector(UserListRoleResolver.class), UserListRoleResolver.class));
    getRuntimeAPI().instantiateProcess(first.getUUID());

    SearchQueryBuilder query = new SearchQueryBuilder(new ActivityInstanceIndex());
    query.criterion(ActivityInstanceIndex.NAME).equalsTo("step2");
    List<LightActivityInstance> activities = getQueryRuntimeAPI().search(query, 0, 10);
    assertEquals(0, activities.size());

    query = new SearchQueryBuilder(new ActivityInstanceIndex());
    query.criterion(ActivityInstanceIndex.NAME).equalsTo("step1");
    activities = getQueryRuntimeAPI().search(query, 0, 10);
    assertEquals(1, activities.size());

    getManagementAPI().deleteProcess(first.getUUID());
  }

  public void testSearchPriority() throws Exception {
    ProcessDefinition first = ProcessBuilder.createProcess("first", "1.0")
    .addGroup("users")
      .addGroupResolver(UserListRoleResolver.class.getName())
        .addInputParameter("users", "john, jack, james")
    .addHumanTask("step1", "users")
      .addActivityPriority(2)
    .addSystemTask("system")
    .addTransition("system", "step1")
    .done();
    first = getManagementAPI().deploy(getBusinessArchive(first, getResourcesFromConnector(UserListRoleResolver.class), UserListRoleResolver.class));
    getRuntimeAPI().instantiateProcess(first.getUUID());

    SearchQueryBuilder query = new SearchQueryBuilder(new ActivityInstanceIndex());
    query.criterion(ActivityInstanceIndex.PRIORITY).equalsTo("1");
    List<LightActivityInstance> activities = getQueryRuntimeAPI().search(query, 0, 10);
    assertEquals(0, activities.size());

    query = new SearchQueryBuilder(new ActivityInstanceIndex());
    query.criterion(ActivityInstanceIndex.PRIORITY).equalsTo("2");
    activities = getQueryRuntimeAPI().search(query, 0, 10);
    assertEquals(1, activities.size());

    query = new SearchQueryBuilder(new ActivityInstanceIndex());
    query.criterion(ActivityInstanceIndex.PRIORITY).equalsTo("0");
    activities = getQueryRuntimeAPI().search(query, 0, 10);
    assertEquals(1, activities.size());

    getManagementAPI().deleteProcess(first.getUUID());
  }

  public void testSearchProcessDefUUIDEquals() throws Exception {
    ProcessDefinition first = ProcessBuilder.createProcess("first", "1.0")
    .addHuman(getLogin())
    .addHumanTask("step1", getLogin())
    .done();
    first = getManagementAPI().deploy(getBusinessArchive(first));

    ProcessDefinition second = ProcessBuilder.createProcess("second", "1.0")
    .addHuman("john")
    .addHumanTask("step1", "john")
    .done();
    second = getManagementAPI().deploy(getBusinessArchive(second));

    ProcessInstanceUUID secondUUID = getRuntimeAPI().instantiateProcess(second.getUUID());

    SearchQueryBuilder query = new SearchQueryBuilder(new ActivityInstanceIndex());
    query.criterion(ActivityInstanceIndex.PROCESS_DEFINITION_UUID).equalsTo(second.getUUID().getValue());
    List<LightActivityInstance> activities = getQueryRuntimeAPI().search(query, 0, 10);
    assertEquals(1, activities.size());
    LightActivityInstance activity = activities.get(0);
    assertEquals(new ActivityInstanceUUID(secondUUID, "step1", "it1", "mainActivityInstance", "noLoop"), activity.getUUID());

    getManagementAPI().deleteProcess(first.getUUID());
    getManagementAPI().deleteProcess(second.getUUID());
  }

  public void testSearchProcessDefUUIDAndActivityDescription() throws Exception {
    ProcessDefinition first = ProcessBuilder.createProcess("first", "1.0")
    .addHuman(getLogin())
    .addHumanTask("step1", getLogin())
    .done();
    first = getManagementAPI().deploy(getBusinessArchive(first));

    ProcessDefinition second = ProcessBuilder.createProcess("second", "1.0")
    .addHuman("john")
    .addHumanTask("step1", "john")
      .addDescription("this is a description")
    .done();
    second = getManagementAPI().deploy(getBusinessArchive(second));

    getRuntimeAPI().instantiateProcess(first.getUUID());
    ProcessInstanceUUID secondUUID = getRuntimeAPI().instantiateProcess(second.getUUID());

    SearchQueryBuilder query = new SearchQueryBuilder(new ActivityInstanceIndex());
    query.criterion(ActivityInstanceIndex.PROCESS_DEFINITION_UUID).equalsTo(second.getUUID().getValue());
    query.and().criterion(ActivityInstanceIndex.DESCRIPTION).startsWith("this is");
    List<LightActivityInstance> activities = getQueryRuntimeAPI().search(query, 0, 10);
    assertEquals(1, activities.size());
    LightActivityInstance activity = activities.get(0);
    assertEquals(new ActivityInstanceUUID(secondUUID, "step1", "it1", "mainActivityInstance", "noLoop"), activity.getUUID());

    getManagementAPI().deleteProcess(first.getUUID());
    getManagementAPI().deleteProcess(second.getUUID());
  }

  public void testSearchProcessInstanceUUID() throws Exception {
    ProcessDefinition first = ProcessBuilder.createProcess("first", "1.0")
    .addHuman(getLogin())
    .addHumanTask("step1", getLogin())
    .done();
    first = getManagementAPI().deploy(getBusinessArchive(first));

    ProcessDefinition second = ProcessBuilder.createProcess("second", "1.0")
    .addHuman("john")
    .addHumanTask("step1", "john")
    .done();
    second = getManagementAPI().deploy(getBusinessArchive(second));

    getRuntimeAPI().instantiateProcess(first.getUUID());
    ProcessInstanceUUID secondUUID = getRuntimeAPI().instantiateProcess(second.getUUID());

    SearchQueryBuilder query = new SearchQueryBuilder(new ActivityInstanceIndex());
    query.criterion(ActivityInstanceIndex.PROCESS_INSTANCE_UUID).equalsTo(secondUUID.getValue());
    List<LightActivityInstance> activities = getQueryRuntimeAPI().search(query, 0, 10);
    assertEquals(1, activities.size());
    LightActivityInstance activity = activities.get(0);
    assertEquals(new ActivityInstanceUUID(secondUUID, "step1", "it1", "mainActivityInstance", "noLoop"), activity.getUUID());

    getManagementAPI().deleteProcess(first.getUUID());
    getManagementAPI().deleteProcess(second.getUUID());
  }
  
  public void testSearchProcessRootInstanceUUID() throws Exception {
    ProcessDefinition first = ProcessBuilder.createProcess("first", "1.0")
    .addHuman(getLogin())
    .addHumanTask("step1", getLogin())
    .done();
    first = getManagementAPI().deploy(getBusinessArchive(first));

    ProcessDefinition second = ProcessBuilder.createProcess("second", "1.0")
    .addHuman("john")
    .addSubProcess("step1", "first")
    .done();
    second = getManagementAPI().deploy(getBusinessArchive(second));

    getRuntimeAPI().instantiateProcess(first.getUUID());
    ProcessInstanceUUID secondUUID = getRuntimeAPI().instantiateProcess(second.getUUID());

    SearchQueryBuilder query = new SearchQueryBuilder(new ActivityInstanceIndex());
    query.criterion(ActivityInstanceIndex.PROCESS_ROOT_INSTANCE_UUID).equalsTo(secondUUID.getValue());
    List<LightActivityInstance> instances = getQueryRuntimeAPI().search(query, 0, 10);
    assertEquals(2, instances.size());

    getManagementAPI().deleteProcess(second.getUUID());
    getManagementAPI().deleteProcess(first.getUUID());
  }

  public void testSearchEndDate() throws Exception {
    SearchDate from = new SearchDate(new Date(System.currentTimeMillis() - 10000));
    SearchDate to = new SearchDate(new Date(System.currentTimeMillis() + 20000));

    ProcessDefinition definition = ProcessBuilder.createProcess("expected", "1.0")
    .addHuman(getLogin())
    .addHumanTask("task1", getLogin())
      .addActivityExecutingTime(10000)
    .done();

    getManagementAPI().deploy(getBusinessArchive(definition));
    getRuntimeAPI().instantiateProcess(definition.getUUID());

    SearchQueryBuilder query = new SearchQueryBuilder(new ActivityInstanceIndex());
    query.criterion(ActivityInstanceIndex.EXPECTED_END_DATE).ranges(from.toString(), to.toString(), true);
    List<LightActivityInstance> instances = getQueryRuntimeAPI().search(query, 0, 10);
    assertEquals(1, instances.size());

    getManagementAPI().deleteProcess(definition.getUUID());
  }

}
