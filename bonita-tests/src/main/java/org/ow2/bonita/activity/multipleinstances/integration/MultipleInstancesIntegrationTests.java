package org.ow2.bonita.activity.multipleinstances.integration;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class MultipleInstancesIntegrationTests extends TestCase {

  public static Test suite() {
    final TestSuite suite = new TestSuite("Multiple Activities Integration Tests");
    // Instantiators
    suite.addTestSuite(FixedNumberInstantiatorTest.class);
    suite.addTestSuite(GroovyInstantiatorTest.class);
    suite.addTestSuite(VariableInstantiatorTest.class);
    // Join Checkers
    suite.addTestSuite(FixedNumberJoinCheckerTest.class);
    suite.addTestSuite(PercentageJoinCheckerTest.class);
    suite.addTestSuite(GroovyJoinCheckerTest.class);
    return suite;
  }

}
