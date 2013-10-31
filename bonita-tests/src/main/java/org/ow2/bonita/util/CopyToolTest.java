package org.ow2.bonita.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ow2.bonita.APITestCase;

// FIXME: move to module
public class CopyToolTest extends APITestCase {

  @Override
  protected void setUp() throws Exception {
    super.setUp();
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
  }

  public void testCopySet() {
    Set<String> set = null;
    Set<String> res = CopyTool.copy(set);
    assertNotNull(res);

    res = null;
    set = new HashSet<String>();
    Integer i;
    for (i = 0; i < 2; i++) {
      set.add(i.toString());
    }
    res = CopyTool.copy(set);
    assertNotNull(res);
    assertEquals(set, res);
  }

  public void testCopyList() {
    List<String> list = null;
    List<String> res = CopyTool.copy(list);
    assertNotNull(res);

    res = null;
    list = new ArrayList<String>();
    Integer i;
    for (i = 0; i < 2; i++) {
      list.add(i.toString());
    }
    res = CopyTool.copy(list);
    assertNotNull(res);
    assertEquals(list, res);
  }

  public void testCopyMap() {
    Map<String, String> map = null;
    Map<String, String> res = CopyTool.copy(map);
    assertNotNull(res);

    res = null;
    map = new HashMap<String, String>();
    Integer i;
    for (i = 0; i < 2; i++) {
      map.put(i.toString(), i.toString());
    }
    res = CopyTool.copy(map);
    assertNotNull(res);
    assertEquals(map, res);
  }

  public void testCopyDate() {
    Date date = null;
    Date res = CopyTool.copy(date);
    assertNull(res);

    res = null;
    date = new Date(System.currentTimeMillis());
    res = CopyTool.copy(date);
    assertNotNull(res);
    assertEquals(date, res);
  }





}
