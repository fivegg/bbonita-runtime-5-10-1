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

import java.util.ArrayList;
import java.util.List;

import org.ow2.bonita.env.Descriptor;
import org.ow2.bonita.env.WireContext;
import org.ow2.bonita.env.WireDefinition;
import org.ow2.bonita.services.CommandService;
import org.ow2.bonita.services.impl.Interceptor;

/**
 * @author Tom Baeyens
 */
public class CommandServiceDescriptor extends AbstractDescriptor {

  private static final long serialVersionUID = 1L;

  CommandService commandService;
  List<Descriptor> interceptorDescriptors;

  public Object construct(WireContext wireContext) {
    CommandService interceptedCommandService = commandService;
    if (interceptorDescriptors != null) {
      for (int i = interceptorDescriptors.size() - 1; i >= 0; i--) {
        Descriptor descriptor = interceptorDescriptors.get(i);
        Interceptor interceptor = (Interceptor) descriptor
            .construct(wireContext);
        interceptor.setNext(interceptedCommandService);
        interceptedCommandService = interceptor;
      }
    }
    return interceptedCommandService;
  }

  public Class<?> getType(WireDefinition wireDefinition) {
    return CommandService.class;
  }

  public void addInterceptorDescriptor(Descriptor descriptor) {
    if (interceptorDescriptors == null) {
      interceptorDescriptors = new ArrayList<Descriptor>();
    }
    interceptorDescriptors.add(descriptor);
  }

  public void setCommandService(CommandService commandService) {
    this.commandService = commandService;
  }
}
