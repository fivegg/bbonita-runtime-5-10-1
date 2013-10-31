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
 **/
package org.ow2.bonita.runtime;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.util.Misc;

public class ProcessClassLoader extends AbstractClassLoader {

  private static final Logger LOG = Logger.getLogger(ProcessClassLoader.class.getName());
  
  ProcessClassLoader(ProcessDefinitionUUID processUUID) {
    super(Misc.getBusinessArchiveCategories(processUUID), new VirtualCommonClassloader());
    if (LOG.isLoggable(Level.FINE)) {
      LOG.fine("Creating a new ProcessClassLoader... ProcessDefinitionUUID = " + processUUID);
    }
  }
  
  @Override
  public void release() {
    super.release();
    ((VirtualCommonClassloader)getParent()).cleanCommonClassLoader();
  }

}
