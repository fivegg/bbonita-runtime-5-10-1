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
package org.ow2.bonita.parsing.def.binding;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.ow2.bonita.building.XmlDef;
import org.ow2.bonita.util.ProcessBuilder;
import org.ow2.bonita.util.xml.Parse;
import org.ow2.bonita.util.xml.Parser;
import org.ow2.bonita.util.xml.XmlUtil;
import org.w3c.dom.Element;

/**
 * @author Matthieu Chaffotte
 */
public class EventSubProcessBinding extends ElementBinding {

  private static final Logger LOGGER = Logger.getLogger(EventSubProcessBinding.class.getName());
  
  public EventSubProcessBinding() {
    super(XmlDef.EVENT_SUB_PROCESS);
  }

  public Object parse(Element eventSubProcessElement, Parse parse, Parser parser) {
    if (LOGGER.isLoggable(Level.FINE)) {
      LOGGER.fine("parsing element = " + eventSubProcessElement);
    }
    String name = null;

    try {
      ProcessBuilder processBuilder = parse.findObject(ProcessBuilder.class);
      name = XmlUtil.attribute(eventSubProcessElement, XmlDef.NAME);
      final String label = getChildTextContent(eventSubProcessElement, XmlDef.LABEL);
      final String description = getChildTextContent(eventSubProcessElement, XmlDef.DESCRIPTION);
      final String version = getChildTextContent(eventSubProcessElement, XmlDef.VERSION);

      processBuilder.addEventSubProcess(name, version);
      processBuilder.addDescription(description);
      processBuilder.addLabel(label);
    } catch (Exception e) {
      parse.addProblem("Error parsing Event sub-process " + name, e);
    }
    return null;
  }

}
