/**
 * Copyright (C) 2009  BonitaSoft S.A..
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
package org.ow2.bonita.util;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author chaffotm
 *
 */
public class ExceptionManagerTest extends TestCase {

	private ExceptionManager manager = ExceptionManager.getInstance();
	
	public void testGetIdMessage() {
		String actual = manager.getIdMessage("bh_DBT_1");
		String expected = "Bonita Error: bh_DBT_1\n";
		Assert.assertEquals(expected, actual);
	}
	
	public void testGetBadIdMessage() {
		String actual = manager.getIdMessage("aa_DBT_1");
		String expected = "Bonita Error: aa_DBT_1\n";
		Assert.assertEquals(expected, actual);
	}
	
	public void testGetMessage() {
		String actual = manager.getMessage("bh_DBT_1");
		String expected = "Unable to find EnvironmentFactory.\n";
		Assert.assertEquals(expected, actual);
	}
	
	public void testGetBadMessage() {
		String actual = manager.getMessage("aa_DBT_1");
		String expected = "Bonita Error: aa_DBT_1 does not exist.\n";
		Assert.assertEquals(expected, actual);
	}
}
