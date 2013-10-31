/**
 * Copyright (C) 2011-2012 BonitaSoft S.A.
 * BonitaSoft, 31 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.ow2.bonita.facade.monitoring.model.impl;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;

/**
 * @author Christophe Havard, Matthieu Chaffotte
 * 
 */
public class MBeanUtil {

  private static MBeanServer mbserver = null;

  public static MBeanServer getMBeanServer() {
    if (mbserver == null) {
      final ArrayList<MBeanServer> mbservers = MBeanServerFactory.findMBeanServer(null);
      if (mbservers.size() > 0) {
        mbserver = mbservers.get(0);
      }
      if (mbserver == null) {
        mbserver = MBeanServerFactory.createMBeanServer();
      }
    }
    return mbserver;
  }

  public static MemoryMXBean getMemoryMXBean() {
    return ManagementFactory.getMemoryMXBean();
  }

  public static OperatingSystemMXBean getOSMXBean() {
    return ManagementFactory.getOperatingSystemMXBean();
  }

  public static RuntimeMXBean getRuntimeMXBean() {
    return ManagementFactory.getRuntimeMXBean();
  }

  public static ThreadMXBean getThreadMXBean() {
    return ManagementFactory.getThreadMXBean();
  }

}
