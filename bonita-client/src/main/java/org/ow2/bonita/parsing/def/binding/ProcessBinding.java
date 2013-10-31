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
import org.ow2.bonita.light.LightProcessDefinition.ProcessType;
import org.ow2.bonita.util.BonitaRuntimeException;
import org.ow2.bonita.util.ExceptionManager;
import org.ow2.bonita.util.Misc;
import org.ow2.bonita.util.ProcessBuilder;
import org.ow2.bonita.util.xml.Parse;
import org.ow2.bonita.util.xml.Parser;
import org.ow2.bonita.util.xml.XmlUtil;
import org.w3c.dom.Element;

/**
 * @author Anthony Birembaut, Matthieu Chaffotte
 */
public class ProcessBinding extends ElementBinding {

  public ProcessBinding() {
    super(XmlDef.PROCESS);
  }

  private static final Logger LOGGER = Logger.getLogger(ProcessBinding.class.getName());

  public Object parse(final Element processElement, final Parse parse, final Parser parser) {
    if (LOGGER.isLoggable(Level.FINE)) {
      LOGGER.fine("parsing element = " + processElement);
    }
    String name = null;
    try {
      name = XmlUtil.attribute(processElement, XmlDef.NAME);
      final String version = XmlUtil.attribute(processElement, XmlDef.VERSION);
      final String description = getChildTextContent(processElement, XmlDef.DESCRIPTION);
      final String label = getChildTextContent(processElement, XmlDef.LABEL);
      final ProcessType type = getEnumValue(ProcessType.class, getChildTextContent(processElement, XmlDef.TYPE), ProcessType.PROCESS);
  
      ProcessBuilder processBuilder = ProcessBuilder.createProcess(name, version);
      processBuilder.addDescription(description);
      processBuilder.addLabel(label);
      if (ProcessType.EVENT_SUB_PROCESS.equals(type)) {
        processBuilder.setEventSubProcess();
      }

      parse.pushObject(processBuilder);

      parseElementList(processElement, XmlDef.PARTICIPANTS, XmlDef.PARTICIPANT, parse, parser);
      parseElementList(processElement, XmlDef.DATA_FIELDS, XmlDef.DATA_FIELD, parse, parser);
      parseElementList(processElement, XmlDef.ATTACHMENTS, XmlDef.ATTACHMENT, parse, parser);
      parseElementList(processElement, XmlDef.CONNECTORS, XmlDef.CONNECTOR, parse, parser);
      parseElementList(processElement, XmlDef.ACTIVITIES, XmlDef.ACTIVITY, parse, parser);
      parseElementList(processElement, XmlDef.TRANSITIONS, XmlDef.TRANSITION, parse, parser);
      parseElementList(processElement, XmlDef.CATEGORIES, XmlDef.CATEGORY, parse, parser);
      parseElementList(processElement, XmlDef.EVENT_SUB_PROCESSES, XmlDef.EVENT_SUB_PROCESS, parse, parser);

      parse.popObject();
      return processBuilder.done();
    } catch (Exception e) {
      parse.addProblem("Error parsing Process " + name, e);
      return null;
    }
  }

  protected <T extends Enum<T> > T getEnumValue(Class< T > enumType, String valueAsString, T defaultValue) {
    if (valueAsString == null) {
      return defaultValue;
    }
    try {
      return Misc.stringToEnum(enumType, valueAsString);
    } catch (IllegalArgumentException e) {
      String message = ExceptionManager.getInstance().getMessage(
          "bpx_EB_1", valueAsString, enumType.getName());
      throw new BonitaRuntimeException(message, e);
    }
  }

}
