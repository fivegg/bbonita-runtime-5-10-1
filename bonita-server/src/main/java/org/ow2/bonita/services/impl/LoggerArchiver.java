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
 * 
 * Modified by Matthieu Chaffotte - BonitaSoft S.A.
 **/
package org.ow2.bonita.services.impl;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.ow2.bonita.facade.def.InternalProcessDefinition;
import org.ow2.bonita.facade.runtime.impl.InternalProcessInstance;
import org.ow2.bonita.services.Archiver;

/**
 * This implementation of History does nothing: it never archive though
 * returning empty set, true and null values.
 *
 * @author Pierre Vigneras
 */
public class LoggerArchiver implements Archiver {

  public static final Logger LOG = Logger.getLogger(LoggerArchiver.class.getName());
  private final Level level;

  public LoggerArchiver() {
    this(Level.FINE);
  }

  public LoggerArchiver(String levelName) {
    this(Level.parse(levelName));
  }

  public LoggerArchiver(final Level level) {
    this.level = level;
  }

  public void archive(InternalProcessInstance processInst) {
    if (LOG.isLoggable(level)) {
      LOG.log(level, "Archiving: " + processInst);
    }
  }

  public void archive(InternalProcessDefinition processDef) {
    if (LOG.isLoggable(level)) {
      LOG.log(level, "Archiving: " + processDef);
    }
  }
  
  public void remove(InternalProcessDefinition processDef) {
    if (LOG.isLoggable(level)) {
      LOG.log(level, "Removing: " + processDef);
    }
  }
  public void remove(InternalProcessInstance processInst) {
    if (LOG.isLoggable(level)) {
      LOG.log(level, "Removing: " + processInst);
    }
  }
  
}
