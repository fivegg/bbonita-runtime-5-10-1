package org.ow2.bonita.activity.multipleinstances;


import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.ow2.bonita.activity.multipleinstances.integration.MultipleInstancesIntegrationTests;

public class MultipleInstancesTests extends TestCase {

  public static Test suite() {
    final TestSuite suite = new TestSuite("Multiple Activities Tests");
    // Default Engine tests
    suite.addTestSuite(MultipleInstancesTest.class);
    // Implementation tests
    suite.addTest(MultipleInstancesIntegrationTests.suite());
    return suite;
  }

}
