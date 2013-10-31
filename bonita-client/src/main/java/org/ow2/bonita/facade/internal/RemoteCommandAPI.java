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
 * 
 * Modified by Elias Ricken de Medeiros - BonitaSoft S.A.
 **/
package org.ow2.bonita.facade.internal;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.util.Command;

/**
 * For internal use only.
 */
@Path("/API/commandAPI/")
@Produces({"text/*","application/xml"})
@Consumes({"application/x-www-form-urlencoded","text/*", "application/xml"})
public interface RemoteCommandAPI extends Remote {

	@POST @Path("execute")
  <T> T execute(
      @FormParam("command") Command<T> command,
      @FormParam("options") final Map<String, String> options)
	throws Exception, RemoteException;
  
	@POST @Path("execute/{processUUID}")
  <T> T execute(
      @FormParam("command") Command<T> command,
      @PathParam("processUUID")ProcessDefinitionUUID processUUID, 
  		@FormParam("options") final Map<String, String> options)
	throws Exception, RemoteException;

}
