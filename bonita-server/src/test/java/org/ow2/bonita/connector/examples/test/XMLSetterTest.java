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
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

import junit.framework.TestCase;

import org.ow2.bonita.connector.core.ConnectorAPI;
import org.ow2.bonita.connector.core.ConnectorDescription;
import org.ow2.bonita.connector.core.ConnectorDescriptorAPI;
import org.ow2.bonita.connector.core.desc.ConnectorDescriptor;
import org.ow2.bonita.connector.core.desc.Getter;
import org.ow2.bonita.connector.core.desc.Page;
import org.ow2.bonita.connector.core.desc.Setter;
import org.ow2.bonita.connector.examples.XMLConnector;
import org.ow2.bonita.util.Misc;
import org.w3c.dom.Document;

/**
 * @author Mickael Istria
 *
 */
public class XMLSetterTest extends TestCase {

  public void testXMLSetter() throws Exception {
    final Setter setter = new Setter("setO", null, null, new Object[] { DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument() });

    final ConnectorDescriptor connectorDescriptor = new ConnectorDescriptor("connectorId", getList(ConnectorAPI.other), null, 
        getList(setter), new ArrayList<Getter>(), new ArrayList<Page>());

    //create folder and file
    //final File myFolder = new File(System.getProperty(BonitaConstants.BONITA_HOME) + File.separator + "myFolder");
    final URI currentFolderURI = new URI(getClass().getClassLoader().getResource(XMLConnector.class.getName().replace('.', '/') + ".class").getFile());
    final File curentFolder = new File(currentFolderURI.getPath());

    final File myFolder = new File(curentFolder.getParent());

    assertTrue(myFolder.exists());
    assertTrue(myFolder.isDirectory());
    final File file = new File(myFolder, XMLConnector.class.getSimpleName() + ".xml");
    assertTrue(file.createNewFile());
    final OutputStream output = new FileOutputStream(file);
    //save the connector descriptor file
    ConnectorDescriptorAPI.save(connectorDescriptor, output);

    // Check contents of file
    BufferedReader reader = new BufferedReader(new FileReader(file));
    String line = null;
    boolean found = false;
    while (!found && (line = reader.readLine()) != null) {
      if (line.contains("<xml/>")) {
        found = true;
      } else if (line.contains("DocumentImpl")) {
        fail("A default serialization was used");
      }
    }
    reader.close();
    assertTrue("no xml element found", found);

    //load the connector
    final ConnectorAPI connectorAPI = new ConnectorAPI(getClass().getClassLoader(), getList(XMLConnector.class.getName()));
    final ConnectorDescription connectorDescription = connectorAPI.getConnector("connectorId");
    assertNotNull(connectorDescription);
    final List<Setter> inputs = connectorDescription.getInputs();
    assertEquals(1, inputs.size());
    Setter daSetter = inputs.get(0);
    Document doc = Misc.generateDocument("<kikoo>lol</kikoo>");
    assertTrue(daSetter.getParameters()[0] instanceof Document);
    XMLConnector connector = (XMLConnector)connectorDescription.getConnectorClass().newInstance();
    connector.setO(doc);
    output.close();
    file.delete();
  }

  private static <T> List<T> getList(T o) {
    final List<T> result = new ArrayList<T>();
    result.add(o);
    return result;
  }

}
