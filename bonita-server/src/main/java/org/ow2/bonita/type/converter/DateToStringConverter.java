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
 * 
 * Modified by Matthieu Chaffotte - BonitaSoft S.A.
 */
package org.ow2.bonita.type.converter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.ow2.bonita.type.Converter;
import org.ow2.bonita.util.BonitaRuntimeException;
import org.ow2.bonita.util.ExceptionManager;

/**
 * @author Tom Baeyens
 */
public class DateToStringConverter implements Converter {

  private static final long serialVersionUID = 1L;
  private static final String FORMAT = "yyyy-MM-dd HH:mm:ss,SSS";

  private static ThreadLocal<SimpleDateFormat> dateFormat = new ThreadLocal<SimpleDateFormat>() {

    protected synchronized SimpleDateFormat initialValue() {
      return new SimpleDateFormat(FORMAT);
    }

  };

  public Object convert(Object o) {
    return dateFormat.get().format((Date) o);
  }

  public Object revert(Object o) {
    try {
      return dateFormat.get().parseObject((String) o);
    } catch (ParseException e) {
    	String message = ExceptionManager.getInstance().getFullMessage("bp_DTSC_1", o);
      throw new BonitaRuntimeException(message, e);
    }
  }

  public boolean supports(Object value) {
    return ((value != null) && (value instanceof Date));
  }

}
