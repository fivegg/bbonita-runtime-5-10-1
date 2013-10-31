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

import java.util.ArrayList;
import java.util.List;

import org.ow2.bonita.connector.core.Connector;
import org.ow2.bonita.connector.core.ConnectorAPI;
import org.ow2.bonita.connector.core.ConnectorDescription;
import org.ow2.bonita.connector.core.ConnectorDescriptionTest;
import org.ow2.bonita.connector.core.ConnectorException;
import org.ow2.bonita.connector.core.desc.Category;
import org.ow2.bonita.connector.core.desc.Component;
import org.ow2.bonita.connector.examples.NoFieldsConnector;

public class NoFieldsConnectorTest extends ConnectorDescriptionTest {

  @Override
  protected Class<? extends Connector> getConnectorClass() {
    return NoFieldsConnector.class;
  }

  @Override
  protected String getExpectedConnectorId() {
    return "NoFieldsConnector";
  }

  @Override
  public void testGetPages() throws ConnectorException {
    connector = new ConnectorDescription(getConnectorClass());
    List<String> pages = connector.getPages();
    assertEquals(0, pages.size());
    connector = null;
  }

  @Override
  public void testGetAllInputs() throws ConnectorException {
    connector = new ConnectorDescription(getConnectorClass());
    List<Component> components = connector.getAllInputs();
    assertEquals(0, components.size());
    connector = null;
  }

  @Override
  public void testGetAllPageInputs() throws ConnectorException {
    connector = new ConnectorDescription(getConnectorClass());
    try {
      connector.getAllPageInputs("UnknownPage");
      fail("The page does not exist!");
    } catch (Exception e) {
    } finally {
      connector = null;
    }
  }

  @Override
  protected List<Category> getExpectedCategoryId() {
    List<Category> categories = new ArrayList<Category>();
    categories.add(ConnectorAPI.other);
    return categories;
  }

  @Override
  protected String getExpectedDescription() {
    return null;
  }
}
