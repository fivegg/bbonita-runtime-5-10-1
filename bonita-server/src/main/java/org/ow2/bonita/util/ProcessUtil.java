/**
 * Copyright (C) 2009-2013 BonitaSoft S.A.
 * BonitaSoft, 31 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA  02110-1301, USA.
 **/
package org.ow2.bonita.util;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ow2.bonita.definition.activity.ConnectorExecutor;
import org.ow2.bonita.deployment.DeploymentRuntimeException;
import org.ow2.bonita.facade.def.InternalProcessDefinition;
import org.ow2.bonita.facade.def.element.AttachmentDefinition;
import org.ow2.bonita.facade.def.element.EventDefinition;
import org.ow2.bonita.facade.def.element.HookDefinition;
import org.ow2.bonita.facade.def.element.IncomingEventDefinition;
import org.ow2.bonita.facade.def.majorElement.ActivityDefinition;
import org.ow2.bonita.facade.def.majorElement.EventProcessDefinition;
import org.ow2.bonita.facade.exception.DocumentAlreadyExistsException;
import org.ow2.bonita.facade.exception.DocumentNotFoundException;
import org.ow2.bonita.facade.exception.DocumentationCreationException;
import org.ow2.bonita.facade.exception.ProcessNotFoundException;
import org.ow2.bonita.facade.impl.FacadeUtil;
import org.ow2.bonita.facade.runtime.InitialAttachment;
import org.ow2.bonita.facade.runtime.ProcessInstance;
import org.ow2.bonita.facade.runtime.impl.InternalProcessInstance;
import org.ow2.bonita.facade.uuid.ActivityDefinitionUUID;
import org.ow2.bonita.facade.uuid.ActivityInstanceUUID;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.runtime.event.EventConstants;
import org.ow2.bonita.runtime.event.EventInstance;
import org.ow2.bonita.runtime.event.IncomingEventInstance;
import org.ow2.bonita.runtime.event.Job;
import org.ow2.bonita.runtime.event.JobBuilder;
import org.ow2.bonita.runtime.model.Execution;
import org.ow2.bonita.services.Document;
import org.ow2.bonita.services.DocumentationManager;
import org.ow2.bonita.services.EventService;
import org.ow2.bonita.services.Recorder;
import org.ow2.bonita.type.Variable;

/**
 * 
 * @author Charles Souillard, Matthieu Chaffotte
 * 
 */
public class ProcessUtil {

  static final Logger LOG = Logger.getLogger(ProcessUtil.class.getName());

  public static void removeAllInstanceEvents(final ProcessInstance instance) {
    final EventService eventService = EnvTool.getEventService();
    eventService.removeSubscriptions(instance.getUUID());
    eventService.removeFiredEvents(instance.getUUID());
    eventService.removeJobs(instance.getUUID());
    eventService.removeLock(instance.getRootInstanceUUID());
  }

  public static void removeInternalInstanceEvents(final ProcessInstanceUUID instanceUUID) {
    final EventService eventService = EnvTool.getEventService();
    eventService.removeSubscriptions(instanceUUID);
    eventService.removeJobs(instanceUUID);
    eventService.removeLock(instanceUUID);
  }

  public static Date getTimerDate(final String condition, final ProcessDefinitionUUID definitionUUID,
      final long lastTime) throws GroovyException {
    return getTimerDate(condition, definitionUUID, null, lastTime);
  }

  public static Date getTimerDate(final String condition, final ActivityInstanceUUID activityUUID)
      throws GroovyException {
    return getTimerDate(condition, null, activityUUID, 0);
  }

  private static Date getTimerDate(final String condition, final ProcessDefinitionUUID definitionUUID,
      final ActivityInstanceUUID activityUUID, final long lastTime) throws GroovyException {
    Misc.checkArgsNotNull(condition);
    Date dueDate = null;
    String evaluatedCondition = condition;
    if (Misc.isJustAGroovyExpression(condition)) {
      Object value = null;
      if (activityUUID != null) {
        value = GroovyUtil.evaluate(condition, null, activityUUID, false, false);
      } else {
        final Map<String, Object> context = new HashMap<String, Object>();
        context.put(BonitaConstants.TIMER_LAST_EXECUTION, lastTime);
        value = GroovyUtil.evaluate(condition, context, definitionUUID, false);
      }
      if (value instanceof Date) {
        dueDate = (Date) value;
      } else {
        if (value instanceof String) {
          evaluatedCondition = (String) value;
        } else if (value instanceof Long) {
          evaluatedCondition = ((Long) value).toString();
        } else if (value instanceof Integer) {
          evaluatedCondition = ((Integer) value).toString();
        }
      }
    }
    if (dueDate == null) {
      try {
        final Long date = Long.parseLong(evaluatedCondition);
        if (activityUUID != null) {
          dueDate = new Date(System.currentTimeMillis() + date);
        } else if (lastTime < 0) {
          dueDate = new Date(System.currentTimeMillis() + date);
        } else {
          dueDate = new Date(date);
        }
      } catch (final NumberFormatException e1) {
        try {
          dueDate = DateUtil.parseDate(evaluatedCondition);
        } catch (final IllegalArgumentException e2) {
          throw new BonitaRuntimeException("Timer condition '" + evaluatedCondition
              + "' is neither a Long nor a formatted date", e2);
        }
      }
    }
    return dueDate;
  }

  public static Execution createProcessInstance(final ProcessDefinitionUUID processUUID,
      final Map<String, Object> variables, final Collection<InitialAttachment> attachments,
      final ProcessInstanceUUID parentInstanceUUID, final ProcessInstanceUUID rootInstanceUUID,
      final ActivityDefinitionUUID activityUUID, final ActivityInstanceUUID parentActivityUUID)
      throws ProcessNotFoundException {
    final InternalProcessDefinition process = FacadeUtil.getProcessDefinition(processUUID);

    final Execution rootExecution = createProcessInstance(process, rootInstanceUUID);
    final InternalProcessInstance instance = rootExecution.getInstance();

    final Recorder recorder = EnvTool.getRecorder();
    final Map<String, Variable> givenVariables = VariableUtil.createVariableMap(processUUID, variables);
    final Map<String, Variable> processVariables = VariableUtil.createVariables(process.getDataFields(), null,
        variables);
    final Map<String, Variable> initialVariables = new HashMap<String, Variable>();
    if (processVariables != null) {
      initialVariables.putAll(processVariables);
    }
    if (givenVariables != null) {
      initialVariables.putAll(givenVariables);
    }

    instance.setParentUUIDs(parentInstanceUUID, parentActivityUUID);
    instance.setInitialVaribales(initialVariables);
    String initiator = EnvTool.getUserId();
    if (parentInstanceUUID != null) {
      final InternalProcessInstance parentInstance = EnvTool.getJournalQueriers()
          .getProcessInstance(parentInstanceUUID);
      initiator = parentInstance.getStartedBy();
    }

    final DocumentationManager manager = EnvTool.getDocumentationManager();
    final Set<String> runtimeAttachmentNames = new HashSet<String>();
    if (attachments != null) {
      for (final InitialAttachment attachment : attachments) {
        final Document createDocument;
        try {
          final byte[] content = attachment.getContent();
          if (content != null) {
            String mimeType = attachment.getMetaData().get("content-type");
            if (mimeType == null) {
              mimeType = DocumentService.DEFAULT_MIME_TYPE;
            }
            createDocument = manager.createDocument(attachment.getName(), processUUID, instance.getUUID(),
                attachment.getFileName(), mimeType, content);
          } else {
            createDocument = manager.createDocument(attachment.getName(), processUUID, instance.getUUID());
          }
          if (rootInstanceUUID != null && !rootInstanceUUID.equals(instance.getUUID())) {
            final InternalProcessInstance rootProcessInstance = FacadeUtil.getInstance(rootInstanceUUID, null);
            if (rootProcessInstance != null && rootProcessInstance.getProcessDefinitionUUID() != null) {
              try {
                final long nbOfDocuments = manager.getNbOfDocuments(rootProcessInstance.getProcessInstanceUUID(),
                    createDocument.getName());
                if (nbOfDocuments == 0) {
                  manager.attachDocumentTo(rootProcessInstance.getProcessDefinitionUUID(), rootInstanceUUID,
                      createDocument.getId());
                }
              } catch (final DocumentNotFoundException e) {
                throw new BonitaRuntimeException(e);
              }
            }
          }
        } catch (final DocumentAlreadyExistsException e) {
          throw new BonitaRuntimeException(e);
        } catch (final DocumentationCreationException e) {
          throw new BonitaRuntimeException(e);
        }
        runtimeAttachmentNames.add(attachment.getName());
      }
      // Keep mapping with number of attachments.
      instance.setNbOfAttachments(attachments.size());
    } else {
      // Keep mapping with number of attachments.
      instance.setNbOfAttachments(0);
    }
    int previousNbOfAttachments;
    final Collection<AttachmentDefinition> values = process.getAttachments().values();
    if (values != null && !values.isEmpty()) {
      final List<Document> documents = DocumentService.getDocuments(manager, processUUID);
      Document document = null;
      for (final AttachmentDefinition attachmentDefinition : values) {
        final Iterator<Document> iterator = documents.iterator();
        document = null;
        final String attachmentName = attachmentDefinition.getName();
        if (!runtimeAttachmentNames.contains(attachmentName)) {
          try {
            while (iterator.hasNext() && document == null) {
              final Document next = iterator.next();
              if (attachmentName.equals(next.getName())) {
                document = next;
                iterator.remove();
                break;
              }
            }
            if (document == null) {
              throw new BonitaRuntimeException("Cannot retrieve document");
            }
            final byte[] content = manager.getContent(document);
            manager.createDocument(attachmentDefinition.getName(), processUUID, instance.getUUID(),
                attachmentDefinition.getFileName(), DocumentService.DEFAULT_MIME_TYPE, content);
            // Keep mapping of number of attachments
            previousNbOfAttachments = instance.getNbOfAttachments();
            if (previousNbOfAttachments <= 0) {
              instance.setNbOfAttachments(1);
            } else {
              instance.setNbOfAttachments(previousNbOfAttachments + 1);
            }
          } catch (final Exception e) {
            throw new BonitaRuntimeException(e);
          }
        }
      }
    }
    recorder.recordInstanceStarted(instance, initiator);
    if (instance.getUUID().equals(instance.getRootInstanceUUID())) {
      final EventService eventService = EnvTool.getEventService();
      eventService.lockRootInstance(instance.getUUID());
    }
    ConnectorExecutor.executeConnectors(rootExecution, HookDefinition.Event.instanceOnStart);
    return rootExecution;
  }

  public static void startEventSubProcesses(final ProcessInstance instance) throws ProcessNotFoundException {
    final ProcessInstanceUUID instanceUUID = instance.getUUID();
    final InternalProcessDefinition process = FacadeUtil.getProcessDefinition(instance.getProcessDefinitionUUID());
    final EventService eventService = EnvTool.getEventService();
    for (final EventProcessDefinition eventSubProcess : process.getEventSubProcesses()) {
      final InternalProcessDefinition eventProcess = EnvTool.getJournalQueriers().getProcess(eventSubProcess.getName(),
          eventSubProcess.getVersion());
      if (eventProcess == null) {
        throw new ProcessNotFoundException("bs_PU_1", eventSubProcess.getName(), eventSubProcess.getVersion());
      }
      final ActivityDefinition startEvent = getStartEvent(eventProcess);
      if (startEvent.isTimer()) {
        final String eventName = BonitaConstants.TIMER_EVENT_PREFIX + startEvent.getUUID();
        final String condition = startEvent.getTimerCondition();
        try {
          final Date date = getTimerDate(condition, process.getUUID(), -1);
          final long enableTime = date.getTime();
          final Job timer = JobBuilder.startTimerJob(eventName, startEvent.getUUID(), condition, enableTime);
          timer.setEventSubProcessRootInstanceUUID(instanceUUID);
          eventService.storeJob(timer);
        } catch (final GroovyException e) {
          throw new DeploymentRuntimeException(e.getMessage(), e.getCause());
        }
      } else if (startEvent.isCatchingSignalEvent()) {
        final String eventName = startEvent.getTimerCondition();
        final IncomingEventInstance signalEventInstance = new IncomingEventInstance(eventName, null, null,
            startEvent.getUUID(), null, eventProcess.getName(), startEvent.getName(), null,
            EventConstants.SIGNAL_START_EVENT, -1, false);
        signalEventInstance.setPermanent(true);
        signalEventInstance.setEventSubProcessRootInstanceUUID(instanceUUID);
        eventService.subscribe(signalEventInstance);
      } else if (startEvent.isReceiveEvent()) {
        final IncomingEventDefinition event = startEvent.getIncomingEvent();
        if (event != null) {
          final IncomingEventInstance eventInstance = new IncomingEventInstance(event.getName(), event.getExpression(),
              null, startEvent.getUUID(), null, eventProcess.getName(), startEvent.getName(), null,
              EventConstants.MESSAGE_START_EVENT, System.currentTimeMillis(), false);
          eventInstance.setPermanent(true);
          eventInstance.setEventSubProcessRootInstanceUUID(instanceUUID);
          addCorrelationKeys(event, eventInstance, instance.getUUID());
          eventService.subscribe(eventInstance);
        }
      }
    }
  }

  private static ActivityDefinition getStartEvent(final InternalProcessDefinition eventProcess) {
    for (final ActivityDefinition activity : eventProcess.getActivities()) {
      if (activity.getIncomingTransitions().isEmpty()) {
        return activity;
      }
    }
    return null;
  }

  private static Execution createProcessInstance(final InternalProcessDefinition process,
      final ProcessInstanceUUID rootInstanceUUID) {
    final ProcessDefinitionUUID processUUID = process.getUUID();
    final long instanceNb = EnvTool.getUUIDService().getNewProcessInstanceNb(processUUID);
    final ProcessInstanceUUID instanceUUID = new ProcessInstanceUUID(processUUID, instanceNb);

    ProcessInstanceUUID theRootInstanceUUID = null;
    if (rootInstanceUUID == null) {
      theRootInstanceUUID = instanceUUID;
    } else {
      theRootInstanceUUID = rootInstanceUUID;
    }
    final InternalProcessInstance instance = new InternalProcessInstance(instanceUUID, process, theRootInstanceUUID,
        instanceNb);
    final Execution processInstance = instance.getRootExecution();

    if (LOG.isLoggable(Level.FINE)) {
      LOG.fine("creating new execution for process '" + process.getName() + "'");
    }
    return processInstance;
  }

  public static String getEvaluatedCorrelationKey(final ProcessInstanceUUID processInstanceUUID,
      final String correlationKeyName, final String correlationKeyExpression) {
    if (correlationKeyExpression != null) {
      try {
        final String expression = GroovyUtil.evaluate(correlationKeyExpression, null, processInstanceUUID, false, false).toString();
        return correlationKeyName.concat("||||").concat(expression);
      } catch (final GroovyException e) {
        throw new BonitaRuntimeException(e);
      }
    }
    return null;
  }

  public static String getEvaluatedCorrelationKey(final ProcessDefinitionUUID definitionUUID,
      final String correlationKeyName, final String correlationKeyExpression) {
    if (correlationKeyExpression != null) {
      try {
        final String expression = GroovyUtil.evaluate(correlationKeyExpression, null, definitionUUID, false).toString();
        return correlationKeyName.concat("||||").concat(expression);
      } catch (final GroovyException e) {
        throw new BonitaRuntimeException(e);
      }
    }
    return null;
  }

  public static void addCorrelationKeys(final EventDefinition eventDefinition, final EventInstance eventInstance,
      final ProcessDefinitionUUID definitionUUID) {
    String correlationKeyName = eventDefinition.getCorrelationKeyName1();
    if (correlationKeyName != null) {
      final String correlationKey = getEvaluatedCorrelationKey(definitionUUID, correlationKeyName,
          eventDefinition.getCorrelationKeyExpression1());
      eventInstance.setCorrelationKey1(correlationKey);
    }
    correlationKeyName = eventDefinition.getCorrelationKeyName2();
    if (correlationKeyName != null) {
      final String correlationKey = getEvaluatedCorrelationKey(definitionUUID, correlationKeyName,
          eventDefinition.getCorrelationKeyExpression2());
      eventInstance.setCorrelationKey2(correlationKey);
    }
    correlationKeyName = eventDefinition.getCorrelationKeyName3();
    if (correlationKeyName != null) {
      final String correlationKey = getEvaluatedCorrelationKey(definitionUUID, correlationKeyName,
          eventDefinition.getCorrelationKeyExpression3());
      eventInstance.setCorrelationKey3(correlationKey);
    }
    correlationKeyName = eventDefinition.getCorrelationKeyName4();
    if (correlationKeyName != null) {
      final String correlationKey = getEvaluatedCorrelationKey(definitionUUID, correlationKeyName,
          eventDefinition.getCorrelationKeyExpression4());
      eventInstance.setCorrelationKey4(correlationKey);
    }
    correlationKeyName = eventDefinition.getCorrelationKeyName5();
    if (correlationKeyName != null) {
      final String correlationKey = getEvaluatedCorrelationKey(definitionUUID, correlationKeyName,
          eventDefinition.getCorrelationKeyExpression5());
      eventInstance.setCorrelationKey5(correlationKey);
    }
  }

  public static void addCorrelationKeys(final EventDefinition eventDefinition, final EventInstance eventInstance,
      final ProcessInstanceUUID instanceUUID) {
    String correlationKeyName = eventDefinition.getCorrelationKeyName1();
    if (correlationKeyName != null) {
      final String correlationKey = getEvaluatedCorrelationKey(instanceUUID, correlationKeyName,
          eventDefinition.getCorrelationKeyExpression1());
      eventInstance.setCorrelationKey1(correlationKey);
    }
    correlationKeyName = eventDefinition.getCorrelationKeyName2();
    if (correlationKeyName != null) {
      final String correlationKey = getEvaluatedCorrelationKey(instanceUUID, correlationKeyName,
          eventDefinition.getCorrelationKeyExpression2());
      eventInstance.setCorrelationKey2(correlationKey);
    }
    correlationKeyName = eventDefinition.getCorrelationKeyName3();
    if (correlationKeyName != null) {
      final String correlationKey = getEvaluatedCorrelationKey(instanceUUID, correlationKeyName,
          eventDefinition.getCorrelationKeyExpression3());
      eventInstance.setCorrelationKey3(correlationKey);
    }
    correlationKeyName = eventDefinition.getCorrelationKeyName4();
    if (correlationKeyName != null) {
      final String correlationKey = getEvaluatedCorrelationKey(instanceUUID, correlationKeyName,
          eventDefinition.getCorrelationKeyExpression4());
      eventInstance.setCorrelationKey4(correlationKey);
    }
    correlationKeyName = eventDefinition.getCorrelationKeyName5();
    if (correlationKeyName != null) {
      final String correlationKey = getEvaluatedCorrelationKey(instanceUUID, correlationKeyName,
          eventDefinition.getCorrelationKeyExpression5());
      eventInstance.setCorrelationKey5(correlationKey);
    }
  }

}
