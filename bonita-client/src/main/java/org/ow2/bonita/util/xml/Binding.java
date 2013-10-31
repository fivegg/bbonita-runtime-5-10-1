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
package org.ow2.bonita.util.xml;

import org.w3c.dom.Element;

/**
 * a modular mapping between an xml element and a java object in the domain
 * model, see also {@link Parser}.
 * 
 * <a href="./Parser.html#bindings">See also 'Bindings'</a>.
 * 
 * @see Parser
 */
public interface Binding {

  String getCategory();

  /** does this binding apply to the given element? */
  boolean matches(Element element);

  /**
   * translates the given element into a domain model java object. Use the parse
   * to report problems.
   * 
   * @return the domain model java object.
   * @see Parser
   */
  Object parse(Element element, Parse parse, Parser parser);
}
