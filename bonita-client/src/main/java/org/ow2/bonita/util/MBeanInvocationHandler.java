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

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

/**
 * @author Marc Blachon, Guillaume Porcher, Charles Souillard, Miguel Valdes, Pierre Vigneras
 */
public class MBeanInvocationHandler implements InvocationHandler {
  private final MBeanServerConnection mbsc;
  private final ObjectName mbeanObjectName;

  public MBeanInvocationHandler(String jmxServiceUrl, String jmxObjectName)
    throws IOException, MalformedObjectNameException {

    final JMXServiceURL url = new JMXServiceURL(jmxServiceUrl);
    final JMXConnector jmxc = JMXConnectorFactory.connect(url, null);
    this.mbsc = jmxc.getMBeanServerConnection();
    this.mbeanObjectName = ObjectName.getInstance(jmxObjectName);
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object,
   *      java.lang.reflect.Method, java.lang.Object[])
   */
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

    final String[] signature = (args == null) ? null : new String[args.length];
    if (args != null) {
      final Class< ? >[] types = method.getParameterTypes();
      for (int i = 0; i < signature.length; i++) {
        signature[i] = types[i].getName();
      }
    }
    return mbsc.invoke(mbeanObjectName, method.getName(), args, signature);
  }
}
