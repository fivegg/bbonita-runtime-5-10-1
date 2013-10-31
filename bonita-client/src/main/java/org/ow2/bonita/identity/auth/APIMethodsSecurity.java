package org.ow2.bonita.identity.auth;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ow2.bonita.facade.ManagementAPI;
import org.ow2.bonita.facade.WebAPI;
import org.ow2.bonita.facade.internal.AbstractRemoteManagementAPI;
import org.ow2.bonita.facade.internal.AbstractRemoteWebAPI;

/**
 * This class purpose is to name the methods that shouldn't be secured (no authentication is required in order to call them)
 * @author Anthony Birembaut, Elias Ricken de Medeiros
 *
 */
public class APIMethodsSecurity {

  private static final List<Method> UNSECURED_METHODS = new ArrayList<Method>();

  static {
    try {
      // no identity check should be performed on the checkUserCredentials method as it's required for authentication
      UNSECURED_METHODS.add(ManagementAPI.class.getMethod("checkUserCredentials", String.class, String.class));
      UNSECURED_METHODS.add(AbstractRemoteManagementAPI.class.getMethod("checkUserCredentials", String.class, String.class, Map.class));
   // no identity check should be performed on the checkUserCredentialsWithPasswordHash method as it's required for authentication
      UNSECURED_METHODS.add(ManagementAPI.class.getMethod("checkUserCredentialsWithPasswordHash", String.class, String.class));
      UNSECURED_METHODS.add(AbstractRemoteManagementAPI.class.getMethod("checkUserCredentialsWithPasswordHash", String.class, String.class, Map.class));
   // no identity check should be performed on the getIdentityKeyFromTemporaryToken method as it's required for authentication
      UNSECURED_METHODS.add(WebAPI.class.getMethod("getIdentityKeyFromTemporaryToken", String.class));
      UNSECURED_METHODS.add(AbstractRemoteWebAPI.class.getMethod("getIdentityKeyFromTemporaryToken", String.class, Map.class));
    } catch (SecurityException e) {
      e.printStackTrace();
    } catch (NoSuchMethodException e) {
      e.printStackTrace();
    }
  }

  public static boolean isSecuredMethod(Method m) {
    if (UNSECURED_METHODS.contains(m)) {
      return false;
    }
    return true;
  }
}
