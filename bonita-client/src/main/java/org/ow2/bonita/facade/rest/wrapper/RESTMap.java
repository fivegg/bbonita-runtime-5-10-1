/**
 * Copyright (C) 2010-2013 BonitaSoft S.A.
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
package org.ow2.bonita.facade.rest.wrapper;

import java.util.HashMap;
import java.util.Map;

import org.ow2.bonita.util.xml.XStreamUtil;

import com.thoughtworks.xstream.XStream;

/**
 * 
 * @author Elias Ricken de Medeiros, Matthieu Chaffotte
 * 
 * @param <K>
 * @param <V>
 */
public class RESTMap<K, V> extends HashMap<K, V> {

  private static final long serialVersionUID = 2121584713810757386L;

  private final String xml;

  public RESTMap(final String xml) {
    super();
    this.xml = xml;
  }

  @SuppressWarnings("unchecked")
  public Map<K, V> getActualMap() {
    final XStream xstream = XStreamUtil.getDefaultXstream();
    return (Map<K, V>) xstream.fromXML(xml);
  }

  @Override
  public String toString() {
    final XStream xstream = XStreamUtil.getDefaultXstream();
    return xstream.toXML(this);
  }

  public static RESTMap<?, ?> valueOf(final String jsonRepresetation) {
    final XStream xstream = XStreamUtil.getDefaultXstream();
    return (RESTMap<?, ?>) xstream.fromXML(jsonRepresetation);
  }

}
