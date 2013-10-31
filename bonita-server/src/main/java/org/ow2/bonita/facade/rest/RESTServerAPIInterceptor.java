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
package org.ow2.bonita.facade.rest;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import org.ow2.bonita.env.Environment;
import org.ow2.bonita.env.EnvironmentFactory;
import org.ow2.bonita.env.GlobalEnvironmentFactory;
import org.ow2.bonita.facade.exception.BonitaWrapperException;
import org.ow2.bonita.facade.exception.UnRollbackableException;
import org.ow2.bonita.identity.auth.DomainOwner;
import org.ow2.bonita.services.CommandService;
import org.ow2.bonita.services.impl.DefaultCommandService;
import org.ow2.bonita.util.BonitaRuntimeException;

/**
 * 
 * @author Elias Ricken de Medeiros, Matthieu Chaffotte, Charles Souillard
 * 
 */
public class RESTServerAPIInterceptor implements InvocationHandler {

  private final Object api;

  public RESTServerAPIInterceptor(final Object api) {
    this.api = api;
  }

  @Override
  public Object invoke(final Object proxy, final Method method,
      final Object[] args) throws Throwable {
    if (Environment.getCurrent() != null) {
      return new RESTServerAPIInterceptorCommand(method, args, api)
          .execute(Environment.getCurrent());
    }
    // If no environment: use directly the command Service with interceptors
    final EnvironmentFactory envFactory = GlobalEnvironmentFactory
        .getEnvironmentFactory(DomainOwner.getDomain());
    try {
      return envFactory.get(CommandService.class).execute(
          new RESTServerAPIInterceptorCommand(method, args, api));
    } catch (final BonitaWrapperException e) {
      throw e.getCause();
    } catch (final BonitaRuntimeException e) {
      if (DefaultCommandService.class.getName().equals(e.getWrappedBy()) || UnRollbackableException.class.getName().equals(e.getWrappedBy())) {
        throw e.getCause();
      } else {
        throw e;
      }
    }
  }

}
