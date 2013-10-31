package org.ow2.bonita.search;

import java.util.List;

import org.ow2.bonita.APITestCase;
import org.ow2.bonita.facade.identity.Group;
import org.ow2.bonita.facade.identity.impl.GroupImpl;
import org.ow2.bonita.search.index.GroupIndex;

public class SearchGroupTest extends APITestCase {
  
  public void testSearchName() throws Exception {
    Group group = getIdentityAPI().addGroup("A-Team", null);

    SearchQueryBuilder query = new SearchQueryBuilder(new GroupIndex());
    query.criterion(GroupIndex.NAME).equalsTo("A-Team");

    List<GroupImpl> groups = getQueryRuntimeAPI().search(query, 0, 10);
    assertEquals(1, groups.size());

    getIdentityAPI().removeGroupByUUID(group.getUUID());
  }

  public void testSearchDescription() throws Exception {
    Group group = getIdentityAPI().addGroup("A-Team", "label", "description", null);

    SearchQueryBuilder query = new SearchQueryBuilder(new GroupIndex());
    query.criterion(GroupIndex.DESCRIPTION).startsWith("desc");

    List<GroupImpl> groups = getQueryRuntimeAPI().search(query, 0, 10);
    assertEquals(1, groups.size());

    getIdentityAPI().removeGroupByUUID(group.getUUID());
  }

  public void testSearchLabel() throws Exception {
    Group group = getIdentityAPI().addGroup("A-Team", "label", "description", null);

    SearchQueryBuilder query = new SearchQueryBuilder(new GroupIndex());
    query.criterion(GroupIndex.LABEL).equalsTo("label");

    List<GroupImpl> groups = getQueryRuntimeAPI().search(query, 0, 10);
    assertEquals(1, groups.size());

    getIdentityAPI().removeGroupByUUID(group.getUUID());
  }

}
