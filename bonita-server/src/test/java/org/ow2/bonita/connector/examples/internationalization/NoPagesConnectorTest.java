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
package org.ow2.bonita.connector.examples.internationalization;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import junit.framework.Assert;

import org.ow2.bonita.connector.core.Connector;
import org.ow2.bonita.connector.core.InternationalizationConnectorTest;
import org.ow2.bonita.connector.core.desc.Category;
import org.ow2.bonita.connector.core.desc.Component;

public class NoPagesConnectorTest extends InternationalizationConnectorTest {

  @Override
  protected Class<? extends Connector> getConnectorClass() {
    return NoPagesConnector.class;
  }

  @Override
  protected String getExpectedConnectorId() {
    return "Id";
  }

  @Override
  public void testGetAllInputs() {
    connector.setLocale(new Locale("en", "US"));
    List<Component> inputs = connector.getAllInputs();
    Assert.assertEquals(4, inputs.size());

    /*Component a = new Text("SMTP Host", "a", false, String.class, 20, 20);
    Component b = new Text("SMTP Port", "b", false, String.class, 20, 20);
    Component c = new Text("from", "c",false, String.class, 20, 20);
    Component d = new Text("to", "d",false, String.class, 20, 20);

    Assert.assertTrue(inputs.contains(a));
    Assert.assertTrue(inputs.contains(b));
    Assert.assertTrue(inputs.contains(c));
    Assert.assertTrue(inputs.contains(d));*/
  }

  @Override
  public void testGetAllFrenchInputs() {
    connector.setLocale(new Locale("fr", "FR"));
    List<Component> inputs = connector.getAllInputs();
    Assert.assertEquals(4, inputs.size());

    /*Component a = new Text("Hote SMTP", "a", false, String.class, 20, 20);
    Component b = new Text("Port SMTP", "b", false, String.class, 20, 20);
    Component c = new Text("de", "c", false, String.class, 20, 20);
    Component d = new Text("a", "d", false, String.class, 20, 20);

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

  @Override
  protected List<Category> getExpectedCategoryId() {
    List<Category> categories = new ArrayList<Category>();
    categories.add(new Category("Messaging", "org/ow2/bonita/connector/impl/category/Messagingr.png", null));
    return categories;
  }

  @Override
  protected String getExpectedDescription() {
    return "Sending e-mails";
  }

  @Override
  protected String getExpectedFrenchDescription() {
    return "Envoyer des courriels";
  }
}
