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
package org.ow2.bonita.env.descriptor;

import java.net.URL;

import org.ow2.bonita.env.WireContext;
import org.ow2.bonita.env.WireException;
import org.ow2.bonita.util.ExceptionManager;

/**
 * @author Tom Baeyens
 */
public class UrlDescriptor extends AbstractDescriptor {

  private static final long serialVersionUID = 1L;

  String text = null;

  public Object construct(WireContext wireContext) {
    try {
      return new URL(text);
    } catch (Exception e) {
    	String message = ExceptionManager.getInstance().getFullMessage("bp_UD_1", text);
      throw new WireException(message, e);
    }
  }

  public void setUrlText(String text) {
    this.text = text;
  }

  public void setUrl(URL url) {
    this.text = url.toString();
  }
}
