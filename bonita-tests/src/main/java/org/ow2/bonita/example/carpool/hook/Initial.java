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
package org.ow2.bonita.example.carpool.hook;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.ow2.bonita.definition.TxHook;
import org.ow2.bonita.facade.APIAccessor;
import org.ow2.bonita.facade.runtime.ActivityInstance;

public class Initial implements TxHook {

  private static final Logger LOG = Logger.getLogger(Initial.class.getName());

  public void execute(APIAccessor arg0, ActivityInstance arg1) throws Exception {
    if (LOG.isLoggable(Level.INFO)) {
      LOG.info("\n***** Executing Hook: " + this.getClass().getName() + " *****\n");
    }
  }

}
