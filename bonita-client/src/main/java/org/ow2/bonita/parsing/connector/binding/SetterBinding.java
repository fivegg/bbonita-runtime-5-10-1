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
package org.ow2.bonita.parsing.connector.binding;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;

import org.ow2.bonita.connector.core.desc.ConnectorDescriptor;
import org.ow2.bonita.connector.core.desc.Setter;
import org.ow2.bonita.facade.runtime.impl.AttachmentInstanceImpl;
import org.ow2.bonita.facade.uuid.DocumentUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.parsing.def.binding.ElementBinding;
import org.ow2.bonita.util.BonitaRuntimeException;
import org.ow2.bonita.util.Misc;
import org.ow2.bonita.util.xml.Parse;
import org.ow2.bonita.util.xml.Parser;
import org.ow2.bonita.util.xml.XmlUtil;
import org.w3c.dom.Element;

/**
 * 
 * @author Matthieu Chaffotte
 * 
 */
public class SetterBinding extends ElementBinding {

  private static final Set<String> ALLOWED_PARAMETERS = new HashSet<String>(14);

  static {
    ALLOWED_PARAMETERS.add("date");
    ALLOWED_PARAMETERS.add("string");
    ALLOWED_PARAMETERS.add("int");
    ALLOWED_PARAMETERS.add("double");
    ALLOWED_PARAMETERS.add("float");
    ALLOWED_PARAMETERS.add("long");
    ALLOWED_PARAMETERS.add("short");
    ALLOWED_PARAMETERS.add("boolean");
    ALLOWED_PARAMETERS.add("list");
    ALLOWED_PARAMETERS.add("map");
    ALLOWED_PARAMETERS.add("object");
    ALLOWED_PARAMETERS.add("set");
    ALLOWED_PARAMETERS.add("xml");
    ALLOWED_PARAMETERS.add("attachment");
  }

  public SetterBinding() {
    super("setter");
  }

  @Override
  public Object parse(final Element setterElement, final Parse parse, final Parser parser) {
    final ConnectorDescriptor descriptor = parse.findObject(ConnectorDescriptor.class);
    final String setterName = getChildTextContent(setterElement, "setterName");
    final String required = getChildTextContent(setterElement, "required");
    final String forbidden = getChildTextContent(setterElement, "forbidden");
    final Object[] parameters = getParameters(setterElement);
    final Setter setter = new Setter(setterName, required, forbidden, parameters);
    descriptor.addSetter(setter);
    return null;
  }

  private Object[] getParameters(final Element setter) {
    final Element parametersElement = XmlUtil.element(setter, "parameters");
    if (parametersElement != null) {
      final List<Object> parameters = new ArrayList<Object>();
      final List<Element> elements = XmlUtil.elements(parametersElement, ALLOWED_PARAMETERS);
      for (final Element element : elements) {
        parameters.add(getParameter(element));
      }
      return parameters.toArray();
    } else {
      return null;
    }
  }

  private Object getParameter(final Element parameter) {
    final String parameterType = parameter.getNodeName();
    final String parameterValue = parameter.getTextContent().trim();
    if ("int".equals(parameterType)) {
      return Integer.parseInt(parameterValue);
    }
    if ("double".equals(parameterType)) {
      return Double.parseDouble(parameterValue);
    }
    if ("string".equals(parameterType)) {
      return parameterValue;
    }
    if ("boolean".equals(parameterType)) {
      return Boolean.parseBoolean(parameterValue);
    }
    if ("long".equals(parameterType)) {
      return Long.parseLong(parameterValue);
    }
    if ("float".equals(parameterType)) {
      return new Float(parameterValue);
    }
    if ("short".equals(parameterType)) {
      return new Short(parameterValue);
    }
    if ("map".equals(parameterType)) {
      return new HashMap<Object, Object>();
    }
    if ("list".equals(parameterType)) {
      return new ArrayList<Object>();
    }
    if ("set".equals(parameterType)) {
      return new HashSet<Object>();
    }
    if ("object".equals(parameterType)) {
      return new Object();
    }
    if ("date".equals(parameterType)) {
      final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS z");
      try {
        return format.parse(parameterValue);
      } catch (final ParseException e) {
        e.printStackTrace();
      }
    }
    if ("xml".equals(parameterType)) {
      try {
        if (parameterValue != null && parameterValue.trim().length() > 0) {
          return Misc.generateDocument(parameterValue);
        } else {
          return DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        }
      } catch (final Exception ex) {
        throw new BonitaRuntimeException(ex);
      }
    }
    if ("attachment".equals(parameterType)) {
      return new AttachmentInstanceImpl(new DocumentUUID("attachment"), "attachment", new ProcessInstanceUUID("mock"),
          "admin", new Date());
    }
    throw new BonitaRuntimeException(parameterType);
  }

}
