/**
 * Copyright (C) 2010  BonitaSoft S.A.
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
package org.ow2.bonita.command;

import java.util.Date;

import org.ow2.bonita.env.Environment;
import org.ow2.bonita.facade.runtime.impl.WebTemporaryTokenImpl;
import org.ow2.bonita.util.Command;
import org.ow2.bonita.util.EnvTool;

/**
 * 
 * @author Nicolas	Chabanoles
 *
 */
public class AddExpiredWebTemporaryTokenCommand implements Command<Void> {

  private static final long serialVersionUID = -8498000008068025693L;
  private int nbOfTokenToBuild;

  public AddExpiredWebTemporaryTokenCommand(int number) {
    this.nbOfTokenToBuild = number;
    
  }

  public Void execute(Environment environment) throws Exception {
	  WebTemporaryTokenImpl token;
	  long expirationDate;
	  long now = new Date().getTime();
	  for (int i = 0; i<nbOfTokenToBuild; i++) {
		  long expiration = ((i+1) * 1000L * 60L * 60L);
		  System.err.println("Expiration was " + expiration + "ms ago");
		  expirationDate = now - expiration;
		  token = new WebTemporaryTokenImpl("token" + i, expirationDate, "identity" + i);
		  EnvTool.getWebTokenManagementService().addTemporaryToken(token);
	  }
    return null;
  }
}
