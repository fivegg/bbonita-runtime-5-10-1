/**
 * Copyright (C) 2009  Bull S. A. S.
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
package org.ow2.bonita.connector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.TestCase;

import org.bonitasoft.connectors.bonita.AddCommentConnector;
import org.bonitasoft.connectors.bonita.ExecuteTaskConnector;
import org.bonitasoft.connectors.bonita.FinishTaskConnector;
import org.bonitasoft.connectors.bonita.SetVarConnector;
import org.bonitasoft.connectors.bonita.StartTaskConnector;
import org.bonitasoft.connectors.bonita.filters.RandomMultipleFilter;
import org.bonitasoft.connectors.bonita.filters.UniqueRandomFilter;
import org.bonitasoft.connectors.bonita.instantiators.FixedNumberInstantiator;
import org.bonitasoft.connectors.bonita.instantiators.GroovyInstantiator;
import org.bonitasoft.connectors.bonita.joincheckers.FixedNumberJoinChecker;
import org.bonitasoft.connectors.bonita.joincheckers.GroovyJoinChecker;
import org.bonitasoft.connectors.bonita.joincheckers.PercentageJoinChecker;
import org.bonitasoft.connectors.bonita.resolvers.ProcessInitiatorRoleResolver;
import org.bonitasoft.connectors.bonita.resolvers.UserListRoleResolver;
import org.bonitasoft.connectors.java.JavaConnector;
import org.bonitasoft.connectors.legacy.HookConnector;
import org.bonitasoft.connectors.legacy.RoleMapperRoleResolver;
import org.bonitasoft.connectors.legacy.VariablePerformerAssignFilter;
import org.bonitasoft.connectors.scripting.GroovyConnector;
import org.bonitasoft.connectors.scripting.ShellConnector;
import org.ow2.bonita.activity.multipleinstances.instantiator.EmptyContextInitiator;
import org.ow2.bonita.activity.multipleinstances.instantiator.NoContextMulitpleActivitiesInstantiator;
import org.ow2.bonita.activity.multipleinstances.instantiator.NullMulitpleActivitiesInstantiator;
import org.ow2.bonita.connector.core.Connector;
import org.ow2.bonita.connector.core.ConnectorAPI;
import org.ow2.bonita.connector.core.ConnectorDescription;
import org.ow2.bonita.connector.core.ConnectorException;
import org.ow2.bonita.connector.core.Filter;
import org.ow2.bonita.connector.core.MultipleInstancesInstantiator;
import org.ow2.bonita.connector.core.MultipleInstancesJoinChecker;
import org.ow2.bonita.connector.core.PerformerAssignFilter;
import org.ow2.bonita.connector.core.ProcessConnector;
import org.ow2.bonita.connector.core.RoleResolver;
import org.ow2.bonita.connector.core.desc.Category;
import org.ow2.bonita.connector.example.NoDescriptorConnector;
import org.ow2.bonita.connector.example.NoFieldsConnector;
import org.ow2.bonita.connector.example.OtherConnector;
import org.ow2.bonita.connector.example.TestEncodingConnector;
import org.ow2.bonita.expression.ExpressionEvaluator;

public class ConnectorAPITest extends TestCase {

  protected static final Logger LOG = Logger.getLogger(ConnectorAPITest.class.getName());

  private List<Category> getAllEnCategories() {
    List<Category> cat = new ArrayList<Category>();
    cat.add(new Category("Bonita", "org/bonitasoft/connectors/bonita/bonita.png", SetVarConnector.class.getClassLoader()));
    cat.add(new Category("Java", "org/bonitasoft/connectors/java/java.png", JavaConnector.class.getClassLoader()));
    cat.add(ConnectorAPI.other);
    cat.add(new Category("Scripting", "org/bonitasoft/connectors/scripting/scripting.png", GroovyConnector.class.getClassLoader()));
    return cat;
  }

  private List<Category> getAllEnJavaCategories() {
    List<Category> cat = new ArrayList<Category>();
    cat.add(new Category("Bonita", "org/bonitasoft/connectors/bonita/bonita.png", SetVarConnector.class.getClassLoader()));
    cat.add(new Category("Java", "org/bonitasoft/connectors/java/java.png", JavaConnector.class.getClassLoader()));
    cat.add(ConnectorAPI.other);
    cat.add(new Category("Scripting", "org/bonitasoft/connectors/scripting/scripting.png", GroovyConnector.class.getClassLoader()));
    return cat;
  }

  private List<String> getBonitaConnectors() {
    List<String> list = new ArrayList<String>();
    list.add(SetVarConnector.class.getName());
    list.add(StartTaskConnector.class.getName());
    list.add(FinishTaskConnector.class.getName());
    list.add(ExecuteTaskConnector.class.getName());
    list.add(AddCommentConnector.class.getName());
    list.add(HookConnector.class.getName());
    return list;
  }
  
  private List<String> getScriptConnectors() {
    List<String> list = new ArrayList<String>();
    list.add(GroovyConnector.class.getName());
    list.add(ShellConnector.class.getName());
    return list;
  }

  private List<String> getOtherConnectors() {
  	List<String> list = new ArrayList<String>();
  	list.add(OtherConnector.class.getName());
    list.add(NoFieldsConnector.class.getName());
    list.add(NoDescriptorConnector.class.getName());
    return list;
  }

  private List<String> getJavaConnectors() {
    List<String> list = new ArrayList<String>();
    list.addAll(getBonitaConnectors());
    list.addAll(getScriptConnectors());
    list.addAll(getOtherConnectors());
    list.add(JavaConnector.class.getName());
    return list;
  }

  private List<String> getFilters() {
    List<String> list = new ArrayList<String>();
    list.add(RandomMultipleFilter.class.getName());
    list.add(UniqueRandomFilter.class.getName());
    list.add(PerformerAssignFilter.class.getName());
    list.add(VariablePerformerAssignFilter.class.getName());
    return list;
  }

  private List<String> getResolvers() {
    List<String> list = new ArrayList<String>();
    list.add(ProcessInitiatorRoleResolver.class.getName());
    list.add(UserListRoleResolver.class.getName());
    list.add(RoleMapperRoleResolver.class.getName());
    return list;
  }

  private List<String> getInstantiators() {
    List<String> list = new ArrayList<String>();
    list.add(NullMulitpleActivitiesInstantiator.class.getName());
    list.add(EmptyContextInitiator.class.getName());
    list.add(NoContextMulitpleActivitiesInstantiator.class.getName());
    list.add(FixedNumberInstantiator.class.getName());
    list.add(GroovyInstantiator.class.getName());
    return list;
  }

  private List<String> getJoinCheckers() {
    List<String> list = new ArrayList<String>();
    //list.add(DefinedNumberJoinChecker.class.getName());
    list.add(FixedNumberJoinChecker.class.getName());
    list.add(GroovyJoinChecker.class.getName());
    list.add(PercentageJoinChecker.class.getName());
    return list;
  }

  private List<String> getNotConnectors() {
    List<String> list = new ArrayList<String>();
    list.add(Connector.class.getName());
    list.add(Filter.class.getName());
    list.add(RoleResolver.class.getName());
    list.add(ProcessConnector.class.getName());
    list.add(String.class.getName());
    list.add(ExpressionEvaluator.class.getName());
    list.add(MultipleInstancesInstantiator.class.getName());
    list.add(MultipleInstancesJoinChecker.class.getName());
    return list;
  }
  
  private List<String> getAllConnectors() {
    List<String> list = new ArrayList<String>();
    list.addAll(getJavaConnectors());
    list.addAll(getFilters());
    list.addAll(getResolvers());
    list.addAll(getInstantiators());
    list.addAll(getJoinCheckers());
    return list;
  }
  
  private List<String> getClassesList() {
    List<String> list = new ArrayList<String>();
    list.addAll(getAllConnectors());
    list.addAll(getNotConnectors());
    return list;
  }
  
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    if (LOG.isLoggable(Level.WARNING)) {
      LOG.warning("======== Starting test: " + this.getClass().getName() + "." + this.getName() + "() ==========");
    }
  }

  @Override
  protected void tearDown() throws Exception {
    if (LOG.isLoggable(Level.WARNING)) {
      LOG.warning("======== Ending test: " + this.getName() + " ==========");
    }
    super.tearDown();
  }

  public void testGetDefaultConnectors() {
  	ConnectorAPI c = getDefaultConnectorAPI();
    List<ConnectorDescription> descriptions = c.getAllConnectors();
    assertEquals(getAllConnectors().size(), descriptions.size());
  }

  public void testGetJavaConnectors() {
  	ConnectorAPI c = getDefaultConnectorAPI();
    List<ConnectorDescription> descriptions = c.getJavaConnectors();
    assertEquals(getJavaConnectors().size(), descriptions.size());
  }

  public void testGetRoleResolversConnectors() {
  	ConnectorAPI c = getDefaultConnectorAPI();
    List<ConnectorDescription> descriptions = c.getRoleResolvers();
    assertEquals(getResolvers().size(), descriptions.size());
  }

  public void testGetFiltersConnectors() {
  	ConnectorAPI c = getDefaultConnectorAPI();
    List<ConnectorDescription> descriptions = c.getFilters();
    assertEquals(getFilters().size(), descriptions.size());
  }

  public void testGetJoinCheckersConnectors() {
    ConnectorAPI c = getDefaultConnectorAPI();
    List<ConnectorDescription> descriptions = c.getJoinCheckers();
    assertEquals(getJoinCheckers().size(), descriptions.size());
  }

  public void testGetInstantiatorsConnectors() {
    ConnectorAPI c = getDefaultConnectorAPI();
    List<ConnectorDescription> descriptions = c.getInstantiators();
    assertEquals(getInstantiators().size(), descriptions.size());
  }

  private void ListsEqual(List<Category> first, List<Category> second) {
    for (int i = 0; i < first.size(); i++) {
      System.out.println(first.get(i).getName() + " " + first.get(i).getIconPath());
      System.out.println(second.get(i).getName() + " " + second.get(i).getIconPath());
      assertEquals(first.get(i), second.get(i));
    }
  }

  public void testGetAllCategories() {
  	ConnectorAPI c = getDefaultConnectorAPI();
    List<Category> actual = c.getAllCategories();
    List<Category> expected = getAllEnCategories();
    assertEquals(expected.size(), actual.size());
    ListsEqual(expected, actual);
  }

  public void testInvalidConnectorId() throws Exception {
  	ConnectorAPI c = getDefaultConnectorAPI();
    try {
      c.getJavaConnector("Nothing");
      fail("Nothing is not a valid Connector identifier");
    } catch (IllegalArgumentException iae) {
    } catch (Exception e) {
      fail("An IllegalArgumentException should have been thrown");
    }
  }

  public void testConnectorId() throws Exception {
  	ConnectorAPI c = getDefaultConnectorAPI();
    try {
      ConnectorDescription connector = c.getRoleResolverConnector("ProcessInitiatior");
      assertNotNull(connector);
    } catch (Exception e) {
      fail("Impossible! \"ProcessInitiatior\" is the EmailConnector ID!");
    }
  }

  public void testGetJavaConnectorsCategories() {
    ConnectorAPI c = getDefaultConnectorAPI();
    List<Category> actual = c.getJavaConnectorsCategories();
    List<Category> expected = getAllEnJavaCategories();
    assertEquals(expected.size(), actual.size());
    ListsEqual(expected, actual);
  }

  public void testGetScriptingJavaConnectors() {
  	ConnectorAPI c = getDefaultConnectorAPI();
    List<ConnectorDescription> databases = c.getJavaConnectors("Scripting");
    assertEquals(getScriptConnectors().size(), databases.size());
  }

  public void testGetFrenchJavaConnectorsCategories() {
    ConnectorAPI c = getDefaultFrenchConnectorAPI();
    List<Category> actual = c.getJavaConnectorsCategories();
    List<Category> expected = getAllEnJavaCategories();
    assertEquals(expected.size(), actual.size());
  }

  public void testGetFrenchScriptingJavaConnectors() {
    ConnectorAPI c = getDefaultFrenchConnectorAPI();
    List<ConnectorDescription> databases = c.getJavaConnectors("Script");
    assertEquals(getScriptConnectors().size(), databases.size());
  }

  public void testGetOtherJavaConnectors() {
    ConnectorAPI c = getDefaultConnectorAPI();
    List<ConnectorDescription> other = c.getJavaConnectors("Other");
    assertEquals(getOtherConnectors().size(), other.size());
  }

  public void testGetUnknownJavaConnectors() {
  	ConnectorAPI c = getDefaultConnectorAPI();
    List<ConnectorDescription> list = c.getJavaConnectors("Nothing");
    assertTrue(list.isEmpty());
  }
  
  public void testBadConnector() throws Exception {
    List<String> classesList = getClassesList();
    classesList.add("org.bonitasoft.connectors.GodModeConnector");
    ConnectorAPI c = new ConnectorAPI(ClassLoader.getSystemClassLoader(), classesList, Locale.ENGLISH);
    
    Collection<ConnectorException> exceptions = c.getExcpetions();
    assertEquals(1, exceptions.size());
    ConnectorException exception = exceptions.iterator().next();
    assertTrue(exception.getMessage().contains("java.lang.ClassNotFoundException"));
    assertTrue(exception.getMessage().contains("org.bonitasoft.connectors.GodModeConnector"));

  }
  
  private ConnectorAPI getDefaultFrenchConnectorAPI() {
    try {
      return new ConnectorAPI(ClassLoader.getSystemClassLoader(), getClassesList(), Locale.FRENCH);
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  private ConnectorAPI getDefaultConnectorAPI() {
    try {
      return new ConnectorAPI(ClassLoader.getSystemClassLoader(), getClassesList(), Locale.ENGLISH);
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }
  
  public void testConnectorEncoding() throws Exception {
	  // Test at engine level for bug 3154
	  List<String> classNames = new ArrayList<String>();
	  classNames.add(TestEncodingConnector.class.getName());
	  ConnectorAPI api = new ConnectorAPI(ClassLoader.getSystemClassLoader(), classNames, Locale.ENGLISH);
	  assertEquals("Encoding issue", "ü", api.getConnector("TestEncodingConnector").getDescription());
  }

}
