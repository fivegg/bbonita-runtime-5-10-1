package org.ow2.bonita.event;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class EventTests extends TestCase {

  public static Test suite() throws Exception {
    final TestSuite suite = new TestSuite("Process and Activity Event tests");
    suite.addTestSuite(MessageEventTest.class);
    suite.addTestSuite(CorrelationMessageEventTest.class);
    suite.addTestSuite(TimerEventTest.class);
    suite.addTestSuite(ErrorEventTest.class);
    suite.addTestSuite(SignalEventTest.class);
    suite.addTestSuite(MultipleEventsTest.class);
    suite.addTestSuite(EventSubProcessTest.class);
    suite.addTestSuite(EventManagementTest.class);
    suite.addTestSuite(BoudaryEventsAndMultipleInstancesTest.class);
    return suite;
  }

}
