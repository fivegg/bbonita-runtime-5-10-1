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
 * 
 * Modified by Matthieu Chaffotte - BonitaSoft S.A.
 **/
package org.ow2.bonita.connector.core;

import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import org.ow2.bonita.connector.core.desc.Category;
import org.ow2.bonita.util.Misc;

/**
 * 
 * @author Matthieu Chaffotte
 * 
 */
public class ConnectorAPI {

  protected final static String OTHER_CATEGORY = "Other";
  protected final static String OTHER_ICON = "org/ow2/bonita/connector/core/other.png";
  public static final Category other = new Category(OTHER_CATEGORY, OTHER_ICON, Thread.currentThread()
      .getContextClassLoader());

  private final List<ConnectorDescription> connectors;
  private Locale currentLocale;
  private final ClassLoader classLoader;
  private final Collection<ConnectorException> exceptions;

  public ConnectorAPI(final ClassLoader classLoader, final List<String> classNames) throws Exception {
    this(classLoader, classNames, Locale.getDefault());
  }

  public ConnectorAPI(final ClassLoader classLoader, final List<String> classNames, final Locale locale)
      throws Exception {
    Misc.checkArgsNotNull(classLoader, classNames, locale);
    this.currentLocale = locale;
    this.connectors = new ArrayList<ConnectorDescription>();
    this.classLoader = classLoader;
    this.exceptions = new ArrayList<ConnectorException>();
    getConnectors(classNames);
  }

  private void getConnectors(final List<String> classNames) throws Exception {
    for (final String className : classNames) {
      final Class<?> javaClass = getClass(className);
      if (javaClass != null) {
        addClass(javaClass);
      }
    }
  }

  private Class<?> getClass(final String className) {
    try {
      return classLoader.loadClass(className);
    } catch (final Throwable e) {
      final ConnectorException exception = new ConnectorException(e, "Cannot load " + className, null);
      exceptions.add(exception);
      return null;
    }
  }

  @SuppressWarnings("unchecked")
  private void addClass(final Class<?> javaClass) {
    try {
      if (isAnInstanceOfConnector(javaClass)) {
        final ConnectorDescription temp = new ConnectorDescription((Class<? extends Connector>) javaClass,
            currentLocale);
        final String id = temp.getId();
        if (idExists(id)) {
          exceptions.add(new ConnectorException("Cannot load " + javaClass.getName() + " because a similar id exists",
              id, javaClass.getName(), null));
        }
        connectors.add(temp);
      }
    } catch (final ConnectorException e) {
      exceptions.add(e);
    } catch (final Throwable e) {
      String className = "null";
      if (javaClass != null) {
        className = javaClass.getName();
      }
      final ConnectorException exception = new ConnectorException(e, className, null);
      exceptions.add(exception);
    }
  }

  private boolean idExists(final String id) {
    boolean exists = false;
    for (final ConnectorDescription connector : connectors) {
      if (id.equals(connector.getId())) {
        exists = true;
        break;
      }
    }
    return exists;
  }

  public List<ConnectorDescription> getAllConnectors() {
    return connectors;
  }

  public List<ConnectorDescription> getJavaConnectors() {
    final List<ConnectorDescription> javaconnectors = new ArrayList<ConnectorDescription>();
    for (final ConnectorDescription connector : connectors) {
      final Class<? extends Connector> clazz = connector.getConnectorClass();
      if (isAConnector(clazz) && !isMapper(clazz) && !isMultiInstantiator(clazz) && !isInstantiator(clazz)
          && !isJoinChecker(clazz)) {
        javaconnectors.add(connector);
      }
    }
    return javaconnectors;
  }

  public List<ConnectorDescription> getRoleResolvers() {
    final List<ConnectorDescription> roleResolvers = new ArrayList<ConnectorDescription>();
    for (final ConnectorDescription connector : connectors) {
      final Class<? extends Connector> clazz = connector.getConnectorClass();
      if (isRoleResolver(clazz)) {
        roleResolvers.add(connector);
      }
    }
    return roleResolvers;
  }

  public List<ConnectorDescription> getFilters() {
    final List<ConnectorDescription> filters = new ArrayList<ConnectorDescription>();
    for (final ConnectorDescription connector : connectors) {
      final Class<? extends Connector> clazz = connector.getConnectorClass();
      if (isFilter(clazz)) {
        filters.add(connector);
      }
    }
    return filters;
  }

  @Deprecated
  public List<ConnectorDescription> getMultiInstantiators() {
    final List<ConnectorDescription> multis = new ArrayList<ConnectorDescription>();
    for (final ConnectorDescription connector : connectors) {
      final Class<? extends Connector> clazz = connector.getConnectorClass();
      if (isMultiInstantiator(clazz)) {
        multis.add(connector);
      }
    }
    return multis;
  }

  public List<ConnectorDescription> getInstantiators() {
    final List<ConnectorDescription> instantiators = new ArrayList<ConnectorDescription>();
    for (final ConnectorDescription connector : connectors) {
      final Class<? extends Connector> clazz = connector.getConnectorClass();
      if (isInstantiator(clazz)) {
        instantiators.add(connector);
      }
    }
    return instantiators;
  }

  public List<ConnectorDescription> getJoinCheckers() {
    final List<ConnectorDescription> joinCheckers = new ArrayList<ConnectorDescription>();
    for (final ConnectorDescription connector : connectors) {
      final Class<? extends Connector> clazz = connector.getConnectorClass();
      if (isJoinChecker(clazz)) {
        joinCheckers.add(connector);
      }
    }
    return joinCheckers;
  }

  private boolean isJoinChecker(final Class<?> clazz) {
    try {
      return clazz.newInstance() instanceof MultipleInstancesJoinChecker;
    } catch (final Throwable e) {
      return false;
    }
  }

  public List<ConnectorDescription> getAllConnectors(final String categoryName) {
    return getConnectors(connectors, categoryName);
  }

  public List<ConnectorDescription> getJavaConnectors(final String categoryName) {
    final List<ConnectorDescription> javaConnectors = getJavaConnectors();
    return getConnectors(javaConnectors, categoryName);
  }

  public List<ConnectorDescription> getRoleResolverConnectors(final String categoryName) {
    final List<ConnectorDescription> roleMappers = getRoleResolvers();
    return getConnectors(roleMappers, categoryName);
  }

  public List<ConnectorDescription> getFilterConnectors(final String categoryName) {
    final List<ConnectorDescription> mappers = getFilters();
    return getConnectors(mappers, categoryName);
  }

  @Deprecated
  public List<ConnectorDescription> getMultiInstantiatorConnectors(final String categoryName) {
    final List<ConnectorDescription> multi = getMultiInstantiators();
    return getConnectors(multi, categoryName);
  }

  public List<ConnectorDescription> getInstantiatorConnectors(final String categoryName) {
    final List<ConnectorDescription> multi = getInstantiators();
    return getConnectors(multi, categoryName);
  }

  public List<ConnectorDescription> getJoinCheckerConnectors(final String categoryName) {
    final List<ConnectorDescription> multi = getJoinCheckers();
    return getConnectors(multi, categoryName);
  }

  private List<ConnectorDescription> getConnectors(final List<ConnectorDescription> list, final String categoryName) {
    final List<ConnectorDescription> connectors = new ArrayList<ConnectorDescription>();
    for (final ConnectorDescription connector : list) {
      final List<Category> categories = connector.getCategories();
      for (final Category category : categories) {
        String categ = getPropertyValue(connector.getConnectorClass(), category.getName());
        if (categ == null) {
          categ = category.getName();
        }
        if (categoryName.equals(categ)) {
          connectors.add(connector);
        }
      }
    }
    return connectors;
  }

  public ConnectorDescription getConnector(final String id) {
    return getConnector(connectors, id);
  }

  public ConnectorDescription getRoleResolverConnector(final String id) {
    final List<ConnectorDescription> roleMappers = getRoleResolvers();
    return getConnector(roleMappers, id);
  }

  public ConnectorDescription getJavaConnector(final String id) {
    final List<ConnectorDescription> javaConnectors = getJavaConnectors();
    return getConnector(javaConnectors, id);
  }

  public ConnectorDescription getFilterConnector(final String id) {
    final List<ConnectorDescription> mappers = getFilters();
    return getConnector(mappers, id);
  }

  private ConnectorDescription getConnector(final List<ConnectorDescription> list, final String id) {
    ConnectorDescription desc = null;
    final int size = list.size();
    int i = 0;
    boolean found = false;
    while (i < size && !found) {
      final ConnectorDescription temp = list.get(i);
      if (id.equals(temp.getId())) {
        desc = temp;
        found = true;
      }
      i++;
    }
    if (!found) {
      throw new IllegalArgumentException("The identifier " + id + " does not refer to a Connector");
    }
    return desc;
  }

  public List<Category> getAllCategories() {
    return getCategories(connectors);
  }

  public List<Category> getJavaConnectorsCategories() {
    final List<ConnectorDescription> javaConnectors = getJavaConnectors();
    return getCategories(javaConnectors);
  }

  public List<Category> getRoleResolversCategories() {
    final List<ConnectorDescription> roleMappers = getRoleResolvers();
    return getCategories(roleMappers);
  }

  public List<Category> getFiltersCategories() {
    final List<ConnectorDescription> mappers = getFilters();
    return getCategories(mappers);
  }

  @Deprecated
  public List<Category> getMulitInstantiatorCategories() {
    final List<ConnectorDescription> multis = getMultiInstantiators();
    return getCategories(multis);
  }

  public List<Category> getInstantiatorCategories() {
    final List<ConnectorDescription> multis = getInstantiators();
    return getCategories(multis);
  }

  public List<Category> getJoinCheckerCategories() {
    final List<ConnectorDescription> multis = getJoinCheckers();
    return getCategories(multis);
  }

  private Map<String, String> getCategoriesMap(final List<ConnectorDescription> list) {
    final Map<String, String> categories = new HashMap<String, String>();
    for (final ConnectorDescription connector : list) {
      final List<Category> connectorCategories = connector.getCategories();
      if (connectorCategories.isEmpty()) {
        connectorCategories.add(other);
      }
      for (final Category category : connectorCategories) {
        final String categoryId = category.getName();
        String categoryName = connector.getCategoryName(categoryId);
        if (categoryName == null) {
          categoryName = categoryId;
        }
        if (!categories.containsKey(categoryName)) {
          categories.put(categoryName, category.getIconPath());
        } else {
          final String icon = categories.get(categoryName);
          final InputStream categoryIcon = category.getIcon();
          if (icon == null && categoryIcon != null) {
            categories.put(categoryName, category.getIconPath());
          }
        }
      }
    }
    return categories;
  }

  private List<Category> getCategories(final List<ConnectorDescription> list) {
    final Map<String, String> categoriesMap = getCategoriesMap(list);
    final Set<String> categoryNames = categoriesMap.keySet();
    final List<String> cat = new ArrayList<String>(categoryNames);
    Collections.sort(cat);
    final List<Category> categories = new ArrayList<Category>();
    for (final String categoryName : cat) {
      final String icon = categoriesMap.get(categoryName);
      final Category category = new Category(categoryName, icon, classLoader);
      categories.add(category);
    }
    return categories;
  }

  private String getPropertyValue(final Class<? extends Connector> connectorClass, final String property) {
    try {
      final ResourceBundle bundle = ResourceBundle.getBundle(connectorClass.getName(), currentLocale,
          connectorClass.getClassLoader());
      return bundle.getString(property);
    } catch (final Exception e) {
      return null;
    }
  }

  private boolean isAnInstanceOfConnector(final Class<?> c) {
    if (Modifier.isAbstract(c.getModifiers()) || c.equals(Connector.class)) {
      return false;
    }
    return isAConnector(c);
  }

  private boolean isAssignable(final Class<?> clazz, final Class<?> assignableClass) {
    if (assignableClass.equals(clazz)) {
      return true;
    }
    final Class<?> superClass = clazz.getSuperclass();
    if (superClass != null) {
      return isAssignable(superClass, assignableClass);
    }
    return false;
  }

  private boolean isAConnector(final Class<?> c) {
    return isAssignable(c, Connector.class);
  }

  private boolean isMapper(final Class<?> clazz) {
    return isAssignable(clazz, Mapper.class);
  }

  private boolean isFilter(final Class<?> clazz) {
    return isAssignable(clazz, Filter.class);
  }

  private boolean isRoleResolver(final Class<?> clazz) {
    return isAssignable(clazz, RoleResolver.class);
  }

  private boolean isMultiInstantiator(final Class<?> clazz) {
    return isAssignable(clazz, MultiInstantiator.class);
  }

  private boolean isInstantiator(final Class<?> clazz) {
    return isAssignable(clazz, MultipleInstancesInstantiator.class);
  }

  public void setCurrentLocale(final Locale currentLocale) {
    this.currentLocale = currentLocale;
  }

  public Locale getCurrentLocale() {
    return currentLocale;
  }

  public Collection<ConnectorException> getExcpetions() {
    return exceptions;
  }

}
