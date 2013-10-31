/**
 * Copyright (C) 2010-2013 BonitaSoft S.A.
 * BonitaSoft, 31 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA  02110-1301, USA.
 **/
package org.ow2.bonita.env.binding;

import org.ow2.bonita.env.descriptor.BooleanDescriptor;
import org.ow2.bonita.env.descriptor.ContextTypeRefDescriptor;
import org.ow2.bonita.env.descriptor.EventExecutorDescriptor;
import org.ow2.bonita.env.descriptor.IntegerDescriptor;
import org.ow2.bonita.env.descriptor.ObjectDescriptor;
import org.ow2.bonita.env.descriptor.ReferenceDescriptor;
import org.ow2.bonita.env.descriptor.StringDescriptor;
import org.ow2.bonita.env.operation.InvokeOperation;
import org.ow2.bonita.runtime.event.EventExecutor;
import org.ow2.bonita.services.CommandService;
import org.ow2.bonita.util.xml.Parse;
import org.ow2.bonita.util.xml.Parser;
import org.ow2.bonita.util.xml.XmlUtil;
import org.w3c.dom.Element;

/**
 * parses a descriptor for creating a {@link EventExecutor}.
 * 
 * See schema docs for more details.
 * 
 * @author Charles Souillard, Matthieu Chaffotte
 */
public class EventExecutorBinding extends WireDescriptorBinding {

  public EventExecutorBinding() {
    super("event-executor");
  }

  @Override
  public Object parse(final Element element, final Parse parse, final Parser parser) {
    // create a event executor object
    final EventExecutorDescriptor descriptor = new EventExecutorDescriptor();

    if (element.hasAttribute("command-service")) {
      descriptor.addInjection("commandService", new ReferenceDescriptor(element.getAttribute("command-service")));
    } else {
      descriptor.addInjection("commandService", new ContextTypeRefDescriptor(CommandService.class));
    }

    if (element.hasAttribute("name")) {
      descriptor.addInjection("name", new StringDescriptor(element.getAttribute("name")));
    }

    parseIntAttribute(element, "threads", descriptor, "nbrOfThreads", parse);
    parseIntAttribute(element, "idle", descriptor, "idleMillis", parse);
    parseIntAttribute(element, "idle-min", descriptor, "minimumInterval", parse);
    parseIntAttribute(element, "lock", descriptor, "lockMillis", parse);
    parseIntAttribute(element, "retries", descriptor, "retries", parse);

    final Element jobExecutor = XmlUtil.element(element, "job-executor");
    final String jobExecutorClassName = XmlUtil.attribute(jobExecutor, "class");
    descriptor.addInjection("jobExecutorClassName", new StringDescriptor(jobExecutorClassName));
    parseIntAttribute(jobExecutor, "locks-to-query", descriptor, "locksToQuery", parse);
    parseIntAttribute(jobExecutor, "lock-idle-time", descriptor, "lockIdleTime", parse);

    final Element matcher = XmlUtil.element(element, "matcher");
    parseIntAttribute(matcher, "max-couples", descriptor, "matcherMaxCouples", parse);
    final String eventMatcherClassName = XmlUtil.attribute(matcher, "class");
    descriptor.addInjection("eventMatcherClassName", new StringDescriptor(eventMatcherClassName));
    final Element conditionMatching = XmlUtil.element(matcher, "condition-matching");
    parseBooleanAttribute(conditionMatching, "enable", descriptor, "expressionMatcherEnable", parse);

    final Element masterChecker = XmlUtil.element(element, "master-checker");
    final String masterCheckerClassName = XmlUtil.attribute(masterChecker, "class");
    descriptor.addInjection("masterCheckerClassName", new StringDescriptor(masterCheckerClassName));
    parseBooleanAttribute(masterChecker, "enable", descriptor, "masterCheckerEnable", parse);
    parseIntAttribute(masterChecker, "master-heartbeat-delay", descriptor, "masterCheckerMasterHeartbeatDelay", parse);
    parseIntAttribute(masterChecker, "slave-heartbeat-delay", descriptor, "masterCheckerSlaveHeartbeatDelay", parse);
    parseIntAttribute(masterChecker, "max-idle-delay", descriptor, "masterCheckerMaxIdleDelay", parse);

    // by default invoke the start method, unless auto-start is disabled
    if (XmlUtil.attributeBoolean(element, "auto-start", false, parse, true)) {
      final InvokeOperation invokeStartOperation = new InvokeOperation();
      invokeStartOperation.setMethodName("start");
      descriptor.addOperation(invokeStartOperation);
      descriptor.setAutoStart(true);
    }

    return descriptor;
  }

  private void parseIntAttribute(final Element element, final String attributeName, final ObjectDescriptor descriptor,
      final String fieldName, final Parse parse) {
    final Integer intValue = XmlUtil.attributeInteger(element, attributeName, false, parse);
    if (intValue != null) {
      descriptor.addInjection(fieldName, new IntegerDescriptor(intValue));
    }
  }

  private void parseBooleanAttribute(final Element element, final String attributeName,
      final ObjectDescriptor descriptor, final String fieldName, final Parse parse) {
    final Boolean boolValue = XmlUtil.attributeBoolean(element, attributeName, false, parse);
    if (boolValue != null) {
      descriptor.addInjection(fieldName, new BooleanDescriptor(boolValue));
    }
  }

}
