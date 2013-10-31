/**
 * Copyright (C) 2007  Bull S. A. S.
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
package org.ow2.bonita.util;

import junit.framework.TestCase;

import org.ow2.bonita.facade.APIAccessor;
import org.ow2.bonita.facade.Context;

public class AccessorUtilTest extends TestCase {

  public void testContext() {
    // Load the class so the static initializer is executed.
    APIAccessor.class.getName();
    // Exception should not be thrown!
  }

  private void testFindContextFromProperty(final Context context) {
    System.setProperty(AccessorUtil.API_TYPE_PROPERTY, context.toString());
    assertEquals(context, AccessorUtil.CONTEXT.get());
  }

  public void testFindContextFromProperty() {
    testFindContextFromProperty(Context.Standard);
    testFindContextFromProperty(Context.EJB2);
    testFindContextFromProperty(Context.EJB3);
    System.setProperty(AccessorUtil.API_TYPE_PROPERTY, Misc.getRandomString(5));
    try {
      AccessorUtil.CONTEXT.get();
      fail("Check invalid properties!");
    } catch (final IllegalArgumentException iae) {
      assertTrue(iae.getMessage().contains("Unknown enum string for " + Context.class.getName()));
    } finally {
      System.clearProperty(AccessorUtil.API_TYPE_PROPERTY);
    }
  }

  public void testGetAPIAccessor() {
    final APIAccessor accessor = AccessorUtil.getAPIAccessor();
    assertNotNull(accessor);
  }

}
