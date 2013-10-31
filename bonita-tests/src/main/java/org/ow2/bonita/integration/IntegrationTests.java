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
package org.ow2.bonita.integration;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.ow2.bonita.integration.cycle.CycleDetectionTest;
import org.ow2.bonita.integration.cycle.CycleTest;
import org.ow2.bonita.integration.hook.ActivityHookTest;
import org.ow2.bonita.integration.routeSplitJoin.JoinAnd1to1TrXpdl1Test;
import org.ow2.bonita.integration.routeSplitJoin.JoinXor1to1TrXpdl1Test;
import org.ow2.bonita.integration.routeSplitJoin.RoutesAndJoinsProedTest;
import org.ow2.bonita.integration.routeSplitJoin.SplitAndXorJoinTest;
import org.ow2.bonita.integration.routeSplitJoin.SplitStartingNodeTest;
import org.ow2.bonita.integration.routeSplitJoin.SplitsJoinsTest;
import org.ow2.bonita.integration.task.TaskTest;
import org.ow2.bonita.integration.task.TasksSplitJoinTest;
import org.ow2.bonita.integration.transition.TransitionConditionMultiTypesTest;
import org.ow2.bonita.integration.transition.TransitionConditionTest;
import org.ow2.bonita.integration.var.ActivityAutomaticVariableTest;
import org.ow2.bonita.integration.var.ProcessVariableTest;

/**
 * @author Charles Souillard, Marc Blachon
 */
public final class IntegrationTests extends TestCase {
  /**
   * private constructor.
   */
  private IntegrationTests() {
  }

  /**
   * TestSuite.
   *
   * @return suite
   */
  public static Test suite() {
    TestSuite suite = new TestSuite(IntegrationTests.class.getName());
    // $JUnit-BEGIN$
    // basic tests
    suite.addTestSuite(SplitAndXorJoinTest.class);
    suite.addTestSuite(JoinAnd1to1TrXpdl1Test.class);
    suite.addTestSuite(JoinXor1to1TrXpdl1Test.class);

    // other advanced tests
    suite.addTestSuite(SplitsJoinsTest.class);
    suite.addTestSuite(ProcessVariableTest.class);
    suite.addTestSuite(ActivityAutomaticVariableTest.class);
    suite.addTestSuite(ActivityHookTest.class);
    suite.addTestSuite(TransitionConditionTest.class);
    suite.addTestSuite(TransitionConditionMultiTypesTest.class);
    suite.addTestSuite(RoutesAndJoinsProedTest.class);
    suite.addTestSuite(TaskTest.class);
    suite.addTestSuite(TasksSplitJoinTest.class);
    suite.addTestSuite(SplitStartingNodeTest.class);
    suite.addTestSuite(CycleTest.class);

    suite.addTestSuite(CycleDetectionTest.class);

    // $JUnit-END$
    return suite;
  }
}
