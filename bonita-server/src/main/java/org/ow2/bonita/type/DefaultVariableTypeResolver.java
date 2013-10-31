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
package org.ow2.bonita.type;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Tom Baeyens
 */
public class DefaultVariableTypeResolver implements VariableTypeResolver,
    Serializable {

  private static final long serialVersionUID = 1L;

  List<TypeMapping> typeMappings;

  public Type findTypeByMatch(String key, Object value) {
    if (typeMappings != null) {
      for (TypeMapping typeMapping : typeMappings) {
        if (typeMapping.matches(key, value)) {
          return typeMapping.getType();
        }
      }
    }

    return null;
  }

  public Type findTypeByName(String typeName) {
    if ((typeMappings != null) && (typeName != null)) {
      for (TypeMapping typeMapping : typeMappings) {
        Type type = typeMapping.getType();
        if (typeName.equals(type.getName())) {
          return type;
        }
      }
    }
    return null;
  }

  public void addTypeMapping(TypeMapping typeMapping) {
    if (typeMappings == null) {
      typeMappings = new ArrayList<TypeMapping>();
    }
    typeMappings.add(typeMapping);
  }
}
