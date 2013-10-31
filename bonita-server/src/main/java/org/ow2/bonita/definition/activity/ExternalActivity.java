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
package org.ow2.bonita.definition.activity;

import java.io.Serializable;
import java.util.Map;

import org.ow2.bonita.runtime.model.Execution;

/**
 * <p>
 * Process languages will provide a set of these node implementations. But
 * languages like jPDL even allow users to provide their own node behaviour with
 * this interface.
 * </p>
 * 
 * @author Tom Baeyens
 */
public interface ExternalActivity extends Serializable {

  /**
   * handles an external trigger.
   * 
   * <p>
   * An external trigger that comes into an execution through one of the
   * {@link Execution#signal()} methods, will be delegated to the node in which
   * the execution is positioned when it receives the external trigger.
   * </p>
   * 
   * <p>
   * The signal method implements how the node will react on that signal. For
   * example, the outgoing transition could be taken that corresponds with the
   * given signal.
   * </p>
   * 
   * @param execution
   *          the {@link Execution} for which the signal is given
   * 
   * @param signalName
   *          is an abstract text that can be associated with a signal. this
   *          corresponds to e.g. a method name in a java class interface. The
   *          implementation can decide e.g. to use the signal to identify the
   *          outgoing transition.
   * 
   * @param parameters
   *          is extra information that can be provided with a signal. In this
   *          way, it is somewhat similar to the parameters that can be fed into
   *          a method invocation through reflection.
   * 
   * @throws Exception
   *           to indicate any kind of failure. Note that exceptions are
   *           considered non recoverable. After an Exception, the execution
   *           should not be used any more and if this is during a transaction,
   *           the transaction should be rolled back.
   */
  void signal(Execution execution, String signalName, Map<String, Object> parameters) throws Exception;
  
  /**
   * piece of Java code that is to be included in a process as node behaviour or
   * as a hidden listener to process events.
   * 
   * <p>
   * Activities can be used to implement the behaviour of nodes, in which case
   * this behaviour is associated to a graphical node in the diagram, or they
   * can be added as events, in that case, they are being hidden from the
   * diagram.
   * </p>
   * 
   * <p>
   * If an activity is the node behaviour, then it can control the propagation
   * of execution. Node behaviours can be external activities. That means their
   * runtime behaviour is a wait state. In that case, {@link ExternalActivity}
   * should be implemented to also handle the external triggers.
   * </p>
   */
  void execute(Execution execution, boolean checkJoinType) throws Exception;

}
