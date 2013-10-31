/**
 * Copyright (C) 2007 Bull S. A. S.
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
 * Modified by Matthieu Chaffotte - BonitaSoft
 **/
package org.ow2.bonita.util;

import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

/**
 * @author Pierre Vigneras
 */
public class SimpleCallbackHandler implements CallbackHandler {
  private final String name;
  private final String password;

  public SimpleCallbackHandler(final String name, final String password) {
    this.name = name;
    this.password = password;
  }

  @Deprecated
  public SimpleCallbackHandler(final String name, final String password, final boolean emptyLoginPermitted) {
    this(name, password);
    if (!emptyLoginPermitted && (password == null || password.length() == 0)) {
      throw new IllegalArgumentException();
    }
  }

  @Override
  public void handle(final Callback[] callbacks) throws IOException, UnsupportedCallbackException {
    for (final Callback callback : callbacks) {
      if (callback instanceof NameCallback) {
        final NameCallback nc = (NameCallback) callback;
        nc.setName(name);
      } else if (callback instanceof PasswordCallback) {
        final PasswordCallback pc = (PasswordCallback) callback;
        pc.setPassword(password.toCharArray());
      }
    }
  }

}
