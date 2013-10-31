/**
 * Copyright (C) 2006 Bull S. A. S.
 * Bull, Rue Jean Jaures, B.P.68, 78340, Les Clayes-sous-Bois
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 * 
 * Modified by Charles Souillard, Matthieu Chaffotte, Nicolas Chabanoles, Elias Ricken de Medeiros -
 * BonitaSoft S.A.
 **/
package org.ow2.bonita.persistence.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.collections.CollectionUtils;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.util.Version;
import org.hibernate.ConnectionReleaseMode;
import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.search.FullTextQuery;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.hibernate.transform.Transformers;
import org.hibernate.type.Type;
import org.ow2.bonita.facade.def.InternalActivityDefinition;
import org.ow2.bonita.facade.def.InternalProcessDefinition;
import org.ow2.bonita.facade.def.element.impl.MetaDataImpl;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition.ProcessState;
import org.ow2.bonita.facade.exception.BonitaInternalException;
import org.ow2.bonita.facade.identity.impl.GroupImpl;
import org.ow2.bonita.facade.identity.impl.MembershipImpl;
import org.ow2.bonita.facade.identity.impl.ProfileMetadataImpl;
import org.ow2.bonita.facade.identity.impl.RoleImpl;
import org.ow2.bonita.facade.identity.impl.UserImpl;
import org.ow2.bonita.facade.paging.ActivityInstanceCriterion;
import org.ow2.bonita.facade.paging.GroupCriterion;
import org.ow2.bonita.facade.paging.ProcessDefinitionCriterion;
import org.ow2.bonita.facade.paging.ProcessInstanceCriterion;
import org.ow2.bonita.facade.paging.RoleCriterion;
import org.ow2.bonita.facade.paging.UserCriterion;
import org.ow2.bonita.facade.privilege.Rule;
import org.ow2.bonita.facade.privilege.Rule.RuleType;
import org.ow2.bonita.facade.privilege.RuleTypePolicy;
import org.ow2.bonita.facade.runtime.ActivityState;
import org.ow2.bonita.facade.runtime.Category;
import org.ow2.bonita.facade.runtime.Comment;
import org.ow2.bonita.facade.runtime.InstanceState;
import org.ow2.bonita.facade.runtime.TaskInstance;
import org.ow2.bonita.facade.runtime.WebTemporaryToken;
import org.ow2.bonita.facade.runtime.impl.CaseImpl;
import org.ow2.bonita.facade.runtime.impl.CategoryImpl;
import org.ow2.bonita.facade.runtime.impl.InternalActivityInstance;
import org.ow2.bonita.facade.runtime.impl.InternalProcessInstance;
import org.ow2.bonita.facade.runtime.impl.LabelImpl;
import org.ow2.bonita.facade.uuid.ActivityDefinitionUUID;
import org.ow2.bonita.facade.uuid.ActivityInstanceUUID;
import org.ow2.bonita.facade.uuid.CategoryUUID;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.persistence.DocumentDbSession;
import org.ow2.bonita.persistence.EventDbSession;
import org.ow2.bonita.persistence.IdentityDbSession;
import org.ow2.bonita.persistence.JournalDbSession;
import org.ow2.bonita.persistence.PrivilegeDbSession;
import org.ow2.bonita.persistence.QuerierDbSession;
import org.ow2.bonita.persistence.WebDbSession;
import org.ow2.bonita.persistence.WebTokenManagementDbSession;
import org.ow2.bonita.runtime.event.EventCouple;
import org.ow2.bonita.runtime.event.IncomingEventInstance;
import org.ow2.bonita.runtime.event.Job;
import org.ow2.bonita.runtime.event.JobLock;
import org.ow2.bonita.runtime.event.OutgoingEventInstance;
import org.ow2.bonita.runtime.model.Execution;
import org.ow2.bonita.search.DocumentCriterion;
import org.ow2.bonita.search.DocumentSearchBuilder;
import org.ow2.bonita.search.SearchQueryBuilder;
import org.ow2.bonita.search.SearchUtil;
import org.ow2.bonita.search.index.DocumentIndex;
import org.ow2.bonita.services.Document;
import org.ow2.bonita.services.DocumentContent;
import org.ow2.bonita.services.DocumentDescriptor;
import org.ow2.bonita.services.DocumentDescriptorMapping;
import org.ow2.bonita.services.DocumentVersion;
import org.ow2.bonita.services.impl.DocumentContentImpl;
import org.ow2.bonita.services.impl.DocumentDescriptorImpl;
import org.ow2.bonita.services.impl.DocumentVersionImpl;
import org.ow2.bonita.util.BonitaConstants;
import org.ow2.bonita.util.BonitaRuntimeException;
import org.ow2.bonita.util.DateUtil;
import org.ow2.bonita.util.Misc;
import org.ow2.bonita.util.ProcessInstanceEndedDateComparatorAsc;
import org.ow2.bonita.util.ProcessInstanceEndedDateComparatorDesc;
import org.ow2.bonita.util.ProcessInstanceLastUpdateComparator;
import org.ow2.bonita.util.ProcessInstanceLastUpdateComparatorAsc;
import org.ow2.bonita.util.ProcessInstanceNbComparatorAsc;
import org.ow2.bonita.util.ProcessInstanceNbComparatorDesc;
import org.ow2.bonita.util.ProcessInstanceStartedDateComparatorAsc;
import org.ow2.bonita.util.ProcessInstanceStartedDateComparatorDesc;
import org.ow2.bonita.util.ProcessInstanceUUIDComparatorAsc;
import org.ow2.bonita.util.ProcessInstanceUUIDComparatorDesc;
import org.ow2.bonita.util.hibernate.GenericEnumUserType;

@SuppressWarnings("deprecation")
public class DbSessionImpl extends HibernateDbSession implements QuerierDbSession, JournalDbSession, WebDbSession,
    IdentityDbSession, EventDbSession, PrivilegeDbSession, WebTokenManagementDbSession, DocumentDbSession {

  private static final Logger LOG = Logger.getLogger(DbSessionImpl.class.getName());

  private static final int MAX_LOOP = 100;

  private static final long PAUSE_TIME_MILLIS = 100L;

  private static final String METADATA_TABLE = "BN_METADATA";

  private static final String EXECUTION_TABLE = "BN_PVM_EXEC";

  public DbSessionImpl(final Session session) {
    super();
    setSession(session);
  }

  /*
   * QUERIER
   */
  private static final Type DEFINITION_STATE_USER_TYPE = Hibernate.custom(GenericEnumUserType.class,
      new String[] { "enumClass" }, new String[] { ProcessState.class.getName() });

  private static final Type ACTIVITY_STATE_USER_TYPE = Hibernate.custom(GenericEnumUserType.class,
      new String[] { "enumClass" }, new String[] { ActivityState.class.getName() });

  private static final Type INSTANCE_STATE_USER_TYPE = Hibernate.custom(GenericEnumUserType.class,
      new String[] { "enumClass" }, new String[] { InstanceState.class.getName() });

  protected static final Version LUCENE_VERSION = SearchUtil.LUCENE_VERSION;

  private static String getInsertMetadataStatement(final String schemaPrefix) {
    return "insert into " + schemaPrefix + METADATA_TABLE + " (key_, value_) values (?,'0')";
  }

  private static String getSelectMetadataStatement(final String schemaPrefix) {
    return "select value_ from " + schemaPrefix + METADATA_TABLE + " where key_=?";
  }

  private static String getLockMetadataStatement(final String schemaPrefix) {
    return "update " + schemaPrefix + METADATA_TABLE + " set value_ = value_ where key_=?";
  }

  private static String getUpdateMetadata(final String schemaPrefix) {
    return "update " + schemaPrefix + METADATA_TABLE + " set value_ = ? where key_=?";
  }

  private static String getRemoveMetadataStatement(final String schemaPrefix) {
    return "delete from " + schemaPrefix + METADATA_TABLE + " where key_=?";
  }

  private static String getRemoveExecution(final String schemaPrefix) {
    return "delete from " + schemaPrefix + EXECUTION_TABLE + " where dbid_=?";
  }

  @Override
  public long getLockedMetadata(final String key) {
    Connection connection = null;
    try {
      connection = getConnection();
      return getMetadata(connection, key);
    } catch (final SQLException e) {
      // FIXME find a more appropriate exception type
      throw new BonitaInternalException("Unexpected DB access exception: " + e, e);
    } finally {
      releaseConnection(connection);
    }
  }

  @Override
  public void lockMetadata(final String key) {
    Connection connection = null;
    try {
      connection = getConnection();
      final boolean locked = lockMetadata(connection, key);
      if (!locked) {
        createMetadata(connection, key);
      }
    } catch (final SQLException e) {
      // FIXME find a more appropriate exception type
      throw new BonitaInternalException("Unexpected DB access exception: " + e, e);
    } finally {
      releaseConnection(connection);
    }
  }

  @Override
  public void updateLockedMetadata(final String key, final long value) {
    Connection connection = null;
    try {
      connection = getConnection();
      updateMetadata(connection, key, value);
    } catch (final SQLException e) {
      // FIXME find a more appropriate exception type
      throw new BonitaInternalException("Unexpected DB access exception: " + e, e);
    } finally {
      releaseConnection(connection);
    }
  }

  @Override
  public void removeLockedMetadata(final String key) {
    Connection connection = null;
    try {
      connection = getConnection();
      removeMetadata(connection, key);
    } catch (final SQLException e) {
      // FIXME find a more appropriate exception type
      throw new BonitaInternalException("Unexpected DB access exception: " + e, e);
    } finally {
      releaseConnection(connection);
    }
  }

  private void releaseConnection(final Connection connection) {
    if (connection != null && shouldReleaseConnections()) {
      try {
        connection.close();
      } catch (final SQLException e) {
        if (LOG.isLoggable(Level.FINE)) {
          LOG.fine("SQLException during connection close: " + e);
        }
      }
    }
  }

  private boolean shouldReleaseConnections() {
    return ((SessionFactoryImplementor) session.getSessionFactory()).getSettings().getConnectionReleaseMode() == ConnectionReleaseMode.AFTER_STATEMENT;
  }

  private Connection getConnection() {
    return session.connection();
  }

  private long getMetadata(final Connection connection, final String key) throws SQLException {
    final String schemaPrefix = getSchemaPrefix();
    final PreparedStatement selectStmt = connection.prepareStatement(getSelectMetadataStatement(schemaPrefix));
    try {
      selectStmt.setString(1, key);
      final ResultSet resultSet = selectStmt.executeQuery();
      try {
        if (resultSet.next()) {
          final String value = resultSet.getString(1);
          return Long.valueOf(value);
        } else {
          throw new IllegalStateException("value not found for key=" + key);
        }
      } finally {
        resultSet.close();
      }
    } finally {
      selectStmt.close();
    }
  }

  private String getSchemaPrefix() throws SQLException {
    String schemaPrefix = "";
    final String defaultSchema = ((SessionFactoryImplementor) session.getSessionFactory()).getSettings()
        .getDefaultSchemaName();
    if (defaultSchema != null && !defaultSchema.trim().isEmpty()) {
      schemaPrefix = defaultSchema + ".";
    }
    return schemaPrefix;
  }

  private void updateMetadata(final Connection connection, final String key, final long nb) throws SQLException {
    final String schemaName = getSchemaPrefix();
    final PreparedStatement updateStmt = connection.prepareStatement(getUpdateMetadata(schemaName));
    try {
      updateStmt.setString(1, String.valueOf(nb));
      updateStmt.setString(2, key);
      updateStmt.executeUpdate();
    } finally {
      updateStmt.close();
    }
  }

  @Override
  public void deleteExecution(final long id) {
    Connection connection = null;
    try {
      connection = getConnection();
      final String schemaName = getSchemaPrefix();
      final PreparedStatement updateStmt = connection.prepareStatement(getRemoveExecution(schemaName));
      try {
        updateStmt.setLong(1, id);
        updateStmt.executeUpdate();
      } finally {
        updateStmt.close();
      }
    } catch (final SQLException e) {
      // FIXME find a more appropriate exception type
      throw new BonitaInternalException("Unexpected DB access exception: " + e, e);
    } finally {
      releaseConnection(connection);
    }
  }

  private void removeMetadata(final Connection connection, final String key) throws SQLException {
    final String schemaName = getSchemaPrefix();
    final PreparedStatement updateStmt = connection.prepareStatement(getRemoveMetadataStatement(schemaName));
    try {
      updateStmt.setString(1, key);
      updateStmt.executeUpdate();
    } finally {
      updateStmt.close();
    }
  }

  private void createMetadata(final Connection connection, final String key) throws SQLException {
    int loops = 0;
    final String schemaName = getSchemaPrefix();
    do {
      final PreparedStatement insertStmt = connection.prepareStatement(getInsertMetadataStatement(schemaName));
      try {
        insertStmt.setString(1, key);
        insertStmt.executeUpdate();
        return;
      } catch (final SQLException e) {
        if (isConstraintViolation(e)) {
          try {
            Thread.sleep(PAUSE_TIME_MILLIS);
          } catch (final InterruptedException e1) {
            LOG.log(Level.FINE, "interrupted");
          }
          // another process has inserted or is inserting at the same time
          final boolean locked = lockMetadata(connection, key);
          if (locked) {
            return;
          }
        } else {
          throw e;
        }
      } finally {
        insertStmt.close();
      }
      loops++;
    } while (loops < MAX_LOOP);
    throw new IllegalStateException(" Could not create or lock value for key=" + key + ". Giving up after " + loops
        + " iterations.");
  }

  private boolean lockMetadata(final Connection connection, final String key) throws SQLException {
    final String schemaName = getSchemaPrefix();
    final PreparedStatement updateStmt = connection.prepareStatement(getLockMetadataStatement(schemaName));
    try {
      updateStmt.setString(1, key);
      final int updateCount = updateStmt.executeUpdate();
      return updateCount > 0;
    } finally {
      updateStmt.close();
    }
  }

  private boolean isConstraintViolation(final SQLException e) {
    final String sqlState = getSqlState(e);
    boolean isConstraintViolation = false;
    if (sqlState != null && sqlState.length() >= 2) {
      final String classCode = sqlState.substring(0, 2);
      if ("23".equals(classCode)) {
        isConstraintViolation = true;
      }
    }
    return isConstraintViolation;
  }

  private String getSqlState(final SQLException e) {
    String sqlState = e.getSQLState();
    if (sqlState == null) {
      if (e.getCause() != null) {
        final SQLException nextException = e.getNextException();
        if (nextException != null) {
          sqlState = nextException.getSQLState();
        }
      }
    }
    return sqlState;
  }

  @Override
  public int getNumberOfParentProcessInstances() {
    final Query query = getSession().getNamedQuery("getNumberOfParentProcessInstances");

    return ((Long) query.uniqueResult()).intValue();
  }

  @Override
  public int getNumberOfProcessInstances() {
    final Query query = getSession().getNamedQuery("getNumberOfProcessInstances");

    return ((Long) query.uniqueResult()).intValue();
  }

  @Override
  public int getNumberOfProcesses() {
    final Query query = getSession().getNamedQuery("getNumberOfProcesses");

    return ((Long) query.uniqueResult()).intValue();
  }

  @Override
  public Set<TaskInstance> getUserInstanceTasks(final String userId, final ProcessInstanceUUID instanceUUID,
      final ActivityState taskState) {
    final Set<TaskInstance> result = new HashSet<TaskInstance>();
    final Query query = getSession().getNamedQuery("getUserInstanceTasksWithState");

    query.setString("userId", userId);
    query.setString("instanceUUID", instanceUUID.toString());
    query.setParameter("state", taskState, ACTIVITY_STATE_USER_TYPE);
    CollectionUtils.addAll(result, query.iterate());
    return result;
  }

  @Override
  public Set<TaskInstance> getUserTasks(final String userId, final Collection<ActivityState> taskStates) {
    final Query query = getSession().getNamedQuery("getUserTasksWithStates");

    query.setString("userId", userId);
    query.setParameterList("states", taskStates, ACTIVITY_STATE_USER_TYPE);
    final Set<TaskInstance> result = new HashSet<TaskInstance>();
    CollectionUtils.addAll(result, query.iterate());
    return result;
  }

  @Override
  public Set<InternalActivityInstance> getActivityInstances(final ProcessInstanceUUID instanceUUID,
      final String activityName) {
    final Query query = getSession().getNamedQuery("getActivityInstancesWithName");

    query.setString("instanceUUID", instanceUUID.getValue());
    query.setString("name", activityName);
    final Set<InternalActivityInstance> result = new HashSet<InternalActivityInstance>();
    CollectionUtils.addAll(result, query.iterate());
    return result;
  }

  @Override
  public Set<InternalActivityInstance> getActivityInstances(final ProcessInstanceUUID instanceUUID,
      final String activityName, final String iterationId) {
    final Query query = getSession().getNamedQuery("getActivityInstances");

    query.setString("instanceUUID", instanceUUID.getValue());
    query.setString("activityName", activityName);
    query.setString("iterationId", iterationId);
    final Set<InternalActivityInstance> result = new HashSet<InternalActivityInstance>();
    CollectionUtils.addAll(result, query.iterate());
    return result;
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<InternalActivityInstance> getActivityInstancesFromRoot(final ProcessInstanceUUID rootInstanceUUID) {
    if (rootInstanceUUID == null) {
      return Collections.emptyList();
    }
    final Query query = getSession().getNamedQuery("getActivityInstancesFromRoot");

    query.setString("rootInstanceUUID", rootInstanceUUID.getValue());
    return query.list();
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<InternalActivityInstance> getActivityInstancesFromRoot(final Set<ProcessInstanceUUID> rootInstanceUUIDs) {
    if (rootInstanceUUIDs == null || rootInstanceUUIDs.isEmpty()) {
      return Collections.emptyList();
    }
    final Collection<String> uuids = new HashSet<String>();
    for (final ProcessInstanceUUID processInstanceUUID : rootInstanceUUIDs) {
      uuids.add(processInstanceUUID.getValue());
    }

    final Query query = getSession().getNamedQuery("getMatchingActivityInstancesFromRoot");

    query.setParameterList("rootInstanceUUIDs", uuids);
    return query.list();
  }

  @Override
  @SuppressWarnings("unchecked")
  public Map<ProcessInstanceUUID, InternalActivityInstance> getLastUpdatedActivityInstanceFromRoot(
      final Set<ProcessInstanceUUID> rootInstanceUUIDs, final boolean considerSystemTaks) {
    if (rootInstanceUUIDs == null || rootInstanceUUIDs.isEmpty()) {
      return Collections.emptyMap();
    }

    final Map<ProcessInstanceUUID, InternalActivityInstance> result = new HashMap<ProcessInstanceUUID, InternalActivityInstance>();
    Query query;
    for (final ProcessInstanceUUID processInstanceUUID : rootInstanceUUIDs) {
      if (considerSystemTaks) {
        query = getSession().getNamedQuery("getMatchingActivityInstancesFromRoot");
      } else {
        query = getSession().getNamedQuery("getMatchingHumanTaskInstancesFromRoot");
      }

      query.setParameterList("rootInstanceUUIDs", Arrays.asList(processInstanceUUID.getValue()));
      query.setMaxResults(1);

      final List<InternalActivityInstance> tmp = query.list();
      if (tmp != null && !tmp.isEmpty()) {
        result.put(processInstanceUUID, tmp.get(0));
      }
    }
    return result;
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<InternalActivityInstance> getActivityInstancesFromRoot(final Set<ProcessInstanceUUID> rootInstanceUUIDs,
      final ActivityState state) {
    if (rootInstanceUUIDs == null || rootInstanceUUIDs.isEmpty()) {
      return Collections.emptyList();
    }
    final Collection<String> uuids = new HashSet<String>();
    for (final ProcessInstanceUUID processInstanceUUID : rootInstanceUUIDs) {
      uuids.add(processInstanceUUID.getValue());
    }

    final Query query = getSession().getNamedQuery("getMatchingActivityInstancesWithStateFromRoot");

    query.setParameterList("rootInstanceUUIDs", uuids);
    query.setParameter("state", state, ACTIVITY_STATE_USER_TYPE);
    return query.list();
  }

  @Override
  public TaskInstance getOneTask(final String userId, final ActivityState taskState) {
    final Query query = getSession().getNamedQuery("getOneTask");

    query.setString("userId", userId);
    query.setParameter("state", taskState, ACTIVITY_STATE_USER_TYPE);
    query.setMaxResults(1);
    return (TaskInstance) query.uniqueResult();
  }

  @Override
  public TaskInstance getOneTask(final String userId, final ProcessDefinitionUUID processUUID,
      final ActivityState taskState) {
    final Query query = getSession().getNamedQuery("getOneTaskOfProcess");

    query.setString("userId", userId);
    query.setString("processUUID", processUUID.getValue());
    query.setParameter("state", taskState, ACTIVITY_STATE_USER_TYPE);
    query.setMaxResults(1);
    return (TaskInstance) query.uniqueResult();
  }

  @Override
  public TaskInstance getOneTask(final String userId, final ProcessInstanceUUID instanceUUID,
      final ActivityState taskState) {
    final Query query = getSession().getNamedQuery("getOneTaskOfInstance");

    query.setString("userId", userId);
    query.setString("instanceUUID", instanceUUID.getValue());
    query.setParameter("state", taskState, ACTIVITY_STATE_USER_TYPE);
    query.setMaxResults(1);
    return (TaskInstance) query.uniqueResult();
  }

  @Override
  public Set<InternalProcessInstance> getUserInstances(final String userId) {
    final Query query = getSession().getNamedQuery("getUserInstances");

    query.setString("userId", userId);
    final Set<InternalProcessInstance> result = new HashSet<InternalProcessInstance>();
    CollectionUtils.addAll(result, query.iterate());
    return result;
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<InternalProcessInstance> getParentUserInstances(final String userId, final int fromIndex,
      final int pageSize) {
    final Query query = getSession().getNamedQuery("getParentUserInstancesPage");

    query.setFirstResult(fromIndex);
    query.setMaxResults(pageSize);
    query.setString("userId", userId);
    final List<InternalProcessInstance> results = query.list();
    if (results == null) {
      return Collections.emptyList();
    }
    return results;

  }

  @Override
  @SuppressWarnings("unchecked")
  public List<InternalProcessInstance> getParentUserInstances(final String userId, final int startingIndex,
      final int pageSize, final ProcessInstanceCriterion pagingCriterion) {
    Query query = null;
    switch (pagingCriterion) {
      case LAST_UPDATE_ASC:
        query = getSession().getNamedQuery("getParentUserInstancesPageOrderByLastUpdateAsc");
        break;
      case STARTED_DATE_ASC:
        query = getSession().getNamedQuery("getParentUserInstancesPageOrderByStartedDateAsc");
        break;
      case ENDED_DATE_ASC:
        query = getSession().getNamedQuery("getParentUserInstancesPageOrderByEndedDateAsc");
        break;
      case INSTANCE_NUMBER_ASC:
        query = getSession().getNamedQuery("getParentUserInstancesPageOrderByInstanceNumberAsc");
        break;
      case INSTANCE_UUID_ASC:
        query = getSession().getNamedQuery("getParentUserInstancesPageOrderByInstanceUUIDAsc");
        break;
      case LAST_UPDATE_DESC:
        query = getSession().getNamedQuery("getParentUserInstancesPageOrderByLastUpdateDesc");
        break;
      case STARTED_DATE_DESC:
        query = getSession().getNamedQuery("getParentUserInstancesPageOrderByStartedDateDesc");
        break;
      case ENDED_DATE_DESC:
        query = getSession().getNamedQuery("getParentUserInstancesPageOrderByEndedDateDesc");
        break;
      case INSTANCE_NUMBER_DESC:
        query = getSession().getNamedQuery("getParentUserInstancesPageOrderByInstanceNumberDesc");
        break;
      case INSTANCE_UUID_DESC:
        query = getSession().getNamedQuery("getParentUserInstancesPageOrderByInstanceUUIDDesc");
        break;
      case DEFAULT:
        query = getSession().getNamedQuery("getParentUserInstancesPage");
        break;
    }

    query.setFirstResult(startingIndex);
    query.setMaxResults(pageSize);
    query.setString("userId", userId);
    final List<InternalProcessInstance> results = query.list();
    if (results == null) {
      return Collections.emptyList();
    }
    return results;
  }

  @Override
  public Set<InternalProcessInstance> getUserInstances(final String userId, final Date minStartDate) {
    final Query query = getSession().getNamedQuery("getUserInstancesAfterDate");

    query.setString("userId", userId);
    query.setLong("minStartDate", minStartDate.getTime());
    final Set<InternalProcessInstance> result = new HashSet<InternalProcessInstance>();
    CollectionUtils.addAll(result, query.iterate());
    return result;
  }

  @Override
  public Set<InternalProcessInstance> getUserParentInstances(final String userId, final Date minStartDate) {
    final Query query = getSession().getNamedQuery("getUserParentInstancesAfterDate");

    query.setString("userId", userId);
    query.setLong("minStartDate", minStartDate.getTime());
    final Set<InternalProcessInstance> result = new HashSet<InternalProcessInstance>();
    CollectionUtils.addAll(result, query.iterate());
    return result;
  }

  @Override
  public Set<InternalProcessInstance> getUserInstancesExcept(final String userId,
      final Set<ProcessInstanceUUID> instances) {
    if (instances == null || instances.isEmpty()) {
      return getUserInstances(userId);
    }
    final Collection<String> uuids = new HashSet<String>();
    for (final ProcessInstanceUUID processInstanceUUID : instances) {
      uuids.add(processInstanceUUID.getValue());
    }
    final Query query = getSession().getNamedQuery("getUserInstancesExcept");

    query.setString("userId", userId);
    return executeSplittedQuery(InternalProcessInstance.class, query, "uuids", uuids);
  }

  @Override
  @SuppressWarnings("unchecked")
  public Set<InternalActivityInstance> getActivityInstances(final ProcessInstanceUUID instanceUUID) {
    final Query query = getSession().getNamedQuery("findActivityInstances");

    query.setString("instanceUUID", instanceUUID.toString());
    final List<InternalActivityInstance> results = query.list();
    if (results != null) {
      return new HashSet<InternalActivityInstance>(results);
    }
    return null;
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<InternalActivityInstance> getActivityInstances(final ProcessInstanceUUID instanceUUID,
      final int fromIndex, final int pageSize, final ActivityInstanceCriterion pagingCriterion) {
    Query query = null;

    switch (pagingCriterion) {
      case LAST_UPDATE_ASC:
        query = getSession().getNamedQuery("findActivityInstancesOrderByLastUpdateAsc");
        break;
      case STARTED_DATE_ASC:
        query = getSession().getNamedQuery("findActivityInstancesOrderByStartedDateAsc");
        break;
      case ENDED_DATE_ASC:
        query = getSession().getNamedQuery("findActivityInstancesOrderByEndedDateAsc");
        break;
      case NAME_ASC:
        query = getSession().getNamedQuery("findActivityInstancesOrderByNameAsc");
        break;
      case PRIORITY_ASC:
        query = getSession().getNamedQuery("findActivityInstancesOrderByPriorityAsc");
        break;
      case LAST_UPDATE_DESC:
        query = getSession().getNamedQuery("findActivityInstancesOrderByLastUpdateDesc");
        break;
      case STARTED_DATE_DESC:
        query = getSession().getNamedQuery("findActivityInstancesOrderByStartedDateDesc");
        break;
      case ENDED_DATE_DESC:
        query = getSession().getNamedQuery("findActivityInstancesOrderByEndedDateDesc");
        break;
      case NAME_DESC:
        query = getSession().getNamedQuery("findActivityInstancesOrderByNameDesc");
        break;
      case PRIORITY_DESC:
        query = getSession().getNamedQuery("findActivityInstancesOrderByPriorityDesc");
        break;
      case DEFAULT:
        query = getSession().getNamedQuery("findActivityInstancesOrderByLastUpdateDesc");
        break;
    }

    query.setString("instanceUUID", instanceUUID.toString());
    query.setFirstResult(fromIndex);
    query.setMaxResults(pageSize);

    final List<InternalActivityInstance> results = query.list();
    if (results != null) {
      return new ArrayList<InternalActivityInstance>(results);
    }
    return null;
  }

  @Override
  public long getLastProcessInstanceNb(final ProcessDefinitionUUID processUUID) {
    final Query query = getSession().getNamedQuery("getLastProcessInstanceNb");

    query.setString("processUUID", processUUID.getValue());
    query.setMaxResults(1);
    final Long result = (Long) query.uniqueResult();
    if (result == null) {
      return -1;
    }
    return result;
  }

  @Override
  public InternalProcessInstance getProcessInstance(final ProcessInstanceUUID instanceUUID) {
    final Query query = getSession().getNamedQuery("findProcessInstance");

    query.setString("instanceUUID", instanceUUID.toString());
    query.setMaxResults(1);
    return (InternalProcessInstance) query.uniqueResult();
  }

  @Override
  @SuppressWarnings("unchecked")
  public Set<InternalProcessInstance> getProcessInstances() {
    final Set<InternalProcessInstance> processInsts = new HashSet<InternalProcessInstance>();
    final Query query = getSession().getNamedQuery("findAllProcessInstances");

    final List<InternalProcessInstance> results = query.list();
    if (results != null) {
      processInsts.addAll(results);
    }
    return processInsts;
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<InternalProcessInstance> getProcessInstances(final int fromIndex, final int pageSize) {
    final Query query = getSession().getNamedQuery("findAllProcessInstances");

    query.setFirstResult(fromIndex);
    query.setMaxResults(pageSize);
    final List<InternalProcessInstance> results = query.list();
    if (results == null) {
      return Collections.emptyList();
    }
    return results;
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<InternalProcessInstance> getProcessInstances(final int fromIndex, final int pageSize,
      final ProcessInstanceCriterion pagingCriterion) {
    Query query = null;
    switch (pagingCriterion) {
      case LAST_UPDATE_ASC:
        query = getSession().getNamedQuery("findAllProcessInstancesOrderByLastUpdateAsc");
        break;
      case STARTED_DATE_ASC:
        query = getSession().getNamedQuery("findAllProcessInstancesOrderByStartedDateAsc");
        break;
      case ENDED_DATE_ASC:
        query = getSession().getNamedQuery("findAllProcessInstancesOrderByEndedDateAsc");
        break;
      case INSTANCE_NUMBER_ASC:
        query = getSession().getNamedQuery("findAllProcessInstancesOrderByInstanceNumberAsc");
        break;
      case INSTANCE_UUID_ASC:
        query = getSession().getNamedQuery("findAllProcessInstancesOrderByInstanceUUIDAsc");
        break;
      case LAST_UPDATE_DESC:
        query = getSession().getNamedQuery("findAllProcessInstancesOrderByLastUpdateDesc");
        break;
      case STARTED_DATE_DESC:
        query = getSession().getNamedQuery("findAllProcessInstancesOrderByStartedDateDesc");
        break;
      case ENDED_DATE_DESC:
        query = getSession().getNamedQuery("findAllProcessInstancesOrderByEndedDateDesc");
        break;
      case INSTANCE_NUMBER_DESC:
        query = getSession().getNamedQuery("findAllProcessInstancesOrderByInstanceNumberDesc");
        break;
      case INSTANCE_UUID_DESC:
        query = getSession().getNamedQuery("findAllProcessInstancesOrderByInstanceUUIDDesc");
        break;
      case DEFAULT:
        query = getSession().getNamedQuery("findAllProcessInstances");
        break;
    }

    query.setFirstResult(fromIndex);
    query.setMaxResults(pageSize);
    final List<InternalProcessInstance> results = query.list();
    if (results == null) {
      return Collections.emptyList();
    }
    return results;
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<InternalProcessInstance> getMostRecentProcessInstances(final int maxResults, final long time) {
    final Query query = getSession().getNamedQuery("getMostRecentProcessInstances");

    query.setFirstResult(0);
    query.setLong("time", time);
    query.setMaxResults(maxResults);
    final List<InternalProcessInstance> results = query.list();
    if (results == null) {
      return Collections.emptyList();
    }
    return results;
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<InternalProcessInstance> getMostRecentProcessInstances(final int maxResults, final long time,
      final ProcessInstanceCriterion pagingCriterion) {
    Query query = null;
    switch (pagingCriterion) {
      case LAST_UPDATE_ASC:
        query = getSession().getNamedQuery("getMostRecentProcessInstancesOrderByLastUpdateAsc");
        break;
      case STARTED_DATE_ASC:
        query = getSession().getNamedQuery("getMostRecentProcessInstancesOrderByStartedDateAsc");
        break;
      case ENDED_DATE_ASC:
        query = getSession().getNamedQuery("getMostRecentProcessInstancesOrderByEndedDateAsc");
        break;
      case INSTANCE_NUMBER_ASC:
        query = getSession().getNamedQuery("getMostRecentProcessInstancesOrderByInstanceNumberAsc");
        break;
      case INSTANCE_UUID_ASC:
        query = getSession().getNamedQuery("getMostRecentProcessInstancesOrderByInstanceUUIDAsc");
        break;
      case LAST_UPDATE_DESC:
        query = getSession().getNamedQuery("getMostRecentProcessInstancesOrderByLastUpdateDesc");
        break;
      case STARTED_DATE_DESC:
        query = getSession().getNamedQuery("getMostRecentProcessInstancesOrderByStartedDateDesc");
        break;
      case ENDED_DATE_DESC:
        query = getSession().getNamedQuery("getMostRecentProcessInstancesOrderByEndedDateDesc");
        break;
      case INSTANCE_NUMBER_DESC:
        query = getSession().getNamedQuery("getMostRecentProcessInstancesOrderByInstanceNumberDesc");
        break;
      case INSTANCE_UUID_DESC:
        query = getSession().getNamedQuery("getMostRecentProcessInstancesOrderByInstanceUUIDDesc");
        break;
      case DEFAULT:
        query = getSession().getNamedQuery("getMostRecentProcessInstances");
        break;
    }

    query.setFirstResult(0);
    query.setLong("time", time);
    query.setMaxResults(maxResults);
    final List<InternalProcessInstance> results = query.list();
    if (results == null) {
      return Collections.emptyList();
    }
    return results;
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<InternalProcessInstance> getMostRecentParentProcessInstances(final int maxResults, final long time) {
    final Query query = getSession().getNamedQuery("getMostRecentParentProcessInstances");

    query.setFirstResult(0);
    query.setLong("time", time);
    query.setMaxResults(maxResults);
    final List<InternalProcessInstance> results = query.list();
    if (results == null) {
      return Collections.emptyList();
    }
    return results;
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<InternalProcessInstance> getMostRecentParentProcessInstances(final int maxResults, final long time,
      final ProcessInstanceCriterion pagingCriterion) {
    Query query = null;
    switch (pagingCriterion) {
      case LAST_UPDATE_ASC:
        query = getSession().getNamedQuery("getMostRecentParentProcessInstancesOrderByLastUpdateAsc");
        break;
      case STARTED_DATE_ASC:
        query = getSession().getNamedQuery("getMostRecentParentProcessInstancesOrderByStartedDateAsc");
        break;
      case ENDED_DATE_ASC:
        query = getSession().getNamedQuery("getMostRecentParentProcessInstancesOrderByEndedDateAsc");
        break;
      case INSTANCE_NUMBER_ASC:
        query = getSession().getNamedQuery("getMostRecentParentProcessInstancesOrderByInstanceNumberAsc");
        break;
      case INSTANCE_UUID_ASC:
        query = getSession().getNamedQuery("getMostRecentParentProcessInstancesOrderByInstanceUUIDAsc");
        break;
      case LAST_UPDATE_DESC:
        query = getSession().getNamedQuery("getMostRecentParentProcessInstancesOrderByLastUpdateDesc");
        break;
      case STARTED_DATE_DESC:
        query = getSession().getNamedQuery("getMostRecentParentProcessInstancesOrderByStartedDateDesc");
        break;
      case ENDED_DATE_DESC:
        query = getSession().getNamedQuery("getMostRecentParentProcessInstancesOrderByEndedDateDesc");
        break;
      case INSTANCE_NUMBER_DESC:
        query = getSession().getNamedQuery("getMostRecentParentProcessInstancesOrderByInstanceNumberDesc");
        break;
      case INSTANCE_UUID_DESC:
        query = getSession().getNamedQuery("getMostRecentParentProcessInstancesOrderByInstanceUUIDDesc");
        break;
      case DEFAULT:
        query = getSession().getNamedQuery("getMostRecentParentProcessInstances");
        break;
    }

    query.setFirstResult(0);
    query.setLong("time", time);
    query.setMaxResults(maxResults);
    final List<InternalProcessInstance> results = query.list();
    if (results == null) {
      return Collections.emptyList();
    }
    return results;
  }

  @Override
  public List<InternalProcessInstance> getMostRecentMatchingProcessInstances(
      final Collection<ProcessInstanceUUID> instanceUUIDs, final int maxResults, final long time) {
    final Query query = getSession().getNamedQuery("getMostRecentMatchingProcessInstances");

    query.setFirstResult(0);
    query.setLong("time", time);
    query.setMaxResults(maxResults);

    final Set<String> uuids = new HashSet<String>();
    for (final ProcessInstanceUUID uuid : instanceUUIDs) {
      uuids.add(uuid.toString());
    }

    final List<InternalProcessInstance> allInstances = new ArrayList<InternalProcessInstance>();
    allInstances.addAll(executeSplittedQuery(InternalProcessInstance.class, query, "instanceUUIDs", uuids));
    Collections.sort(allInstances, new ProcessInstanceLastUpdateComparator());
    final List<InternalProcessInstance> results = Misc.subList(InternalProcessInstance.class, allInstances, 0,
        maxResults);
    if (results == null) {
      return Collections.emptyList();
    }
    return results;
  }

  @Override
  public List<InternalProcessInstance> getMostRecentMatchingProcessInstances(
      final Set<ProcessInstanceUUID> instanceUUIDs, final int maxResults, final long time,
      final ProcessInstanceCriterion pagingCriterion) {
    Query query = null;
    Comparator<InternalProcessInstance> comparator = null;
    switch (pagingCriterion) {
      case LAST_UPDATE_ASC:
        comparator = new ProcessInstanceLastUpdateComparatorAsc();
        query = getSession().getNamedQuery("getMostRecentMatchingProcessInstancesOrderByLastUpdateAsc");
        break;
      case STARTED_DATE_ASC:
        comparator = new ProcessInstanceStartedDateComparatorAsc();
        query = getSession().getNamedQuery("getMostRecentMatchingProcessInstancesOrderByStartedDateAsc");
        break;
      case ENDED_DATE_ASC:
        comparator = new ProcessInstanceEndedDateComparatorAsc();
        query = getSession().getNamedQuery("getMostRecentMatchingProcessInstancesOrderByEndedDateAsc");
        break;
      case INSTANCE_NUMBER_ASC:
        comparator = new ProcessInstanceNbComparatorAsc();
        query = getSession().getNamedQuery("getMostRecentMatchingProcessInstancesOrderByInstanceNumberAsc");
        break;
      case INSTANCE_UUID_ASC:
        comparator = new ProcessInstanceUUIDComparatorAsc();
        query = getSession().getNamedQuery("getMostRecentMatchingProcessInstancesOrderByInstanceUUIDAsc");
        break;
      case LAST_UPDATE_DESC:
        comparator = new ProcessInstanceLastUpdateComparator();
        query = getSession().getNamedQuery("getMostRecentMatchingProcessInstancesOrderByLastUpdateDesc");
        break;
      case STARTED_DATE_DESC:
        comparator = new ProcessInstanceStartedDateComparatorDesc();
        query = getSession().getNamedQuery("getMostRecentMatchingProcessInstancesOrderByStartedDateDesc");
        break;
      case ENDED_DATE_DESC:
        comparator = new ProcessInstanceEndedDateComparatorDesc();
        query = getSession().getNamedQuery("getMostRecentMatchingProcessInstancesOrderByEndedDateDesc");
        break;
      case INSTANCE_NUMBER_DESC:
        comparator = new ProcessInstanceNbComparatorDesc();
        query = getSession().getNamedQuery("getMostRecentMatchingProcessInstancesOrderByInstanceNumberDesc");
        break;
      case INSTANCE_UUID_DESC:
        comparator = new ProcessInstanceUUIDComparatorDesc();
        query = getSession().getNamedQuery("getMostRecentMatchingProcessInstancesOrderByInstanceUUIDDesc");
        break;
      case DEFAULT:
        comparator = new ProcessInstanceLastUpdateComparator();
        query = getSession().getNamedQuery("getMostRecentMatchingProcessInstances");
        break;
    }

    query.setFirstResult(0);
    query.setLong("time", time);
    query.setMaxResults(maxResults);

    final Set<String> uuids = new HashSet<String>();
    for (final ProcessInstanceUUID uuid : instanceUUIDs) {
      uuids.add(uuid.toString());
    }

    final List<InternalProcessInstance> allInstances = new ArrayList<InternalProcessInstance>();
    allInstances.addAll(executeSplittedQuery(InternalProcessInstance.class, query, "instanceUUIDs", uuids));
    Collections.sort(allInstances, comparator);
    final List<InternalProcessInstance> results = Misc.subList(InternalProcessInstance.class, allInstances, 0,
        maxResults);
    if (results == null) {
      return Collections.emptyList();
    }
    return results;
  }

  @Override
  public List<InternalProcessInstance> getMostRecentProcessesProcessInstances(
      final Collection<ProcessDefinitionUUID> definitionUUIDs, final int maxResults, final long time,
      final ProcessInstanceCriterion pagingCriterion) {
    Query query = null;
    Comparator<InternalProcessInstance> comparator = null;
    switch (pagingCriterion) {
      case LAST_UPDATE_ASC:
        comparator = new ProcessInstanceLastUpdateComparatorAsc();
        query = getSession().getNamedQuery("getMostRecentProcessesProcessInstancesOrderByLastUpdateAsc");
        break;
      case STARTED_DATE_ASC:
        comparator = new ProcessInstanceStartedDateComparatorAsc();
        query = getSession().getNamedQuery("getMostRecentProcessesProcessInstancesOrderByStartedDateAsc");
        break;
      case ENDED_DATE_ASC:
        comparator = new ProcessInstanceEndedDateComparatorAsc();
        query = getSession().getNamedQuery("getMostRecentProcessesProcessInstancesOrderByEndedDateAsc");
        break;
      case INSTANCE_NUMBER_ASC:
        comparator = new ProcessInstanceNbComparatorAsc();
        query = getSession().getNamedQuery("getMostRecentProcessesProcessInstancesOrderByInstanceNumberAsc");
        break;
      case INSTANCE_UUID_ASC:
        comparator = new ProcessInstanceUUIDComparatorAsc();
        query = getSession().getNamedQuery("getMostRecentProcessesProcessInstancesOrderByInstanceUUIDAsc");
        break;
      case LAST_UPDATE_DESC:
        comparator = new ProcessInstanceLastUpdateComparator();
        query = getSession().getNamedQuery("getMostRecentProcessesProcessInstancesOrderByLastUpdateDesc");
        break;
      case STARTED_DATE_DESC:
        comparator = new ProcessInstanceStartedDateComparatorDesc();
        query = getSession().getNamedQuery("getMostRecentProcessesProcessInstancesOrderByStartedDateDesc");
        break;
      case ENDED_DATE_DESC:
        comparator = new ProcessInstanceEndedDateComparatorDesc();
        query = getSession().getNamedQuery("getMostRecentProcessesProcessInstancesOrderByEndedDateDesc");
        break;
      case INSTANCE_NUMBER_DESC:
        comparator = new ProcessInstanceNbComparatorDesc();
        query = getSession().getNamedQuery("getMostRecentProcessesProcessInstancesOrderByInstanceNumberDesc");
        break;
      case INSTANCE_UUID_DESC:
        comparator = new ProcessInstanceUUIDComparatorDesc();
        query = getSession().getNamedQuery("getMostRecentProcessesProcessInstancesOrderByInstanceUUIDDesc");
        break;
      case DEFAULT:
        comparator = new ProcessInstanceLastUpdateComparator();
        query = getSession().getNamedQuery("getMostRecentProcessesProcessInstances");
        break;
    }

    query.setFirstResult(0);
    query.setLong("time", time);
    query.setMaxResults(maxResults);

    final Set<String> uuids = new HashSet<String>();
    if (definitionUUIDs != null) {
      for (final ProcessDefinitionUUID uuid : definitionUUIDs) {
        uuids.add(uuid.toString());
      }
    }

    final List<InternalProcessInstance> allInstances = new ArrayList<InternalProcessInstance>();
    allInstances.addAll(executeSplittedQuery(InternalProcessInstance.class, query, "processUUIDs", uuids));
    Collections.sort(allInstances, comparator);
    final List<InternalProcessInstance> results = Misc.subList(InternalProcessInstance.class, allInstances, 0,
        maxResults);
    if (results == null) {
      return Collections.emptyList();
    }
    return results;
  }

  @Override
  public List<InternalProcessInstance> getMostRecentProcessesProcessInstances(
      final Collection<ProcessDefinitionUUID> definitionUUIDs, final int maxResults, final long time) {
    final Query query = getSession().getNamedQuery("getMostRecentProcessesProcessInstances");

    query.setFirstResult(0);
    query.setLong("time", time);
    query.setMaxResults(maxResults);

    final Set<String> uuids = new HashSet<String>();
    if (definitionUUIDs != null) {
      for (final ProcessDefinitionUUID uuid : definitionUUIDs) {
        uuids.add(uuid.toString());
      }
    }

    final List<InternalProcessInstance> allInstances = new ArrayList<InternalProcessInstance>();
    allInstances.addAll(executeSplittedQuery(InternalProcessInstance.class, query, "processUUIDs", uuids));
    Collections.sort(allInstances, new ProcessInstanceLastUpdateComparator());
    final List<InternalProcessInstance> results = Misc.subList(InternalProcessInstance.class, allInstances, 0,
        maxResults);
    if (results == null) {
      return Collections.emptyList();
    }
    return results;
  }

  @Override
  public List<InternalProcessInstance> getParentProcessInstances(final ProcessDefinitionUUID definitionUUID,
      final int fromIndex, final int pageSize) {
    final Query query = getSession().getNamedQuery("getParentInstancesOfProcessDefinition");
    query.setString("definitionUUID", definitionUUID.getValue());
    query.setFirstResult(fromIndex);
    query.setMaxResults(pageSize);
    return formatList(query.list());
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<InternalProcessInstance> getParentProcessInstances(final int fromIndex, final int pageSize) {
    final Query query = getSession().getNamedQuery("getParentInstances");
    query.setFirstResult(fromIndex);
    query.setMaxResults(pageSize);
    return formatList(query.list());
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<InternalProcessInstance> getParentProcessInstances(final int fromIndex, final int pageSize,
      final ProcessInstanceCriterion pagingCriterion) {
    Query query = null;
    switch (pagingCriterion) {
      case LAST_UPDATE_ASC:
        query = getSession().getNamedQuery("getParentInstancesOrderByLastUpdateAsc");
        break;
      case STARTED_DATE_ASC:
        query = getSession().getNamedQuery("getParentInstancesOrderByStartedDateAsc");
        break;
      case ENDED_DATE_ASC:
        query = getSession().getNamedQuery("getParentInstancesOrderByEndedDateAsc");
        break;
      case INSTANCE_NUMBER_ASC:
        query = getSession().getNamedQuery("getParentInstancesOrderByInstanceNumberAsc");
        break;
      case INSTANCE_UUID_ASC:
        query = getSession().getNamedQuery("getParentInstancesOrderByInstanceUUIDAsc");
        break;
      case LAST_UPDATE_DESC:
        query = getSession().getNamedQuery("getParentInstancesOrderByLastUpdateDesc");
        break;
      case STARTED_DATE_DESC:
        query = getSession().getNamedQuery("getParentInstancesOrderByStartedDateDesc");
        break;
      case ENDED_DATE_DESC:
        query = getSession().getNamedQuery("getParentInstancesOrderByEndedDateDesc");
        break;
      case INSTANCE_NUMBER_DESC:
        query = getSession().getNamedQuery("getParentInstancesOrderByInstanceNumberDesc");
        break;
      case INSTANCE_UUID_DESC:
        query = getSession().getNamedQuery("getParentInstancesOrderByInstanceUUIDDesc");
        break;
      case DEFAULT:
        query = getSession().getNamedQuery("getParentInstances");
        break;
    }

    query.setFirstResult(fromIndex);
    query.setMaxResults(pageSize);
    return formatList(query.list());
  }

  @Override
  public List<InternalProcessInstance> getParentProcessInstances(final Set<ProcessDefinitionUUID> processUUIDs,
      final int fromIndex, final int pageSize, final ProcessInstanceCriterion pagingCriterion) {
    Query query = null;
    switch (pagingCriterion) {
      case LAST_UPDATE_ASC:
        query = getSession().getNamedQuery("getParentInstancesFromProcessUUIDsOrderByLastUpdateAsc");
        break;
      case STARTED_DATE_ASC:
        query = getSession().getNamedQuery("getParentInstancesFromProcessUUIDsOrderByStartedDateAsc");
        break;
      case ENDED_DATE_ASC:
        query = getSession().getNamedQuery("getParentInstancesFromProcessUUIDsOrderByEndedDateAsc");
        break;
      case INSTANCE_NUMBER_ASC:
        query = getSession().getNamedQuery("getParentInstancesFromProcessUUIDsOrderByInstanceNumberAsc");
        break;
      case INSTANCE_UUID_ASC:
        query = getSession().getNamedQuery("getParentInstancesFromProcessUUIDsOrderByInstanceUUIDAsc");
        break;
      case LAST_UPDATE_DESC:
        query = getSession().getNamedQuery("getParentInstancesFromProcessUUIDsOrderByLastUpdateDesc");
        break;
      case STARTED_DATE_DESC:
        query = getSession().getNamedQuery("getParentInstancesFromProcessUUIDsOrderByStartedDateDesc");
        break;
      case ENDED_DATE_DESC:
        query = getSession().getNamedQuery("getParentInstancesFromProcessUUIDsOrderByEndedDateDesc");
        break;
      case INSTANCE_NUMBER_DESC:
        query = getSession().getNamedQuery("getParentInstancesFromProcessUUIDsOrderByInstanceNumberDesc");
        break;
      case INSTANCE_UUID_DESC:
        query = getSession().getNamedQuery("getParentInstancesFromProcessUUIDsOrderByInstanceUUIDDesc");
        break;
      case DEFAULT:
        query = getSession().getNamedQuery("getParentInstancesFromProcessUUIDsOrderByLastUpdateDesc");
        break;
    }

    query.setFirstResult(fromIndex);
    query.setMaxResults(pageSize);
    final List<String> uuids = new ArrayList<String>();
    for (final ProcessDefinitionUUID uuid : processUUIDs) {
      uuids.add(uuid.toString());
    }

    final List<InternalProcessInstance> results = executeSplittedQueryList(InternalProcessInstance.class, query,
        "processUUIDs", uuids);
    if (results == null) {
      return Collections.emptyList();
    }
    return results;
  }

  @Override
  public List<InternalProcessInstance> getParentProcessInstancesExcept(final Set<ProcessDefinitionUUID> exceptions,
      final int fromIndex, final int pageSize, final ProcessInstanceCriterion pagingCriterion) {
    Query query = null;
    switch (pagingCriterion) {
      case LAST_UPDATE_ASC:
        query = getSession().getNamedQuery("getParentInstancesExceptOrderByLastUpdateAsc");
        break;
      case STARTED_DATE_ASC:
        query = getSession().getNamedQuery("getParentInstancesExceptOrderByStartedDateAsc");
        break;
      case ENDED_DATE_ASC:
        query = getSession().getNamedQuery("getParentInstancesExceptOrderByEndedDateAsc");
        break;
      case INSTANCE_NUMBER_ASC:
        query = getSession().getNamedQuery("getParentInstancesExceptOrderByInstanceNumberAsc");
        break;
      case INSTANCE_UUID_ASC:
        query = getSession().getNamedQuery("getParentInstancesExceptOrderByInstanceUUIDAsc");
        break;
      case LAST_UPDATE_DESC:
        query = getSession().getNamedQuery("getParentInstancesExceptOrderByLastUpdateDesc");
        break;
      case STARTED_DATE_DESC:
        query = getSession().getNamedQuery("getParentInstancesExceptOrderByStartedDateDesc");
        break;
      case ENDED_DATE_DESC:
        query = getSession().getNamedQuery("getParentInstancesExceptOrderByEndedDateDesc");
        break;
      case INSTANCE_NUMBER_DESC:
        query = getSession().getNamedQuery("getParentInstancesExceptOrderByInstanceNumberDesc");
        break;
      case INSTANCE_UUID_DESC:
        query = getSession().getNamedQuery("getParentInstancesExceptOrderByInstanceUUIDDesc");
        break;
      case DEFAULT:
        query = getSession().getNamedQuery("getParentInstancesExceptOrderByLastUpdateDesc");
        break;
    }

    query.setFirstResult(fromIndex);
    query.setMaxResults(pageSize);
    final List<String> uuids = new ArrayList<String>();
    for (final ProcessDefinitionUUID uuid : exceptions) {
      uuids.add(uuid.toString());
    }

    final List<InternalProcessInstance> results = executeSplittedQueryList(InternalProcessInstance.class, query,
        "processUUIDs", uuids);
    if (results == null) {
      return Collections.emptyList();
    }
    return results;
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<InternalProcessDefinition> getProcesses(final int fromIndex, final int pageSize) {
    final Query query = getSession().getNamedQuery("getAllProcesses");
    query.setFirstResult(fromIndex);
    query.setMaxResults(pageSize);
    return formatList(query.list());
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<InternalProcessDefinition> getProcesses(final int fromIndex, final int pageSize,
      final ProcessDefinitionCriterion pagingCriterion) {
    Query query = null;
    switch (pagingCriterion) {
      case NAME_ASC:
        query = getSession().getNamedQuery("getAllProcessesOrderByNameAsc");
        break;

      case LABEL_ASC:
        query = getSession().getNamedQuery("getAllProcessesOrderByLabelAsc");
        break;

      case VERSION_ASC:
        query = getSession().getNamedQuery("getAllProcessesOrderByVersionAsc");
        break;

      case STATE_ASC:
        query = getSession().getNamedQuery("getAllProcessesOrderByStateAsc");
        break;

      case NAME_DESC:
        query = getSession().getNamedQuery("getAllProcessesOrderByNameDesc");
        break;

      case LABEL_DESC:
        query = getSession().getNamedQuery("getAllProcessesOrderByLabelDesc");
        break;

      case VERSION_DESC:
        query = getSession().getNamedQuery("getAllProcessesOrderByVersionDesc");
        break;

      case STATE_DESC:
        query = getSession().getNamedQuery("getAllProcessesOrderByStateDesc");
        break;

      case DEFAULT:
        query = getSession().getNamedQuery("getAllProcesses");
        break;
    }

    query.setFirstResult(fromIndex);
    query.setMaxResults(pageSize);
    return formatList(query.list());
  }

  @Override
  public List<InternalProcessInstance> getProcessInstances(final Collection<ProcessInstanceUUID> instanceUUIDs,
      final int fromIndex, final int pageSize) {
    final Query query = getSession().getNamedQuery("findMatchingProcessInstances");

    query.setFirstResult(fromIndex);
    query.setMaxResults(pageSize);
    final Set<String> uuids = new HashSet<String>();
    if (instanceUUIDs != null) {
      for (final ProcessInstanceUUID uuid : instanceUUIDs) {
        uuids.add(uuid.toString());
      }
    }

    final List<InternalProcessInstance> allInstances = new ArrayList<InternalProcessInstance>();
    allInstances.addAll(executeSplittedQuery(InternalProcessInstance.class, query, "instanceUUIDs", uuids));
    Collections.sort(allInstances, new ProcessInstanceLastUpdateComparator());
    final List<InternalProcessInstance> results = Misc
        .subList(InternalProcessInstance.class, allInstances, 0, pageSize);
    if (results == null) {
      return Collections.emptyList();
    }
    return results;
  }

  @Override
  public List<InternalProcessInstance> getProcessInstancesWithInstanceUUIDs(
      final Set<ProcessInstanceUUID> instanceUUIDs, final int fromIndex, final int pageSize,
      final ProcessInstanceCriterion pagingCriterion) {
    Query query = null;
    Comparator<InternalProcessInstance> comparator = null;
    switch (pagingCriterion) {
      case LAST_UPDATE_ASC:
        comparator = new ProcessInstanceLastUpdateComparatorAsc();
        query = getSession().getNamedQuery("findMatchingProcessInstancesOrderByLastUpdateAsc");
        break;
      case STARTED_DATE_ASC:
        comparator = new ProcessInstanceStartedDateComparatorAsc();
        query = getSession().getNamedQuery("findMatchingProcessInstancesOrderByStartedDateAsc");
        break;
      case ENDED_DATE_ASC:
        comparator = new ProcessInstanceEndedDateComparatorAsc();
        query = getSession().getNamedQuery("findMatchingProcessInstancesOrderByEndedDateAsc");
        break;
      case INSTANCE_NUMBER_ASC:
        comparator = new ProcessInstanceNbComparatorAsc();
        query = getSession().getNamedQuery("findMatchingProcessInstancesOrderByInstanceNumberAsc");
        break;
      case INSTANCE_UUID_ASC:
        comparator = new ProcessInstanceUUIDComparatorAsc();
        query = getSession().getNamedQuery("findMatchingProcessInstancesOrderByIntanceUUIDAsc");
        break;
      case LAST_UPDATE_DESC:
        comparator = new ProcessInstanceLastUpdateComparator();
        query = getSession().getNamedQuery("findMatchingProcessInstancesOrderByLastUpdateDesc");
        break;
      case STARTED_DATE_DESC:
        comparator = new ProcessInstanceStartedDateComparatorDesc();
        query = getSession().getNamedQuery("findMatchingProcessInstancesOrderByStartedDateDesc");
        break;
      case ENDED_DATE_DESC:
        comparator = new ProcessInstanceEndedDateComparatorDesc();
        query = getSession().getNamedQuery("findMatchingProcessInstancesOrderByEndedDateDesc");
        break;
      case INSTANCE_NUMBER_DESC:
        comparator = new ProcessInstanceNbComparatorDesc();
        query = getSession().getNamedQuery("findMatchingProcessInstancesOrderByInstanceNumberDesc");
        break;
      case INSTANCE_UUID_DESC:
        comparator = new ProcessInstanceUUIDComparatorDesc();
        query = getSession().getNamedQuery("findMatchingProcessInstancesOrderByIntanceUUIDDesc");
        break;
      case DEFAULT:
        comparator = new ProcessInstanceLastUpdateComparator();
        query = getSession().getNamedQuery("findMatchingProcessInstances");
        break;
    }

    query.setFirstResult(fromIndex);
    query.setMaxResults(pageSize);
    final Set<String> uuids = new HashSet<String>();
    if (instanceUUIDs != null) {
      for (final ProcessInstanceUUID uuid : instanceUUIDs) {
        uuids.add(uuid.toString());
      }
    }

    final List<InternalProcessInstance> allInstances = new ArrayList<InternalProcessInstance>();
    allInstances.addAll(executeSplittedQuery(InternalProcessInstance.class, query, "instanceUUIDs", uuids));
    Collections.sort(allInstances, comparator);
    final List<InternalProcessInstance> results = Misc
        .subList(InternalProcessInstance.class, allInstances, 0, pageSize);
    if (results == null) {
      return Collections.emptyList();
    }
    return results;
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<ProcessInstanceUUID> getLabelsCaseUUIDs(final String ownerName, final Set<String> labelNames,
      final int fromIndex, final int pageSize) {
    final Query query = getSession().getNamedQuery("getLabelsCaseUUIDs");

    query.setFirstResult(fromIndex);
    query.setMaxResults(pageSize);
    query.setString("ownerName", ownerName);
    query.setParameterList("labelNames", labelNames);
    return query.list();
  }

  @Override
  @SuppressWarnings("unchecked")
  public Set<InternalProcessInstance> getParentInstances() {
    final Set<InternalProcessInstance> processInsts = new HashSet<InternalProcessInstance>();
    final Query query = getSession().getNamedQuery("getParentInstances");

    final List<InternalProcessInstance> results = query.list();
    if (results != null) {
      processInsts.addAll(results);
    }
    return processInsts;
  }

  @Override
  @SuppressWarnings("unchecked")
  public Set<ProcessInstanceUUID> getParentInstancesUUIDs() {
    final Set<ProcessInstanceUUID> processInsts = new HashSet<ProcessInstanceUUID>();
    final Query query = getSession().getNamedQuery("getParentInstancesUUIDs");
    final List<ProcessInstanceUUID> results = query.list();
    if (results != null) {
      processInsts.addAll(results);
    }
    return processInsts;
  }

  @Override
  public Set<InternalProcessInstance> getProcessInstances(final Collection<ProcessInstanceUUID> instanceUUIDs) {
    final Query query = getSession().getNamedQuery("findMatchingProcessInstances");
    final Set<String> uuids = new HashSet<String>();
    for (final ProcessInstanceUUID uuid : instanceUUIDs) {
      uuids.add(uuid.toString());
    }
    return executeSplittedQuery(InternalProcessInstance.class, query, "instanceUUIDs", uuids);
  }

  @Override
  @SuppressWarnings("unchecked")
  public Set<InternalProcessInstance> getProcessInstancesWithTaskState(final Collection<ActivityState> activityStates) {
    final Query getActivitiesQuery = getSession().getNamedQuery("findActivityInstancesWithTaskState");
    getActivitiesQuery.setParameterList("activityStates", activityStates, ACTIVITY_STATE_USER_TYPE);
    final List<String> uuids = getActivitiesQuery.list();
    final Set<InternalProcessInstance> processInsts = new HashSet<InternalProcessInstance>();
    if (uuids.isEmpty()) {
      return processInsts;
    }
    final Query query = getSession().getNamedQuery("findMatchingProcessInstances");
    return executeSplittedQuery(InternalProcessInstance.class, query, "instanceUUIDs", uuids);
  }

  @Override
  @SuppressWarnings("unchecked")
  public Set<InternalProcessInstance> getProcessInstancesWithInstanceStates(
      final Collection<InstanceState> instanceStates) {
    final Query getProcessInstancesQuery = getSession().getNamedQuery("findProcessInstancesWithInstanceStates");
    getProcessInstancesQuery.setParameterList("instanceStates", instanceStates, DEFINITION_STATE_USER_TYPE);
    final List<String> uuids = getProcessInstancesQuery.list();
    final Set<InternalProcessInstance> processInsts = new HashSet<InternalProcessInstance>();
    if (uuids.isEmpty()) {
      return processInsts;
    }
    final Query query = getSession().getNamedQuery("findMatchingProcessInstances");
    return executeSplittedQuery(InternalProcessInstance.class, query, "instanceUUIDs", uuids);
  }

  @Override
  @SuppressWarnings("unchecked")
  public Set<InternalProcessInstance> getProcessInstances(final ProcessDefinitionUUID processUUID) {
    final Set<InternalProcessInstance> processInsts = new HashSet<InternalProcessInstance>();
    final Query query = getSession().getNamedQuery("findProcessInstances");
    query.setString("processUUID", processUUID.toString());
    final List<InternalProcessInstance> results = query.list();
    if (results != null) {
      processInsts.addAll(results);
    }
    return processInsts;
  }

  @Override
  @SuppressWarnings("unchecked")
  public Set<InternalProcessInstance> getProcessInstances(final ProcessDefinitionUUID processUUID,
      final InstanceState instanceState) {
    final Set<InternalProcessInstance> processInsts = new HashSet<InternalProcessInstance>();
    final Query query = getSession().getNamedQuery("findProcessInstancesWithState");
    query.setString("processUUID", processUUID.toString());
    query.setParameter("state", instanceState, INSTANCE_STATE_USER_TYPE);
    final List<InternalProcessInstance> results = query.list();
    if (results != null) {
      processInsts.addAll(results);
    }
    return processInsts;
  }

  @Override
  public TaskInstance getTaskInstance(final ActivityInstanceUUID taskUUID) {
    final Query query = getSession().getNamedQuery("findTaskInstance");
    query.setString("taskUUID", taskUUID.toString());
    query.setMaxResults(1);
    return (TaskInstance) query.uniqueResult();
  }

  @Override
  @SuppressWarnings("unchecked")
  public Set<TaskInstance> getTaskInstances(final ProcessInstanceUUID instanceUUID) {
    final Query query = getSession().getNamedQuery("findTaskInstances");
    query.setString("instanceUUID", instanceUUID.toString());
    final Set<TaskInstance> taskInstances = new HashSet<TaskInstance>();
    final List<TaskInstance> results = query.list();
    if (results != null) {
      taskInstances.addAll(results);
    }
    return taskInstances;
  }

  @Override
  @SuppressWarnings("unchecked")
  public Set<TaskInstance> getTaskInstances(final ProcessInstanceUUID instanceUUID, final Set<String> taskNames) {
    final Query query = getSession().getNamedQuery("getTasksFromNames");
    query.setString("instanceUUID", instanceUUID.getValue());
    query.setParameterList("taskNames", taskNames);
    final Set<TaskInstance> taskInstances = new HashSet<TaskInstance>();
    final List<TaskInstance> results = query.list();
    if (results != null) {
      taskInstances.addAll(results);
    }
    return taskInstances;
  }

  /*
   * DEFINITION
   */
  @SuppressWarnings("unchecked")
  private Set<InternalProcessDefinition> getProcessSet(final Query query) {
    final List<InternalProcessDefinition> results = query.list();
    if (results != null) {
      return new HashSet<InternalProcessDefinition>(results);
    }
    return null;
  }

  @Override
  public Set<InternalProcessDefinition> getProcesses() {
    final Query query = getSession().getNamedQuery("getAllProcesses");

    return getProcessSet(query);
  }

  @Override
  public Set<InternalProcessDefinition> getProcesses(final String processId) {
    final Query query = getSession().getNamedQuery("getProcesses2");

    query.setString("processId", processId);
    return getProcessSet(query);
  }

  @Override
  public InternalProcessDefinition getProcess(final ProcessDefinitionUUID processUUID) {
    final Query query = getSession().getNamedQuery("getProcess");

    query.setString("processUUID", processUUID.toString());
    query.setMaxResults(1);
    return (InternalProcessDefinition) query.uniqueResult();
  }

  @Override
  public InternalProcessDefinition getProcess(final String processId, final String version) {
    final Query query = getSession().getNamedQuery("getProcessFromIdAndVersion");

    query.setString("processId", processId);
    query.setString("version", version);
    return (InternalProcessDefinition) query.uniqueResult();
  }

  @Override
  public Set<InternalProcessDefinition> getProcesses(final ProcessState processState) {
    final Query query = getSession().getNamedQuery("getProcessesFromState");
    query.setParameter("state", processState, DEFINITION_STATE_USER_TYPE);

    return getProcessSet(query);
  }

  @Override
  public Set<InternalProcessDefinition> getProcesses(final String processId, final ProcessState processState) {
    final Query query = getSession().getNamedQuery("getProcessesFromProcessIdAndState");
    query.setParameter("state", processState, DEFINITION_STATE_USER_TYPE);

    query.setString("processId", processId);
    return getProcessSet(query);
  }

  @Override
  public String getLastProcessVersion(final String processName) {
    final Query query = getSession().getNamedQuery("getLastProcessVersion");

    query.setString("name", processName);
    query.setMaxResults(1);
    return (String) query.uniqueResult();
  }

  @Override
  public InternalProcessDefinition getLastProcess(final String processId, final ProcessState processState) {
    final Query query = getSession().getNamedQuery("getLastProcessFromProcessIdAndState");
    query.setParameter("state", processState, DEFINITION_STATE_USER_TYPE);

    query.setString("processId", processId);
    query.setMaxResults(1);
    return (InternalProcessDefinition) query.uniqueResult();
  }

  @Override
  public InternalActivityInstance getActivityInstance(final ProcessInstanceUUID instanceUUID, final String activityId,
      final String iterationId, final String activityInstanceId, final String loopId) {
    final Query query = getSession().getNamedQuery("getActivityInstance");

    query.setString("instanceUUID", instanceUUID.toString());
    query.setString("activityId", activityId);
    query.setString("iterationId", iterationId);
    query.setString("activityInstanceId", activityInstanceId);
    query.setString("loopId", loopId);
    return (InternalActivityInstance) query.uniqueResult();
  }

  @Override
  public InternalActivityInstance getActivityInstance(final ActivityInstanceUUID activityInstanceUUID) {
    final Query query = getSession().getNamedQuery("getActivityInstanceFromUUID");

    query.setString("activityUUID", activityInstanceUUID.toString());
    return (InternalActivityInstance) query.uniqueResult();
  }

  @Override
  public ActivityState getActivityInstanceState(final ActivityInstanceUUID activityInstanceUUID) {
    final Query query = getSession().getNamedQuery("getActivityInstanceStateFromUUID");

    query.setString("activityUUID", activityInstanceUUID.toString());
    return (ActivityState) query.uniqueResult();
  }

  @Override
  public InternalActivityDefinition getActivityDefinition(final ActivityDefinitionUUID activityDefinitionUUID) {
    final Query query = getSession().getNamedQuery("getActivityDefinition");

    query.setString("activityUUID", activityDefinitionUUID.toString());
    query.setMaxResults(1);
    return (InternalActivityDefinition) query.uniqueResult();
  }

  @Override
  public Execution getExecutionWithEventUUID(final String eventUUID) {
    final Query query = getSession().getNamedQuery("getExecutionWithEventUUID");

    query.setString("eventUUID", eventUUID);
    return (Execution) query.uniqueResult();
  }

  @Override
  public Set<Execution> getExecutions(final ProcessInstanceUUID instanceUUID) {
    final Query query = getSession().getNamedQuery("getInstanceExecutions");

    query.setString("instanceUUID", instanceUUID.getValue());
    final Set<Execution> executions = new HashSet<Execution>();
    CollectionUtils.addAll(executions, query.iterate());
    return executions;
  }

  @Override
  public Execution getExecutionPointingOnNode(final ActivityInstanceUUID activityUUID) {
    final Query query = getSession().getNamedQuery("findInstanceExecutionPointingOnNode");

    query.setString("activityUUID", activityUUID.toString());
    return (Execution) query.uniqueResult();
  }

  /*
   * RUNTIME
   */
  @Override
  public MetaDataImpl getMetaData(final String key) {
    final Query query = getSession().getNamedQuery("getMetaData");

    query.setString("key", key);
    return (MetaDataImpl) query.uniqueResult();
  }

  @Override
  public Set<ProcessInstanceUUID> getAllCases() {
    final Set<ProcessInstanceUUID> result = new HashSet<ProcessInstanceUUID>();
    final Query query = getSession().getNamedQuery("getAllCases");

    CollectionUtils.addAll(result, query.iterate());
    return result;
  }

  @Override
  public Set<CaseImpl> getCases(final Set<ProcessInstanceUUID> caseUUIDs) {
    final Collection<String> uuids = new HashSet<String>();
    if (caseUUIDs != null) {
      for (final ProcessInstanceUUID processInstanceUUID : caseUUIDs) {
        uuids.add(processInstanceUUID.getValue());
      }
    }
    final Query query = getSession().getNamedQuery("getMatchingCases");

    return executeSplittedQuery(CaseImpl.class, query, "uuids", uuids);
  }

  @Override
  public List<LabelImpl> getUserCustomLabels(final String ownerName) {
    final List<LabelImpl> result = new ArrayList<LabelImpl>();
    final Query query = getSession().getNamedQuery("getUserCustomLabels");
    query.setString("ownerName", ownerName);
    CollectionUtils.addAll(result, query.iterate());
    return result;
  }

  @Override
  public List<LabelImpl> getSystemLabels(final String ownerName) {

    final List<LabelImpl> result = new ArrayList<LabelImpl>();
    final Query query = getSession().getNamedQuery("getSystemLabels");
    query.setString("ownerName", ownerName);
    CollectionUtils.addAll(result, query.iterate());

    return result;
  }

  @Override
  public LabelImpl getLabel(final String ownerName, final String labelName) {
    final Query query = getSession().getNamedQuery("getLabelByID");
    query.setString("ownerName", ownerName);
    query.setString("labelName", labelName);

    query.setMaxResults(1);
    final LabelImpl theResult = (LabelImpl) query.uniqueResult();

    return theResult;
  }

  @Override
  public Set<LabelImpl> getLabels(final String ownerName) {
    final Set<LabelImpl> result = new HashSet<LabelImpl>();
    final Query query = getSession().getNamedQuery("getAllLabels");
    query.setString("ownerName", ownerName);
    CollectionUtils.addAll(result, query.iterate());

    return result;
  }

  @Override
  @SuppressWarnings("unchecked")
  public Set<LabelImpl> getCaseLabels(final String ownerName, final ProcessInstanceUUID case_) {
    final Query getUserCases = getSession().getNamedQuery("getUserCases");
    getUserCases.setString("ownerName", ownerName);
    getUserCases.setString("caseId", case_.getValue());
    final List<String> caseLabelsNames = getUserCases.list();
    Set<LabelImpl> result = new HashSet<LabelImpl>();
    if (!caseLabelsNames.isEmpty()) {
      final Query query = getSession().getNamedQuery("getLabels");
      query.setString("ownerName", ownerName);

      result = executeSplittedQuery(LabelImpl.class, query, "labelNames", caseLabelsNames);
    }
    return result;
  }

  private <T> Set<T> executeSplittedQuery(final Class<T> clazz, final Query query, final String parameterName,
      final Collection<? extends Object> values) {
    if (values == null || values.isEmpty()) {
      return Collections.emptySet();
    }
    final Set<T> result = new HashSet<T>();
    if (values.size() <= BonitaConstants.MAX_QUERY_SIZE) {
      query.setParameterList(parameterName, values);
      CollectionUtils.addAll(result, query.iterate());
      return result;
    }

    final List<Collection<Object>> newValues = Misc.splitCollection(values, BonitaConstants.MAX_QUERY_SIZE);
    for (final Collection<Object> set : newValues) {
      query.setParameterList(parameterName, set);
      CollectionUtils.addAll(result, query.iterate());
    }
    return result;
  }

  private <T> List<T> executeSplittedQueryList(final Class<T> clazz, final Query query, final String parameterName,
      final Collection<? extends Object> values) {
    return executeSplittedQueryList(clazz, query, parameterName, values, BonitaConstants.MAX_QUERY_SIZE);
  }

  @SuppressWarnings("unchecked")
  private <T> List<T> executeSplittedQueryList(final Class<T> clazz, final Query query, final String parameterName,
      final Collection<? extends Object> values, final int size) {
    if (values == null || values.isEmpty()) {
      return Collections.emptyList();
    }
    final List<T> result = new ArrayList<T>();
    if (values.size() <= size) {
      query.setParameterList(parameterName, values);
      return query.list();
    }

    final List<Collection<Object>> newValues = Misc.splitCollection(values, size);
    for (final Collection<Object> set : newValues) {
      query.setParameterList(parameterName, set);
      CollectionUtils.addAll(result, query.iterate());
    }
    return result;
  }

  @Override
  public Set<CaseImpl> getCases(final String ownerName, final String labelName) {
    final Set<CaseImpl> result = new HashSet<CaseImpl>();
    final Query query = getSession().getNamedQuery("getLabelCases");
    query.setString("ownerName", ownerName);
    query.setString("labelName", labelName);
    CollectionUtils.addAll(result, query.iterate());
    return result;
  }

  @Override
  public Set<CaseImpl> getCases(final ProcessInstanceUUID case_) {
    final Set<CaseImpl> result = new HashSet<CaseImpl>();
    final Query query = getSession().getNamedQuery("getCases");
    query.setString("caseId", case_.getValue());
    CollectionUtils.addAll(result, query.iterate());
    return result;
  }

  @Override
  public CaseImpl getCase(final ProcessInstanceUUID caseUUID, final String ownerName, final String labelName) {
    final Query query = getSession().getNamedQuery("getCase");
    query.setString("caseUUID", caseUUID.getValue());
    query.setString("ownerName", ownerName);
    query.setString("labelName", labelName);

    return (CaseImpl) query.uniqueResult();
  }

  @Override
  public Set<LabelImpl> getLabels(final String ownerName, final Set<String> labelsName) {
    final Query query = getSession().getNamedQuery("getLabels");
    query.setString("ownerName", ownerName);

    return executeSplittedQuery(LabelImpl.class, query, "labelNames", labelsName);
  }

  @Override
  public Set<LabelImpl> getLabels(final Set<String> labelsName) {
    final Query query = getSession().getNamedQuery("getLabelsWithName");

    return executeSplittedQuery(LabelImpl.class, query, "labelNames", labelsName);
  }

  @Override
  public Set<LabelImpl> getLabelsByNameExcept(final Set<String> labelNames) {
    final Query query = getSession().getNamedQuery("getLabelsByNameExcept");

    return executeSplittedQuery(LabelImpl.class, query, "labelNames", labelNames);
  }

  @Override
  public int getCasesNumberWithTwoLabels(final String ownerName, final String label1Name, final String label2Name) {
    final Query query = getSession().getNamedQuery("getCasesNumberWithTwoLabels");
    query.setString("label1", CaseImpl.buildLabel(ownerName, label1Name));
    query.setString("label2", CaseImpl.buildLabel(ownerName, label2Name));
    query.setReadOnly(true);

    return ((Long) query.uniqueResult()).intValue();
  }

  @Override
  public int getCasesNumber(final String ownerName, final String labelName) {
    final Query query = getSession().getNamedQuery("getCasesNumber");
    query.setString("ownerName", ownerName);
    query.setString("label", labelName);
    query.setReadOnly(true);
    return ((Long) query.uniqueResult()).intValue();
  }

  @Override
  @SuppressWarnings("unchecked")
  public Set<CaseImpl> getCasesWithTwoLabels(final String ownerName, final String label1Name, final String label2Name,
      final int limit) {
    final Query query = getSession().getNamedQuery("getCasesWithTwoLabelsWithLimit");
    query.setString("label1", CaseImpl.buildLabel(ownerName, label1Name));
    query.setString("label2", CaseImpl.buildLabel(ownerName, label2Name));
    query.setReadOnly(true);
    query.setMaxResults(limit);

    final List<CaseImpl> results = query.list();
    if (results != null) {
      return new HashSet<CaseImpl>(results);
    } else {
      return new HashSet<CaseImpl>();
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public Set<CaseImpl> getCases(final String ownerName, final String labelName, final int limit) {
    final Query query = getSession().getNamedQuery("getLabelCases");
    query.setString("ownerName", ownerName);
    query.setString("labelName", labelName);
    query.setMaxResults(limit);
    final List<CaseImpl> results = query.list();
    if (results != null) {
      return new HashSet<CaseImpl>(results);
    } else {
      return new HashSet<CaseImpl>();
    }
  }

  @Override
  public Set<ProcessInstanceUUID> getCases(final String ownerName, final Set<String> theLabelsName) {
    final Query query = getSession().getNamedQuery("getLabelsCases");
    query.setString("ownerName", ownerName);

    return executeSplittedQuery(ProcessInstanceUUID.class, query, "labelNames", theLabelsName);
  }

  // getAllWebCases
  // getMatchingCases

  @Override
  public void deleteAllCases() {
    final Session session = getSession();
    final Query query = session.getNamedQuery("getAllWebCases");

    for (final Object webCase : query.list()) {
      session.delete(webCase);
    }
  }

  @Override
  public void deleteCases(final Set<ProcessInstanceUUID> webCases) {
    final Session session = getSession();
    final Query query = session.getNamedQuery("getMatchingCases");

    final Set<String> uuids = new HashSet<String>();
    for (final ProcessInstanceUUID webCase : webCases) {
      uuids.add(webCase.getValue());
    }
    for (final CaseImpl webCase : executeSplittedQuery(CaseImpl.class, query, "uuids", uuids)) {
      session.delete(webCase);
    }
  }

  @Override
  public Set<ProcessInstanceUUID> getCasesUUIDs(final String ownerName, final String labelName,
      final Set<ProcessInstanceUUID> cases) {
    final Collection<String> uuids = new HashSet<String>();
    if (cases != null) {
      for (final ProcessInstanceUUID processInstanceUUID : cases) {
        uuids.add(processInstanceUUID.getValue());
      }
    }

    final Query query = getSession().getNamedQuery("getLabelCasesUUIDsSublist");
    query.setString("ownerName", ownerName);
    query.setString("labelName", labelName);

    return executeSplittedQuery(ProcessInstanceUUID.class, query, "caseUUIDs", uuids);
  }

  @Override
  public Set<ProcessInstanceUUID> getLabelCases(final String labelName, final Set<ProcessInstanceUUID> caseUUIDs) {
    if (caseUUIDs == null || caseUUIDs.isEmpty()) {
      return Collections.emptySet();
    }
    final Collection<String> uuids = new HashSet<String>();
    for (final ProcessInstanceUUID processInstanceUUID : caseUUIDs) {
      uuids.add(processInstanceUUID.getValue());
    }

    final Query query = getSession().getNamedQuery("getLabelNameCases");
    query.setString("labelName", labelName);

    return executeSplittedQuery(ProcessInstanceUUID.class, query, "uuids", uuids);
  }

  @Override
  public Set<CaseImpl> getLabelCases(final String ownerName, final Set<String> labelsNames,
      final Set<ProcessInstanceUUID> caseUUIDs) {
    if (caseUUIDs == null || caseUUIDs.isEmpty()) {
      return Collections.emptySet();
    }
    final Collection<String> uuids = new HashSet<String>();
    for (final ProcessInstanceUUID processInstanceUUID : caseUUIDs) {
      uuids.add(processInstanceUUID.getValue());
    }

    final Query query = getSession().getNamedQuery("getLabelsNameCases");
    query.setParameterList("labelsNames", labelsNames);
    query.setString("ownerName", ownerName);

    return executeSplittedQuery(CaseImpl.class, query, "uuids", uuids);
  }

  @Override
  public Set<CaseImpl> getCases(final String ownerName, final String labelName, final Set<ProcessInstanceUUID> caseList) {
    final Collection<String> uuids = new HashSet<String>();
    if (caseList != null) {
      for (final ProcessInstanceUUID processInstanceUUID : caseList) {
        uuids.add(processInstanceUUID.getValue());
      }
    }

    final Query query = getSession().getNamedQuery("getLabelCasesSublist");
    query.setString("ownerName", ownerName);
    query.setString("labelName", labelName);

    return executeSplittedQuery(CaseImpl.class, query, "caseUUIDs", uuids);
  }

  @Override
  public List<Integer> getNumberOfExecutingCasesPerDay(Date since, final Date to) {
    final List<Integer> executingCases = new ArrayList<Integer>();
    while (since.before(to) || since.equals(to)) {
      final Date nextDayBegining = DateUtil.getBeginningOfTheDay(DateUtil.getNextDay(since));
      final Query query = getSession().getNamedQuery("getNumberOfExecutingCases");

      query.setLong("date", nextDayBegining.getTime());
      executingCases.add(((Long) query.uniqueResult()).intValue());
      since = DateUtil.getNextDay(since);
    }
    return executingCases;
  }

  @Override
  public List<Integer> getNumberOfFinishedCasesPerDay(Date since, final Date to) {
    final List<Integer> finishedCases = new ArrayList<Integer>();
    while (since.before(to) || since.equals(to)) {
      final Date beginningOfTheDay = DateUtil.getBeginningOfTheDay(since);
      final Date nextBeginningOfTheDay = DateUtil.getBeginningOfTheDay(DateUtil.getNextDay(since));
      final Query query = getSession().getNamedQuery("getNumberOfFinishedCases");

      query.setLong("beginningOfTheDay", beginningOfTheDay.getTime());
      query.setLong("nextBeginningOfTheDay", nextBeginningOfTheDay.getTime());
      finishedCases.add(((Long) query.uniqueResult()).intValue());
      since = DateUtil.getNextDay(since);
    }
    return finishedCases;
  }

  @Override
  public List<Integer> getNumberOfOpenStepsPerDay(Date since, final Date to) {
    final List<Integer> finishedCases = new ArrayList<Integer>();
    while (since.before(to) || since.equals(to)) {
      final Date nextDayBegining = DateUtil.getBeginningOfTheDay(DateUtil.getNextDay(since));
      final Query query = getSession().getNamedQuery("getNumberOfOpenSteps2");

      query.setLong("date", nextDayBegining.getTime());
      finishedCases.add(((Long) query.uniqueResult()).intValue());
      since = DateUtil.getNextDay(since);
    }
    return finishedCases;
  }

  @Override
  public int getNumberOfOpenSteps() {
    final Query query = getSession().getNamedQuery("getNumberOfOpenSteps");

    return ((Long) query.uniqueResult()).intValue();
  }

  @Override
  public int getNumberOfOverdueSteps(final Date currentDate) {
    final Query query = getSession().getNamedQuery("getNumberOfOverdueSteps");

    query.setLong("currentDate", currentDate.getTime());
    return ((Long) query.uniqueResult()).intValue();
  }

  @Override
  public int getNumberOfStepsAtRisk(final Date currentDate, final Date atRisk) {
    final Query query = getSession().getNamedQuery("getNumberOfStepsAtRisk");

    query.setLong("atRisk", atRisk.getTime());
    query.setLong("currentDate", currentDate.getTime());
    return ((Long) query.uniqueResult()).intValue();
  }

  @Override
  public int getNumberOfUserOpenSteps(final String userId) {
    final Query query = getSession().getNamedQuery("getNumberOfUserOpenSteps");

    query.setString("userId", userId);
    return ((Long) query.uniqueResult()).intValue();
  }

  @Override
  public int getNumberOfUserOverdueSteps(final String userId, final Date currentDate) {
    final Query query = getSession().getNamedQuery("getNumberOfUserOverdueSteps");

    query.setString("userId", userId);
    query.setLong("currentDate", currentDate.getTime());
    return ((Long) query.uniqueResult()).intValue();
  }

  @Override
  public int getNumberOfUserStepsAtRisk(final String userId, final Date currentDate, final Date atRisk) {
    final Query query = getSession().getNamedQuery("getNumberOfUserStepsAtRisk");

    query.setString("userId", userId);
    query.setLong("atRisk", atRisk.getTime());
    query.setLong("currentDate", currentDate.getTime());
    return ((Long) query.uniqueResult()).intValue();
  }

  @Override
  public int getNumberOfFinishedSteps(final int priority, final Date since) {
    final Query query = getSession().getNamedQuery("getNumberOfFinishedSteps");

    query.setInteger("priority", priority);
    query.setLong("since", since.getTime());
    return ((Long) query.uniqueResult()).intValue();
  }

  @Override
  public int getNumberOfOpenSteps(final int priority) {
    final Query query = getSession().getNamedQuery("getNumberOfPriorityOpenSteps");

    query.setInteger("priority", priority);
    return ((Long) query.uniqueResult()).intValue();
  }

  @Override
  public int getNumberOfUserFinishedSteps(final String userId, final int priority, final Date since) {
    final Query query = getSession().getNamedQuery("getNumberOfUserFinishedSteps");

    query.setInteger("priority", priority);
    query.setLong("since", since.getTime());
    query.setString("userId", userId);
    return ((Long) query.uniqueResult()).intValue();
  }

  @Override
  public int getNumberOfUserOpenSteps(final String userId, final int priority) {
    final Query query = getSession().getNamedQuery("getNumberOfPriorityUserOpenSteps");

    query.setInteger("priority", priority);
    query.setString("userId", userId);
    return ((Long) query.uniqueResult()).intValue();
  }

  @Override
  public Set<IncomingEventInstance> getIncomingEvents(final ProcessInstanceUUID instanceUUID) {
    final Set<IncomingEventInstance> result = new HashSet<IncomingEventInstance>();
    final Query query = getSession().getNamedQuery("getInstanceIncomingEvents");

    query.setString("instanceUUID", instanceUUID.getValue());
    CollectionUtils.addAll(result, query.iterate());
    return result;
  }

  @Override
  public IncomingEventInstance getIncomingEvent(final ProcessInstanceUUID instanceUUID, final String name) {
    final Query query = getSession().getNamedQuery("getUniqueInstanceIncomingEvent");

    query.setString("instanceUUID", instanceUUID.getValue());
    query.setString("eventName", name);
    return (IncomingEventInstance) query.uniqueResult();
  }

  @Override
  public Set<IncomingEventInstance> getIncomingEvents(final ActivityDefinitionUUID activityUUID) {
    final Set<IncomingEventInstance> result = new HashSet<IncomingEventInstance>();
    final Query query = getSession().getNamedQuery("getActivityDefinitionIncomingEvents");

    query.setString("activityUUID", activityUUID.getValue());
    CollectionUtils.addAll(result, query.iterate());
    return result;
  }

  @Override
  public Set<IncomingEventInstance> getIncomingEvents(final ActivityInstanceUUID activityUUID) {
    final Set<IncomingEventInstance> result = new HashSet<IncomingEventInstance>();
    final Query query = getSession().getNamedQuery("getActivityInstanceIncomingEvents");

    query.setString("activityUUID", activityUUID.getValue());
    CollectionUtils.addAll(result, query.iterate());
    return result;
  }

  @Override
  public Set<IncomingEventInstance> getBoundaryIncomingEvents(final ActivityInstanceUUID activityUUID) {
    final Set<IncomingEventInstance> result = new HashSet<IncomingEventInstance>();
    final Query query = getSession().getNamedQuery("getActivityBoundaryIncomingEvents");

    query.setString("activityUUID", activityUUID.getValue());
    CollectionUtils.addAll(result, query.iterate());
    return result;
  }

  @Override
  public Set<OutgoingEventInstance> getBoundaryOutgoingEvents(final ActivityInstanceUUID activityUUID) {
    final Set<OutgoingEventInstance> result = new HashSet<OutgoingEventInstance>();
    final Query query = getSession().getNamedQuery("getActivityBoundaryOutgoingEvents");

    query.setString("activityUUID", activityUUID.getValue());
    CollectionUtils.addAll(result, query.iterate());
    return result;
  }

  @Override
  public Set<OutgoingEventInstance> getOutgoingEvents(final ProcessInstanceUUID instanceUUID) {
    final Set<OutgoingEventInstance> result = new HashSet<OutgoingEventInstance>();
    final Query query = getSession().getNamedQuery("getInstanceOutgoingEvents");

    query.setString("instanceUUID", instanceUUID.getValue());
    CollectionUtils.addAll(result, query.iterate());
    return result;
  }

  @Override
  public Set<OutgoingEventInstance> getOutgoingEvents() {
    final Set<OutgoingEventInstance> result = new HashSet<OutgoingEventInstance>();
    final Query query = getSession().getNamedQuery("getOutgoingEvents");

    CollectionUtils.addAll(result, query.iterate());
    return result;
  }

  @Override
  public Set<OutgoingEventInstance> getOutgoingEvents(final String eventName, final String toProcessName,
      final String toActivityName, final ActivityInstanceUUID activityUUID) {
    final Set<OutgoingEventInstance> result = new HashSet<OutgoingEventInstance>();
    Query query = null;
    query = getSession().getNamedQuery("getOutgoingEventsWithUUID");
    query.setString("processName", toProcessName);
    query.setString("activityName", toActivityName);
    query.setString("activityUUID", activityUUID == null ? null : activityUUID.getValue());
    query.setString("name", eventName);

    CollectionUtils.addAll(result, query.iterate());
    return result;
  }

  @Override
  public Set<IncomingEventInstance> getIncomingEvents(final String eventName, final String toProcessName,
      final String toActivityName, final ActivityInstanceUUID actiivtyUUID) {
    final Set<IncomingEventInstance> result = new HashSet<IncomingEventInstance>();
    final Query query = getSession().getNamedQuery("getIncomingEventsWithUUID");
    query.setString("name", eventName);
    query.setString("processName", toProcessName);
    query.setString("activityName", toActivityName);
    query.setString("activityUUID", actiivtyUUID.getValue());

    CollectionUtils.addAll(result, query.iterate());
    return result;
  }

  @Override
  public Set<IncomingEventInstance> getSignalIncomingEvents(final String signal) {
    final Set<IncomingEventInstance> result = new HashSet<IncomingEventInstance>();
    final Query query = getSession().getNamedQuery("getSignalIncomingEvents");
    query.setString("eventName", signal);

    CollectionUtils.addAll(result, query.iterate());
    return result;
  }

  @Override
  public Set<OutgoingEventInstance> getOverdueEvents() {
    final long currentTime = System.currentTimeMillis();
    final Query oeiQuery = getSession().getNamedQuery("getOverdueEvents");
    oeiQuery.setLong("current", currentTime);
    final Set<OutgoingEventInstance> result = new HashSet<OutgoingEventInstance>();
    CollectionUtils.addAll(result, oeiQuery.iterate());
    return result;
  }

  @Override
  public Set<IncomingEventInstance> getIncomingEvents() {
    final Set<IncomingEventInstance> result = new HashSet<IncomingEventInstance>();
    final Query query = getSession().getNamedQuery("getIncomingEvents");
    CollectionUtils.addAll(result, query.iterate());
    return result;
  }

  @Override
  public Set<IncomingEventInstance> getActivityIncomingEvents(final ActivityInstanceUUID activityUUID) {
    final Set<IncomingEventInstance> result = new HashSet<IncomingEventInstance>();
    final Query query = getSession().getNamedQuery("getActivityIncomingEvents");
    query.setString("activityUUID", activityUUID.getValue());

    CollectionUtils.addAll(result, query.iterate());
    return result;
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<EventCouple> getMessageEventCouples() {
    final Query query = getSession().getNamedQuery("getMessageEventCouples");
    query.setResultTransformer(Transformers.aliasToBean(EventCouple.class));
    return formatList(query.list());
  }

  @Override
  public IncomingEventInstance getIncomingEvent(final long incomingId) {
    final Query query = getSession().getNamedQuery("getIncomingEvent");
    query.setLong("id", incomingId);
    return (IncomingEventInstance) query.uniqueResult();
  }

  @Override
  public OutgoingEventInstance getOutgoingEvent(final long outgoingId) {
    final Query query = getSession().getNamedQuery("getOutgoingEvent");
    query.setLong("id", outgoingId);
    return (OutgoingEventInstance) query.uniqueResult();
  }

  @Override
  public Set<InternalProcessDefinition> getProcesses(final Set<ProcessDefinitionUUID> definitionUUIDs) {
    final Query query = getSession().getNamedQuery("getProcessesList");
    final Set<String> uuids = new HashSet<String>();
    for (final ProcessDefinitionUUID uuid : definitionUUIDs) {
      uuids.add(uuid.toString());
    }
    return executeSplittedQuery(InternalProcessDefinition.class, query, "definitionsUUIDs", uuids);
  }

  @Override
  public Set<InternalProcessDefinition> getProcesses(final Set<ProcessDefinitionUUID> definitionUUIDs,
      final ProcessState processState) {
    final Query query = getSession().getNamedQuery("getProcessesListByState");
    query.setParameter("state", processState);
    final Set<String> uuids = new HashSet<String>();
    for (final ProcessDefinitionUUID uuid : definitionUUIDs) {
      uuids.add(uuid.toString());
    }
    return executeSplittedQuery(InternalProcessDefinition.class, query, "definitionsUUIDs", uuids);
  }

  @Override
  public List<InternalProcessDefinition> getProcesses(final Set<ProcessDefinitionUUID> definitionUUIDs,
      final int fromIndex, final int pageSize) {
    final Query query = getSession().getNamedQuery("getAllProcessesList");

    query.setFirstResult(fromIndex);
    query.setMaxResults(pageSize);
    final Set<String> uuids = new HashSet<String>();
    for (final ProcessDefinitionUUID uuid : definitionUUIDs) {
      uuids.add(uuid.toString());
    }
    return executeSplittedQueryList(InternalProcessDefinition.class, query, "definitionsUUIDs", uuids);
  }

  @Override
  public List<InternalProcessDefinition> getProcesses(final Set<ProcessDefinitionUUID> definitionUUIDs,
      final int fromIndex, final int pageSize, final ProcessDefinitionCriterion pagingCriterion) {
    Query query = null;
    switch (pagingCriterion) {
      case NAME_ASC:
        query = getSession().getNamedQuery("getAllProcessesListOrderByNameAsc");
        break;

      case LABEL_ASC:
        query = getSession().getNamedQuery("getAllProcessesListOrderByLabelAsc");
        break;

      case VERSION_ASC:
        query = getSession().getNamedQuery("getAllProcessesListOrderByVersionAsc");
        break;

      case STATE_ASC:
        query = getSession().getNamedQuery("getAllProcessesListOrderByStateAsc");
        break;

      case NAME_DESC:
        query = getSession().getNamedQuery("getAllProcessesListOrderByNameDesc");
        break;

      case LABEL_DESC:
        query = getSession().getNamedQuery("getAllProcessesListOrderByLabelDesc");
        break;

      case VERSION_DESC:
        query = getSession().getNamedQuery("getAllProcessesListOrderByVersionDesc");
        break;

      case STATE_DESC:
        query = getSession().getNamedQuery("getAllProcessesListOrderByStateDesc");
        break;

      case DEFAULT:
        query = getSession().getNamedQuery("getAllProcessesList");
        break;
    }

    query.setFirstResult(fromIndex);
    query.setMaxResults(pageSize);
    final Set<String> uuids = new HashSet<String>();
    for (final ProcessDefinitionUUID uuid : definitionUUIDs) {
      uuids.add(uuid.toString());
    }
    return executeSplittedQueryList(InternalProcessDefinition.class, query, "definitionsUUIDs", uuids);
  }

  @Override
  public InternalProcessDefinition getLastProcess(final Set<ProcessDefinitionUUID> definitionUUIDs,
      final ProcessState processState) {
    final Query query = getSession().getNamedQuery("getLastProcessFromProcessSetAndState");
    query.setParameter("state", processState, DEFINITION_STATE_USER_TYPE);
    final Set<String> uuids = new HashSet<String>();
    for (final ProcessDefinitionUUID uuid : definitionUUIDs) {
      uuids.add(uuid.toString());
    }

    query.setMaxResults(1);
    final Set<InternalProcessDefinition> result = executeSplittedQuery(InternalProcessDefinition.class, query,
        "definitionsUUIDs", uuids);
    return result.iterator().next();
  }

  @Override
  public Set<InternalProcessInstance> getProcessInstances(final Set<ProcessDefinitionUUID> definitionUUIDs) {
    final Query query = getSession().getNamedQuery("getProcessInstancesFromDefinitionUUIDs");

    final Set<String> uuids = new HashSet<String>();
    for (final ProcessDefinitionUUID uuid : definitionUUIDs) {
      uuids.add(uuid.toString());
    }
    return executeSplittedQuery(InternalProcessInstance.class, query, "definitionsUUIDs", uuids);
  }

  @Override
  public Set<InternalProcessInstance> getUserInstances(final String userId,
      final Set<ProcessDefinitionUUID> definitionUUIDs) {
    final Query query = getSession().getNamedQuery("getUserInstancesFromDefinitionUUIDs");

    query.setString("userId", userId);
    final Set<String> uuids = new HashSet<String>();
    for (final ProcessDefinitionUUID uuid : definitionUUIDs) {
      uuids.add(uuid.toString());
    }
    return executeSplittedQuery(InternalProcessInstance.class, query, "definitionsUUIDs", uuids);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<InternalProcessInstance> getParentUserInstances(final String userId, final int fromIndex,
      final int pageSize, final Set<ProcessDefinitionUUID> definitionUUIDs) {
    final Query query = getSession().getNamedQuery("getParentUserInstancesPageFromDefinitionUUIDs");

    query.setFirstResult(fromIndex);
    query.setMaxResults(pageSize);
    query.setString("userId", userId);
    final Set<String> uuids = new HashSet<String>();
    for (final ProcessDefinitionUUID uuid : definitionUUIDs) {
      uuids.add(uuid.toString());
    }
    query.setParameterList("definitionsUUIDs", uuids);
    return formatList(query.list());
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<InternalProcessInstance> getParentUserInstances(final String userId, final int startingIndex,
      final int pageSize, final Set<ProcessDefinitionUUID> definitionUUIDs,
      final ProcessInstanceCriterion pagingCriterion) {
    Query query = null;
    switch (pagingCriterion) {
      case LAST_UPDATE_ASC:
        query = getSession().getNamedQuery("getParentUserInstancesPageFromDefinitionUUIDsOrderByLastUpdateAsc");
        break;
      case STARTED_DATE_ASC:
        query = getSession().getNamedQuery("getParentUserInstancesPageFromDefinitionUUIDsOrderByStartedDateAsc");
        break;
      case ENDED_DATE_ASC:
        query = getSession().getNamedQuery("getParentUserInstancesPageFromDefinitionUUIDsOrderByEndedDateAsc");
        break;
      case INSTANCE_NUMBER_ASC:
        query = getSession().getNamedQuery("getParentUserInstancesPageFromDefinitionUUIDsOrderByInstanceNumberAsc");
        break;
      case INSTANCE_UUID_ASC:
        query = getSession().getNamedQuery("getParentUserInstancesPageFromDefinitionUUIDsOrderByInstanceUUIDAsc");
        break;
      case LAST_UPDATE_DESC:
        query = getSession().getNamedQuery("getParentUserInstancesPageFromDefinitionUUIDsOrderByLastUpdateDesc");
        break;
      case STARTED_DATE_DESC:
        query = getSession().getNamedQuery("getParentUserInstancesPageFromDefinitionUUIDsOrderByStartedDateDesc");
        break;
      case ENDED_DATE_DESC:
        query = getSession().getNamedQuery("getParentUserInstancesPageFromDefinitionUUIDsOrderByEndedDateDesc");
        break;
      case INSTANCE_NUMBER_DESC:
        query = getSession().getNamedQuery("getParentUserInstancesPageFromDefinitionUUIDsOrderByInstanceNumberDesc");
        break;
      case INSTANCE_UUID_DESC:
        query = getSession().getNamedQuery("getParentUserInstancesPageFromDefinitionUUIDsOrderByInstanceUUIDDesc");
        break;
      case DEFAULT:
        query = getSession().getNamedQuery("getParentUserInstancesPageFromDefinitionUUIDs");
        break;
    }

    query.setFirstResult(startingIndex);
    query.setMaxResults(pageSize);
    query.setString("userId", userId);
    final Set<String> uuids = new HashSet<String>();
    for (final ProcessDefinitionUUID uuid : definitionUUIDs) {
      uuids.add(uuid.toString());
    }
    query.setParameterList("definitionsUUIDs", uuids);
    return formatList(query.list());
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<InternalProcessInstance> getParentProcessInstancesWithActiveUser(final String userId,
      final int fromIndex, final int pageSize, final Set<ProcessDefinitionUUID> visibleProcessUUIDs) {
    final Query query = getSession().getNamedQuery("getParentProcessInstancesWithActiveUserFromDefinitionUUIDs");

    query.setFirstResult(fromIndex);
    query.setMaxResults(pageSize);
    query.setString("userId", userId);
    final List<String> uuids = new ArrayList<String>();
    for (final ProcessDefinitionUUID uuid : visibleProcessUUIDs) {
      uuids.add(uuid.toString());
    }
    query.setParameterList("definitionsUUIDs", uuids);
    return formatList(query.list());
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<InternalProcessInstance> getParentProcessInstancesWithActiveUser(final String aUserId,
      final int aStartingIndex, final int aPageSize, final Set<ProcessDefinitionUUID> aVisibleProcessUUIDs,
      final ProcessInstanceCriterion pagingCriterion) {
    Query query = null;
    switch (pagingCriterion) {
      case LAST_UPDATE_ASC:
        query = getSession().getNamedQuery(
            "getParentProcessInstancesWithActiveUserFromDefinitionUUIDsOrderByLastUpdateAsc");
        break;
      case STARTED_DATE_ASC:
        query = getSession().getNamedQuery(
            "getParentProcessInstancesWithActiveUserFromDefinitionUUIDsOrderByStartedDateAsc");
        break;
      case ENDED_DATE_ASC:
        query = getSession().getNamedQuery(
            "getParentProcessInstancesWithActiveUserFromDefinitionUUIDsOrderByEndedDateAsc");
        break;
      case INSTANCE_NUMBER_ASC:
        query = getSession().getNamedQuery(
            "getParentProcessInstancesWithActiveUserFromDefinitionUUIDsOrderByInstanceNumberAsc");
        break;
      case INSTANCE_UUID_ASC:
        query = getSession().getNamedQuery(
            "getParentProcessInstancesWithActiveUserFromDefinitionUUIDsOrderByInstanceUUIDAsc");
        break;
      case LAST_UPDATE_DESC:
        query = getSession().getNamedQuery(
            "getParentProcessInstancesWithActiveUserFromDefinitionUUIDsOrderByLastUpdateDesc");
        break;
      case STARTED_DATE_DESC:
        query = getSession().getNamedQuery(
            "getParentProcessInstancesWithActiveUserFromDefinitionUUIDsOrderByStartedDateDesc");
        break;
      case ENDED_DATE_DESC:
        query = getSession().getNamedQuery(
            "getParentProcessInstancesWithActiveUserFromDefinitionUUIDsOrderByEndedDateDesc");
        break;
      case INSTANCE_NUMBER_DESC:
        query = getSession().getNamedQuery(
            "getParentProcessInstancesWithActiveUserFromDefinitionUUIDsOrderByInstanceNumberDesc");
        break;
      case INSTANCE_UUID_DESC:
        query = getSession().getNamedQuery(
            "getParentProcessInstancesWithActiveUserFromDefinitionUUIDsOrderByInstanceUUIDDesc");
        break;
      case DEFAULT:
        query = getSession().getNamedQuery("getParentProcessInstancesWithActiveUserFromDefinitionUUIDs");
        break;
    }

    query.setFirstResult(aStartingIndex);
    query.setMaxResults(aPageSize);
    query.setString("userId", aUserId);

    final List<String> uuids = new ArrayList<String>();
    for (final ProcessDefinitionUUID uuid : aVisibleProcessUUIDs) {
      uuids.add(uuid.toString());
    }
    query.setParameterList("definitionsUUIDs", uuids);
    return formatList(query.list());
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<InternalProcessInstance> getParentProcessInstancesWithActiveUser(final String userId,
      final int fromIndex, final int pageSize) {
    final Query query = getSession().getNamedQuery("getParentProcessInstancesWithActiveUser");
    query.setFirstResult(fromIndex);
    query.setMaxResults(pageSize);
    query.setString("userId", userId);
    return formatList(query.list());
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<InternalProcessInstance> getParentProcessInstancesWithActiveUser(final String aUserId,
      final int aStartingIndex, final int aPageSize, final ProcessInstanceCriterion pagingCriterion) {
    Query query = null;
    switch (pagingCriterion) {
      case LAST_UPDATE_ASC:
        query = getSession().getNamedQuery("getParentProcessInstancesWithActiveUserOrderByLastUpdateAsc");
        break;
      case STARTED_DATE_ASC:
        query = getSession().getNamedQuery("getParentProcessInstancesWithActiveUserOrderByStartedDateAsc");
        break;
      case ENDED_DATE_ASC:
        query = getSession().getNamedQuery("getParentProcessInstancesWithActiveUserOrderByEndedDateAsc");
        break;
      case INSTANCE_NUMBER_ASC:
        query = getSession().getNamedQuery("getParentProcessInstancesWithActiveUserOrderByInstanceNumberAsc");
        break;
      case INSTANCE_UUID_ASC:
        query = getSession().getNamedQuery("getParentProcessInstancesWithActiveUserOrderByInstanceUUIDAsc");
        break;
      case LAST_UPDATE_DESC:
        query = getSession().getNamedQuery("getParentProcessInstancesWithActiveUserOrderByLastUpdateDesc");
        break;
      case STARTED_DATE_DESC:
        query = getSession().getNamedQuery("getParentProcessInstancesWithActiveUserOrderByStartedDateDesc");
        break;
      case ENDED_DATE_DESC:
        query = getSession().getNamedQuery("getParentProcessInstancesWithActiveUserOrderByEndedDateDesc");
        break;
      case INSTANCE_NUMBER_DESC:
        query = getSession().getNamedQuery("getParentProcessInstancesWithActiveUserOrderByInstanceNumberDesc");
        break;
      case INSTANCE_UUID_DESC:
        query = getSession().getNamedQuery("getParentProcessInstancesWithActiveUserOrderByInstanceUUIDDesc");
        break;
      case DEFAULT:
        query = getSession().getNamedQuery("getParentProcessInstancesWithActiveUser");
        break;
    }

    query.setFirstResult(aStartingIndex);
    query.setMaxResults(aPageSize);
    query.setString("userId", aUserId);
    return formatList(query.list());
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<InternalProcessInstance> getParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate(
      final String userId, final Date currentDate, final Date atRisk, final int startingIndex, final int pageSize,
      final Set<ProcessDefinitionUUID> visibleProcessUUIDs) {
    final Query query = getSession().getNamedQuery(
        "getParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDateFromDefinitionUUIDs");

    query.setString("userId", userId);
    final List<String> uuids = new ArrayList<String>();
    for (final ProcessDefinitionUUID uuid : visibleProcessUUIDs) {
      uuids.add(uuid.toString());
    }
    query.setParameterList("definitionsUUIDs", uuids);
    query.setLong("atRisk", atRisk.getTime());
    query.setLong("currentDate", currentDate.getTime());
    final List<ProcessInstanceUUID> results = query.list();
    if (results == null) {
      return Collections.emptyList();
    }
    return new ArrayList<InternalProcessInstance>(getProcessInstances(new HashSet<ProcessInstanceUUID>(results),
        startingIndex, pageSize));
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<InternalProcessInstance> getParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate(
      final String userId, final Date currentDate, final Date atRisk, final int startingIndex, final int pageSize,
      final Set<ProcessDefinitionUUID> visibleProcessUUIDs, final ProcessInstanceCriterion pagingCriterion) {
    final Query query = getSession().getNamedQuery(
        "getParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDateFromDefinitionUUIDs");

    query.setString("userId", userId);
    final List<String> uuids = new ArrayList<String>();
    for (final ProcessDefinitionUUID uuid : visibleProcessUUIDs) {
      uuids.add(uuid.toString());
    }
    query.setParameterList("definitionsUUIDs", uuids);
    query.setLong("atRisk", atRisk.getTime());
    query.setLong("currentDate", currentDate.getTime());
    final List<ProcessInstanceUUID> results = query.list();
    if (results == null) {
      return Collections.emptyList();
    }
    return getProcessInstancesWithInstanceUUIDs(new HashSet<ProcessInstanceUUID>(results), startingIndex, pageSize,
        pagingCriterion);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<InternalProcessInstance> getParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate(
      final String userId, final Date currentDate, final Date atRisk, final int startingIndex, final int pageSize) {
    final Query query = getSession().getNamedQuery(
        "getParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate");

    query.setString("userId", userId);
    query.setLong("atRisk", atRisk.getTime());
    query.setLong("currentDate", currentDate.getTime());
    final List<ProcessInstanceUUID> results = query.list();
    if (results == null) {
      return Collections.emptyList();
    }
    return new ArrayList<InternalProcessInstance>(getProcessInstances(new HashSet<ProcessInstanceUUID>(results),
        startingIndex, pageSize));

  }

  @Override
  @SuppressWarnings("unchecked")
  public List<InternalProcessInstance> getParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate(
      final String userId, final Date currentDate, final Date atRisk, final int startingIndex, final int pageSize,
      final ProcessInstanceCriterion pagingCriterion) {
    final Query query = getSession().getNamedQuery(
        "getParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate");

    query.setString("userId", userId);
    query.setLong("atRisk", atRisk.getTime());
    query.setLong("currentDate", currentDate.getTime());
    final List<ProcessInstanceUUID> results = query.list();
    if (results == null) {
      return Collections.emptyList();
    }
    return new ArrayList<InternalProcessInstance>(getProcessInstancesWithInstanceUUIDs(
        new HashSet<ProcessInstanceUUID>(results), startingIndex, pageSize, pagingCriterion));
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<InternalProcessInstance> getParentProcessInstancesWithOverdueTasks(final String userId,
      final Date currentDate, final int startingIndex, final int pageSize,
      final Set<ProcessDefinitionUUID> visibleProcessUUIDs) {
    final Query query = getSession().getNamedQuery("getParentProcessInstancesWithOverdueTasksFromDefinitionUUIDs");

    query.setString("userId", userId);
    final List<String> uuids = new ArrayList<String>();
    for (final ProcessDefinitionUUID uuid : visibleProcessUUIDs) {
      uuids.add(uuid.toString());
    }
    query.setParameterList("definitionsUUIDs", uuids);
    query.setLong("currentDate", currentDate.getTime());
    final List<ProcessInstanceUUID> results = query.list();
    if (results == null) {
      return Collections.emptyList();
    }
    return getProcessInstances(new HashSet<ProcessInstanceUUID>(results), startingIndex, pageSize);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<InternalProcessInstance> getParentProcessInstancesWithOverdueTasks(final String userId,
      final Date currentDate, final int startingIndex, final int pageSize,
      final Set<ProcessDefinitionUUID> visibleProcessUUIDs, final ProcessInstanceCriterion pagingCriterion) {
    final Query query = getSession().getNamedQuery("getParentProcessInstancesWithOverdueTasksFromDefinitionUUIDs");

    query.setString("userId", userId);
    final List<String> uuids = new ArrayList<String>();
    for (final ProcessDefinitionUUID uuid : visibleProcessUUIDs) {
      uuids.add(uuid.toString());
    }
    query.setParameterList("definitionsUUIDs", uuids);
    query.setLong("currentDate", currentDate.getTime());
    final List<ProcessInstanceUUID> results = query.list();
    if (results == null) {
      return Collections.emptyList();
    }
    return getProcessInstancesWithInstanceUUIDs(new HashSet<ProcessInstanceUUID>(results), startingIndex, pageSize,
        pagingCriterion);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<InternalProcessInstance> getParentProcessInstancesWithOverdueTasks(final String userId,
      final Date currentDate, final int startingIndex, final int pageSize) {
    final Query query = getSession().getNamedQuery("getParentProcessInstancesWithOverdueTasks");

    query.setString("userId", userId);
    query.setLong("currentDate", currentDate.getTime());
    final List<ProcessInstanceUUID> results = query.list();
    if (results == null) {
      return Collections.emptyList();
    }
    return getProcessInstances(new HashSet<ProcessInstanceUUID>(results), startingIndex, pageSize);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<InternalProcessInstance> getParentProcessInstancesWithOverdueTasks(final String userId,
      final Date currentDate, final int startingIndex, final int pageSize,
      final ProcessInstanceCriterion pagingCriterion) {
    final Query query = getSession().getNamedQuery("getParentProcessInstancesWithOverdueTasks");

    query.setString("userId", userId);
    query.setLong("currentDate", currentDate.getTime());
    final List<ProcessInstanceUUID> results = query.list();
    if (results == null) {
      return Collections.emptyList();
    }
    return getProcessInstancesWithInstanceUUIDs(new HashSet<ProcessInstanceUUID>(results), startingIndex, pageSize,
        pagingCriterion);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<InternalProcessInstance> getParentProcessInstancesWithInvolvedUser(final String userId,
      final int fromIndex, final int pageSize, final Set<ProcessDefinitionUUID> visibleProcessUUIDs) {
    final Query query = getSession().getNamedQuery("getParentProcessInstancesWithInvolvedUserFromDefinitionUUIDs");

    query.setFirstResult(fromIndex);
    query.setMaxResults(pageSize);
    query.setString("userId", userId);
    final List<String> uuids = new ArrayList<String>();
    for (final ProcessDefinitionUUID uuid : visibleProcessUUIDs) {
      uuids.add(uuid.toString());
    }
    query.setParameterList("definitionsUUIDs", uuids);
    return formatList(query.list());
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<InternalProcessInstance> getParentProcessInstancesWithInvolvedUser(final String userId,
      final int startingIndex, final int pageSize, final Set<ProcessDefinitionUUID> visibleProcessUUIDs,
      final ProcessInstanceCriterion pagingCriterion) {
    Query query = null;
    switch (pagingCriterion) {
      case LAST_UPDATE_ASC:
        query = getSession().getNamedQuery(
            "getParentProcessInstancesWithInvolvedUserFromDefinitionUUIDsOrderByLastUpdateAsc");
        break;
      case STARTED_DATE_ASC:
        query = getSession().getNamedQuery(
            "getParentProcessInstancesWithInvolvedUserFromDefinitionUUIDsOrderByStartedDateAsc");
        break;
      case ENDED_DATE_ASC:
        query = getSession().getNamedQuery(
            "getParentProcessInstancesWithInvolvedUserFromDefinitionUUIDsOrderByEndedDateAsc");
        break;
      case INSTANCE_NUMBER_ASC:
        query = getSession().getNamedQuery(
            "getParentProcessInstancesWithInvolvedUserFromDefinitionUUIDsOrderByInstanceNumberAsc");
        break;
      case INSTANCE_UUID_ASC:
        query = getSession().getNamedQuery(
            "getParentProcessInstancesWithInvolvedUserFromDefinitionUUIDsOrderByInstanceUUIDAsc");
        break;
      case LAST_UPDATE_DESC:
        query = getSession().getNamedQuery(
            "getParentProcessInstancesWithInvolvedUserFromDefinitionUUIDsOrderByLastUpdateDesc");
        break;
      case STARTED_DATE_DESC:
        query = getSession().getNamedQuery(
            "getParentProcessInstancesWithInvolvedUserFromDefinitionUUIDsOrderByStartedDateDesc");
        break;
      case ENDED_DATE_DESC:
        query = getSession().getNamedQuery(
            "getParentProcessInstancesWithInvolvedUserFromDefinitionUUIDsOrderByEndedDateDesc");
        break;
      case INSTANCE_NUMBER_DESC:
        query = getSession().getNamedQuery(
            "getParentProcessInstancesWithInvolvedUserFromDefinitionUUIDsOrderByInstanceNumberDesc");
        break;
      case INSTANCE_UUID_DESC:
        query = getSession().getNamedQuery(
            "getParentProcessInstancesWithInvolvedUserFromDefinitionUUIDsOrderByInstanceUUIDDesc");
        break;
      case DEFAULT:
        query = getSession().getNamedQuery("getParentProcessInstancesWithInvolvedUserFromDefinitionUUIDs");
        break;
    }

    query.setFirstResult(startingIndex);
    query.setMaxResults(pageSize);
    query.setString("userId", userId);
    final List<String> uuids = new ArrayList<String>();
    for (final ProcessDefinitionUUID uuid : visibleProcessUUIDs) {
      uuids.add(uuid.toString());
    }
    query.setParameterList("definitionsUUIDs", uuids);
    return formatList(query.list());
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<InternalProcessInstance> getParentProcessInstancesWithInvolvedUser(final String userId,
      final int fromIndex, final int pageSize) {
    final Query query = getSession().getNamedQuery("getParentProcessInstancesWithInvolvedUser");

    query.setFirstResult(fromIndex);
    query.setMaxResults(pageSize);
    query.setString("userId", userId);
    return formatList(query.list());
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<InternalProcessInstance> getParentProcessInstancesWithInvolvedUser(final String aUserId,
      final int aStartingIndex, final int aPageSize, final ProcessInstanceCriterion pagingCriterion) {
    Query query = null;
    switch (pagingCriterion) {
      case LAST_UPDATE_ASC:
        query = getSession().getNamedQuery("getParentProcessInstancesWithInvolvedUserOrderByLastUpdateAsc");
        break;
      case STARTED_DATE_ASC:
        query = getSession().getNamedQuery("getParentProcessInstancesWithInvolvedUserOrderByStartedDateAsc");
        break;
      case ENDED_DATE_ASC:
        query = getSession().getNamedQuery("getParentProcessInstancesWithInvolvedUserOrderByEndedDateAsc");
        break;
      case INSTANCE_NUMBER_ASC:
        query = getSession().getNamedQuery("getParentProcessInstancesWithInvolvedUserOrderByInstanceNumberAsc");
        break;
      case INSTANCE_UUID_ASC:
        query = getSession().getNamedQuery("getParentProcessInstancesWithInvolvedUserOrderByInstanceUUIDAsc");
        break;
      case LAST_UPDATE_DESC:
        query = getSession().getNamedQuery("getParentProcessInstancesWithInvolvedUserOrderByLastUpdateDesc");
        break;
      case STARTED_DATE_DESC:
        query = getSession().getNamedQuery("getParentProcessInstancesWithInvolvedUserOrderByStartedDateDesc");
        break;
      case ENDED_DATE_DESC:
        query = getSession().getNamedQuery("getParentProcessInstancesWithInvolvedUserOrderByEndedDateDesc");
        break;
      case INSTANCE_NUMBER_DESC:
        query = getSession().getNamedQuery("getParentProcessInstancesWithInvolvedUserOrderByInstanceNumberDesc");
        break;
      case INSTANCE_UUID_DESC:
        query = getSession().getNamedQuery("getParentProcessInstancesWithInvolvedUserOrderByInstanceUUIDDesc");
        break;
      case DEFAULT:
        query = getSession().getNamedQuery("getParentProcessInstancesWithInvolvedUser");
        break;
    }

    query.setFirstResult(aStartingIndex);
    query.setMaxResults(aPageSize);
    query.setString("userId", aUserId);
    return formatList(query.list());
  }

  @Override
  public Integer getNumberOfParentProcessInstancesWithActiveUser(final String userId,
      final Set<ProcessDefinitionUUID> visibleProcessUUIDs) {
    final Query query = getSession()
        .getNamedQuery("getNumberOfParentProcessInstancesWithActiveUserFromDefinitionUUIDs");
    query.setString("userId", userId);
    final List<String> uuids = new ArrayList<String>();
    for (final ProcessDefinitionUUID uuid : visibleProcessUUIDs) {
      uuids.add(uuid.toString());
    }
    query.setParameterList("definitionsUUIDs", uuids);
    return ((Long) query.uniqueResult()).intValue();

  }

  @Override
  public Integer getNumberOfParentProcessInstancesWithActiveUser(final String userId) {
    final Query query = getSession().getNamedQuery("getNumberOfParentProcessInstancesWithActiveUser");
    query.setString("userId", userId);
    return ((Long) query.uniqueResult()).intValue();
  }

  @Override
  public Integer getNumberOfParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate(final String userId,
      final Date currentDate, final Date atRisk, final Set<ProcessDefinitionUUID> visibleProcessUUIDs) {
    final Query query = getSession().getNamedQuery(
        "getNumberOfParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDateFromDefinitionUUIDs");
    query.setString("userId", userId);
    query.setLong("atRisk", atRisk.getTime());
    query.setLong("currentDate", currentDate.getTime());
    final List<String> uuids = new ArrayList<String>();
    for (final ProcessDefinitionUUID uuid : visibleProcessUUIDs) {
      uuids.add(uuid.toString());
    }
    query.setParameterList("definitionsUUIDs", uuids);
    return ((Long) query.uniqueResult()).intValue();
  }

  @Override
  public Integer getNumberOfParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate(final String userId,
      final Date currentDate, final Date atRisk) {
    final Query query = getSession().getNamedQuery(
        "getNumberOfParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate");
    query.setString("userId", userId);
    query.setLong("atRisk", atRisk.getTime());
    query.setLong("currentDate", currentDate.getTime());
    return ((Long) query.uniqueResult()).intValue();
  }

  @Override
  public Integer getNumberOfParentProcessInstancesWithOverdueTasks(final String userId, final Date currentDate,
      final Set<ProcessDefinitionUUID> visibleProcessUUIDs) {
    final Query query = getSession().getNamedQuery(
        "getNumberOfParentProcessInstancesWithOverdueTasksFromDefinitionUUIDs");
    query.setString("userId", userId);
    query.setLong("currentDate", currentDate.getTime());
    final List<String> uuids = new ArrayList<String>();
    for (final ProcessDefinitionUUID uuid : visibleProcessUUIDs) {
      uuids.add(uuid.toString());
    }
    query.setParameterList("definitionsUUIDs", uuids);
    return ((Long) query.uniqueResult()).intValue();
  }

  @Override
  public Integer getNumberOfParentProcessInstancesWithOverdueTasks(final String userId, final Date currentDate) {
    final Query query = getSession().getNamedQuery("getNumberOfParentProcessInstancesWithOverdueTasks");
    query.setString("userId", userId);
    query.setLong("currentDate", currentDate.getTime());
    return ((Long) query.uniqueResult()).intValue();
  }

  @Override
  public Integer getNumberOfParentProcessInstancesWithInvolvedUser(final String userId,
      final Set<ProcessDefinitionUUID> visibleProcessUUIDs) {
    final Query query = getSession().getNamedQuery(
        "getNumberOfParentProcessInstancesWithInvolvedUserFromDefinitionUUIDs");
    query.setString("userId", userId);
    final List<String> uuids = new ArrayList<String>();
    for (final ProcessDefinitionUUID uuid : visibleProcessUUIDs) {
      uuids.add(uuid.toString());
    }
    query.setParameterList("definitionsUUIDs", uuids);
    return ((Long) query.uniqueResult()).intValue();
  }

  @Override
  public Integer getNumberOfParentProcessInstancesWithInvolvedUser(final String userId) {
    final Query query = getSession().getNamedQuery("getNumberOfParentProcessInstancesWithInvolvedUser");
    query.setString("userId", userId);
    return ((Long) query.uniqueResult()).intValue();
  }

  @Override
  public Integer getNumberOfParentProcessInstancesWithStartedBy(final String userId,
      final Set<ProcessDefinitionUUID> visibleProcessUUIDs) {
    final Query query = getSession().getNamedQuery("getNumberOfParentProcessInstancesWithStartedByFromDefinitionUUIDs");
    query.setString("userId", userId);
    final List<String> uuids = new ArrayList<String>();
    for (final ProcessDefinitionUUID uuid : visibleProcessUUIDs) {
      uuids.add(uuid.toString());
    }
    query.setParameterList("definitionsUUIDs", uuids);
    return ((Long) query.uniqueResult()).intValue();
  }

  @Override
  public Integer getNumberOfParentProcessInstancesWithStartedBy(final String userId) {
    final Query query = getSession().getNamedQuery("getNumberOfParentProcessInstancesWithStartedBy");
    query.setString("userId", userId);
    return ((Long) query.uniqueResult()).intValue();
  }

  @Override
  public int getNumberOfProcessInstances(final Set<ProcessDefinitionUUID> definitionUUIDs) {
    final Query query = getSession().getNamedQuery("getNumberOfProcessInstancesFromDefinitionUUIDs");
    final Set<String> uuids = new HashSet<String>();
    for (final ProcessDefinitionUUID uuid : definitionUUIDs) {
      uuids.add(uuid.toString());
    }
    query.setParameterList("definitionsUUIDs", uuids);
    return ((Long) query.uniqueResult()).intValue();
  }

  @Override
  public int getNumberOfParentProcessInstances(final Set<ProcessDefinitionUUID> definitionUUIDs) {
    final Query query = getSession().getNamedQuery("getNumberOfParentProcessInstancesFromDefinitionUUIDs");
    final Set<String> uuids = new HashSet<String>();
    for (final ProcessDefinitionUUID uuid : definitionUUIDs) {
      uuids.add(uuid.toString());
    }
    query.setParameterList("definitionsUUIDs", uuids);
    return ((Long) query.uniqueResult()).intValue();
  }

  @Override
  @SuppressWarnings("unchecked")
  public Set<InternalProcessInstance> getProcessInstancesWithTaskState(final Collection<ActivityState> activityStates,
      final Set<ProcessDefinitionUUID> definitionUUIDs) {
    final Query getActivitiesQuery = getSession()
        .getNamedQuery("findActivityInstancesWithTaskStateAndDefinitionsUUIDs");
    getActivitiesQuery.setParameterList("activityStates", activityStates, ACTIVITY_STATE_USER_TYPE);
    List<String> uuids = new ArrayList<String>();
    for (final ProcessDefinitionUUID uuid : definitionUUIDs) {
      uuids.add(uuid.toString());
    }
    getActivitiesQuery.setParameterList("definitionsUUIDs", uuids);
    uuids = getActivitiesQuery.list();

    final Set<InternalProcessInstance> processInsts = new HashSet<InternalProcessInstance>();
    if (uuids.isEmpty()) {
      return processInsts;
    }
    final Query query = getSession().getNamedQuery("findMatchingProcessInstances");
    return executeSplittedQuery(InternalProcessInstance.class, query, "instanceUUIDs", uuids);
  }

  @Override
  @SuppressWarnings("unchecked")
  public Set<InternalProcessInstance> getProcessInstancesWithInstanceStates(
      final Collection<InstanceState> instanceStates, final Set<ProcessDefinitionUUID> visibleProcessUUIDs) {
    final Query getProcessInstancesQuery = getSession().getNamedQuery(
        "ProcessInstancesWithInstanceStatesAndDefinitionsUUIDs");
    getProcessInstancesQuery.setParameterList("instanceStates", instanceStates, INSTANCE_STATE_USER_TYPE);
    List<String> uuids = new ArrayList<String>();
    for (final ProcessDefinitionUUID uuid : visibleProcessUUIDs) {
      uuids.add(uuid.toString());
    }
    getProcessInstancesQuery.setParameterList("definitionsUUIDs", uuids);
    uuids = getProcessInstancesQuery.list();

    final Set<InternalProcessInstance> processInsts = new HashSet<InternalProcessInstance>();
    if (uuids.isEmpty()) {
      return processInsts;
    }

    final Query query = getSession().getNamedQuery("findMatchingProcessInstances");
    return executeSplittedQuery(InternalProcessInstance.class, query, "instanceUUIDs", uuids);
  }

  @Override
  public TaskInstance getOneTask(final String userId, final ActivityState taskState,
      final Set<ProcessDefinitionUUID> definitionUUIDs) {
    final Query query = getSession().getNamedQuery("getOneTaskOfProcessFromDefinitionUUIDs");
    query.setString("userId", userId);
    final List<String> uuids = new ArrayList<String>();
    for (final ProcessDefinitionUUID uuid : definitionUUIDs) {
      uuids.add(uuid.toString());
    }
    query.setParameterList("definitionsUUIDs", uuids);
    query.setParameter("state", taskState, ACTIVITY_STATE_USER_TYPE);
    query.setMaxResults(1);
    return (TaskInstance) query.uniqueResult();
  }

  @Override
  public List<InternalProcessInstance> getProcessInstances(final Set<ProcessDefinitionUUID> definitionUUIDs,
      final int fromIndex, final int pageSize) {
    final Query query = getSession().getNamedQuery("getProcessInstancesWithDefinitionUUIDs");
    query.setFirstResult(fromIndex);
    query.setMaxResults(pageSize);
    final Set<String> uuids = new HashSet<String>();
    for (final ProcessDefinitionUUID uuid : definitionUUIDs) {
      uuids.add(uuid.toString());
    }
    return executeSplittedQueryList(InternalProcessInstance.class, query, "definitionsUUIDs", uuids);
  }

  @Override
  public List<InternalProcessInstance> getProcessInstances(final Set<ProcessDefinitionUUID> definitionUUIDs,
      final int fromIndex, final int pageSize, final ProcessInstanceCriterion pagingCriterion) {
    Query query = null;
    switch (pagingCriterion) {
      case LAST_UPDATE_ASC:
        query = getSession().getNamedQuery("getProcessInstancesWithDefinitionUUIDsOrderByLastUpdateAsc");
        break;
      case STARTED_DATE_ASC:
        query = getSession().getNamedQuery("getProcessInstancesWithDefinitionUUIDsOrderByStartedDateAsc");
        break;
      case ENDED_DATE_ASC:
        query = getSession().getNamedQuery("getProcessInstancesWithDefinitionUUIDsOrderByEndedDateAsc");
        break;
      case INSTANCE_NUMBER_ASC:
        query = getSession().getNamedQuery("getProcessInstancesWithDefinitionUUIDsOrderByInstanceNumberAsc");
        break;
      case INSTANCE_UUID_ASC:
        query = getSession().getNamedQuery("getProcessInstancesWithDefinitionUUIDsOrderByInstanceUUIDAsc");
        break;
      case LAST_UPDATE_DESC:
        query = getSession().getNamedQuery("getProcessInstancesWithDefinitionUUIDsOrderByLastUpdateDesc");
        break;
      case STARTED_DATE_DESC:
        query = getSession().getNamedQuery("getProcessInstancesWithDefinitionUUIDsOrderByStartedDateDesc");
        break;
      case ENDED_DATE_DESC:
        query = getSession().getNamedQuery("getProcessInstancesWithDefinitionUUIDsOrderByEndedDateDesc");
        break;
      case INSTANCE_NUMBER_DESC:
        query = getSession().getNamedQuery("getProcessInstancesWithDefinitionUUIDsOrderByInstanceNumberDesc");
        break;
      case INSTANCE_UUID_DESC:
        query = getSession().getNamedQuery("getProcessInstancesWithDefinitionUUIDsOrderByInstanceUUIDDesc");
        break;
      case DEFAULT:
        query = getSession().getNamedQuery("getProcessInstancesWithDefinitionUUIDs");
        break;
    }

    query.setFirstResult(fromIndex);
    query.setMaxResults(pageSize);
    final Set<String> uuids = new HashSet<String>();
    for (final ProcessDefinitionUUID uuid : definitionUUIDs) {
      uuids.add(uuid.toString());
    }
    return executeSplittedQueryList(InternalProcessInstance.class, query, "definitionsUUIDs", uuids);
  }

  @Override
  public Rule findRuleByName(final String name) {
    final Query query = getSession().getNamedQuery("getRuleByName");
    query.setString("name", name);
    query.setMaxResults(1);
    return (Rule) query.uniqueResult();
  }

  @Override
  public Rule getRule(final String ruleUUID) {
    final Query query = getSession().getNamedQuery("getRule");
    query.setString("ruleUUID", ruleUUID);
    query.setMaxResults(1);
    return (Rule) query.uniqueResult();
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<Rule> getRules() {
    final Query query = getSession().getNamedQuery("getAllRules");
    final List<Rule> results = query.list();
    if (results != null) {
      return results;
    } else {
      return Collections.emptyList();
    }
  }

  @Override
  @Deprecated
  @SuppressWarnings("unchecked")
  public Set<Rule> findRulesByNames(final Set<String> rulesNames) {
    final Query query = getSession().getNamedQuery("findRulesByNames");
    query.setParameterList("names", rulesNames);
    final List<Rule> results = query.list();
    if (results != null) {
      return new HashSet<Rule>(results);
    } else {
      return Collections.emptySet();
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<Rule> getRules(final Collection<String> ruleUUIDs) {
    final Query query = getSession().getNamedQuery("getRules");
    query.setParameterList("uuids", ruleUUIDs);
    return formatList(query.list());
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<Rule> getAllApplicableRules(final String userUUID, final Collection<String> roleUUIDs,
      final Collection<String> groupUUIDs, final Collection<String> membershipUUIDs, final String entityID) {

    final Set<Rule> rules = new HashSet<Rule>();
    if (userUUID != null) {
      final Query usersQuery = getSession().getNamedQuery("getAllApplicableRulesForUser");
      usersQuery.setParameter("userUUID", userUUID);
      final List<Rule> userResults = usersQuery.list();
      if (userResults != null) {
        rules.addAll(userResults);
      }
    }
    if (entityID != null) {
      final Query entityQuery = getSession().getNamedQuery("getAllApplicableRulesForEntity");
      entityQuery.setParameter("entityID", entityID);
      final List<Rule> entityResults = entityQuery.list();
      if (entityResults != null) {
        rules.addAll(entityResults);
      }
    }
    if (groupUUIDs != null && !groupUUIDs.isEmpty()) {
      final Query groupsQuery = getSession().getNamedQuery("getAllApplicableRulesForGroups");
      groupsQuery.setParameterList("groupUUIDs", groupUUIDs);
      final List<Rule> groupsResults = groupsQuery.list();
      if (groupsResults != null) {
        rules.addAll(groupsResults);
      }
    }
    if (roleUUIDs != null && !roleUUIDs.isEmpty()) {
      final Query rolesQuery = getSession().getNamedQuery("getAllApplicableRulesForRoles");
      rolesQuery.setParameterList("roleUUIDs", roleUUIDs);
      final List<Rule> rolesResults = rolesQuery.list();
      if (rolesResults != null) {
        rules.addAll(rolesResults);
      }
    }
    if (membershipUUIDs != null && !membershipUUIDs.isEmpty()) {
      final Query membershipsQuery = getSession().getNamedQuery("getAllApplicableRulesForMemberships");
      membershipsQuery.setParameterList("membershipUUIDs", membershipUUIDs);
      final List<Rule> membershipsResults = membershipsQuery.list();
      if (membershipsResults != null) {
        rules.addAll(membershipsResults);
      }
    }
    final List<Rule> rulesList = new ArrayList<Rule>(rules);
    Collections.sort(rulesList);
    return rulesList;
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<Rule> getApplicableRules(final RuleType ruleType, final String userUUID,
      final Collection<String> roleUUIDs, final Collection<String> groupUUIDs,
      final Collection<String> membershipUUIDs, final String entityID) {

    final Set<Rule> rules = new HashSet<Rule>();
    if (userUUID != null) {
      final Query usersQuery = getSession().getNamedQuery("getApplicableRulesForUser");
      usersQuery.setParameter("ruleType", ruleType.name());
      usersQuery.setParameter("userUUID", userUUID);
      final List<Rule> userResults = usersQuery.list();
      if (userResults != null) {
        rules.addAll(userResults);
      }
    }
    if (entityID != null) {
      final Query entityQuery = getSession().getNamedQuery("getApplicableRulesForEntity");
      entityQuery.setParameter("ruleType", ruleType.name());
      entityQuery.setParameter("entityID", entityID);
      final List<Rule> entityResults = entityQuery.list();
      if (entityResults != null) {
        rules.addAll(entityResults);
      }
    }
    if (groupUUIDs != null && !groupUUIDs.isEmpty()) {
      final Query groupsQuery = getSession().getNamedQuery("getApplicableRulesForGroups");
      groupsQuery.setParameter("ruleType", ruleType.name());
      groupsQuery.setParameterList("groupUUIDs", groupUUIDs);
      final List<Rule> groupsResults = groupsQuery.list();
      if (groupsResults != null) {
        rules.addAll(groupsResults);
      }
    }
    if (roleUUIDs != null && !roleUUIDs.isEmpty()) {
      final Query rolesQuery = getSession().getNamedQuery("getApplicableRulesForRoles");
      rolesQuery.setParameter("ruleType", ruleType.name());
      rolesQuery.setParameterList("roleUUIDs", roleUUIDs);
      final List<Rule> rolesResults = rolesQuery.list();
      if (rolesResults != null) {
        rules.addAll(rolesResults);
      }
    }
    if (membershipUUIDs != null && !membershipUUIDs.isEmpty()) {
      final Query membershipsQuery = getSession().getNamedQuery("getApplicableRulesForMemberships");
      membershipsQuery.setParameter("ruleType", ruleType.name());
      membershipsQuery.setParameterList("membershipUUIDs", membershipUUIDs);
      final List<Rule> membershipsResults = membershipsQuery.list();
      if (membershipsResults != null) {
        rules.addAll(membershipsResults);
      }
    }
    final List<Rule> rulesList = new ArrayList<Rule>(rules);
    Collections.sort(rulesList);
    return rulesList;
  }

  @Override
  @SuppressWarnings("unchecked")
  public Set<Rule> getRulesByType(final Set<String> ruleTypes) {
    final Query query = getSession().getNamedQuery("getRuleListByType");

    query.setParameterList("types", ruleTypes);
    final List<Rule> results = query.list();
    if (results != null) {
      return new HashSet<Rule>(results);
    } else {
      return Collections.emptySet();
    }
  }

  @Override
  public RuleTypePolicy getRuleTypePolicy(final RuleType ruleType) {
    final Query query = getSession().getNamedQuery("getRuleTypePolicy");
    query.setParameter("type", ruleType.name());
    query.setMaxResults(1);
    return (RuleTypePolicy) query.uniqueResult();
  }

  @Override
  @Deprecated
  @SuppressWarnings("unchecked")
  public Set<Rule> getAllApplicableRules(final String entityID) {
    final Query query = getSession().getNamedQuery("getRuleListByEntity");
    query.setString("entity", entityID);
    final List<Rule> results = query.list();
    if (results != null) {
      return new HashSet<Rule>(results);
    } else {
      return Collections.emptySet();
    }
  }

  @Override
  @Deprecated
  @SuppressWarnings("unchecked")
  public Set<Rule> getAllApplicableRules(final String entityID, final RuleType ruleType) {
    final Query query = getSession().getNamedQuery("getRuleListByEntityAndType");
    query.setString("entity", entityID);
    final String name = ruleType.name();
    query.setString("ruletype", name);
    final List<Rule> results = query.list();
    if (results != null) {
      return new HashSet<Rule>(results);
    } else {
      return Collections.emptySet();
    }
  }

  @Override
  public List<InternalProcessDefinition> getProcessesExcept(final Set<ProcessDefinitionUUID> processUUIDs,
      final int fromIndex, final int pageSize) {
    final Query query = getSession().getNamedQuery("getAllProcessesListExcept");

    query.setFirstResult(fromIndex);
    query.setMaxResults(pageSize);
    final Set<String> uuids = new HashSet<String>();
    for (final ProcessDefinitionUUID uuid : processUUIDs) {
      uuids.add(uuid.toString());
    }
    return executeSplittedQueryList(InternalProcessDefinition.class, query, "definitionsUUIDs", uuids);
  }

  @Override
  public List<InternalProcessDefinition> getProcessesExcept(final Set<ProcessDefinitionUUID> processUUIDs,
      final int fromIndex, final int pageSize, final ProcessDefinitionCriterion pagingCriterion) {
    Query query = null;
    switch (pagingCriterion) {
      case NAME_ASC:
        query = getSession().getNamedQuery("getAllProcessesListExceptOrderByNameAsc");
        break;

      case LABEL_ASC:
        query = getSession().getNamedQuery("getAllProcessesListExceptOrderByLabelAsc");
        break;

      case VERSION_ASC:
        query = getSession().getNamedQuery("getAllProcessesListExceptOrderByVersionAsc");
        break;

      case STATE_ASC:
        query = getSession().getNamedQuery("getAllProcessesListExceptOrderByStateAsc");
        break;

      case NAME_DESC:
        query = getSession().getNamedQuery("getAllProcessesListExceptOrderByNameDesc");
        break;

      case LABEL_DESC:
        query = getSession().getNamedQuery("getAllProcessesListExceptOrderByLabelDesc");
        break;

      case VERSION_DESC:
        query = getSession().getNamedQuery("getAllProcessesListExceptOrderByVersionDesc");
        break;

      case STATE_DESC:
        query = getSession().getNamedQuery("getAllProcessesListExceptOrderByStateDesc");
        break;

      case DEFAULT:
        query = getSession().getNamedQuery("getAllProcessesListExcept");
        break;
    }

    query.setFirstResult(fromIndex);
    query.setMaxResults(pageSize);
    final Set<String> uuids = new HashSet<String>();
    for (final ProcessDefinitionUUID uuid : processUUIDs) {
      uuids.add(uuid.toString());
    }
    return executeSplittedQueryList(InternalProcessDefinition.class, query, "definitionsUUIDs", uuids);
  }

  @Override
  public int getNumberOfActivityInstanceComments(final ActivityInstanceUUID activityUUID) {
    final Query query = getSession().getNamedQuery("getNumberOfActivityComments");
    query.setString("activityUUID", activityUUID.getValue());
    return ((Long) query.uniqueResult()).intValue();
  }

  @Override
  public int getNumberOfComments(final ProcessInstanceUUID instanceUUID) {
    final Query query = getSession().getNamedQuery("getNumberOfAllComments");
    query.setString("instanceUUID", instanceUUID.getValue());
    return ((Long) query.uniqueResult()).intValue();
  }

  @Override
  public int getNumberOfProcessInstanceComments(final ProcessInstanceUUID instanceUUID) {
    final Query query = getSession().getNamedQuery("getNumberOfInstanceComments");
    query.setString("instanceUUID", instanceUUID.getValue());
    return ((Long) query.uniqueResult()).intValue();
  }

  @Override
  @SuppressWarnings("unchecked")
  public Map<ActivityInstanceUUID, Integer> getNumberOfActivityInstanceComments(
      final Set<ActivityInstanceUUID> activityUUIDs) {
    final Query query = getSession().getNamedQuery("getActivitiesComments");
    final List<String> uuids = new ArrayList<String>();
    for (final ActivityInstanceUUID uuid : activityUUIDs) {
      uuids.add(uuid.toString());
    }
    query.setParameterList("uuids", uuids);
    final List<Comment> comments = query.list();

    final Map<ActivityInstanceUUID, Integer> result = new HashMap<ActivityInstanceUUID, Integer>();
    for (final Comment comment : comments) {
      final ActivityInstanceUUID activityUUID = comment.getActivityUUID();
      if (result.containsKey(activityUUID)) {
        result.put(activityUUID, result.get(activityUUID) + 1);
      } else {
        result.put(activityUUID, 1);
      }
    }
    return result;
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<Comment> getActivityInstanceCommentFeed(final ActivityInstanceUUID activityUUID) {
    final Query query = getSession().getNamedQuery("getActivityComments");
    query.setString("activityUUID", activityUUID.getValue());
    return query.list();
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<Comment> getProcessInstanceCommentFeed(final ProcessInstanceUUID instanceUUID) {
    final Query query = getSession().getNamedQuery("getInstanceComments");
    query.setString("instanceUUID", instanceUUID.getValue());
    return query.list();
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<Comment> getCommentFeed(final ProcessInstanceUUID instanceUUID) {
    final Query query = getSession().getNamedQuery("getAllComments");
    query.setString("instanceUUID", instanceUUID.getValue());
    return query.list();
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<Rule> getRules(final RuleType ruleType, final int fromIndex, final int pageSize) {
    final Query query = getSession().getNamedQuery("getRuleListByExactType");
    query.setString("ruletype", ruleType.name());
    query.setFirstResult(fromIndex);
    query.setMaxResults(pageSize);
    return formatList(query.list());
  }

  @Override
  @SuppressWarnings("unchecked")
  public Set<String> getAllExceptions(final String entityID, final RuleType ruleType) {
    final Query query = getSession().getNamedQuery("getExceptionListByEntityAndRuleType");
    query.setString("entity", entityID);
    query.setString("ruletype", ruleType.name());
    final List<String> results = query.list();
    if (results != null) {
      return new HashSet<String>(results);
    } else {
      return Collections.emptySet();
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public Set<ProcessDefinitionUUID> getAllProcessDefinitionUUIDs() {
    final Query query = getSession().getNamedQuery("getAllProcessDefinitionUUIDs");
    final List<ProcessDefinitionUUID> results = query.list();
    if (results != null) {
      return new HashSet<ProcessDefinitionUUID>(results);
    } else {
      return Collections.emptySet();
    }
  }

  @Override
  public Set<ProcessDefinitionUUID> getAllProcessDefinitionUUIDsExcept(final Set<ProcessDefinitionUUID> processUUIDs) {
    if (processUUIDs == null || processUUIDs.isEmpty()) {
      return getAllProcessDefinitionUUIDs();
    }
    final Query query = getSession().getNamedQuery("getAllProcessDefinitionUUIDsExcept");
    final Set<String> uuids = new HashSet<String>();
    for (final ProcessDefinitionUUID uuid : processUUIDs) {
      uuids.add(uuid.toString());
    }
    return executeSplittedQuery(ProcessDefinitionUUID.class, query, "definitionsUUIDs", uuids);
  }

  @Override
  public long getNumberOfRules(final RuleType ruleType) {
    final Query query = getSession().getNamedQuery("getNumberOfRulesForType");
    query.setString("ruletype", ruleType.name());
    return ((Long) query.uniqueResult()).intValue();
  }

  // -- Web token management.
  @Override
  public WebTemporaryToken getToken(final String token) {
    final Query query = getSession().getNamedQuery("getTemporaryTokenFromKey");
    query.setString("tokenKey", token);
    query.setMaxResults(1);
    return (WebTemporaryToken) query.uniqueResult();
  }

  @Override
  @SuppressWarnings("unchecked")
  public Set<WebTemporaryToken> getExpiredTokens() {
    final Query query = getSession().getNamedQuery("getExpiredTemporaryTokens");
    query.setLong("currentDate", new Date().getTime());

    final List<WebTemporaryToken> results = query.list();
    if (results != null) {
      return new HashSet<WebTemporaryToken>(results);
    } else {
      return Collections.emptySet();
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public Set<Category> getCategories(final Collection<String> categoryNames) {
    final Query query = getSession().getNamedQuery("getCategoriesByName");
    query.setParameterList("names", categoryNames);

    final List<Category> results = query.list();
    if (results != null) {
      return new HashSet<Category>(results);
    } else {
      return Collections.emptySet();
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public Set<Category> getAllCategories() {
    final Query query = getSession().getNamedQuery("getAllCategories");

    final List<Category> results = query.list();
    if (results != null) {
      return new HashSet<Category>(results);
    } else {
      return Collections.emptySet();
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public Set<Category> getAllCategoriesExcept(final Set<String> uuids) {
    final Query query = getSession().getNamedQuery("getAllCategoriesExcept");
    query.setParameterList("uuids", uuids);

    final List<Category> results = query.list();
    if (results != null) {
      return new HashSet<Category>(results);
    } else {
      return Collections.emptySet();
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public Set<CategoryImpl> getCategoriesByUUIDs(final Set<CategoryUUID> uuids) {
    if (uuids == null || uuids.isEmpty()) {
      return Collections.emptySet();
    }
    final Collection<String> ids = new HashSet<String>();
    for (final CategoryUUID categoryUUID : uuids) {
      ids.add(categoryUUID.getValue());
    }
    final Query query = getSession().getNamedQuery("getCategoriesByUUID");
    query.setParameterList("uuids", ids);

    final List<CategoryImpl> results = query.list();
    if (results != null) {
      return new HashSet<CategoryImpl>(results);
    } else {
      return Collections.emptySet();
    }
  }

  @Override
  public CategoryImpl getCategoryByUUID(final String uuid) {
    final Query query = getSession().getNamedQuery("getCategoryByUUID");
    query.setString("uuid", uuid);

    return (CategoryImpl) query.uniqueResult();
  }

  @Override
  @SuppressWarnings("unchecked")
  public Set<ProcessDefinitionUUID> getProcessUUIDsFromCategory(final String category) {
    final Query query = getSession().getNamedQuery("getProcessUUIDsFromCategory");
    query.setString("category", category);

    final List<ProcessDefinitionUUID> results = query.list();
    if (results != null) {
      return new HashSet<ProcessDefinitionUUID>(results);
    } else {
      return Collections.emptySet();
    }
  }

  /*
   * SEARCH
   */

  @Override
  @SuppressWarnings("unchecked")
  public List<Object> search(final SearchQueryBuilder query, final int firstResult, final int maxResults,
      final Class<?> indexClass) {
    List<Object> result = new ArrayList<Object>();
    final FullTextQuery hibernateQuery = createFullTextQuery(query, indexClass);
    if (hibernateQuery != null) {
      if (firstResult >= 0) {
        hibernateQuery.setFirstResult(firstResult);
        hibernateQuery.setMaxResults(maxResults);
      }
      result = hibernateQuery.list();
    }
    return result;
  }

  @Override
  public int search(final SearchQueryBuilder query, final Class<?> indexClass) {
    int result = 0;
    final FullTextQuery hibernateQuery = createFullTextQuery(query, indexClass);
    if (hibernateQuery != null) {
      result = hibernateQuery.getResultSize();
    }
    return result;
  }

  private FullTextQuery createFullTextQuery(final SearchQueryBuilder query, final Class<?> indexClass) {
    QueryParser parser = null;
    final String expression = query.getQuery();
    if (!expression.contains("\\:")) {
      parser = new QueryParser(LUCENE_VERSION, query.getIndex().getDefaultField(), new StandardAnalyzer(LUCENE_VERSION));
    } else {
      final List<String> list = query.getIndex().getAllFields();
      final String[] fields = list.toArray(new String[list.size()]);
      parser = new MultiFieldQueryParser(LUCENE_VERSION, fields, new StandardAnalyzer(LUCENE_VERSION));
    }
    final FullTextSession searchSession = Search.getFullTextSession(getSession());
    try {
      final org.apache.lucene.search.Query luceneQuery = parser.parse(query.getQuery());
      return searchSession.createFullTextQuery(luceneQuery, indexClass);
    } catch (final ParseException e) {
      throw new BonitaRuntimeException(e.getMessage());
    }
  }

  /*
   * IDENTITY
   */
  @Override
  public RoleImpl findRoleByName(final String name) {
    final Query query = getSession().getNamedQuery("findRoleByName");
    query.setString("roleName", name);
    return (RoleImpl) query.uniqueResult();
  }

  @Override
  public UserImpl findUserByUsername(final String username) {
    final Query query = getSession().getNamedQuery("findUserByUsername");
    query.setString("username", username);
    return (UserImpl) query.uniqueResult();
  }

  @Override
  @SuppressWarnings("unchecked")
  public Set<GroupImpl> findGroupsByName(final String name) {
    final Query query = getSession().getNamedQuery("findGroupsByName");
    query.setString("groupName", name);
    final List<GroupImpl> results = query.list();
    if (results != null) {
      return new HashSet<GroupImpl>(results);
    } else {
      return Collections.emptySet();
    }
  }

  @Override
  public ProfileMetadataImpl findProfileMetadataByName(final String metadataName) {
    final Query query = getSession().getNamedQuery("findProfileMetadataByName");
    query.setString("metadataName", metadataName);
    return (ProfileMetadataImpl) query.uniqueResult();
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<GroupImpl> getAllGroups() {
    final Query query = getSession().getNamedQuery("getAllGroups");
    return formatList(query.list());
  }

  @Override
  @SuppressWarnings("unchecked")
  public Set<MembershipImpl> getAllMemberships() {
    final Query query = getSession().getNamedQuery("getMemberships");
    final List<MembershipImpl> results = query.list();
    if (results != null) {
      return new HashSet<MembershipImpl>(results);
    } else {
      return Collections.emptySet();
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<ProfileMetadataImpl> getAllProfileMetadata() {
    final Query query = getSession().getNamedQuery("getProfileMetadata");
    return formatList(query.list());
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<RoleImpl> getAllRoles() {
    final Query query = getSession().getNamedQuery("getAllRoles");
    return formatList(query.list());
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<UserImpl> getAllUsers() {
    final Query query = getSession().getNamedQuery("getAllUsers");
    return formatList(query.list());
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<GroupImpl> getGroupChildren(final String parentGroupUUID) {
    final Query query;
    if (parentGroupUUID == null) {
      query = getSession().getNamedQuery("getRootGroups");
    } else {
      query = getSession().getNamedQuery("getGroupChildren");
      query.setString("parentGroupUUID", parentGroupUUID);
    }
    return formatList(query.list());
  }

  @Override
  public MembershipImpl findMembershipByRoleAndGroup(final String roleUUID, final String groupUUID) {
    final Query query = getSession().getNamedQuery("findMembershipByRoleAndGroup");
    query.setString("roleUUID", roleUUID);
    query.setString("groupUUID", groupUUID);
    return (MembershipImpl) query.uniqueResult();
  }

  @Override
  @SuppressWarnings("unchecked")
  public Set<MembershipImpl> getMembershipsByGroup(final String groupUUID) {
    final Query query = getSession().getNamedQuery("getMembershipsByGroup");
    query.setString("groupUUID", groupUUID);
    final List<MembershipImpl> results = query.list();
    if (results != null) {
      return new HashSet<MembershipImpl>(results);
    } else {
      return Collections.emptySet();
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public Set<MembershipImpl> getMembershipsByRole(final String roleUUID) {
    final Query query = getSession().getNamedQuery("getMembershipsByRole");
    query.setString("roleUUID", roleUUID);
    final List<MembershipImpl> results = query.list();
    if (results != null) {
      return new HashSet<MembershipImpl>(results);
    } else {
      return Collections.emptySet();
    }
  }

  @Override
  public GroupImpl getGroup(final String groupUUID) {
    final Query query = getSession().getNamedQuery("getGroup");
    query.setString("groupUUID", groupUUID);
    return (GroupImpl) query.uniqueResult();
  }

  @Override
  public RoleImpl getRole(final String roleUUID) {
    final Query query = getSession().getNamedQuery("getRole");
    query.setString("roleUUID", roleUUID);
    return (RoleImpl) query.uniqueResult();
  }

  @Override
  public UserImpl getUser(final String userUUID) {
    final Query query = getSession().getNamedQuery("getUser");
    query.setString("userUUID", userUUID);
    return (UserImpl) query.uniqueResult();
  }

  @Override
  public MembershipImpl getMembership(final String membershipUUID) {
    final Query query = getSession().getNamedQuery("getMembership");
    query.setString("membershipUUID", membershipUUID);
    return (MembershipImpl) query.uniqueResult();
  }

  @Override
  public ProfileMetadataImpl getProfileMetadata(final String profileMetadataUUID) {
    final Query query = getSession().getNamedQuery("getProfileMetadataByUUID");
    query.setString("profileMetadataUUID", profileMetadataUUID);
    return (ProfileMetadataImpl) query.uniqueResult();
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<UserImpl> getUsersByGroup(final String groupUUID) {
    final Query query = getSession().getNamedQuery("getUsersByGroup");
    query.setString("groupUUID", groupUUID);
    return formatList(query.list());
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<UserImpl> getUsersByMembership(final String membershipUUID) {
    final Query query = getSession().getNamedQuery("getUsersByMembership");
    query.setString("membershipUUID", membershipUUID);
    return formatList(query.list());
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<UserImpl> getUsersByRole(final String roleUUID) {
    final Query query = getSession().getNamedQuery("getUsersByRole");
    query.setString("roleUUID", roleUUID);
    return formatList(query.list());
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<GroupImpl> getGroups(final int fromIndex, final int numberOfGroups) {
    final Query query = getSession().getNamedQuery("getAllGroups");
    query.setFirstResult(fromIndex);
    query.setMaxResults(numberOfGroups);
    return formatList(query.list());
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<GroupImpl> getGroups(final int fromIndex, final int numberOfGroups, final GroupCriterion pagingCriterion) {
    Query query = null;
    switch (pagingCriterion) {
      case NAME_ASC:
        query = getSession().getNamedQuery("getAllGroupsOrderByNameAsc");
        break;
      case LABEL_ASC:
        query = getSession().getNamedQuery("getAllGroupsOrderByLabelAsc");
        break;
      case NAME_DESC:
        query = getSession().getNamedQuery("getAllGroupsOrderByNameDesc");
        break;
      case LABEL_DESC:
        query = getSession().getNamedQuery("getAllGroupsOrderByLabelDesc");
        break;
      case DEFAULT:
        query = getSession().getNamedQuery("getAllGroups");
        break;
    }
    query.setFirstResult(fromIndex);
    query.setMaxResults(numberOfGroups);
    return formatList(query.list());
  }

  @Override
  public int getNumberOfGroups() {
    final Query query = getSession().getNamedQuery("getNumberOfGroups");
    return ((Long) query.uniqueResult()).intValue();
  }

  @Override
  public int getNumberOfRoles() {
    final Query query = getSession().getNamedQuery("getNumberOfRoles");
    return ((Long) query.uniqueResult()).intValue();
  }

  @Override
  public int getNumberOfUsers() {
    final Query query = getSession().getNamedQuery("getNumberOfUsers");
    return ((Long) query.uniqueResult()).intValue();
  }

  @Override
  public int getNumberOfUsersByGroup(final String groupUUID) {
    final Query query = getSession().getNamedQuery("getNumberOfUsersByGroup");
    query.setString("groupUUID", groupUUID);
    return ((Long) query.uniqueResult()).intValue();
  }

  @Override
  public int getNumberOfUsersByRole(final String roleUUID) {
    final Query query = getSession().getNamedQuery("getNumberOfUsersByRole");
    query.setString("roleUUID", roleUUID);
    return ((Long) query.uniqueResult()).intValue();
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<RoleImpl> getRoles(final int fromIndex, final int numberOfRoles) {
    final Query query = getSession().getNamedQuery("getAllRoles");
    query.setFirstResult(fromIndex);
    query.setMaxResults(numberOfRoles);
    return formatList(query.list());
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<RoleImpl> getRoles(final int fromIndex, final int numberOfRoles, final RoleCriterion pagingCriterion) {
    Query query = null;
    switch (pagingCriterion) {
      case NAME_ASC:
        query = getSession().getNamedQuery("getAllRolesOrderByNameAsc");
        break;
      case LABEL_ASC:
        query = getSession().getNamedQuery("getAllRolesOrderByLabelAsc");
        break;
      case NAME_DESC:
        query = getSession().getNamedQuery("getAllRolesOrderByNameDesc");
        break;
      case LABEL_DESC:
        query = getSession().getNamedQuery("getAllRolesOrderByLabelDesc");
        break;
      case DEFAULT:
        query = getSession().getNamedQuery("getAllRoles");
        break;
    }
    query.setFirstResult(fromIndex);
    query.setMaxResults(numberOfRoles);
    return formatList(query.list());
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<UserImpl> getUsers(final int fromIndex, final int numberOfUsers) {
    final Query query = getSession().getNamedQuery("getAllUsers");
    query.setFirstResult(fromIndex);
    query.setMaxResults(numberOfUsers);
    return formatList(query.list());
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<UserImpl> getUsers(final int fromIndex, final int numberOfUsers, final UserCriterion pagingCriterion) {
    Query query = null;
    switch (pagingCriterion) {
      case FIRST_NAME_ASC:
        query = getSession().getNamedQuery("getAllUsersOderByFirstNameAsc");
        break;
      case LAST_NAME_ASC:
        query = getSession().getNamedQuery("getAllUsersOderByLastNameAsc");
        break;
      case USER_NAME_ASC:
        query = getSession().getNamedQuery("getAllUsersOderByUserNameAsc");
        break;
      case FIRST_NAME_DESC:
        query = getSession().getNamedQuery("getAllUsersOderByFirtNameDesc");
        break;
      case LAST_NAME_DESC:
        query = getSession().getNamedQuery("getAllUsersOderByLastNameDesc");
        break;
      case USER_NAME_DESC:
        query = getSession().getNamedQuery("getAllUsersOderByUserNameDesc");
        break;
      case DEFAULT:
        query = getSession().getNamedQuery("getAllUsers");
        break;
    }
    query.setFirstResult(fromIndex);
    query.setMaxResults(numberOfUsers);
    return formatList(query.list());
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<UserImpl> getUsersByGroup(final String groupUUID, final int fromIndex, final int numberOfUsers) {
    final Query query = getSession().getNamedQuery("getUsersByGroup");
    query.setString("groupUUID", groupUUID);
    query.setFirstResult(fromIndex);
    query.setMaxResults(numberOfUsers);
    return formatList(query.list());
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<UserImpl> getUsersByGroup(final String groupUUID, final int fromIndex, final int numberOfUsers,
      final UserCriterion pagingCriterion) {
    Query query = null;
    switch (pagingCriterion) {
      case FIRST_NAME_ASC:
        query = getSession().getNamedQuery("getUsersByGroupOrderByFirstNameAsc");
        break;
      case LAST_NAME_ASC:
        query = getSession().getNamedQuery("getUsersByGroupOrderByLastNameAsc");
        break;
      case USER_NAME_ASC:
        query = getSession().getNamedQuery("getUsersByGroupOrderByUserNameAsc");
        break;
      case FIRST_NAME_DESC:
        query = getSession().getNamedQuery("getUsersByGroupOrderByFirstNameDesc");
        break;
      case LAST_NAME_DESC:
        query = getSession().getNamedQuery("getUsersByGroupOrderByLastNameDesc");
        break;
      case USER_NAME_DESC:
        query = getSession().getNamedQuery("getUsersByGroupOrderByUserNameDesc");
        break;
      case DEFAULT:
        query = getSession().getNamedQuery("getUsersByGroup");
        break;
    }

    query.setString("groupUUID", groupUUID);
    query.setFirstResult(fromIndex);
    query.setMaxResults(numberOfUsers);
    return formatList(query.list());
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<UserImpl> getUsersByRole(final String roleUUID, final int fromIndex, final int numberOfUsers) {
    final Query query = getSession().getNamedQuery("getUsersByRole");
    query.setString("roleUUID", roleUUID);
    query.setFirstResult(fromIndex);
    query.setMaxResults(numberOfUsers);
    return formatList(query.list());
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<UserImpl> getUsersByRole(final String roleUUID, final int fromIndex, final int numberOfUsers,
      final UserCriterion pagingCriterion) {
    Query query = null;
    switch (pagingCriterion) {
      case FIRST_NAME_ASC:
        query = getSession().getNamedQuery("getUsersByRoleOrderByFirstNameAsc");
        break;
      case LAST_NAME_ASC:
        query = getSession().getNamedQuery("getUsersByRoleOrderByLastNameAsc");
        break;
      case USER_NAME_ASC:
        query = getSession().getNamedQuery("getUsersByRoleOrderByUserNameAsc");
        break;
      case FIRST_NAME_DESC:
        query = getSession().getNamedQuery("getUsersByRoleOrderByFirstNameDesc");
        break;
      case LAST_NAME_DESC:
        query = getSession().getNamedQuery("getUsersByRoleOrderByLastNameDesc");
        break;
      case USER_NAME_DESC:
        query = getSession().getNamedQuery("getUsersByRoleOrderByUserNameDesc");
        break;
      case DEFAULT:
        query = getSession().getNamedQuery("getUsersByRole");
        break;
    }
    query.setString("roleUUID", roleUUID);
    query.setFirstResult(fromIndex);
    query.setMaxResults(numberOfUsers);
    return formatList(query.list());
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<UserImpl> getUsersByManager(final String managerUUID) {
    final Query query = getSession().getNamedQuery("getUsersByManager");
    query.setString("managerUUID", managerUUID);
    return formatList(query.list());
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<UserImpl> getUsersByDelegee(final String delegeeUUID) {
    final Query query = getSession().getNamedQuery("getUsersByDelegee");
    query.setString("delegeeUUID", delegeeUUID);
    return formatList(query.list());
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<ProfileMetadataImpl> getProfileMetadata(final int fromIndex, final int numberOfMetadata) {
    final Query query = getSession().getNamedQuery("getProfileMetadata");
    query.setFirstResult(fromIndex);
    query.setMaxResults(numberOfMetadata);
    return formatList(query.list());
  }

  @Override
  public int getNumberOfProfileMetadata() {
    final Query query = getSession().getNamedQuery("getNumberOfProfileMetadata");
    return ((Long) query.uniqueResult()).intValue();
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<GroupImpl> getGroupChildren(final String parentGroupUUID, final int fromIndex, final int numberOfGroups) {
    final Query query = getSession().getNamedQuery("getGroupChildren");
    query.setFirstResult(fromIndex);
    query.setMaxResults(numberOfGroups);
    query.setString("parentGroupUUID", parentGroupUUID);
    return formatList(query.list());
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<GroupImpl> getGroupChildren(final String parentGroupUUID, final int fromIndex, final int numberOfGroups,
      final GroupCriterion pagingCriterion) {
    Query query = null;
    switch (pagingCriterion) {
      case NAME_ASC:
        query = getSession().getNamedQuery("getGroupChildrenOrderByNameAsc");
        break;
      case LABEL_ASC:
        query = getSession().getNamedQuery("getGroupChildrenOrderByLabelAsc");
        break;
      case NAME_DESC:
        query = getSession().getNamedQuery("getGroupChildrenOrderByNameDesc");
        break;
      case LABEL_DESC:
        query = getSession().getNamedQuery("getGroupChildrenOrderByLabelDesc");
        break;
      case DEFAULT:
        query = getSession().getNamedQuery("getGroupChildren");
        break;
    }
    query.setFirstResult(fromIndex);
    query.setMaxResults(numberOfGroups);
    query.setString("parentGroupUUID", parentGroupUUID);
    return formatList(query.list());
  }

  @Override
  public int getNumberOfGroupChildren(final String parentGroupUUID) {
    final Query query = getSession().getNamedQuery("getNumberOfGroupChildren");
    query.setString("parentGroupUUID", parentGroupUUID);
    return ((Long) query.uniqueResult()).intValue();
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<GroupImpl> getGroups(final Collection<String> groupUUIDs) {
    final Query query = getSession().getNamedQuery("getGroups");
    query.setParameterList("groupUUIDs", groupUUIDs);
    return formatList(query.list());
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<MembershipImpl> getMemberships(final Collection<String> membershipUUIDs) {
    final Query query = getSession().getNamedQuery("getMemberships");
    query.setParameterList("membershipUUIDs", membershipUUIDs);
    return formatList(query.list());
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<RoleImpl> getRoles(final Collection<String> roleUUIDs) {
    final Query query = getSession().getNamedQuery("getRoles");
    query.setParameterList("roleUUIDs", roleUUIDs);
    return formatList(query.list());
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<UserImpl> getUsers(final Collection<String> userUUIDs) {
    final Query query = getSession().getNamedQuery("getUsers");
    query.setParameterList("userUUIDs", userUUIDs);
    return formatList(query.list());
  }

  @Override
  @SuppressWarnings("unchecked")
  public Set<ActivityDefinitionUUID> getProcessTaskUUIDs(final ProcessDefinitionUUID definitionUUID) {
    final Query query = getSession().getNamedQuery("getProcessTaskUUIDs");
    query.setString("uuid", definitionUUID.getValue());
    final Set<ActivityDefinitionUUID> activityUUIDs = new HashSet<ActivityDefinitionUUID>();
    final List<ActivityDefinitionUUID> uuids = query.list();
    if (uuids != null) {
      activityUUIDs.addAll(uuids);
    }
    return activityUUIDs;
  }

  @Override
  public boolean processExists(final ProcessDefinitionUUID definitionUUID) {
    final Query query = getSession().getNamedQuery("processExists");
    query.setString("uuid", definitionUUID.getValue());
    final Long uuid = (Long) query.uniqueResult();
    boolean exists = true;
    if (uuid != 1) {
      exists = false;
    }
    return exists;
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<Long> getProcessInstancesDuration(final Date since, final Date until) {
    final Query query = getSession().getNamedQuery("getProcessInstancesDuration");
    query.setLong("since", since.getTime());
    query.setLong("until", until.getTime());
    return formatList(query.list());
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<Long> getProcessInstancesDuration(final ProcessDefinitionUUID processUUID, final Date since,
      final Date until) {
    final Query query = getSession().getNamedQuery("getProcessInstancesDurationFromDefinitionUUID");
    query.setLong("since", since.getTime());
    query.setLong("until", until.getTime());
    query.setString("processUUID", processUUID.getValue());
    return formatList(query.list());
  }

  @Override
  public List<Long> getProcessInstancesDurationFromProcessUUIDs(final Set<ProcessDefinitionUUID> processUUIDs,
      final Date since, final Date until) {
    final Query query = getSession().getNamedQuery("getProcessInstancesDurationFromDefinitionUUIDs");
    query.setLong("since", since.getTime());
    query.setLong("until", until.getTime());
    final Set<String> uuids = new HashSet<String>();
    for (final ProcessDefinitionUUID uuid : processUUIDs) {
      uuids.add(uuid.toString());
    }
    final List<Long> durations = executeSplittedQueryList(Long.class, query, "processUUIDs", uuids);
    if (durations != null) {
      Collections.sort(durations);
      return durations;
    } else {
      return Collections.emptyList();
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<Long> getActivityInstancesExecutionTime(final Date since, final Date until) {
    final Query query = getSession().getNamedQuery("getActivityInstancesExecutionTime");
    query.setLong("since", since.getTime());
    query.setLong("until", until.getTime());
    return formatList(query.list());
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<Long> getActivityInstancesExecutionTime(final ProcessDefinitionUUID processUUID, final Date since,
      final Date until) {
    final Query query = getSession().getNamedQuery("getActivityInstancesExecutionTimeFromProcessDefinitionUUID");
    query.setLong("since", since.getTime());
    query.setLong("until", until.getTime());
    query.setString("processUUID", processUUID.getValue());
    return formatList(query.list());
  }

  @Override
  public List<Long> getActivityInstancesExecutionTimeFromProcessUUIDs(final Set<ProcessDefinitionUUID> processUUIDs,
      final Date since, final Date until) {
    final Query query = getSession().getNamedQuery("getActivityInstancesExecutionTimeFromProcessDefinitionUUIDs");
    query.setLong("since", since.getTime());
    query.setLong("until", until.getTime());
    final Set<String> uuids = new HashSet<String>();
    for (final ProcessDefinitionUUID uuid : processUUIDs) {
      uuids.add(uuid.toString());
    }

    final List<Long> executionTimes = executeSplittedQueryList(Long.class, query, "processUUIDs", uuids);
    if (executionTimes != null) {
      Collections.sort(executionTimes);
      return executionTimes;
    } else {
      return Collections.emptyList();
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<Long> getActivityInstancesExecutionTime(final ActivityDefinitionUUID activityUUID, final Date since,
      final Date until) {
    final Query query = getSession().getNamedQuery("getActivityInstancesExecutionTimeFromActivityDefinitionUUID");
    query.setLong("since", since.getTime());
    query.setLong("until", until.getTime());
    query.setString("activityUUID", activityUUID.getValue());
    return formatList(query.list());
  }

  @Override
  public List<Long> getActivityInstancesExecutionTimeFromActivityUUIDs(final Set<ActivityDefinitionUUID> activityUUIDs,
      final Date since, final Date until) {
    final Query query = getSession().getNamedQuery("getActivityInstancesExecutionTimeFromActivityDefinitionUUIDs");
    query.setLong("since", since.getTime());
    query.setLong("until", until.getTime());
    final Set<String> uuids = new HashSet<String>();
    for (final ActivityDefinitionUUID uuid : activityUUIDs) {
      uuids.add(uuid.toString());
    }
    final List<Long> executionTimes = executeSplittedQueryList(Long.class, query, "activityUUIDs", uuids);
    if (executionTimes != null) {
      Collections.sort(executionTimes);
      return executionTimes;
    } else {
      return Collections.emptyList();
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<Long> getTaskInstancesWaitingTime(final Date since, final Date until) {
    final Query query = getSession().getNamedQuery("getTaskInstancesWaitingTime");
    query.setLong("since", since.getTime());
    query.setLong("until", until.getTime());
    return formatList(query.list());
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<Long> getTaskInstancesWaitingTime(final ProcessDefinitionUUID processUUID, final Date since,
      final Date until) {
    final Query query = getSession().getNamedQuery("getTaskInstancesWaitingTimeFromProcessDefinitionUUID");
    query.setLong("since", since.getTime());
    query.setLong("until", until.getTime());
    query.setString("processUUID", processUUID.getValue());
    return formatList(query.list());
  }

  @Override
  public List<Long> getTaskInstancesWaitingTimeFromProcessUUIDs(final Set<ProcessDefinitionUUID> processUUIDs,
      final Date since, final Date until) {
    final Query query = getSession().getNamedQuery("getTaskInstancesWaitingTimeFromProcessDefinitionUUIDs");
    query.setLong("since", since.getTime());
    query.setLong("until", until.getTime());
    final Set<String> uuids = new HashSet<String>();

    for (final ProcessDefinitionUUID uuid : processUUIDs) {
      uuids.add(uuid.toString());
    }

    final List<Long> waitingTimes = executeSplittedQueryList(Long.class, query, "processUUIDs", uuids);
    if (waitingTimes != null) {
      Collections.sort(waitingTimes);
      return waitingTimes;
    } else {
      return Collections.emptyList();
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<Long> getTaskInstancesWaitingTime(final ActivityDefinitionUUID taskUUID, final Date since,
      final Date until) {
    final Query query = getSession().getNamedQuery("getTaskInstancesWaitingTimeFromActivityDefinitionUUID");
    query.setLong("since", since.getTime());
    query.setLong("until", until.getTime());
    query.setString("activityUUID", taskUUID.getValue());
    return formatList(query.list());
  }

  @Override
  public List<Long> getTaskInstancesWaitingTimeFromTasksUUIDs(final Set<ActivityDefinitionUUID> tasksUUIDs,
      final Date since, final Date until) {
    final Query query = getSession().getNamedQuery("getTaskInstancesWaitingTimeFromActivityDefinitionUUIDs");
    query.setLong("since", since.getTime());
    query.setLong("until", until.getTime());

    final Set<String> uuids = new HashSet<String>();
    for (final ActivityDefinitionUUID uuid : tasksUUIDs) {
      uuids.add(uuid.toString());
    }

    final List<Long> waitingTimes = executeSplittedQueryList(Long.class, query, "activityUUIDs", uuids);
    if (waitingTimes != null) {
      Collections.sort(waitingTimes);
      return waitingTimes;
    } else {
      return Collections.emptyList();
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<Long> getTaskInstancesWaitingTimeOfUser(final String username, final Date since, final Date until) {
    final Query query = getSession().getNamedQuery("getTaskInstancesWaitingTimeOfUser");
    query.setLong("since", since.getTime());
    query.setLong("until", until.getTime());
    query.setString("userId", username);
    return formatList(query.list());
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<Long> getTaskInstancesWaitingTimeOfUser(final String username, final ProcessDefinitionUUID processUUID,
      final Date since, final Date until) {
    final Query query = getSession().getNamedQuery("getTaskInstancesWaitingTimeOfUserFromProcessDefinitionUUID");
    query.setLong("since", since.getTime());
    query.setLong("until", until.getTime());
    query.setString("processUUID", processUUID.getValue());
    query.setString("userId", username);
    return formatList(query.list());
  }

  @Override
  public List<Long> getTaskInstancesWaitingTimeOfUserFromProcessUUIDs(final String username,
      final Set<ProcessDefinitionUUID> processUUIDs, final Date since, final Date until) {
    final Query query = getSession().getNamedQuery("getTaskInstancesWaitingTimeOfUserFromProcessDefinitionUUIDs");
    query.setLong("since", since.getTime());
    query.setLong("until", until.getTime());
    query.setString("userId", username);

    final Set<String> uuids = new HashSet<String>();
    for (final ProcessDefinitionUUID uuid : processUUIDs) {
      uuids.add(uuid.toString());
    }

    final List<Long> waitingTimes = executeSplittedQueryList(Long.class, query, "processUUIDs", uuids);
    if (waitingTimes != null) {
      Collections.sort(waitingTimes);
      return waitingTimes;
    } else {
      return Collections.emptyList();
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<Long> getTaskInstancesWaitingTimeOfUser(final String username, final ActivityDefinitionUUID taskUUID,
      final Date since, final Date until) {
    final Query query = getSession().getNamedQuery("getTaskInstancesWaitingTimeOfUserFromActivityDefinitionUUID");
    query.setLong("since", since.getTime());
    query.setLong("until", until.getTime());
    query.setString("activityUUID", taskUUID.getValue());
    query.setString("userId", username);
    return formatList(query.list());
  }

  @Override
  public List<Long> getTaskInstancesWaitingTimeOfUserFromTaskUUIDs(final String username,
      final Set<ActivityDefinitionUUID> taskUUIDs, final Date since, final Date until) {
    final Query query = getSession().getNamedQuery("getTaskInstancesWaitingTimeOfUserFromActivityDefinitionUUIDs");
    query.setLong("since", since.getTime());
    query.setLong("until", until.getTime());
    query.setString("userId", username);

    final Set<String> uuids = new HashSet<String>();
    for (final ActivityDefinitionUUID uuid : taskUUIDs) {
      uuids.add(uuid.toString());
    }

    final List<Long> waitingTimes = executeSplittedQueryList(Long.class, query, "activityUUIDs", uuids);
    if (waitingTimes != null) {
      Collections.sort(waitingTimes);
      return waitingTimes;
    } else {
      return Collections.emptyList();
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<Long> getActivityInstancesDuration(final Date since, final Date until) {
    final Query query = getSession().getNamedQuery("getActivityInstancesDuration");
    query.setLong("since", since.getTime());
    query.setLong("until", until.getTime());
    return formatList(query.list());
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<Long> getActivityInstancesDuration(final ProcessDefinitionUUID processUUID, final Date since,
      final Date until) {
    final Query query = getSession().getNamedQuery("getActivityInstancesDurationFromProcessDefinitionUUID");
    query.setLong("since", since.getTime());
    query.setLong("until", until.getTime());
    query.setString("processUUID", processUUID.getValue());
    return formatList(query.list());
  }

  @Override
  public List<Long> getActivityInstancesDurationFromProcessUUIDs(final Set<ProcessDefinitionUUID> processUUIDs,
      final Date since, final Date until) {
    final Query query = getSession().getNamedQuery("getActivityInstancesDurationFromProcessDefinitionUUIDs");
    query.setLong("since", since.getTime());
    query.setLong("until", until.getTime());

    final Set<String> uuids = new HashSet<String>();
    for (final ProcessDefinitionUUID uuid : processUUIDs) {
      uuids.add(uuid.toString());
    }

    final List<Long> durations = executeSplittedQueryList(Long.class, query, "processUUIDs", uuids);
    if (durations != null) {
      Collections.sort(durations);
      return durations;
    } else {
      return Collections.emptyList();
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<Long> getActivityInstancesDuration(final ActivityDefinitionUUID activityUUID, final Date since,
      final Date until) {
    final Query query = getSession().getNamedQuery("getActivityInstancesDurationFromActivityDefinitionUUID");
    query.setLong("since", since.getTime());
    query.setLong("until", until.getTime());
    query.setString("activityUUID", activityUUID.getValue());
    return formatList(query.list());
  }

  @Override
  public List<Long> getActivityInstancesDurationFromActivityUUIDs(final Set<ActivityDefinitionUUID> activityUUIDs,
      final Date since, final Date until) {
    final Query query = getSession().getNamedQuery("getActivityInstancesDurationFromActivityDefinitionUUIDs");
    query.setLong("since", since.getTime());
    query.setLong("until", until.getTime());

    final Set<String> uuids = new HashSet<String>();
    for (final ActivityDefinitionUUID uuid : activityUUIDs) {
      uuids.add(uuid.toString());
    }

    final List<Long> durations = executeSplittedQueryList(Long.class, query, "activityUUIDs", uuids);
    if (durations != null) {
      Collections.sort(durations);
      return durations;
    } else {
      return Collections.emptyList();
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<Long> getActivityInstancesDurationByActivityType(
      final org.ow2.bonita.facade.def.majorElement.ActivityDefinition.Type activityType, final Date since,
      final Date until) {
    final Query query = getSession().getNamedQuery("getActivityInstancesDurationByActivityType");
    query.setLong("since", since.getTime());
    query.setLong("until", until.getTime());
    query.setString("activityType", activityType.toString());
    return formatList(query.list());
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<Long> getActivityInstancesDurationByActivityType(
      final org.ow2.bonita.facade.def.majorElement.ActivityDefinition.Type activityType,
      final ProcessDefinitionUUID processUUID, final Date since, final Date until) {
    final Query query = getSession().getNamedQuery(
        "getActivityInstancesDurationByActivityTypeFromProcessDefinitionUUID");
    query.setLong("since", since.getTime());
    query.setLong("until", until.getTime());
    query.setString("activityType", activityType.toString());
    query.setString("processUUID", processUUID.getValue());
    return formatList(query.list());
  }

  @Override
  public List<Long> getActivityInstancesDurationByActivityTypeFromProcessUUIDs(
      final org.ow2.bonita.facade.def.majorElement.ActivityDefinition.Type activityType,
      final Set<ProcessDefinitionUUID> processUUIDs, final Date since, final Date until) {
    final Query query = getSession().getNamedQuery(
        "getActivityInstancesDurationByActivityTypeFromProcessDefinitionUUIDs");
    query.setLong("since", since.getTime());
    query.setLong("until", until.getTime());
    query.setString("activityType", activityType.toString());

    final Set<String> uuids = new HashSet<String>();
    for (final ProcessDefinitionUUID uuid : processUUIDs) {
      uuids.add(uuid.toString());
    }

    final List<Long> durations = executeSplittedQueryList(Long.class, query, "processUUIDs", uuids);
    if (durations != null) {
      Collections.sort(durations);
      return durations;
    } else {
      return Collections.emptyList();
    }
  }

  @Override
  public long getNumberOfCreatedProcessInstances(final Date since, final Date until) {
    final Query query = getSession().getNamedQuery("getNumberOfCreatedProcessInstances");
    query.setLong("since", since.getTime());
    query.setLong("until", until.getTime());
    return (Long) query.uniqueResult();
  }

  @Override
  public long getNumberOfCreatedProcessInstances(final ProcessDefinitionUUID processUUID, final Date since,
      final Date until) {
    final Query query = getSession().getNamedQuery("getNumberOfCreatedProcessInstancesFromProcessUUID");
    query.setLong("since", since.getTime());
    query.setLong("until", until.getTime());
    query.setString("processUUID", processUUID.getValue());
    return (Long) query.uniqueResult();
  }

  @Override
  public long getNumberOfCreatedActivityInstances(final Date since, final Date until) {
    final Query query = getSession().getNamedQuery("getNumberOfCreatedActivityInstances");
    query.setLong("since", since.getTime());
    query.setLong("until", until.getTime());
    return (Long) query.uniqueResult();
  }

  @Override
  public long getNumberOfCreatedActivityInstances(final ProcessDefinitionUUID processUUID, final Date since,
      final Date until) {
    final Query query = getSession().getNamedQuery("getNumberOfCreatedActivityInstancesFromProcessUUID");
    query.setLong("since", since.getTime());
    query.setLong("until", until.getTime());
    query.setString("processUUID", processUUID.getValue());
    return (Long) query.uniqueResult();
  }

  @Override
  public long getNumberOfCreatedActivityInstancesFromProcessUUIDs(final Set<ProcessDefinitionUUID> processUUIDs,
      final Date since, final Date until) {
    final Query query = getSession().getNamedQuery("getNumberOfCreatedActivityInstancesFromProcessUUIDs");
    query.setLong("since", since.getTime());
    query.setLong("until", until.getTime());
    final Set<String> uuids = new HashSet<String>();
    for (final ProcessDefinitionUUID uuid : processUUIDs) {
      uuids.add(uuid.getValue());
    }
    query.setParameterList("processUUIDs", uuids);
    return (Long) query.uniqueResult();
  }

  @Override
  public long getNumberOfCreatedActivityInstances(final ActivityDefinitionUUID activityUUID, final Date since,
      final Date until) {
    final Query query = getSession().getNamedQuery("getNumberOfCreatedActivityInstancesFromActivityDefUUID");
    query.setLong("since", since.getTime());
    query.setLong("until", until.getTime());
    query.setString("activityUUID", activityUUID.getValue());
    return (Long) query.uniqueResult();
  }

  @Override
  public long getNumberOfCreatedActivityInstancesFromActivityUUIDs(final Set<ActivityDefinitionUUID> activityUUIDs,
      final Date since, final Date until) {
    final Query query = getSession().getNamedQuery("getNumberOfCreatedActivityInstancesFromActivityDefUUIDs");
    query.setLong("since", since.getTime());
    query.setLong("until", until.getTime());
    final Set<String> uuids = new HashSet<String>();
    for (final ActivityDefinitionUUID uuid : activityUUIDs) {
      uuids.add(uuid.getValue());
    }
    query.setParameterList("activityUUIDs", uuids);
    return (Long) query.uniqueResult();
  }

  @Override
  public long getNumberOfCreatedActivityInstancesByActivityType(
      final org.ow2.bonita.facade.def.majorElement.ActivityDefinition.Type activityType, final Date since,
      final Date until) {
    final Query query = getSession().getNamedQuery("getNumberOfCreatedActivityInstancesByActivityType");
    query.setLong("since", since.getTime());
    query.setLong("until", until.getTime());
    query.setString("activityType", activityType.toString());
    return (Long) query.uniqueResult();
  }

  @Override
  public long getNumberOfCreatedActivityInstancesByActivityType(
      final org.ow2.bonita.facade.def.majorElement.ActivityDefinition.Type activityType,
      final ProcessDefinitionUUID processUUID, final Date since, final Date until) {
    final Query query = getSession().getNamedQuery("getNumberOfCreatedActivityInstancesByActivityTypeFromProcessUUID");
    query.setLong("since", since.getTime());
    query.setLong("until", until.getTime());
    query.setString("activityType", activityType.toString());
    query.setString("processUUID", processUUID.getValue());
    return (Long) query.uniqueResult();
  }

  @Override
  public long getNumberOfCreatedActivityInstancesByActivityTypeFromProcessUUIDs(
      final org.ow2.bonita.facade.def.majorElement.ActivityDefinition.Type activityType,
      final Set<ProcessDefinitionUUID> processUUIDs, final Date since, final Date until) {
    final Query query = getSession().getNamedQuery("getNumberOfCreatedActivityInstancesByActivityTypeFromProcessUUIDs");
    query.setLong("since", since.getTime());
    query.setLong("until", until.getTime());
    query.setString("activityType", activityType.toString());
    final Set<String> uuids = new HashSet<String>();
    for (final ProcessDefinitionUUID uuid : processUUIDs) {
      uuids.add(uuid.getValue());
    }
    query.setParameterList("processUUIDs", uuids);
    return (Long) query.uniqueResult();
  }

  @Override
  public boolean containsOtherActiveActivities(final ProcessInstanceUUID instanceUUID,
      final ActivityInstanceUUID activityUUID) {
    final Query query = getSession().getNamedQuery("getNumberOfOtherActiveActivities");
    query.setString("instanceUUID", instanceUUID.getValue());
    String actUUID = "null";
    if (activityUUID != null) {
      actUUID = activityUUID.getValue();
    }
    query.setString("activityUUID", actUUID);
    final Collection<ActivityState> taskStates = new ArrayList<ActivityState>();
    taskStates.add(ActivityState.READY);
    taskStates.add(ActivityState.EXECUTING);
    taskStates.add(ActivityState.FAILED);
    taskStates.add(ActivityState.SUSPENDED);
    query.setParameterList("states", taskStates, ACTIVITY_STATE_USER_TYPE);
    final Long count = (Long) query.uniqueResult();
    boolean contains = true;
    if (count == 0) {
      contains = false;
    }
    return contains;
  }

  @Override
  public IncomingEventInstance getSignalStartIncomingEvent(final List<String> processNames, final String signalCode) {
    final Query query = getSession().getNamedQuery("getSignalStartIncomingEvent");
    query.setParameterList("processNames", processNames);
    query.setString("signalCode", signalCode);
    return (IncomingEventInstance) query.uniqueResult();
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<IncomingEventInstance> getMessageStartIncomingEvents(final Set<String> processNames) {
    final Query query = getSession().getNamedQuery("getMessageStartIncomingEvents");
    query.setParameterList("processNames", processNames);
    return query.list();
  }

  @Override
  public DocumentVersion getDocumentVersion(final long versionId) {
    final Query query = getSession().getNamedQuery("getDocumentVersion");
    query.setLong("versionId", versionId);
    return (DocumentVersionImpl) query.uniqueResult();
  }

  @Override
  public DocumentContent getDocumentContent(final long contentStorageId) {
    final Query query = getSession().getNamedQuery("getDocumentContent");
    query.setLong("contentStorageId", contentStorageId);
    return (DocumentContentImpl) query.uniqueResult();
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<DocumentDescriptor> getDocumentDescriptors(final int fromIndex, final int maxResults) {
    final Query query = getSession().getNamedQuery("getDocumentDescriptors");
    query.setFirstResult(fromIndex);
    query.setMaxResults(maxResults);
    return query.list();
  }

  @Override
  public DocumentDescriptor getDocumentDescriptor(final long documentDescriptorId) {
    final Query query = getSession().getNamedQuery("getDocumentDescriptor");
    query.setLong("documentDescriptorId", documentDescriptorId);
    return (DocumentDescriptorImpl) query.uniqueResult();
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<DocumentVersion> getDocumentVersions(final int fromIndex, final int maxResults, final long versionSeriesId) {
    final Query query = getSession().getNamedQuery("getDocumentVersions");
    query.setLong("versionSeriesId", versionSeriesId);
    query.setFirstResult(fromIndex);
    query.setMaxResults(maxResults);
    return query.list();
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<DocumentVersion> getDocumentVersions(final long versionSeriesId) {
    final Query query = getSession().getNamedQuery("getDocumentVersions");
    query.setLong("versionSeriesId", versionSeriesId);
    return query.list();
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<Document> searchDocuments(final DocumentSearchBuilder builder, final int fromIndex, final int maxResults) {
    final Query query = getCompleteSearchQuery(builder, getSession().getNamedQuery("searchDocuments"), true);
    query.setFirstResult(fromIndex);
    query.setMaxResults(maxResults);
    return query.list();
  }

  @Override
  public long getNumberOfDocuments(final DocumentSearchBuilder builder) {
    final Query query = getCompleteSearchQuery(builder, getSession().getNamedQuery("getNumberOfDocuments"), false);
    return (Long) query.uniqueResult();
  }

  private Query getCompleteSearchQuery(final DocumentSearchBuilder builder, final Query query, final boolean addOrderBy) {
    final String queryString = query.getQueryString();
    final StringBuilder newQuery = new StringBuilder();
    newQuery.append(queryString);

    final Map<String, Object> parameters = new HashMap<String, Object>();
    final List<Object> queryObjects = builder.getQuery();
    if (!queryObjects.isEmpty()) {
      newQuery.append(" AND ");
    }
    for (final Object object : queryObjects) {
      if (object instanceof DocumentCriterion) {
        final DocumentCriterion criterion = (DocumentCriterion) object;
        switch (criterion.getField()) {
          case ID:
            createEqualsOrInClause(newQuery, criterion, "dv.id", "documentId", parameters);
            break;
          case PROCESS_DEFINITION_UUID:
            createEqualsOrInClause(newQuery, criterion, "dm.processDefinitionUUID.value", "processDef", parameters);
            break;
          case PROCESS_DEFINITION_UUID_WITHOUT_INSTANCES:
            newQuery.append(" (");
            createEqualsOrInClause(newQuery, criterion, "dm.processDefinitionUUID.value", "processDef", parameters);
            newQuery.append(" AND dm.processInstanceUUID.value IS NULL");
            newQuery.append(" )");
            break;
          case PROCESS_INSTANCE_UUID:
            createEqualsOrInClause(newQuery, criterion, "dm.processInstanceUUID.value", "processInstance", parameters);
            break;
          case NAME:
            createEqualsOrInClause(newQuery, criterion, "dd.name", "name", parameters);
            break;
          case FILENAME:
            createEqualsOrInClause(newQuery, criterion, "dv.contentFileName", "fileName", parameters);
            break;
          case CREATION_DATE:
            createEqualsOrInClause(newQuery, criterion, "dv.creationDate", "creationDate", parameters);
            break;
          case AUTHOR:
            createEqualsOrInClause(newQuery, criterion, "dv.author", "author", parameters);
            break;
          case LAST_MODIFICATION_DATE:
            createEqualsOrInClause(newQuery, criterion, "dv.lastModificationDate", "lastModificationDate", parameters);
            break;
          case IS_EMPTY:
            if ((Boolean) criterion.getValue()) {
              newQuery.append(" dv.contentSize = 0");
            } else {
              newQuery.append(" dv.contentSize > 0");
            }
            break;
        }
      } else {
        newQuery.append(" ").append(object).append(" ");
      }
    }
    if (addOrderBy) {
      newQuery.append(" ORDER BY dd.name, dv.creationDate DESC, dv.id");
    }

    final Query searchQuery = session.createQuery(newQuery.toString());
    for (final Map.Entry<String, Object> entry : parameters.entrySet()) {
      final Object value = entry.getValue();
      if (value instanceof Collection<?>) {
        searchQuery.setParameterList(entry.getKey(), (Collection<?>) value);
      } else {
        searchQuery.setParameter(entry.getKey(), value);
      }
    }
    return searchQuery;
  }

  private void createEqualsOrInClause(final StringBuilder whereClause, final DocumentCriterion criterion,
      final String field, final String parameterName, final Map<String, Object> parameters) {
    final boolean isDate = DocumentIndex.CREATION_DATE.equals(criterion.getField())
        || DocumentIndex.LAST_MODIFICATION_DATE.equals(criterion.getField());
    final boolean fromStringToLong = DocumentIndex.ID.equals(criterion.getField());
    switch (criterion.getCriterionType()) {
      case BETWEEN:
        Object fromValue = criterion.getFrom();
        Object toValue = criterion.getTo();
        if (isDate) {
          fromValue = ((Date) fromValue).getTime();
          toValue = ((Date) toValue).getTime();
        } else if (fromStringToLong) {
          fromValue = Long.valueOf((String) fromValue);
          toValue = Long.valueOf((String) toValue);
        }
        final String from = parameterName + "_from";
        final String to = parameterName + "_to";
        whereClause.append(" (");
        whereClause.append(field);
        whereClause.append(" >= :");
        whereClause.append(from);
        whereClause.append(" AND ");
        whereClause.append(field);
        whereClause.append(" <= :");
        whereClause.append(to);
        whereClause.append(") ");
        parameters.put(from, fromValue);
        parameters.put(to, toValue);
        break;
      case IN:
        whereClause.append(" " + field + " IN (");
        final Collection<?> values = criterion.getValues();
        final Iterator<?> iterator = values.iterator();
        for (int i = 0; iterator.hasNext(); i++) {
          Object value = iterator.next();
          whereClause.append(':');
          whereClause.append(parameterName);
          whereClause.append('_');
          whereClause.append(i);
          if (iterator.hasNext()) {
            whereClause.append(',');
          }

          if (fromStringToLong) {
            value = Long.valueOf((String) value);
          }

          parameters.put(parameterName + "_" + i, value);
        }
        whereClause.append(") ");
        break;
      case EQUALS:
        whereClause.append(field);
        whereClause.append(" = :");
        whereClause.append(parameterName);
        Object value = criterion.getValue();
        if (isDate) {
          value = ((Date) value).getTime();
        } else if (fromStringToLong) {
          value = Long.valueOf((String) value);
        }
        parameters.put(parameterName, value);
        break;
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<Document> getDocuments(final ProcessInstanceUUID instanceUUID, final int fromResult, final int maxResults) {
    final Query query = getSession().getNamedQuery("getDocumentsOfProcessInstance");
    query.setString("processInstanceUUID", instanceUUID.getValue());
    query.setFirstResult(fromResult);
    query.setMaxResults(maxResults);
    return query.list();
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<Document> getDocuments(final ProcessInstanceUUID instanceUUID, final String documentName,
      final int fromResult, final int maxResults) {
    final Query query = getSession().getNamedQuery("getDocumentsOfProcessInstanceWithName");
    query.setString("processInstanceUUID", instanceUUID.getValue());
    query.setString("name", documentName);
    query.setFirstResult(fromResult);
    query.setMaxResults(maxResults);
    return query.list();
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<Document> getDocuments(final ProcessDefinitionUUID processDefUUID, final String documentName,
      final int fromResult, final int maxResults) {
    final Query query = getSession().getNamedQuery("getDocumentsOfProcessDefinitionWithName");
    query.setString("processDefinitionUUID", processDefUUID.getValue());
    query.setString("name", documentName);
    query.setFirstResult(fromResult);
    query.setMaxResults(maxResults);
    return query.list();
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<Document> getDocumentsOfProcessDefinitionWithoutInstances(final ProcessDefinitionUUID processDefUUID,
      final int fromResult, final int maxResults) {
    final Query query = getSession().getNamedQuery("getDocumentsOfProcessDefinitionWithoutInstances");
    query.setString("processDefinitionUUID", processDefUUID.getValue());
    query.setFirstResult(fromResult);
    query.setMaxResults(maxResults);
    return query.list();
  }

  @Override
  public long getNbOfDocuments(final ProcessInstanceUUID instanceUUID, final String documentName) {
    final Query query = getSession().getNamedQuery("getNbOfDocumentsOfProcessInstanceWithName");
    query.setString("processInstanceUUID", instanceUUID.getValue());
    query.setString("name", documentName);
    return (Long) query.uniqueResult();
  }

  @Override
  public long hasDocuments(final ProcessInstanceUUID instanceUUID, final String documentName, final boolean metaDocument) {
    final Query query = getSession().getNamedQuery("hasDocumentsOfProcessInstanceWithNameAndMetaType");
    query.setString("processInstanceUUID", instanceUUID.getValue());
    query.setString("name", documentName);
    query.setBoolean("meta", metaDocument);
    return (Long) query.uniqueResult();
  }

  @Override
  public long hasDocuments(final ProcessDefinitionUUID processUUID, final String documentName,
      final boolean metaDocument) {
    final Query query = getSession().getNamedQuery("hasDocumentsOfProcessDefinitionWithNameAndMetaType");
    query.setString("processDefinitionUUID", processUUID.getValue());
    query.setString("name", documentName);
    query.setBoolean("meta", metaDocument);
    return (Long) query.uniqueResult();
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<DocumentDescriptorMapping> getDocumentDescriptorMappings(final long documentDescriptorId,
      final int fromIndex, final int maxResults) {
    final Query query = getSession().getNamedQuery("getDocumentDescriptorMappings");
    query.setLong("documentDescriptorId", documentDescriptorId);
    query.setFirstResult(fromIndex);
    query.setMaxResults(maxResults);
    return query.list();
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<Document> getMetaDocumentOfProcessDefinition(final ProcessDefinitionUUID processDefUUID,
      final int fromResult, final int maxResults) {
    final Query query = getSession().getNamedQuery("getMetaDocumentsDocumentsOfProcessDefinition");
    query.setString("processDefinitionUUID", processDefUUID.getValue());
    query.setFirstResult(fromResult);
    query.setMaxResults(maxResults);
    return query.list();
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<DocumentDescriptor> getDocumentDescriptors(final ProcessDefinitionUUID processDefinitionUUID,
      final int fromIndex, final int maxResults) {
    final Query query = getSession().getNamedQuery("getDocumentDescriptorsOfProcessDefinition");
    query.setString("processDefinitionUUID", processDefinitionUUID.getValue());
    query.setFirstResult(fromIndex);
    query.setMaxResults(maxResults);
    return query.list();
  }

  @Override
  public void deleteIncompatibleEvents(final OutgoingEventInstance outgoing) {
    final Query query = getSession().getNamedQuery("deleteIncompatibleEvents");
    query.setLong("outgoingId", outgoing.getId());
    query.executeUpdate();
  }

  @Override
  public Job getJob(final Long jobId) {
    final Query query = getSession().getNamedQuery("getJob");
    query.setLong("id", jobId);
    return (Job) query.uniqueResult();
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<Job> getJobs() {
    final Query query = getSession().getNamedQuery("getJobs");
    return formatList(query.list());
  }

  @Override
  public Job getJob(final ActivityDefinitionUUID activityUUID) {
    final Query query = getSession().getNamedQuery("getJobOfActivityUUID");
    query.setString("activityUUID", activityUUID.getValue());
    return (Job) query.uniqueResult();
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<Job> getExecutableJobs(final String processUUID) {
    final Query query = getSession().getNamedQuery("getExecutableJobsOfProcess");
    query.setString("processUUID", processUUID);
    query.setLong("current", System.currentTimeMillis());
    return formatList(query.list());
  }

  @Override
  public List<String> getNonLockedProcessesHavingJobs(final Set<String> lockedProcessUUIDs, final int maxResult) {
	  final Query query = getSession().getNamedQuery("getNonLockedProcessesHavingJobs");
	  if (!lockedProcessUUIDs.isEmpty()) {
		  query.setParameterList("lockedProcessUUIDs", lockedProcessUUIDs);
	    } else {
	    	//bad but first iteration
	    	final Set<String> dummyProcessUUIDsToExclude = new HashSet<String>();
	    	dummyProcessUUIDsToExclude.add("ddddduuuuuuummmmmmyyyyyy");
	    	query.setParameterList("lockedProcessUUIDs", dummyProcessUUIDsToExclude);
	    }
	  
	  query.setLong("current", System.currentTimeMillis());
	  query.setMaxResults(maxResult);
	  return formatList(query.list());
  }
  
  protected <T> List<T> formatList(final List<T> list) {
    if (list == null) {
      return Collections.emptyList();
    } else {
      return list;
    }
  }

  @Override
  public Long getNextJobDueDate(final Set<String> processUUIDsToExclude) {
    final Query query = getSession().getNamedQuery("getNextJobDueDate");
    if (!processUUIDsToExclude.isEmpty()) {
    	query.setParameterList("processUUIDsToExclude", processUUIDsToExclude);	
    } else {
    	//bad but first iteration
    	final Set<String> dummyProcessUUIDsToExclude = new HashSet<String>();
    	dummyProcessUUIDsToExclude.add("ddddduuuuuuummmmmmyyyyyy");
    	query.setParameterList("processUUIDsToExclude", dummyProcessUUIDsToExclude);
    }
    return (Long) query.uniqueResult();
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<Job> getJobsOfExecution(final String executionEventUUID) {
    final Query query = getSession().getNamedQuery("getJobsOfExecution");
    query.setString("executionEventUUID", executionEventUUID);
    final List<Job> jobs = query.list();
    return formatList(jobs);
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<Job> getJobsOfInstance(final ProcessInstanceUUID instanceUUID) {
    final Query query = getSession().getNamedQuery("getJobsOfInstance");
    query.setString("instanceUUID", instanceUUID.getValue());
    final List<Job> jobs = query.list();
    return formatList(jobs);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<Job> getJobs(final String eventType) {
    final Query query = getSession().getNamedQuery("getJobsOfType");
    query.setString("eventType", eventType);
    return formatList(query.list());
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<Job> getJobs(final String eventType, final ProcessInstanceUUID instanceUUID) {
    final Query query = getSession().getNamedQuery("getJobsOfInstanceType");
    query.setString("eventType", eventType);
    query.setString("instanceUUID", instanceUUID.getValue());
    return formatList(query.list());
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<Job> getJobs(final String eventType, final String eventUUID) {
    final Query query = getSession().getNamedQuery("getJobsOfExecutionType");
    query.setString("eventType", eventType);
    query.setString("eventUUID", eventUUID);
    return formatList(query.list());
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public List<Job> getJobsWithoutProcessUUID(final int fromIndex, final int maxResults) {
    final Query query = getSession().getNamedQuery("getJobsWithoutProcessUUID");
    query.setFirstResult(fromIndex);
    query.setMaxResults(maxResults);
    return formatList(query.list());
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<EventCouple> getCorrelationKeyMessageEventCouples(final int maxCouples) {
    final Query query = getSession().getNamedQuery("getCorrelationKeyMessageEventCouples");
    query.setMaxResults(maxCouples);
    query.setResultTransformer(Transformers.aliasToBean(EventCouple.class));
    return formatList(query.list());
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<String> getInstanceIdsFromMetadata(final int index, final int maxResult) {
    final Query query = getSession().getNamedQuery("getInstanceIdsFromMetadata");
    query.setFirstResult(index);
    query.setMaxResults(maxResult);
    return formatList(query.list());
  }

  @Override
  public Long getNextEventDueDate() {
    final Query query = getSession().getNamedQuery("getNextMessageEventDueDate");
    return (Long) query.uniqueResult();
  }

  @Override
  public Long getNextExpressionEventDueDate() {
    final Query query = getSession().getNamedQuery("getNextExpressionEventDueDate");
    return (Long) query.uniqueResult();
  }

  @Override
  public JobLock getJobLock(final String processUUID) {
    final Query query = getSession().getNamedQuery("getJobLock");
    query.setString("processUUID", processUUID);
    return (JobLock) query.uniqueResult();
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<IncomingEventInstance> getMessageEventSubProcessIncomingEvents(final ProcessInstanceUUID eventSubProcessRootInstanceUUID, final long outgoingId) {
    final Query query = getSession().getNamedQuery("getMessageEventSubProcessIncomingEvents");
    query.setString("eventSubProcessRootInstanceUUID", eventSubProcessRootInstanceUUID.getValue());
    query.setLong("outgoingId", outgoingId);
    return formatList(query.list());
  }

}
