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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.ow2.bonita.util.SimpleCallbackHandler;

/**
 * @author Charles Souillard
 */
public final class Main {

  private Main() { }
  private static LoginContext loginContext;
  private static final Map<String, String> parameters = new HashMap<String, String>();

  public static void main(String[] args) throws Exception {

    parseArgs(args);
    StressPerfTest test = new StressPerfTest(
        getParameter(String.class, "algo"),
        getParameter(Long.class, "threadNb"),
        getParameter(Boolean.class, "printFinished"),
        getParameter(Boolean.class, "printLaunched"),
        getParameter(Long.class, "timeBetweenVerifications"),
        getParameter(Long.class, "timeBetweenPrints"),
        getParameter(Long.class, "thinkTime"),
        getParameter(Long.class, "loadTime"),
        getParameter(Long.class, "warmupTime"), 
        getPerfTestCases());
    login();
    test.clean();
    test.deployTests();
    test.launchTests();
    test.undeployTests();
    logout();
  }

  private static void login() throws LoginException {
    System.setProperty("java.security.auth.login.config", StressPerfTest.class.getResource("jaas.cfg").getFile());
    loginContext = new LoginContext("BonitaAuth", new SimpleCallbackHandler("john", "bpm"));
    loginContext.login();
    loginContext.logout();
    loginContext = new LoginContext("BonitaStore", new SimpleCallbackHandler("john", "bpm"));
    loginContext.login();
  }
  private static void logout() throws LoginException {
    loginContext.logout();
  }

  @SuppressWarnings("unchecked")
  private static <T> T getParameter(Class<T> clazz, final String key) {
    final String value = parameters.get(key);
    try {
      return (T) new Long(value); 
    } catch (NumberFormatException e) {
      if ("true".equals(value) || "false".equals(value)) {
        return (T) Boolean.valueOf(value);
      }
      return (T) value;   
    }
  }
  private static Map<String, String> parseArgs(final String[] args) throws PerfException {
    for (String arg : args) {
      final String[] tmp = arg.split("=");
      parameters.put(tmp[0], tmp[1]);
    }
    return parameters;
  }

  private static List<PerfTestCase> getPerfTestCases() throws Exception {
    final List<PerfTestCase> tests = new ArrayList<PerfTestCase>();
    final String testsToRunClasses = parameters.get("testsToRunClasses");
    for (String className : testsToRunClasses.split(",")) {
      PerfTestCase perfTestCase = null;
      final Class< ? > clazz = Class.forName(className);
      if (!PerfTestCase.class.isAssignableFrom(clazz)) {
        throw new PerfException("Class : " + className + " is not an instanceof Perftestcase.");
      }
      perfTestCase = (PerfTestCase) clazz.newInstance();
      tests.add(perfTestCase);
    }
    return tests;
  }

}
