/**
 * Copyright (C) 2012 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
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

/**
 * 
 * @author Matthieu Chaffotte
 *
 */
public interface EventDefinition extends Serializable {

  /**
   * Returns the name of the event.
   * @return the event name.
   */
  String getName();

  String getCorrelationKeyName1();

  String getCorrelationKeyExpression1();

  String getCorrelationKeyName2();
  
  String getCorrelationKeyExpression2();
  
  String getCorrelationKeyName3();
  
  String getCorrelationKeyExpression3();
  
  String getCorrelationKeyName4();
  
  String getCorrelationKeyExpression4();
  
  String getCorrelationKeyName5();

  String getCorrelationKeyExpression5();
  
}
