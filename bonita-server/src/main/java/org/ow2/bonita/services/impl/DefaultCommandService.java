/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.ow2.bonita.services.impl;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.ow2.bonita.env.Environment;
import org.ow2.bonita.services.CommandService;
import org.ow2.bonita.util.BonitaRuntimeException;
import org.ow2.bonita.util.Command;
import org.ow2.bonita.util.ExceptionManager;

/**
 * @author Tom Baeyens
 */
public class DefaultCommandService implements CommandService {

	static final Logger LOG = Logger.getLogger(DefaultCommandService.class.getName());

  public <T> T execute(Command<T> command) {
    Environment environment = Environment.getCurrent();

    try {
      return command.execute(environment);

    } catch (RuntimeException e) {
    	if (LOG.isLoggable(Level.SEVERE)) {
        LOG.severe("exception while executing command " + command + ": " + e.getMessage());
    	}
      throw e;

    } catch (Exception e) {
    	if (LOG.isLoggable(Level.SEVERE)) {
        LOG.severe("exception while executing command " + command + ": " + e.getMessage());
    	}
      String message = ExceptionManager.getInstance().getFullMessage("bp_DCS_1 ", command);
      throw new BonitaRuntimeException(message, e, DefaultCommandService.class.getName());
    }
  }
}
