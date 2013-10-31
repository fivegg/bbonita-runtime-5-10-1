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
package org.ow2.bonita.facade.interceptor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import javax.interceptor.InvocationContext;

import org.ow2.bonita.facade.APIAccessor;
import org.ow2.bonita.facade.impl.StandardAPIAccessorImpl;
import org.ow2.bonita.identity.auth.DomainOwner;
import org.ow2.bonita.identity.auth.UserOwner;
import org.ow2.bonita.util.BonitaRuntimeException;
import org.ow2.bonita.util.ExceptionManager;

/**
 * Interceptor class to redirect ejb3 bean calls to the standalone api
 * @author Thomas Gueze
 *
 */
public class EJB3Interceptor {

  private static final int SUFFIX_LENGTH = "Bean".length();

  private Object getStandaloneAPI(final String className, final String queryList) {
    try {
      final APIAccessor apiAccessor = new StandardAPIAccessorImpl();
      final Method m = apiAccessor.getClass().getMethod("get" + className, String.class);
      return m.invoke(apiAccessor, new Object[]{queryList});
    } catch (final Exception e) {
    	String message = ExceptionManager.getInstance().getFullMessage("baie_EJB3I_1");
      throw new BonitaRuntimeException(message, e);
    }
  }

  @SuppressWarnings("unchecked")
  public Object performInterception(final InvocationContext ctx) throws Exception {
    String className = ctx.getTarget().getClass().getSimpleName();
    className = className.substring(0, className.length() - SUFFIX_LENGTH);
    
    final Map<String, String> options = (Map<String, String>) ctx.getParameters()[ctx.getParameters().length - 1];
    final String queryList = options.get(APIAccessor.QUERYLIST_OPTION);
    final String user = options.get(APIAccessor.USER_OPTION);
  	final String domain = options.get(APIAccessor.DOMAIN_OPTION);
  	UserOwner.setUser(user);
  	DomainOwner.setDomain(domain);
  	
  	
    final Class< ? >[] currentParameterTypes = ctx.getMethod().getParameterTypes();
    final Object[] currentParameterValues = ctx.getParameters();
    
    final Class< ? >[] newParameterTypes = new Class[currentParameterTypes.length - 1];
    final Object[] newParameterValues = new Object[currentParameterValues.length - 1];
    
    for (int i = 0 ; i < (currentParameterTypes.length - 1) ; i++) {
    	newParameterTypes[i] = currentParameterTypes[i]; 
    }
    
    for (int i = 0 ; i < (currentParameterValues.length - 1) ; i++) {
    	newParameterValues[i] = currentParameterValues[i]; 
    }
    
    final Object standaloneAPI = getStandaloneAPI(className, queryList);
    Method implMethod = null;
    try {
      implMethod = standaloneAPI.getClass().getMethod(ctx.getMethod().getName(), newParameterTypes);
      return implMethod.invoke(standaloneAPI, newParameterValues);
    } catch (final Exception e) {
      if (e instanceof InvocationTargetException && implMethod != null) {
        final Throwable invocationExceptionCause = e.getCause();
        InterceptorUtil.manageInvokeExceptionCause(implMethod, invocationExceptionCause);
      }
      String message = ExceptionManager.getInstance().getFullMessage("baie_EJB3I_2", ctx.getMethod().getName());
      throw new BonitaRuntimeException(message, e);
    }
  }
}
