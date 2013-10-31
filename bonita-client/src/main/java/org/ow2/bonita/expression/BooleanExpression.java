/**
 * Copyright (C) 2009  Bull S. A. S.
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

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.ow2.bonita.util.Misc;

/**
 * 
 * @author Matthieu Chaffotte
 *
 */
public class BooleanExpression {

  private static final String JAVA_FIELD_PATTERN = "^[a-z][a-zA-Z0-9]*$";
  private List<String> tokens;

  /**
   * Create a new boolean expression.
   * @param expression the expression
   * @throws InvalidExpressionException if the expression is not valid
   */
  public BooleanExpression(final String expression)
  throws InvalidExpressionException {
    Misc.checkArgsNotNull(expression);
    tokens = new Vector<String>();
    checkExpression(formatExpression(expression));
  }

  /**
   * Checks whether the expression given in parameter is a boolean expression.
   * @param expression the expression to check
   * @throws InvalidExpressionException if the expression is not valid
   */
  private void checkExpression(String expression)
  throws InvalidExpressionException {
    expression = expression.trim();
    if (expression.length() == 0) {
      throw new InvalidExpressionException(expression,
          "The expression cannot be an empty String");
    }
    if (expression.startsWith(KeyWord.LEFT_PARENTHESIS.value())
        && isABloc(expression)) {
      tokens.add(KeyWord.LEFT_PARENTHESIS.value());
      checkExpression(expression.substring(1, expression.length() - 1));
      tokens.add(KeyWord.RIGHT_PARENTHESIS.value());
    } else {
      if (isField(expression)) {
        if (!isConformField(expression)) {
          throw new InvalidExpressionException(expression,
               "The field is not conform to expression " + JAVA_FIELD_PATTERN);
        } else {
          tokens.add(expression);
        }
      } else {
        String not = Character.toString(expression.charAt(0));
        if (KeyWord.NOT.value().equals(not)) {
          tokens.add(KeyWord.NOT.value());
          checkExpression(expression.substring(1));
        } else {
          List<String> expressions =
            getExpressionsFromBinaryOperation(expression);
          String operator = expressions.get(0);
          String left = expressions.get(1).trim();
          String right = expressions.get(2).trim();
          if (left.length() == 0) {
            throw new InvalidExpressionException(expression,
                "The left expression is missing");
          }
          if (operator.length() == 0) {
            throw new InvalidExpressionException(expression,
                "An operator is missing");
          }
          if (right.length() == 0) {
            throw new InvalidExpressionException(expression,
                "The right expression is missing");
          }
          if (operator.equals(KeyWord.AND.value())
              || operator.equals(KeyWord.OR.value())
              || operator.equals(KeyWord.XOR.value())) {
            
            checkExpression(left);
            tokens.add(operator);
            checkExpression(right);
          } else {
            throw new InvalidExpressionException(expression,
                "Unknown operator : \"" + operator + "\"");
          }
        }
      }
    }
  }

  /**
   * Checks whether the expression is a bloc.
   * A bloc is a well-formed expression in parenthesis.
   * (the number of left parentheses equals the number of right parentheses)
   * @param expression the expression to check
   * @return true if it is a bloc; false otherwise
   * @throws InvalidExpressionException if the expression is invalid
   */
  private static boolean isABloc(final String expression)
  throws InvalidExpressionException {
    boolean bloc = false;
    if(expression.startsWith(KeyWord.LEFT_PARENTHESIS.value())) {
      int index = 1;
      int count = 1;
      char current;
      while (index < expression.length() && count > 0) {
        current = expression.charAt(index);
        if (KeyWord.LEFT_PARENTHESIS.value().equals(String.valueOf(current))) {
          count++;
        } else if (
            KeyWord.RIGHT_PARENTHESIS.value().equals(String.valueOf(current))) {
          count--;
        }
        index++;
      }
      if (count > 0) {
        throw new InvalidExpressionException(expression,
            "A right parenthesis is missing");
      }
      if (index == expression.length()) {
        bloc = true;
      }
    }
    return bloc;
  }

  /**
   * Returns the operator and both expressions from a binary operation.
   * @param expression the expression to split
   * @return the list containing the operator, the left expression and
   * the right expression. (in this order)
   * @throws InvalidExpressionException if the expression is invalid
   */
  private static List<String> getExpressionsFromBinaryOperation(
      final String expression) throws InvalidExpressionException {
    List<String> exp = new ArrayList<String>();
    int count = 0;
    int i = 1;
    int length = expression.length();
    if (expression.startsWith(KeyWord.LEFT_PARENTHESIS.value())) {
      count = 1;
      char current;
      while (i < length && count > 0) {
        current = expression.charAt(i);
        if (KeyWord.LEFT_PARENTHESIS.value().equals(String.valueOf(current))) {
          count++;
        } else if (
            KeyWord.RIGHT_PARENTHESIS.value().equals(String.valueOf(current))) {
          count--;
        }
        i++;
      }
      if (count > 0) {
        throw new InvalidExpressionException(expression,
            "A right parenthesis is missing");
      }
    } else {
      i = 0;
      char current;
      String oper = "";
      boolean op = false;
      while (i < (length - 1) && !op) {
        current = expression.charAt(i);
        oper = String.valueOf(current);
        if (oper.equals(KeyWord.AND.value()) || oper.equals(KeyWord.OR.value())
            || oper.equals(KeyWord.XOR.value())) {
          op = true;
        }
        i++;
      }
      if (!op) {
        throw new InvalidExpressionException(expression,
            "Operator not valid");
      }
      i--;
    }
    String left = expression.substring(0, i);
    String tail = expression.substring(i);
    String operator = getOperator(tail);
    int cut = tail.indexOf(operator);
    String right = tail.substring(cut + operator.length(), tail.length());
    exp.add(operator);
    exp.add(left);
    exp.add(right);
    return exp;
  }

  /**
   * Gets the first operator from the given expression.
   * It can be a valid or an invalid operator.
   * @param expression the expression
   * @return the first operator
   */
  private static String getOperator(String expression) {
    expression = expression.trim();
    String operator = expression.substring(0, 1);
    if (expression.startsWith(KeyWord.LEFT_PARENTHESIS.value())
        || expression.startsWith(KeyWord.NOT.value())) {
      operator = "";
    } else if (!operator.equals(KeyWord.AND.value())
        && !operator.equals(KeyWord.OR.value())
        && !operator.equals(KeyWord.XOR.value())) {
      int index = 0;
      boolean end = false;
      char c = 'a';
      StringBuilder builder = new StringBuilder();
      while (index < expression.length() && !end) {
        c = expression.charAt(index);
        builder.append(c);
        if (Character.isWhitespace(c)) {
          end = true;
        }
        index++;
      }
      operator = builder.toString();
    }
    return operator;
  }

  /**
   * Checks whether the expression is a field.
   * A field contains only a chain of letters and digits.
   * @param expression the expression
   * @return true if the expression is a field; false otherwise
   */
  private static boolean isField(final String expression) {
    int i = 0;
    char ch = ' ';
    boolean field = true;
    while (i < expression.length() && field) {
      ch = expression.charAt(i);
      if (!Character.isJavaIdentifierPart(ch)) {
        field = false;
      }
      i++;
    }
    return field;
  }

  /**
   * Checks if the field is conform to the java field pattern:
   * "^[a-z][a-zA-Z0-9]*$".
   * @param expression the expression
   * @return true if this expression matches this pattern; false otherwise
   */
  private static boolean isConformField(final String expression) {
    return expression.matches(JAVA_FIELD_PATTERN);
  }

  /**
   * Gets an formatted expression. Tab, newline and carriage-return characters
   * are replaced by a space character.
   * @param expression
   * @return
   */
  private static String formatExpression(String expression) {
    expression = expression.replaceAll("\t", " ");
    expression = expression.replaceAll("\n", " ");
    return expression.replaceAll("\r", " ");
  }

  public List<String> getTokens() {
    return tokens;
  }
}
