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
package org.ow2.bonita.env.descriptor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ow2.bonita.env.Descriptor;
import org.ow2.bonita.env.WireContext;
import org.ow2.bonita.env.WireException;
import org.ow2.bonita.util.ExceptionManager;

/**
 * 
 * <p>
 * This {@link Descriptor} creates a {@link Map}.
 * </p>
 * 
 * <p>
 * If no specific implementation for the {@link Map} is specified, a
 * {@link HashMap} will be used.
 * </p>
 * 
 * <p>
 * Entries can be added during the map initialization. The list of entries (key,
 * value) to add must be specified with two operations:
 * <ol>
 * <li>{@link #setKeyDescriptors(List)} sets the list of the keys to generate.</li>
 * <li>{@link #setValueDescriptors(List)} sets the list of value associated with
 * these keys.</li>
 * </ol>
 * The two lists must be in the same order (the n-th element of the key list
 * will be associated with the n-th element of the value list).
 * </p>
 * 
 * @author Tom Baeyens
 * @author Guillaume Porcher (documentation)
 * 
 * @see Descriptor
 */
public class MapDescriptor extends CollectionDescriptor {

  private static final long serialVersionUID = 1L;

  List<Descriptor> keyDescriptors;

  public MapDescriptor() {
    super(HashMap.class.getName());
  }

  @SuppressWarnings("unchecked")
  public void initialize(Object object, WireContext wireContext) {
    Map<Object, Object> map = (Map<Object, Object>) object;
    try {
      if (keyDescriptors != null) {
        for (int i = 0; i < keyDescriptors.size(); i++) {
          Descriptor keyDescriptor = keyDescriptors.get(i);
          Descriptor valueDescriptor = valueDescriptors.get(i);
          Object key = wireContext.create(keyDescriptor, true);
          Object value = wireContext.create(valueDescriptor, true);
          map.put(key, value);
        }
      }
    } catch (WireException e) {
      throw e;
    } catch (Exception e) {
    	String message = ExceptionManager.getInstance().getFullMessage(
    			"bp_MD_1", (name != null ? name : className));
      throw new WireException(message, e);
    }
  }

  public List<Descriptor> getKeyDescriptors() {
    return keyDescriptors;
  }

  public void setKeyDescriptors(List<Descriptor> keyDescriptors) {
    this.keyDescriptors = keyDescriptors;
  }
}
