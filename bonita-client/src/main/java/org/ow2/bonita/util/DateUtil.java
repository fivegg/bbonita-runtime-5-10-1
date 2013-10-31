/**
 * Copyright (C) 2010-2012 BonitaSoft S.A.
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
package org.ow2.bonita.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public final class DateUtil {

  private static ThreadLocal<SimpleDateFormat> DATE_FORMAT_1 = new ThreadLocal<SimpleDateFormat>() {

    @Override
    protected synchronized SimpleDateFormat initialValue() {
      return new SimpleDateFormat("yyyy/MM/dd/HH/mm/ss/SSS");
    }

  };

  private static ThreadLocal<SimpleDateFormat> DATE_FORMAT_2 = new ThreadLocal<SimpleDateFormat>() {

    @Override
    protected synchronized SimpleDateFormat initialValue() {
      return new SimpleDateFormat("yyyy/MM/dd/HH/mm/ss");
    }

  };

  // ISO-8601 requires the T between date and time.
  private static ThreadLocal<SimpleDateFormat> ISO_8601_FORMAT = new ThreadLocal<SimpleDateFormat>() {

    @Override
    protected synchronized SimpleDateFormat initialValue() {
      return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    }

  };

  private DateUtil() {
  }

  public static Date parseDate(final String formattedDate) {
    Date result;
    try {
      result = DateUtil.DATE_FORMAT_1.get().parse(formattedDate);
    } catch (final ParseException e1) {
      try {
        result = DateUtil.DATE_FORMAT_2.get().parse(formattedDate);
      } catch (final ParseException e2) {
        try {
          result = DateUtil.ISO_8601_FORMAT.get().parse(formattedDate);
        } catch (final ParseException e3) {
          final String message = ExceptionManager.getInstance().getFullMessage("bu_DU_1", formattedDate,
              DateUtil.DATE_FORMAT_2.get().toPattern(), DateUtil.DATE_FORMAT_1.get().toPattern(),
              DateUtil.ISO_8601_FORMAT.get().toPattern());
          throw new IllegalArgumentException(message);
        }
      }
    }
    return result;
  }

  public static String format(final Date date) {
    return DateUtil.ISO_8601_FORMAT.get().format(date);
  }

  public static Date backTo(final Date date, final int dayNumber) {
    final Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    calendar.add(Calendar.DATE, -dayNumber);
    return calendar.getTime();
  }

  public static Date getNextDay(final Date date) {
    final Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    calendar.add(Calendar.DATE, 1);
    return calendar.getTime();
  }

  public static Date getBeginningOfTheDay(final Date date) {
    final Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    calendar.set(Calendar.MILLISECOND, 0);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MINUTE, 0);
    calendar.set(Calendar.HOUR_OF_DAY, 0);
    return calendar.getTime();
  }

}
