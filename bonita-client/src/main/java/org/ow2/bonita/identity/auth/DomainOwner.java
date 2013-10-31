package org.ow2.bonita.identity.auth;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.security.auth.login.LoginContext;

import org.ow2.bonita.util.Misc;


public class DomainOwner {

  private static final ThreadLocal<String> DOMAINS = new ThreadLocal<String>();

  private static final Logger LOG = Logger.getLogger(DomainOwner.class.getName());

  public static final String HELP = "Domain has not been set up using setDomain(String domain)!"
    + "Problem may be:"
    + Misc.LINE_SEPARATOR
    + "\t - you did not logged in (e.g. using: "
    + LoginContext.class.getName()
    + ".login()"
    + Misc.LINE_SEPARATOR
    + "\t - your JAAS configuration did not fill the domain option";


  public static void setDomain(final String domain) {
    DOMAINS.set(domain);
  }

  public static String getDomain() {
    if (LOG.isLoggable(Level.FINEST)) {
      LOG.entering(DomainOwner.class.getName(), "getDomain");
    }
    final String s = DOMAINS.get();
    if (s == null) {
      throw new IllegalStateException(HELP);
    }

    if (LOG.isLoggable(Level.FINEST)) {
      LOG.exiting(DomainOwner.class.getName(), "getDomain", s);
    }
    return s;
  }


}
