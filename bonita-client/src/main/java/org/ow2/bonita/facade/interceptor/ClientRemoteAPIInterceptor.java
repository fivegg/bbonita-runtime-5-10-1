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
 * Modified by Elias Ricken de Medeiros, Matthieu Chaffotte - BonitaSoft S.A.
 **/
package org.ow2.bonita.facade.interceptor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ow2.bonita.facade.APIAccessor;
import org.ow2.bonita.facade.Context;
import org.ow2.bonita.facade.exception.BonitaInternalException;
import org.ow2.bonita.facade.rest.interceptor.RESTBonitaRuntimeExceptionWrapper;
import org.ow2.bonita.facade.rest.wrapper.RESTSet;
import org.ow2.bonita.identity.auth.APIMethodsSecurity;
import org.ow2.bonita.identity.auth.DomainOwner;
import org.ow2.bonita.identity.auth.PasswordOwner;
import org.ow2.bonita.identity.auth.RESTUserOwner;
import org.ow2.bonita.identity.auth.UserOwner;
import org.ow2.bonita.util.BonitaConstants;
import org.ow2.bonita.util.ExceptionManager;

// This class is only there to wrap try{...} catch(RemoteException e){...}
public class ClientRemoteAPIInterceptor implements InvocationHandler {

  private final Object api;
  private final String queryList;

  public ClientRemoteAPIInterceptor(final Object api, final String queryList) {
    this.api = api;
    this.queryList = queryList;
  }

  @Override
  public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
    final Map<String, String> options = new HashMap<String, String>();
    options.put(APIAccessor.QUERYLIST_OPTION, this.queryList);
    if (!APIMethodsSecurity.isSecuredMethod(method)) {
      options.put(APIAccessor.USER_OPTION, null);
    } else {
      options.put(APIAccessor.USER_OPTION, UserOwner.getUser());
    }
    options.put(APIAccessor.DOMAIN_OPTION, DomainOwner.getDomain());

    // if is REST set hashed password
    if (isREST() && APIMethodsSecurity.isSecuredMethod(method)) {
      options.put(APIAccessor.REST_USER_OPTION, RESTUserOwner.getUser());
      options.put(APIAccessor.PASSWORD_HASH_OPTION, PasswordOwner.getPassword());
    }

    try {
      Class<?>[] params;
      Object[] methodsArgs;

      final Class<?>[] p = method.getParameterTypes();
      if (p.length > 0) {
        params = new Class<?>[p.length + 1];
        for (int i = 0; i < p.length; i++) {
          params[i] = p[i];
        }
        params[p.length] = Map.class;
      } else {
        params = new Class<?>[] { Map.class };
      }
      if (args != null) {
        methodsArgs = new Object[args.length + 1];
        for (int i = 0; i < args.length; i++) {
          methodsArgs[i] = args[i];
        }
        methodsArgs[args.length] = options;
      } else {
        methodsArgs = new Object[] { options };
      }

      if (isREST()) {
        int i = 0;
        final Type[] genericParams = method.getGenericParameterTypes();
        for (final Class<?> currentClass : method.getParameterTypes()) {
          if (currentClass.equals(Collection.class)) {
            params[i] = List.class;
            methodsArgs[i] = getList((Collection<?>) methodsArgs[i]);
          }
          // workaround for generic Set<E>
          else if (currentClass.equals(Set.class) && genericParams[i] instanceof ParameterizedType) {
            final Type type = ((ParameterizedType) genericParams[i]).getActualTypeArguments()[0];
            if (!(type instanceof Class<?>)) {
              params[i] = RESTSet.class;
              methodsArgs[i] = new RESTSet((Set<?>) methodsArgs[i]);
            }
          }
          i++;
        }
      }

      String methodName = method.getName();

      // change method to invoke,
      if (isREST()) {
        if (methodName.equals("createDocument") && methodsArgs.length == 6 && methodsArgs[4] != null /*
                                                                                                      * content cannot
                                                                                                      * be null
                                                                                                      */) {
          methodName = "createDocumentOctetStream";
        } else if (methodName.equals("addDocumentVersion") && methodsArgs.length == 6 && methodsArgs[4] != null /*
                                                                                                                 * content
                                                                                                                 * cannot
                                                                                                                 * be
                                                                                                                 * null
                                                                                                                 */) {
          methodName = "addDocumentVersionOctetStream";
        } else if (methodName.equals("addAttachment") && methodsArgs.length == 5 && methodsArgs[3] != null /*
                                                                                                            * content
                                                                                                            * cannot be
                                                                                                            * null
                                                                                                            */) {
          methodName = "addAttachmentOctetStream";
        }
      }

      final Method m = this.api.getClass().getMethod(methodName, params);
      return m.invoke(this.api, methodsArgs);
    } catch (final Exception e) {
      if (e instanceof InvocationTargetException) {
        final Throwable invocationExceptionCause = e.getCause();
        if (invocationExceptionCause instanceof RemoteException) {
          final RemoteException remoteException = (RemoteException) invocationExceptionCause;
          final Throwable remoteCause = getRemoteCause(remoteException);
          InterceptorUtil.manageInvokeExceptionCause(method, remoteCause);
        } else if (invocationExceptionCause instanceof RESTBonitaRuntimeExceptionWrapper) {
          final RESTBonitaRuntimeExceptionWrapper exception = (RESTBonitaRuntimeExceptionWrapper) invocationExceptionCause;
          throw exception.getCause();
        } else {
          throw invocationExceptionCause;
        }
      }
      final String message = ExceptionManager.getInstance().getFullMessage("baa_CAPII_1", e);
      throw new BonitaInternalException(message, e);
    }
  }

  private boolean isREST() {
    final String apiType = System.getProperty(BonitaConstants.API_TYPE_PROPERTY);
    return apiType != null && apiType.equalsIgnoreCase(Context.REST.toString());
  }

  private <T> List<T> getList(final Collection<T> col) {
    if (col == null) {
      return Collections.emptyList();
    }
    return new ArrayList<T>(col);
  }

  private Throwable getRemoteCause(final RemoteException e) {
    Throwable t = e;
    while (t instanceof RemoteException) {
      t = t.getCause();
    }
    return t;
  }

}
