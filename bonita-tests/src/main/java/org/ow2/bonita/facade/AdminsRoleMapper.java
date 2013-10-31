package org.ow2.bonita.facade;

import java.util.HashSet;
import java.util.Set;

import org.ow2.bonita.definition.RoleMapper;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;

//@XmlRootElement
public class AdminsRoleMapper implements RoleMapper {

//	@XmlTransient
  public Set<String> searchMembers(QueryAPIAccessor accessor, ProcessInstanceUUID instanceUUID, String roleId) {
    Set<String> usersId = new HashSet<String>();
    usersId.add("admin");
    usersId.add("john");
    return usersId;
  }
}
