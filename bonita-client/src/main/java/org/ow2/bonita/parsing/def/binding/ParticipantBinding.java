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
import org.ow2.bonita.util.BonitaRuntimeException;
import org.ow2.bonita.util.GroovyExpression;
import org.ow2.bonita.util.Misc;
import org.ow2.bonita.util.ProcessBuilder;
import org.ow2.bonita.util.xml.Parse;
import org.ow2.bonita.util.xml.Parser;
import org.ow2.bonita.util.xml.XmlUtil;
import org.w3c.dom.Element;

/**
 * @author Anthony Birembaut
 */
public class ParticipantBinding extends ElementBinding {

  private static final Logger LOGGER = Logger.getLogger(ParticipantBinding.class.getName());

  public ParticipantBinding() {
    super(XmlDef.PARTICIPANT);
  }

  public Object parse(final Element participantElement, final Parse parse, final Parser parser) {
    if (LOGGER.isLoggable(Level.FINE)) {
      LOGGER.fine("parsing element = " + participantElement);
    }

    String name = null;
    
    try {
      ProcessBuilder processBuilder = parse.findObject(ProcessBuilder.class);
      
      name = XmlUtil.attribute(participantElement, XmlDef.NAME);
      final String label = getChildTextContent(participantElement, XmlDef.LABEL);
      final String description = getChildTextContent(participantElement, XmlDef.DESCRIPTION);
      final Element roleMapperElement = XmlUtil.element(participantElement, XmlDef.ROLE_MAPPER);
      if (XmlUtil.elements(roleMapperElement) == null || XmlUtil.elements(roleMapperElement).isEmpty()){
        processBuilder.addHuman(name);
        processBuilder.addDescription(description);
        processBuilder.addLabel(label);
      } else {
        processBuilder.addGroup(name);
        processBuilder.addDescription(description);
        processBuilder.addLabel(label);
        final String roleMapperClassName = getChildTextContent(roleMapperElement, XmlDef.CLASSNAME);
        final String roleMapperDescription = getChildTextContent(roleMapperElement, XmlDef.DESCRIPTION);
        processBuilder.addGroupResolver(roleMapperClassName);
        processBuilder.addDescription(roleMapperDescription);
        
        Element roleMapperParametersElement = XmlUtil.element(roleMapperElement, XmlDef.PARAMETERS);
        List<Element> roleMapperParameterElements = XmlUtil.elements(roleMapperParametersElement, XmlDef.PARAMETER);
        if (roleMapperParameterElements != null) {
          for (Element roleMapperParameterElement : roleMapperParameterElements) {
            try {
              String key = roleMapperParameterElement.getAttribute(XmlDef.NAME);
              if (GroovyExpression.isGroovyExpression(key)) {
                processBuilder.addOutputParameter(key, ((Object[])Misc.deserialize(Misc.base64DecodeAndGather(roleMapperParameterElement.getTextContent()), parse.getContextProperties()))[0].toString());
              } else {
                processBuilder.addInputParameter(key, (Object[])Misc.deserialize(Misc.base64DecodeAndGather(roleMapperParameterElement.getTextContent()), parse.getContextProperties()));
              }
            } catch (Exception e) {
              throw new BonitaRuntimeException("Error while deserializing", e);
            }
          }
        }
      }
    } catch (Exception e) {
      parse.addProblem("Error parsing Participant " + name, e);
    }
    return null;
  }
}
