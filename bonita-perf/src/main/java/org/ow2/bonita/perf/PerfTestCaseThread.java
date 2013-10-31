/**
 * Copyright (C) 2006  Bull S. A. S.
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

/**
 * @author Charles Souillard
 */
public class PerfTestCaseThread extends Thread {

  /** perfTestCase to execute. */
  private PerfTestCase perfTestCase;

  /** StressPerf Test instance to post results. */
  private StressPerfTest perfTest;

  public PerfTestCaseThread(StressPerfTest perfTest, PerfTestCase perfTestCase) {
    this.perfTestCase = perfTestCase;
    this.perfTest = perfTest;
    setName(Long.toString(perfTestCase.getId()));
  }

  /**
   * Thread run method.
   */
  public void run() {
    String processId = perfTestCase.getProcessName();
    long testId = perfTestCase.getId();
    try {
      long start = System.currentTimeMillis();
      long end = perfTestCase.launch();
      perfTest.finished(processId, testId, end - start, null);
    } catch (Throwable t) {
      perfTest.finished(processId, testId, 0, t);
    }
  }
}
