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

import java.util.ArrayList;
import java.util.List;

public class BlobStrategyChopped implements BlobStrategy {

  int chopSize = 1024;

  public void set(byte[] bytes, Lob lob) {
    lob.bytesChops = chop(bytes);
  }

  public byte[] get(Lob lob) {
    return glue(lob.bytesChops);
  }

  public List<BytesChop> chop(byte[] bytes) {
    List<BytesChop> chops = null;
    if ( (bytes!=null)
         && (bytes.length>0) ){
      chops = new ArrayList<BytesChop>();
      int index = 0;
      while ( (bytes.length-index) > chopSize ) {
        byte[] byteBlock = new byte[chopSize];
        System.arraycopy(bytes, index, byteBlock, 0, chopSize);
        chops.add(new BytesChop(byteBlock));
        index+=chopSize;
      }
      byte[] byteBlock = new byte[bytes.length-index];
      System.arraycopy(bytes, index, byteBlock, 0, bytes.length-index);
      chops.add(new BytesChop(byteBlock));
    }
    return chops;
  }

  public byte[] glue(List<BytesChop> bytesChops) {
    byte[] bytes = null;
    if (bytesChops!=null) {
      for (BytesChop bytesChop: bytesChops) {
        if (bytes==null) {
          bytes = bytesChop.getBytes();
        } else {
          byte[] oldValue = bytes;
          bytes = new byte[bytes.length+bytesChop.getBytes().length];
          System.arraycopy(oldValue, 0, bytes, 0, oldValue.length);
          System.arraycopy(bytesChop.getBytes(), 0, bytes, oldValue.length, bytesChop.getBytes().length);
        }
      }
    }
    return bytes;
  }
}
