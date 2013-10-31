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
package org.ow2.bonita.connector.core.configuration;

import java.util.List;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.ow2.bonita.connector.core.ConnectorDescription;
import org.ow2.bonita.connector.core.ConnectorException;
import org.ow2.bonita.connector.examples.InputsOutputConnector;
import org.ow2.bonita.connector.examples.NoFieldsConnector;

public class ConfigurationTest extends TestCase {

  public void testCreateARootConfiguration() {
    try {
    	getEmptyConfiguration();
    } catch (ConnectorException e) {
      fail("This configuration has a good id.");
    }
  }

  public void testCreateANullRootConfiguration() throws ConnectorException {
    try {
    	ConnectorDescription desc =
    		new ConnectorDescription(InputsOutputConnector.class);
      new Configuration(null, desc);
      fail("A configuration cannot be null!");
    } catch (IllegalArgumentException e) {
    }
  }

  public void testCreateAnEmptyRootConfiguration() throws ConnectorException {
    try {
    	ConnectorDescription desc =
    		new ConnectorDescription(InputsOutputConnector.class);
      new Configuration("", desc);
      fail("A configuration cannot be empty!");
    } catch (IllegalArgumentException e) {
    }
  }
  
  public void testCreateRootConfigurationWithANullConnector() {
    try {
    	ConnectorDescription c = null;
      new Configuration("A", c);
      fail("A configuration cannot be empty!");
    } catch (IllegalArgumentException e) {
    }
  }

  public void testCreate2Configurations() {
    try {
      Configuration config = getEmptyConfiguration();
      new Configuration("B", config);
    } catch (ConnectorException e) {
      fail("These are two good configurations with different ID");
    }
  }

  public void testCreate2ConfigurationsWithTheSameId() throws ConnectorException {
    try {
      Configuration config = getEmptyConfiguration();
      new Configuration("A", config);
      fail("Two configurations cannot have the same ID!");
    } catch (IllegalArgumentException e) {
    }
  }

  public void testCreateAConfigurationFromANullConfiguration() {
    try {
      Configuration config = null;
      new Configuration("A", config);
      fail("A configuration cannot be created from a null one!");
    } catch (IllegalArgumentException e) {
    }
  }
  
  public void testEquals() throws ConnectorException {
    ConnectorDescription desc1 = new ConnectorDescription(InputsOutputConnector.class);
    ConnectorDescription desc2 = new ConnectorDescription(NoFieldsConnector.class);
    Configuration conf1 = new Configuration("A", desc1);
    Configuration conf2 = new Configuration("A", desc2);

    Assert.assertTrue(conf1.equals(conf2));
  }
  
  public void testNotEquals() throws ConnectorException {
    ConnectorDescription desc1 = new ConnectorDescription(InputsOutputConnector.class);
    ConnectorDescription desc2 = new ConnectorDescription(NoFieldsConnector.class);
    Configuration conf1 = new Configuration("A", desc1);
    Configuration conf2 = new Configuration("B", desc2);

    Assert.assertFalse(conf1.equals(conf2));
  }
  
  public void testNullEquals() throws ConnectorException {
    ConnectorDescription desc1 = new ConnectorDescription(NoFieldsConnector.class);
    Configuration conf1 = new Configuration("A", desc1);
    Configuration conf2 = null;

    Assert.assertFalse(conf1.equals(conf2));
  }
  
  public void testOtherEquals() throws ConnectorException {
    ConnectorDescription desc1 = new ConnectorDescription(NoFieldsConnector.class);
    Configuration conf1 = new Configuration("A", desc1);
    String conf2 = "A";

    Assert.assertFalse(conf1.equals(conf2));
  }

  public void testCreateManyConfigurations() {
    try {
      Configuration configA = getEmptyConfiguration();
      Configuration configB = new Configuration("B", configA);
      new Configuration("C", configA);
      Configuration configD = new Configuration("D", configA);

      new Configuration("E", configB);
      new Configuration("F", configB);

      new Configuration("G", configD);
      new Configuration("H", configD);
      new Configuration("I", configD);
    }
    catch (Exception e) {
      fail("This a good configurations hierarchy!");
    }
  }

  public void testCreateManyConfigurationsWithTheSameID() throws ConnectorException {
    Configuration configA = getEmptyConfiguration();
    Configuration configB = new Configuration("B", configA);
    new Configuration("C", configA);
    Configuration configD = new Configuration("D", configA);

    Configuration configE = new Configuration("E", configB);
    new Configuration("F", configB);

    new Configuration("G", configD);
    new Configuration("H", configD);
    new Configuration("I", configD);
    try {
      new Configuration("I", configE);
      fail("Two configurations cannot have the same ID!");
    }
    catch (Exception e) {
    }
  }

  public void testChangeConfigurationName() throws ConnectorException {
    Configuration configA = getEmptyConfiguration();
    Configuration configB = new Configuration("B", configA);
    new Configuration("C", configA);
    Configuration configD = new Configuration("D", configA);

    Configuration configE = new Configuration("E", configB);
    new Configuration("F", configB);

    new Configuration("G", configD);
    new Configuration("H", configD);
    new Configuration("I", configD);
    try {
      configE.setId("K");
    } catch (Exception e) {
      fail("The configuration name can be changed!");
    }
  }

  public void testCannotChangeConfigurationName() throws ConnectorException {
    Configuration configA = getEmptyConfiguration();
    Configuration configB = new Configuration("B", configA);
    new Configuration("C", configA);
    Configuration configD = new Configuration("D", configA);

    Configuration configE = new Configuration("E", configB);
    new Configuration("F", configB);

    new Configuration("G", configD);
    new Configuration("H", configD);
    new Configuration("I", configD);
    try {
      configE.setId("I");
      fail("The configuration name has already been taken.");
    } catch (Exception e) {
    }
  }

  public void testAddParameter() throws ConnectorException {
    Configuration config = getEmptyConfiguration();
    Parameter param = new Parameter("hostName", "bonita", String.class.getName());
    config.addParameter(param);
    List<Parameter> params = config.getParameters();
    Assert.assertEquals(1, params.size());
    Parameter expected = new Parameter("hostName", "bonita", String.class.getName());
    Assert.assertEquals(expected, params.get(0));
  }

  /*public void testAddUnknownParameter() {
    Configuration config = getEmptyConfiguration();
    Parameter param = new Parameter("host", "bonita", Type.STRING);
    try {
      config.addParameter(param);
      fail("Impossible ! host is not an attribute of H2Connector!");
    } catch (Exception e) {
    }
  }*/

  public void testAddNullParameter() throws ConnectorException {
    Configuration config = getEmptyConfiguration();
    Parameter param = null;
    try {
      config.addParameter(param);
      fail("Impossible ! a parameter cannot be null!");
    } catch (Exception e) {
    }
  }

  public void testAddParameterWithNullName() throws ConnectorException {
    Configuration config = getEmptyConfiguration();
    try {
      config.addParameter(new Parameter(null, "something", Float.class.getName()));
      fail("Impossible ! a parameter name cannot be null!");
    } catch (Exception e) {
    }
  }

  public void testAddParameterWithEmptyName() throws ConnectorException {
    Configuration config = getEmptyConfiguration();
    try {
      config.addParameter(new Parameter("", "something", Float.class.getName()));
      fail("Impossible ! a parameter name cannot be emtpy!");
    } catch (Exception e) {
    }
  }

  public void testAddParameterWithNullValue() throws ConnectorException {
    Configuration config = getEmptyConfiguration();
    try {
      config.addParameter(new Parameter("hostName", null, String.class.getName()));
      fail("Impossible ! a parameter name cannot be emtpy!");
    } catch (Exception e) {
    }
  }

  public void testAddParameterWithNullType() throws ConnectorException {
    Configuration config = getEmptyConfiguration();
    try {
      config.addParameter(new Parameter("hostName", "", null));
      fail("Impossible ! a parameter type cannot be null!");
    } catch (Exception e) {
    }
  }

  public void testUpdateParameterValue() throws ConnectorException {
    Configuration config = getEmptyConfiguration();
    Parameter param = new Parameter("hostName", "bonita", String.class.getName());
    config.addParameter(param);
    param.setValue("Bonita");
    config.addParameter(param);
    List<Parameter> params = config.getParameters();
    Assert.assertEquals(1, params.size());
    Parameter expected = new Parameter("hostName", "Bonita", String.class.getName());
    Assert.assertEquals(expected, params.get(0));
  }
  
  public void testGetParameter() throws ConnectorException {
  	Configuration config = get3Configurations();
  	Parameter expected = new Parameter("local", "false", Boolean.class.getName());
  	Parameter actual = config.getParameter("local");
    Assert.assertEquals(expected, actual);
  }
  
  public void testGetParameterFromParent() throws ConnectorException {
  	Configuration config = get3Configurations();
  	Parameter expected = new Parameter("hostName", "bonita", String.class.getName());
  	Parameter actual = config.getParameter("hostName");
    Assert.assertEquals(expected, actual);
  }
  
  public void testGetUnknownParameterFromParent() throws ConnectorException {
  	Configuration config = get3Configurations();
  	Parameter actual = config.getParameter("commit");
  	Assert.assertNull(actual);
  }
  
  public void testGetUnknownParameter() throws ConnectorException {
  	Configuration config = getConfiguration();
  	Parameter actual = config.getParameter("commit");
  	Assert.assertNull(actual);
  }

  public void testGetParameters() throws ConnectorException {
    Configuration config = getConfiguration();
    List<Parameter> params = config.getParameters();
    Assert.assertEquals(3, params.size());

    Parameter one = new Parameter("hostName", "bonita", String.class.getName());
    Parameter two = new Parameter("port", "8082", Integer.class.getName());
    Parameter three = new Parameter("database", "bonitadb", String.class.getName());
    
    assertTrue(params.contains(one));
    assertTrue(params.contains(two));
    assertTrue(params.contains(three));
  }

  public void testGetLocalParametersFromARootConfiguration() throws ConnectorException {
  	Parameter one = new Parameter("hostName", "bonita", String.class.getName());
    Parameter two = new Parameter("port", "8082", Integer.class.getName());
    Parameter three = new Parameter("database", "bonitadb", String.class.getName());
  	
  	Configuration config = getConfiguration();
  	List<Parameter> params = config.getLocalParameters();
  	Assert.assertEquals(3, params.size());

    assertTrue(params.contains(one));
    assertTrue(params.contains(two));
    assertTrue(params.contains(three));
  }

  public void testGetLocalParametersFromAChildConfiguration() throws ConnectorException {
  	Parameter one = new Parameter("local", "false", Boolean.class.getName());
  	Parameter two = new Parameter("query", "SELECT * FROM ...", String.class.getName());
  	
  	Configuration config = get3Configurations();
  	List<Parameter> params = config.getLocalParameters();
  	Assert.assertEquals(2, params.size());

    assertTrue(params.contains(one));
    assertTrue(params.contains(two));
  }

  public void testGetParametersFrom2Configurations() throws ConnectorException {
    Configuration conf = get2Configurations();
    List<Parameter> params = conf.getParameters();
    Assert.assertEquals(5, params.size());

    Parameter one = new Parameter("hostName", "bonita", String.class.getName());
    Parameter two = new Parameter("port", "8082", Integer.class.getName());
    Parameter three = new Parameter("database", "bonitadb", String.class.getName());
    Parameter four = new Parameter("username", "john", String.class.getName());
    Parameter five = new Parameter("password", "doe", String.class.getName());

    assertTrue(params.contains(one));
    assertTrue(params.contains(two));
    assertTrue(params.contains(three));
    assertTrue(params.contains(four));
    assertTrue(params.contains(five));
  }

  public void testGetParametersFrom2ConfigurationsWithAChange() throws ConnectorException {
    Configuration conf = get2Configurations();
    conf.addParameter(new Parameter("port", "8081", Integer.class.getName()));
    List<Parameter> params = conf.getParameters();
    Assert.assertEquals(5, params.size());

    Parameter one = new Parameter("hostName", "bonita", String.class.getName());
    Parameter two = new Parameter("port", "8081", Integer.class.getName());
    Parameter three = new Parameter("database", "bonitadb", String.class.getName());
    Parameter four = new Parameter("username", "john", String.class.getName());
    Parameter five = new Parameter("password", "doe", String.class.getName());

    assertTrue(params.contains(one));
    assertTrue(params.contains(two));
    assertTrue(params.contains(three));
    assertTrue(params.contains(four));
    assertTrue(params.contains(five));
  }

  public void testGetParametersFrom3ConfigurationsWithAChange() throws ConnectorException {
    Configuration conf = get3Configurations();
    conf.addParameter(new Parameter("port", "8081", Integer.class.getName()));
    List<Parameter> params = conf.getParameters();
    Assert.assertEquals(7, params.size());

    Parameter one = new Parameter("hostName", "bonita", String.class.getName());
    Parameter two = new Parameter("port", "8081", Integer.class.getName());
    Parameter three = new Parameter("database", "bonitadb", String.class.getName());
    Parameter four = new Parameter("username", "john", String.class.getName());
    Parameter five = new Parameter("password", "doe", String.class.getName());
    Parameter six = new Parameter("local", "false", Boolean.class.getName());
    Parameter seven = new Parameter("query", "SELECT * FROM ...", String.class.getName());

    assertTrue(params.contains(one));
    assertTrue(params.contains(two));
    assertTrue(params.contains(three));
    assertTrue(params.contains(four));
    assertTrue(params.contains(five));
    assertTrue(params.contains(six));
    assertTrue(params.contains(seven));
  }
  
  public void testRemoveExistingParameter() throws ConnectorException {
    Configuration config = getConfiguration();
    config.removeParameter(new Parameter("port", "8082", Integer.class.getName()));
    List<Parameter> params = config.getParameters();
    Assert.assertEquals(2, params.size());

    Parameter one = new Parameter("hostName", "bonita", String.class.getName());
    Parameter three = new Parameter("database", "bonitadb", String.class.getName());

    assertTrue(params.contains(one));
    assertTrue(params.contains(three));
  }
  
  public void testRemoveUnknownParameter() throws ConnectorException {
    Configuration config = getConfiguration();
    config.removeParameter(new Parameter("MyPort", "8082", Integer.class.getName()));
    List<Parameter> params = config.getParameters();
    Assert.assertEquals(3, params.size());

    Parameter one = new Parameter("hostName", "bonita", String.class.getName());
    Parameter two = new Parameter("port", "8082", Integer.class.getName());
    Parameter three = new Parameter("database", "bonitadb", String.class.getName());

    assertTrue(params.contains(one));
    assertTrue(params.contains(two));
    assertTrue(params.contains(three));
  }

  public void testRemoveParameterFromAnUpperConfiguration() throws ConnectorException {
    Configuration conf = get3Configurations();
    conf.removeParameter(new Parameter("hostName", "bonita", String.class.getName()));
    List<Parameter> params = conf.getParameters();
    Assert.assertEquals(7, params.size());

    Parameter one = new Parameter("hostName", "bonita", String.class.getName());
    Parameter two = new Parameter("port", "8082", Integer.class.getName());
    Parameter three = new Parameter("database", "bonitadb", String.class.getName());
    Parameter four = new Parameter("username", "john", String.class.getName());
    Parameter five = new Parameter("password", "doe", String.class.getName());
    Parameter six = new Parameter("local", "false", Boolean.class.getName());
    Parameter seven = new Parameter("query", "SELECT * FROM ...", String.class.getName());

    assertTrue("A parameter from an upper configuration cannot be removed",
        params.contains(one));
    assertTrue(params.contains(two));
    assertTrue(params.contains(three));
    assertTrue(params.contains(four));
    assertTrue(params.contains(five));
    assertTrue(params.contains(six));
    assertTrue(params.contains(seven));
  }
  
  private Configuration getEmptyConfiguration() throws ConnectorException {
  	ConnectorDescription desc =
  		new ConnectorDescription(InputsOutputConnector.class);
    return new Configuration("A", desc);
  }

  private Configuration getConfiguration() throws ConnectorException {
  	ConnectorDescription desc =
  		new ConnectorDescription(InputsOutputConnector.class);
    Configuration config = new Configuration("confBonita", desc);
    config.addParameter(new Parameter("hostName", "bonita", String.class.getName()));
    config.addParameter(new Parameter("port", "8082", Integer.class.getName()));
    config.addParameter(new Parameter("database", "bonitadb", String.class.getName()));
    return config;
  }

  private Configuration get2Configurations() throws ConnectorException {
    Configuration parent = getConfiguration();
    Configuration conf = new Configuration("B", parent);
    conf.addParameter(new Parameter("username", "john", String.class.getName()));
    conf.addParameter(new Parameter("password", "doe", String.class.getName()));
    return conf;
  }

  private Configuration get3Configurations() throws ConnectorException {
    Configuration parent = get2Configurations();
    Configuration conf = new Configuration("Cc", parent);
    conf.addParameter(new Parameter("local", "false", Boolean.class.getName()));
    conf.addParameter(new Parameter("query", "SELECT * FROM ...", String.class.getName()));
    return conf;
  }
}
