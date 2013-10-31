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
package org.ow2.bonita.search;

import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.lucene.document.Document;
import org.hibernate.collection.PersistentSet;
import org.hibernate.search.bridge.FieldBridge;
import org.hibernate.search.bridge.LuceneOptions;

/**
 * 
 * @author Matthieu Chaffotte
 *
 */
public class StringSetFieldBridge implements FieldBridge {

  private static final Logger LOG = Logger.getLogger(StringSetFieldBridge.class.getName());

  public void set(String name, Object value, Document document, LuceneOptions luceneOptions) {
    @SuppressWarnings("unchecked") Set<String> objects = (Set<String>) value;
    if (objects != null) {
      if (objects instanceof PersistentSet) {
        PersistentSet set = (PersistentSet) value;
        if (!set.wasInitialized()) {
          try {
            set.forceInitialization();
          } catch (Throwable e) {
            if (LOG.isLoggable(Level.WARNING)) {
              LOG.warning("Could not initialize set :" + e.getMessage());
            }
            return;
          }
        }
        for (Object object : set) {
        	if(object != null) {
        		luceneOptions.addFieldToDocument(name, object.toString(), document);
        	}
        }
      } else {
        for (String object : objects) {
          if(object != null) {
            luceneOptions.addFieldToDocument(name, object, document);
          }
        }
      } 
    }
  }

}
