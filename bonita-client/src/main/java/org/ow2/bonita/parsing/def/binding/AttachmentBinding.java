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
 * @author Anthony Birembaut
 */
public class AttachmentBinding extends ElementBinding {

  private static final Logger LOGGER = Logger.getLogger(AttachmentBinding.class.getName());

  public AttachmentBinding() {
    super(XmlDef.ATTACHMENT);
  }

  public Object parse(final Element attachmentElement, final Parse parse, final Parser parser) {
    if (LOGGER.isLoggable(Level.FINE)) {
      LOGGER.fine("parsing element = " + attachmentElement);
    }
    
    String name = null;
 
    try {
      ProcessBuilder processBuilder = parse.findObject(ProcessBuilder.class);
      
      name = XmlUtil.attribute(attachmentElement, XmlDef.NAME);
      final String label = getChildTextContent(attachmentElement, XmlDef.LABEL);
      final String filePath = getChildTextContent(attachmentElement, XmlDef.FILE_PATH);
      final String description = getChildTextContent(attachmentElement, XmlDef.DESCRIPTION);
      final String fileName = getChildTextContent(attachmentElement, XmlDef.FILE_NAME);
  
      processBuilder.addAttachment(name, filePath, fileName);
      processBuilder.addDescription(description);
      processBuilder.addLabel(label);
    } catch (Exception e) {
      parse.addProblem("Error parsing Attachment " + name, e);
    }
    return null;
  }
}
