/**
 * Copyright (C) 2009  BonitaSoft S.A..
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
package org.ow2.bonita.util;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * 
 * @author Matthieu Chaffotte
 *
 */
public class GroovyUtilTest extends TestCase {

  final static Map<String, Object> variables;

  static {
    variables = new HashMap<String, Object>();
    variables.put("yksi", Integer.valueOf(8));
    variables.put("man", Integer.valueOf(2));
    variables.put("enumCheck", "no");
    variables.put("null", null);
    variables.put("s_var", "ok");
    variables.put("s-var", "OK");
    variables.put("0_Ty", "OK");
    variables.put("_0Ty", "OK");
    variables.put("Ty_0", "OK");
    variables.put("Ty0", "OK");
    variables.put("j", Integer.valueOf(5));

    Set<String> values = new HashSet<String>();
    values.add("yes");
    values.add("no");
    variables.put("test", "no");
  }

  public void testNullString() {
    String expression = null;
    try {
      GroovyUtil.evaluate(expression, variables);
      fail("The expression cannot be null");
    } catch (GroovyException e) {
    }
  }

  public void testEmptyString() {
    String expression = "";
    try {
      GroovyUtil.evaluate(expression, variables);
      fail("The expression cannot be empty");
    } catch (GroovyException e) {
    }
  }

  public void testWhiteSpaceString() {
    String expression = "   ";
    try {
      GroovyUtil.evaluate(expression, variables);
      fail("The expression cannot be empty");
    } catch (GroovyException e) {
    }
  }

  public void testAStringExpression() {
    String expression = "Hello Brian";
    try {
      GroovyUtil.evaluate(expression, variables);
      fail(expression + ": is not a Groovy Expression");
    } catch (GroovyException e) {
    }
  }

  public void testAWrongGroovyExpression() {
    String expression = "4 + two";
    try {
      GroovyUtil.evaluate(expression, variables);
      fail(expression + ": is not a Groovy Expression");
    } catch (GroovyException e) {
    }
  }

  public void testUnderScoreVariable() throws GroovyException {
    String expression = "${s_var}";
    String actual = (String) GroovyUtil.evaluate(expression, variables);
    Assert.assertEquals("ok", actual);
  }

  public void testHyphenVariable() throws GroovyException {
    try {
      String expression = "${s-var}";
      GroovyUtil.evaluate(expression, variables);
      fail("A variable name cannot contain an hyphen");
    } catch (GroovyException e) {
    }
  }

  public void testVariableOne() throws GroovyException {
    try {
      String expression = "${0_Ty}";
      GroovyUtil.evaluate(expression, variables);
      fail("A variable name cannot begin with a number");
    } catch (GroovyException e) {
    }
  }

  public void testVariableTwo() throws GroovyException {
    String expression = "${_0Ty}";
    String actual = (String) GroovyUtil.evaluate(expression, variables);
    Assert.assertEquals("OK", actual);
  }

  public void testVariableThree() throws GroovyException {
    String expression = "${Ty_0}";
    String actual = (String) GroovyUtil.evaluate(expression, variables);
    Assert.assertEquals("OK", actual);
  }

  public void testVariableFour() throws GroovyException {
    String expression = "${Ty0}";
    String actual = (String) GroovyUtil.evaluate(expression, variables);
    Assert.assertEquals("OK", actual);
  }

  public void testSimpleExpressionWithoutMarkers() {
    String expression = "4 + yksi";
    try {
      GroovyUtil.evaluate(expression, variables);
      fail("this expression should not be evaluated");
    } catch (GroovyException e) {
    }
  }

  public void testNoGroovyMarkers() {
    String expression = "} Brian ${ is";
    try {
      GroovyUtil.evaluate(expression, variables);
      fail("this expression should not be evaluated");
    } catch (GroovyException e) {
    }
  }

  public void testSimpleGroovyExpressionWitANullVariable() throws GroovyException {
    String expression = "${null}";
    Assert.assertNull(GroovyUtil.evaluate(expression, variables));
  }

  public void testGroovyExpressionWithANullVariable() {
    try {
      String expression = "${null + 4}";
      GroovyUtil.evaluate(expression, variables);
      fail("this expression should not be evaluated because of a NullPointer");
    } catch (GroovyException e) {
    }
  }

  public void testGroovyExpressionWithANullVariableEquals() throws GroovyException {
    String expression = "${null.equals(\"ok\")}";
    Boolean bool = (Boolean) GroovyUtil.evaluate(expression, variables);
    Assert.assertFalse(bool);
  }

  public void testSimpleGroovyExpression() throws GroovyException {
    String expression = "${4 + yksi}";
    Integer actual = (Integer) GroovyUtil.evaluate(expression, variables);
    Assert.assertEquals(Integer.valueOf(12), actual);
  }

  public void testGroovyAndStringExpression() throws GroovyException {
    String expression = "${4 + yksi} hello";
    String actual = (String) GroovyUtil.evaluate(expression, variables);
    Assert.assertEquals("12 hello", actual);
  }

  public void testGroovyAndStringEndExpression() throws GroovyException {
    String expression = "${4 + yksi} hello }";
    String actual = (String) GroovyUtil.evaluate(expression, variables);
    Assert.assertEquals("12 hello }", actual);
  }

  public void testBooleanExpression() throws GroovyException {
    String expression = "${enumCheck.equals(\"no\")}";
    Boolean actual = (Boolean) GroovyUtil.evaluate(expression, variables);
    Assert.assertTrue(actual);
  }

  public void testDateGroovyExpression() throws GroovyException {
    String expression = "${new Date(1000000l)}";
    Date actual = (Date) GroovyUtil.evaluate(expression, variables);
    Assert.assertEquals(new Date(1000000l), actual);
  }

  public void testTwoStringGroovyExpression() throws GroovyException {
    String expression = "${4 + yksi}${3}";
    String actual = (String) GroovyUtil.evaluate(expression, variables);
    Assert.assertEquals("123", actual);
  }

  public void testStringGroovyExpression() throws GroovyException {
    String expression = "Where is ${men = ['James', 'John', 'Brian']; men[man]}?";
    String actual = (String) GroovyUtil.evaluate(expression, variables);
    Assert.assertEquals("Where is Brian?", actual);
  }

  public void testGroovyScriptIf() throws GroovyException {
    String expression = "${int i = 5; if (i == 5) {return true}}";
    Boolean actual = (Boolean) GroovyUtil.evaluate(expression, variables);
    Assert.assertTrue(actual);
  }

  public void testGroovyScript() throws GroovyException {
    String expression = "${j; if (j == 5) {return true}}";
    Boolean actual = (Boolean) GroovyUtil.evaluate(expression, variables);
    Assert.assertTrue(actual);
  }

  public void testStringTwoGroovyExpressions() throws GroovyException {
    String expression = "Where is ${men = ['James', 'John', 'Brian']; men[man]}? He is in the ${places=['kitchen', 'bathroom', 'garden']; places[0]}.";
    String actual = (String) GroovyUtil.evaluate(expression, variables);
    Assert.assertEquals("Where is Brian? He is in the kitchen.", actual);
  }

  public void testTwoGroovyExpressions() throws GroovyException {
    String expression = "${men = ['James', 'John', 'Brian']; men[man]}? He is in the ${places=['kitchen', 'bathroom', 'garden']; places[1]}.";
    String actual = (String) GroovyUtil.evaluate(expression, variables);
    Assert.assertEquals("Brian? He is in the bathroom.", actual);
  }

  public void testExpressionsWithAWrongGroovyExpression() {
    String expression = "${men = ['James', 'John', 'Brian']; men[man]}? He is in the ${places=['kitchen', 'bathroom', 'garden']; place[1]}.";
    try {
      GroovyUtil.evaluate(expression, variables);
      fail(expression + ": is not a Groovy Expression");
    } catch (GroovyException e) {
    }
  }

  public void testBooleanExpressionFromEnumeration() throws GroovyException {
    String expression = "${test.equals(\"no\")}";
    Boolean actual = (Boolean) GroovyUtil.evaluate(expression, variables);
    assertTrue(actual);
  }

  public void testCannotUseAPIAccessorWhenEngineIsNotStarted() {
    String script = "${" + BonitaConstants.API_ACCESSOR + ".getIdentityAPI().getAllUsers();}";
    try {
      GroovyUtil.evaluate(script, variables);
      fail("The engine was not started");
    } catch (GroovyException e) {
      assertTrue(e.getMessage().contains("domain"));
    }
  }

}
