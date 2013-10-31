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
 **/
package org.ow2.bonita.parsing.xpdl.binding;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.ow2.bonita.util.ProcessBuilder;
import org.ow2.bonita.util.xml.Parse;
import org.ow2.bonita.util.xml.Parser;
import org.ow2.bonita.util.xml.XmlUtil;
import org.w3c.dom.Element;

/**
 * @author Marc Blachon, Guillaume Porcher, Charles Souillard, Miguel Valdes, Pierre Vigneras
 */
public class DataFieldBinding extends MajorElementBinding {

  private static final Logger LOG = Logger.getLogger(DataFieldBinding.class.getName());

  private static final String[] JAVA_KEYWORDS = new String[]{"abstract", "continue", "for", "new", "switch",
    "assert", "default", "goto", "package", "synchronized", "boolean", "do", "if", "private", "this", "break",
    "double", "implements", "protected", "throw", "byte", "else", "import", "public", "throws", "case", "enum",
    "instanceof", "return", "transient", "catch", "extends", "int", "short", "try", "char", "final", "interface",
    "static", "void", "class", "finally", "long", "strictfp", "volatile", "const", "float", "native", "super", "while"};

  public DataFieldBinding() {
    super("DataField");
  }

  public Object parse(final Element datafieldElement, final Parse parse, final Parser parser) {
    if (LOG.isLoggable(Level.FINE)) {
      LOG.fine("parsing element = " + datafieldElement);
    }

    Boolean isParsingActivity = parse.findObject(Boolean.class);
    final String name = getId(datafieldElement);
    for (final String javaKeyword : JAVA_KEYWORDS) {
      if (javaKeyword.equals(name)) {
        parse.addProblem("A datafield is declared with id : " + name + ", it is forbidden as it is a java Keyword");
      }
    }

    final boolean activityDatafield = containsExtendedAttribute(datafieldElement, "PropertyActivity");
    if (activityDatafield && !datafieldElement.getParentNode().getParentNode().getLocalName().equals("WorkflowProcess")) {
      parse.addProblem("A datafield with process id = " + name
          + " defined at package level is expected to be an 'activity datafield' as it declares "
          + "the corresponding extended attribute: it is forbiden. "
          + "'Activity datafields' can only be defined at process level.");
    }
    if (activityDatafield && isParsingActivity == null) {
      return datafieldElement; 
    }
 
    final String description = getChildTextContent(datafieldElement, "Description");
    final String initialValue = getChildTextContent(datafieldElement, "InitialValue");
    final String length = getChildTextContent(datafieldElement, "Length");
    if (length != null) {
      parse.addProblem("Length element not yet supported on element DataField, processDefinitionUUID = " + name);
    }

    boolean array = false;
    final String isArray = XmlUtil.attribute(datafieldElement, "IsArray");
    if (isArray != null) {
      array = Boolean.valueOf(isArray);
      if (array) {
        parse.addProblem("isArray=true not yet supported on element DataField.");
      }
    }

    ProcessBuilder processBuilder = parse.findObject(ProcessBuilder.class);

    addData(datafieldElement, name, initialValue, processBuilder, parse);

    processBuilder.addDescription(description);
    processBuilder.addLabel(XmlUtil.attribute(datafieldElement, "Name"));
    return null;
  }
}
