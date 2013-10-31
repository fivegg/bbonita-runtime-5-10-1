/**
 * Copyright (C) 2010-2012 BonitaSoft S.A.
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
package org.ow2.bonita.parsing.connector;

import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;

import org.ow2.bonita.parsing.connector.binding.CategoryBinding;
import org.ow2.bonita.parsing.connector.binding.ConnectorBinding;
import org.ow2.bonita.parsing.connector.binding.GetterBinding;
import org.ow2.bonita.parsing.connector.binding.PageBinding;
import org.ow2.bonita.parsing.connector.binding.PropertyBinding;
import org.ow2.bonita.parsing.connector.binding.SetterBinding;
import org.ow2.bonita.util.BonitaRuntimeException;
import org.ow2.bonita.util.xml.Bindings;
import org.ow2.bonita.util.xml.Entity;
import org.ow2.bonita.util.xml.Parse;
import org.ow2.bonita.util.xml.Parser;
import org.w3c.dom.Element;

/**
 * 
 * @author Matthieu Chaffotte
 * 
 */
public class ConnectorDescriptorParser extends Parser {

  public static final Bindings DEFAULT_BINDINGS = getDefaultBindings();

  // the default entities are initialized at the bottom of this file.
  private static Map<String, Entity> defaultEntities = getDefaultEntities();

  public ConnectorDescriptorParser() {
    super(DEFAULT_BINDINGS, defaultEntities);
  }

  @Override
  public Object parseDocumentElement(final Element connectorElement, final Parse parse) {
    if (!"connector".equals(connectorElement.getNodeName())) {
      final String message = "No connector defined within this XML file";
      throw new BonitaRuntimeException(message);
    }
    return parseElement(connectorElement, parse);
  }

  @Override
  public synchronized DocumentBuilderFactory getDocumentBuilderFactory() {
    documentBuilderFactory = newDocumentBuilderFactory();
    documentBuilderFactory.setNamespaceAware(true);
    return documentBuilderFactory;
  }

  /** factory method for the default bindings */
  private static Bindings getDefaultBindings() {
    final Bindings defaultBindings = new Bindings();
    defaultBindings.addBinding(new ConnectorBinding());
    defaultBindings.addBinding(new CategoryBinding());
    defaultBindings.addBinding(new SetterBinding());
    defaultBindings.addBinding(new GetterBinding());
    defaultBindings.addBinding(new PageBinding());
    defaultBindings.addBinding(new PropertyBinding());
    return defaultBindings;
  }

  /** factory method for the default entities */
  private static Map<String, Entity> getDefaultEntities() {
    return new HashMap<String, Entity>();
  }

}
