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
package org.ow2.bonita.parsing.def;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;

import org.ow2.bonita.building.XmlDef;
import org.ow2.bonita.parsing.def.binding.ActivityBinding;
import org.ow2.bonita.parsing.def.binding.AttachmentBinding;
import org.ow2.bonita.parsing.def.binding.CategoryBinding;
import org.ow2.bonita.parsing.def.binding.ConnectorBinding;
import org.ow2.bonita.parsing.def.binding.DataFieldBinding;
import org.ow2.bonita.parsing.def.binding.EventSubProcessBinding;
import org.ow2.bonita.parsing.def.binding.ParticipantBinding;
import org.ow2.bonita.parsing.def.binding.ProcessBinding;
import org.ow2.bonita.parsing.def.binding.TransitionBinding;
import org.ow2.bonita.util.BonitaRuntimeException;
import org.ow2.bonita.util.ExceptionManager;
import org.ow2.bonita.util.XmlConstants;
import org.ow2.bonita.util.xml.Bindings;
import org.ow2.bonita.util.xml.Entity;
import org.ow2.bonita.util.xml.Parse;
import org.ow2.bonita.util.xml.Parser;
import org.ow2.bonita.util.xml.UrlEntity;
import org.ow2.bonita.util.xml.XmlUtil;
import org.w3c.dom.Element;

/**
 * @author Anthony Birembaut
 * 
 */
public class XmlDefParser extends Parser {

  public static final String CATEGORY_MAJOR_ELT = "majorElements";

  /** path to the directory containing all xsd files. */
  private static final String RESSOURCES_DIR = "";

  /** all schema resources used to parse xml def files. */
  private static final String[] SCHEMA_RESOURCES = { RESSOURCES_DIR + XmlConstants.XML_SCHEMA,
      RESSOURCES_DIR + XmlConstants.XML_PROCESS_DEF_TRANSITIONAL_SCHEMA };

  public static final Bindings DEFAULT_BINDINGS = getDefaultBindings();

  // the default entities are initialized at the bottom of this file.
  private static final Map<String, Entity> DEFAULT_ENTITIES = getDefaultEntities();

  /** schema document URIs as stringsF */
  private static final String[] SCHEMA_SOURCES = getSchemaSources();

  public XmlDefParser() {
    super(DEFAULT_BINDINGS, DEFAULT_ENTITIES);
  }

  @Override
  public Object parseDocumentElement(final Element processDefinitionElement, final Parse parse) {

    final String processDefinitionTag = processDefinitionElement.getTagName();
    if (!XmlDef.PROCESS_DEFINITION.equals(processDefinitionTag)) {
      final String message = ExceptionManager.getInstance().getMessage("bpx_XDP_1", processDefinitionTag);
      throw new BonitaRuntimeException(message);
    }
    final List<Element> processes = XmlUtil.elements(processDefinitionElement, XmlDef.PROCESS);
    if (processes != null && processes.size() > 1) {
      final String message = ExceptionManager.getInstance().getMessage("bpx_XDP_2");
      throw new BonitaRuntimeException(message);
    }
    final Element processElement = processes.get(0);
    return parseElement(processElement, parse, CATEGORY_MAJOR_ELT);
  }

  @Override
  public synchronized DocumentBuilderFactory getDocumentBuilderFactory() {
    documentBuilderFactory = newDocumentBuilderFactory();
    documentBuilderFactory.setNamespaceAware(true);
    documentBuilderFactory.setValidating(true);
    // select xml schema as the schema language (a.o.t. DTD)
    documentBuilderFactory.setAttribute(XmlConstants.JAXP_SCHEMALANGUAGE, XmlConstants.XML_NS);
    // set schema sources
    documentBuilderFactory.setAttribute(XmlConstants.JAXP_SCHEMASOURCE, SCHEMA_SOURCES);
    return documentBuilderFactory;
  }

  /** factory method for the default bindings */
  private static Bindings getDefaultBindings() {
    final Bindings defaultBindings = new Bindings();

    defaultBindings.addBinding(new ParticipantBinding());
    defaultBindings.addBinding(new DataFieldBinding());
    defaultBindings.addBinding(new ProcessBinding());
    defaultBindings.addBinding(new ActivityBinding());
    defaultBindings.addBinding(new TransitionBinding());
    defaultBindings.addBinding(new AttachmentBinding());
    defaultBindings.addBinding(new ConnectorBinding());
    defaultBindings.addBinding(new CategoryBinding());
    defaultBindings.addBinding(new EventSubProcessBinding());

    return defaultBindings;
  }

  /** factory method for the default entities */
  private static Map<String, Entity> getDefaultEntities() {
    final Map<String, Entity> defaultSchemaCatalog = new HashMap<String, Entity>();
    final ClassLoader resourceLoader = XmlDefParser.class.getClassLoader();
    defaultSchemaCatalog.put(XmlConstants.XML_NS, new UrlEntity(XmlConstants.XML_SCHEMA, resourceLoader));
    defaultSchemaCatalog.put(XmlConstants.XML_NS2, new UrlEntity(XmlConstants.XML_SCHEMA, resourceLoader));
    return defaultSchemaCatalog;
  }

  private static String[] getSchemaSources() {
    final String[] tab = new String[SCHEMA_RESOURCES.length];
    final ClassLoader resourceLoader = XmlDefParser.class.getClassLoader();
    for (int i = 0; i < SCHEMA_RESOURCES.length; i++) {
      tab[i] = resourceLoader.getResource(SCHEMA_RESOURCES[i]).toExternalForm();
    }
    return tab;
  }
}
