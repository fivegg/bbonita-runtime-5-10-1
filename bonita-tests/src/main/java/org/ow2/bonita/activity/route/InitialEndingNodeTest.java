package org.ow2.bonita.activity.route;

import java.net.URL;

import org.ow2.bonita.APITestCase;
import org.ow2.bonita.util.BonitaException;

public class InitialEndingNodeTest extends APITestCase {

  public void testNoEndingNode() throws BonitaException {
    URL xpdlUrl = this.getClass().getResource("noEndingNode.xpdl");
    try {
      getManagementAPI().deploy(getBusinessArchiveFromXpdl(xpdlUrl));
      fail("expected exception");
    } catch (Exception e) {
      //assertTrue(e.getMessage().contains("no ending node"));
    }
  }
}
