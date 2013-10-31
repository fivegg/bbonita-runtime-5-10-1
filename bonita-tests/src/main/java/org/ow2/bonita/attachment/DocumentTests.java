package org.ow2.bonita.attachment;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class DocumentTests extends TestCase {

  public static Test suite() throws Exception {
    final TestSuite suite = new TestSuite("Document tests");
    suite.addTestSuite(AttachmentTest.class);
    suite.addTestSuite(DocumentTest.class);
    suite.addTestSuite(DeleteDocOfProcInstCommandTest.class);
    return suite;
  }

}
