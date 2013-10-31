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

import org.ow2.bonita.facade.def.InternalProcessDefinition;
import org.ow2.bonita.facade.runtime.impl.InternalProcessInstance;
import org.ow2.bonita.services.Archiver;
import org.ow2.bonita.services.Recorder;

/**
 * @author Pierre Vigneras
 */
public final class ArchiveTool {

  private ArchiveTool() { }

  /**
   * Perform a all-or-nothing archive.
   *
   * This method does the following: it first tries to archive the given Archivable
   * into the specified {@link Archiver}. If it succeeds and only in this case, it tries
   * to remove the given record from the specified
   *
   * @param record a record
   * @throws AtomicArchiveException if archiving failed.
   */

  public static void atomicArchive(final InternalProcessInstance instance) {
    final Recorder recorder = EnvTool.getRecorder();
    final Archiver archiver = EnvTool.getArchiver();
    
    ProcessUtil.removeInternalInstanceEvents(instance.getUUID());
    final InternalProcessInstance newInstance = new InternalProcessInstance(instance);
    // Flag the instance as Archived.
    newInstance.setIsArchived(true);
    archiver.archive(newInstance);
    recorder.remove(instance);
    //delete all cases of this instance
    EnvTool.getWebService().removeCase(instance.getUUID());
  }
  
  public static void atomicArchive(final InternalProcessDefinition process) {
    final Recorder recorder = EnvTool.getRecorder();
    final Archiver archiver = EnvTool.getArchiver();
    archiver.archive(new InternalProcessDefinition(process));
    recorder.remove(process);
  }
}
