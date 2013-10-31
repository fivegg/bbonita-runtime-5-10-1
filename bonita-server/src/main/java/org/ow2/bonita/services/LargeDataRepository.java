/**
 * Copyright (C) 2009  BonitaSoft S.A.
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
package org.ow2.bonita.services;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Charles Souillard
 */
public interface LargeDataRepository {

  void storeData(List<String> categories, String key, Serializable value, boolean overWrite);

  boolean deleteData(List<String> categories, String key);
  boolean deleteData(List<String> categories);
  void clean();
  
  Set<String> getKeys();
  Set<String> getKeys(List<String> categories);
  Set<String> getKeys(List<String> categories, String regex);
  
  <T> T getData(Class<T> clazz, List<String> categories, String key);
  <T> Map<String, T> getData(Class<T> clazz, List<String> categories);
  <T> Map<String, T> getData(Class<T> clazz, List<String> categories, Collection<String> keys);
  <T> Map<String, T> getDataFromRegex(Class<T> clazz, List<String> categories, String regex);
  
  boolean isEmpty();
}
