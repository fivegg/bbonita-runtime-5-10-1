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
package org.ow2.bonita.runtime.hibernate;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.hibernate.HibernateException;
import org.hibernate.type.ImmutableType;
import org.hibernate.usertype.ParameterizedType;
import org.ow2.bonita.type.Converter;
import org.ow2.bonita.util.BonitaRuntimeException;
import org.ow2.bonita.util.ExceptionManager;
import org.ow2.bonita.util.ReflectUtil;

/**
 * @author Tom Baeyens
 */
public class ConverterType extends ImmutableType implements ParameterizedType {

  private static final long serialVersionUID = 1L;
  private Map<Class< ? >, String> converterNames = null;
  private Map<String, Converter> converters = null;

  public Object fromStringValue(String arg0) throws HibernateException {
    return null;
  }

  public Object get(ResultSet resultSet, String name) throws HibernateException, SQLException {
    String converterName = resultSet.getString(name);
    return converters.get(converterName);
  }

  public void set(PreparedStatement stmt, Object value, int index) throws HibernateException, SQLException {
    String converterName = value != null ? converterNames
        .get(value.getClass()) : null;
    stmt.setString(index, converterName);
  }

  public int sqlType() {
    return Types.VARCHAR;
  }

  public String toString(Object arg0) throws HibernateException {
    return null;
  }

  public String getName() {
    return "converter";
  }

  public Class< ? > getReturnedClass() {
    return Converter.class;
  }

  public void setParameterValues(Properties properties) {
    converterNames = new HashMap<Class< ? >, String>();
    converters = new HashMap<String, Converter>();

    for (Object key : properties.keySet()) {
      String converterClassName = (String) key;
      try {
        ClassLoader classLoader = Thread.currentThread()
            .getContextClassLoader();
        Class< ? > converterClass = ReflectUtil.loadClass(classLoader,
            converterClassName);

        String converterName = properties.getProperty(converterClassName);
        converterNames.put(converterClass, converterName);
        Converter converter = (Converter) converterClass.newInstance();
        converters.put(converterName, converter);
      } catch (Exception e) {
      	String message = ExceptionManager.getInstance().getFullMessage(
      			"bp_CT_1", converterClassName);
        throw new BonitaRuntimeException(message, e);
      }
    }
  }
}
