package org.ow2.bonita.integration.connector.resolver;

import junit.framework.Test;
import junit.framework.TestSuite;

public class RoleResolverTests {

  public static Test suite() {
    TestSuite suite = new TestSuite("RoleResolver Integration Tests");
    suite.addTestSuite(ProcessInitiatiorRoleResolverIntegrationTest.class);
    suite.addTestSuite(UserListRoleResolverIntegrationTest.class);
    suite.addTestSuite(GroupUsersRoleResolverIntergrationTest.class);
    suite.addTestSuite(GroupRoleUsersRoleResolverIntegrationTest.class);
    suite.addTestSuite(UserRoleResolverIntegrationTest.class);
    suite.addTestSuite(TeamManagerRoleResolverIntegrationTest.class);
    suite.addTestSuite(ManagerRoleResolverIntegrationTest.class);
    suite.addTestSuite(DelegeeRoleResolverIntegrationTest.class);
    suite.addTestSuite(NullSetRoleResolverTest.class);
    return suite;
  }

}
