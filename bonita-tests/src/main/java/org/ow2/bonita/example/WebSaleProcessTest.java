package org.ow2.bonita.example;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.ow2.bonita.definition.InstanceInitiator;
import org.ow2.bonita.example.websale.WebSale;
import org.ow2.bonita.example.websale.hook.Archive;
import org.ow2.bonita.example.websale.hook.Express;
import org.ow2.bonita.example.websale.hook.Reject;
import org.ow2.bonita.facade.def.element.HookDefinition.Event;
import org.ow2.bonita.facade.def.majorElement.ActivityDefinition.JoinType;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition.ProcessState;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.util.BonitaException;
import org.ow2.bonita.util.DateUtil;
import org.ow2.bonita.util.ProcessBuilder;

public class WebSaleProcessTest extends WebsaleTest {

  protected void execute() throws BonitaException, IOException, ClassNotFoundException {
    ProcessDefinition process = getWebSaleProcess();
    ProcessInstanceUUID instanceUUID = WebSale.execute(process, "grant");
    WebSale.cleanProcess(instanceUUID);
    assertEquals(0, getQueryDefinitionAPI().getProcesses(WebSale.PROCESS_ID, ProcessState.ENABLED).size());
  }

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
}
