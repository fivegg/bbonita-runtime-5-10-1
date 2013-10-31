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
package org.ow2.bonita.search;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * 
 * @author Matthieu Chaffotte
 *
 */
public class SearchDate {

  private static ThreadLocal<SimpleDateFormat> LUCENE_FORMAT = new ThreadLocal<SimpleDateFormat>() {

    protected synchronized SimpleDateFormat initialValue() {
      SimpleDateFormat gmtTime = new SimpleDateFormat("yyyyMMddHHmmssSSS");
      gmtTime.setTimeZone(TimeZone.getTimeZone("GMT"));
      return gmtTime;
    }

  };

  private String luceneDate;

  public SearchDate(Date date) {
    SimpleDateFormat luceneDateFormat = SearchDate.LUCENE_FORMAT.get();
    luceneDate = luceneDateFormat.format(date);
  }

  @Override
  public String toString() {
    return luceneDate;
  }

}
