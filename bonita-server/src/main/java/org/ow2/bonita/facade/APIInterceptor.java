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
 * 
 * Modifed by Matthieu Chaffotte - BonitaSoft S.A.
 **/
package org.ow2.bonita.facade;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.MalformedObjectNameException;

import org.ow2.bonita.env.Authentication;
import org.ow2.bonita.env.Environment;
import org.ow2.bonita.env.EnvironmentFactory;
import org.ow2.bonita.env.GlobalEnvironmentFactory;
import org.ow2.bonita.env.InvalidEnvironmentException;
import org.ow2.bonita.facade.exception.BonitaInternalException;
import org.ow2.bonita.facade.exception.BonitaWrapperException;
import org.ow2.bonita.facade.monitoring.model.Jvm;
import org.ow2.bonita.facade.monitoring.model.JvmMBean;
import org.ow2.bonita.facade.monitoring.model.MBeanStartException;
import org.ow2.bonita.facade.monitoring.model.MBeanStopException;
import org.ow2.bonita.identity.auth.APIMethodsSecurity;
import org.ow2.bonita.identity.auth.DomainOwner;
import org.ow2.bonita.identity.auth.UserOwner;
import org.ow2.bonita.services.CommandService;
import org.ow2.bonita.util.BonitaConstants;
import org.ow2.bonita.util.BonitaException;
import org.ow2.bonita.util.BonitaRuntimeException;
import org.ow2.bonita.util.Command;
import org.ow2.bonita.util.EnvTool;
import org.ow2.bonita.util.ExceptionManager;

public class APIInterceptor implements InvocationHandler, Serializable {

  private static final long serialVersionUID = 1L;

  protected static final Logger LOG = Logger.getLogger(APIInterceptor.class.getName());

  private final Object api;

  static private JvmMBean jvmMBean;
  static private Object lockObj = "this is a lock";

  public APIInterceptor(final Object api) {
    this.api = api;
  }

  class APIInterceptorCommand implements Command<Object> {

    private static final long serialVersionUID = 6293853276365124717L;
    private final transient Method m;
    private final Object[] args;
    private final boolean isSecuritySet;

    public APIInterceptorCommand(final Method m, final Object[] args, final boolean isSecuritySet) {
      this.args = args;
      this.m = m;
      this.isSecuritySet = isSecuritySet;
      if (LOG.isLoggable(Level.FINE)) {
        final StringBuffer sb = new StringBuffer();
        sb.append("Creating APIInterceptorCommand: " + this + ". Method: " + m + ", isSecuritySet:" + isSecuritySet);
        if (args != null) {
          for (final Object arg : args) {
            sb.append(" - Arg: " + arg);
          }
        } else {
          sb.append(" Args: null.");
        }
        LOG.fine(sb.toString());
      }
    }

    @Override
    public Object execute(final Environment env) throws Exception {
      // initialize event executor
      EnvTool.getEventExecutor();

      ensureMBeanIsRegistered();

      // no identity check should be performed on some methods as they are required for authentication
      if (!this.isSecuritySet && APIMethodsSecurity.isSecuredMethod(m)) {
        String userId = null;
        // if the user defined a security context, use it. Else, use the default UserOwner mechanism
        try {
          userId = EnvTool.getBonitaSecurityContext().getUser();
        } catch (final Throwable t) {
          userId = UserOwner.getUser();
        }

        Authentication.setUserId(userId);
      }
      try {
        if (LOG.isLoggable(Level.FINE)) {
          final StringBuffer sb = new StringBuffer();
          sb.append("Executing APIInterceptorCommand: " + this + " on api: " + APIInterceptor.this.api + ". Env: "
              + env);
          LOG.fine(sb.toString());
        }
        return this.m.invoke(APIInterceptor.this.api, this.args);
      } catch (final InvocationTargetException e) {
        final Throwable t = e.getCause();
        if (t instanceof Exception) {
          throw (Exception) t;
        } else if (t instanceof Error) {
          throw (Error) t;
        } else {
          final String message = ExceptionManager.getInstance().getFullMessage("bai_APII_1", t);
          throw new BonitaInternalException(message, t);
        }
      }
    }

    private void ensureMBeanIsRegistered() {
      synchronized (lockObj) {
        if (jvmMBean != null) {
          return;
        }
        try {
          jvmMBean = new Jvm();
          jvmMBean.start();
          registerShutdownHookToRemoveMBean(jvmMBean);
        } catch (final MalformedObjectNameException e) {
          LOG.warning("jvmMBean can not been started:" + e.getMessage());
        } catch (final NullPointerException e) {
          LOG.warning("jvmMBean can not been started:" + e.getMessage());
          e.printStackTrace();
        } catch (final MBeanStartException e) {
          LOG.warning("jvmMBean can not been started:" + e.getMessage());
          e.printStackTrace();
        }
      }

    }

    private void registerShutdownHookToRemoveMBean(final JvmMBean jvmMBean) {
      Runtime.getRuntime().addShutdownHook(new Thread() {
        @Override
        public void run() {
          super.run();
          try {
            if (LOG.isLoggable(Level.INFO)) {
              LOG.log(Level.INFO, "Unregistering MBean as the JVM is going to shutdown...");
            }
            jvmMBean.stop();
            if (LOG.isLoggable(Level.INFO)) {
              LOG.log(Level.INFO, "MBean unregistered.");
            }
          } catch (final MBeanStopException e) {
            if (LOG.isLoggable(Level.WARNING)) {
              LOG.log(Level.WARNING, "Unable to unregister MBean.", e);
            }

          }
        }
      });

    }
  }

  @Override
  public Object invoke(final Object proxy, final Method method, final Object[] args) throws BonitaException {
    BonitaConstants.getBonitaHomeFolder();
    try {
      // If already in environment: use the same environment to execute the
      // command directly
      if (Environment.getCurrent() != null) {
        return new APIInterceptorCommand(method, args, true).execute(Environment.getCurrent());
      }
      // If no environment: use directly the command Service with interceptors
      final EnvironmentFactory envFactory = GlobalEnvironmentFactory.getEnvironmentFactory(DomainOwner.getDomain());
      return envFactory.get(CommandService.class).execute(new APIInterceptorCommand(method, args, false));
    } catch (final BonitaRuntimeException e) {
      final Throwable cause = e.getCause();
      if (cause instanceof BonitaException) {
        throw (BonitaException) cause;
      } else if (e instanceof InvalidEnvironmentException) {
        throw e;
      } else if (e instanceof BonitaWrapperException) {
        throw (RuntimeException) e.getCause();
      }
      throw e;
    } catch (final BonitaException e) {
      throw e;
    } catch (final RuntimeException e) {
      throw BonitaInternalException.build(e);
    } catch (final Exception t) {
      final String message = ExceptionManager.getInstance().getFullMessage("bai_APII_2", t);
      throw new BonitaInternalException(message, t);
    }
  }

}
