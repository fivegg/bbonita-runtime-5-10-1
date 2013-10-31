/**
 * Copyright (C) 2007  Bull S. A. S.
 * Bull, Rue Jean Jaures, B.P.68, 78340, Les Clayes-sous-Bois
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA  02110-1301, USA.
 * 
 * Modified by Matthieu Chaffotte - BonitaSoft S.A.
 **/
package org.ow2.bonita.deployment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ow2.bonita.facade.def.InternalProcessDefinition;
import org.ow2.bonita.facade.def.element.AttachmentDefinition;
import org.ow2.bonita.facade.def.element.BoundaryEvent;
import org.ow2.bonita.facade.def.element.BusinessArchive;
import org.ow2.bonita.facade.def.element.IncomingEventDefinition;
import org.ow2.bonita.facade.def.element.impl.BusinessArchiveImpl;
import org.ow2.bonita.facade.def.element.impl.IterationDescriptor;
import org.ow2.bonita.facade.def.majorElement.ActivityDefinition;
import org.ow2.bonita.facade.def.majorElement.ActivityDefinition.Type;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition.ProcessState;
import org.ow2.bonita.facade.def.majorElement.TransitionDefinition;
import org.ow2.bonita.facade.def.majorElement.impl.ActivityDefinitionImpl;
import org.ow2.bonita.facade.def.majorElement.impl.ProcessDefinitionImpl;
import org.ow2.bonita.facade.exception.DeploymentException;
import org.ow2.bonita.facade.runtime.Category;
import org.ow2.bonita.facade.runtime.impl.CategoryImpl;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.iteration.IterationNode;
import org.ow2.bonita.iteration.IterationProcess;
import org.ow2.bonita.iteration.IterationTransition;
import org.ow2.bonita.iteration.SuspiciousIterationException;
import org.ow2.bonita.light.LightProcessDefinition.ProcessType;
import org.ow2.bonita.runtime.IterationDetectionPolicy;
import org.ow2.bonita.runtime.event.EventConstants;
import org.ow2.bonita.runtime.event.IncomingEventInstance;
import org.ow2.bonita.runtime.event.Job;
import org.ow2.bonita.runtime.event.JobBuilder;
import org.ow2.bonita.services.DocumentationManager;
import org.ow2.bonita.services.EventService;
import org.ow2.bonita.services.LargeDataRepository;
import org.ow2.bonita.services.Querier;
import org.ow2.bonita.services.Recorder;
import org.ow2.bonita.util.ArchiveTool;
import org.ow2.bonita.util.BonitaRuntimeException;
import org.ow2.bonita.util.DocumentService;
import org.ow2.bonita.util.EnvTool;
import org.ow2.bonita.util.ExceptionManager;
import org.ow2.bonita.util.GroovyException;
import org.ow2.bonita.util.Misc;
import org.ow2.bonita.util.ProcessUtil;

/**
 * Takes {@link BusinessArchiveImpl}s as input, parses the contents into a
 * {@link ProcessDefinition}, stores the {@link ProcessDefinition} into the
 * 
 * <p>
 * This class is thread safe and can be shared between multiple threads.
 * </p>
 * 
 * @author Marc Blachon, Guillaume Porcher, Charles Souillard, Miguel Valdes,
 *         Pierre Vigneras
 */
public final class Deployer {

  private static final Logger LOG = Logger.getLogger(Deployer.class.getName());

  private Deployer() {
  }

  public static ProcessDefinition deploy(final BusinessArchive businessArchive) throws DeploymentException {
    final Recorder recorder = EnvTool.getRecorder();
    final ProcessDefinitionUUID processUUID = businessArchive.getProcessUUID();
    if (processUUID == null) {
      throw new DeploymentException("The given businessArchive does not contain any process.");
    }
    final LargeDataRepository ldr = EnvTool.getLargeDataRepository();
    for (final Map.Entry<String, byte[]> resource : businessArchive.getResources().entrySet()) {
      ldr.storeData(Misc.getBusinessArchiveCategories(processUUID), resource.getKey(), resource.getValue(), true);
    }
    boolean removeDeps = true;
    IterationProcess iterationProcess = null;
    try {
      ProcessDefinition process = null;
      final ClassLoader current = Thread.currentThread().getContextClassLoader();
      Set<IterationDescriptor> iterationDescriptors = null;
      try {
        final ClassLoader processClassloader = EnvTool.getClassDataLoader().getProcessClassLoader(processUUID);
        Thread.currentThread().setContextClassLoader(processClassloader);
        process = businessArchive.getProcessDefinition();
        if (process == null) {
          throw new DeploymentRuntimeException("The given bar archive does not contain any process file");
        }
        iterationProcess = getIterationProcess(process);
        iterationDescriptors = getIterations(iterationProcess);
      } catch (final SuspiciousIterationException sie) {
        if (EnvTool.getIterationDetectionPolicy() == IterationDetectionPolicy.DISABLE) {
          LOG.log(Level.WARNING, "The iteration detection is disable but a suspicious cycle was detected in process "
              + processUUID + ", disabling this is not recommanded, use this process at your own risks", sie);
          iterationDescriptors = getIterations(iterationProcess, false);
        } else {
          throw sie;
        }
      } finally {
        Thread.currentThread().setContextClassLoader(current);
      }

      // update process
      for (final IterationDescriptor iterationDescriptor : iterationDescriptors) {
        ((ProcessDefinitionImpl) process).addIterationDescriptors(iterationDescriptor);
        for (final String activityName : iterationDescriptor.getCycleNodes()) {
          final ActivityDefinitionImpl activity = (ActivityDefinitionImpl) process.getActivity(activityName);
          activity.setInCycle(true);
        }
      }

      final String lastProcessVersion = EnvTool.getAllQueriers().getLastProcessVersion(process.getName());
      if (lastProcessVersion != null) {
        final BARVersion barVersion = new BARVersion(process.getVersion());
        final int versionComparison = barVersion.compareTo(lastProcessVersion);
        if (versionComparison <= 0) {
          if (versionComparison == 0) {
            removeDeps = false;
          }
          final String message = ExceptionManager.getInstance().getFullMessage("bd_D_3", process.getName(),
              lastProcessVersion, process.getVersion(), lastProcessVersion);
          throw new DeploymentRuntimeException(message);
        }
      }

      if (process.getClassDependencies() != null) {
        for (final String className : process.getClassDependencies()) {
          try {
            EnvTool.getClassDataLoader().getClass(process.getUUID(), className);
          } catch (final ClassNotFoundException e) {
            final String message = ExceptionManager.getInstance()
                .getFullMessage("bd_D_7", process.getName(), className);
            throw new DeploymentRuntimeException(message);
          }
        }
      }

      final DocumentationManager manager = EnvTool.getDocumentationManager();
      // at the end, stores data
      for (final AttachmentDefinition attachment : process.getAttachments().values()) {
        if (attachment.getFilePath() != null) {
          final byte[] content = businessArchive.getResource(attachment.getFilePath());
          try {
            manager.createDocument(attachment.getName(), attachment.getProcessDefinitionUUID(),
                attachment.getFileName(), DocumentService.DEFAULT_MIME_TYPE, content);
          } catch (final Exception e) {
            throw new BonitaRuntimeException(e);
          }
        } else {
          try {
            manager.createDocument(attachment.getName(), attachment.getProcessDefinitionUUID());
          } catch (final Exception e) {
            throw new BonitaRuntimeException(e);
          }
        }
      }
      InternalProcessDefinition internalProcess = null;
      try {
        Thread.currentThread().setContextClassLoader(EnvTool.getClassDataLoader().getProcessClassLoader(processUUID));
        internalProcess = new InternalProcessDefinition(process);

        if (internalProcess.getCategoryNames() != null && !internalProcess.getCategoryNames().isEmpty()) {
          final Collection<String> categoryNamesToBind = new ArrayList<String>(internalProcess.getCategoryNames());
          final Set<Category> persistedCategories = EnvTool.getJournalQueriers().getCategories(categoryNamesToBind);
          if (persistedCategories.size() < categoryNamesToBind.size()) {
            // some categories do not yet exist
            for (final Category category : persistedCategories) {
              categoryNamesToBind.remove(category.getName());
            }
            // create missing categories
            for (final String name : categoryNamesToBind) {
              recorder.recordNewCategory(new CategoryImpl(name));
            }
          }
        }
        // Keep tracking of number of attachments.
        internalProcess.setNbOfAttachments(process.getAttachments().size());
        recorder.recordProcessDeployed(internalProcess, EnvTool.getUserId());

        addStartEvents(internalProcess);
      } finally {
        Thread.currentThread().setContextClassLoader(current);
      }
      return new ProcessDefinitionImpl(internalProcess);
    } catch (final RuntimeException re) {
      EnvTool.getClassDataLoader().removeProcessClassLoader(processUUID);
      if (removeDeps) {
        ldr.deleteData(Misc.getBusinessArchiveCategories(processUUID));
        ldr.deleteData(Misc.getAttachmentCategories(processUUID));
      }
      throw re;
    }
  }

  public static IterationProcess getIterationProcess(final ProcessDefinition process) {
    final IterationProcess iterationProcess = new IterationProcess();
    for (final ActivityDefinition activityDefinition : process.getActivities()) {
      final String joinType = activityDefinition.getJoinType().toString();
      final String splitType = activityDefinition.getSplitType().toString();
      final IterationNode node = new IterationNode(activityDefinition.getName(),
          org.ow2.bonita.iteration.IterationNode.JoinType.valueOf(joinType),
          org.ow2.bonita.iteration.IterationNode.SplitType.valueOf(splitType));
      iterationProcess.addNode(node);
    }
    for (final ActivityDefinition activityDefinition : process.getActivities()) {
      final IterationNode node = iterationProcess.getNode(activityDefinition.getName());
      for (final TransitionDefinition transition : activityDefinition.getIncomingTransitions()) {
        final IterationNode source = iterationProcess.getNode(transition.getFrom());
        node.addIncomingTransition(new IterationTransition(source, node));
      }
      for (final TransitionDefinition transition : activityDefinition.getOutgoingTransitions()) {
        final IterationNode destination = iterationProcess.getNode(transition.getTo());
        node.addOutgoingTransition(new IterationTransition(node, destination));
      }
      for (final BoundaryEvent boundaryEvent : activityDefinition.getBoundaryEvents()) {
        final IterationNode destination = iterationProcess.getNode(boundaryEvent.getTransition().getTo());
        node.addOutgoingTransition(new IterationTransition(node, destination));
      }
    }
    return iterationProcess;
  }

  public static Set<IterationDescriptor> getIterations(final IterationProcess process) {
    return IterationDetection.findIterations(process);
  }

  public static Set<IterationDescriptor> getIterations(final IterationProcess process, final boolean throwException) {
    return IterationDetectionNoException.findIterations(process);
  }

  public static boolean enableProcess(final ProcessDefinitionUUID processUUID) {
    Misc.badStateIfNull(processUUID, "Impossible to enable a processUUID from a null uuid");
    final Querier journal = EnvTool.getJournalQueriers();
    final InternalProcessDefinition processDef = journal.getProcess(processUUID);
    if (processDef == null) {
      final String message = ExceptionManager.getInstance().getFullMessage("bd_D_8", processUUID);
      throw new DeploymentRuntimeException(message);
    }
    if (!processDef.getState().equals(ProcessState.DISABLED)) {
      throw new DeploymentRuntimeException("Impossible to enable a process which is state is not disable");
    } else {
      final Recorder recorder = EnvTool.getRecorder();
      recorder.recordProcessEnable(processDef);
      addStartEvents(processDef);
      if (LOG.isLoggable(Level.INFO)) {
        final StringBuilder logMsg = new StringBuilder();
        logMsg.append("Process ").append(processDef.getName()).append(" enable (UUID: ").append(processDef.getUUID())
            .append(").");
        LOG.info(logMsg.toString());
      }
      return true;
    }
  }

  public static boolean disableProcess(final ProcessDefinitionUUID processUUID) {
    Misc.badStateIfNull(processUUID, "Impossible to disable a processUUID from a null uuid");
    final Querier journal = EnvTool.getJournalQueriers();
    final InternalProcessDefinition processDef = journal.getProcess(processUUID);
    if (processDef == null) {
      final String message = ExceptionManager.getInstance().getFullMessage("bd_D_8", processUUID);
      throw new DeploymentRuntimeException(message);
    }
    if (!processDef.getState().equals(ProcessState.ENABLED)) {
      throw new DeploymentRuntimeException("Impossible to disable a process which is state is not enable");
    } else {
      removeStartEvents(processDef);

      final Recorder recorder = EnvTool.getRecorder();
      recorder.recordProcessDisable(processDef);
      if (LOG.isLoggable(Level.INFO)) {
        final StringBuilder logMsg = new StringBuilder();
        logMsg.append("Process ").append(processDef.getName()).append(" disable (UUID: ").append(processDef.getUUID())
            .append(").");
        LOG.info(logMsg.toString());
      }
      return true;
    }
  }

  public static boolean archiveProcess(final ProcessDefinitionUUID processUUID, final String userId) {
    Misc.badStateIfNull(processUUID, "Impossible to archive a processUUID from a null uuid");
    final Querier journal = EnvTool.getJournalQueriers();
    final InternalProcessDefinition processDef = journal.getProcess(processUUID);
    if (processDef == null) {
      final String message = ExceptionManager.getInstance().getFullMessage("bd_D_8", processUUID);
      throw new DeploymentRuntimeException(message);
    }
    if (!processDef.getState().equals(ProcessState.DISABLED)) {
      throw new DeploymentRuntimeException("Impossible to archive a process which its state is not disable");
    } else {
      final Set<ProcessDefinitionUUID> definitionUUIDs = new HashSet<ProcessDefinitionUUID>();
      definitionUUIDs.add(processUUID);
      final int numberOfProcessInstances = journal.getNumberOfProcessInstances(definitionUUIDs);
      if (numberOfProcessInstances != 0) {
        throw new DeploymentRuntimeException("Impossible to archive a process with " + numberOfProcessInstances
            + " running instance(s)");
      } else {
        removeStartEvents(processDef);
        final Recorder recorder = EnvTool.getRecorder();
        recorder.recordProcessArchive(processDef, userId);
        ArchiveTool.atomicArchive(processDef);
        if (LOG.isLoggable(Level.INFO)) {
          final StringBuilder logMsg = new StringBuilder();
          logMsg.append("Process ").append(processDef.getName()).append(" archive (UUID: ")
              .append(processDef.getUUID()).append(").");
          LOG.info(logMsg.toString());
        }
        EnvTool.getClassDataLoader().removeProcessClassLoader(processDef.getUUID());
        EnvTool.getUUIDService().archiveOrDeleteProcess(processUUID);
        return true;
      }
    }
  }

  private static void addStartEvents(final ProcessDefinition processDef) throws DeploymentRuntimeException {
    // install "start events" if necessary
    final EventService eventService = EnvTool.getEventService();
    eventService.lockProcessDefinition(processDef.getUUID());
    if (ProcessType.PROCESS.equals(processDef.getType())) {
      for (final ActivityDefinition activity : processDef.getActivities()) {
        if (activity.getIncomingTransitions().isEmpty()) {
          final Type activityType = activity.getType();
          if (activityType.equals(Type.ReceiveEvent)) {
            final IncomingEventDefinition event = activity.getIncomingEvent();
            if (event != null) {
              final IncomingEventInstance eventInstance = new IncomingEventInstance(event.getName(),
                  event.getExpression(), null, activity.getUUID(), null, processDef.getName(), activity.getName(),
                  null, EventConstants.START, System.currentTimeMillis(), false);
              eventInstance.setPermanent(true);
              ProcessUtil.addCorrelationKeys(event, eventInstance, processDef.getUUID());
              eventService.subscribe(eventInstance);
            }
          } else if (activityType.equals(Type.Timer)) {
            final ProcessDefinitionUUID definitionUUID = activity.getProcessDefinitionUUID();
            final String condition = activity.getTimerCondition();
            try {
              final Date date = ProcessUtil.getTimerDate(condition, definitionUUID, System.currentTimeMillis());
              final Job timer = JobBuilder.startTimerJob(activity.getName(), activity.getUUID(), condition,
                  date.getTime());
              eventService.storeJob(timer);
            } catch (final GroovyException e) {
              throw new DeploymentRuntimeException(e.getMessage(), e.getCause());
            }
          } else if (activityType.equals(Type.SignalEvent)) {
            final String eventName = activity.getTimerCondition();
            final IncomingEventInstance signalEventInstance = new IncomingEventInstance(eventName, null, null,
                activity.getUUID(), null, processDef.getName(), activity.getName(), null,
                EventConstants.SIGNAL_START_EVENT, -1, false);
            signalEventInstance.setPermanent(true);
            eventService.subscribe(signalEventInstance);
          }
        }
      }
    }
  }

  public static void removeStartEvents(final ProcessDefinition processDef) {
    final EventService eventService = EnvTool.getEventService();
    eventService.removeLock(processDef.getUUID());
    for (final ActivityDefinition activity : processDef.getActivities()) {
      if (activity.getIncomingTransitions().isEmpty()) {
        final Type activityType = activity.getType();
        final IncomingEventDefinition event = activity.getIncomingEvent();
        if (event != null || activityType.equals(Type.SignalEvent)) {
          eventService.removeSubscriptions(activity.getUUID());
          eventService.removeJob(activity.getUUID());
        } else if (activityType.equals(Type.Timer) || activityType.equals(Type.ErrorEvent)) {
          eventService.removeJob(activity.getUUID());
        }
      }
    }
  }

}
