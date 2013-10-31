package org.ow2.bonita.integration.routeSplitJoin;

/**
 *
 * Bonita
 * Copyright (C) 1999 Bull S.A.
 * Bull 68 route de versailles  78434 Louveciennes Cedex France
 * Further information: bonita@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *
 */
import org.ow2.bonita.definition.TxHook;
import org.ow2.bonita.facade.APIAccessor;
import org.ow2.bonita.facade.runtime.ActivityInstance;

/**
 *
 * @author Marc Blachon, Charles Souillard
 *
 */

public class MailAccept implements TxHook {
  private static final long serialVersionUID = 1L;

  public void execute(APIAccessor accessor, ActivityInstance activityInstance) throws Exception {
    accessor.getRuntimeAPI().setProcessInstanceVariable(
        activityInstance.getProcessInstanceUUID(), "StatusMail", "mailAccept sent");
  }

}
