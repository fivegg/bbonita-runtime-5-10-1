/**
 * Copyright (C) 2009-2010  BonitaSoft S.A.
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
package org.ow2.bonita.services.impl;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.ow2.bonita.env.EnvConstants;
import org.ow2.bonita.env.generator.DbHistoryEnvGenerator;
import org.ow2.bonita.env.generator.EnvEntry;
import org.ow2.bonita.env.generator.EnvGenerator;

/** 
 * @author Charles Souillard
 */

public class HiloDbUUIDServiceTests extends TestCase {
  
  private HiloDbUUIDServiceTests() { }
  
  public static EnvGenerator getEnvGenerator() {
    final EnvGenerator envGenerator = new DbHistoryEnvGenerator();
    
    final String key = EnvConstants.UUID_SERVICE_DEFAULT_KEY;
    final String xml = "<" + key + " name='" + key + "' class='" + HiloDbUUIDService.class.getName() + "'/>";
    
    final EnvEntry entry = EnvGenerator.getEnvEntry(key, "Implementation of the UUID Service.", xml, true);
    
    envGenerator.addApplicationEntry(entry);
    return envGenerator;
  }
  
  public static Test suite() throws Exception {
    final TestSuite suite = new TestSuite(HiloDbUUIDServiceTests.class.getName());
    suite.addTestSuite(HiloDbUUIDServiceTest.class);
    return suite;
  }
  
}
