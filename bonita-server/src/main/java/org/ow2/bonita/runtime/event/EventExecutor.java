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
package org.ow2.bonita.runtime.event;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ow2.bonita.services.CommandService;
import org.ow2.bonita.util.EnvTool;
import org.ow2.bonita.util.Misc;
import org.ow2.bonita.util.ReflectUtil;

/**
 * 
 * @author Charles Souillard, Matthieu Chaffotte
 * 
 */
public class EventExecutor implements Serializable {

  private static final Logger LOG = Logger.getLogger(EventExecutor.class.getName());

  private static final long serialVersionUID = 1L;

  // injected
  transient CommandService commandService;

  int nbrOfThreads = 3;

  int idleMillis = 5000; // default normal poll interval is 5 seconds

  int lockMillis = 120000; // default max lock time is 2 minutes

  int minimumInterval = 50;

  int retries = 1;

  boolean expressionMatcherEnable = true;

  int matcherMaxCouples = 50;

  String name;

  private transient EventMatcher eventMatcher;

  String jobExecutorClassName;

  String eventMatcherClassName;

  String masterCheckerClassName;

  private transient JobExecutor jobExecutor = null;

  int locksToQuery = 50;

  int lockIdleTime = 5000;

  boolean cleanLocks = false;

  private boolean isActive = false;

  private String domain;

  private MasterChecker masterChecker;

  boolean masterCheckerEnable;

  int masterCheckerMasterHeartbeatDelay;

  int masterCheckerSlaveHeartbeatDelay;

  int masterCheckerMaxIdleDelay;

  public JobExecutor getJobExecutor() {
    return jobExecutor;
  }

  public synchronized void start() {
    domain = commandService.execute(new GetDomainCommand());
    name = EventExecutor.class.getSimpleName() + "-" + Misc.getHostName() + "-" + domain;
    if (isActive) {
      LOG.severe("Cannot start event executor '" + name + "' because it is already running...");
      return;
    }
    if (!isActive) {
      if (LOG.isLoggable(Level.INFO)) {
        LOG.info("starting event executor threads for event executor '" + name + "'...");
      }

      if (LOG.isLoggable(Level.INFO)) {
        LOG.info("Starting job dispatcher thread for executing events '" + name + "'...");
      }

      final Class<JobExecutor> jobExecutorClass = (Class<JobExecutor>) ReflectUtil.loadClass(Thread.currentThread()
          .getContextClassLoader(), jobExecutorClassName);
      final Constructor<JobExecutor> jobExecutorConstructor = ReflectUtil.getConstructor(jobExecutorClass, new Class[] {
          EventExecutor.class, String.class, int.class, int.class, int.class });
      jobExecutor = ReflectUtil.newInstance(jobExecutorConstructor,
          new Object[] { this, jobExecutorClass.getSimpleName() + "-" + Misc.getHostName() + "-" + domain,
              nbrOfThreads, locksToQuery, lockIdleTime });
      // jobExecutor.setMaxParallelJobs(maxParallelJobs);
      jobExecutor.start();

      final Class<MasterChecker> masterCheckerClass = (Class<MasterChecker>) ReflectUtil.loadClass(Thread
          .currentThread().getContextClassLoader(), masterCheckerClassName);
      final Constructor<MasterChecker> masterCheckerConstructor = ReflectUtil.getConstructor(masterCheckerClass,
          new Class[] { EventExecutor.class, String.class });
      masterChecker = ReflectUtil.newInstance(masterCheckerConstructor,
          new Object[] { this, masterCheckerClass.getSimpleName() + "-" + Misc.getHostName() + "-" + domain });
      masterChecker.setEnable(masterCheckerEnable);
      masterChecker.setMasterHeartbeatDelay(masterCheckerMasterHeartbeatDelay);
      masterChecker.setSlaveHeartbeatDelay(masterCheckerSlaveHeartbeatDelay);
      masterChecker.setMaxIdleDelay(masterCheckerMaxIdleDelay);

      masterChecker.start();

      isActive = true;
    }
  }

  static final class EventRejectionHandler implements RejectedExecutionHandler {

    static final EventRejectionHandler INSTANCE = new EventRejectionHandler();

    @Override
    public void rejectedExecution(final Runnable task, final ThreadPoolExecutor executor) {
      try {
        executor.getQueue().put(task);
      } catch (final InterruptedException e) {
        throw new RejectedExecutionException("queuing " + task + " got interrupted", e);
      }
    }
  }

  public synchronized boolean isActive() {
    return isActive;
  }

  public synchronized void stop() {
    stop(false);
  }

  public synchronized void stop(final boolean join) {
    if (LOG.isLoggable(Level.INFO)) {
      LOG.info("stopping event executor");
    }

    if (isActive) {
      isActive = false;

      if (LOG.isLoggable(Level.INFO)) {
        LOG.info("Event executor: deactivating job dispatcher thread...");
      }
      jobExecutor.deactivate(join);
      if (LOG.isLoggable(Level.INFO)) {
        LOG.info("Event executor: job dispatcher thread deactivated.");
      }

      if (eventMatcher != null) {
        if (LOG.isLoggable(Level.INFO)) {
          LOG.info("Event executor: deactivating event matcher...");
        }
        eventMatcher.deactivate(true);
        if (LOG.isLoggable(Level.INFO)) {
          LOG.info("Event executor: event matcher deactivated.");
        }
      }

      if (masterChecker != null) {
        if (LOG.isLoggable(Level.INFO)) {
          LOG.info("Event executor: deactivating masterChekcer...");
        }
        masterChecker.interrupt();
        if (LOG.isLoggable(Level.INFO)) {
          LOG.info("Event executor: masterChecker deactivated.");
        }
      }
    } else if (LOG.isLoggable(Level.FINE)) {
      LOG.fine("ignoring stop: event executor '" + name + "' not started");
    }
    if (LOG.isLoggable(Level.INFO)) {
      LOG.info("Event executor stopped");
    }
  }

  public int getNbrOfThreads() {
    return nbrOfThreads;
  }

  public int getIdleMillis() {
    return idleMillis;
  }

  public int getLockMillis() {
    return lockMillis;
  }

  public CommandService getCommandService() {
    return commandService;
  }

  public int getMinimumInterval() {
    return minimumInterval;
  }

  public int getRetries() {
    return retries;
  }

  public void setCommandService(final CommandService commandService) {
    this.commandService = commandService;
  }

  public void internalJobExecutorRefresh() {
    if (jobExecutor != null && jobExecutor.isActive()) {
      jobExecutor.refresh();
    }
  }

  public void internalEventMatcherRefresh() {
    if (eventMatcher != null && eventMatcher.isActive()) {
      eventMatcher.refresh();
    }
  }

  public void refreshJobExecutor() {
    EnvTool.getTransaction().registerSynchronization(new NewJobNotification(this));
  }

  public void refreshEventMatcher() {
    EnvTool.getTransaction().registerSynchronization(new EventAddedNotification(this));
  }

  public synchronized boolean isJobExecutorActive() {
    return isActive && jobExecutor != null && jobExecutor.isActive();
  }

  public synchronized boolean isMatcherActive() {
    return isActive && eventMatcher != null && eventMatcher.isActive();
  }

  public synchronized boolean isMatchingConditionMatcherActive() {
    return isMatcherActive() && expressionMatcherEnable;
  }

  public synchronized boolean isMasterCheckerActive() {
    return isActive && masterChecker != null && masterChecker.isActive();
  }

  public synchronized void startMatcher() {
    if (!isMatcherActive()) {
      if (LOG.isLoggable(Level.INFO)) {
        LOG.info("Starting matcher of message correlation based on correlation keys/no expression '" + name + "'...");
      }

      final Class<EventMatcher> eventMatcherClass = (Class<EventMatcher>) ReflectUtil.loadClass(Thread.currentThread()
          .getContextClassLoader(), eventMatcherClassName);
      final Constructor<EventMatcher> constructor = ReflectUtil.getConstructor(eventMatcherClass, new Class[] {
          EventExecutor.class, String.class });
      eventMatcher = ReflectUtil.newInstance(constructor,
          new Object[] { this, eventMatcherClassName + "-" + Misc.getHostName() + "-" + domain });

      eventMatcher.setMaxCouples(matcherMaxCouples);
      eventMatcher.setMatchingConditionMatcher(expressionMatcherEnable);
      eventMatcher.start();
    }
  }

  public synchronized void startMatchingConditionMatcher() {
    if (isMatcherActive()) {
      expressionMatcherEnable = true;
      eventMatcher.setMatchingConditionMatcher(true);
    }
  }

  public synchronized void stopMatcher() {
    if (isMatcherActive()) {
      eventMatcher.deactivate(true);
      eventMatcher = null;
    }
  }

  public synchronized void stopMatchingConditionMatcher() {
    if (isMatchingConditionMatcherActive() && isMatcherActive()) {
      expressionMatcherEnable = false;
      eventMatcher.setMatchingConditionMatcher(false);
    }
  }

  public synchronized void declareMaster() {
    startMatcher();
  }

  public synchronized void declareSlave() {
    stopMatcher();
  }

  public void setMasterChecker(final MasterChecker masterChecker) {
    this.masterChecker = masterChecker;
  }

}
