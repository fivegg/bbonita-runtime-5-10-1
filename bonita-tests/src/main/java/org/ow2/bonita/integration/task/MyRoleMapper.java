package org.ow2.bonita.integration.task;

import java.util.HashSet;
import java.util.Set;

import org.ow2.bonita.definition.RoleMapper;
import org.ow2.bonita.facade.QueryAPIAccessor;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;

public class MyRoleMapper implements RoleMapper {

  public Set<String> searchMembers(QueryAPIAccessor accessor, ProcessInstanceUUID instanceUUID, String roleId) {
    Set<String> usersId = new HashSet<String>();
    usersId.add("miguel");
    usersId.add("marc");
    return usersId;
  }
}
