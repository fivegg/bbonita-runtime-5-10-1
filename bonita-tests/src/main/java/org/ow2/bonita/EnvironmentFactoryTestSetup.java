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
package org.ow2.bonita;

import junit.extensions.TestSetup;
import junit.framework.Test;

import org.ow2.bonita.env.GlobalEnvironmentFactory;
import org.ow2.bonita.env.generator.EnvGenerator;


/**
 * @author Guillaume Porcher
 *
 */
public class EnvironmentFactoryTestSetup extends TestSetup {


  private final EnvGenerator envGenerator;
  private final String domain;

  public EnvironmentFactoryTestSetup(final Test test, final EnvGenerator envGenerator, final String domain) {
    super(test);
    this.envGenerator = envGenerator;
    this.domain = domain;
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    GlobalEnvironmentFactory.setEnvironmentFactory(domain, this.envGenerator.createEnvironmentFactory());
  }

  @Override
  protected void tearDown() throws Exception {
    GlobalEnvironmentFactory.getEnvironmentFactory(domain).close();
    GlobalEnvironmentFactory.setEnvironmentFactory(domain, null);
    super.tearDown();
  }

}
