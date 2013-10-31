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

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ow2.bonita.building.XmlDef;
import org.ow2.bonita.facade.def.element.HookDefinition.Event;
import org.ow2.bonita.util.BonitaRuntimeException;
import org.ow2.bonita.util.Misc;
import org.ow2.bonita.util.ProcessBuilder;
import org.ow2.bonita.util.xml.Parse;
import org.ow2.bonita.util.xml.Parser;
import org.ow2.bonita.util.xml.XmlUtil;
import org.w3c.dom.Element;

/**
 * @author Anthony Birembaut, Matthieu Chaffotte
 */
public class ConnectorBinding extends ElementBinding {

  private static final Logger LOGGER = Logger.getLogger(ConnectorBinding.class.getName());

  public ConnectorBinding() {
    super(XmlDef.CONNECTOR);
  }

  public Object parse(final Element connectorElement, final Parse parse, final Parser parser) {
    if (LOGGER.isLoggable(Level.FINE)) {
      LOGGER.fine("parsing element = " + connectorElement);
    }
    
    String className = null;
 
    try {
      ProcessBuilder processBuilder = parse.findObject(ProcessBuilder.class);
      Boolean isReceiveEventActivity = parse.findObject(Boolean.class);
      
      className = getChildTextContent(connectorElement, XmlDef.CLASSNAME);
      final Event event = getEnumValue(Event.class, getChildTextContent(connectorElement, XmlDef.EVENT), Event.instanceOnStart);
      final String description = getChildTextContent(connectorElement, XmlDef.DESCRIPTION);
      final String throwingException = getChildTextContent(connectorElement, XmlDef.IS_THROWING_EXCEPTION);
      final String errorCode = getChildTextContent(connectorElement, XmlDef.ERROR_CODE);
  
      if (isReceiveEventActivity != null && isReceiveEventActivity) {
        processBuilder.addReceiveEventConnector(className, Boolean.parseBoolean(throwingException));
      } else {
        processBuilder.addConnector(event, className, Boolean.parseBoolean(throwingException));
      }
      if (errorCode != null) {
        processBuilder.throwCatchError(errorCode);
      }
      processBuilder.addDescription(description);
      
      Element connectorParametersElement = XmlUtil.element(connectorElement, XmlDef.PARAMETERS);
      List<Element> connectorParameterElements = XmlUtil.elements(connectorParametersElement, XmlDef.PARAMETER);
      if (connectorParameterElements != null) {
        for (Element connectorParameterElement : connectorParameterElements) {
          try {
            String key = connectorParameterElement.getAttribute(XmlDef.NAME);
            if (Misc.isSetter(key)) {
              processBuilder.addInputParameter(key, (Object[])Misc.deserialize(Misc.base64DecodeAndGather(connectorParameterElement.getTextContent()), parse.getContextProperties()));
            } else {
              processBuilder.addOutputParameter(((Object[])Misc.deserialize(Misc.base64DecodeAndGather(connectorParameterElement.getTextContent()), parse.getContextProperties()))[0].toString(), key);
            }
          } catch (Exception e) {
            throw new BonitaRuntimeException("Error while deserializing", e);
          }
        }
      }
    } catch (Exception e) {
      parse.addProblem("Error parsing Connector " + className, e);
    }
    return null;
  }
}
