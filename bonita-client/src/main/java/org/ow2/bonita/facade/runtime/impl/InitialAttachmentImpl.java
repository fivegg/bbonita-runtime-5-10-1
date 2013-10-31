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
package org.ow2.bonita.facade.runtime.impl;

import java.util.Collections;
import java.util.Map;

import org.ow2.bonita.facade.def.element.AttachmentDefinition;
import org.ow2.bonita.facade.runtime.InitialAttachment;
import org.ow2.bonita.util.Misc;
import org.ow2.bonita.util.xml.XStreamUtil;

import com.thoughtworks.xstream.XStream;

public class InitialAttachmentImpl implements InitialAttachment {

  private static final long serialVersionUID = -4911584710221345476L;
  
  protected String name;
  protected String label;
  protected String description;

  protected String fileName;
  protected byte[] content;
  protected Map<String, String> metaData;

  public InitialAttachmentImpl(String name, byte[] content) {
    Misc.checkArgsNotNull(name);
    this.name = name;
    this.content = content;
  }
  
  public InitialAttachmentImpl(AttachmentDefinition attachment, byte[] content) {
    this(attachment.getName(), content);
    setDescription(attachment.getDescription());
    setFileName(attachment.getFileName());
    setLabel(attachment.getLabel());
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public void setDescription(String description) {
    this.description = description;
  }


  public void setFileName(String fileName) {
    this.fileName = fileName;
  }


  public void setMetaData(Map<String, String> metaData) {
    this.metaData = metaData;
  }

  public String getName() {
    return name;
  }

  public String getLabel() {
    return label;
  }

  public String getDescription() {
    return description;
  }

  public String getFileName() {
    return fileName;
  }

  public byte[] getContent() {
    return content;
  }

  public Map<String, String> getMetaData() {
    if (metaData == null) {
      return Collections.emptyMap();
    }
    return metaData;
  }

  @Override
  public String toString() {
  	XStream xstream = XStreamUtil.getDefaultXstream();
	  return xstream.toXML(this);
  }

}
