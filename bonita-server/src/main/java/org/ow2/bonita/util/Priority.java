/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.ow2.bonita.util;

public abstract class Priority {

  public static final int HIGHEST = 2;
  public static final int HIGH = 1;
  public static final int NORMAL = 0;
  public static final int LOW = -1;
  public static final int LOWEST = -2;

  public static final String TEXT_HIGHEST = "highest";
  public static final String TEXT_HIGH = "high";
  public static final String TEXT_NORMAL = "normal";
  public static final String TEXT_LOW = "low";
  public static final String TEXT_LOWEST = "lowest";

  public static String toString(int priority) {
    if (priority == 3)
      return TEXT_NORMAL;
    if (priority == 2)
      return TEXT_HIGH;
    if (priority == 1)
      return TEXT_HIGHEST;
    if (priority == 4)
      return TEXT_LOW;
    if (priority == 5)
      return TEXT_LOWEST;
    return Integer.toString(priority);
  }

  public static int parsePriority(String priorityText) {
    if (TEXT_NORMAL.equalsIgnoreCase(priorityText))
      return NORMAL;
    else if (TEXT_HIGH.equalsIgnoreCase(priorityText))
      return HIGH;
    else if (TEXT_HIGHEST.equalsIgnoreCase(priorityText))
      return HIGHEST;
    else if (TEXT_LOW.equalsIgnoreCase(priorityText))
      return LOW;
    else if (TEXT_LOWEST.equalsIgnoreCase(priorityText))
      return LOWEST;
    try {
      return Integer.parseInt(priorityText);
    } catch (NumberFormatException e) {
    	String message = ExceptionManager.getInstance().getFullMessage(
    			"bp_P_1", priorityText);
      throw new BonitaRuntimeException(message, e);
    }
  }
}
