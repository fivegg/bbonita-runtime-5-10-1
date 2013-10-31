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
package org.ow2.bonita.hook.badclass;

import java.net.URL;

import org.ow2.bonita.APITestCase;
import org.ow2.bonita.facade.exception.DeploymentException;

/**
 * @author "Charles Souillard"
 */
public class OnBadClassHookTest extends APITestCase {

  private String badClassName = "org.ow2.bonita.badpackage.DefaultTestHook";

  public void testOnBadClassHook() {
    URL xpdlUrl = this.getClass().getResource("onBadClassHook.xpdl");
    try {
      getManagementAPI().deploy(getBusinessArchiveFromXpdl(xpdlUrl));
      fail("This test was expected to fail as a bad class was specified for the hook.");
    } catch (DeploymentException e) {
      assertTrue(causeContains("No Class available with classname: " + badClassName, e));
    } 
  }

  public void testOnBadClassTxHook() {
	    URL xpdlUrl = this.getClass().getResource("onBadClassTxHook.xpdl");
	    try {
	      getManagementAPI().deploy(getBusinessArchiveFromXpdl(xpdlUrl));
	      fail("This test was expected to fail as a bad class was specified for the hook.");
	    } catch (DeploymentException e) {
	      assertTrue(causeContains("No Class available with classname: " + badClassName, e));
	    } 
	  }
}
