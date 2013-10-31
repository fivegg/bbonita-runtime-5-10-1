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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ow2.bonita.building.XmlDef;
import org.ow2.bonita.util.BonitaRuntimeException;
import org.ow2.bonita.util.Misc;
import org.ow2.bonita.util.ProcessBuilder;
import org.ow2.bonita.util.xml.Parse;
import org.ow2.bonita.util.xml.Parser;
import org.ow2.bonita.util.xml.XmlUtil;
import org.w3c.dom.Element;

/**
 * @author Anthony Birembaut, Elias Ricken de Medeiros
 */
public class DataFieldBinding extends ElementBinding {

  private static final Logger LOGGER = Logger.getLogger(DataFieldBinding.class.getName());

  public DataFieldBinding() {
    super(XmlDef.DATA_FIELD);
  }

  public Object parse(final Element datafieldElement, final Parse parse, final Parser parser) {
    if (LOGGER.isLoggable(Level.FINE)) {
      LOGGER.fine("parsing element = " + datafieldElement);
    }
 
    String name = null;
    
    try {
      ProcessBuilder processBuilder = parse.findObject(ProcessBuilder.class);
      
      name = XmlUtil.attribute(datafieldElement, XmlDef.NAME);
      final String label = getChildTextContent(datafieldElement, XmlDef.LABEL);
      final String description = getChildTextContent(datafieldElement, XmlDef.DESCRIPTION);
      final String dataTypeClassName = getChildTextContent(datafieldElement, XmlDef.DATATYPE_CLASSNAME);
      final String initialValue = getChildTextContent(datafieldElement, XmlDef.VALUE);
      final String scriptingValue = getChildTextContent(datafieldElement, XmlDef.SCRIPTING_VALUE);
      final String isTransient = getChildTextContent(datafieldElement, XmlDef.IS_TRANSIENT);
      Element enumerationValuesElement = XmlUtil.element(datafieldElement, XmlDef.ENUMERATION_VALUES);
      List<Element> enumerationValueElements = XmlUtil.elements(enumerationValuesElement, XmlDef.ENUMERATION_VALUE);
      Set<String> enumerationValues = new HashSet<String>();
      if (enumerationValueElements != null) {
        for (Element enumerationValueElement : enumerationValueElements) {
          enumerationValues.add(enumerationValueElement.getTextContent());
        }
      }
      if (scriptingValue != null && scriptingValue.length() > 0) {
        if (enumerationValues.isEmpty()) {
          processBuilder.addObjectData(name, dataTypeClassName, scriptingValue);
        } else {
          processBuilder.addEnumData(name, scriptingValue, enumerationValues);
        }
      } else {
        try {
          Object value = Misc.deserialize(Misc.base64DecodeAndGather(initialValue), parse.getContextProperties());
          if (enumerationValues.isEmpty()) {
            processBuilder.addObjectData(name, dataTypeClassName, value);
          } else {
            processBuilder.addEnumData(name, enumerationValues, (String)value);
          }
        } catch (Exception e) {
          throw new BonitaRuntimeException("Error while deserializing", e);
        }
      }
      if (isTransient != null) {
        boolean isTransientValue = Boolean.parseBoolean(isTransient);
        if (isTransientValue) {
          processBuilder.setTransient();
        }
      }
      processBuilder.addDescription(description);
      processBuilder.addLabel(label);
    } catch (Exception e) {
      parse.addProblem("Error parsing DataField " + name, e);
    }
    return null;
  }
}
