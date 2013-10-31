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
 * 
 * Modified by Matthieu Chaffotte, Elias Ricken de Medeiros - BonitaSoft S.A.
 **/
package org.ow2.bonita.facade.def.majorElement;

import java.io.Serializable;
import java.util.Set;

import org.ow2.bonita.facade.uuid.DataFieldDefinitionUUID;

/**
 * This interface represents the DataField definition.
 * It's derived from the DataField Definition (also called Workflow Relevant Data) of XPDL.
 */
public interface DataFieldDefinition extends ProcessElement {

  /**
   * Returns the UUID for the DataFieldDefinition.
   */
  DataFieldDefinitionUUID getUUID();

  /**
   * Returns the class name of data type.
   * @return the class name of data type
   */
  String getDataTypeClassName();

  /**
   * Returns either the initial value of the dataField or null
   * if the initial value is a scripting or an enumeration one.
   * @return the initial value
   */
  Serializable getInitialValue();

  /**
   * Returns either the scripting value of the dataField as initial value if available or null.
   * @return the scripting value
   */
  String getScriptingValue();

  /**
   * Returns either the enumerations values or null
   * @return the enumeration values
   */
  Set<String> getEnumerationValues();

  /**
   * Checks whether the dataField is an enumeration.
   * @return true if the dataField is an enumeration; false otherwise
   */
  boolean isEnumeration();
  
  /**
   * Checks whether the dataField is transient
   * @return true if the dataField is transient; false otherwise
   */
  boolean isTransient();

}
