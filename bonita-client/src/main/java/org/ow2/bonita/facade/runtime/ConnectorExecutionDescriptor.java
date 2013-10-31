/**
 * Copyright (C) 2012  BonitaSoft S.A.
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
package org.ow2.bonita.facade.runtime;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Elias Ricken de Medeiros
 *
 */
public class ConnectorExecutionDescriptor implements Serializable {
  
  private static final long serialVersionUID = 3490306986344558425L;

  /**
   * Connector class name
   */
  private final String className;
  
  /**
   * Input parameters
   */
  private Map<String, Serializable[]> inputParameters;
  
  /**
   * Output parameters
   */
  private Map<String, Serializable[]> outputParameters;
  
  private boolean throwingException;
  
  /**
   * Create a new connector execution descriptor
   * @param className the connector class name
   */
  public ConnectorExecutionDescriptor(final String className) {
    this.className = className;
    throwingException = true;
  }

  /**
   * Create a new connector execution descriptor
   * @param className the connector class name
   * @param inputParameters the input parameters
   * @param outputParameters the output parameters
   */
  public ConnectorExecutionDescriptor(final String className, final Map<String, Serializable[]> inputParameters, final Map<String, Serializable[]> outputParameters) {
    this(className);
    this.inputParameters = inputParameters;
    this.outputParameters = outputParameters;
    throwingException = true;
  }

  /**
   * Create a new connector execution descriptor
   * @param className the connector class name
   * @param inputParameters the input parameters
   * @param outputParameters the output parameters
   */
  public ConnectorExecutionDescriptor(final String className, final Map<String, Serializable[]> inputParameters, final Map<String, Serializable[]> outputParameters, boolean throwingException) {
    this(className, inputParameters, outputParameters);
    this.throwingException = throwingException;
  }
  
  /**
   * Get the class name of the connector.
   * @return the class name of the connector
   */
  public String getClassName() {
    return className;
  }

  /**
   * Get the connector input parameters.
   * @return the connector input parameters
   */
  public Map<String, Serializable[]> getInputParameters() {
    if(inputParameters == null) {
      return Collections.emptyMap();
    } 
    return Collections.unmodifiableMap(inputParameters);
  }
  
  /**
   * add an input parameter
   * @param key input parameter key
   * @param values input parameter values
   */
  public void addInputParameter(final String key, final Serializable... values) {
    if (this.inputParameters == null) {
      this.inputParameters = new HashMap<String, Serializable[]>();
    }
    this.inputParameters.put(key, values); 
  }
  
  /**
   * Add input parameters
   * @param parameters
   */
  public void addInputParameters(final Map<String, Serializable[]> parameters) {
    if (this.inputParameters == null) {
      this.inputParameters = new HashMap<String, Serializable[]>();
    }
    this.inputParameters.putAll(parameters); 
  }

  /**
   * Get the connector output parameters.
   * @return the connector output parameters
   */
  public Map<String, Serializable[]> getOutputParameters() {
    if (outputParameters == null) {
      return Collections.emptyMap();
    }
    return Collections.unmodifiableMap(outputParameters);
  }
  
  /**
   * Add an output parameter
   * @param key output parameter key
   * @param values output parameter values
   */
  public void addOutputParameter(final String key, final Serializable... values) {
    if (this.outputParameters == null) {
      this.outputParameters = new HashMap<String, Serializable[]>(1);
    }
    this.outputParameters.put(key, values); 
  }
  
  /**
   * Add output parameters
   * @param parameters
   */
  public void addOutputParameters(final Map<String, Serializable[]> parameters) {
    if (this.outputParameters == null) {
      this.outputParameters = new HashMap<String, Serializable[]>(parameters.size());
    }
    this.outputParameters.putAll(parameters); 
  }

  /**
   * Defines whether the connector throws exceptions when executing.
   * @return true if the connector throws exception; false otherwise
   */
  public boolean isThrowingException() {
    return throwingException;
  }

  public void setThrowingException(boolean throwingException) {
    this.throwingException = throwingException;
  }

}
