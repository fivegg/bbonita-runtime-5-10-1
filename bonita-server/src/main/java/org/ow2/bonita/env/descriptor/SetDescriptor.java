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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ow2.bonita.env.Descriptor;

/**
 * 
 * <p>
 * This {@link Descriptor} creates a {@link Set}.
 * </p>
 * 
 * <p>
 * If no specific implementation for the {@link Set} is specified, a
 * {@link HashSet} will be used.
 * 
 * <p>
 * Entries can be added during the set initialization. The list of entries is
 * specified with {@link #setValueDescriptors(List)}.
 * </p>
 * 
 * @author Tom Baeyens
 * @author Guillaume Porcher (documentation)
 * 
 * @see Descriptor
 */
public class SetDescriptor extends CollectionDescriptor {

  private static final long serialVersionUID = 1L;

  public SetDescriptor() {
    super(HashSet.class.getName());
  }
}
