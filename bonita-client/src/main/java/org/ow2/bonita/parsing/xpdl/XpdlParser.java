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
 **/
package org.ow2.bonita.parsing.xpdl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilderFactory;

import org.ow2.bonita.parsing.xpdl.binding.ActivityBinding;
import org.ow2.bonita.parsing.xpdl.binding.DataFieldBinding;
import org.ow2.bonita.parsing.xpdl.binding.ParticipantBinding;
import org.ow2.bonita.parsing.xpdl.binding.TransitionBinding;
import org.ow2.bonita.parsing.xpdl.binding.WorkflowProcessBinding;
import org.ow2.bonita.util.BonitaRuntimeException;
import org.ow2.bonita.util.ExceptionManager;
import org.ow2.bonita.util.Misc;
import org.ow2.bonita.util.XmlConstants;
import org.ow2.bonita.util.xml.Bindings;
import org.ow2.bonita.util.xml.Entity;
import org.ow2.bonita.util.xml.Parse;
import org.ow2.bonita.util.xml.Parser;
import org.ow2.bonita.util.xml.Problem;
import org.ow2.bonita.util.xml.UrlEntity;
import org.ow2.bonita.util.xml.XmlUtil;
import org.w3c.dom.Element;

/**
 * @author Marc Blachon, Guillaume Porcher, Charles Souillard, Miguel Valdes, Pierre Vigneras
 */
public class XpdlParser extends Parser {
  private static final Logger LOG = Logger.getLogger(XpdlParser.class.getName());

  public static final String CATEGORY_MAJOR_ELT = "majorElements";

  /** path to the directory containing all xsd files. */
  private static final String RESSOURCES_DIR = "";

  /** all schema resources used to parse xpdl files. */
  private static final String[] SCHEMA_RESOURCES = { RESSOURCES_DIR + XmlConstants.XML_SCHEMA,
      RESSOURCES_DIR + XmlConstants.XPDL_1_0_SCHEMA };

  public static final Bindings DEFAULT_BINDINGS = getDefaultBindings();

  // the default entities are initialized at the bottom of this file.
  private static final Map<String, Entity> DEFAULT_ENTITIES = getDefaultEntities();

  /** schema document URIs as stringsF */
  private static final String[] SCHEMA_SOURCES = getSchemaSources();

  public XpdlParser() {
    super(DEFAULT_BINDINGS, DEFAULT_ENTITIES);
  }

  @Override
  public Object parseDocumentElement(final Element packageElement, final Parse parse) {
    final String packageNS = packageElement.getNamespaceURI();
    if (!XmlConstants.XPDL_1_0_NS.equals(packageNS)) {
      final String message = ExceptionManager.getInstance().getMessage("bpx_XP_1", packageNS);
      throw new BonitaRuntimeException(message);
    }

    final Element processesContainer = XmlUtil.element(packageElement, "WorkflowProcesses");
    if (processesContainer == null) {
      final String message = ExceptionManager.getInstance().getMessage("bpx_XP_3");
      throw new BonitaRuntimeException(message);
    }
    final List<Element> processes = XmlUtil.elements(processesContainer, "WorkflowProcess");
    if (processes != null && processes.size() > 1) {
      final String message = ExceptionManager.getInstance().getMessage("bpx_XP_4");
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
    // ignore white space can only be set if parser is validating
    documentBuilderFactory.setIgnoringElementContentWhitespace(false);
    // select XML schema as the schema language (a.o.t. DTD)
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
    defaultBindings.addBinding(new WorkflowProcessBinding());
    defaultBindings.addBinding(new ActivityBinding());
    defaultBindings.addBinding(new TransitionBinding());
    return defaultBindings;
  }

  /** factory method for the default entities */
  private static Map<String, Entity> getDefaultEntities() {
    final Map<String, Entity> defaultSchemaCatalog = new HashMap<String, Entity>();
    final ClassLoader resourceLoader = XpdlParser.class.getClassLoader();
    defaultSchemaCatalog.put(XmlConstants.XML_NS, new UrlEntity(XmlConstants.XML_SCHEMA, resourceLoader));
    defaultSchemaCatalog.put(XmlConstants.XML_NS2, new UrlEntity(XmlConstants.XML_SCHEMA, resourceLoader));
    defaultSchemaCatalog.put(XmlConstants.XPDL_1_0_NS, new UrlEntity(XmlConstants.XPDL_1_0_SCHEMA, resourceLoader));
    return defaultSchemaCatalog;
  }

  private static String[] getSchemaSources() {
    final String[] tab = new String[SCHEMA_RESOURCES.length];
    final ClassLoader resourceLoader = XpdlParser.class.getClassLoader();
    for (int i = 0; i < SCHEMA_RESOURCES.length; i++) {
      tab[i] = resourceLoader.getResource(SCHEMA_RESOURCES[i]).toExternalForm();
    }
    return tab;
  }

  /**
   * throws an exception with appropriate message in case the parse contains errors or fatal errors. This method also
   * logs the problems with severity 'warning'.
   */
  public void checkProblems(final String description, final Parse parse) {
    if (parse.hasProblems()) {
      StringBuffer errorMsg = null;
      for (final Problem p : parse.getProblems()) {
        if (p.getSeverity().equals(Problem.SEVERITY_ERROR) || p.getSeverity().equals(Problem.SEVERITY_FATALERROR)) {
          if (errorMsg == null) {
            errorMsg = new StringBuffer();
          }
          errorMsg.append(Misc.LINE_SEPARATOR).append("  ").append(p.toString());
          if (p.getCause() != null) {
            LOG.log(Level.SEVERE, p.toString(), p.getCause());
          } else {
            LOG.severe(p.toString());
          }
        } else {
          LOG.warning(p.toString());
        }
      }
      if (errorMsg != null) {
        throw new BonitaRuntimeException("errors during parsing of " + description + ": " + errorMsg);
      }
    }
  }

}
