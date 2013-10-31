/**
 * Copyright (C) 2009-2012 BonitaSoft S.A.
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
package org.ow2.bonita.connector.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ow2.bonita.connector.core.desc.Array;
import org.ow2.bonita.connector.core.desc.Category;
import org.ow2.bonita.connector.core.desc.Checkbox;
import org.ow2.bonita.connector.core.desc.CompositeWidget;
import org.ow2.bonita.connector.core.desc.ConnectorDescriptor;
import org.ow2.bonita.connector.core.desc.Enumeration;
import org.ow2.bonita.connector.core.desc.Getter;
import org.ow2.bonita.connector.core.desc.Group;
import org.ow2.bonita.connector.core.desc.Page;
import org.ow2.bonita.connector.core.desc.Password;
import org.ow2.bonita.connector.core.desc.Radio;
import org.ow2.bonita.connector.core.desc.Select;
import org.ow2.bonita.connector.core.desc.Setter;
import org.ow2.bonita.connector.core.desc.SimpleList;
import org.ow2.bonita.connector.core.desc.Text;
import org.ow2.bonita.connector.core.desc.Textarea;
import org.ow2.bonita.definition.PerformerAssign;
import org.ow2.bonita.facade.runtime.AttachmentInstance;
import org.ow2.bonita.parsing.connector.ConnectorDescriptorParser;
import org.ow2.bonita.util.xml.Parse;
import org.w3c.dom.Document;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

/**
 * 
 * @author Matthieu Chaffotte
 * 
 */
public final class ConnectorDescriptorAPI {

  private static final String CURRENT_VERSION = "5.0";

  private static final Logger LOG = Logger.getLogger(ConnectorDescriptorAPI.class.getName());

  private static XStream xstream;

  private ConnectorDescriptorAPI() {
    super();
  }

  /**
   * Load the connector descriptor
   * 
   * @param c the connector class
   * @return the connector descriptor
   */
  public final static ConnectorDescriptor load(final Class<? extends Connector> c) {
    load();
    final String descriptorPath = c.getSimpleName() + ".xml";
    InputStream input = null;
    try {
      final ConnectorDescriptorParser parser = new ConnectorDescriptorParser();
      final Parse parse = parser.createParse();
      input = c.getResourceAsStream(descriptorPath);
      parse.setInputStream(input);
      parse.setClassLoader(c.getClassLoader());
      return (ConnectorDescriptor) parse.execute().getDocumentObject();
    } catch (final Throwable e) {
      return null;
    } finally {
      try {
        if (input != null) {
          input.close();
        }
      } catch (final IOException e) {
        if (LOG.isLoggable(Level.SEVERE)) {
          LOG.severe(e.getMessage());
        }
      }
    }
  }

  private static synchronized void load() {
    if (xstream == null) {
      xstream = new XStream(new DomDriver());
      xstream.alias("connector", ConnectorDescriptor.class);
      xstream.alias("setter", Setter.class);
      xstream.alias("getter", Getter.class);
      xstream.alias("page", Page.class);
      xstream.alias("text", Text.class);
      xstream.alias("textarea", Textarea.class);
      xstream.alias("checkbox", Checkbox.class);
      xstream.alias("radio", Radio.class);
      xstream.alias("select", Select.class);
      xstream.alias("enumeration", Enumeration.class);
      xstream.alias("group", Group.class);
      xstream.alias("compositeWidget", CompositeWidget.class);
      xstream.alias("password", Password.class);
      xstream.alias("array", Array.class);
      xstream.alias("list", SimpleList.class);
      xstream.alias("perfomerAssign", PerformerAssign.class);
      xstream.alias("category", Category.class);
      xstream.aliasType("xml", Document.class);
      xstream.registerConverter(new DocumentConverter());
      xstream.aliasType("attachment", AttachmentInstance.class);
      xstream.registerConverter(new AttachmentConverter());
      xstream.omitField(Category.class, "classLoader");
      xstream.omitField(ConnectorDescriptor.class, "classLoader");
      xstream.setMode(XStream.XPATH_ABSOLUTE_REFERENCES);
    }
  }

  public static void save(final ConnectorDescriptor descriptor, final OutputStream output) {
    load();
    final String version = descriptor.getVersion();
    if (!CURRENT_VERSION.equals(version)) {
      descriptor.setVersion(CURRENT_VERSION);
    }
    xstream.toXML(descriptor, output);
  }

}
