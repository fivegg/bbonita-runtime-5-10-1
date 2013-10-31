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
package org.ow2.bonita.services.impl;

import java.sql.Blob;
import java.sql.Clob;

import org.hibernate.Hibernate;
import org.ow2.bonita.services.LobCreator;

public class HibernateLobCreator implements LobCreator {

  public Blob createBlob(byte[] bytes) {
    return Hibernate.createBlob(bytes);
  }
  
  public Clob createClob(char[] chars) {
    return Hibernate.createClob(new String(chars));
  }

}
