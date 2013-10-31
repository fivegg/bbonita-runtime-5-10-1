package org.ow2.bonita.util;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;

import org.ow2.bonita.APITestCase;
import org.ow2.bonita.command.InstanceOfStringVariableCommand;

public class ToolTest extends APITestCase implements Serializable {

  private static final long serialVersionUID = -7120402287780464333L;

  public void testCreateVariableMap() {
    assertNull(VariableUtil.createVariableMap(null, new HashMap<String, Object>()));

    assertNull(VariableUtil.createVariableMap(null, null));
  }

  public void testGetClasses() throws IOException {
    assertEquals(new HashSet<byte[]>(), ClassDataTool.getClasses());

    final Class<?> nullClass = null;
    try {
      ClassDataTool.getClasses(nullClass);
      fail("Exception should be throw when calling getClasses with a null class");
    } catch (final IOException e) {
      // Test passed
    }
  }

  public void testGetClassData() throws IOException {
    try {
      ClassDataTool.getClassData((Class<?>) null);
      fail("BonitaRuntimeException should be thrown when calling getClassData with null parameter");
    } catch (final IOException e) {
      // Test passed
    }
    try {
      ClassDataTool.getClassData(ToolTest.class);
    } catch (final BonitaRuntimeException e) {
      fail("BonitaRuntimeException must not be thrown");
    }
  }

  public void testCreateVariableString() throws Exception {
    getManagementAPI().deployJar("toolc2.jar", Misc.generateJar(InstanceOfStringVariableCommand.class));
    final Boolean actual = getCommandAPI().execute(new InstanceOfStringVariableCommand());
    assertTrue(actual);
    getManagementAPI().removeJar("toolc2.jar");
  }

}
