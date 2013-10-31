package org.ow2.bonita.runtime.event;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.transaction.Synchronization;

import org.hibernate.AssertionFailure;
import org.hibernate.StaleStateException;
import org.hibernate.exception.LockAcquisitionException;
import org.ow2.bonita.definition.activity.ConnectorExecutor;
import org.ow2.bonita.env.Authentication;
import org.ow2.bonita.env.Environment;
import org.ow2.bonita.env.Transaction;
import org.ow2.bonita.facade.QueryRuntimeAPI;
import org.ow2.bonita.facade.RuntimeAPI;
import org.ow2.bonita.facade.def.InternalActivityDefinition;
import org.ow2.bonita.facade.def.element.HookDefinition.Event;
import org.ow2.bonita.facade.exception.DocumentNotFoundException;
import org.ow2.bonita.facade.exception.InstanceNotFoundException;
import org.ow2.bonita.facade.exception.ProcessNotFoundException;
import org.ow2.bonita.facade.impl.StandardAPIAccessorImpl;
import org.ow2.bonita.facade.runtime.impl.InternalActivityInstance;
import org.ow2.bonita.facade.runtime.impl.InternalProcessInstance;
import org.ow2.bonita.facade.uuid.ActivityDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.runtime.model.Execution;
import org.ow2.bonita.services.Document;
import org.ow2.bonita.services.DocumentationManager;
import org.ow2.bonita.services.EventService;
import org.ow2.bonita.services.Querier;
import org.ow2.bonita.services.Recorder;
import org.ow2.bonita.util.BonitaConstants;
import org.ow2.bonita.util.BonitaRuntimeException;
import org.ow2.bonita.util.Command;
import org.ow2.bonita.util.DocumentService;
import org.ow2.bonita.util.EnvTool;
import org.ow2.bonita.util.GroovyException;
import org.ow2.bonita.util.ProcessUtil;

public class ExecuteJobsCommand implements Command<Void> {

	private static final long serialVersionUID = 1L;
	protected static final Logger LOG = Logger.getLogger(ExecuteJobsCommand.class.getName());

	protected final EventExecutor eventExecutor;
	protected final JobExecutor jobExecutor;
	protected final String processUUID;

	public ExecuteJobsCommand(EventExecutor eventExecutor, final JobExecutor jobExecutor, String processUUID) {
		super();
		this.eventExecutor = eventExecutor;
		this.jobExecutor = jobExecutor;
		this.processUUID = processUUID;
	}

	public Void execute(Environment environment) throws Exception {
		boolean locked = false; 
		try {
			final long before = System.currentTimeMillis();
			try {
				locked = jobExecutor.lockJob(processUUID);
			} catch (Exception e) {
				//WARNING, only true because this is the first statement of the command in the transaction
				if (LOG.isLoggable(Level.FINE)) {
					LOG.fine("On " + jobExecutor.getJobExecutorName() + ", acquisition of lock on processUUID = " + processUUID + " failed******");
				}
			}
			final long after = System.currentTimeMillis();
			if (LOG.isLoggable(Level.FINE)) {
				LOG.fine("On " + jobExecutor.getJobExecutorName() + ", acquisition of lock on processUUID = " + processUUID + " resulted in: " + locked + ", lockTime = " + (after - before));
			}
			if (locked) {
				List<Job> jobs = null;
					jobs = jobExecutor.getLockedJobs(processUUID);
					for (final Job job : jobs) {
						if (LOG.isLoggable(Level.FINE)) {
							LOG.fine("On " + jobExecutor.getJobExecutorName() + ", executing job:" + job.getId());
						}
						executeJob(environment, job);
						//we cannot execute more than one job at a time in the same tx...
						break;
					}
			} else {
				jobExecutor.addLockedProcessUUID(processUUID);
				if (LOG.isLoggable(Level.FINE)) {
					LOG.fine("ProcessUUID: " + processUUID + " is already locked by another execution flow.");
				}
			}
			EnvTool.getTransaction().registerSynchronization(new Synchronization() {
				@Override
				public void beforeCompletion() {
				}
				
				@Override
				public void afterCompletion(int status) {
					jobExecutor.notifyThreadFinished(processUUID);
				}
			});
			return null;
		} catch (Exception e) {
			if (LOG.isLoggable(Level.WARNING)) {
				LOG.log(Level.WARNING, "Exception while executing " + ExecuteJobsCommand.class.getSimpleName() + " on " + jobExecutor.getJobExecutorName(), e);
			}
			throw e;
		} finally {
			if (locked) {
				jobExecutor.releaseLock(processUUID);
			}
		}
	}

	protected void executeJob(final Environment environment, final Job job) throws Exception {
		final String eventPosition = job.getEventPosition();
		try {
			if (EventConstants.START.equals(eventPosition)) {
				instantiateStartEvent(job);
			} else if (EventConstants.BOUNDARY.equals(eventPosition)
					|| EventConstants.EXECUTE_CONNECTORS_AUTOMATIC_ON_ENTER.equals(eventPosition)
					|| EventConstants.CONNECTORS_AUTOMATIC_ON_ENTER_EXECUTED.equals(eventPosition)) {
				handleJobsWithExecution(job);
			} else {
				executeEvent(job);
			}
		} catch (final StaleStateException sse) {
			throw sse;
		} catch (final AssertionFailure af) {
			throw af;
		} catch (final LockAcquisitionException lae) {
			throw lae;
		} catch (final Throwable exception) {
			final String executionUUID = job.getExecutionUUID();
			if (job.getRetries() == 1 && executionUUID != null) {
				final Execution execution = EnvTool.getJournal().getExecutionWithEventUUID(executionUUID);
				// compares with 1 because number of retries will be decremented in
				// handle exception.
				final InternalActivityInstance activityInstance = execution.getActivityInstance();
				final Recorder recorder = EnvTool.getRecorder();
				recorder.recordActivityFailed(activityInstance);
				if (LOG.isLoggable(Level.SEVERE)) {
					LOG.severe("The activity \"" + activityInstance.getUUID()
							+ "\" is in the state FAILED because an exception caught while executing job: " + exception
							+ ". Exception: " + exception.getMessage());
				}
			} else {
				if (LOG.isLoggable(Level.INFO)) {
					LOG.info("Exception caught while executing job: " + exception + ". Exception: " + exception.getMessage());
				}
				if (LOG.isLoggable(Level.INFO)) {
					LOG.info("handling job: " + job + " exception: " + exception.getMessage());
				}
				handleException(environment, job, exception);
			}
		}
	}

	private void instantiateStartEvent(final Job job) throws GroovyException, ProcessNotFoundException,
	InstanceNotFoundException {
		final EventService eventService = EnvTool.getEventService();
		final InternalActivityDefinition activity = EnvTool.getJournal().getActivity(job.getActivityDefinitionUUID());
		final ProcessDefinitionUUID processUUID = activity.getProcessDefinitionUUID();

		Authentication.setUserId(BonitaConstants.SYSTEM_USER);
		final StandardAPIAccessorImpl accessor = new StandardAPIAccessorImpl();
		final RuntimeAPI runtimeAPI = accessor.getRuntimeAPI();
		final ProcessInstanceUUID eventSubProcessRootInstanceUUID = job.getEventSubProcessRootInstanceUUID();
		if (eventSubProcessRootInstanceUUID != null) {
			instantiateStartEventSubProcess(job);
		} else {
			final ActivityDefinitionUUID activityUUID = job.getActivityDefinitionUUID();
			final ProcessInstanceUUID instanceUUID = runtimeAPI.instantiateProcess(processUUID, activityUUID);
			if (EventConstants.TIMER.equals(job.getEventType())) {
				final String condition = job.getExpression();
				final long lastExecution = job.getFireTime();
				final Date nextTime = ProcessUtil.getTimerDate(condition, processUUID, lastExecution);
				if (lastExecution == nextTime.getTime()) {
					if (LOG.isLoggable(Level.WARNING)) {
						LOG.warning("A start timer event of process " + processUUID + " ends its cycle.");
					}
					eventService.removeJob(job);
				} else {
					job.renewFireTime(nextTime.getTime());
					this.eventExecutor.refreshJobExecutor();
				}
			} else {
				if (LOG.isLoggable(Level.INFO)) {
					LOG.info("An event started a new process instance of " + processUUID);
				}
				if (EventConstants.MESSAGE.equals(job.getEventType())) {
					/*
					final IncomingEventInstance newIncoming = eventService.getIncomingEvent(instanceUUID, job.getEventName());
					executeEvent(job, newIncoming.getExecutionUUID());
					*/
					final String eventUUID = instanceUUID + "-----START-----";
					executeEvent(job, eventUUID);
					
				} else {
					eventService.removeJob(job);
				}
			}
		}
	}

	private void instantiateStartEventSubProcess(final Job job) throws InstanceNotFoundException,
	ProcessNotFoundException {
		final Querier journal = EnvTool.getJournalQueriers();
		final Set<Execution> executions = journal.getExecutions(job.getEventSubProcessRootInstanceUUID());
		try {
			final Execution current = executions.iterator().next();
			current.abort();
			final Recorder recorder = EnvTool.getRecorder();
			recorder.recordInstanceAborted(job.getEventSubProcessRootInstanceUUID(), EnvTool.getUserId());
		} catch (final BonitaRuntimeException e) {
			if (LOG.isLoggable(Level.WARNING)) {
				LOG.log(Level.WARNING, "The process: " + job.getEventSubProcessRootInstanceUUID()
						+ " has already been aborted by another start event.", e);
			}
		}
		final StandardAPIAccessorImpl accessor = new StandardAPIAccessorImpl();
		final QueryRuntimeAPI queryRuntimeAPI = accessor.getQueryRuntimeAPI();
		final Map<String, Object> variables = queryRuntimeAPI.getProcessInstanceVariables(job
				.getEventSubProcessRootInstanceUUID());

		final InternalActivityDefinition activity = EnvTool.getJournal().getActivity(job.getActivityDefinitionUUID());
		final ProcessDefinitionUUID processUUID = activity.getProcessDefinitionUUID();
		final InternalProcessInstance eventSubProcessInstance = instantiateEventSubProcess(processUUID, variables,
				job.getEventSubProcessRootInstanceUUID());
		final ProcessInstanceUUID instanceUUID = eventSubProcessInstance.getUUID();
		final InternalProcessInstance processInstance = EnvTool.getAllQueriers().getProcessInstance(
				job.getEventSubProcessRootInstanceUUID());
		if (processInstance.getNbOfAttachments() > 0) {
			final DocumentationManager manager = EnvTool.getDocumentationManager();
			final List<Document> lastAttachments = DocumentService.getAllDocumentVersions(manager,
					job.getEventSubProcessRootInstanceUUID());
			for (final Document document : lastAttachments) {
				try {
					manager.attachDocumentTo(processUUID, instanceUUID, document.getId());
				} catch (final DocumentNotFoundException e) {
					new BonitaRuntimeException(e);
				}
			}
			eventSubProcessInstance.setNbOfAttachments(processInstance.getNbOfAttachments());
		}

		if (LOG.isLoggable(Level.INFO)) {
			LOG.info("An event started a new process instance of " + processUUID);
		}
		final EventService eventService = EnvTool.getEventService();
		if (EventConstants.MESSAGE.equals(job.getEventType())) {
			/*
			final IncomingEventInstance newIncoming = eventService.getIncomingEvent(instanceUUID, job.getEventName());
			executeEvent(job, newIncoming.getExecutionUUID());
			*/
			final String eventUUID = instanceUUID + "-----START-----";
			executeEvent(job, eventUUID);
		} else {
			eventService.removeJob(job);
		}
	}

	private InternalProcessInstance instantiateEventSubProcess(final ProcessDefinitionUUID processUUID,
			final Map<String, Object> variables, final ProcessInstanceUUID rootEventSubProcessInstanceUUID)
					throws ProcessNotFoundException {
		if (LOG.isLoggable(Level.FINE)) {
			LOG.fine("Starting a new instance of process : " + processUUID);
		}
		final Querier journal = EnvTool.getJournalQueriers();
		final InternalProcessInstance rootInstance = journal.getProcessInstance(rootEventSubProcessInstanceUUID);
		final Execution rootExecution = ProcessUtil.createProcessInstance(processUUID, variables, null,
				rootEventSubProcessInstanceUUID, rootInstance.getRootInstanceUUID(), null, null);
		final InternalProcessInstance instance = rootExecution.getInstance();
		if (LOG.isLoggable(Level.FINE)) {
			LOG.fine("Started: " + instance);
		}
		instance.begin(null);
		return instance;
	}

	private void handleJobsWithExecution(final Job job) throws Exception {
		final EventService eventService = EnvTool.getEventService();
		eventService.removeJob(job);

		Authentication.setUserId(BonitaConstants.SYSTEM_USER);
		final String executionUUID = job.getExecutionUUID();
		final Execution execution = EnvTool.getJournalQueriers().getExecutionWithEventUUID(executionUUID);
		if (execution == null) {
			if (LOG.isLoggable(Level.INFO)) {
				LOG.info("No active execution found for uuid: " + executionUUID);
			}
			throw new BonitaRuntimeException("No active execution found with uuid: " + executionUUID);
		} else {
			if (LOG.isLoggable(Level.INFO)) {
				LOG.info("Execution found for uuid: " + executionUUID);
			}
			if (!execution.isActive()) {
				execution.unlock();
			}
			final String eventPosition = job.getEventPosition();
			final Map<String, Object> parameters = new HashMap<String, Object>();
			parameters.put("eventName", job.getEventName());

			if (EventConstants.EXECUTE_CONNECTORS_AUTOMATIC_ON_ENTER.equals(eventPosition)) {
				ConnectorExecutor.executeConnectors(execution.getNode(), execution, Event.automaticOnEnter);
				final String eventName = BonitaConstants.CONNECTOR_AUTOMATIC_ON_ENTER_EXECUTED_PREFIX
						+ execution.getActivityInstanceId();
				final Job sigConnecAutOnEnterexecutedJob = JobBuilder.signalConnectorsAutoOnEnterExecutedJob(eventName,
						execution.getInstance().getRootInstanceUUID(), execution.getEventUUID(), execution.getInstance()
						.getProcessInstanceUUID());
				eventService.storeJob(sigConnecAutOnEnterexecutedJob);
			} else if (EventConstants.CONNECTORS_AUTOMATIC_ON_ENTER_EXECUTED.equals(eventPosition)) {
				execution.signal(EventConstants.CONNECTORS_AUTOMATIC_ON_ENTER_EXECUTED, parameters);
			} else if (EventConstants.BOUNDARY.equals(eventPosition)) {
				execution.signal(EventConstants.BOUNDARY, parameters);
			} else {
				throw new BonitaRuntimeException("Unexpected event position: " + eventPosition);
			}
		}
	}

	private void executeEvent(final Job job, final String executionId) {
		Authentication.setUserId(BonitaConstants.SYSTEM_USER);
		final String execId = executionId == null ? job.getExecutionUUID() : executionId;
		final Execution execution = EnvTool.getJournal().getExecutionWithEventUUID(execId);
		if (execution == null) {
			if (LOG.isLoggable(Level.INFO)) {
				LOG.info("No active execution found for identifer: " + execId);
			}
			throw new BonitaRuntimeException("No active execution found for identifer: " + execId);
		} else {
			if (LOG.isLoggable(Level.FINE)) {
				LOG.fine("Execution found for identifer: " + execId);
			}
			if (!Execution.STATE_ACTIVE.equals(execution.getState())) {
				execution.unlock();
			}
			final EventService eventService = EnvTool.getEventService();
			eventService.removeJob(job);
			String signal = job.getEventType();
			final Map<String, Object> parameters = new HashMap<String, Object>();
			if (job.getOutgoingEvent() != null) {
				parameters.putAll(job.getOutgoingEvent().getParameters());
				if (job.getEventSubProcessRootInstanceUUID() == null) {
					eventService.removeEvent(job.getOutgoingEvent());
				}
			} else if (EventConstants.DEADLINE.equals(job.getEventPosition())) {
				signal = job.getEventPosition();
				parameters.put("id", Long.valueOf(job.getEventName()));
			}
			execution.signal(signal, parameters);
		}
	}

	private void executeEvent(final Job job) {
		executeEvent(job, null);
	}

	private void handleException(final Environment environment, final Job job, final Throwable exception) {
		final Transaction transaction = environment.get(Transaction.class);
		final JobExceptionHandler handler = new JobExceptionHandler(job.getId(), exception, this.eventExecutor.getCommandService());
		transaction.registerSynchronization(handler);
		if (exception instanceof RuntimeException) {
			throw (RuntimeException) exception;
		}
		throw new BonitaRuntimeException("Execution of job (" + job + ") failed: " + exception.getMessage(), exception);
	}


}
