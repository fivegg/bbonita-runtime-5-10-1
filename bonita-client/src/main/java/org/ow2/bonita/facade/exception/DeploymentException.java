/**
 * Copyright (C) 2010  BonitaSoft S.A.
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
package org.ow2.bonita.facade.exception;

import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.util.BonitaException;
/**
 * Thrown by the ManagementAPI if a failure during the deployment operation occured.
 * @author Marc Blachon, Guillaume Porcher, Charles Souillard, Miguel Valdes, Pierre Vigneras
 */
public class DeploymentException extends BonitaException {

  private static final long serialVersionUID = 6967327173196513880L;
  private final String className;
  private final ProcessDefinitionUUID processUUID;

  /**
   * Constructs an DeploymentException with the specified detail message.
   * ClassName and processDefinitionUUID are set to null.
   * @param msg the detail message.
   */
  public DeploymentException(String msg) {
    super(msg);
    this.className = null;
    this.processUUID = null;
  }

  /**
   * Constructs a DeploymentException with the specified detail message and the throwable cause.
   * ClassName and processDefinitionUUID are set to null.
   * @param msg the detail message.
   * @param cause exception causing the abort.
   */
  public DeploymentException(String msg, Throwable cause) {
    super(msg, cause);
    this.className = null;
    this.processUUID = null;
  }
  
  /**
   * Constructs a DeploymentException with the specified detail message, the throwable cause and the process in fault.
   * ClassName is set to null.
   * @param msg the detail message.
   * @param cause exception causing the abort.
   * @param processUUID the process that causes the error.
   */
  public DeploymentException(String msg, Throwable cause, ProcessDefinitionUUID processUUID) {
    super(msg, cause);
    this.className = null;
    this.processUUID = processUUID;
  }

  /**
   * Constructs a DeploymentException with the specified detail message and className parameter.
   * processId is set to null.
   * @param msg the detail message.
   * @param className the name of the class to deploy.
   */
  public DeploymentException(String msg, String className) {
    super(msg + className);
    this.className = className;
    this.processUUID = null;
  }

  /**
   * Constructs a DeploymentException with the specified detail message and className, processDefinitionUUID parameters.
   * @param msg the detail message.
   * @param className the name of the class to deploy.
   * @param processUUID the UUID of the process.
   */
  public DeploymentException(String msg, String className, ProcessDefinitionUUID processUUID) {
    super(msg + " className: " + className + " processDefinitionUUID: " + processUUID);
    this.className = className;
    this.processUUID = processUUID;
  }

  public String getClassName() {
    return this.className;
  }

  public ProcessDefinitionUUID getProcessDefinitionUUID() {
    return this.processUUID;
  }

}
