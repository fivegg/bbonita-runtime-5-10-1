package org.ow2.bonita.identity.auth;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.security.auth.login.LoginContext;

import org.ow2.bonita.util.Misc;


public class UserOwner {

  private static final ThreadLocal<String> USERS = new ThreadLocal<String>();

  private static final Logger LOG = Logger.getLogger(UserOwner.class.getName());

  public static final String HELP = "User has not been set up using setUser(String user)!"
    + "Problem may be:"
    + Misc.LINE_SEPARATOR
    + "\t - you did not logged in (e.g. using: " + LoginContext.class.getName() + ".login()";


  public static void setUser(final String user) {
  	if (LOG.isLoggable(Level.FINEST)) {
      LOG.entering(UserOwner.class.getName(), "setUser: " + user);
    }
    USERS.set(user);
  }

  public static String getUser() {
    if (LOG.isLoggable(Level.FINEST)) {
      LOG.entering(UserOwner.class.getName(), "getUser");
    }
    final String s = USERS.get();
    if (s == null) {
      throw new IllegalStateException(HELP);
    }

    if (LOG.isLoggable(Level.FINEST)) {
      LOG.exiting(UserOwner.class.getName(), "getUser", s);
    }
    return s;
  }

}
