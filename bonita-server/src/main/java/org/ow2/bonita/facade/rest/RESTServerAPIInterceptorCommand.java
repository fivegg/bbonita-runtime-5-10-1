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
package org.ow2.bonita.facade.rest;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ow2.bonita.env.Authentication;
import org.ow2.bonita.env.Environment;
import org.ow2.bonita.facade.APIAccessor;
import org.ow2.bonita.facade.QueryDefinitionAPI;
import org.ow2.bonita.facade.def.majorElement.DataFieldDefinition;
import org.ow2.bonita.facade.exception.ActivityDefNotFoundException;
import org.ow2.bonita.facade.exception.BonitaInternalException;
import org.ow2.bonita.facade.exception.DataFieldNotFoundException;
import org.ow2.bonita.facade.exception.ProcessNotFoundException;
import org.ow2.bonita.facade.impl.StandardAPIAccessorImpl;
import org.ow2.bonita.facade.interceptor.InterceptorUtil;
import org.ow2.bonita.facade.rest.wrapper.RESTCommand;
import org.ow2.bonita.facade.rest.wrapper.RESTMap;
import org.ow2.bonita.facade.rest.wrapper.RESTObject;
import org.ow2.bonita.facade.runtime.ActivityInstance;
import org.ow2.bonita.facade.runtime.ProcessInstance;
import org.ow2.bonita.facade.uuid.ActivityInstanceUUID;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.identity.auth.APIMethodsSecurity;
import org.ow2.bonita.identity.auth.UserOwner;
import org.ow2.bonita.util.Command;
import org.ow2.bonita.util.EnvTool;
import org.ow2.bonita.util.ExceptionManager;
import org.ow2.bonita.util.Misc;
import org.ow2.bonita.util.xml.XStreamUtil;

import com.thoughtworks.xstream.XStream;

/**
 * @author Nicolas Chabanoles, Matthieu Chaffotte
 * 
 */
public class RESTServerAPIInterceptorCommand implements Command<Object> {

  private static final long serialVersionUID = 7925435794747108162L;

  protected static final Logger LOG = Logger.getLogger(RESTServerAPIInterceptorCommand.class.getName());

  private static final String SET_ACTIVITY_INSTANCE_VARIABLE = "setActivityInstanceVariable";
  private static final String SET_PROCESS_INSTANCE_VARIABLE = "setProcessInstanceVariable";
  private static final String SET_VARIABLE = "setVariable";

  private final transient Method m;
  private final Object[] args;
  private final Object api;

  public RESTServerAPIInterceptorCommand(final Method m, final Object[] args, final Object api) {
    this.args = args;
    this.m = m;
    this.api = api;
    if (LOG.isLoggable(Level.FINE)) {
      logCommandCreation(m, args);
    }
  }

  private void logCommandCreation(final Method m, final Object[] args) {
    final StringBuffer sb = new StringBuffer();
    sb.append("Creating APIInterceptorCommand: " + this + ". Method: " + m);
    if (args != null) {
      for (final Object arg : args) {
        sb.append(" - Arg: " + arg);
      }
    } else {
      sb.append(" Args: null.");
    }
    LOG.fine(sb.toString());
  }

  @Override
  public Object execute(final Environment environment) throws Exception {
    try {
      handleSecurity();
      return doInvocation();
    } catch (final InvocationTargetException e) {
      final Throwable invocationExceptionCause = e.getCause();
      if (invocationExceptionCause instanceof RemoteException) {
        final RemoteException remoteException = (RemoteException) invocationExceptionCause;
        final Throwable remoteCause = getRemoteCause(remoteException);
        InterceptorUtil.manageInvokeExceptionCause(m, remoteCause);
      } else {
        if (invocationExceptionCause instanceof Exception) {
          throw (Exception) invocationExceptionCause;
        }

      }
      final String message = ExceptionManager.getInstance().getFullMessage("baa_CAPII_1", e);
      throw new BonitaInternalException(message, e);
    }
  }

  private Object doInvocation() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException,
      IOException, ClassNotFoundException {
    preprocessInputParametersDependingOnMethodName(args, m);

    Object ret = m.invoke(api, args);
    // workaround : to be sure that GenericObjectProvider will be used
    // even if the object is instance of a primitive type
    if (isAmbigousReturnType(m, ret)) {
      ret = new RESTObject((Serializable) ret);
    }
    return ret;

  }

  private void preprocessInputParametersDependingOnMethodName(final Object[] args, final Method m) {

    // get current ClassLoader
    final ClassLoader baseClassLoader = Thread.currentThread().getContextClassLoader();
    try {
      final ClassLoader localClassLoader = null;

      if (isSetVariableMethod(args, m)) {
        processSetVariableMethods(args);
      } else if (isExecuteMethod(m, args)) {
        processExecuteMethod(args, localClassLoader);
      } else if (isInstantiateProcessMethod(m, args)) {
        processInstantiateProcessMethod(args);
      } else if (isGetModifiedObjectMethod(m, args)) {
        processGetModifiedObjectMethod(args);
      }
    } catch (final Exception e) {
      if (LOG.isLoggable(Level.SEVERE)) {
        LOG.severe("Error on RESTServerInterceptor: " + Misc.getStackTraceFrom(e));
      }
    } finally {
      Thread.currentThread().setContextClassLoader(baseClassLoader);
    }
  }

  private void handleSecurity() {
    if (APIMethodsSecurity.isSecuredMethod(m)) {
      String userId = null;
      // if the user defined a security context, use it. Else, use the
      // default UserOwner mechanism
      try {
        userId = EnvTool.getBonitaSecurityContext().getUser();
      } catch (final Throwable t) {
        userId = UserOwner.getUser();
      }

      Authentication.setUserId(userId);
    }
  }

  private void processGetModifiedObjectMethod(final Object[] args) throws IOException, ClassNotFoundException {
    final ProcessDefinitionUUID processUUID = (ProcessDefinitionUUID) args[0];
    if (args[2] instanceof RESTObject) {
      args[2] = getObjectFromRESTObject((RESTObject) args[2], processUUID);
    }
    if (args[3] instanceof RESTObject) {
      args[3] = getObjectFromRESTObject((RESTObject) args[3], processUUID);
    }
  }

  private boolean isGetModifiedObjectMethod(final Method m, final Object[] args) {
    return m.getName().equals("getModifiedJavaObject") && args[0] instanceof ProcessDefinitionUUID && args.length == 4;
  }

  private void processInstantiateProcessMethod(final Object[] args) {
    final ClassLoader localClassLoader = EnvTool.getClassDataLoader().getProcessClassLoader(
        (ProcessDefinitionUUID) args[0]);
    Thread.currentThread().setContextClassLoader(localClassLoader);
    args[1] = ((RESTMap<?, ?>) args[1]).getActualMap();
  }

  private boolean isInstantiateProcessMethod(final Method m, final Object[] args) {
    return m.getName().equals("instantiateProcess") && (args.length == 2 || args.length == 3)
        && args[1] instanceof RESTMap<?, ?>;
  }

  private void processExecuteMethod(final Object[] args, ClassLoader localClassLoader) throws IOException,
      ClassNotFoundException {
    // commomClassLoader
    if (args.length == 1) {
      localClassLoader = EnvTool.getClassDataLoader().getCommonClassLoader();
    } else if (args.length == 2 && args[1] instanceof ProcessDefinitionUUID) {
      // processClassLoader
      localClassLoader = EnvTool.getClassDataLoader().getProcessClassLoader((ProcessDefinitionUUID) args[1]);
    }
    Thread.currentThread().setContextClassLoader(localClassLoader);

    args[0] = ((RESTCommand<?>) args[0]).getCommand();
  }

  private boolean isExecuteMethod(final Method m, final Object[] args) {
    return m.getName().equals("execute") && args.length >= 1 && args[0] instanceof RESTCommand<?>;
  }

  private void processSetVariableMethods(final Object[] args) throws IOException, ClassNotFoundException,
      ActivityDefNotFoundException, ProcessNotFoundException, DataFieldNotFoundException {
    ProcessDefinitionUUID processUUID = null;
    ActivityInstance activity = null;
    if (args[0] instanceof ActivityInstanceUUID) {
      final ActivityInstanceUUID activityUUID = (ActivityInstanceUUID) args[0];
      activity = EnvTool.getAllQueriers().getActivityInstance(activityUUID);
      processUUID = activity.getProcessDefinitionUUID();
    } else if (args[0] instanceof ProcessInstanceUUID) {
      final ProcessInstanceUUID instanceUUID = (ProcessInstanceUUID) args[0];
      final ProcessInstance instance = EnvTool.getAllQueriers().getProcessInstance(instanceUUID);
      processUUID = instance.getProcessDefinitionUUID();
    }

    if (args[2] instanceof RESTObject) {
      args[2] = getObjectFromRESTObject((RESTObject) args[2], processUUID);
    } else if (args[2] instanceof String) {
      final String variableValue = (String) args[2];

      // can be a String representation of a Java object
      if (variableValue.startsWith("<")) {
        final String variableName = (String) args[1];
        final DataFieldDefinition dataField = getDataFieldDefinition(processUUID, activity, variableName);
        if (!dataField.getDataTypeClassName().equals(String.class.getName())) {
          args[2] = getObjectFromXML(variableValue, processUUID);
        }
      }
    }
  }

  private boolean isSetVariableMethod(final Object[] args, final Method m) {
    return (m.getName().equals(SET_VARIABLE) || m.getName().equals(SET_PROCESS_INSTANCE_VARIABLE) || m.getName()
        .equals(SET_ACTIVITY_INSTANCE_VARIABLE))
        && args.length == 3
        && (args[2] instanceof RESTObject || args[2] instanceof String);
  }

  private DataFieldDefinition getDataFieldDefinition(final ProcessDefinitionUUID processUUID,
      final ActivityInstance activity, final String variableName) throws ActivityDefNotFoundException,
      ProcessNotFoundException, DataFieldNotFoundException {

    final APIAccessor apiAccessor = new StandardAPIAccessorImpl();
    final QueryDefinitionAPI queryDefinitionAPI = apiAccessor.getQueryDefinitionAPI();
    DataFieldDefinition dataField = null;
    if (activity != null) {
      try {
        dataField = queryDefinitionAPI.getActivityDataField(activity.getActivityDefinitionUUID(), variableName);
      } catch (final DataFieldNotFoundException e) {
        // it's a global variable
        dataField = queryDefinitionAPI.getProcessDataField(processUUID, variableName);
      }
    } else {
      dataField = queryDefinitionAPI.getProcessDataField(processUUID, variableName);
    }
    return dataField;
  }

  private boolean isAmbigousReturnType(final Method m, final Object ret) {
    if (ret == null || !ret.getClass().isArray()) {
      return false;
    }

    final String methodName = m.getName();
    // methods returning an Object or a generic unknown type
    return "execute".equals(methodName) || "getVariable".equals(methodName)
        || "getActivityInstanceVariable".equals(methodName) || "getProcessInstanceVariable".equals(methodName)
        || "evaluateGroovyExpression".equals(methodName);
  }

  private Object getObjectFromRESTObject(final RESTObject restObject, final ProcessDefinitionUUID processUUID)
      throws IOException, ClassNotFoundException {
    Object result = null;
    ClassLoader localClassLoader = null;
    try {
      result = restObject.getObject();
    } catch (final ClassNotFoundException e) {
      // try with ProcessClassLoader
      localClassLoader = EnvTool.getClassDataLoader().getProcessClassLoader(processUUID);
      Thread.currentThread().setContextClassLoader(localClassLoader);

      try {
        result = restObject.getObject();
      } catch (final ClassNotFoundException ex) {
        // try with CommonClassLoader
        localClassLoader = EnvTool.getClassDataLoader().getCommonClassLoader();
        Thread.currentThread().setContextClassLoader(localClassLoader);

        result = restObject.getObject();
      }
    }

    return result;
  }

  private Object getObjectFromXML(final String xmlRepresentation, final ProcessDefinitionUUID processUUID) {
    final XStream xstream = XStreamUtil.getDefaultXstream();
    Object result = null;
    ClassLoader localClassLoader = null;
    try {
      result = xstream.fromXML(xmlRepresentation);
    } catch (final Exception e) {
      // try with ProcessClassLoader
      localClassLoader = EnvTool.getClassDataLoader().getProcessClassLoader(processUUID);
      Thread.currentThread().setContextClassLoader(localClassLoader);

      try {
        result = xstream.fromXML(xmlRepresentation);
      } catch (final Exception ex) {
        // try with CommonClassLoader
        localClassLoader = EnvTool.getClassDataLoader().getCommonClassLoader();
        Thread.currentThread().setContextClassLoader(localClassLoader);

        result = xstream.fromXML(xmlRepresentation);
      }
    }

    return result;
  }

  private Throwable getRemoteCause(final RemoteException e) {
    Throwable t = e;
    while (t instanceof RemoteException) {
      t = t.getCause();
    }
    return t;
  }

}
