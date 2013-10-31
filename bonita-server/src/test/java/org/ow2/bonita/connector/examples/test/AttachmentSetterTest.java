/**
 * Copyright (C) 2010 BonitaSoft S.A.
 * BonitaSoft, 31 rue Gustave Eiffel - 38000 Grenoble
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.ow2.bonita.connector.examples.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import junit.framework.TestCase;

import org.ow2.bonita.connector.core.ConnectorAPI;
import org.ow2.bonita.connector.core.ConnectorDescription;
import org.ow2.bonita.connector.core.ConnectorDescriptorAPI;
import org.ow2.bonita.connector.core.desc.ConnectorDescriptor;
import org.ow2.bonita.connector.core.desc.Getter;
import org.ow2.bonita.connector.core.desc.Page;
import org.ow2.bonita.connector.core.desc.Setter;
import org.ow2.bonita.connector.examples.AttachmentConnector;
import org.ow2.bonita.facade.runtime.AttachmentInstance;
import org.ow2.bonita.facade.runtime.impl.AttachmentInstanceImpl;
import org.ow2.bonita.facade.uuid.DocumentUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;

/**
 * @author Mickael Istria
 *
 */
public class AttachmentSetterTest extends TestCase {

  public void testAttachmentSetter() throws Exception {
    AttachmentInstance attachment = new AttachmentInstanceImpl(new DocumentUUID("test"), "kikoo", new ProcessInstanceUUID("dummy"), "billy", new Date());
    final Setter setter = new Setter("setO", null, null, new Object[] { attachment });

    final ConnectorDescriptor connectorDescriptor = new ConnectorDescriptor("connectorId", getList(ConnectorAPI.other), null, 
        getList(setter), new ArrayList<Getter>(), new ArrayList<Page>());

    //create folder and file
    //final File myFolder = new File(System.getProperty(BonitaConstants.BONITA_HOME) + File.separator + "myFolder");
    final File curentFolder = new File(getClass().getClassLoader().getResource(AttachmentConnector.class.getName().replace('.', '/') + ".class").getFile());

    final File myFolder = new File(curentFolder.getParent());

    assertTrue(myFolder.exists());
    assertTrue(myFolder.isDirectory());
    final File file = new File(myFolder, AttachmentConnector.class.getSimpleName() + ".xml");
    file.createNewFile();
    final OutputStream output = new FileOutputStream(file);
    //save the connector descriptor file
    ConnectorDescriptorAPI.save(connectorDescriptor, output);

    // Check contents of file
    BufferedReader reader = new BufferedReader(new FileReader(file));
    String line = null;
    boolean found = false;
    while (!found && (line = reader.readLine()) != null) {
      if (line.contains("<attachment/>")) {
        found = true;
      } else if (line.contains("DocumentImpl")) {
        fail("A default serialization was used");
      }
    }
    reader.close();
    assertTrue("no atttachment element found", found);

    //load the connector
    final ConnectorAPI connectorAPI = new ConnectorAPI(getClass().getClassLoader(), getList(AttachmentConnector.class.getName()));
    final ConnectorDescription connectorDescription = connectorAPI.getConnector("connectorId");
    assertNotNull(connectorDescription);
    final List<Setter> inputs = connectorDescription.getInputs();
    assertEquals(1, inputs.size());
    Setter daSetter = inputs.get(0);
    assertTrue(daSetter.getParameters()[0] instanceof AttachmentInstance);
    AttachmentConnector connector = (AttachmentConnector)connectorDescription.getConnectorClass().newInstance();
    connector.setO(attachment);
    output.close();
    file.delete();
  }

  private static <T> List<T> getList(T o) {
    final List<T> result = new ArrayList<T>();
    result.add(o);
    return result;
  }
}
