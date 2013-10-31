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
package org.ow2.bonita.env.binding;

import java.util.List;

import org.ow2.bonita.env.descriptor.ArgDescriptor;
import org.ow2.bonita.env.operation.SubscribeOperation;
import org.ow2.bonita.env.xml.WireParser;
import org.ow2.bonita.util.xml.Parse;
import org.ow2.bonita.util.xml.Parser;
import org.ow2.bonita.util.xml.XmlUtil;
import org.w3c.dom.Element;

/**
 * parses a descriptor for a {@link SubscribeOperation subscribe operation}.
 * 
 * See schema docs for more details.
 * 
 * @author Tom Baeyens
 * @author Guillaume Porcher (documentation)
 */
public class SubscribeBinding extends WireOperationBinding {

  public SubscribeBinding() {
    super("subscribe");
  }

  public Object parse(Element element, Parse parse, Parser parser) {
    SubscribeOperation subscribeOperation = new SubscribeOperation();

    // these are the different ways of specifying observables:
    // <subscribe [context='contextName'] [event(s)='...']... /> will use the
    // context as the observable
    // <subscribe [context='contextName'] object(s)='objectName'
    // [event(s)='...'] ... /> will use the object(s) with the given name in the
    // specified context
    // <subscribe [context='contextName'] to='wire-events' event(s)='...'
    // [object(s)='...'] ... /> will listen to wire events of a specific object
    // in the specified context

    // context
    String contextName = XmlUtil.attribute(element, "context");
    subscribeOperation.setContextName(contextName);

    // to
    String to = XmlUtil.attribute(element, "to");
    if ("wire-events".equalsIgnoreCase(to)) {
      subscribeOperation.setWireEvents(true);
    }

    // events
    List<String> eventNames = XmlUtil.parseList(element, "event");
    subscribeOperation.setEventNames(eventNames);

    // objects
    List<String> objectNames = XmlUtil.parseList(element, "object");
    subscribeOperation.setObjectNames(objectNames);

    // method & args
    String methodName = XmlUtil.attribute(element, "method");
    subscribeOperation.setMethodName(methodName);
    List<Element> argElements = XmlUtil.elements(element, "arg");
    WireParser wireParser = (WireParser) parser;
    List<ArgDescriptor> argDescriptors = wireParser.parseArgs(argElements,
        parse);
    subscribeOperation.setArgDescriptors(argDescriptors);

    return subscribeOperation;
  }
}
