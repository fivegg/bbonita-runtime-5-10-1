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
package org.ow2.bonita.facade.paging;

/**
 * 
 * @author Elias Ricken de Medeiros
 *
 */
public enum ProcessInstanceCriterion {
	/**
	 * Last update ascending order
	 */
	LAST_UPDATE_ASC,
	
	/**
	 * Started date ascending order
	 */
	STARTED_DATE_ASC,
	
	/**
	 * Ended date ascending order
	 */
	ENDED_DATE_ASC,
	
	/**
	 * Instance number ascending order
	 */
	INSTANCE_NUMBER_ASC,
	
	/**
	 * Instance UUID ascending order
	 */
	INSTANCE_UUID_ASC,
	
	/**
	 * Last update descending order
	 */
	LAST_UPDATE_DESC,
	
	/**
	 * Started date descending order
	 */
	STARTED_DATE_DESC,
	
	/**
	 * Ended date descending order
	 */
	ENDED_DATE_DESC,	
	
	/**
	 * Instance number descending order
	 */
	INSTANCE_NUMBER_DESC,
	
	/**
	 * Instance UUID descending order
	 */
	INSTANCE_UUID_DESC,
	
	/**
	 * 
	 */
	DEFAULT
}
