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

import java.util.List;

import org.ow2.bonita.util.Misc;

/**
 * Generates an environment entry for a query API list.
 *
 * @author Guillaume Porcher
 *
 */
public class QueryApiEnvEntry extends EnvEntry {

  private final List<EnvEntry> entries;

  /**
   * Default constructor
   * @param name name of the environment entry
   * @param description description of the entry
   * @param entries list of the queriers in the queryApi
   * @param enabled is this entry enabled or disabled.
   */
  public QueryApiEnvEntry(final String name, final String description, final List<EnvEntry> entries, final boolean enabled) {
    super(name, description, "", enabled);
    this.entries = entries;
  }

  /**
   * Generate Xml declaration of the chain
   */
  @Override
  public String getXmlEntry() {
    final StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append("<queryApi name='").append(getName()).append("'>").append(Misc.LINE_SEPARATOR);
    for (final EnvEntry entry : this.entries) {
      stringBuilder.append(entry.getEnvXml(EnvGenerator.INDENT));
    }
    stringBuilder.append("</queryApi>").append(Misc.LINE_SEPARATOR);
    return stringBuilder.toString();
  }
}
