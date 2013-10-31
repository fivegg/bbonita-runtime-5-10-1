package org.ow2.bonita.integration.connector.resolver;

import java.util.HashSet;
import java.util.Set;

import org.ow2.bonita.connector.core.RoleResolver;

public class NullSetRoleResolver extends RoleResolver {

	@Override
	protected Set<String> getMembersSet(String roleId) throws Exception {
		Set<String> members = new HashSet<String>();
		members.add(null);
		return members;
	}

}
