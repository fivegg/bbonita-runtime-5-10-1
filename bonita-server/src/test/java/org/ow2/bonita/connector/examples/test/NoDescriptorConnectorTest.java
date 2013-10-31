package org.ow2.bonita.connector.examples.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.ow2.bonita.connector.core.Connector;
import org.ow2.bonita.connector.core.ConnectorAPI;
import org.ow2.bonita.connector.core.ConnectorDescription;
import org.ow2.bonita.connector.core.ConnectorTest;
import org.ow2.bonita.connector.core.desc.Category;
import org.ow2.bonita.connector.core.desc.Component;
import org.ow2.bonita.connector.examples.NoDescriptorConnector;

public class NoDescriptorConnectorTest extends ConnectorTest {

  @Override
  protected Class<? extends Connector> getConnectorClass() {
    return NoDescriptorConnector.class;
  }

  public void testConnectorDescriptor() throws Exception {
    ConnectorDescription description = new ConnectorDescription(getConnectorClass(), Locale.ENGLISH);
    List<Component> components = description.getAllInputs();
    assertEquals(2, components.size());
    components = description.getAllPageInputs("lonely");
    assertEquals(2, components.size());
    List<Category> categories = description.getCategories();
    assertEquals(1, categories.size());
    assertEquals(ConnectorAPI.other.getName(), categories.get(0).getName());
    assertEquals("NoDescriptorConnector", description.getConnectorLabel());
    assertNull(description.getDescription());
    assertNull(description.getIcon());
    assertNull(description.getIconPath());
    assertEquals("NoDescriptorConnector", description.getId());
    List<String> outputs = description.getOutputNames();
    List<String> expected = new ArrayList<String>();
    expected.add("result");
    assertEquals(expected, outputs);
    List<String> expectedPages = new ArrayList<String>();
    expectedPages.add("lonely");
    assertEquals(expectedPages,description.getPages());
  }

}
