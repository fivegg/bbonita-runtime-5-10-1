/**
 * Copyright (C) 2006  Bull S. A. S.
 * Bull, Rue Jean Jaures, B.P.68, 78340, Les Clayes-sous-Bois
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA  02110-1301, USA.
 **/
package org.ow2.bonita.connector.examples.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.ow2.bonita.connector.core.ConnectorAPI;
import org.ow2.bonita.connector.core.ConnectorDescription;
import org.ow2.bonita.connector.core.ConnectorDescriptorAPI;
import org.ow2.bonita.connector.core.desc.ConnectorDescriptor;
import org.ow2.bonita.connector.core.desc.Getter;
import org.ow2.bonita.connector.core.desc.Page;
import org.ow2.bonita.connector.core.desc.Setter;
import org.ow2.bonita.connector.examples.ObjectConnector;

public class ObjectConnectorTest extends TestCase {

  public void testObjectConnector() throws Exception {
    final Setter setter = new Setter("setO", "", null, new Object[]{new Object()});
    final Getter getter = new Getter("o");
    
    final ConnectorDescriptor connectorDescriptor = new ConnectorDescriptor("connectorId", getList(ConnectorAPI.other), null, 
        getList(setter), getList(getter), new ArrayList<Page>());
    
    //create folder and file
    //final File myFolder = new File(System.getProperty(BonitaConstants.BONITA_HOME) + File.separator + "myFolder");
    final URI currentFolderURI = new URI(getClass().getClassLoader().getResource(ObjectConnector.class.getName().replace('.', '/') + ".class").toString());
    final File curentFolder = new File(currentFolderURI.getPath());
    
    final File myFolder = new File(curentFolder.getParent());
    
    assertTrue(myFolder.exists());
    assertTrue(myFolder.isDirectory());
    final File file = new File(myFolder, ObjectConnector.class.getSimpleName() + ".xml");
    file.createNewFile();
    final OutputStream output = new FileOutputStream(file);
    //save the connector descriptor file
    ConnectorDescriptorAPI.save(connectorDescriptor, output);
    
    //load the connector
    final ConnectorAPI connectorAPI = new ConnectorAPI(getClass().getClassLoader(), getList(ObjectConnector.class.getName()));
    final ConnectorDescription connectorDescription = connectorAPI.getConnector("connectorId");
    assertNotNull(connectorDescription);
    final List<Setter> inputs = connectorDescription.getInputs();
    assertNotNull(inputs);
    assertEquals(1, inputs.size());
    final Setter input = inputs.iterator().next();
    assertEquals("setO", input.getSetterName());

    final ObjectConnector connector = new ObjectConnector();
    final Object o = new Object();
    connector.setO(o);
    connector.execute();
    final Object result = connector.getO();
    assertEquals(o, result);
    assertSame(o, result);
    output.close();
    file.delete();
  }
  
  private static <T> List<T> getList(T o) {
    final List<T> result = new ArrayList<T>();
    result.add(o);
    return result;
  }

}
