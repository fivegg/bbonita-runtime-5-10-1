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
 * 
 * MOdified by Matthieu Chaffotte - BonitaSoft S.A.
 **/
package org.ow2.bonita.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Pierre Vigneras
 */
public abstract class CopyTool {

  public static final String DEPLOYMENT_IDPREFIX = "deployment$";

  protected CopyTool() { }

  public static Set<String> copy(final Set<String> src) {
    if (src == null) {
      return new HashSet<String>();
    }
    return new HashSet<String>(src);
  }

  public static List<String> copy(final List<String> src) {
    if (src == null) {
      return new ArrayList<String>();
    }
    return new ArrayList<String>(src);
  }

  public static Map<String, Object> copySer(final Map<String, Serializable> src) {
  	final Map<String, Object> result = new HashMap<String, Object>();
    if (src != null) {
    	for (Map.Entry<String, Serializable> entry : src.entrySet()) {
    		result.put(entry.getKey(), entry.getValue());
    	}
    }
    return result;
  }
  
  public static Map<String, String> copy(final Map<String, String> src) {
    if (src == null) {
      return new HashMap<String, String>();
    }
    return new HashMap<String, String>(src);
  }
  
  public static Map<String, Object[]> copyMap(final Map<String, Object[]> src) {
    if (src == null) {
      return new HashMap<String, Object[]>();
    }
    Map<String, Object[]> result = new HashMap<String, Object[]>();
    for (Map.Entry<String, Object[]> entry : src.entrySet()) {
      String key = entry.getKey();
      Object[] value = entry.getValue();
      Collection<Object> newValue = null;
      if (value != null) {
        newValue = new ArrayList<Object>();
        for (Object o : value) {
          newValue.add(o);
        }
      }
      result.put(key, newValue.toArray());
    }
    return result;
  }

  public static Date copy(final Date src) {
    if (src == null) {
      return null;
    }
    return (Date) src.clone();
  }

}
