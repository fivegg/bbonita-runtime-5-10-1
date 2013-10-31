package org.ow2.bonita.search;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class SearchTests extends TestCase {

  public static Test suite() {
    final TestSuite suite = new TestSuite("Search Entity Tests");
    suite.addTestSuite(SearchUserTest.class);
    suite.addTestSuite(SearchGroupTest.class);
    suite.addTestSuite(SearchRoleTest.class);
    suite.addTestSuite(SearchProcessDefinitionTest.class);
    suite.addTestSuite(SearchProcessInstanceTest.class);
    suite.addTestSuite(SearchActivityInstanceTest.class);
    return suite;
  }

}
