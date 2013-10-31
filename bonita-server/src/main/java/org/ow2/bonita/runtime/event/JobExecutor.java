/**
 * Copyright (C) 2102-2013 BonitaSoft S.A.
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import org.ow2.bonita.runtime.event.EventExecutor.EventRejectionHandler;
import org.ow2.bonita.util.Command;

/**
 * 
 * @author Matthieu Chaffotte
 * 
 */
public abstract class JobExecutor extends EventExecutorThread {

  private final int locksToQuery;
  private final int lockIdleTime;
  private final int nbrOfThreads;

  private final Map<String, Future<?>> runningThreads = new ConcurrentHashMap<String, Future<?>>();
  private final Map<String, Long> lockedProcessUUIDs = new ConcurrentHashMap<String, Long>();

  private transient ThreadPoolExecutor threadPool;

  JobExecutor(final EventExecutor executor, final String name, final int nbrOfThreads, final int locksToQuery,
      final int lockIdleTime) {
    super(executor, name);
    this.locksToQuery = locksToQuery;
    this.lockIdleTime = lockIdleTime;
    this.nbrOfThreads = nbrOfThreads;
    threadPool = new ThreadPoolExecutor(nbrOfThreads, nbrOfThreads, 0L, TimeUnit.MILLISECONDS,
        new ArrayBlockingQueue<Runnable>(nbrOfThreads), EventRejectionHandler.INSTANCE);
  }

  @Override
  protected final void activate() {

  }

  public int getLockIdleTime() {
    return lockIdleTime;
  }

  public int getLocksToQuery() {
    return locksToQuery;
  }

  @Override
  public void deactivate(final boolean join) {
    if (LOG.isLoggable(Level.INFO)) {
      LOG.info("Event executor: shutdown threadpool...");
    }
    threadPool.shutdown();
    if (LOG.isLoggable(Level.INFO)) {
      LOG.info("Event executor: threadpool shutdowned.");
    }
    if (join) {
      try {
        if (LOG.isLoggable(Level.INFO)) {
          LOG.info("Event executor: waiting for threadPool termination...");
        }
        threadPool.awaitTermination(1000 * 60 * 5, TimeUnit.MILLISECONDS);
        if (LOG.isLoggable(Level.INFO)) {
          LOG.info("Event executor: threadPool termination OK...");
        }
      } catch (final InterruptedException e) {
        LOG.severe("joining got interrupted");
      }
    }
    super.deactivate(join);
  }

  protected abstract Command<List<String>> getNonlockedProcessUUIDsCommand(final Set<String> processUUIDsToExclude);

  protected abstract boolean lockJob(final String processUUID);

  protected abstract List<Job> getLockedJobs(final String processUUID);

  protected abstract void releaseLock(final String processUUID);

  protected void notifyThreadFinished(final String processUUID) {
    runningThreads.remove(processUUID);
    refresh();
  }

  @Override
  protected void execute() throws InterruptedException {
    cleanRunningThreads();
    cleanLockedProcessUUIDs();

    final List<String> nonLockedProcessUUIDs = getCommandService().execute(
        getNonlockedProcessUUIDsCommand(getProcessUUIDsToExclude()));
    if (LOG.isLoggable(Level.FINE)) {
      LOG.fine("On " + getJobExecutorName() + ", nonLockedProcessUUIDs(" + nonLockedProcessUUIDs.size() + ") = "
          + nonLockedProcessUUIDs);
    }

    if (nonLockedProcessUUIDs.size() > 0) {

      // submit one more runnable than the number of available threads to ensure
      // we are not looping for ever waiting for a thread to be available. This
      // way, a call to submit is blocking
      final int availableThreads = nbrOfThreads - threadPool.getActiveCount() + nbrOfThreads * 3;
      final int nbOfElementsTohandle = Math.min(availableThreads, nonLockedProcessUUIDs.size());

      final List<String> processUUIDsToHandleInRandomOrder = new ArrayList<String>(nbOfElementsTohandle);

      if (nonLockedProcessUUIDs.size() < availableThreads) {
        // we are going to process all of them, save the random time
        processUUIDsToHandleInRandomOrder.addAll(nonLockedProcessUUIDs);
      } else {
        final Random random = new Random(System.currentTimeMillis());
        for (int i = 0; i < nbOfElementsTohandle; i++) {
          final int indexToTake = random.nextInt(nonLockedProcessUUIDs.size());
          if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("On " + getJobExecutorName() + ", selecting a processUUID for thread nb (loop): " + i
                + ", indexToTake: " + indexToTake);
          }
          final String selectedUUID = nonLockedProcessUUIDs.remove(indexToTake);
          if (!processUUIDsToHandleInRandomOrder.contains(selectedUUID)) {
            if (LOG.isLoggable(Level.FINE)) {
              LOG.fine("On " + getJobExecutorName() + ", adding processUUID for thread nb (loop): " + selectedUUID
                  + " to list of processUUIDs to handle");
            }
            processUUIDsToHandleInRandomOrder.add(selectedUUID);
          }
        }
      }

      final List<String> processUUIDsToHandle = removeDuplicates(processUUIDsToHandleInRandomOrder);
      for (final String processUUID : processUUIDsToHandle) {
        final JobExecutorThread thread = new JobExecutorThread(getEventExecutor(), new ExecuteJobsCommand(
            getEventExecutor(), this, processUUID));
        if (LOG.isLoggable(Level.FINE)) {
          LOG.fine("On " + getJobExecutorName() + ", trying to process processUUID = " + processUUID);
        }
        // this call is blocking if we have reached the max capacity
        final Future<?> runningThread = threadPool.submit(thread);
        runningThreads.put(processUUID, runningThread);
      }
    }
  }

  private List<String> removeDuplicates(final List<String> original) {
    final List<String> result = new ArrayList<String>();
    for (final String s : original) {
      if (!result.contains(s)) {
        result.add(s);
      }
    }
    return result;
  }

  private void cleanRunningThreads() {
    final List<String> processUUIDsToRemove = new ArrayList<String>();
    for (final Map.Entry<String, Future<?>> runningThread : runningThreads.entrySet()) {
      if (runningThread.getValue().isDone()) {
        processUUIDsToRemove.add(runningThread.getKey());
      }
    }
    for (final String processUUIDToRemove : processUUIDsToRemove) {
      runningThreads.remove(processUUIDToRemove);
    }
  }

  private void cleanLockedProcessUUIDs() {
    final long now = System.currentTimeMillis();
    final List<String> processUUIDsToRemove = new ArrayList<String>();
    for (final Map.Entry<String, Long> lockedProcessUUID : lockedProcessUUIDs.entrySet()) {
      if (lockedProcessUUID.getValue() < now) {
        processUUIDsToRemove.add(lockedProcessUUID.getKey());
      }
    }
    for (final String processUUIDToRemove : processUUIDsToRemove) {
      lockedProcessUUIDs.remove(processUUIDToRemove);
    }
  }

  protected Set<String> getProcessUUIDsToExclude() {
    final Set<String> processUUIDsToExclude = new HashSet<String>();
    processUUIDsToExclude.addAll(runningThreads.keySet());
    processUUIDsToExclude.addAll(lockedProcessUUIDs.keySet());
    return processUUIDsToExclude;
  }

  protected void addLockedProcessUUID(final String processUUID) {
    final long now = System.currentTimeMillis();
    lockedProcessUUIDs.put(processUUID, now + lockIdleTime);
  }

}
