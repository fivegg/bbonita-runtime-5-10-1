package org.ow2.bonita.facade.rest.apachehttpclient;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.Response.Status;

import org.apache.http.HttpResponse;
import org.ow2.bonita.facade.def.element.BusinessArchive;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.rest.RESTAPITestCase;
import org.ow2.bonita.facade.rest.apachehttpclient.api.ApacheHttpClientManagementAPI;
import org.ow2.bonita.facade.rest.apachehttpclient.api.ApacheHttpClientQueryRuntimeAPI;
import org.ow2.bonita.facade.rest.apachehttpclient.api.ApacheHttpClientRuntimeAPI;
import org.ow2.bonita.facade.uuid.ActivityInstanceUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.light.LightActivityInstance;
import org.ow2.bonita.util.ProcessBuilder;
import org.ow2.bonita.util.xml.XStreamUtil;
import org.ow2.bonita.variable.MyObject;

import com.thoughtworks.xstream.XStream;

public class RESTVariableTest  extends RESTAPITestCase {
  public void testGetBooleanVariable() throws Exception {
    final ProcessDefinition process = 
      ProcessBuilder.createProcess("Pwbv", "1.0")
        .addBooleanData("bVariable", true)
        .addSystemTask("activity1")
        .done();
    
    final BusinessArchive businessArchive = getBusinessArchive(process);
    
    final HttpResponse deployResponse = ApacheHttpClientManagementAPI.deploy(businessArchive, getUserForTheOptionsMap(), REST_USER, REST_PSWD);
    assertEquals(Status.OK.getStatusCode(), deployResponse.getStatusLine().getStatusCode());

    final String strProcessDefinition = ApacheHttpClientUtil.getResponseContent(deployResponse);
    
    final XStream xstream = XStreamUtil.getDefaultXstream();
    final ProcessDefinition processDefinition = (ProcessDefinition)xstream.fromXML(strProcessDefinition);
    assertEquals(processDefinition.getName(), process.getName());
    
    final HttpResponse instanciateResponse = ApacheHttpClientRuntimeAPI.instantiateProcess(process.getUUID().toString(), 
        getUserForTheOptionsMap(), REST_USER, REST_PSWD);
    assertEquals(Status.OK.getStatusCode(), instanciateResponse.getStatusLine().getStatusCode());
    final String xInstanceUUID = ApacheHttpClientUtil.getResponseContent(instanciateResponse);
    
    final ProcessInstanceUUID instanceUUID = (ProcessInstanceUUID)xstream.fromXML(xInstanceUUID);

    final HttpResponse getProcessInstanceVariableResponse = ApacheHttpClientQueryRuntimeAPI.getProcessInstanceVariable(
        instanceUUID.toString(), "bVariable", getUserForTheOptionsMap(), REST_USER, REST_PSWD);
    assertEquals(Status.OK.getStatusCode(), getProcessInstanceVariableResponse.getStatusLine().getStatusCode());
    final String xBVariable = ApacheHttpClientUtil.getResponseContent(getProcessInstanceVariableResponse);
    
    assertNotNull(xBVariable);
    final Boolean bVariable = (Boolean) xstream.fromXML(xBVariable);
    assertEquals(Boolean.TRUE, bVariable);

    final HttpResponse deleteProcessResponce = ApacheHttpClientManagementAPI.deleteProcess(
        process.getUUID().toString(), getUserForTheOptionsMap(), REST_USER, REST_PSWD);
    assertEquals(Status.NO_CONTENT.getStatusCode(), deleteProcessResponce.getStatusLine().getStatusCode());    

  }
  
  public void testSpecialCaracterOnVariables() throws Exception {
    final ProcessDefinition process = 
      ProcessBuilder.createProcess("Psv", "1.0")
        .addStringData("strVariable", "default")
        .addSystemTask("activity1")
        .done();
    
    final BusinessArchive businessArchive = getBusinessArchive(process);
    
    final HttpResponse deployResponse = ApacheHttpClientManagementAPI.deploy(businessArchive, getUserForTheOptionsMap(), REST_USER, REST_PSWD);
    assertEquals(Status.OK.getStatusCode(), deployResponse.getStatusLine().getStatusCode());

    final String strProcessDefinition = ApacheHttpClientUtil.getResponseContent(deployResponse);
    
    final XStream xstream = XStreamUtil.getDefaultXstream();
    final ProcessDefinition processDefinition = (ProcessDefinition)xstream.fromXML(strProcessDefinition);
    assertEquals(processDefinition.getName(), process.getName());
    
    final Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("strVariable", "%");
    final String xmlVariables = URLEncoder.encode(xstream.toXML(variables), "UTF-8");
    final HttpResponse instanciateResponse = ApacheHttpClientRuntimeAPI.instantiateProcessWithVariables(process.getUUID().toString(), 
        xmlVariables, getUserForTheOptionsMap(), REST_USER, REST_PSWD);
    assertEquals(Status.OK.getStatusCode(), instanciateResponse.getStatusLine().getStatusCode());
    final String xInstanceUUID = ApacheHttpClientUtil.getResponseContent(instanciateResponse);
    
    final ProcessInstanceUUID instanceUUID = (ProcessInstanceUUID)xstream.fromXML(xInstanceUUID);

    final HttpResponse getProcessInstanceVariableResponse = ApacheHttpClientQueryRuntimeAPI.getProcessInstanceVariable(
        instanceUUID.toString(), "strVariable", getUserForTheOptionsMap(), REST_USER, REST_PSWD);
    assertEquals(Status.OK.getStatusCode(), getProcessInstanceVariableResponse.getStatusLine().getStatusCode());
    final String strVariable = ApacheHttpClientUtil.getResponseContent(getProcessInstanceVariableResponse);
    
    assertNotNull(strVariable);
    assertEquals("%", strVariable);

    final HttpResponse deleteProcessResponce = ApacheHttpClientManagementAPI.deleteProcess(
        process.getUUID().toString(), getUserForTheOptionsMap(), REST_USER, REST_PSWD);
    assertEquals(Status.NO_CONTENT.getStatusCode(), deleteProcessResponce.getStatusLine().getStatusCode());    

  }
  
  @SuppressWarnings("unchecked")
  public void testCustomObjects() throws Exception {
    final MyObject myObject = new MyObject("test");
    final MyObject myObjectUpdate = new MyObject("update");
    final MyObject myObjectUpdateTwice = new MyObject("update update");

    final String localVariableName = "myObject2";
    final String globlaVariableName = "myObject";
    final ProcessDefinition process = ProcessBuilder.createProcess("p", "1.0")
    .addHuman("admin")
    .addObjectData(globlaVariableName, MyObject.class.getName(), myObject)
    .addHumanTask("task", "admin")
      .addObjectData(localVariableName, MyObject.class.getName(), myObject)
    .done();
    
    final BusinessArchive businessArchive = getBusinessArchive(process, null, MyObject.class);
    final HttpResponse deployResponse = ApacheHttpClientManagementAPI.deploy(businessArchive, getUserForTheOptionsMap(), REST_USER, REST_PSWD);
    assertEquals(Status.OK.getStatusCode(), deployResponse.getStatusLine().getStatusCode());

    final String strProcessDefinition = ApacheHttpClientUtil.getResponseContent(deployResponse);
    
    final XStream xstream = XStreamUtil.getDefaultXstream();
    final ProcessDefinition processDefinition = (ProcessDefinition)xstream.fromXML(strProcessDefinition);
    assertEquals(processDefinition.getName(), process.getName());
    
    final HttpResponse instanciateResponse = ApacheHttpClientRuntimeAPI.instantiateProcess(process.getUUID().toString(), 
        getUserForTheOptionsMap(), REST_USER, REST_PSWD);
    assertEquals(Status.OK.getStatusCode(), instanciateResponse.getStatusLine().getStatusCode());
    final String xInstanceUUID = ApacheHttpClientUtil.getResponseContent(instanciateResponse);
    
    final ProcessInstanceUUID instanceUUID = (ProcessInstanceUUID)xstream.fromXML(xInstanceUUID);
    
    //get the ActivityInstanceUUID
    final HttpResponse lightActivitiesResponse = ApacheHttpClientQueryRuntimeAPI.getLightActivities(instanceUUID.toString(), getUserForTheOptionsMap(), REST_USER, REST_PSWD);
    assertEquals(Status.OK.getStatusCode(), lightActivitiesResponse.getStatusLine().getStatusCode());
    final String strActivities = ApacheHttpClientUtil.getResponseContent(lightActivitiesResponse);
    assertNotNull(strActivities);
    @SuppressWarnings("unchecked")
    final Set<LightActivityInstance> activities = (Set<LightActivityInstance>) xstream.fromXML(strActivities);
    assertEquals(1, activities.size());
    final LightActivityInstance activity = activities.iterator().next();
    final ActivityInstanceUUID activityInstanceUUID = activity.getUUID();
    
    HttpResponse getVariablesResponse = ApacheHttpClientQueryRuntimeAPI.getVariables(
        activityInstanceUUID.toString(), getUserForTheOptionsMap(), REST_USER, REST_PSWD);
    assertEquals(Status.OK.getStatusCode(), getVariablesResponse.getStatusLine().getStatusCode());
    String strVariables = ApacheHttpClientUtil.getResponseContent(getVariablesResponse);
    assertNotNull(strVariables);
    
    Map<String, Object> retrievedVariables = (Map<String, Object>) xstream.fromXML(strVariables);
    assertEquals(myObject, retrievedVariables.get(globlaVariableName));
    assertEquals(myObject, retrievedVariables.get(localVariableName));
    
    //setvariable
    HttpResponse setVariableResponse = ApacheHttpClientRuntimeAPI.setVariable(activityInstanceUUID.getValue(), localVariableName, xstream.toXML(myObjectUpdate), getUserForTheOptionsMap(), REST_USER, REST_PSWD);
    assertEquals(Status.NO_CONTENT.getStatusCode(), setVariableResponse.getStatusLine().getStatusCode());
    
    setVariableResponse = ApacheHttpClientRuntimeAPI.setVariable(activityInstanceUUID.getValue(), globlaVariableName, xstream.toXML(myObjectUpdateTwice), getUserForTheOptionsMap(), REST_USER, REST_PSWD);
    assertEquals(Status.NO_CONTENT.getStatusCode(), setVariableResponse.getStatusLine().getStatusCode());
    
    getVariablesResponse = ApacheHttpClientQueryRuntimeAPI.getVariables(
        activityInstanceUUID.toString(), getUserForTheOptionsMap(), REST_USER, REST_PSWD);
    assertEquals(Status.OK.getStatusCode(), getVariablesResponse.getStatusLine().getStatusCode());
    strVariables = ApacheHttpClientUtil.getResponseContent(getVariablesResponse);
    assertNotNull(strVariables);
    
    retrievedVariables = (Map<String, Object>) xstream.fromXML(strVariables);
    assertEquals(myObjectUpdateTwice, retrievedVariables.get(globlaVariableName));
    assertEquals(myObjectUpdate, retrievedVariables.get(localVariableName));
    
    final HttpResponse deleteProcessResponce = ApacheHttpClientManagementAPI.deleteProcess(
        process.getUUID().toString(), getUserForTheOptionsMap(), REST_USER, REST_PSWD);
    assertEquals(Status.NO_CONTENT.getStatusCode(), deleteProcessResponce.getStatusLine().getStatusCode());

  }
}
