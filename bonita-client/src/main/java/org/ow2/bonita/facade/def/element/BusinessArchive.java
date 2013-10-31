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
package org.ow2.bonita.facade.def.element;

import java.io.Serializable;
import java.util.Map;

import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;

/**
 * A BusinessArchive contains all resources in order to manage a process.
 */
public interface BusinessArchive extends Serializable {

  /**
   * Obtains the process definition depending on the business archive.
   * @return the process definition of the business archive
   */
  ProcessDefinition getProcessDefinition();

  /**
   * Obtains all JAR files containing in the business archive.<br>
   * For each JAR file, the name and the content are returned
   * @return all JAR files
   */
  Map<String, byte[]> getJarFiles();

  /**
   * Gets a resource given by its path.
   * @param resourcePath the resource path
   * @return the resource content
   */
  byte[] getResource(String resourcePath);

  /**
   * Obtains all resources which match with the given regular expression.
   * @param regex the regular expression
   * @return all resources which match with the given regular expression
   */
  Map<String, byte[]> getResources(String regex);

  /**
   * Returns all resources of the business archive.
   * @return all resources of the business archive
   */
  Map<String, byte[]> getResources();

  /**
   * Gets the process definition UUID related to the business archive.
   * @return the process definition UUID of the business archive.
   */
  ProcessDefinitionUUID getProcessUUID();

  /**
   * Obtains all resources which not match with the given regular expression.
   * @param regex the regular expression
   * @return all resources which not match with the given regular expression
   */
  Map<String, byte[]> getOtherResources(String regex);

}
