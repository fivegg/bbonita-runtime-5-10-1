package org.ow2.bonita.integration.connector;

import junit.framework.Test;
import junit.framework.TestSuite;

public class ConnectorsTests {

  public static Test suite() {
    TestSuite suite = new TestSuite("Connector Integration Tests");
    suite.addTestSuite(ConnectorsAtProcessLevelTest.class);
    suite.addTest(ActivityConnectorTests.suite());
    return suite;
  }
}
