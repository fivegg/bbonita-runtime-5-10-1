package org.ow2.bonita.building;

import java.io.File;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

import org.ow2.bonita.APITestCase;
import org.ow2.bonita.definition.InstanceInitiator;
import org.ow2.bonita.example.websale.hook.Archive;
import org.ow2.bonita.example.websale.hook.Express;
import org.ow2.bonita.example.websale.hook.Reject;
import org.ow2.bonita.facade.def.element.HookDefinition.Event;
import org.ow2.bonita.facade.def.majorElement.ActivityDefinition.JoinType;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.runtime.ActivityState;
import org.ow2.bonita.facade.runtime.TaskInstance;
import org.ow2.bonita.facade.uuid.ActivityInstanceUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.util.DateUtil;
import org.ow2.bonita.util.Misc;
import org.ow2.bonita.util.ProcessBuilder;

public class TestXmlDefBuilder extends APITestCase implements Serializable {

  private static final long serialVersionUID = 247591709832787382L;

  private ProcessDefinition getWebSaleProcess() {
    Set<String> products = new HashSet<String>();
    products.add("TV");
    products.add("Laptop");
    products.add("CellPhone");
    
    Set<String> decision = new HashSet<String>();
    decision.add("grant");
    decision.add("reject");
    decision.add("moreinfo");

    ProcessDefinition process = 
      ProcessBuilder.createProcess("WebSale", "2.5")
        .addDescription("WebSale application")
        .addStringData("User_Name")
        .addStringData("Phone_Number")
        .addStringData("Email_Address")
        .addEnumData("Products", products, "TV")

        .addGroup("Customer")
          .addGroupResolver(InstanceInitiator.class.getName())
        .addGroup("Agent")
          .addGroupResolver(InstanceInitiator.class.getName())

        .addSystemTask("BonitaEnd")
          .addJoinType(JoinType.XOR)
        .addSystemTask("BonitaStart")
        .addHumanTask("Request", "Customer")
        .addHumanTask("SalesReview", "Agent")
          .addJoinType(JoinType.XOR)
          .addEnumData("decision", decision, "reject")
        .addHumanTask("MoreInfo", "Customer")
          .addStringData("Comment")
        .addHumanTask("Pay", "Customer")
          .addIntegerData("Credit_Card_Number")
          .addDateData("Credit_Card_Expiration_Date", DateUtil.parseDate("2008-09-24T10:01:34.000+0000"))
          .addIntegerData("Credit_Card_Digits")
          .addBooleanData("Express_Delivery", false)
        .addSystemTask("Archive")
          .addJoinType(JoinType.XOR)
          .addConnector(Event.automaticOnEnter, Archive.class.getName(), true)
        .addSystemTask("Reject")
          .addConnector(Event.automaticOnEnter, Reject.class.getName(), false)
        .addSystemTask("ExpressDelivery")
          .addConnector(Event.automaticOnEnter, Express.class.getName(), true)

        .addTransition("Start_Request", "BonitaStart", "Request")
        .addTransition("Reject_End", "Reject", "BonitaEnd")
        .addTransition("Archive_End", "Archive", "BonitaEnd")
        .addTransition("Request_SalesReview", "Request", "SalesReview")
        .addTransition("SalesReview_MoreInfo", "SalesReview", "MoreInfo")
          .addCondition("decision.compareTo(\"moreinfo\") == 0") 
        .addTransition("MoreInfo_SalesReview", "MoreInfo", "SalesReview")
        .addTransition("ExpressDelivery_Archive", "ExpressDelivery", "Archive")
        .addTransition("SalesReview_Pay", "SalesReview", "Pay")
          .addCondition("decision.compareTo(\"grant\") == 0") 
        .addTransition("SalesReview_Reject", "SalesReview", "Reject")
          .addCondition("decision.compareTo(\"reject\") == 0") 
        .addTransition("Pay_ExpressDelivery", "Pay", "ExpressDelivery")
          .addCondition("(Express_Delivery.compareTo(Boolean.valueOf(\"true\")) == 0) && (Credit_Card_Expiration_Date.compareTo(org.ow2.bonita.util.DateUtil.ISO_8601_FORMAT.parse(\"2008-09-24T10:01:34.000+0000\")) == 0)")
        .addTransition("Pay_Archive", "Pay", "Archive")
          .addCondition("Express_Delivery.compareTo(Boolean.valueOf(\"false\")) == 0")
      .done();
    return process;
  }
  
  public void testCreateProcessDefinition() throws Exception {
    
    File xmlDefFile = File.createTempFile("def", ".xml");
    xmlDefFile.deleteOnExit();

    byte[] xmlDefContent = XmlDefExporter.getInstance().createProcessDefinition(getWebSaleProcess());
    
    Misc.getFile(xmlDefFile, xmlDefContent);
    
    ProcessDefinition process = ProcessBuilder.createProcessFromXmlDefFile(xmlDefFile.toURL());
    
    process = getManagementAPI().deploy(getBusinessArchive(process));
    ProcessInstanceUUID processInstanceUUID = getRuntimeAPI().instantiateProcess(process.getUUID());
    
    //tasks execution
    Collection<TaskInstance> activities = getQueryRuntimeAPI().getTaskList(processInstanceUUID, ActivityState.READY);
    if (activities.isEmpty()) {
      fail("No activity to execute.");
    }
    while (!activities.isEmpty()) {     
      for (TaskInstance activity : activities) {
        
        final ActivityInstanceUUID taskUUID = activity.getUUID();
        final String activityId = activity.getActivityName();
        log("Starting task associated to activity: " + activityId);
        getRuntimeAPI().startTask(taskUUID, true);
        log("Finishing task associated to activity: " + activityId);
        getRuntimeAPI().finishTask(taskUUID, true);
        log("Task associated to activity: " + activityId + " finished.");
      }
      activities = getQueryRuntimeAPI().getTaskList(processInstanceUUID, ActivityState.READY);
    }

    assertTrue(xmlDefFile.delete());
    getRuntimeAPI().deleteProcessInstance(processInstanceUUID);
    getManagementAPI().deleteProcess(process.getUUID());
  }
  
  public void testCreateProcessDefinitionNullValue() throws Exception {
    
    File xmlDefFile = new File("src/main/resources/org/ow2/bonita/building/process-def-null.xml");
    
    ProcessDefinition process = ProcessBuilder.createProcessFromXmlDefFile(xmlDefFile.toURL());
    
    process = getManagementAPI().deploy(getBusinessArchive(process));
    ProcessInstanceUUID processInstanceUUID = getRuntimeAPI().instantiateProcess(process.getUUID());
    
    getRuntimeAPI().deleteProcessInstance(processInstanceUUID);
    getManagementAPI().deleteProcess(process.getUUID());
  }
  
  private static void log(String msg) {
    if (LOG.isLoggable(Level.INFO)) {
      LOG.info(Misc.LINE_SEPARATOR + "***** " + msg + " *****" + Misc.LINE_SEPARATOR);
    }
  }

}
