/**
 * Copyright (C) 2006  Bull S. A. S.
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
package org.ow2.bonita.perf;

/**
 * @author Charles Souillard
 */
public final class Util {

  /** 1 hour = 3600000 ms. */
  public static final long SECOND_IN_MILLIS = 1000;

  /** 1 minute = 60000 ms. */
  public static final long MINUTE_IN_MILLIS = 60000;

  /** 1 second = 1000 ms. */
  public static final long HOUR_IN_MILLIS = 3600000;

  /**
   * private constructor.
   */
  private Util() {
  }

  /**
   * Return a string representation of a duration in millis.
   * 
   * @param durationInMillis
   *            duration to transform.
   * @return a string representation of a duration in millis.
   */
  public static String getDuration(long durationInMillis) {
    long h = durationInMillis / HOUR_IN_MILLIS;
    durationInMillis -= HOUR_IN_MILLIS * h;

    long m = durationInMillis / MINUTE_IN_MILLIS;
    durationInMillis -= MINUTE_IN_MILLIS * m;

    long s = durationInMillis / SECOND_IN_MILLIS;
    durationInMillis -= SECOND_IN_MILLIS * s;

    long ms = durationInMillis;

    String st = "";
    if (h != 0) {
      st += h + "h";
    }
    if (m != 0) {
      st += m + "m";
    }
    if (s != 0) {
      st += s + "s";
    }
    if (ms != 0) {
      st += ms + "ms";
    }
    return st;
  }

  public static long parseTime(String s) {
    long result = 0;
    try {
      result = new Long(s).longValue();
    } catch (NumberFormatException e) {
      // it is not a number of milisecond so the format is
      // [xxh][xxm][xxs][xxms]
      int msIndex = s.indexOf("ms");
      int sIndex = s.indexOf('s');
      if (sIndex == msIndex + 1) {
        sIndex = -1;
      }
      int mIndex = s.indexOf('m');
      if (mIndex == msIndex) {
        mIndex = -1;
      }
      int hIndex = s.indexOf('h');

      int start = 0;

      if (hIndex != -1) {
        String h = s.substring(start, hIndex);
        result += HOUR_IN_MILLIS * new Long(h).longValue();
        start = hIndex + 1;
      }
      if (mIndex != -1) {
        String m = s.substring(start, mIndex);
        result += MINUTE_IN_MILLIS * new Long(m).longValue();
        start = mIndex + 1;
      }
      if (sIndex != -1) {
        String sec = s.substring(start, sIndex);
        result += SECOND_IN_MILLIS * new Long(sec).longValue();
        start = sIndex + 1;
      }
      if (msIndex != -1) {
        String ms = s.substring(start, msIndex);
        result += new Long(ms).longValue();
        start = msIndex + 1;
      }
    }
    return result;
  }

}
