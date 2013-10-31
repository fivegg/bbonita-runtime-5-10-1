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
package org.ow2.bonita.hook;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.ow2.bonita.hook.badclass.OnBadClassHookTest;
import org.ow2.bonita.hook.misc.MiscHookTest;
import org.ow2.bonita.hook.ondeadline.OnDeadlineHookTest;

/**
 * @author "Charles Souillard"
 */
public class HookTestSuite extends TestCase {

  public static Test suite() {
    TestSuite suite = new TestSuite(HookTestSuite.class.getName());
    suite.addTestSuite(OnDeadlineHookTest.class);
    suite.addTestSuite(MiscHookTest.class);
    suite.addTestSuite(OnBadClassHookTest.class);
    return suite;
  }
}
