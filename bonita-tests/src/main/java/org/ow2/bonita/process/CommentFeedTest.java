package org.ow2.bonita.process;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ow2.bonita.APITestCase;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.runtime.ActivityInstance;
import org.ow2.bonita.facade.runtime.Comment;
import org.ow2.bonita.facade.runtime.TaskInstance;
import org.ow2.bonita.facade.runtime.impl.CommentImpl;
import org.ow2.bonita.facade.uuid.ActivityInstanceUUID;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.util.BonitaException;
import org.ow2.bonita.util.ProcessBuilder;

public class CommentFeedTest extends APITestCase {

  private boolean equalsOrBefore(Date date, Date anotherDate) {
    return date.compareTo(anotherDate) <= 0;
  }
  
  public void testAddComment() throws BonitaException {
    ProcessDefinition definition =
      ProcessBuilder.createProcess("feed", "1.0")
      .addHuman(getLogin())
      .addHumanTask("sala", getLogin())
      .done();
    definition = getManagementAPI().deploy(getBusinessArchive(definition));
    final ProcessDefinitionUUID processUUID = definition.getUUID();
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    getRuntimeAPI().addComment(instanceUUID, "First comment", "john");

    List<Comment> feed = getQueryRuntimeAPI().getCommentFeed(instanceUUID);
    assertEquals(1, feed.size());
    Comment c = new CommentImpl(feed.get(0));
    assertEquals("First comment", c.getMessage());
    assertEquals("john", c.getUserId());
    assertNotNull(c.getDate());
    assertNull(c.getActivityUUID());

    getManagementAPI().deleteProcess(processUUID);
  }

  public void testAddComments() throws BonitaException {
    ProcessDefinition definition =
      ProcessBuilder.createProcess("feed", "1.0")
      .addHuman(getLogin())
      .addHumanTask("sala", getLogin())
      .done();
    definition = getManagementAPI().deploy(getBusinessArchive(definition));
    final ProcessDefinitionUUID processUUID = definition.getUUID();
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    Collection<ActivityInstance> acts = this.getQueryRuntimeAPI().getActivityInstances(instanceUUID, "sala");
    assertEquals(1, acts.size());
    ActivityInstance activityInst = acts.iterator().next();

    getRuntimeAPI().addComment(instanceUUID, "First comment", "john");
    getRuntimeAPI().addComment(activityInst.getUUID(), "Done", "joe");

    List<Comment> feed = this.getQueryRuntimeAPI().getCommentFeed(instanceUUID);
    assertEquals(2, feed.size());
    Comment c = new CommentImpl(feed.get(0));
    assertEquals("First comment", c.getMessage());
    assertEquals("john", c.getUserId());
    assertNotNull(c.getDate());
    assertNull(c.getActivityUUID());

    Comment c1 = new CommentImpl(feed.get(1));
    assertEquals("Done", c1.getMessage());
    assertEquals("joe", c1.getUserId());
    assertNotNull(c1.getDate());
    assertEquals(activityInst.getUUID(), c1.getActivityUUID());

    getManagementAPI().deleteProcess(processUUID);
  }

  public void testAddCommentBadProcessUUID() throws BonitaException {
    ProcessInstanceUUID instanceUUID = new ProcessInstanceUUID("tests");
    try {
      this.getRuntimeAPI().addComment(instanceUUID, "First comment", "john");
      fail("The comment cannot be added because of a bad process instance");
    } catch (Exception e) {
    }
  }

  public void testGetActivityCommendFeed() throws Exception {
    ProcessDefinition definition =
      ProcessBuilder.createProcess("feed", "1.0")
      .addHuman(getLogin())
      .addHumanTask("sala", getLogin())
      .done();
    definition = getManagementAPI().deploy(getBusinessArchive(definition));

    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(definition.getUUID());
    getRuntimeAPI().addComment(instanceUUID, "This is a process instance comment", "john");

    Set<TaskInstance> tasks = getQueryRuntimeAPI().getTasks(instanceUUID);
    TaskInstance task = tasks.iterator().next();

    getRuntimeAPI().addComment(task.getUUID(), "This is an activity instance comment", "jack");

    List<Comment> comments = getQueryRuntimeAPI().getActivityInstanceCommentFeed(task.getUUID());
    assertEquals(1, comments.size());

    Comment comment1 = new CommentImpl(comments.get(0));
    assertEquals("This is an activity instance comment", comment1.getMessage());
    assertEquals("jack", comment1.getUserId());
    assertNotNull(comment1.getDate());
    assertEquals(task.getUUID(), comment1.getActivityUUID());

    getRuntimeAPI().addComment(task.getUUID(), "This is another activity instance comment", "james");

    comments = getQueryRuntimeAPI().getActivityInstanceCommentFeed(task.getUUID());
    assertEquals(2, comments.size());

    comment1 = new CommentImpl(comments.get(0));
    assertEquals("This is an activity instance comment", comment1.getMessage());
    assertEquals("jack", comment1.getUserId());
    assertNotNull(comment1.getDate());
    assertEquals(task.getUUID(), comment1.getActivityUUID());

    Comment comment2 = new CommentImpl(comments.get(1));
    assertEquals("This is another activity instance comment", comment2.getMessage());
    assertEquals("james", comment2.getUserId());
    assertNotNull(comment2.getDate());

    assertTrue(equalsOrBefore(comment1.getDate(), comment2.getDate()));

    getManagementAPI().deleteProcess(definition.getUUID());
  }
  
  public void testGetNumberOfCommendFeed() throws Exception {
    ProcessDefinition definition =
      ProcessBuilder.createProcess("feed", "1.0")
      .addHuman(getLogin())
      .addHumanTask("sala", getLogin())
      .done();
    definition = getManagementAPI().deploy(getBusinessArchive(definition));

    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(definition.getUUID());
    getRuntimeAPI().addComment(instanceUUID, "This is a process instance comment", "john");
    getRuntimeAPI().addComment(instanceUUID, "This is another process instance comment", "jack");

    Set<TaskInstance> tasks = getQueryRuntimeAPI().getTasks(instanceUUID);
    TaskInstance task = tasks.iterator().next();

    getRuntimeAPI().addComment(task.getUUID(), "This is an activity instance comment", "jack");
    getRuntimeAPI().addComment(task.getUUID(), "This is another activity instance comment", "jane");
    getRuntimeAPI().addComment(task.getUUID(), "This is my activity instance comment", "john");

    assertEquals(3, getQueryRuntimeAPI().getNumberOfActivityInstanceComments(task.getUUID()));
    assertEquals(2, getQueryRuntimeAPI().getNumberOfProcessInstanceComments(instanceUUID));
    assertEquals(5, getQueryRuntimeAPI().getNumberOfComments(instanceUUID));

    getManagementAPI().deleteProcess(definition.getUUID());
  }
  
  public void testGetNumberOfActivityInstanceCommentsSetActivityInstanceUUIDWithNullArgs() throws Exception {
    assertTrue(getQueryRuntimeAPI().getNumberOfActivityInstanceComments((Set<ActivityInstanceUUID>)null).isEmpty());
	  assertTrue(getQueryRuntimeAPI().getNumberOfActivityInstanceComments(new HashSet<ActivityInstanceUUID>()).isEmpty());
  }

  public void testGetNumberOfActivityInstanceCommentsSetActivityInstanceUUID() throws Exception {
    ProcessDefinition definition =
      ProcessBuilder.createProcess("feed", "1.0")
      .addHuman(getLogin())
      .addHumanTask("sala", getLogin())
      .done();
    definition = getManagementAPI().deploy(getBusinessArchive(definition));

    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(definition.getUUID());
    getRuntimeAPI().addComment(instanceUUID, "This is a process instance comment", "john");
    getRuntimeAPI().addComment(instanceUUID, "This is another process instance comment", "jack");

    Set<TaskInstance> tasks = getQueryRuntimeAPI().getTasks(instanceUUID);
    TaskInstance task = tasks.iterator().next();

    getRuntimeAPI().addComment(task.getUUID(), "This is an activity instance comment", "jack");
    getRuntimeAPI().addComment(task.getUUID(), "This is another activity instance comment", "jane");
    getRuntimeAPI().addComment(task.getUUID(), "This is my activity instance comment", "john");

    HashSet<ActivityInstanceUUID> taskUUIDs = new HashSet<ActivityInstanceUUID>();
    for (TaskInstance theTaskInstance : tasks) {
      taskUUIDs.add(theTaskInstance.getUUID());
    }

    assertEquals(3, getQueryRuntimeAPI().getNumberOfActivityInstanceComments(task.getUUID()));
    assertEquals(2, getQueryRuntimeAPI().getNumberOfProcessInstanceComments(instanceUUID));
    assertEquals(5, getQueryRuntimeAPI().getNumberOfComments(instanceUUID));
    assertEquals(1, getQueryRuntimeAPI().getNumberOfActivityInstanceComments(taskUUIDs).size());
    assertEquals(3, getQueryRuntimeAPI().getNumberOfActivityInstanceComments(taskUUIDs).values().iterator().next().intValue());

    getManagementAPI().deleteProcess(definition.getUUID());
  }
  
  public void testGetProcessInstanceCommentFeed() throws Exception {
    ProcessDefinition definition =
      ProcessBuilder.createProcess("feed", "1.0")
      .addHuman(getLogin())
      .addHumanTask("sala", getLogin())
      .done();
    definition = getManagementAPI().deploy(getBusinessArchive(definition));

    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(definition.getUUID());

    Set<TaskInstance> tasks = getQueryRuntimeAPI().getTasks(instanceUUID);
    TaskInstance task = tasks.iterator().next();

    getRuntimeAPI().addComment(instanceUUID, "This is a process instance comment", "john");
    Thread.sleep(2000);
    getRuntimeAPI().addComment(task.getUUID(), "This is an activity instance comment", "jack");
    Thread.sleep(2000);
    getRuntimeAPI().addComment(task.getUUID(), "This is another activity instance comment", "jane");
    Thread.sleep(2000);
    getRuntimeAPI().addComment(instanceUUID, "This is another process instance comment", "jack");
    Thread.sleep(2000);
    getRuntimeAPI().addComment(task.getUUID(), "This is my activity instance comment", "john");

    List<Comment> comments = getQueryRuntimeAPI().getProcessInstanceCommentFeed(instanceUUID);
    assertEquals(2, comments.size());

    CommentImpl instanceComment1 = new CommentImpl(comments.get(0));
    assertEquals("This is a process instance comment", instanceComment1.getMessage());
    assertEquals("john", instanceComment1.getUserId());
    assertNotNull(instanceComment1.getDate());
    assertEquals(null, instanceComment1.getActivityUUID());

    CommentImpl instanceComment2 = new CommentImpl(comments.get(1));
    assertEquals("This is another process instance comment", instanceComment2.getMessage());
    assertEquals("jack", instanceComment2.getUserId());
    assertNotNull(instanceComment2.getDate());
    assertEquals(null, instanceComment2.getActivityUUID());

    assertTrue(equalsOrBefore(instanceComment1.getDate(), instanceComment2.getDate()));
    
    getManagementAPI().deleteProcess(definition.getUUID());
  }
  
  public void testGetActivityInstanceCommentFeed() throws Exception {
    ProcessDefinition definition =
      ProcessBuilder.createProcess("feed", "1.0")
      .addHuman(getLogin())
      .addHumanTask("sala", getLogin())
      .done();
    definition = getManagementAPI().deploy(getBusinessArchive(definition));

    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(definition.getUUID());

    Set<TaskInstance> tasks = getQueryRuntimeAPI().getTasks(instanceUUID);
    TaskInstance task = tasks.iterator().next();

    getRuntimeAPI().addComment(instanceUUID, "This is a process instance comment", "john");
    getRuntimeAPI().addComment(task.getUUID(), "This is an activity instance comment", "jack");
    Thread.sleep(1000);
    getRuntimeAPI().addComment(task.getUUID(), "This is another activity instance comment", "jane");
    getRuntimeAPI().addComment(instanceUUID, "This is another process instance comment", "jack");
    getRuntimeAPI().addComment(task.getUUID(), "This is my activity instance comment", "john");

    List<Comment> comments = getQueryRuntimeAPI().getActivityInstanceCommentFeed(task.getUUID());
    assertEquals(3, comments.size());

    CommentImpl activityComment1 = new CommentImpl(comments.get(0));
    assertEquals("This is an activity instance comment", activityComment1.getMessage());
    assertEquals("jack", activityComment1.getUserId());
    assertNotNull(activityComment1.getDate());
    assertEquals(task.getUUID(), activityComment1.getActivityUUID());

    CommentImpl activityComment2 = new CommentImpl(comments.get(1));
    assertEquals("This is another activity instance comment", activityComment2.getMessage());
    assertEquals("jane", activityComment2.getUserId());
    assertNotNull(activityComment2.getDate());
    assertEquals(task.getUUID(), activityComment2.getActivityUUID());

    CommentImpl activityComment3 = new CommentImpl(comments.get(2));
    assertEquals("This is my activity instance comment", activityComment3.getMessage());
    assertEquals("john", activityComment3.getUserId());
    assertNotNull(activityComment3.getDate());
    assertEquals(task.getUUID(), activityComment3.getActivityUUID());

    assertTrue(equalsOrBefore(activityComment1.getDate(), activityComment2.getDate()));
    assertTrue(equalsOrBefore(activityComment2.getDate(), activityComment3.getDate()));

    getManagementAPI().deleteProcess(definition.getUUID());
  }

  public void testGetCommentFeed() throws Exception {
    ProcessDefinition definition =
      ProcessBuilder.createProcess("feed", "1.0")
      .addHuman(getLogin())
      .addHumanTask("sala", getLogin())
      .done();
    definition = getManagementAPI().deploy(getBusinessArchive(definition));

    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(definition.getUUID());

    Set<TaskInstance> tasks = getQueryRuntimeAPI().getTasks(instanceUUID);
    TaskInstance task = tasks.iterator().next();

    getRuntimeAPI().addComment(instanceUUID, "This is a process instance comment", "john");
    getRuntimeAPI().addComment(task.getUUID(), "This is an activity instance comment", "jack");
    getRuntimeAPI().addComment(task.getUUID(), "This is another activity instance comment", "jane");
    getRuntimeAPI().addComment(instanceUUID, "This is another process instance comment", "jack");
    getRuntimeAPI().addComment(task.getUUID(), "This is my activity instance comment", "john");

    List<Comment> comments = getQueryRuntimeAPI().getCommentFeed(instanceUUID);
    assertEquals(5, comments.size());

    CommentImpl instanceComment1 = new CommentImpl(comments.get(0));
    assertEquals("This is a process instance comment", instanceComment1.getMessage());
    assertEquals("john", instanceComment1.getUserId());
    assertNotNull(instanceComment1.getDate());
    assertEquals(null, instanceComment1.getActivityUUID());

    CommentImpl activityComment1 = new CommentImpl(comments.get(1));
    assertEquals("This is an activity instance comment", activityComment1.getMessage());
    assertEquals("jack", activityComment1.getUserId());
    assertNotNull(activityComment1.getDate());
    assertEquals(task.getUUID(), activityComment1.getActivityUUID());

    CommentImpl activityComment2 = new CommentImpl(comments.get(2));
    assertEquals("This is another activity instance comment", activityComment2.getMessage());
    assertEquals("jane", activityComment2.getUserId());
    assertNotNull(activityComment2.getDate());
    assertEquals(task.getUUID(), activityComment2.getActivityUUID());

    CommentImpl instanceComment2 = new CommentImpl(comments.get(3));
    assertEquals("This is another process instance comment", instanceComment2.getMessage());
    assertEquals("jack", instanceComment2.getUserId());
    assertNotNull(instanceComment2.getDate());
    assertEquals(null, instanceComment2.getActivityUUID());

    CommentImpl activityComment3 = new CommentImpl(comments.get(4));
    assertEquals("This is my activity instance comment", activityComment3.getMessage());
    assertEquals("john", activityComment3.getUserId());
    assertNotNull(activityComment3.getDate());
    assertEquals(task.getUUID(), activityComment3.getActivityUUID());

    assertTrue(equalsOrBefore(instanceComment1.getDate(), activityComment1.getDate()));
    assertTrue(equalsOrBefore(activityComment1.getDate(), activityComment2.getDate()));
    assertTrue(equalsOrBefore(activityComment2.getDate(), instanceComment2.getDate()));
    assertTrue(equalsOrBefore(instanceComment2.getDate(), activityComment3.getDate()));

    getManagementAPI().deleteProcess(definition.getUUID());
  }

  public void testAddHugeComment() throws Exception {
    StringBuilder message = new StringBuilder();
    for (int i = 0; i < 255; i++) {
      message.append(i);
    }
    
    ProcessDefinition definition =
      ProcessBuilder.createProcess("huge", "1.0")
      .addHuman(getLogin())
      .addHumanTask("test", getLogin())
      .done();

    final ProcessDefinition process = this.getManagementAPI().deploy(getBusinessArchive(definition));
    final ProcessDefinitionUUID processUUID = process.getUUID();
    final ProcessInstanceUUID instanceUUID = this.getRuntimeAPI().instantiateProcess(processUUID);

    this.getRuntimeAPI().addComment(instanceUUID, message.toString(), "john");
    
    List<Comment> feed = this.getQueryRuntimeAPI().getCommentFeed(instanceUUID);
    assertEquals(1, feed.size());
    Comment c = new CommentImpl(feed.get(0));
    assertEquals(message.toString(), c.getMessage());
    assertEquals("john", c.getUserId());
    assertNotNull(c.getDate());
    assertNull(c.getActivityUUID());

    this.getRuntimeAPI().deleteProcessInstance(instanceUUID);
    this.getManagementAPI().disable(processUUID);
    this.getManagementAPI().deleteProcess(processUUID);
  }

}
