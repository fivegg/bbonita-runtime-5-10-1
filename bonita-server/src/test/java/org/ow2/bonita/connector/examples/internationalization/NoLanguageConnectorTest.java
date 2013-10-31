/**
 * Copyright (C) 2009  BonitaSoft S.A..
 * BonitaSoft, 31 rue Gustave Eiffel - 38000 Grenoble
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
package org.ow2.bonita.connector.examples.internationalization;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import junit.framework.Assert;

import org.ow2.bonita.connector.core.Connector;
import org.ow2.bonita.connector.core.InternationalizationConnectorTest;
import org.ow2.bonita.connector.core.desc.Category;
import org.ow2.bonita.connector.core.desc.Component;

/**
 * @author Matthieu Chaffotte
 *
 */
public class NoLanguageConnectorTest extends InternationalizationConnectorTest {

  @Override
  protected String getExpectedFrenchDescription() {
    return null;
  }

  @Override
  public void testGetAllFrenchInputs() {
    connector.setLocale(new Locale("fr", "FR"));
    List<Component> inputs = connector.getAllInputs();
    Assert.assertEquals(4, inputs.size());

    /*Component a = new Text("a", "a", false, String.class, 20, 20);
    Component b = new Text("b", "b", false, String.class, 20, 20);
    Component c = new Text("c", "c", false, String.class, 20, 20);
    Component d = new Text("d", "d", false, String.class, 20, 20);

    Assert.assertTrue(inputs.contains(a));
    Assert.assertTrue(inputs.contains(b));
    Assert.assertTrue(inputs.contains(c));
    Assert.assertTrue(inputs.contains(d));*/
  }

  @Override
  protected Class<? extends Connector> getConnectorClass() {
    return NoLanguageConnector.class;
  }

  @Override
  protected List<Category> getExpectedCategoryId() {
    List<Category> categories = new ArrayList<Category>();
    categories.add(new Category("Messaging", "org/ow2/bonita/connector/impl/category/Messagingr.png", null));
    return categories;
  }

  @Override
  protected String getExpectedDescription() {
    return null;
  }

  @Override
  protected String getExpectedConnectorId() {
    return "NoLanguage";
  }

  @Override
  public void testGetAllInputs() {
    connector.setLocale(new Locale("en", "US"));
    List<Component> inputs = connector.getAllInputs();
    Assert.assertEquals(4, inputs.size());

    /*Component a = new Text("a", "a", false, String.class, 20, 20);
    Component b = new Text("b", "b", false, String.class, 20, 20);
    Component c = new Text("c", "c", false, String.class, 20, 20);
    Component d = new Text("d", "d", false, String.class, 20, 20);

    Assert.assertTrue(inputs.contains(a));
    Assert.assertTrue(inputs.contains(b));
    Assert.assertTrue(inputs.contains(c));
    Assert.assertTrue(inputs.contains(d));*/
  }

  @Override
  public void testGetAllPageInputs() {
  }

  @Override
  public void testGetPages() {
    List<String> pages = connector.getPages();
    Assert.assertEquals(0, pages.size());
  }
}
