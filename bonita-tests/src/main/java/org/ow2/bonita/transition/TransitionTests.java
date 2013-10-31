package org.ow2.bonita.transition;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class TransitionTests extends TestCase {

  public static Test suite() {
    final TestSuite suite = new TestSuite("Transition Tests");
    suite.addTestSuite(TransitionWithActivityVarTest.class);
    suite.addTestSuite(TransitionWithWFProcessVarTest.class);
    suite.addTestSuite(TransitionWithGroovyExpressions.class);
    return suite;
  }

}
