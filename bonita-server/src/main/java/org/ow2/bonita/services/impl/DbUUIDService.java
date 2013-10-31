/**
 * Copyright (C) 2009-2010  BonitaSoft S.A.
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
package org.ow2.bonita.services.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ow2.bonita.env.Environment;
import org.ow2.bonita.facade.ManagementAPI;
import org.ow2.bonita.facade.impl.StandardAPIAccessorImpl;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.services.UUIDService;
import org.ow2.bonita.util.Command;
import org.ow2.bonita.util.EnvTool;
import org.ow2.bonita.util.ProcessUtil;

/**
 * 
 * @author Charles Souillard
 *
 */
public class DbUUIDService implements UUIDService {

  static final Logger LOG = Logger.getLogger(ProcessUtil.class.getName());
  private Map<ProcessDefinitionUUID, Long> uuids = new HashMap<ProcessDefinitionUUID, Long>();
  private Map<ProcessDefinitionUUID, Object> mutexs = new HashMap<ProcessDefinitionUUID, Object>();

  private synchronized Object getMutex(final ProcessDefinitionUUID processUUID) {
    if (!mutexs.containsKey(processUUID)) {
      mutexs.put(processUUID, new Object());
    }
    return mutexs.get(processUUID);
  }

  private synchronized void removeUUID(final ProcessDefinitionUUID processUUID) {
    uuids.remove(processUUID);
  }

  private synchronized Long getUUIDValue(final ProcessDefinitionUUID processUUID) {
    return uuids.get(processUUID);
  }

  private synchronized void setUUIDValue(final ProcessDefinitionUUID processUUID, final Long value) {
    uuids.put(processUUID, value);
  }

  public long getNewProcessInstanceNb(ProcessDefinitionUUID processUUID) {
    long newProcessInstanceNb; 
    synchronized (getMutex(processUUID)) {
      newProcessInstanceNb = EnvTool.getCommandService().execute(new StoreMetaDataCommand(processUUID));
    }
    if (LOG.isLoggable(Level.FINE)) {
      LOG.fine("Creating a new ProcessInstance with nb: " + newProcessInstanceNb);
    }
    return newProcessInstanceNb;
  }

  private String getMetadataName(final ProcessDefinitionUUID processUUID) {
    return "*****" + processUUID + "*****instance-nb*****";
  }

  public void archiveOrDeleteProcess(ProcessDefinitionUUID processUUID) {
    synchronized (getMutex(processUUID)) {
      final StandardAPIAccessorImpl accessor = new StandardAPIAccessorImpl();
      final ManagementAPI managementAPI = accessor.getManagementAPI();
      final String metadataName = getMetadataName(processUUID);
      managementAPI.deleteMetaData(metadataName);
      removeUUID(processUUID);
    }
  }

  private class StoreMetaDataCommand implements Command<Long> {
    private static final long serialVersionUID = 1L;
    private ProcessDefinitionUUID processUUID;

    public StoreMetaDataCommand(final ProcessDefinitionUUID processUUID) {
      this.processUUID = processUUID;
    }

    public Long execute(Environment environment) throws Exception {
      long newProcessInstanceNb;
      final String metaName = getMetadataName(processUUID);
      StandardAPIAccessorImpl accessor = new StandardAPIAccessorImpl();
      final ManagementAPI managementAPI = accessor.getManagementAPI();
      long lastInMemory = 0;
      final Long uuidValue = getUUIDValue(processUUID);
      if (uuidValue != null) {
        lastInMemory = uuidValue;
      }
      if (LOG.isLoggable(Level.FINE)) {
        LOG.fine("Last process instance number found in memory: " + lastInMemory);
      }

      long lastInDb = 0;
      final String metadataValue = managementAPI.getMetaData(metaName);
      if (metadataValue != null) {
        lastInDb = Long.valueOf(metadataValue);
      }
      if (LOG.isLoggable(Level.FINE)) {
        LOG.fine("Last process instance number found in database: " + lastInDb);
      }

      newProcessInstanceNb = Math.max(lastInDb, lastInMemory) + 1;
      setUUIDValue(processUUID, newProcessInstanceNb);
      managementAPI.addMetaData(metaName, Long.toString(newProcessInstanceNb));
      return newProcessInstanceNb;
    }
  }

}
