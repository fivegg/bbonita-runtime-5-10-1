package org.ow2.bonita.search;

import java.util.List;

import org.ow2.bonita.APITestCase;
import org.ow2.bonita.facade.identity.Role;
import org.ow2.bonita.facade.identity.impl.RoleImpl;
import org.ow2.bonita.search.index.RoleIndex;

public class SearchRoleTest extends APITestCase {
  
  public void testSearchName() throws Exception {
    Role role = getIdentityAPI().addRole("Role1");

    SearchQueryBuilder query = new SearchQueryBuilder(new RoleIndex());
    query.criterion(RoleIndex.NAME).equalsTo("Role1");

    List<RoleImpl> roles = getQueryRuntimeAPI().search(query, 0, 10);
    assertEquals(1, roles.size());

    getIdentityAPI().removeRoleByUUID(role.getUUID());
  }

  public void testSearchDescription() throws Exception {
    Role role = getIdentityAPI().addRole("Role2", "label", "description");

    SearchQueryBuilder query = new SearchQueryBuilder(new RoleIndex());
    query.criterion(RoleIndex.DESCRIPTION).startsWith("desc");

    List<RoleImpl> roles = getQueryRuntimeAPI().search(query, 0, 10);
    assertEquals(1, roles.size());

    getIdentityAPI().removeRoleByUUID(role.getUUID());
  }

  public void testSearchLabel() throws Exception {
    Role role = getIdentityAPI().addRole("Role3", "label", "description");

    SearchQueryBuilder query = new SearchQueryBuilder(new RoleIndex());
    query.criterion(RoleIndex.LABEL).equalsTo("label");

    List<RoleImpl> roles = getQueryRuntimeAPI().search(query, 0, 10);
    assertEquals(1, roles.size());

    getIdentityAPI().removeRoleByUUID(role.getUUID());
  }

}
