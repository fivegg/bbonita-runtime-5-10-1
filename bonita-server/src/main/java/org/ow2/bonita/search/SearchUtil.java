/**
 * Copyright (C) 2010-2011  BonitaSoft S.A.
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
package org.ow2.bonita.search;

import java.lang.annotation.ElementType;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.lucene.util.Version;
import org.hibernate.cfg.Configuration;
import org.hibernate.search.Environment;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Resolution;
import org.hibernate.search.cfg.SearchMapping;
import org.hibernate.search.store.FSDirectoryProvider;
import org.ow2.bonita.facade.def.InternalProcessDefinition;
import org.ow2.bonita.facade.def.majorElement.impl.DescriptionElementImpl;
import org.ow2.bonita.facade.def.majorElement.impl.NamedElementImpl;
import org.ow2.bonita.facade.exception.BonitaInternalException;
import org.ow2.bonita.facade.identity.ContactInfo;
import org.ow2.bonita.facade.identity.impl.GroupImpl;
import org.ow2.bonita.facade.identity.impl.RoleImpl;
import org.ow2.bonita.facade.identity.impl.UserImpl;
import org.ow2.bonita.facade.runtime.ActivityInstance;
import org.ow2.bonita.facade.runtime.Comment;
import org.ow2.bonita.facade.runtime.ProcessInstance;
import org.ow2.bonita.facade.runtime.TaskInstance;
import org.ow2.bonita.facade.runtime.impl.ActivityInstanceImpl;
import org.ow2.bonita.facade.runtime.impl.CaseImpl;
import org.ow2.bonita.facade.runtime.impl.CommentImpl;
import org.ow2.bonita.facade.runtime.impl.InternalActivityInstance;
import org.ow2.bonita.facade.runtime.impl.InternalProcessInstance;
import org.ow2.bonita.facade.runtime.impl.ProcessInstanceImpl;
import org.ow2.bonita.facade.runtime.impl.RuntimeRecordImpl;
import org.ow2.bonita.light.LightActivityInstance;
import org.ow2.bonita.light.impl.LightActivityInstanceImpl;
import org.ow2.bonita.light.impl.LightProcessDefinitionImpl;
import org.ow2.bonita.light.impl.LightProcessInstanceImpl;
import org.ow2.bonita.search.index.ActivityInstanceIndex;
import org.ow2.bonita.search.index.CaseIndex;
import org.ow2.bonita.search.index.CommentIndex;
import org.ow2.bonita.search.index.ContactInfoIndex;
import org.ow2.bonita.search.index.GroupIndex;
import org.ow2.bonita.search.index.ProcessDefinitionIndex;
import org.ow2.bonita.search.index.ProcessInstanceIndex;
import org.ow2.bonita.search.index.RoleIndex;
import org.ow2.bonita.search.index.UserIndex;
import org.ow2.bonita.util.BonitaConstants;

/**
 * 
 * @author Matthieu Chaffotte
 *
 */
public class SearchUtil {

  private static final Logger LOG = Logger.getLogger(SearchUtil.class.getName());

  public static final Version LUCENE_VERSION = Version.LUCENE_29;

  private static boolean disableSearchConfiguration(final Configuration configuration) {
    boolean disable = false;
    final String sysValue = System.getProperty("bonita.search.use");
    if ("false".equals(sysValue)) {
      disable = true;
    } else {
      final String useSearch = configuration.getProperty("bonita.search.use");
      if (!"true".equals(useSearch)) {
        disable = true;
      }
    }
    return disable;
  }

  public static void addSearchConfiguration(final Configuration configuration) {
    final boolean disable = disableSearchConfiguration(configuration);
    if (disable) {
      if (LOG.isLoggable(Level.INFO)) {
        LOG.info("Disable Bonita Indexes");
      }
    } else {
      if (LOG.isLoggable(Level.INFO)) {
        LOG.info("Configuring indexes");
      }
      final SearchMapping mapping = new SearchMapping();
      mapping
      .entity(InternalProcessDefinition.class).indexed().indexName("process_definition")
        .property("dbid", ElementType.FIELD).documentId().name(ProcessDefinitionIndex.DBID)
      .entity(LightProcessDefinitionImpl.class)
        .property("uuid", ElementType.FIELD).field().name(ProcessDefinitionIndex.UUID)
          .bridge(UUIDFieldBridge.class)
        .property("categoryNames", ElementType.METHOD).field().name(ProcessDefinitionIndex.CATEGORY_NAME)
          .bridge(StringSetFieldBridge.class)
      .entity(NamedElementImpl.class)
        .property("name", ElementType.METHOD).field().name(ProcessDefinitionIndex.NAME)
      .entity(DescriptionElementImpl.class) // share with All DescriptionElement: processes, groups, ...
        .property("description", ElementType.METHOD).field().name(ProcessDefinitionIndex.DESCRIPTION)

      .entity(InternalProcessInstance.class).indexed().indexName("process_instance")
        .property("dbid", ElementType.FIELD).documentId().name(ProcessInstanceIndex.DBID)
      .entity(ProcessInstanceImpl.class)
        .property("commentFeed", ElementType.METHOD).indexEmbedded()
        .property("involvedUsers", ElementType.METHOD).field().name(ProcessInstanceIndex.INVOLVED_USER)
          .bridge(StringSetFieldBridge.class)
        .property("activities", ElementType.METHOD).indexEmbedded()
        .property("lastKnownVariableValues", ElementType.METHOD).field().name("variable")
          .bridge(ObjectMapFieldBridge.class)
        .property("activeUsers", ElementType.METHOD).field().name(ProcessInstanceIndex.ACTIVE_USER)
          .bridge(StringSetFieldBridge.class)
      .entity(LightProcessInstanceImpl.class)
        .property("nb", ElementType.FIELD).field().name(ProcessInstanceIndex.NB)
        .property("startedBy", ElementType.METHOD).field().name(ProcessInstanceIndex.STARTED_BY)
        .property("endedBy", ElementType.METHOD).field().name(ProcessInstanceIndex.ENDED_BY)
        .property("startedDate", ElementType.METHOD).field().name(ProcessInstanceIndex.STARTED_DATE)
          .index(Index.UN_TOKENIZED).dateBridge(Resolution.MILLISECOND)
        .property("endedDate", ElementType.METHOD).field().name(ProcessInstanceIndex.ENDED_DATE)
          .index(Index.UN_TOKENIZED).dateBridge(Resolution.MILLISECOND)
        .property("lastUpdate", ElementType.METHOD).field().name(ProcessInstanceIndex.LAST_UPDATE)
          .index(Index.UN_TOKENIZED).dateBridge(Resolution.MILLISECOND)
      .entity(RuntimeRecordImpl.class)
        .property("processDefinitionUUID", ElementType.METHOD).field().name(ProcessInstanceIndex.PROCESS_DEFINITION_UUID)
          .bridge(UUIDFieldBridge.class)
        .property("processInstanceUUID", ElementType.METHOD).field().name(ProcessInstanceIndex.PROCESS_INSTANCE_UUID)
          .bridge(UUIDFieldBridge.class)
        .property("rootInstanceUUID", ElementType.METHOD).field().name(ProcessInstanceIndex.PROCESS_ROOT_INSTANCE_UUID)
          .bridge(UUIDFieldBridge.class)

      .entity(CommentImpl.class).indexed().indexName("comment")
        .property("dbid", ElementType.FIELD).documentId().name(CommentIndex.DBID)
      .entity(Comment.class)
        .property("message", ElementType.METHOD).field().name(CommentIndex.MESSAGE)
        .property("userId", ElementType.METHOD).field().name(CommentIndex.AUTHOR)
        .property("date", ElementType.METHOD).field().name(CommentIndex.DATE)
          .index(Index.UN_TOKENIZED).dateBridge(Resolution.MILLISECOND)

      .entity(InternalActivityInstance.class).indexed().indexName("activity_instance")
        .property("dbid", ElementType.FIELD).documentId().name(ActivityInstanceIndex.DBID)
      .entity(TaskInstance.class)
        .property("taskCandidates", ElementType.METHOD).field().name(ActivityInstanceIndex.CANDIDATE)
          .bridge(StringSetFieldBridge.class)
      .entity(ActivityInstanceImpl.class)
        .property("taskCandidates", ElementType.METHOD).field().name(ActivityInstanceIndex.CANDIDATE)
          .bridge(StringSetFieldBridge.class)
        .property("lastKnownVariableValues", ElementType.METHOD).field().name("variable")
          .bridge(ObjectMapFieldBridge.class)
      .entity(LightActivityInstanceImpl.class)
        .property("taskUser", ElementType.METHOD).field().name(ActivityInstanceIndex.USERID)
        .property("activityName", ElementType.METHOD).field().name(ActivityInstanceIndex.NAME)
        .property("state", ElementType.METHOD).field().name(ActivityInstanceIndex.STATE)
        .property("priority", ElementType.METHOD).field().name(ActivityInstanceIndex.PRIORITY)
          .index(Index.UN_TOKENIZED)
        .property("lastUpdate", ElementType.METHOD).field().name(ActivityInstanceIndex.LAST_UPDATE)
          .index(Index.UN_TOKENIZED).dateBridge(Resolution.MILLISECOND)
        .property("expectedEndDate", ElementType.METHOD).field().name(ActivityInstanceIndex.EXPECTED_END_DATE)
          .index(Index.UN_TOKENIZED).dateBridge(Resolution.MILLISECOND)
      .entity(ActivityInstance.class)
        .property("activityName", ElementType.METHOD).field().name(ActivityInstanceIndex.NAME)
        .property("state", ElementType.METHOD).field().name(ActivityInstanceIndex.STATE)
        .property("lastKnownVariableValues", ElementType.METHOD).field().name("variable")
          .bridge(ObjectMapFieldBridge.class)
      .entity(LightActivityInstance.class)
        .property("activityDescription", ElementType.METHOD).field().name(ActivityInstanceIndex.DESCRIPTION)
        .property("priority", ElementType.METHOD).field().name(ActivityInstanceIndex.PRIORITY)
          .index(Index.UN_TOKENIZED)
        .property("lastUpdate", ElementType.METHOD).field().name(ActivityInstanceIndex.LAST_UPDATE)
          .index(Index.UN_TOKENIZED).dateBridge(Resolution.MILLISECOND)
        .property("expectedEndDate", ElementType.METHOD).field().name(ActivityInstanceIndex.EXPECTED_END_DATE)
          .index(Index.UN_TOKENIZED).dateBridge(Resolution.MILLISECOND)

      .entity(UserImpl.class).indexed().indexName("user")
        .property("dbid", ElementType.FIELD).documentId().name(UserIndex.DBID)
        .property("username", ElementType.FIELD).field().name(UserIndex.NAME)
        .property("firstName", ElementType.FIELD).field().name(UserIndex.FIRST_NAME)
        .property("lastName", ElementType.FIELD).field().name(UserIndex.LAST_NAME)
        .property("manager", ElementType.FIELD).field().name(UserIndex.MANAGER)
        .property("delegate", ElementType.FIELD).field().name(UserIndex.DELEGATE)
        .property("title", ElementType.FIELD).field().name(UserIndex.TITLE)
        .property("jobTitle", ElementType.FIELD).field().name(UserIndex.JOB_TITLE)
        .property("professionalContactInfo", ElementType.FIELD).indexEmbedded()
        .property("personalContactInfo", ElementType.FIELD).indexEmbedded()
      .entity(ContactInfo.class)
        .property("email", ElementType.METHOD).field().name(ContactInfoIndex.EMAIL)
        .property("phoneNumber", ElementType.METHOD).field().name(ContactInfoIndex.PHONE_NUMBER)
        .property("faxNumber", ElementType.METHOD).field().name(ContactInfoIndex.FAX_NUMBER)
        .property("building", ElementType.METHOD).field().name(ContactInfoIndex.BUILDING)
        .property("room", ElementType.METHOD).field().name(ContactInfoIndex.ROOM)
        .property("address", ElementType.METHOD).field().name(ContactInfoIndex.ADDRESS)
        .property("zipCode", ElementType.METHOD).field().name(ContactInfoIndex.ZIP_CODE)
        .property("city", ElementType.METHOD).field().name(ContactInfoIndex.CITY)
        .property("state", ElementType.METHOD).field().name(ContactInfoIndex.STATE)
        .property("country", ElementType.METHOD).field().name(ContactInfoIndex.COUNTRY)
        .property("website", ElementType.METHOD).field().name(ContactInfoIndex.WEBSITE)

      .entity(RoleImpl.class).indexed().indexName("role")
        .property("dbid", ElementType.FIELD).documentId().name(RoleIndex.DBID)
        .property("name", ElementType.FIELD).field().name(RoleIndex.NAME)
        .property("label", ElementType.FIELD).field().name(RoleIndex.LABEL)

      .entity(GroupImpl.class).indexed().indexName("group")
        .property("dbid", ElementType.FIELD).documentId().name(GroupIndex.DBID)
        .property("name", ElementType.METHOD).field().name(GroupIndex.NAME)
        .property("label", ElementType.METHOD).field().name(GroupIndex.LABEL)

      .entity(CaseImpl.class).indexed().indexName("case")
        .property("dbid", ElementType.FIELD).documentId().name(CaseIndex.DBID)
        .property("labelName", ElementType.FIELD).field().name(CaseIndex.LABEL_NAME)
        .property("ownerName", ElementType.FIELD).field().name(CaseIndex.OWNER_NAME)
        .property("uuid", ElementType.FIELD).field().name(CaseIndex.PROCESS_INSTANCE_UUID)
          .bridge(UUIDFieldBridge.class)
      ;
      configuration.getProperties().put(Environment.MODEL_MAPPING, mapping);

      configuration.setProperty("hibernate.search.default.directory_provider", FSDirectoryProvider.class.getName());

      final String path = getDefaultIndexesDirectorPath(configuration);
      configuration.setProperty("hibernate.search.default.indexBase", path);
    }
  }

  public static String getIndexesDirectoryPath(final Configuration configuration) {
    if (disableSearchConfiguration(configuration)) {
      return null;
    } else {
      return configuration.getProperty("hibernate.search.default.indexBase");
    }
  }

  private static String getDefaultIndexesDirectorPath(final Configuration configuration) {
    String indexBasePath = getIndexesDirectoryPath(configuration);
    if (indexBasePath == null || "".equals(indexBasePath.trim())) {
      throw new BonitaInternalException("The hibernate.search.default.indexBase property is not set");
    }
    if (indexBasePath.startsWith("${" + BonitaConstants.HOME + "}")) {
      indexBasePath = indexBasePath.replace("${" + BonitaConstants.HOME + "}", System.getProperty(BonitaConstants.HOME));
    }
    if(LOG.isLoggable(Level.FINE)) {
    	LOG.fine("Hibernate search index base:" + indexBasePath);
    }
    return indexBasePath;
  }

}
