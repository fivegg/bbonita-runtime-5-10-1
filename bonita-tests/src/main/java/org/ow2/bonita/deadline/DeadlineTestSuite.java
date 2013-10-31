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
package org.ow2.bonita.deadline;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author Pascal Verdage
 */
public final class DeadlineTestSuite {

  private DeadlineTestSuite() { }

  public static Test suite() {
    TestSuite suite = new TestSuite(DeadlineTestSuite.class.getName());
    //$JUnit-BEGIN$
    suite.addTestSuite(DeadlineParsingTest.class);
    suite.addTestSuite(DeadlineFunctionalTest.class);
    suite.addTestSuite(DeadlineIntegrationTest.class);
    suite.addTestSuite(DeadlineConcurrentTest.class);
    //$JUnit-END$
    return suite;
  }

}
