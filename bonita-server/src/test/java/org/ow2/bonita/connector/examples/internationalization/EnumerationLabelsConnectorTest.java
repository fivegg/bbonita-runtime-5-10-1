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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import junit.framework.Assert;

import org.ow2.bonita.connector.core.Connector;
import org.ow2.bonita.connector.core.InternationalizationConnectorTest;
import org.ow2.bonita.connector.core.desc.Category;
import org.ow2.bonita.connector.core.desc.Component;

/**
 * @author chaffotm
 *
 */
public class EnumerationLabelsConnectorTest extends
    InternationalizationConnectorTest {

  @Override
  protected String getExpectedFrenchDescription() {
  	return "Envoyer des courriels";
  }

  @Override
  public void testGetAllFrenchInputs() {
    connector.setLocale(new Locale("fr", "FR"));
    List<Component> inputs = connector.getAllInputs();
    Assert.assertEquals(2, inputs.size());
    Map<String, String> values = new HashMap<String, String>();
    values.put("Choix A", "s");
    values.put("Choix B", "g");
    values.put("Choix Q", "z");
    values.put("Choix P", "f");
    int[] top = new int [1];
    top[0] = 1;
    /*Component a = new Enumeration("Hote SMTP", "a", false, String.class, values, top, 2, Selection.SINGLE);
    Assert.assertTrue(inputs.contains(a));*/
  }

  @Override
  protected Class<? extends Connector> getConnectorClass() {
	  return EnumerationLabelsConnector.class;
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
  protected String getExpectedConnectorId() {
	  return "TempEnum";
  }

  @Override
  public void testGetAllInputs() {
    connector.setLocale(new Locale("en", "US"));
    List<Component> inputs = connector.getAllInputs();
    Assert.assertEquals(2, inputs.size());
    Map<String, String> values = new HashMap<String, String>();
    values.put("Choice A", "s");
    values.put("Choice B", "g");
    values.put("Choice Q", "z");
    values.put("Choice P", "f");
    int[] top = new int [1];
    top[0] = 1;
    /*Component a = new Enumeration("SMTP Host", "a", false, String.class, values, top, 2, Selection.SINGLE);
    Assert.assertTrue(inputs.contains(a));*/
  }

  @Override
  public void testGetAllPageInputs() {
  }

  @Override
  public void testGetPages() {
  }
}
