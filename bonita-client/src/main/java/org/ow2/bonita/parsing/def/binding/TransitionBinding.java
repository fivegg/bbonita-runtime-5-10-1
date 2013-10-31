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
 * @author Anthony Birembaut, Matthieu Chaffotte
 */
public class TransitionBinding extends ElementBinding {

  public TransitionBinding() {
    super(XmlDef.TRANSITION);
  }

  private static final Logger LOGGER = Logger.getLogger(TransitionBinding.class.getName());

  @Override
  public Object parse(final Element transitionElement, final Parse parse, final Parser parser) {
    if (LOGGER.isLoggable(Level.FINE)) {
      LOGGER.fine("parsing element = " + transitionElement);
    }
    String name = null;
    try {
      final ProcessBuilder processBuilder = parse.findObject(ProcessBuilder.class);
      name = XmlUtil.attribute(transitionElement, XmlDef.NAME);
      final String label = getChildTextContent(transitionElement, XmlDef.LABEL);
      final String description = getChildTextContent(transitionElement, XmlDef.DESCRIPTION);
      final String from = getChildTextContent(transitionElement, XmlDef.FROM);
      final String boundaryEvent = getChildTextContent(transitionElement, XmlDef.BOUNDARY_EVENT);
      final String to = getChildTextContent(transitionElement, XmlDef.TO);
      final String condition = getChildTextContent(transitionElement, XmlDef.CONDITION);
      final String isDefault = getChildTextContent(transitionElement, XmlDef.ISDEFAULT);
      if (boundaryEvent == null) {
        processBuilder.addTransition(name, from, to);
      } else {
        processBuilder.addExceptionTransition(from, boundaryEvent, to);
      }
      processBuilder.addDescription(description);
      processBuilder.addLabel(label);
      if (condition != null && condition.length() > 0) {
        processBuilder.addCondition(condition);
      }
      if (isDefault != null) {
        final boolean isDefaultValue = Boolean.parseBoolean(isDefault);
        if (isDefaultValue) {
          processBuilder.setDefault();
        }
      }
    } catch (final Exception e) {
      parse.addProblem("Error parsing Transition " + name, e);
    }
    return null;
  }

}
