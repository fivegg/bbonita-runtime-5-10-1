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
 * Modified by Charles Souillard, Matthieu Chaffotte - BonitaSoft S.A.
 **/
package org.ow2.bonita.persistence;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.ow2.bonita.facade.def.element.impl.MetaDataImpl;
import org.ow2.bonita.facade.runtime.Category;

public interface JournalDbSession extends QuerierDbSession {

  @Override
  MetaDataImpl getMetaData(String key);

  @Override
  Set<Category> getCategories(Collection<String> aCategoryNames);

  /**
   * Create or lock sequence of name seqName. Should be call before any further
   * operation on this sequence.
   * 
   * @param key
   *          name of the sequence
   */
  void lockMetadata(String key);

  /**
   * get current sequence value from DB. DB hit is guaranteed (no cache).
   * 
   * @param key
   *          the last value.
   * @return
   */
  long getLockedMetadata(String key);

  /**
   * Update sequence with value
   * 
   * @param key
   *          Name of the sequence
   * @param value
   *          New value to Set
   */
  void updateLockedMetadata(String key, long value);

  /**
   * remove the sequence with the given name
   * 
   * @param key
   *          the last value.
   * @return
   */
  void removeLockedMetadata(String key);

  void deleteExecution(long id);

  List<String> getInstanceIdsFromMetadata(int index, int maxResult);

}
