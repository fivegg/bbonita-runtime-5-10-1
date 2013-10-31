package org.ow2.bonita.variable;

import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.ow2.bonita.APITestCase;
import org.ow2.bonita.facade.QueryRuntimeAPI;
import org.ow2.bonita.facade.uuid.ActivityInstanceUUID;
import org.ow2.bonita.util.BonitaException;
import org.ow2.bonita.util.DateUtil;


public abstract class VariableTestCase extends APITestCase {

  /*
   *  check setup of string variable
   */
  private static void checkStringVariable(String str) {
    assertNotNull(str);
    if ("initial value".equals(str)) {
      assertEquals("initial value", str);
    } else {
      assertEquals("user", str);
    }
  }

  private static void checkEnumVariable(String str) {
    assertNotNull(str);
    assertEquals("iiii", str);
  }

  /*
   *  check setup of float variable
   */
  private static void checkFloatVariable(Double flt) {
    assertNotNull(flt);
    if (flt != null) {
      assertEquals(42.42, flt);
    }
  }

  /*
   *  check setup of integer variable
   */
  private static void checkIntegerVariable(Long i) {
    assertNotNull(i);
    if (i != null) {
      assertEquals(42L, i.intValue());
    }
  }

  /*
   *  check setup of boolean variable
   */
  private static void checkBooleanVariable(Boolean b) {
    assertNotNull(b);
    if (b != null) {
      assertEquals(true, b.booleanValue());
    }
  }

  /*
   *  check setup of date variable
   */
  private static void checkDateVariable(Date d) {
    assertNotNull(d);
    if (d != null) {
      assertEquals(DateUtil.parseDate("2008/07/31/14/00/00"), d);
    }
  }

  /*
   * check creation/availability of the 4 variables
   */
  protected static void checkVariables(QueryRuntimeAPI queryRuntimeAPI, ActivityInstanceUUID activityUUID, 
      int expectedNb) throws BonitaException {
    Map<String, Object> variables = queryRuntimeAPI.getActivityInstanceVariables(activityUUID);
    assertEquals(expectedNb, variables.size());
    for (Map.Entry<String, Object> entry : variables.entrySet()) {
      Object var = entry.getValue();
      String varName = entry.getKey();
      if (varName.startsWith("enum_act")) {
        String str = (String)var;
        checkEnumVariable(str);
      } else if (var instanceof String) {
        String str = (String)var;
        checkStringVariable(str);
      } else if (var instanceof Double) {
        Double flt = (Double)var;
        checkFloatVariable(flt);
      } else if (var instanceof Long) {
        Long i = (Long)var;
        checkIntegerVariable(i);
      } else if (var instanceof Boolean) {
        Boolean b = (Boolean)var;
        checkBooleanVariable(b);
      } else if (var instanceof Date) {
        Date d = (Date)var;
        checkDateVariable(d);
      } else {
        fail("Unknown variable: " + var);
      }
    }
  }

  /*
   *   check creation/availability of expected propagated variables
   */
  protected static void checkPropagatedVariables(QueryRuntimeAPI queryRuntimeAPI,
      ActivityInstanceUUID activityUUID, int expectedNb) throws BonitaException {
    Set<String> keyList = queryRuntimeAPI.getActivityInstanceVariables(activityUUID).keySet();

    assertEquals(expectedNb, keyList.size());
    Iterator<String> it = keyList.iterator();
    while (it.hasNext()) {
      String key = it.next();
      Object var = queryRuntimeAPI.getActivityInstanceVariable(activityUUID, key);
      if (var instanceof String) {
        String str = (String)var;
        checkStringVariable(str);
      } else if (var instanceof Double) {
        Double flt = (Double)var;
        checkFloatVariable(flt);
      } else if (var instanceof Long) {
        Long i = (Long)var;
        checkIntegerVariable(i);
      }
    }
  }
}
