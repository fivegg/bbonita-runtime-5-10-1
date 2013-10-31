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
package org.ow2.bonita.services;

import org.ow2.bonita.facade.def.InternalProcessDefinition;
import org.ow2.bonita.facade.runtime.impl.InternalProcessInstance;

/**
 * An Archiver is responsible for archiving <strong>non-runtime</strong> instances
 * such as undeployed packages and processes, dead instances, activities and tasks.
 *
 * @author Pierre Vigneras
 *
 */
public interface Archiver {

  String DEFAULT_KEY = "archiver";

  void archive(InternalProcessDefinition processDeef);
  void archive(InternalProcessInstance processInst);

  /**
   * Generic method for removing a history record.
   *
   * Implementation may delegate to overloaded methods according to record type.
   * @param processInst the record to archive
   */
  void remove(InternalProcessInstance processInst);
  void remove(InternalProcessDefinition processDef);

}
