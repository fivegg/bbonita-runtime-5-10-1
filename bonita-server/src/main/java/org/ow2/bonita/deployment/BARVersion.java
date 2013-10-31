/**
 * Copyright (C) 2010 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.ow2.bonita.deployment;

/**
 * 
 * @author Matthieu Chaffotte
 *
 */
public class BARVersion implements Comparable<String> {

  private final String formattedVersion;

  public BARVersion(final String version) {
    formattedVersion = format(version);
  }

  @Override
  public int compareTo(final String version) {
    final String otherFormatedVersion = format(version);
    return formattedVersion.compareTo(otherFormatedVersion);
  }

  private String format(final String version) {
    if (version == null) {
      throw new IllegalArgumentException("The version cannot be null");
    }
    String trimVersion = version.trim();
    if (version.endsWith(".0")) {
      trimVersion = version.substring(0, version.length() - 2);
    }
    final String[] split = trimVersion.split("\\.");
    final StringBuilder builder = new StringBuilder();
    for (String s : split) {
        builder.append(String.format("%4s", s));
    }
    return builder.toString();
  }

}
