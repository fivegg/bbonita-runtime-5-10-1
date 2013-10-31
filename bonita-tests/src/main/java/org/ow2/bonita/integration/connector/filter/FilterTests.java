package org.ow2.bonita.integration.connector.filter;

import junit.framework.Test;
import junit.framework.TestSuite;

public class FilterTests {

  public static Test suite() {
    TestSuite suite = new TestSuite("Filters Integration Tests");
    suite.addTestSuite(RandomMultipleFilterIntegrationTest.class);
    suite.addTestSuite(UniqueRandomFilterIntegrationTest.class);
    suite.addTestSuite(AssignedUserTaskFilterIntegrationTest.class);
    suite.addTestSuite(HasPerformedTaskIntegrationTest.class);
    return suite;
  }

}
