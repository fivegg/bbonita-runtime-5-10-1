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
 * 
 * Modified by Matthieu Chaffotte - BonitaSoft S.A.
 **/
package org.ow2.bonita.parsing.xpdl.binding;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ow2.bonita.definition.InstanceInitiator;
import org.ow2.bonita.util.ProcessBuilder;
import org.ow2.bonita.util.xml.Parse;
import org.ow2.bonita.util.xml.Parser;
import org.ow2.bonita.util.xml.XmlUtil;
import org.w3c.dom.Element;

/**
 * @author Marc Blachon, Guillaume Porcher, Charles Souillard, Miguel Valdes, Pierre Vigneras
 */
public class ParticipantBinding extends MajorElementBinding {

  private static final Logger LOG = Logger.getLogger(ParticipantBinding.class.getName());

  public ParticipantBinding() {
    super("Participant");
  }

  @Override
  public Object parse(final Element participantElement, final Parse parse, final Parser parser) {
    if (LOG.isLoggable(Level.FINE)) {
      LOG.fine("parsing element = " + participantElement);
    }

    final String id = getId(participantElement);
    final String description = getChildTextContent(participantElement, "Description");

    final ProcessBuilder processBuilder = parse.findObject(ProcessBuilder.class);

    String participantType = null;
    final Element participantTypeElement = XmlUtil.element(participantElement, "ParticipantType");
    if (participantElement != null) {
      participantType = XmlUtil.attribute(participantTypeElement, "Type");
    }
    if (participantType == null) {
      parse.addProblem("Participant: " + id
          + " does not define the mandatory Type attribute on ParticipantType element.");
    }

    if ("HUMAN".equals(participantType)) {
      processBuilder.addHuman(id);
    } else if ("ROLE".equals(participantType)) {
      processBuilder.addGroup(id);
    }
    processBuilder.addDescription(description);
    processBuilder.addLabel(XmlUtil.attribute(participantElement, "Name"));
    if ("ROLE".equals(participantType)) {
      parseRoleMapper(participantElement, parse, processBuilder, id);
    }
    return null;
  }

  protected void parseRoleMapper(final Element participantElement, final Parse parse,
      final ProcessBuilder processBuilder, final String roleId) {
    final Element mapperElement = getExtendedAttribute(participantElement, "Mapper");
    if (mapperElement != null) {
      final String value = XmlUtil.attribute(mapperElement, "Value");
      String className = null;
      if ("Custom".equals(value)) {
        final Element mapperClassNameElement = getExtendedAttribute(participantElement, "MapperClassName");
        className = XmlUtil.attribute(mapperClassNameElement, "Value");
      } else if ("Instance Initiator".equals(value)) {
        className = InstanceInitiator.class.getName();
      } else {
        parse.addProblem("Unsupported value on extendedAttribute Mapper: " + value);
      }
      processBuilder.addGroupResolver(className);
      final Map<String, Object[]> parameters = getRoleMapperParameters(mapperElement, parse);
      if (parameters != null) {
        for (final Entry<String, Object[]> parameter : parameters.entrySet()) {
          processBuilder.addInputParameter(parameter.getKey(), parameter.getValue());
        }
      }

    }
  }

  private Map<String, Object[]> getRoleMapperParameters(final Element participantElement, final Parse parse) {
    Map<String, Object[]> roleMapperParameters = null;
    final Element roleMapperParameterElement = XmlUtil.element(participantElement, "Parameters");
    if (roleMapperParameterElement != null) {
      roleMapperParameters = new HashMap<String, Object[]>();
      final List<Element> parameters = XmlUtil.elements(roleMapperParameterElement);
      for (final Element param : parameters) {
        final String paramType = param.getLocalName();
        final String variable = param.getAttribute("Var");
        String method;
        if ("InParameter".equals(paramType)) {
          if (param.hasAttribute("Setter")) {
            method = param.getAttribute("Setter");
            if (method.length() != 0) {
              roleMapperParameters.put(method, new Object[] { variable });
            } else {
              parse.addProblem("The Setter attribute should not be empty!");
            }
          } else {
            parse.addProblem("InParameter need the Setter attribute");
          }
        } else if ("OutParameter".equals(paramType)) {
          parse.addProblem("A RoleMapper cannot set process variables.");
        } else if ("Properties".equals(paramType)) {
          if (variable.startsWith("file:")) {
            roleMapperParameters.put("file:", new Object[] { variable.substring(5) });
          } else if (variable.startsWith("bar:")) {
            roleMapperParameters.put("bar:", new Object[] { variable.substring(4) });
          } else if (variable.startsWith("resource:")) {
            roleMapperParameters.put("resource:", new Object[] { variable.substring(9) });
          } else {
            parse
                .addProblem("The value of Var attribute can only be either file:<absolute_file_path> or bar:<file_path_in_bar_file> or resource:<resource_path>");
          }
        } // ignores all other tags
      }
    }
    return roleMapperParameters;
  }

}
