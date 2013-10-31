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

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.ow2.bonita.util.Misc;

/**
 * 
 * @author Matthieu Chaffotte
 *
 */
public class ExpressionEvaluator {

  private List<String> list;

  public ExpressionEvaluator(final String expression)
  throws InvalidExpressionException {
    BooleanExpression booleanExpression = new BooleanExpression(expression);
    list = booleanExpression.getTokens();
  }

  public boolean evaluate(Map<String, Boolean> variables)
  throws VariableNotFoundException {
    Misc.checkArgsNotNull(variables);

    for (Entry<String, Boolean> entry : variables.entrySet()) {
      String key = entry.getKey();
      for (int i = 0; i < list.size(); i++) {
        String token = list.get(i);
        if (token.equals(key)) {
          list.set(i, entry.getValue().toString());
        }
      }
    }

    for (String element : list) {
      if (isAField(element)
          && !element.equals(KeyWord.FALSE.value())
          && !element.equals(KeyWord.TRUE.value())) {
        throw new VariableNotFoundException(element);
      }
    }
    return evalBooleanExpressionInParentheses(list);
  }

  private boolean isAField(String element) {
    return element.matches("^[a-zA-Z_0-9]*$");
  }

  private boolean evalBooleanExpressionInParentheses(List<String> tokens) {
    if (tokens.contains(KeyWord.LEFT_PARENTHESIS.value())) {
      int fromIndex = tokens.indexOf(KeyWord.LEFT_PARENTHESIS.value());
      int toIndex = getEndExpressionInParentheses(tokens, fromIndex);
      List<String> toks = tokens.subList(fromIndex + 1 , toIndex);
      boolean eval = evalBooleanExpressionInParentheses(toks);
      tokens.set(fromIndex, String.valueOf(eval));
      tokens.remove(fromIndex + 1);
      tokens.remove(fromIndex + 1);
      return evalBooleanExpressionInParentheses(tokens);
    } else {
      return evalBooleanExpression(tokens);
    }
  }

  private int getEndExpressionInParentheses(List<String> tokens, int fromIndex) {
    int count = 1;
    int index = fromIndex + 1;
    while (index < tokens.size() && count > 0) {
      String token = tokens.get(index);
      if (token.equals(KeyWord.LEFT_PARENTHESIS.value())) {
        count++;
      } else if (token.equals(KeyWord.RIGHT_PARENTHESIS.value())) {
        count--;
      }
      index++;
    }
    return index - 1;
  }

  private boolean evalBooleanExpression(List<String> tokens) {
    if (tokens.size() == 1) {
      return Boolean.valueOf(tokens.get(0));
    }
    if (tokens.contains(KeyWord.NOT.value())) {
      int i = tokens.indexOf(KeyWord.NOT.value());
      int c = i;
      int count = 0;
      while (tokens.get(c).equals(KeyWord.NOT.value())) {
        count++;
        c++;
      }
      boolean not = !Boolean.valueOf(tokens.get(c));
      if (c % 2 == 0) {
        not = !not;
      }
      tokens.set(i, String.valueOf(not));
      for (int j = 0; j < count; j++) {
        tokens.remove(i + 1);
      }
      return evalBooleanExpression(tokens);
    }
    if (tokens.contains(KeyWord.AND.value())) {
      int i = tokens.indexOf(KeyWord.AND.value());
      boolean and = Boolean.valueOf(tokens.get(i - 1))
          & Boolean.valueOf(tokens.get(i + 1));
      tokens.set(i - 1, String.valueOf(and));
      tokens.remove(i);
      tokens.remove(i);
      return evalBooleanExpression(tokens);
    }
    if (tokens.contains(KeyWord.XOR.value())) {
      int i = tokens.indexOf(KeyWord.XOR.value());
      boolean xor = Boolean.valueOf(tokens.get(i - 1))
          ^ Boolean.valueOf(tokens.get(i + 1));
      tokens.set(i - 1, String.valueOf(xor));
      tokens.remove(i);
      tokens.remove(i);
      return evalBooleanExpression(tokens);
    }
    if (tokens.contains(KeyWord.OR.value())) {
      int i = tokens.indexOf(KeyWord.OR.value());
      boolean or = Boolean.valueOf(tokens.get(i - 1))
          | Boolean.valueOf(tokens.get(i + 1));
      tokens.set(i - 1, String.valueOf(or));
      tokens.remove(i);
      tokens.remove(i);
      return evalBooleanExpression(tokens);
    }
    return evalBooleanExpression(tokens);
  }

}
