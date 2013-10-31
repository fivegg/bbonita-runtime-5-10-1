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
 * 
 * Modified by Matthieu Chaffotte - BonitaSoft S.A.
 **/
package org.ow2.bonita.connector.core;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import org.ow2.bonita.connector.core.desc.ConnectorDescriptor;
import org.ow2.bonita.connector.core.desc.Getter;
import org.ow2.bonita.connector.core.desc.Setter;
import org.ow2.bonita.definition.TxHook;
import org.ow2.bonita.expression.ExpressionEvaluator;
import org.ow2.bonita.facade.APIAccessor;
import org.ow2.bonita.facade.runtime.ActivityInstance;

/**
 * The abstract class <code>Connector</code> is the superclass of all classes that represent connectors.
 * 
 * @author Matthieu Chaffotte
 * 
 */
public abstract class Connector implements TxHook {

  static final Logger LOG = Logger.getLogger(Connector.class.getName());

  /**
   * Enables or not a validation before executing the connector.
   */
  public boolean validation = true;

  /**
   * Checks whether the Connector is valid and well-formed.
   * 
   * @param c
   *          the Connector Class
   * @return a list of errors if there is any errors; null otherwise
   */
  public static List<ConnectorError> validateConnector(final Class<? extends Connector> c) {
    final List<ConnectorError> errors = checkRuntimeAnnotations(c);
    errors.addAll(checkGraphicalAnnotations(c));
    return errors;
  }

  protected static List<ConnectorError> checkGraphicalAnnotations(final Class<? extends Connector> c) {
    return ConnectorValidator.validateView(c);
  }

  protected static List<ConnectorError> checkRuntimeAnnotations(final Class<? extends Connector> c) {
    return ConnectorValidator.validateRuntime(c);
  }

  /**
   * Checks if the field exists in this connector.
   * 
   * @param c
   *          the connector class
   * @param fieldName
   *          the field name
   * @return true if the field exist; false otherwise
   */
  protected static boolean isFieldExist(final Class<? extends Connector> c, final String fieldName) {
    if (fieldName == null || "".equals(fieldName.trim())) {
      return false;
    }
    final Field field = getField(c, fieldName);
    boolean exist = false;
    if (field != null) {
      exist = true;
    }
    return exist;
  }

  /**
   * Checks whether the field is set. (different from null)
   * 
   * @param fieldName
   *          the field name
   * @return true if the field is set; false otherwise
   */
  private boolean isFieldSet(final String fieldName) {
    final Field field = getField(this.getClass(), fieldName);
    final Object value = getFieldValue(field);
    boolean set = true;
    if (value == null) {
      set = false;
    }
    return set;
  }

  /**
   * Checks whether the given field name belongs to an existing connector field
   * 
   * @param c
   *          the connector
   * @param fieldName
   *          the field name
   * @return true if the field is a connector one; false otherwise
   */
  public static boolean fieldExists(final Class<? extends Connector> c, final String fieldName) {
    final Field field = searchField(c, fieldName);
    boolean exists = true;
    if (field == null) {
      exists = false;
    }
    return exists;
  }

  /**
   * Returns the Field given by its Class and its name.
   * 
   * @param c
   *          the class of the wanted field
   * @param fieldName
   *          the name of the wanted field
   * @return the wanted Field
   */
  protected static Field getField(final Class<? extends Connector> c, final String fieldName) {
    Field field = searchField(c, fieldName);
    if (field == null) {
      final String upper = getFirstUpperCaseLetterFieldName(fieldName);
      field = searchField(c, upper);
    }
    return field;
  }

  /**
   * Searches a Field given by its Class and its name.
   * 
   * @param c
   *          the class of the wanted field
   * @param fieldName
   *          the name of the wanted field
   * @return the wanted Field
   */
  private static Field searchField(final Class<?> c, final String fieldName) {
    Field field = null;
    if (c != null) {
      int i = 0;
      final Field[] fields = c.getDeclaredFields();
      while (i < fields.length && field == null) {
        if (fields[i].getName().equals(fieldName)) {
          field = fields[i];
        }
        i++;
      }
      // let us check if the field is in the upper class
      if (field == null) {
        return searchField(c.getSuperclass(), fieldName);
      }
    }
    return field;
  }

  /**
   * Gives the field name from its method name.
   * 
   * @param methodName
   *          the method name of the field
   * @return the field name from its method name
   */
  public static String getFieldName(final String methodName) {
    int cut = 4;
    if (methodName.startsWith("is")) {
      cut = 3;
    }
    if (methodName.length() < cut) {
      return "";
    }
    final String end = methodName.substring(cut);
    final char c = methodName.charAt(cut - 1);
    final String begin = String.valueOf(c).toLowerCase();
    return begin.concat(end);
  }

  public static String getGetterName(final String fieldName) {
    final StringBuilder builder = new StringBuilder("get");
    builder.append(String.valueOf(fieldName.charAt(0)).toUpperCase());
    builder.append(fieldName.substring(1));
    return builder.toString();
  }

  private static String getFirstUpperCaseLetterFieldName(final String fieldName) {
    final String end = fieldName.substring(1);
    final char c = fieldName.charAt(0);
    final String begin = String.valueOf(c).toUpperCase();
    return begin.concat(end);
  }

  /**
   * Execute the content of the <code>Connector</code>. If the connector contains error an IllegalStateException is
   * thrown
   * 
   * @throws Exception
   *           if an exception occurs
   */
  public final void execute() throws Exception {
    final List<ConnectorError> errors = validate();
    if (!errors.isEmpty()) {
      final StringBuilder misconfigurationBuilder = new StringBuilder();
      for (final ConnectorError error : errors) {
        misconfigurationBuilder.append(error.getField()).append(": ");
        final Exception exception = error.getError();
        if (exception != null) {
          misconfigurationBuilder.append(exception.getMessage());
        } else {
          misconfigurationBuilder.append("unknown error");
        }
        misconfigurationBuilder.append("\n");
      }
      throw new IllegalStateException("The connector " + this.getClass().getName()
          + " cannot be executed due to a misconfiguration: " + misconfigurationBuilder.toString());
    }
    executeConnector();
  }

  /**
   * Execute the specific content of the <code>Connector</code>.
   * 
   * @throws Exception
   *           if an exception occurs
   */
  protected abstract void executeConnector() throws Exception;

  /**
   * Checks if field values are well-set.
   * 
   * @return a list containing <code>ConnectorError</code>; an empty list otherwise
   */
  protected abstract List<ConnectorError> validateValues();

  /**
   * Checks if all required fields are set.
   * 
   * @return a list containing <code>ConnectorError</code>; an empty list otherwise
   */
  public final List<ConnectorError> validate() {
    final List<ConnectorError> errors = new ArrayList<ConnectorError>();
    final ConnectorDescriptor descriptor = ConnectorDescriptorAPI.load(this.getClass());
    if (descriptor != null) {
      final List<Setter> inputs = descriptor.getInputs();
      if (inputs != null) {
        for (final Setter input : inputs) {
          ConnectorError error = null;
          final String fieldName = getFieldName(input.getSetterName());
          final boolean set = isFieldSet(fieldName);
          final String requiredExpression = input.getRequired();
          final String forbiddenExpression = input.getForbidden();
          if (requiredExpression != null) {
            final boolean required = getResultExpression(requiredExpression);
            if (required && !set) {
              error = new ConnectorError(fieldName, new IllegalArgumentException(
                  "This field is required so it must be set."));
              errors.add(error);
            }
          }

          if (forbiddenExpression != null) {
            final boolean forbidden = getResultExpression(forbiddenExpression);
            if (forbidden && set) {
              error = new ConnectorError(fieldName, new IllegalArgumentException("This field cannot be set because"
                  + " of other field values."));
              errors.add(error);
            }
          }
        }
      }

      if (errors.isEmpty()) {
        final List<ConnectorError> errorValues = validateValues();
        if (errorValues != null) {
          errors.addAll(errorValues);
        }
      }
    }
    return errors;
  }

  /**
   * Gives the result of the boolean expression.
   * 
   * @param expression
   *          the boolean expression
   * @return true if the result is OK; false otherwise
   */
  private boolean getResultExpression(final String expression) {
    boolean result = false;
    if ("".equals(expression)) {
      result = true;
    } else {
      String operands = expression.replace('(', ' ');
      operands = operands.replace(')', ' ');
      operands = operands.replace('&', ' ');
      operands = operands.replace('|', ' ');
      operands = operands.replace('!', ' ');
      final StringTokenizer token = new StringTokenizer(operands);
      final Map<String, Boolean> variables = new HashMap<String, Boolean>();
      while (token.hasMoreElements()) {
        final String fieldName = (String) token.nextElement();
        if (!variables.containsKey(fieldName)) {
          final Field field = getField(this.getClass(), fieldName);
          final Type type = field.getGenericType();
          boolean set = false;
          if (type.toString().equals("boolean") || type.toString().equals("class java.lang.Boolean")) {
            final Object value = getFieldValue(field);
            if (value == null) {
              set = false;
            } else {
              set = (Boolean) value;
            }
          } else {
            set = isFieldSet(fieldName);
          }
          variables.put(fieldName, set);
        }
      }
      try {
        result = new ExpressionEvaluator(expression).evaluate(variables);
      } catch (final Exception e) {
        LOG.severe(e.getMessage());
      }
    }
    return result;
  }

  /**
   * Gives the field value of a <code>Field</code>.
   * 
   * @param field
   *          the Field
   * @return the field value if it exists; null otherwise
   */
  private Object getFieldValue(final Field field) {
    Object value = null;
    field.setAccessible(true);
    try {
      value = field.get(this);
    } catch (final Exception e) {
      return null;
    }
    return value;
  }

  /**
   * Returns true if the Connector contains errors.
   * 
   * @return true if the Connector contains errors; false otherwise
   */
  public final boolean containsErrors() {
    return !validate().isEmpty();
  }

  /**
   * @see org.ow2.bonita.definition.TxHook#execute(APIAccessor, ActivityInstance)
   */
  @Override
  public void execute(final APIAccessor accessor, final ActivityInstance activityInstance) throws Exception {
    execute();
  }

  /**
   * Obtains the setter list of the connector
   * 
   * @return the list of setters
   */
  public final List<Setter> getSetters() {
    final ConnectorDescriptor descriptor = ConnectorDescriptorAPI.load(this.getClass());
    if (descriptor != null) {
      return descriptor.getInputs();
    } else {
      final Method[] methods = getAllSetters();
      if (methods == null) {
        return null;
      }
      final List<Setter> setters = new ArrayList<Setter>();
      for (final Method method : methods) {
        final String methodName = method.getName();
        setters.add(new Setter(methodName, null, null, method.getParameterTypes()));
      }
      return setters;
    }
  }

  /**
   * Obtains the getter list of the connector
   * 
   * @return the list of getters
   */
  public final List<Getter> getGetters() {
    final ConnectorDescriptor descriptor = ConnectorDescriptorAPI.load(this.getClass());
    if (descriptor != null) {
      return descriptor.getOutputs();
    } else {
      final Method[] methods = getAllGetters();
      if (methods == null) {
        return null;
      }
      final List<Getter> getters = new ArrayList<Getter>();
      for (final Method method : methods) {
        final String methodName = method.getName();
        getters.add(new Getter(getFieldName(methodName)));
      }
      return getters;
    }
  }

  protected <K, V> Map<K, V> bonitaListToMap(final List<List<Object>> array, final Class<K> key, final Class<V> value)
      throws IllegalArgumentException {
    Map<K, V> map = null;
    if (array != null && array.size() > 0) {
      map = new HashMap<K, V>(array.size());
      for (final List<Object> list : array) {
        try {
          map.put(key.cast(list.get(0)), value.cast(list.get(1)));
        } catch (final IndexOutOfBoundsException e) {
          throw new IllegalArgumentException(
              "Cannot convert the list of list in map because a sub list size is different from 2");
        }
      }
    }
    return map;
  }

  protected <K, V> Map<K, Object[]> bonitaListToArrayMap(final List<List<Object>> array, final Class<K> key,
      final Class<V> value) {
    Map<K, Object[]> map = null;
    if (array != null && array.size() > 0) {
      map = new HashMap<K, Object[]>();
      for (final List<Object> list : array) {
        map.put(key.cast(list.get(0)), new Object[] { value.cast(list.get(1)) });
      }
    }
    return map;
  }

  public static Method getMethod(final Class<?> connectorClass, final String methodName, final Class<?>[] paramTypes) {
    try {
      return connectorClass.getMethod(methodName, paramTypes);
    } catch (final Exception e) {
      if (paramTypes != null) {
        final Method[] methods = connectorClass.getMethods();
        for (final Method method : methods) {
          if (methodName.equals(method.getName())) {
            final Class<?>[] types = method.getParameterTypes();
            boolean check = true;
            for (int i = 0; i < types.length; i++) {
              if (!(types[i].isAssignableFrom(paramTypes[i]) || paramTypes[i].isAssignableFrom(types[i]) || isWrapped(
                  types[i], paramTypes[i]))) {
                check = false;
                break;
              }
            }
            if (check) {
              return method;
            }
          }
        }
      }
      return null;
    }
  }

  private static boolean isWrapped(final Class<?> a, final Class<?> b) {
    return a.equals(byte.class) && b.equals(Byte.class) || a.equals(short.class) && b.equals(Short.class)
        || a.equals(int.class) && b.equals(Integer.class) || a.equals(long.class) && b.equals(Long.class)
        || a.equals(float.class) && b.equals(Float.class) || a.equals(double.class) && b.equals(Double.class)
        || a.equals(char.class) && b.equals(Character.class) || a.equals(boolean.class) && b.equals(Boolean.class);
  }

  private static boolean isAGetterMethod(final Method m) {
    final String methodName = m.getName();
    return (methodName.startsWith("get") || methodName.startsWith("is"))
    // && m.getParameterTypes().equals(new Class<?>[0])
        && m.getReturnType() != null;
  }

  private static boolean isASetterMethod(final Method m) {
    final String methodName = m.getName();
    return methodName.startsWith("set")
    // && m.getReturnType().equals(new Class<?>[0])
        && m.getParameterTypes() != null;
  }

  private Method[] getAllSetters() {
    final List<Method> setters = new ArrayList<Method>();
    final Method[] methods = this.getClass().getDeclaredMethods();
    for (final Method method : methods) {
      if (isASetterMethod(method)) {
        setters.add(method);
      }
    }
    return setters.toArray(new Method[0]);
  }

  private Method[] getAllGetters() {
    final List<Method> getters = new ArrayList<Method>();
    final Method[] methods = this.getClass().getDeclaredMethods();
    for (final Method method : methods) {
      if (isAGetterMethod(method)) {
        getters.add(method);
      }
    }
    return getters.toArray(new Method[0]);
  }

  public static Type getGetterReturnType(final Class<? extends Connector> classConnector, final String outputName) {
    try {
      final String getterName = Connector.getGetterName(outputName);
      final Method m = classConnector.getMethod(getterName, new Class[0]);
      return m.getGenericReturnType();
    } catch (final Exception e) {
      return null;
    }
  }
}
