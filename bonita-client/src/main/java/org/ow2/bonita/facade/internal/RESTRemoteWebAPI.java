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
package org.ow2.bonita.facade.internal;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;


/**
 * 
 * @author Elias Ricken de Medeiros
 *
 */
@Path("/API/webAPI/")
@Produces({"text/*","application/xml"})
@Consumes({"application/x-www-form-urlencoded","text/*", "application/xml"})
public interface RESTRemoteWebAPI extends AbstractRemoteWebAPI {

	@POST @Path("removeLabels/{ownerName}") 
  void removeLabels(
      @PathParam("ownerName") String ownerName,
      @FormParam("labelNames") List<String> labelNames,
  		@FormParam("options") final Map<String, String> options)
	throws RemoteException;

	@POST @Path("getCasesNumberByLimit/{ownerName}")
  Map<String, Integer> getCasesNumber(
      @PathParam("ownerName") String ownerName,
      @FormParam("labelNames") List<String> labelNames, 
  		@QueryParam("limit") int limit,
  		@FormParam("options") final Map<String, String> options)
  throws RemoteException;

  @POST @Path("getCasesNumber/{ownerName}")
  Map<String, Integer> getCasesNumber(
      @PathParam("ownerName") String ownerName,
      @FormParam("labelNames") List<String> labelNames, 
  		@FormParam("options") final Map<String, String> options)
  throws RemoteException;

  @POST	@Path("getCasesNumber/{ownerName}/{labelName}")
  Map<String, Integer> getCasesNumber(
      @PathParam("ownerName") String ownerName,
      @PathParam("labelName") String labelName, 
  		@FormParam("labelNames") List<String> labelNames,
  		@QueryParam("limit") int limit,
  		@FormParam("options") final Map<String, String> options)
  throws RemoteException;

}
