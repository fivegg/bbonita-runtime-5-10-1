package org.ow2.bonita.example;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.ow2.bonita.APITestCase;
import org.ow2.bonita.example.aw.MultiInstantiationApproval;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition.ProcessState;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.util.ProcessBuilder;



public class AWMultiInstTest extends APITestCase {
  
  public void testAWMultiInstReject0() throws Exception {
    final Map<String, String> decisions = new HashMap<String, String>();
    decisions.put("john", "no");
    decisions.put("jack", "no");
    decisions.put("james", "no");
    execute(decisions, "Reject");
  }
  
  public void testAWMultiInstReject1() throws Exception {
    final Map<String, String> decisions = new HashMap<String, String>();
	decisions.put("john", "yes");
	decisions.put("jack", "no");
	decisions.put("james", "no");
    execute(decisions, "Reject");
  }
  public void testAWMultiInstAccept2() throws Exception {
	final Map<String, String> decisions = new HashMap<String, String>();
	decisions.put("john", "yes");
	decisions.put("jack", "yes");
	decisions.put("james", "no");
    execute(decisions, "Accept");
  }
  public void testAWMultiInstAccept3() throws Exception {
	final Map<String, String> decisions = new HashMap<String, String>();
	decisions.put("john", "yes");
	decisions.put("jack", "yes");
	decisions.put("james", "yes");
    execute(decisions, "Accept");
  }
  
  protected void execute(final Map<String, String> decisions, final String nodeToCheck) throws Exception {
    URL xpdlUrl = MultiInstantiationApproval.class.getResource("MultiInstantiation.xpdl");
    ProcessDefinition clientProcess = ProcessBuilder.createProcessFromXpdlFile(xpdlUrl);

    ProcessInstanceUUID instanceUUID = MultiInstantiationApproval.execute(clientProcess, decisions);

    login();
    checkExecutedOnce(instanceUUID, new String[]{nodeToCheck});
    MultiInstantiationApproval.cleanProcess(instanceUUID);
    assertEquals(0, getQueryDefinitionAPI().getProcesses(MultiInstantiationApproval.PROCESS_ID, ProcessState.ENABLED).size());
  }
  protected String getLogin() {
    return "john";
  }
  protected String getPassword() {
    return "bpm";
  }
}
