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
package org.ow2.bonita.type.lob;

import java.sql.SQLException;

import org.ow2.bonita.util.BonitaRuntimeException;
import org.ow2.bonita.util.EnvTool;

public class BlobStrategyBlob implements BlobStrategy {
  
  public void set(byte[] bytes, Lob lob) {
    if (bytes!=null) {
      lob.cachedBytes = bytes;
      lob.blob = EnvTool.getLobCreator().createBlob(bytes);
    }
  }

  public byte[] get(Lob lob) {
    if (lob.cachedBytes!=null) {
      return lob.cachedBytes;
    }
    
    java.sql.Blob sqlBlob = lob.blob;
    if (sqlBlob!=null) {
      try {
        return sqlBlob.getBytes(1, (int) sqlBlob.length());
      } catch (SQLException e) {
        throw new BonitaRuntimeException("couldn't extract bytes out of blob", e);
      }
    } 
    return null;
  }
}
