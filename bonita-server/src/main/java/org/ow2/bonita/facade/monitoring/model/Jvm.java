/**
 * Copyright (C) 2011 BonitaSoft S.A.
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
package org.ow2.bonita.facade.monitoring.model;

import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectInstance;
import javax.management.ObjectName;

import org.ow2.bonita.facade.monitoring.model.impl.MBeanUtil;

/**
 * @author Christophe Havard
 * 
 */
public class Jvm implements JvmMBean {

  protected static final Logger LOG = Logger.getLogger(Jvm.class.getName());

  private final MBeanServer mbserver;
  private final ObjectName name;
  private ObjectName nameAfterRegistration;
  private final MemoryMXBean memoryMB;
  private final OperatingSystemMXBean osMB;
  private final RuntimeMXBean runtimeMB;
  private final ThreadMXBean threadMB;

  /**
   * Default constructor.
   * 
   * @throws NullPointerException
   * @throws MalformedObjectNameException
   */
  public Jvm() throws MalformedObjectNameException, NullPointerException {
    mbserver = MBeanUtil.getMBeanServer();
    name = new ObjectName(JVM_MBEAN_NAME);
    memoryMB = MBeanUtil.getMemoryMXBean();
    osMB = MBeanUtil.getOSMXBean();
    runtimeMB = MBeanUtil.getRuntimeMXBean();
    threadMB = MBeanUtil.getThreadMXBean();
  }

  @Override
  public void start() throws MBeanStartException {
    try {
      // register the MXBean
      if (!mbserver.isRegistered(name)) {
        // Some application server rename the mbean while registering
        // it.
        final ObjectInstance objectInstance = mbserver.registerMBean(this, name);
        nameAfterRegistration = objectInstance.getObjectName();
        if (LOG.isLoggable(Level.INFO)) {
          LOG.log(Level.INFO, "Registered MBean: " + nameAfterRegistration.getCanonicalName());
        }
      }
    } catch (final Exception e) {
      throw new MBeanStartException(e);
    }

  }

  @Override
  public void stop() throws MBeanStopException {
    try {
      // Unregister the MXBean
      if (mbserver.isRegistered(name)) {
        mbserver.unregisterMBean(name);
        if (LOG.isLoggable(Level.INFO)) {
          LOG.log(Level.INFO, "Un-registered MBean: " + name);
        }
      }
      if (mbserver.isRegistered(nameAfterRegistration)) {
        mbserver.unregisterMBean(nameAfterRegistration);
        if (LOG.isLoggable(Level.INFO)) {
          LOG.log(Level.INFO, "Un-registered MBean: " + nameAfterRegistration.getCanonicalName());
        }
      }
    } catch (final Exception e) {
      throw new MBeanStopException(e);
    }
  }

  @Override
  public long getCurrentMemoryUsage() {
    return memoryMB.getHeapMemoryUsage().getUsed() + memoryMB.getNonHeapMemoryUsage().getUsed();
  }

  @Override
  public double getSystemLoadAverage() {
    return osMB.getSystemLoadAverage();
  }

  @Override
  public long getUpTime() {
    return runtimeMB.getUptime();
  }

  @Override
  public long getStartTime() {
    return runtimeMB.getStartTime();
  }

  @Override
  public long getTotalThreadsCpuTime() {
    long cpuTimeSum = -1;
    // fetch the threadCpuTime only if it's available
    if (threadMB.isThreadCpuTimeSupported() && threadMB.isThreadCpuTimeEnabled()) {
      // take the total number of thread and sum the cpu time for each.
      final long[] threadIds = threadMB.getAllThreadIds();
      cpuTimeSum = 0;
      for (final long id : threadIds) {
        cpuTimeSum += threadMB.getThreadCpuTime(id);
      }
    }
    return cpuTimeSum;

  }

  @Override
  public int getThreadCount() {
    return threadMB.getThreadCount();
  }

  @Override
  public float getMemoryUsagePercentage() {
    final float currentUsage = memoryMB.getHeapMemoryUsage().getUsed() + memoryMB.getNonHeapMemoryUsage().getUsed();
    final float maxMemory = memoryMB.getHeapMemoryUsage().getMax() + memoryMB.getNonHeapMemoryUsage().getMax();
    final float percentage = currentUsage / maxMemory;
    return percentage * 100;
  }

  @Override
  public String getOSArch() {
    return osMB.getArch();
  }

  @Override
  public int getAvailableProcessors() {
    return osMB.getAvailableProcessors();
  }

  @Override
  public String getOSName() {
    return osMB.getName();
  }

  @Override
  public String getOSVersion() {
    return osMB.getVersion();
  }

  @Override
  public String getJvmName() {
    return runtimeMB.getVmName();
  }

  @Override
  public String getJvmVendor() {
    return runtimeMB.getVmVendor();
  }

  @Override
  public String getJvmVersion() {
    return runtimeMB.getVmVersion();
  }

  @Override
  public Map<String, String> getJvmSystemProperties() {
    return runtimeMB.getSystemProperties();
  }

}
