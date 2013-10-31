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
 * 
 * Modified by Charles Souillard, Matthieu Chaffotte - BonitaSoft S.A.
 **/
package org.ow2.bonita;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.ow2.bonita.env.generator.DbHistoryEnvGenerator;
import org.ow2.bonita.facade.ApplicationAccessTest;
import org.ow2.bonita.facade.NoVarUpdateTest;
import org.ow2.bonita.multitenancy.MultiTenancyTest;
import org.ow2.bonita.services.impl.ArchiveFinishedInstanceHandlerTest;
import org.ow2.bonita.services.impl.ChainFinishedInstanceHandlerTest;
import org.ow2.bonita.services.impl.DeleteFinishedInstanceHandlerTest;
import org.ow2.bonita.services.impl.HiloDbUUIDServiceTests;
import org.ow2.bonita.services.impl.NoOpFinishedInstanceHandlerTest;
import org.ow2.bonita.util.BonitaConstants;

/**
 * @author Pierre Vigneras
 */
public final class NonRegressionTests extends TestCase {

  private NonRegressionTests() {
  }

  public static Test suite() throws Exception {
    final TestSuite suite = new TestSuite(NonRegressionTests.class.getName());

    suite.addTest(LocalTests.suite());
    // Tests using a custom envGenerator
    suite.addTest(new EnvironmentFactoryTestSetup(new TestSuite(ChainFinishedInstanceHandlerTest.class),
        ChainFinishedInstanceHandlerTest.getEnvGenerator(), BonitaConstants.DEFAULT_DOMAIN));
    suite.addTest(new EnvironmentFactoryTestSetup(new TestSuite(DeleteFinishedInstanceHandlerTest.class),
        DeleteFinishedInstanceHandlerTest.getEnvGenerator(), BonitaConstants.DEFAULT_DOMAIN));
    suite.addTest(new EnvironmentFactoryTestSetup(new TestSuite(NoOpFinishedInstanceHandlerTest.class),
        NoOpFinishedInstanceHandlerTest.getEnvGenerator(), BonitaConstants.DEFAULT_DOMAIN));
    suite.addTest(new EnvironmentFactoryTestSetup(new TestSuite(ArchiveFinishedInstanceHandlerTest.class),
        ArchiveFinishedInstanceHandlerTest.getEnvGenerator(), BonitaConstants.DEFAULT_DOMAIN));
    suite.addTest(new EnvironmentFactoryTestSetup(new TestSuite(ApplicationAccessTest.class), ApplicationAccessTest
        .getEnvGenerator(), BonitaConstants.DEFAULT_DOMAIN));
    suite.addTest(new EnvironmentFactoryTestSetup(new TestSuite(NoVarUpdateTest.class), NoVarUpdateTest
        .getEnvGenerator(), BonitaConstants.DEFAULT_DOMAIN));

    suite.addTest(new EnvironmentFactoryTestSetup(HiloDbUUIDServiceTests.suite(), HiloDbUUIDServiceTests
        .getEnvGenerator(), BonitaConstants.DEFAULT_DOMAIN));
    // tests with XmlHistory environment: suite.addTest(new
    // EnvironmentFactoryTestSetup(ProxiedTests.suite(defaultEnvSuite), new
    // XmlHistoryEnvGenerator()));

    // Tests using the default envGenerator
    suite.addTest(new EnvironmentFactoryTestSetup(DefaultEnvNonRegressionTests.suite(), new DbHistoryEnvGenerator(),
        BonitaConstants.DEFAULT_DOMAIN));

    // This test is launched only in local mode as it starts different JVM and
    // it will be very costly to configure them one by one
    // suite.addTestSuite(RestartTest.class);

    // This test is at the end because of a problem using different JAAS
    // configuration
    suite.addTestSuite(MultiTenancyTest.class);
    return suite;
  }

}
