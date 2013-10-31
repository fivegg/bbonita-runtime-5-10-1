/*********************************************************************
 *       This file is part of the Mandala library source code.       *
 *                   Copyright (C) 2002                              *
 *                                                                   *
 *       Authors: eipi - eipiequalsnoone@userOps.sf.net               *
 *                                                                   *
 * This library is free software; you can redistribute it and/or     *
 * modify it under the terms of the GNU Lesser General Public        *
 * License as published by the Free Software Foundation; either      *
 * version 2.1 of the License, or (at your option) any later version.*
 *                                                                   *
 * This library is distributed in the hope that it will be useful,   *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU *
 * Lesser General Public License for more details.                   *
 *                                                                   *
 * You should have received a copy of the GNU Lesser General Public  *
 * License along with this library; if not, write to                 *
 * the Free Software Foundation, Inc., 59 Temple Place, Suite 330,   *
 * Boston, MA  02111-1307  USA                                       *
 *********************************************************************/
package org.ow2.bonita.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

/**
 * <p>
 * Custom implementation of the {@link SimpleFormatter} class.
 * <p>
 * 
 * <p>
 * This implementation produce the following pattern:<br>
 * <center><code>L DATE thr#Id Logger Class.Method(): MSG</code></center>
 * 
 * where:
 * <ul>
 * <li>L is a letter code for log level:
 * <ul>
 * <li>C for Level.CONFIG;</li>
 * <li>F for Level.FINE, Level.FINER, Level.FINEST</li>
 * <li>I for Level.INFO</li>
 * <li>W for Level.WARNING</li>
 * <li>S for Level.SEVERE</li>
 * <li>E for log.entering() or log.exiting() log type</li>
 * <li>T for log.throwing() log type</li>
 * </ul>
 * <li>Id is the thread id of the caller</li>
 * </ul>
 * 
 * @author <a href="mailto:eipiequalsnoone@userOps.sf.net">eipi</a>
 * @version $Revision: 1.4 $
 * @since 1.0
 */
public class TraceFormatter extends SimpleFormatter {

  public static final String UNAVAILABLE_SYMBOL = "-";
  public static final String LEGEND_MSG = TraceFormatter.class.getName() + " uses the following format:"
      + Misc.LINE_SEPARATOR + "L DATE thr#Id Logger Class.Method(): MSG" + Misc.LINE_SEPARATOR + "Where L can be: "
      + Misc.LINE_SEPARATOR + "\tC for Level.CONFIG," + Misc.LINE_SEPARATOR
      + "\tF for Level.FINE, Level.FINER and Level.FINEST" + Misc.LINE_SEPARATOR + "\tI for Level.INFO"
      + Misc.LINE_SEPARATOR + "\tW for Level.WARNING" + Misc.LINE_SEPARATOR + "\tS for Level.SEVERE"
      + Misc.LINE_SEPARATOR + "\tE for log.entering() or log.exiting() log type" + Misc.LINE_SEPARATOR
      + "\tT for log.throwing() log type" + Misc.LINE_SEPARATOR + "\t" + TraceFormatter.UNAVAILABLE_SYMBOL
      + " means that the information is unavailable." + Misc.LINE_SEPARATOR;
  public static final String ALIAS_HELP = "Alias can be provided to shorten the output with the property "
      + TraceFormatter.ALIAS_PROPERTY_KEY + " set to something similar to: string1~alias1,string2~alias2,...";

  private static final DateFormat CONCISE_DATE_FORMATTER = new SimpleDateFormat("yy-MM-dd HH:mm:ss.SSS");
  public static final String ALIAS_PROPERTY_KEY = TraceFormatter.class.getName() + ".alias";
  private static final String PREFIX = "   ";

  private final Map<String, String> alias;
  private boolean firstTime = true;

  public TraceFormatter() {
    super();
    alias = new HashMap<String, String>();
    String aliasConfig = LogManager.getLogManager().getProperty(TraceFormatter.ALIAS_PROPERTY_KEY);
    if (aliasConfig == null) {
      aliasConfig = System.getProperty(TraceFormatter.ALIAS_PROPERTY_KEY);
    }
    if (aliasConfig != null) {
      final StringTokenizer configTokenizer = new StringTokenizer(aliasConfig, ",");
      while (configTokenizer.hasMoreTokens()) {
        final String alia = configTokenizer.nextToken();
        final StringTokenizer aliaTokenizer = new StringTokenizer(alia, "~");
        if (aliaTokenizer.countTokens() != 2) {
          final String message = ExceptionManager.getInstance().getFullMessage("buc_TF_1", aliasConfig);
          throw new IllegalArgumentException(message);
        }
        alias.put(aliaTokenizer.nextToken(), aliaTokenizer.nextToken());
      }
    }
  }

  @Override
  public String format(final LogRecord record) {
    final StringBuilder sb = new StringBuilder(160); // Two 80 colons lines.
    if (firstTime) {
      firstTime = false;
      sb.append(TraceFormatter.LEGEND_MSG);
      if (alias.isEmpty()) {
        sb.append(TraceFormatter.ALIAS_HELP);
      } else {
        sb.append("Using alias: " + alias);
      }
      sb.append(Misc.LINE_SEPARATOR + Misc.LINE_SEPARATOR);
    }

    final String msg = record.getMessage();
    final boolean entry = msg.startsWith("ENTRY");
    final boolean exit = msg.startsWith("RETURN");
    final boolean throwing = msg.startsWith("THROW");

    if (entry || exit) {
      sb.append('E');
    } else if (throwing) {
      sb.append('T');
    } else {
      sb.append(record.getLevel().getName().charAt(0));
    }
    sb.append(' ');
    // Formatter are not thread safe. See Sun Bug #6231579 and Sun Bug
    // #6178997.
    String tmp;
    synchronized (TraceFormatter.CONCISE_DATE_FORMATTER) {
      tmp = TraceFormatter.CONCISE_DATE_FORMATTER.format(record.getMillis());
    }
    sb.append(tmp);
    sb.append(" thr#");
    sb.append(record.getThreadID());
    sb.append(' ');
    tmp = record.getLoggerName();
    final String logger = tmp == null ? TraceFormatter.UNAVAILABLE_SYMBOL : replaceWithAlias(tmp);
    sb.append(logger);
    sb.append(' ');
    tmp = record.getSourceClassName();
    final String className = tmp == null ? TraceFormatter.UNAVAILABLE_SYMBOL : replaceWithAlias(tmp);
    sb.append(className);
    sb.append('.');
    tmp = record.getSourceMethodName();
    sb.append(tmp == null ? TraceFormatter.UNAVAILABLE_SYMBOL : tmp);
    sb.append('(');

    if (entry) {
      final Object[] params = record.getParameters();
      if (params != null) {
        final int last = params.length - 1;
        for (int i = 0; i < last; i++) {
          sb.append(params[i]);
          sb.append(", ");
        }
        sb.append(params[last]);
      }
    }
    sb.append(')');
    final Throwable throwable = record.getThrown();
    if (exit || throwing) {
      sb.append(" -> ");
      if (exit) {
        final Object returnedValue = record.getParameters()[0];
        sb.append(returnedValue == null ? TraceFormatter.UNAVAILABLE_SYMBOL : returnedValue);
      } else {
        sb.append(throwable == null ? TraceFormatter.UNAVAILABLE_SYMBOL : throwable);
      }
    } else if (!entry) {
      sb.append(": ");
      sb.append(msg);
    }
    sb.append(Misc.LINE_SEPARATOR);
    if (throwable != null && !throwing) {
      sb.append(TraceFormatter.PREFIX);
      sb.append(throwable);
      sb.append(Misc.prefixAllLines(Misc.getStackTraceFrom(throwable), TraceFormatter.PREFIX));
      sb.append(Misc.LINE_SEPARATOR);
    }
    return new String(sb);
  }

  private String replaceWithAlias(final String src) {
    if (src == null) {
      return src;
    }
    final StringBuilder dst = new StringBuilder(src);
    for (final String key : alias.keySet()) {
      final int index = dst.indexOf(key);
      if (index != -1) {
        dst.delete(index, index + key.length());
        dst.insert(index, alias.get(key));
      }
    }
    return new String(dst);
  }
}
