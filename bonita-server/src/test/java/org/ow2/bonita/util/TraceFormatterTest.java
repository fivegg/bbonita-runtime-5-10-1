/**
 * Copyright (C) 2007  Bull S. A. S.
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
package org.ow2.bonita.util;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.TestCase;

/**
 * @author Pierre Vigneras
 */
public class TraceFormatterTest extends TestCase {

  public void testLog() {
    System.setProperty(TraceFormatter.ALIAS_PROPERTY_KEY, "org~O,ow2~2,bonita.util~Util");
    final Logger log = Logger.getLogger(TraceFormatter.class.getName());
    log.setLevel(Level.ALL);
    final ConsoleHandler handler = new ConsoleHandler();
    handler.setLevel(Level.ALL);
    handler.setFormatter(new TraceFormatter());
    log.addHandler(handler);
    log.setUseParentHandlers(false);
    log.config("Config");
    log.entering(TraceFormatterTest.class.getName(), "testLog", new String[] {
        "parameter1", "parameter2"
    });
    log.exiting(TraceFormatterTest.class.getName(), "testLog", "returned value");
    log.finest("Finest message");
    log.finest("Finer message");
    log.fine("Fine message");
    log.info("Info message");
    log.warning("Warning message");
    log.severe("Severe message");
    log.throwing(TraceFormatterTest.class.getName(), "testLog", new Exception("Exception Message"));
    log.log(Level.SEVERE, "Severe message with exception", new Throwable("A message"));
  }
}
