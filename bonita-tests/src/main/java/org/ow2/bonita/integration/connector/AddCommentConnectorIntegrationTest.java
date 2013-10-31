package org.ow2.bonita.integration.connector;

import java.util.List;
import java.util.Set;

import org.bonitasoft.connectors.bonita.AddCommentConnector;
import org.ow2.bonita.APITestCase;
import org.ow2.bonita.facade.def.element.HookDefinition.Event;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.runtime.Comment;
import org.ow2.bonita.facade.runtime.TaskInstance;
import org.ow2.bonita.facade.runtime.impl.CommentImpl;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.util.ProcessBuilder;

public class AddCommentConnectorIntegrationTest extends APITestCase {

	public void testAddComments() throws Exception {
		ProcessDefinition definition = ProcessBuilder.createProcess("directory", "1.0")
      .addHuman("john")
      .addHumanTask("task", "john")
        .addConnector(Event.taskOnStart, AddCommentConnector.class.getName(), true)
          .addInputParameter("message", "My first comment")
          .addInputParameter("onActivity", true)
        .addConnector(Event.taskOnFinish, AddCommentConnector.class.getName(), true)
          .addInputParameter("message", "My last comment")
          .addInputParameter("onActivity", false)
      .done();

		ProcessDefinition process = getManagementAPI().deploy(getBusinessArchive(definition, null, AddCommentConnector.class));
    ProcessDefinitionUUID processUUID = process.getUUID();
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
    loginAs("john", "bpm");

    List<Comment> feed = getQueryRuntimeAPI().getCommentFeed(instanceUUID);
    assertEquals(0, feed.size());
    
    Set<TaskInstance> tasks = getQueryRuntimeAPI().getTasks(instanceUUID);
    TaskInstance task = tasks.iterator().next();
    getRuntimeAPI().startTask(task.getUUID(), true);
    
    feed = getQueryRuntimeAPI().getCommentFeed(instanceUUID);
    assertEquals(1, feed.size());
    Comment c = new CommentImpl(feed.get(0));
    assertEquals("My first comment", c.getMessage());
    assertEquals("john", c.getUserId());
    assertNotNull(c.getDate());
    assertEquals(task.getUUID(), c.getActivityUUID());

    getRuntimeAPI().finishTask(task.getUUID(), true);

    feed = getQueryRuntimeAPI().getCommentFeed(instanceUUID);
    assertEquals(2, feed.size());
    c = new CommentImpl(feed.get(1));
    assertEquals("My last comment", c.getMessage());
    assertEquals("john", c.getUserId());
    assertNotNull(c.getDate());
    assertNull(c.getActivityUUID());

    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
	}

}
