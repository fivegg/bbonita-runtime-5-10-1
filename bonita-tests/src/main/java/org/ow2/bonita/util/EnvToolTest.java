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
package org.ow2.bonita.util;

import java.io.Serializable;

import org.ow2.bonita.APITestCase;
import org.ow2.bonita.command.RecorderCommand;
import org.ow2.bonita.facade.exception.BonitaInternalException;
import org.ow2.bonita.services.Recorder;

public class EnvToolTest extends APITestCase implements Serializable {

  private static final long serialVersionUID = -5639119350074706001L;

  public void testGetNullParameters() {
    try {
      EnvTool.get(null, null);
      fail("Exception should be thrown when calling get method with null parameters");
    } catch (final IllegalArgumentException e) {
      // Test passed
      assertTrue(e.getMessage(), e.getMessage().contains("Some parameters are null in org.ow2.bonita.util.EnvTool.get()"));
    }
  }

  public void testGetNullKey() {
    try {
      EnvTool.get(Recorder.class, null);
      fail("Exception should be thrown when calling get method with a null key");
    } catch (final IllegalArgumentException e) {
      // Test passed
      assertTrue(e.getMessage(), e.getMessage().contains("Some parameters are null in org.ow2.bonita.util.EnvTool.get()"));
    }
  }

  public void testGetNullClass() {
    try {
      EnvTool.get(null, "journal");
      fail("Exception should be thrown when calling get method with a null class");
    } catch (final IllegalArgumentException e) {
      // Test passed
      assertTrue(e.getMessage(), e.getMessage().contains("Some parameters are null in org.ow2.bonita.util.EnvTool.get()"));
    }
  }

  public void testGetInvalidObjectName() throws Exception {
    String key = "nonCorrespondingName";
    byte[] jar = Misc.generateJar(RecorderCommand.class);
    getManagementAPI().deployJar("c1.jar", jar);
    try {
      getCommandAPI().execute(new RecorderCommand(key));
      fail("Exception should be thrown when calling get method with a non corresponding object name");
    } catch (final BonitaInternalException e) {
      // Test passed
      assertTrue(e.getMessage(), e.getMessage().contains(key));
    } 
    getManagementAPI().removeJar("c1.jar");
  }

  public void testGetObject() throws Exception {
    byte[] jar = Misc.generateJar(RecorderCommand.class);
    getManagementAPI().deployJar("c2.jar", jar);
    Boolean actual = getCommandAPI().execute(new RecorderCommand("journal"));
    assertTrue(actual);
    getManagementAPI().removeJar("c2.jar");
  }

  public void testNoEnvironment() {
    try {
      EnvTool.get(Recorder.class, "journal");
      fail("Exception should be thrown when calling get method outside an environment");
    } catch (final IllegalStateException e) {
      // Test passed
      assertTrue(e.getMessage(), e.getMessage().contains("Environment is null!"));
    }
  }

}
