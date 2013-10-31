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

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.TestCase;

import org.ow2.bonita.connector.core.ConnectorTest;

public class EvaluatorTest extends TestCase {

  private Map<String, Boolean> variables;
  
  protected static final Logger LOG = Logger.getLogger(ConnectorTest.class.getName());

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    if (EvaluatorTest.LOG.isLoggable(Level.WARNING)) {
      EvaluatorTest.LOG.warning("======== Starting test: " + this.getClass().getName() + "." + this.getName() + "() ==========");
    }
  }

  @Override
  protected void tearDown() throws Exception {
    if (EvaluatorTest.LOG.isLoggable(Level.WARNING)) {
      EvaluatorTest.LOG.warning("======== Ending test: " + this.getName() + "==========");
    }
    super.tearDown();
  }

  public void testSimpleEvaluation() {
    variables = getBasicMap();

    evaluate("trueName", true);
    evaluate("falseAddress", false);
  }

  public void testSimpleEvaluationInParentheses() {
    variables = getBasicMap();

    evaluate("(trueName)", true);
    evaluate("(falseAddress)", false);

    evaluate("((trueName))", true);
    evaluate("((falseAddress))", false);

    evaluate("((((trueName))))", true);
    evaluate("((((falseAddress))))", false);
  }

  public void testSimpleUnaryOperationEvaluation() {
    variables = getBasicMap();

    evaluate("!trueName", false);
    evaluate("!falseAddress", true);

    evaluate("!!trueName", true);
    evaluate("!!falseAddress", false);

    evaluate("!!!trueName", false);
    evaluate("!!!falseAddress", true);
  }

  public void testSimpleUnaryOperationEvaluationInParentheses() {
    variables = getBasicMap();

    evaluate("(!trueName)", false);
    evaluate("!(trueName)", false);
    evaluate("!(falseAddress)", true);
    evaluate("(!falseAddress)", true);

    evaluate("!(!trueName)", true);
    evaluate("!(!falseAddress)", false);
  }

  public void testSimpleBinaryOperationEvaluation() {
    variables = getBasicMap();

    // Truth tables for and, or and xor
    evaluate("trueName & trueAddress", true);
    evaluate("falseName & trueAddress", false);
    evaluate("trueName & falseAddress", false);
    evaluate("falseName & falseAddress", false);

    evaluate("trueName | trueAddress", true);
    evaluate("falseName | trueAddress", true);
    evaluate("trueName | falseAddress", true);
    evaluate("falseName | falseAddress", false);

    evaluate("trueName ^ trueAddress", false);
    evaluate("falseName ^ trueAddress", true);
    evaluate("trueName ^ falseAddress", true);
    evaluate("falseName ^ falseAddress", false);
  }

  public void testComplexEvaluateEvaluation() {
    variables = getBasicMap();

    // Truth tables in complex expressions
    evaluate("trueName & trueAge & trueAddress", true);
    evaluate("trueName & trueAge & falseAddress", false);
    evaluate("trueName & falseAge & trueAddress", false);
    evaluate("trueName & falseAge & falseAddress", false);
    evaluate("falseName & trueAge & trueAddress", false);
    evaluate("falseName & trueAge & falseAddress", false);
    evaluate("falseName & falseAge & trueAddress", false);
    evaluate("falseName & falseAge & falseAddress", false);

    evaluate("trueName ^ trueAge ^ trueAddress", true);
    evaluate("trueName ^ trueAge ^ falseAddress", false);
    evaluate("trueName ^ falseAge ^ trueAddress", false);
    evaluate("trueName ^ falseAge ^ falseAddress", true);
    evaluate("falseName ^ trueAge ^ trueAddress", false);
    evaluate("falseName ^ trueAge ^ falseAddress", true);
    evaluate("falseName ^ falseAge ^ trueAddress", true);
    evaluate("falseName ^ falseAge ^ falseAddress", false);

    evaluate("trueName | trueAge | trueAddress", true);
    evaluate("trueName | trueAge | falseAddress", true);
    evaluate("trueName | falseAge | trueAddress", true);
    evaluate("trueName | falseAge | falseAddress", true);
    evaluate("falseName | trueAge | trueAddress", true);
    evaluate("falseName | trueAge | falseAddress", true);
    evaluate("falseName | falseAge | trueAddress", true);
    evaluate("falseName | falseAge | falseAddress", false);

    evaluate("trueName & trueAge ^ trueAddress", false);
    evaluate("trueName & trueAge ^ falseAddress", true);
    evaluate("trueName & falseAge ^ trueAddress", true);
    evaluate("trueName & falseAge ^ falseAddress", false);
    evaluate("falseName & trueAge ^ trueAddress", true);
    evaluate("falseName & trueAge ^ falseAddress", false);
    evaluate("falseName & falseAge ^ trueAddress", true);
    evaluate("falseName & falseAge ^ falseAddress", false);

    evaluate("trueName ^ trueAge & trueAddress", false);
    evaluate("trueName ^ trueAge & falseAddress", true);
    evaluate("trueName ^ falseAge & trueAddress", true);
    evaluate("trueName ^ falseAge & falseAddress", true);
    evaluate("falseName ^ trueAge & trueAddress", true);
    evaluate("falseName ^ trueAge & falseAddress", false);
    evaluate("falseName ^ falseAge & trueAddress", false);
    evaluate("falseName ^ falseAge & falseAddress", false);

    evaluate("trueName ^ trueAge | trueAddress", true);
    evaluate("trueName ^ trueAge | falseAddress", false);
    evaluate("trueName ^ falseAge | trueAddress", true);
    evaluate("trueName ^ falseAge | falseAddress", true);
    evaluate("falseName ^ trueAge | trueAddress", true);
    evaluate("falseName ^ trueAge | falseAddress", true);
    evaluate("falseName ^ falseAge | trueAddress", true);
    evaluate("falseName ^ falseAge | falseAddress", false);

    evaluate("trueName | trueAge ^ trueAddress", true);
    evaluate("trueName | trueAge ^ falseAddress", true);
    evaluate("trueName | falseAge ^ trueAddress", true);
    evaluate("trueName | falseAge ^ falseAddress", true);
    evaluate("falseName | trueAge ^ trueAddress", false);
    evaluate("falseName | trueAge ^ falseAddress", true);
    evaluate("falseName | falseAge ^ trueAddress", true);
    evaluate("falseName | falseAge ^ falseAddress", false);

    evaluate("trueName & trueAge | trueAddress", true);
    evaluate("trueName & trueAge | falseAddress", true);
    evaluate("trueName & falseAge | trueAddress", true);
    evaluate("trueName & falseAge | falseAddress", false);
    evaluate("falseName & trueAge | trueAddress", true);
    evaluate("falseName & trueAge | falseAddress", false);
    evaluate("falseName & falseAge | trueAddress", true);
    evaluate("falseName & falseAge | falseAddress", false);

    evaluate("trueName | trueAge & trueAddress", true);
    evaluate("trueName | trueAge & falseAddress", true);
    evaluate("trueName | falseAge & trueAddress", true);
    evaluate("trueName | falseAge & falseAddress", true);
    evaluate("falseName | trueAge & trueAddress", true);
    evaluate("falseName | trueAge & falseAddress", false);
    evaluate("falseName | falseAge & trueAddress", false);
    evaluate("falseName | falseAge & falseAddress", false);
  }

  public void testComplexEvaluateEvaluationInParenthesss() {
    variables = getBasicMap();

    // Truth tables in more complex expressions
    evaluate("(trueName | trueAge) & trueAddress", true);
    evaluate("(trueName | trueAge) & falseAddress", false);
    evaluate("(trueName | falseAge) & trueAddress", true);
    evaluate("(trueName | falseAge) & falseAddress", false);
    evaluate("(falseName | trueAge) & trueAddress", true);
    evaluate("(falseName | trueAge) & falseAddress", false);
    evaluate("(falseName | falseAge) & trueAddress", false);
    evaluate("(falseName | falseAge) & falseAddress", false);

    evaluate("trueName | (trueAge & trueAddress)", true);
    evaluate("trueName | (trueAge & falseAddress)", true);
    evaluate("trueName | (falseAge & trueAddress)", true);
    evaluate("trueName | (falseAge & falseAddress)", true);
    evaluate("falseName | (trueAge & trueAddress)", true);
    evaluate("falseName | (trueAge & falseAddress)", false);
    evaluate("falseName | (falseAge & trueAddress)", false);
    evaluate("falseName | (falseAge & falseAddress)", false);

    evaluate("(trueName & trueAge) | trueAddress", true);
    evaluate("(trueName & trueAge) | falseAddress", true);
    evaluate("(trueName & falseAge) | trueAddress", true);
    evaluate("(trueName & falseAge) | falseAddress", false);
    evaluate("(falseName & trueAge) | trueAddress", true);
    evaluate("(falseName & trueAge) | falseAddress", false);
    evaluate("(falseName & falseAge) | trueAddress", true);
    evaluate("(falseName & falseAge) | falseAddress", false);

    evaluate("trueName & (trueAge | trueAddress)", true);
    evaluate("trueName & (trueAge | falseAddress)", true);
    evaluate("trueName & (falseAge | trueAddress)", true);
    evaluate("trueName & (falseAge | falseAddress)", false);
    evaluate("falseName & (trueAge | trueAddress)", false);
    evaluate("falseName & (trueAge | falseAddress)", false);
    evaluate("falseName & (falseAge | trueAddress)", false);
    evaluate("falseName & (falseAge | falseAddress)", false);
  }

  public void testMoreComplexEvaluateEvaluationInParenthesss() {
    variables = getBasicMap();

    evaluate("!trueName & (trueAge | trueAddress)", false);
    evaluate("!trueName & (trueAge | !trueAddress)", false);
    evaluate("!!trueName & (!!trueAge | !!trueAddress)", true);
    evaluate("!(trueName ^ trueAge) & (trueAge | falseAddress)", true);
    evaluate("(((trueName ^ (trueAge & falseAddress)) | (falseAddress))"
      + 		"& (falseAddress | falseAddress | trueAge))", true);
  }
  
  public void testFailVariables() {
    variables = getBasicMap();  
    failEvaluation("trueMan");
    failEvaluation("!trueName & (trueLove | trueAddress)");
  }
  
  public void testCloseFieldNames() {
    variables = getBasicMap();
    variables.put("ab", true);
    variables.put("abc", false);

    evaluate("abc & ab", false);
    evaluate("abc ^ ab", true);
  }
  
  private void failEvaluation(String expression) {
    ExpressionEvaluator evaluator = null;
    try {
      evaluator = new ExpressionEvaluator(expression);
    } catch (InvalidExpressionException iee) {
      fail("ouch");
    }
    try {
      evaluator.evaluate(variables);
      fail("This variable does not exist !");
    } catch (VariableNotFoundException vnfe) {
      //e.printStackTrace();
    }
  }

  private void evaluate(String expression, boolean result) {
    try {
      ExpressionEvaluator evaluator = new ExpressionEvaluator(expression);
      boolean eval = evaluator.evaluate(variables);
      if (result) {
        assertTrue(eval);
      } else {
        assertFalse(eval);
      }
    } catch (Exception e) {
      fail("ouch !");
    }
  }

  private Map<String, Boolean> getBasicMap() {
    Map<String, Boolean> variables = new HashMap<String, Boolean>();
    variables.put("trueName", true);
    variables.put("falseName", false);
    variables.put("trueAddress", true);
    variables.put("falseAddress", false);
    variables.put("trueAge", true);
    variables.put("falseAge", false);
    return variables;
  }
}
