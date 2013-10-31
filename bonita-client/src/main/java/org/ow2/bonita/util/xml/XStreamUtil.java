/**
 * Copyright (C) 2010  BonitaSoft S.A.
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
package org.ow2.bonita.util.xml;

import java.util.Date;

import org.ow2.bonita.facade.def.element.impl.AttachmentDefinitionImpl;
import org.ow2.bonita.facade.def.element.impl.BusinessArchiveImpl;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition.ProcessState;
import org.ow2.bonita.facade.def.majorElement.impl.ActivityDefinitionImpl;
import org.ow2.bonita.facade.def.majorElement.impl.DataFieldDefinitionImpl;
import org.ow2.bonita.facade.def.majorElement.impl.ParticipantDefinitionImpl;
import org.ow2.bonita.facade.def.majorElement.impl.ProcessDefinitionImpl;
import org.ow2.bonita.facade.def.majorElement.impl.TransitionDefinitionImpl;
import org.ow2.bonita.facade.identity.impl.GroupImpl;
import org.ow2.bonita.facade.identity.impl.MembershipImpl;
import org.ow2.bonita.facade.identity.impl.ProfileMetadataImpl;
import org.ow2.bonita.facade.identity.impl.RoleImpl;
import org.ow2.bonita.facade.identity.impl.UserImpl;
import org.ow2.bonita.facade.privilege.PrivilegePolicy;
import org.ow2.bonita.facade.privilege.Rule.RuleType;
import org.ow2.bonita.facade.privilege.impl.ActivityRuleImpl;
import org.ow2.bonita.facade.privilege.impl.CustomRuleImpl;
import org.ow2.bonita.facade.privilege.impl.ProcessRuleImpl;
import org.ow2.bonita.facade.privilege.impl.RuleImpl;
import org.ow2.bonita.facade.runtime.ActivityState;
import org.ow2.bonita.facade.runtime.ConnectorExecutionDescriptor;
import org.ow2.bonita.facade.runtime.Label;
import org.ow2.bonita.facade.runtime.impl.ActivityInstanceImpl;
import org.ow2.bonita.facade.runtime.impl.AssignUpdateImpl;
import org.ow2.bonita.facade.runtime.impl.AttachmentInstanceImpl;
import org.ow2.bonita.facade.runtime.impl.CategoryImpl;
import org.ow2.bonita.facade.runtime.impl.InitialAttachmentImpl;
import org.ow2.bonita.facade.runtime.impl.InstanceStateUpdateImpl;
import org.ow2.bonita.facade.runtime.impl.ProcessInstanceImpl;
import org.ow2.bonita.facade.runtime.impl.StateUpdateImpl;
import org.ow2.bonita.facade.uuid.ActivityDefinitionUUID;
import org.ow2.bonita.facade.uuid.ActivityInstanceUUID;
import org.ow2.bonita.facade.uuid.ParticipantDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.light.impl.LightActivityInstanceImpl;
import org.ow2.bonita.light.impl.LightProcessDefinitionImpl;
import org.ow2.bonita.light.impl.LightProcessInstanceImpl;
import org.ow2.bonita.search.SearchQueryBuilder;
import org.ow2.bonita.util.Command;

import com.thoughtworks.xstream.XStream;

/**
 * 
 * @author Elias Ricken de Medeiros
 *
 */
public class XStreamUtil {
	private static XStream xstream;

	static {
		xstream = new XStream();
		xstream.alias("ActivityInstanceUUID", ActivityInstanceUUID.class);
		xstream.alias("LightProcessInstance", LightProcessInstanceImpl.class);
		xstream.alias("AttachmentDefinition", AttachmentDefinitionImpl.class);
		xstream.alias("ActivityInstance", ActivityInstanceImpl.class);
		xstream.alias("Date", Date.class);
		xstream.alias("ProcessInstanceUUID", ProcessInstanceUUID.class);
		xstream.alias("BusinessArchive", BusinessArchiveImpl.class);
		xstream.alias("Label", Label.class);
		xstream.alias("ActivityDefinition", ActivityDefinitionImpl.class);
		xstream.alias("Group", GroupImpl.class);
		xstream.alias("InitialAttachment", InitialAttachmentImpl.class);
		xstream.alias("Rule", RuleImpl.class);
		xstream.alias("ParticipantDefinitionUUID", ParticipantDefinitionUUID.class);
		xstream.alias("SearchQueryBuilder", SearchQueryBuilder.class);
		xstream.alias("LightActivityInstance", LightActivityInstanceImpl.class);
		xstream.alias("ProcessState", ProcessState.class);
		xstream.alias("ProfileMetadata", ProfileMetadataImpl.class);
		xstream.alias("ProcessDefinition", ProcessDefinitionImpl.class);
		xstream.alias("ProcessDefinitionUUID", ProcessDefinitionUUID.class);
		xstream.alias("RuleType", RuleType.class);
		xstream.alias("Membership", MembershipImpl.class);
		xstream.alias("ActivityState", ActivityState.class);
		xstream.alias("AttachmentInstance", AttachmentInstanceImpl.class);
		xstream.alias("User", UserImpl.class);
		xstream.alias("PrivilegePolicy", PrivilegePolicy.class);
		xstream.alias("DataFieldDefinition", DataFieldDefinitionImpl.class);
		xstream.alias("Object", Object.class);
		xstream.alias("ParticipantDefinition", ParticipantDefinitionImpl.class);
		xstream.alias("ActivityDefinitionUUID", ActivityDefinitionUUID.class);
		xstream.alias("Command", Command.class);
		xstream.alias("ProcessInstance", ProcessInstanceImpl.class);
		xstream.alias("Category", CategoryImpl.class);
		xstream.alias("LightProcessDefinition", LightProcessDefinitionImpl.class);
		xstream.alias("Role", RoleImpl.class);
		xstream.alias("TransitionDefinition", TransitionDefinitionImpl.class);
		xstream.alias("StateUpdate", StateUpdateImpl.class);
		xstream.alias("InstanceStateUpdate", InstanceStateUpdateImpl.class);
		xstream.alias("AssignUpdate", AssignUpdateImpl.class);
		xstream.alias("ProcessRule", ProcessRuleImpl.class);
		xstream.alias("ActivityRule", ActivityRuleImpl.class);
		xstream.alias("CustomRule", CustomRuleImpl.class);
		xstream.alias("CustomRule", CustomRuleImpl.class);
		xstream.alias("connectorExecutionDescriptor", ConnectorExecutionDescriptor.class);
	}

	public static XStream getDefaultXstream(){
		return xstream;
	}

}
