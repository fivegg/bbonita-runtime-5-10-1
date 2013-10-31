/**
 * Copyright (C) 2012 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ow2.bonita.services.impl;

import org.ow2.bonita.services.DocumentContent;
import org.ow2.bonita.type.lob.Lob;

/**
 * @author Elias Ricken de Medeiros
 */
public class DocumentContentImpl extends Lob implements DocumentContent {

  private static final long serialVersionUID = -6333355169480607546L;

  public DocumentContentImpl() {
    super();
  }

  public DocumentContentImpl(final byte[] content) {
    super(content);
  }

  @Override
  public byte[] getContent() {
    return extractBytes();
  }

  @Override
  public String getId() {
    return String.valueOf(dbid);
  }

}
