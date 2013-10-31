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
package org.ow2.bonita.facade;

import java.util.logging.Logger;

/**
 * @author Pierre Vigneras
 * @see SecurityContext
 */
public enum Context {

  /** Auto detect the actual context. */
  AutoDetect,
  /** Standard J2SE context. */
  Standard,
  /** EJB3 J2EE5 context. */
  EJB3,
  /** EJB2 J2EE5 context. */
  EJB2,
  /** REST Context*/
  REST;

  protected static final Logger LOG = Logger.getLogger(Context.class.getName());

  // ---- General idea:
  // In order to set the caller ID, we have to know in which global CONTEXT
  // we are. In an EJB CONTEXT, there should be an EJBContext that holds the
  // caller ID. In a standard J2SE CONTEXT, we consider that the
  // SecurityContext class has been used and that it holds the caller ID.
  // In this last case, (J2SE), the standard way for setting
  // the caller ID in the SecurityContext class is to use JAAS with the
  // StorageLoginModule. Note that this is not required. Any other mechanism
  // can be used. The requirement is that the SecurityContext.getCallerId()
  // should return the proper value. See SecurityContext class for more
  // informations.
}
