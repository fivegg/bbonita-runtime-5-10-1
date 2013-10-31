/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.ow2.bonita.env.descriptor;

import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.hibernate.JDBCException;
import org.hibernate.cfg.Configuration;
import org.hibernate.connection.ConnectionProvider;
import org.hibernate.connection.ConnectionProviderFactory;
import org.hibernate.dialect.Dialect;
import org.hibernate.tool.hbm2ddl.DatabaseMetadata;
import org.hibernate.util.JDBCExceptionReporter;
import org.ow2.bonita.env.WireContext;
import org.ow2.bonita.env.WireDefinition;
import org.ow2.bonita.env.WireException;
import org.ow2.bonita.env.operation.Operation;
import org.ow2.bonita.util.BonitaRuntimeException;
import org.ow2.bonita.util.ExceptionManager;
import org.ow2.bonita.util.ReflectUtil;

/**
 * @author Tom Baeyens
 */
public class HibernateConfigurationDescriptor extends AbstractDescriptor {

  private static final long serialVersionUID = 1L;

  private static final Logger LOG = Logger.getLogger(HibernateConfigurationDescriptor.class.getName());

  String className;
  String namingStrategyClassName;
  List<Operation> mappingOperations;
  List<Operation> cacheOperations;
  PropertiesDescriptor propertiesDescriptor;
  private Operation schemaOperation;

  @Override
  public Object construct(final WireContext wireContext) {
    // instantiation of the configuration
    Configuration configuration = null;
    if (className != null) {
      final ClassLoader classLoader = wireContext.getClassLoader();
      if (LOG.isLoggable(Level.FINE)) {
        LOG.fine("instantiating hibernate configation class " + className);
      }
      final Class<?> configurationClass = ReflectUtil.loadClass(classLoader, className);
      configuration = (Configuration) ReflectUtil.newInstance(configurationClass);
    } else {
      if (LOG.isLoggable(Level.FINE)) {
        LOG.fine("instantiating default hibernate configation");
      }
      configuration = new Configuration();
    }
    return configuration;
  }

  @Override
  public void initialize(final Object object, final WireContext wireContext) {
    final Configuration configuration = (Configuration) object;
    apply(mappingOperations, configuration, wireContext);
    apply(cacheOperations, configuration, wireContext);
    if (propertiesDescriptor != null) {
      final Properties properties = (Properties) wireContext.create(propertiesDescriptor, false);
      if (LOG.isLoggable(Level.FINE)) {
        LOG.fine("adding properties to hibernate configuration: " + properties);
      }
      configuration.addProperties(properties);
    }
    if (schemaOperation != null) {
      schemaOperation.apply(configuration, wireContext);
    }
  }

  private void apply(final List<Operation> operations, final Configuration configuration, final WireContext wireContext) {
    if (operations != null) {
      for (final Operation operation : operations) {
        if (LOG.isLoggable(Level.FINE)) {
          LOG.fine(operation.toString());
        }
        operation.apply(configuration, wireContext);
      }
    }
  }

  @Override
  public Class<?> getType(final WireDefinition wireDefinition) {
    if (className != null) {
      try {
        return ReflectUtil.loadClass(wireDefinition.getClassLoader(), className);
      } catch (final BonitaRuntimeException e) {
        final String message = ExceptionManager.getInstance().getFullMessage("bp_HCD_1", className, e.getMessage());
        throw new WireException(message, e.getCause());
      }
    }
    return Configuration.class;
  }

  public void addMappingOperation(final Operation operation) {
    if (mappingOperations == null) {
      mappingOperations = new ArrayList<Operation>();
    }
    mappingOperations.add(operation);
  }

  public void addCacheOperation(final Operation operation) {
    if (cacheOperations == null) {
      cacheOperations = new ArrayList<Operation>();
    }
    cacheOperations.add(operation);
  }

  // operations ///////////////////////////////////////////////////////////////

  public static class AddResource implements Operation {
    private static final long serialVersionUID = 1L;
    String resource;

    public AddResource(final String resource) {
      this.resource = resource;
    }

    @Override
    public void apply(final Object target, final WireContext wireContext) {
      final Configuration configuration = (Configuration) target;
      configuration.addResource(resource, wireContext.getClassLoader());
    }

    @Override
    public String toString() {
      return "adding mapping resource " + resource + " to hibernate configuration";
    }
  }

  public static class AddFile implements Operation {
    private static final long serialVersionUID = 1L;
    String fileName;

    public AddFile(final String fileName) {
      this.fileName = fileName;
    }

    @Override
    public void apply(final Object target, final WireContext wireContext) {
      final Configuration configuration = (Configuration) target;
      configuration.addFile(fileName);
    }

    @Override
    public String toString() {
      return "adding hibernate mapping file " + fileName + " to configuration";
    }
  }

  public static class AddClass implements Operation {
    private static final long serialVersionUID = 1L;
    String className;

    public AddClass(final String className) {
      this.className = className;
    }

    @Override
    public void apply(final Object target, final WireContext wireContext) {
      final Configuration configuration = (Configuration) target;
      try {
        final Class<?> persistentClass = wireContext.getClassLoader().loadClass(className);
        configuration.addClass(persistentClass);
      } catch (final Exception e) {
        final String message = ExceptionManager.getInstance().getFullMessage("bp_HCD_2", className);
        throw new BonitaRuntimeException(message, e);
      }
    }

    @Override
    public String toString() {
      return "adding persistent class " + className + " to hibernate configuration";
    }
  }

  public static class AddUrl implements Operation {
    private static final long serialVersionUID = 1L;
    String url;

    public AddUrl(final String url) {
      this.url = url;
    }

    @Override
    public void apply(final Object target, final WireContext wireContext) {
      final Configuration configuration = (Configuration) target;
      try {
        configuration.addURL(new URL(url));
      } catch (final Exception e) {
        final String message = ExceptionManager.getInstance().getFullMessage("bp_HCD_3", url);
        throw new BonitaRuntimeException(message, e);
      }
    }
  }

  public static class SetCacheConcurrencyStrategy implements Operation {
    private static final long serialVersionUID = 1L;
    String className;
    String concurrencyStrategy;

    public SetCacheConcurrencyStrategy(final String className, final String concurrencyStrategy) {
      this.className = className;
      this.concurrencyStrategy = concurrencyStrategy;
    }

    @Override
    public void apply(final Object target, final WireContext wireContext) {
      final Configuration configuration = (Configuration) target;
      configuration.setCacheConcurrencyStrategy(className, concurrencyStrategy);
    }

    @Override
    public String toString() {
      return "setting cache concurrency strategy on class " + className + " to " + concurrencyStrategy
          + " on hibernate configuration";
    }
  }

  public static class SetCollectionCacheConcurrencyStrategy implements Operation {
    private static final long serialVersionUID = 1L;
    String collection;
    String concurrencyStrategy;

    public SetCollectionCacheConcurrencyStrategy(final String collection, final String concurrencyStrategy) {
      this.collection = collection;
      this.concurrencyStrategy = concurrencyStrategy;
    }

    @Override
    public void apply(final Object target, final WireContext wireContext) {
      final Configuration configuration = (Configuration) target;
      configuration.setCollectionCacheConcurrencyStrategy(collection, concurrencyStrategy);
    }

    @Override
    public String toString() {
      return "setting cache concurrency strategy on collection " + collection + " to " + concurrencyStrategy
          + " on hibernate configuration";
    }
  }

  public static final class CreateSchema implements Operation {

    private static final long serialVersionUID = 1L;

    /** The sole instance of this class */
    private static final Operation INSTANCE = new CreateSchema();

    private CreateSchema() {
      // suppress default constructor, ensuring non-instantiability
    }

    @Override
    public void apply(final Object target, final WireContext wireContext) {
      final Configuration configuration = (Configuration) target;
      final Properties cfgProperties = configuration.getProperties();
      final Dialect dialect = Dialect.getDialect(cfgProperties);
      final ConnectionProvider connectionProvider = ConnectionProviderFactory.newConnectionProvider(cfgProperties);
      try {
        final Connection connection = connectionProvider.getConnection();
        try {
          if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("dropping db schema");
          }
          final String[] dropScript = configuration.generateDropSchemaScript(dialect);
          executeScript(connection, dropScript);
          if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("creating db schema");
          }
          final String[] createScript = configuration.generateSchemaCreationScript(dialect);
          executeScript(connection, createScript);
        } finally {
          connectionProvider.closeConnection(connection);
        }
      } catch (final SQLException e) {
        final String message = ExceptionManager.getInstance().getFullMessage("bp_HCD_4");
        throw new JDBCException(message, e);
      } finally {
        connectionProvider.close();
      }
    }

    /** Returns the sole instance of this class */
    public static Operation getInstance() {
      return INSTANCE;
    }
  }

  public static final class UpdateSchema implements Operation {

    private static final long serialVersionUID = 1L;

    private static final Operation INSTANCE = new UpdateSchema();

    private UpdateSchema() {
      // suppress default constructor, ensuring non-instantiability
    }

    @Override
    public void apply(final Object target, final WireContext wireContext) {
      final Configuration configuration = (Configuration) target;
      final Properties cfgProperties = configuration.getProperties();
      final Dialect dialect = Dialect.getDialect(cfgProperties);
      final ConnectionProvider connectionProvider = ConnectionProviderFactory.newConnectionProvider(cfgProperties);
      try {
        final Connection connection = connectionProvider.getConnection();
        try {
          final DatabaseMetadata metadata = new DatabaseMetadata(connection, dialect);
          final String[] updateScript = configuration.generateSchemaUpdateScript(dialect, metadata);
          if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("updating db schema");
          }
          executeScript(connection, updateScript);
        } finally {
          connectionProvider.closeConnection(connection);
        }
      } catch (final SQLException e) {
        final String message = ExceptionManager.getInstance().getFullMessage("bp_HCD_5");
        throw new JDBCException(message, e);
      } finally {
        connectionProvider.close();
      }
    }

    public static Operation getInstance() {
      return INSTANCE;
    }
  }

  private static List<SQLException> executeScript(final Connection connection, final String[] script)
      throws SQLException {
    List<SQLException> exceptions = Collections.emptyList();
    final Statement statement = connection.createStatement();
    try {
      for (final String line : script) {
        if (LOG.isLoggable(Level.FINE)) {
          LOG.fine(line);
        }
        try {
          statement.executeUpdate(line);
          if (statement.getWarnings() != null) {
            JDBCExceptionReporter.logAndClearWarnings(connection);
          }
        } catch (final SQLException e) {
          if (exceptions.isEmpty()) {
            exceptions = new ArrayList<SQLException>();
          }
          exceptions.add(e);
        }
      }
    } finally {
      statement.close();
    }
    return exceptions;
  }

  // getters and setters //////////////////////////////////////////////////////

  public String getClassName() {
    return className;
  }

  public void setClassName(final String className) {
    this.className = className;
  }

  public PropertiesDescriptor getPropertiesDescriptor() {
    return propertiesDescriptor;
  }

  public void setPropertiesDescriptor(final PropertiesDescriptor propertiesDescriptor) {
    this.propertiesDescriptor = propertiesDescriptor;
  }

  public String getNamingStrategyClassName() {
    return namingStrategyClassName;
  }

  public void setNamingStrategyClassName(final String namingStrategyClassName) {
    this.namingStrategyClassName = namingStrategyClassName;
  }

  public Operation getSchemaOperation() {
    return schemaOperation;
  }

  public void setSchemaOperation(final Operation schemaOperation) {
    this.schemaOperation = schemaOperation;
  }
}
