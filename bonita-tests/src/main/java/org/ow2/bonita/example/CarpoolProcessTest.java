package org.ow2.bonita.example;

import java.io.IOException;

import org.ow2.bonita.example.carpool.Carpool;
import org.ow2.bonita.example.carpool.hook.Association;
import org.ow2.bonita.example.carpool.hook.CancelPlace;
import org.ow2.bonita.example.carpool.hook.CancelRequestDL;
import org.ow2.bonita.example.carpool.hook.Initial;
import org.ow2.bonita.example.carpool.hook.SendMessage;
import org.ow2.bonita.example.carpool.hook.UpdateRequest;
import org.ow2.bonita.example.carpool.hook.WaitAnswer;
import org.ow2.bonita.example.carpool.hook.WaitAnswerDL;
import org.ow2.bonita.example.carpool.hook.WaitRequest;
import org.ow2.bonita.example.carpool.hook.WaitRequestDL;
import org.ow2.bonita.facade.def.element.HookDefinition.Event;
import org.ow2.bonita.facade.def.majorElement.ActivityDefinition.JoinType;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.runtime.ProcessInstance;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.util.BonitaException;
import org.ow2.bonita.util.ProcessBuilder;

public class CarpoolProcessTest extends CarpoolTest {

  protected ProcessInstance instantiate(int mode) throws BonitaException, IOException, ClassNotFoundException {
    ProcessDefinition process = getCarpoolProcess();
    ProcessInstanceUUID instanceUUID = Carpool.execute(process, mode);
    return getQueryRuntimeAPI().getProcessInstance(instanceUUID);
  }
  
  private ProcessDefinition getCarpoolProcess() {
    ProcessDefinition process = 
      ProcessBuilder.createProcess("carpool", "1.0")
      .addDescription("Carpool offer process")

      .addStringData("requestFound", "no")
      .addStringData("answerFound", "no")
      .addStringData("offerTimeout", "no")
      .addStringData("answerTimeout", "no")

      .addHuman("admin")
 
      .addSystemTask("BonitaEnd")
        .addJoinType(JoinType.XOR)
      .addSystemTask("BonitaStart")
      .addSystemTask("InitRequest")
        .addConnector(Event.automaticOnEnter, UpdateRequest.class.getName(), true)
        .addConnector(Event.automaticOnEnter, SendMessage.class.getName(), true)
      .addHumanTask("CancelRequest", "admin")
        .addDeadline("5000", CancelRequestDL.class.getName())
      .addSystemTask("Association")
        .addConnector(Event.automaticOnEnter, Association.class.getName(), true)
      .addHumanTask("WaitRequestDL", "admin")
        .addDeadline("5000", WaitRequestDL.class.getName())
      .addHumanTask("WaitAnswerDL", "admin")
        .addDeadline("5000", WaitAnswerDL.class.getName())
      .addSystemTask("CancelPlace")
        .addConnector(Event.automaticOnEnter, CancelPlace.class.getName(), true)
      .addSystemTask("Initial")
        .addConnector(Event.automaticOnEnter, Initial.class.getName(), true)
      .addSystemTask("WaitRequest")
        .addJoinType(JoinType.XOR)
        .addConnector(Event.automaticOnEnter, WaitRequest.class.getName(), true)
      .addSystemTask("WaitAnswer")
        .addJoinType(JoinType.XOR)
        .addConnector(Event.automaticOnEnter, WaitAnswer.class.getName(), true)

      .addTransition("CancelRequest_End", "CancelRequest", "BonitaEnd")
      .addTransition("Initilal_WaitRequest", "Initial", "WaitRequest")
      .addTransition("WaitRequest_InitRequest", "WaitRequest", "InitRequest")
        .addCondition("requestFound.compareTo(\"yes\") == 0")
      .addTransition("WaitRequest_cancelPlace", "WaitRequest", "CancelPlace")
        .addCondition("offerTimeout.compareTo(\"yes\") == 0")
      .addTransition("WaitAnswer_Association", "WaitAnswer", "Association")
        .addCondition("answerFound.compareTo(\"yes\") == 0")
      .addTransition("WaitAnswer_CancelRequest", "WaitAnswer", "CancelRequest")
        .addCondition("answerTimeout.compareTo(\"yes\") == 0")
      .addTransition("InitRequest_WaitAnswer", "InitRequest", "WaitAnswer")
      .addTransition("WaitRequestDL_WaitRequest", "WaitRequestDL", "WaitRequest")
      .addTransition("WaitRequest_WaitRequestDL", "WaitRequest", "WaitRequestDL")
        .addCondition("requestFound.compareTo(\"no\") == 0 && offerTimeout.compareTo(\"no\") == 0")
      .addTransition("WaitAnswerDL_WaitAnswer", "WaitAnswerDL", "WaitAnswer")
      .addTransition("WaitAnswer_WaitAnswerDL", "WaitAnswer", "WaitAnswerDL")
        .addCondition("answerFound.compareTo(\"no\") == 0 && answerTimeout.compareTo(\"no\") == 0")
      .addTransition("Start_Initial", "BonitaStart", "Initial")
      .addTransition("CancelPlace_End", "CancelPlace", "BonitaEnd")
      .addTransition("Association_End", "Association", "BonitaEnd")
      .done();
    return process;
  }
}
