package org.ow2.bonita.integration.connector;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.ow2.bonita.integration.connector.filter.FilterTests;
import org.ow2.bonita.integration.connector.resolver.RoleResolverTests;

public class ActivityConnectorTests {

  public static Test suite() {
    final TestSuite suite = new TestSuite("Connector Integration At Activity Level Tests");
    suite.addTestSuite(ActivityConnectorTest.class);

    suite.addTestSuite(ATeamRoleMapperConnectorTest.class);
    suite.addTestSuite(ParametersRoleMapperConnectorTest.class);
    suite.addTestSuite(TooManyArraysConnectorIntegrationTest.class);

    suite.addTestSuite(MultipleRoleMappersTest.class);

    suite.addTestSuite(AddCommentConnectorIntegrationTest.class);
    suite.addTestSuite(GetProcessInstanceInitiatorIntegrationTest.class);
    suite.addTestSuite(GetTaskAuthorIntegrationtest.class);
    suite.addTestSuite(GetUserIntegrationTest.class);
    suite.addTestSuite(SetVarConnectorIntegrationTest.class);
    suite.addTestSuite(StartInstanceConnectorIntegrationTest.class);
    suite.addTestSuite(JavaConnectorIntegrationTest.class);
    suite.addTestSuite(GroovyConnectorIntegrationTest.class);
    suite.addTestSuite(ComplexConnectorOutputTargetTest.class);
    suite.addTestSuite(SlowConnectorsTest.class);

    suite.addTest(RoleResolverTests.suite());
    suite.addTest(FilterTests.suite());
    return suite;
  }

}
