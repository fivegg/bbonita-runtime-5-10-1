/**
 * Copyright (C) 2006  Bull S. A. S.
 * Bull, Rue Jean Jaures, B.P.68, 78340, Les Clayes-sous-Bois
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
package org.ow2.bonita.expression;

import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.TestCase;

import org.ow2.bonita.connector.core.ConnectorTest;

public class BooleanExpressionTest extends TestCase {

  protected static final Logger LOG = Logger.getLogger(ConnectorTest.class.getName());

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    if (BooleanExpressionTest.LOG.isLoggable(Level.WARNING)) {
      BooleanExpressionTest.LOG.warning("======== Starting test: " + this.getClass().getName() + "." + this.getName() + "() ==========");
    }
  }

  @Override
  protected void tearDown() throws Exception {
    if (BooleanExpressionTest.LOG.isLoggable(Level.WARNING)) {
      BooleanExpressionTest.LOG.warning("======== Ending test: " + this.getName() + "==========");
    }
    super.tearDown();
  }
  
  
  public void testBasicWrongBooleanExpression() {
    checkFailBooleanExpression(null);
    checkFailBooleanExpression("");
    checkFailBooleanExpression("    ");
    checkFailBooleanExpression("\t");
    checkFailBooleanExpression("\n");
    checkFailBooleanExpression("\r");
  }
  
  public void testFieldNames() {
    checkSuccesBooleanExpression("name");
    checkSuccesBooleanExpression("familyName");

    checkFailBooleanExpression("FamilyName");
    checkFailBooleanExpression("family_name");
    checkFailBooleanExpression("NAME");
    checkFailBooleanExpression("8women");
    checkFailBooleanExpression("12345");
    checkSuccesBooleanExpression("number8");
  }
   
  public void testBooleanExpression() {
    checkSuccesBooleanExpression("! name");
    checkSuccesBooleanExpression("!! name");
    checkFailBooleanExpression("( name");
    checkFailBooleanExpression(") name");
    checkFailBooleanExpression("& name");
    checkFailBooleanExpression("| name");
    checkFailBooleanExpression("&& name");
    checkFailBooleanExpression("|| name");
    checkFailBooleanExpression("^| name");

    checkFailBooleanExpression("man &");
    checkFailBooleanExpression("man |");
    checkFailBooleanExpression("man !");

    checkFailBooleanExpression("()");
    checkFailBooleanExpression("man &  ");

    checkSuccesBooleanExpression("name & age");
    checkSuccesBooleanExpression("man | woman");
    checkSuccesBooleanExpression("man ^ woman");

    checkFailBooleanExpression("man or woman");
    checkFailBooleanExpression("man | & woman");
    checkFailBooleanExpression("man & | woman");

    checkFailBooleanExpression("man ! | woman");
    checkFailBooleanExpression("man ! ^ woman");
    checkFailBooleanExpression("man ! & woman");
    checkSuccesBooleanExpression("man | ! woman");
    checkSuccesBooleanExpression("man ^ ! woman");
    checkSuccesBooleanExpression("man & ! woman");

    checkSuccesBooleanExpression("(name)");
    checkSuccesBooleanExpression("(!name)");
    checkSuccesBooleanExpression("!(name)");
    checkFailBooleanExpression("(name ! age)");
    checkSuccesBooleanExpression("(name & age)");
    checkSuccesBooleanExpression("(name ^ age)");
    checkSuccesBooleanExpression("(name | age)");

    checkSuccesBooleanExpression("((name))");
    checkSuccesBooleanExpression("((name & (age | sex)))");
    checkSuccesBooleanExpression("((name & surname) & (age | sex))");
    checkSuccesBooleanExpression("((name & surname) & (age|sex))");
    checkSuccesBooleanExpression("(((name) & surname) & (age|sex))");
    checkFailBooleanExpression("(((name) & surname & (age|sex))");

    checkSuccesBooleanExpression("( ( name    & surname     )&(  age | sex   )   )");

    checkFailBooleanExpression("((name & surname) and (age|sex))");
    checkFailBooleanExpression("((name test surname) & (age|sex))");
    checkFailBooleanExpression("((name ^ surname) & )");
    checkFailBooleanExpression("((name ! surname) & (age|sex))");
    checkFailBooleanExpression("((name & surname)(age|sex))");

    checkSuccesBooleanExpression("name & age & address | town");
    checkSuccesBooleanExpression("(name & age) & (address | town) | (sex ^ weight)");
    checkSuccesBooleanExpression("(name&age)&(address|town)|(sex^weight)");
  }

  private void checkFailBooleanExpression(String expression) {
    try {
      new BooleanExpression(expression);
      fail("Exception should have been thrown");
    } catch (Exception e) {
      //e.printStackTrace();
    }
  }

  private void checkSuccesBooleanExpression(String expression) {
    try {
     assertNotNull(new BooleanExpression(expression));
    } catch (Exception e) {
      fail("It is a valid expression ! It cannot fail!");
    }
  }
}
