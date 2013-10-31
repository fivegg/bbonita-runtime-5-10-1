package org.ow2.bonita.facade.identity;

import java.io.Serializable;

/**
 * @author Anthony Birembaut
 *
 */
public interface Membership extends Serializable {
  
  String getUUID();
  
  Role getRole();
  
  Group getGroup();
}
