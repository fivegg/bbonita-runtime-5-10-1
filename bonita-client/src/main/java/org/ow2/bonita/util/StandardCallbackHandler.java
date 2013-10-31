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
 * 
 * Modified by Matthieu Chaffotte - BonitaSoft S.A.
 **/
package org.ow2.bonita.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.TextInputCallback;
import javax.security.auth.callback.TextOutputCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

/**
 * @author "Pierre Vigneras"
 */
public class StandardCallbackHandler implements CallbackHandler {
  private static final Logger LOG = Logger.getLogger(StandardCallbackHandler.class.getName());

  @Override
  public void handle(final Callback[] callbacks) throws IOException, UnsupportedCallbackException {
    if (LOG.isLoggable(Level.CONFIG)) {
      LOG.config("Using: " + this.getClass().getName() + " to request login informations.");
    }
    for (final Callback callback : callbacks) {
      if (callback instanceof TextOutputCallback) {
        // display the message according to the specified type
        final TextOutputCallback toc = (TextOutputCallback) callback;
        switch (toc.getMessageType()) {
          case TextOutputCallback.INFORMATION:
            System.out.println(toc.getMessage());
            break;
          case TextOutputCallback.ERROR:
            System.err.println("ERROR: " + toc.getMessage());
            break;
          case TextOutputCallback.WARNING:
            System.err.println("WARNING: " + toc.getMessage());
            break;
          default:
            final String message = ExceptionManager.getInstance().getFullMessage("buc_SCH_1", toc.getMessageType());
            throw new IOException(message);
        }

      } else if (callback instanceof NameCallback) {
        // prompt the user for a user name
        final NameCallback nc = (NameCallback) callback;
        System.out.println(nc.getPrompt());
        System.out.flush();
        nc.setName(getLineContent());
      } else if (callback instanceof PasswordCallback) {
        // prompt the user for sensitive information
        final PasswordCallback pc = (PasswordCallback) callback;
        System.out.println(pc.getPrompt());
        System.out.flush();
        final String content = getLineContent();
        char[] charArray;
        if (content == null) {
          charArray = null;
        } else {
          charArray = content.toCharArray();
        }
        pc.setPassword(charArray);
      } else if (callback instanceof TextInputCallback) {
        final TextInputCallback tic = (TextInputCallback) callback;
        System.out.println(tic.getPrompt());
        System.out.flush();
        tic.setText(getLineContent());
      } else {
        final String message = ExceptionManager.getInstance().getFullMessage("buc_SCH_2");
        throw new UnsupportedCallbackException(callback, message);
      }
    }
  }

  private String getLineContent() throws IOException {
    final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in, BonitaConstants.FILE_ENCONDING));
    try {
      return reader.readLine();
    } finally {
      reader.close();
    }
  }

}
