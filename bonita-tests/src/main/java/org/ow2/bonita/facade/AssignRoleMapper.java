package org.ow2.bonita.facade;

import java.util.HashSet;
import java.util.Set;

import org.ow2.bonita.definition.RoleMapper;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.util.AccessorUtil;

public class AssignRoleMapper implements RoleMapper {

  public Set<String> searchMembers(QueryAPIAccessor accessor, ProcessInstanceUUID instanceUUID, String roleId) throws Exception {
    RuntimeAPI runtimeAPI = AccessorUtil.getAPIAccessor().getRuntimeAPI();
    QueryRuntimeAPI queryRuntimeAPI = AccessorUtil.getAPIAccessor().getQueryRuntimeAPI();
    Set<String> usersId = new HashSet<String>();
    String roleMapperCondition = null;
    //execute role mapping
    roleMapperCondition = (String)queryRuntimeAPI.getProcessInstanceVariable(instanceUUID, "roleMapperCondition");
    if (roleMapperCondition.equals("roleMapper1")) {
      usersId.add("admin");
      runtimeAPI.setProcessInstanceVariable(instanceUUID, "roleMapperCondition", "roleMapper2");
    } else {
      usersId.add("admin");
      usersId.add("john");
      runtimeAPI.setProcessInstanceVariable(instanceUUID, "roleMapperCondition", "roleMapper1");
    }
    return usersId;
  }
}
