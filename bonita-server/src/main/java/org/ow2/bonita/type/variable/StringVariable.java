/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.ow2.bonita.type.variable;

import org.ow2.bonita.type.Variable;

public class StringVariable extends Variable {
  
  private static final long serialVersionUID = 1L;
  
  private static final int VARCHAR_MAX = 255;
  
  protected String string = null;
  protected String text = null;

  public boolean isStorable(Object value) {
    if (value==null) return true;
    return (String.class==value.getClass());
  }

  public Object getObject() {
    if (string != null) {
      return string;
    }
    return text;
  }

  public void setObject(Object value) {
    this.string = null;
    this.text = null;
    if (value != null) {
      String str = (String)value; 
      if (str.length() <= VARCHAR_MAX) {
        this.string = str;
      } else {
        this.text = str;
      }
    }
  }
}
