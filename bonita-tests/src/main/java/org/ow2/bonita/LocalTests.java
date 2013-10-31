package org.ow2.bonita;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.ow2.bonita.connector.ConnectorAPITest;
import org.ow2.bonita.env.XpdlEnvironmentParserTest;
import org.ow2.bonita.env.descriptor.DbSessionDescriptorTest;
import org.ow2.bonita.event.LocalEventsTest;
import org.ow2.bonita.services.record.HistoryTest;
import org.ow2.bonita.services.record.JournalTest;
import org.ow2.bonita.variable.LocalVariableBasicTypeTest;

public class LocalTests extends TestCase {

  public static Test suite() throws Exception {
    final TestSuite suite = new TestSuite(LocalTests.class.getName());

    // This test cannot be in bonita-server due to connector dependency
    suite.addTestSuite(ConnectorAPITest.class);
    // This test is using a static field on client side so cannot be executed in
    // remote way
    suite.addTestSuite(LocalEventsTest.class);

    // JournalTest and HistoryTest can not be executed on remote tests as they
    // are using bonita-server classes
    suite.addTestSuite(JournalTest.class);
    suite.addTestSuite(HistoryTest.class);

    suite.addTestSuite(LocalVariableBasicTypeTest.class);

    // TESTS that DO NOT use the default EnvGenerator, they are not launched in
    // EJB tests
    suite.addTestSuite(DbSessionDescriptorTest.class);
    suite.addTestSuite(XpdlEnvironmentParserTest.class);

    return suite;
  }

}
