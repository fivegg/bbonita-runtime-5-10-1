package org.ow2.bonita.facade.exception;

import org.ow2.bonita.util.BonitaException;

public class EventNotFoundException extends BonitaException {

  private static final long serialVersionUID = 3777373391546502291L;

  public EventNotFoundException(String message) {
    super(message);
  }

}
