package org.ow2.bonita.runtime;

import java.util.Set;

import org.ow2.bonita.APITestCase;
import org.ow2.bonita.facade.def.element.HookDefinition.Event;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.runtime.ActivityInstance;
import org.ow2.bonita.facade.runtime.ActivityState;
import org.ow2.bonita.facade.runtime.InstanceState;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.util.Command;
import org.ow2.bonita.util.ProcessBuilder;

public class ReplayCommandTest extends APITestCase {

	private Command getCommand() {
		return null;
	}
	public void testReplayActivityInErrorServer() throws Exception {
		testReplayActivityInError(new ServerCommandProvider());
	}
	
	public void testReplayActivityInErrorClient() throws Exception {
		testReplayActivityInError(new ClientCommandProvider());
	}
	
	public void testReplayTaskInErrorServer() throws Exception {
		testReplayTaskInError(new ServerCommandProvider());
	}
	
	public void testReplayTaskInErrorClient() throws Exception {
		testReplayTaskInError(new ClientCommandProvider());
	}
	
	private void testReplayActivityInError(final CommandProvider commandProvider) throws Exception {
		LOG.info("======== ROD  ==========");
	    ProcessDefinition process =
	      ProcessBuilder.createProcess("ReplayTest1", "1.0")
	      .addBooleanData("variable", true)
	      .addBooleanData("variable2", true)
	      .addIntegerData("counter", "${0}")
	      .addHuman("admin")
	      .addSystemTask("step1")
	        .addConnector(Event.automaticOnExit, ReplayErrorConnector.class.getName(), true) 
	        	.addInputParameter("value", "${variable}")
	        .addConnector(Event.automaticOnExit, ReplayErrorConnector.class.getName(), true) 
	        	.addInputParameter("value", "${variable2}")
	      .addHumanTask("step2", "admin")
	      .addTransition("step1", "step2")
	      .done();
	    
	    process = getManagementAPI().deploy(getBusinessArchive(process, null, ReplayErrorConnector.class));
	    LOG.info("======== ROD: DEPLOYED  ==========");
	    
	    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(process.getUUID());

	    LOG.info("======== ROD: STARTED  ==========");
	    
	    Thread.sleep(500); // wait the replay of connectors
	    
	    checkState(instanceUUID, ActivityState.FAILED, "step1");
	    checkState(instanceUUID, InstanceState.STARTED);
	    checkActivityInstanceNotExist(instanceUUID, "step2");
	    
	    LOG.info("======== ROD: FIRST TESTS DONE  ==========");

	    
	    final Set<ActivityInstance> activityInsts = getQueryRuntimeAPI().getActivityInstances(instanceUUID, "step1");
	    final ActivityInstance activityInst = activityInsts.iterator().next();
	    
	      LOG.info("======== ROD: WE WILL CORRECT THE FIRST ERROR ==========");

	    getRuntimeAPI().setProcessInstanceVariable(instanceUUID, "variable", false);
	      
	    final Command<Boolean> command = commandProvider.getReplayCommand(activityInst.getUUID());
	    try{
	    	getCommandAPI().execute(command);
	    } catch(Exception e){
	    	LOG.info("======== ROD: COMMAND THROWS EXCEPTION ==========");
	    }
	    Thread.sleep(500);
	    
	    checkState(instanceUUID, ActivityState.FAILED, "step1");
	    checkState(instanceUUID, InstanceState.STARTED);
	    checkActivityInstanceNotExist(instanceUUID, "step2");
	    
	      LOG.info("======== ROD: WE WILL CORRECT THE SECOND ERROR ==========");
	      
	     getRuntimeAPI().setProcessInstanceVariable(instanceUUID, "variable2", false);
	     		    
	     try{
	    	 getCommandAPI().execute(command);
	     } catch(Exception e){
	    	LOG.info("======== ROD: COMMAND THROWS EXCEPTION ==========");
	     }
	     
	    
	    checkState(instanceUUID, ActivityState.READY, "step2");
	    checkState(instanceUUID, InstanceState.STARTED);
	    
	      LOG.info("======== ROD : YES ==========");

	    
	    getManagementAPI().deleteProcess(process.getUUID());
	  }

	private void testReplayTaskInError(final CommandProvider commandProvider) throws Exception {
		LOG.info("======== ROD  ==========");
	    ProcessDefinition process =
	      ProcessBuilder.createProcess("ReplayTest2", "1.0")
	      .addBooleanData("variable", true)
	      .addBooleanData("variable2", true)
	      .addBooleanData("variable3", true)
	      .addHuman("admin")
	      .addHumanTask("step1","admin")
	        .addConnector(Event.taskOnFinish, ReplayErrorConnector.class.getName(), true) 
	        	.addInputParameter("value", "${variable3}")
	        .addConnector(Event.taskOnStart, ReplayErrorConnector.class.getName(), true) 
	        	.addInputParameter("value", "${variable2}")
	        .addConnector(Event.taskOnReady, ReplayErrorConnector.class.getName(), true) 
	        	.addInputParameter("value", "${variable}")
	      .addHumanTask("step2", "admin")
	      .addTransition("step1", "step2")
	      .done();
	    
	    process = getManagementAPI().deploy(getBusinessArchive(process, null, ReplayErrorConnector.class));
	    LOG.info("======== ROD: DEPLOYED  ==========");
		
	    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(process.getUUID());

	    LOG.info("======== ROD: STARTED  ==========");
	    
	    Thread.sleep(500); // wait the replay of connectors

	    checkState(instanceUUID, ActivityState.FAILED, "step1");
	    checkState(instanceUUID, InstanceState.STARTED);
	    checkActivityInstanceNotExist(instanceUUID, "step2");
	    
	    LOG.info("======== ROD: CORRECT ERROR onREADY  ==========");

	    final Set<ActivityInstance> activityInsts = getQueryRuntimeAPI().getActivityInstances(instanceUUID, "step1");
	    final ActivityInstance activityInst = activityInsts.iterator().next();
	    
	    getRuntimeAPI().setProcessInstanceVariable(instanceUUID, "variable", false);
	      
	    Command<Boolean> command = commandProvider.getReplayCommand(activityInst.getUUID());
	    try{
	    	 getCommandAPI().execute(command);
	     } catch(Exception e){
	    	LOG.info("======== ROD: COMMAND THROWS EXCEPTION ==========");
	     }
	    getRuntimeAPI().startTask(activityInst.getTask().getUUID(), true);
	    
	    Thread.sleep(500); // wait the replay of connectors
	    
	    checkState(instanceUUID, ActivityState.FAILED, "step1");
	    checkState(instanceUUID, InstanceState.STARTED);
	    checkActivityInstanceNotExist(instanceUUID, "step2");
	    
	    LOG.info("======== ROD: CORRECT ERROR onSTART  ==========");
	    
	    getRuntimeAPI().setProcessInstanceVariable(instanceUUID, "variable2", false);
	      
	    try{
	    	 getCommandAPI().execute(command);
	     } catch(Exception e){
	    	LOG.info("======== ROD: COMMAND THROWS EXCEPTION ==========");
	     }
	    getRuntimeAPI().startTask(activityInst.getTask().getUUID(), true);
	    
	    checkState(instanceUUID, ActivityState.EXECUTING, "step1");
	    checkState(instanceUUID, InstanceState.STARTED);
	    checkActivityInstanceNotExist(instanceUUID, "step2");
	    
	    getRuntimeAPI().finishTask(activityInst.getTask().getUUID(), true);
	    
	    Thread.sleep(500); // wait the replay of connectors

	    checkState(instanceUUID, ActivityState.FAILED, "step1");
	    checkState(instanceUUID, InstanceState.STARTED);
	    checkActivityInstanceNotExist(instanceUUID, "step2");
	    
	    LOG.info("======== ROD: CORRECT ERROR onFINISH  ==========");
	    getRuntimeAPI().setProcessInstanceVariable(instanceUUID, "variable3", false);
	      
	    try{
	    	 getCommandAPI().execute(command);
	     } catch(Exception e){
	    	LOG.info("======== ROD: COMMAND THROWS EXCEPTION ==========");
	     }
	    getRuntimeAPI().startTask(activityInst.getTask().getUUID(), true);
	    
	    checkState(instanceUUID, ActivityState.EXECUTING, "step1");
	    checkState(instanceUUID, InstanceState.STARTED);
	    checkActivityInstanceNotExist(instanceUUID, "step2");
	    
	    getRuntimeAPI().finishTask(activityInst.getTask().getUUID(), true);

	    checkState(instanceUUID, ActivityState.FINISHED, "step1");
	    checkState(instanceUUID, InstanceState.STARTED);
	    checkState(instanceUUID, ActivityState.READY, "step2");
	    
	    LOG.info("======== ROD : YES ==========");

	    
	    getManagementAPI().deleteProcess(process.getUUID());
	    
	}


	
}