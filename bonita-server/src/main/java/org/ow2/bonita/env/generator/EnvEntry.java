/**
 * Copyright (C) 2007  Bull S. A. S.
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
 **/
package org.ow2.bonita.env.generator;

import org.ow2.bonita.util.Misc;

/**
 * Generates an environment entry.
 * 
 * @author Guillaume Porcher
 * 
 */
public class EnvEntry {

  /**
   * Will this entry be added in the environment as an entry or only as a comment.
   */
  private boolean enabled = false;

  /**
   * Name of the entry
   */
  private final String name;

  /**
   * Description
   */
  private final String description;

  /**
   * XML declaration of the object in environment.xml
   */
  private final String xmlEntry;

  /**
   * Default constructor
   * 
   * @param name
   *          the name of the environment entry
   * @param description
   *          the description of the entry
   * @param xmlEntry
   *          xml declaration of the object in the environment
   * @param enabled
   *          is this entry enabled or disabled.
   */
  public EnvEntry(final String name, final String description, final String xmlEntry, final boolean enabled) {
    Misc.checkArgsNotNull(name, description, xmlEntry, enabled);
    this.name = name;
    this.description = description;
    this.xmlEntry = xmlEntry;
    this.enabled = enabled;
  }

  /**
   * Generates XML output to write in the environment.xml file.
   * 
   * @param indent
   *          default indentation for the generated xml block
   * @return generated xml block for this environment entry
   */
  public String getEnvXml(final String indent) {
    final StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append(indent).append("<!-- Description: ").append(description).append(" -->")
        .append(Misc.LINE_SEPARATOR);
    if (!enabled) {
      stringBuilder.append(indent).append("<!--").append(Misc.LINE_SEPARATOR);
    }
    stringBuilder.append(Misc.prefixAllLines(getXmlEntry(), indent)).append(Misc.LINE_SEPARATOR);
    if (!enabled) {
      stringBuilder.append(indent).append(" -->").append(Misc.LINE_SEPARATOR);
    }
    return stringBuilder.toString();
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(final boolean enabled) {
    this.enabled = enabled;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public String getXmlEntry() {
    return xmlEntry;
  }

}
