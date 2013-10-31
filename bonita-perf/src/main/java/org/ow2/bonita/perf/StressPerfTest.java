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
 **/
package org.ow2.bonita.perf;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ow2.bonita.util.AccessorUtil;

/**
 * @author Charles Souillard
 */
public class StressPerfTest {

  private long initialStartTime;

  /**
   * time when the perf test started : first init to start of loadTime and then
   * set to time measures started to be mesured.
   */
  private long startTime;

  /** endTime of the perfTest. */
  private long endTime;

  /** last time a status was printed. Used in combination with timeBetweenPrints. */
  private long lastPrintTime;

  /** number of instances already finished. */
  private long finished = 0;

  /** number of instances already launched. */
  private long launched = 0;

  /**
   * Number of instances finished starting from the beginning of the mesured
   * period.
   */
  private long measuredInstances = 0;

  /** map containing all execution times for every instances. */
  private Map<String, Long> executionTimes = new HashMap<String, Long>();
  private Map<String, Long> bestTimes = new HashMap<String, Long>();
  private Map<String, Long> worthTimes = new HashMap<String, Long>();
  private Map<String, Long> processSuccesses = new HashMap<String, Long>();

  /**
   * Time on which the perfTest should stop launching new instances.
   * expectedEndTime = startTime + warmupTime.
   */
  private long expectedEndTime;

  /**
   * true if we are in the launchPeriod : it means we do not take measures
   * during this period.
   */
  private boolean launchPeriod = true;

  /**
   * true if the test is finished (loadTime finished + all launched thread
   * finished.
   */
  private boolean testFinished = false;

  /** number of instances that finished successfully. */
  private int successNb;

  /** number of instances that finished with an error. */
  private int errorNb;

  /** false while all initial threads are not all launched. */
  private boolean initialLaunchCompleted = false;

  // at a given time, always the same number instance of each type of processes
  static final String ALGO_TYPE = "type";
  // at the end, the same number of instances of each type of process executed
  static final String ALGO_NB = "nb";
  
  private List<PerfTestCase> testsToRun;
  private long threadNb = 1;
  private boolean printFinished = false;
  private boolean printLaunched = false;
  private long timeBetweenVerifications = 10000;
  private long timeBetweenPrints = 60000;
  private long loadTime = 60000;
  private long warmupTime = 30000;
  private long thinkTime = 1000;
  private String algo = ALGO_TYPE;

  public StressPerfTest(final String algo, final long threadNb, final boolean printFinished, final boolean printLaunched, 
      final long timeBetweenVerifications, final long timeBetweenPrints, final long thinkTime, final long loadTime, 
      final long warmupTime, final List<PerfTestCase> testsToRun) throws PerfException {
    this.algo = algo;
    this.threadNb = threadNb;
    this.printFinished = printFinished;
    this.printLaunched = printLaunched;
    this.timeBetweenVerifications = timeBetweenVerifications;
    this.timeBetweenPrints = timeBetweenPrints;
    this.thinkTime = thinkTime;
    this.loadTime = loadTime;
    this.warmupTime = warmupTime;
    this.testsToRun = testsToRun;
    
    for (PerfTestCase perfTestCase : testsToRun) {
      executionTimes.put(perfTestCase.getProcessName(), new Long(0));
      bestTimes.put(perfTestCase.getProcessName(), System.currentTimeMillis());
      worthTimes.put(perfTestCase.getProcessName(), new Long(-1));
      processSuccesses.put(perfTestCase.getProcessName(), new Long(0));
    }
  }

  public void clean() throws Exception {
    AccessorUtil.getManagementAPI().deleteAllProcesses();
  }
  /**
   * Deploys all tests to run.
   */
  public void deployTests() throws PerfException {
    for (PerfTestCase perfTestCase : testsToRun) {
      perfTestCase.deploy();
    }
  }

  /**
   * Undeploys all tests to run.
   */
  public void undeployTests() throws PerfException {
    for (PerfTestCase perfTestCase : testsToRun) {
      perfTestCase.undeploy();
    }
  }

  /**
   * Launch all tests.
   */
  public void launchTests() {
    Collection<Throwable> throwables = new ArrayList<Throwable>();
    initialStartTime = System.currentTimeMillis();
    startTime = initialStartTime;
    expectedEndTime = startTime + loadTime + warmupTime;
    lastPrintTime = startTime;
    long endOfLaunchPeriod = startTime + warmupTime;

    Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
      public void uncaughtException(Thread t, Throwable throwable) {
        exit(throwable, "Error caught, aborting the test");
      }
    });

    synchronized (this) {
      for (int i = 0; i < threadNb; i++) {
        if (System.currentTimeMillis() > endOfLaunchPeriod) {
          exit(
              "Launch period is not long enough to complete the initial launch."
              + "Please decrease threadNb or increase launch period.");
        }
        launchOneTest((String) null, launched++);
        printStatus();
        try {
          Thread.sleep(thinkTime);
        } catch (InterruptedException e) {
          throwables.add(e);
        }
      }
      initialLaunchCompleted = true;
    }
    do {

      try {
        Thread.sleep(timeBetweenVerifications);
      } catch (InterruptedException e) {
        throwables.add(e);
      }

      printStatus();
    } while (!testFinished);
    printResults();
    if (!throwables.isEmpty()) {
      System.err.println("Throables caught during test launch : ");
      for (Throwable throwable : throwables) {
        throwable.printStackTrace(System.err);
      }
    }
  }

  /**
   * Launch one instance.
   */
  private void launchOneTest(String processId, long threadId) {
    PerfTestCase testToLaunch = null;
    String algoToExecute = this.algo;
    if (processId == null) {
      // initial launch : we need to launch one of each
      algoToExecute = ALGO_NB;
    }
    if (algoToExecute.equals(ALGO_NB)) {
      int testsToRunSize = testsToRun.size();
      int index = new Long(threadId % testsToRunSize).intValue();
      testToLaunch = testsToRun.get(index);
    } else if (algoToExecute.equals(ALGO_TYPE)) {
      for (PerfTestCase perfTestCase : testsToRun) {
        if (perfTestCase.getProcessName().equals(processId)) {
          testToLaunch = perfTestCase;
          break;
        }
      }
    }
    launchOneTest(testToLaunch, threadId);
  }

  /**
   * Launch one instance.
   */
  private PerfTestCase launchOneTest(PerfTestCase perfTestCase, long threadId) {
    PerfTestCase newPerfTestCase = null;

    try {
      newPerfTestCase = perfTestCase.getClass().newInstance();
    } catch (Exception e) {
      exit(e, "Problem while instantiating class of perftestCase : " + perfTestCase);
    }
    newPerfTestCase.setId(threadId);

    PerfTestCaseThread thread = new PerfTestCaseThread(this, newPerfTestCase);
    thread.start();
    if (printLaunched) {
      log("Launching thread " + threadId + " (" + perfTestCase.getProcessName()
          + ")... ");
    }

    return newPerfTestCase;
  }

  /**
   * Called by testCases thread when the testCase is finished.
   */
  public void finished(String processId, long threadId, long executionTime, Throwable t) {
    long currentTime = System.currentTimeMillis();
    boolean success = t == null;
    String logMessage = null;
    long nextThreadId = -1;
    synchronized (this) {
      finished++;
      if (success) {
        successNb++;
      } else {
        errorNb++;
      }
      if (currentTime <= expectedEndTime) {
        if (launchPeriod && (startTime + warmupTime) <= currentTime) {
          // end of warmup
          startTime = currentTime;
          expectedEndTime = startTime + loadTime;
          launchPeriod = false;
          logMessage = "STARTING MEASURES";
        } else if (!launchPeriod && success) {
          // currently in measuring time, a "normal" success
          long processExecutionTimeTotal = executionTimes.get(processId);
          executionTimes.put(processId, processExecutionTimeTotal + executionTime);
          if (executionTime > worthTimes.get(processId)
              || worthTimes.get(processId) == -1) {
            worthTimes.put(processId, executionTime);
          }
          if (executionTime < bestTimes.get(processId)) {
            bestTimes.put(processId, executionTime);
          }
          processSuccesses.put(processId, processSuccesses.get(processId) + 1);
          measuredInstances++;
        }
        nextThreadId = launched++;
      } else if (!initialLaunchCompleted) {
        // should never enter here there a synchro on the initial launch and on
        // this method
        System.err.println(
            "warmupTime + loadTime reached but initial launch not completed ! "
            + "Please change values to have a coherent comportment !");
      } else {
        // the end : just waitfor the last instances to end
        endTime = currentTime;
        testFinished = finished == launched;
      }
    }

    if (t != null) {
      log(t);
    }
    if (printFinished) {
      log("Finishing thread " + threadId + " (" + processId + ") in "
          + executionTime + " ms " + "at " + System.currentTimeMillis()
          + ", success = " + success);
    }
    if (logMessage != null) {
      log(logMessage);
    }
    if (nextThreadId != -1) {
      launchOneTest(processId, nextThreadId);
    }
  }

  /**
   * Prints the current status of the perf test : launched, finished, remaining
   * time...
   */
  private void printStatus() {
    long currentTime = System.currentTimeMillis();
    if ((currentTime - lastPrintTime) >= timeBetweenPrints) {
      log("finished : " + finished + ", running for "
          + Util.getDuration(currentTime - startTime) + ", remaining time : "
          + Util.getDuration(expectedEndTime - currentTime) + ", "
          + (launched - finished) + " running" + ", " + finished + " finished"
          + ", " + successNb + " success" + ", " + errorNb + " errors" + ", "
          + launched + " launched");
      lastPrintTime = currentTime;
    }
  }

  private void log(String message) {
    System.out.println(message);
  }

  private void log(Throwable t) {
    t.printStackTrace();
  }

  public static String formatFloat(float f) {
    NumberFormat nf = NumberFormat.getIntegerInstance();
    nf.setMinimumFractionDigits(2);
    nf.setMaximumFractionDigits(2);
    return nf.format((double) f);
  }

  private void printResults() {
    log("\nAll finished !");
    long measuredTime = endTime - startTime;
    log(measuredInstances + " executed in " + Util.getDuration(measuredTime));
    if (measuredInstances > 0) {
      long totalExecutionTimeAddition = 0;
      Map<String, Long> averages = new HashMap<String, Long>();
      for (String processAlias : executionTimes.keySet()) {
        long processExecutionTotalTime = executionTimes.get(processAlias);
        long success = processSuccesses.get(processAlias);
        long average = 0;
        if (success != 0) {
          average = processExecutionTotalTime / success;
        }
        averages.put(processAlias, average);
        log("\nResults for process " + processAlias + " " + success
            + " success");
        log("Average for process " + processAlias + " " + average + " ms");
        log("Best time for process " + processAlias + " "
            + bestTimes.get(processAlias) + "ms");
        log("Woth time for process " + processAlias + " "
            + worthTimes.get(processAlias) + "ms");
        totalExecutionTimeAddition += processExecutionTotalTime;
      }
      long instanceExecTime = totalExecutionTimeAddition / measuredInstances;
      log("\nExecution time average per instance for all processes = "
          + Util.getDuration(instanceExecTime));
      long instancesPerMinute = measuredInstances * 60000 / measuredTime;
      float instancesPerSecond = measuredInstances * 60000.0f / measuredTime
      / 60.0f;

      String xlsh = "XLSH;Algo;Tests;Thread Nb;Warmup;Think;Load;Total Instances (loadTime);Total Time;"
        + "Avg Time/Instance (ms);Instances/mn;Instances/s;Errors";

      String xlsd = "XLSD;";
      xlsd += algo + ";";
      for (PerfTestCase test : testsToRun) {
        xlsd += test.getProcessName() + ",";  
      }
      xlsd += ";";
      xlsd += threadNb + ";";
      xlsd += warmupTime / 60000 + ";";
      xlsd += thinkTime + ";";
      xlsd += loadTime / 60000 + ";";
      xlsd += measuredInstances + ";";
      xlsd += (endTime - initialStartTime) / 60000 + ";";
      xlsd += instanceExecTime + ";";
      xlsd += instancesPerMinute + ";";
      xlsd += formatFloat(instancesPerSecond) + ";";
      xlsd += errorNb;

      for (PerfTestCase testCase : testsToRun) {
        xlsh += ";" + "Avg Time/" + testCase.getProcessName();
        xlsd += ";";
        Long l = averages.get(testCase.getProcessName());
        if (l != null) {
          xlsd += l;
        }
      }
      log("\n" + xlsh);
      log(xlsd);

    } else {
      log("This test was not long enough to start measuring intances...");
    }
  }


  private void exit(String message) {
    exit(null, message);
  }
  private void exit(Throwable t, String message) {
    System.err.println("Fatal error, exiting... : " + message);
    if (t != null) {
      t.printStackTrace();
    }
    System.exit(1);
  }
}
