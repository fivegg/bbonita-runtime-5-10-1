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

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

public class ClobStrategyChopped implements ClobStrategy {
  
  int chopSize = 1024;
  
  public char[] get(Lob lob) {
    return glue(lob.charChops);
  }

  public void set(char[] chars, Lob lob) {
    lob.charChops = chop(chars);
  }

  public List<CharChop> chop(char[] chars) {
    List<CharChop> charChops = null;
    if ( (chars!=null)
         && (chars.length>0) ){
      charChops = new ArrayList<CharChop>();
      int index = 0;
      while ( (chars.length-index) > chopSize ) {
        String chop = new String(chars, index, chopSize);
        charChops.add(new CharChop(chop));
        index+=chopSize;
      }
      // add remainder chop
      String chop = new String(chars, index, chars.length-index);
      charChops.add(new CharChop(chop));
    }
    return charChops;
  }

  public char[] glue(List<CharChop> charChops) {
    if (charChops!=null) {
      StringWriter writer = new StringWriter();
      
      for (CharChop charChop: charChops) {
        writer.write(charChop.getText());
      }
      
      return writer.toString().toCharArray();
    }

    return null;
  }
}