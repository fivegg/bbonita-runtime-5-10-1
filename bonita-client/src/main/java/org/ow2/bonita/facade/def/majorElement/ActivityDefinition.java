/**
 * Copyright (C) 2006  Bull S. A. S.
 * Bull, Rue Jean Jaures, B.P.68, 78340, Les Clayes-sous-Bois
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA  02110-1301, USA.
 * 
 * Modified by Matthieu Chaffotte - BonitaSoft S.A.
 **/
package org.ow2.bonita.facade.def.majorElement;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import org.ow2.bonita.connector.core.MultipleInstancesInstantiator;
import org.ow2.bonita.facade.def.element.BoundaryEvent;
import org.ow2.bonita.facade.def.element.DeadlineDefinition;
import org.ow2.bonita.facade.def.element.FilterDefinition;
import org.ow2.bonita.facade.def.element.HookDefinition;
import org.ow2.bonita.facade.def.element.IncomingEventDefinition;
import org.ow2.bonita.facade.def.element.MultiInstantiationDefinition;
import org.ow2.bonita.facade.def.element.OutgoingEventDefinition;
import org.ow2.bonita.facade.def.element.SubflowParameterDefinition;
import org.ow2.bonita.facade.uuid.ActivityDefinitionUUID;

/**
 * This interface represents the activity definition.
 */
public interface ActivityDefinition extends ProcessElement, Serializable {

  /**
   * Join types supported by Bonita.
   */
  public static enum JoinType {
    AND, XOR
  }

  /**
   * Split types supported by Bonita.
   */
  public static enum SplitType {
    AND, XOR
  }

  /**
   * Activity type supported by Bonita.
   */
  public static enum Type {
    Automatic, Human, Timer, Decision, Subflow, SendEvents, ReceiveEvent, ErrorEvent, SignalEvent
  }

  /**
   * Returns the UUID for the ActivityDefinition.
   * @return the activity definition UUID 
   */
  ActivityDefinitionUUID getUUID();

  /**
   * Checks whether the activity has outgoing transitions.
   * @return true if the activity contains outgoing transitions; false otherwise;
   */
  boolean hasOutgoingTransitions();

  /**
   * Gets the outgoing transitions of the activity.
   * @return the outgoing transitions of the activity
   */
  Set<TransitionDefinition> getOutgoingTransitions();

  /**
   * Gets an outgoing transition according to its name.
   * @param transitionName the transition name
   * @return the outgoing transition 
   */
  TransitionDefinition getOutgoingTransition(String transitionName);

  /**
   * Checks whether the activity contains incoming transitions.
   * @return true if the activity contains incoming transitions; false otherwise
   */
  boolean hasIncomingTransitions();

  /**
   * Gets the incoming transitions of the activity.
   * @return the incoming transitions of the activity
   */
  Set<TransitionDefinition> getIncomingTransitions();

  /**
   * Gets an incoming transition according to its name.
   * @param transitionName the transition name
   * @return the incoming transition 
   */
  TransitionDefinition getIncomingTransitions(String transitionName);

  /**
   * Checks whether the activity contains boundary events.
   * @return true if the activity contains at least a boundary event; false otherwise.
   */
  boolean hasBoundaryEvents();

  /**
   * Retrieves the list of all boundary events of the activity.
   * @return the list of all boundary events or an empty list if the activity has no boundary events.
   */
  List<BoundaryEvent> getBoundaryEvents();

  /**
   * Retrieves the boundary event of the activity according to its name.
   * @return the boundary event or null if the event does not exist
   */
  BoundaryEvent getBoundaryEvent(String eventName);

  /**
   * Returns the SubFlowDefinition interface if this activity has subflow implementation
   * otherwise returns null.
   */
  String getSubflowProcessName();

  String getSubflowProcessVersion();
  
  /**
   * Returns the performer of the activity.
   * For an activity with startMode=Manual the performer element contains a defined
   * participant processDefinitionUUID.
   * For an activity with startMode=Automatic the performer element contains the
   * generic participant SYSTEM.
   */
  Set<String> getPerformers();

  /**
   * Deadlines are used to execute a specified java class upon the expiration of
   * a specified period of time.
   * returns (unordered) set of Deadlines.
   * returns empty set if no deadline are defined within the activity.
   */
  Set<DeadlineDefinition> getDeadlines();

  /**
   * Bonita engine allows to specify dataFields for activities.
   * @return The (unordered) set of DataFieldDefinition interfaces.
   */
  Set<DataFieldDefinition> getDataFields();

  /**
   * Hooks are user defined logic that can be triggered at some points of the life of
   * the activity.<br>
   * Those points are:
   * <ul>
   * <li>taskOnReady</li>
   * <li>taskOnStart</li>
   * <li>taskOnFinish</li>
   * </ul>
   * <br>
   * If the activity has startMode=Manual then these points of the life of the activity
   * is synchronized with ones of the task.
   * @return The (unordered) set of HookDefinition interfaces.
   */
  List<HookDefinition> getConnectors();

  /**
   * Performer assignment allows to perform various assignment rules within the task module.<br>
   * All the users defined into the role type can see and execute this one.
   * By adding this functionality, we can:
   * <br>
   * <ul>
   * <li>assign the task to a user of the role (group) by calling a java class in charge to the user selection
   * into the user group (callback performer assignment)</li>
   * <li>assign dynamically the activity to a user by using an activity variable</li>
   * (property performer assignment)</li>
   * </ul>
   * @return The PerformerAssignDefinition interface.
   */
  FilterDefinition getFilter();

  /**
   * Gets the MultiInstantiation Definition or null if the activity does not contain one.
   * @return the MultiInstantiation
   */
  MultiInstantiationDefinition getMultiInstantiationDefinition();

  /**
   * Gets the {@link MultipleInstancesInstantiator} 
   * @return the MultipleActivitiesInstantiator
   */
  MultiInstantiationDefinition getMultipleInstancesInstantiator();

  /**
   * 
   * @return
   */
  MultiInstantiationDefinition getMultipleInstancesJoinChecker();

  /**
   * Returns Join Type definition.
   * @return the join type
   */
  JoinType getJoinType();

  /**
   * Returns Split Type definition.
   * @return the split type
   */
  SplitType getSplitType();

  /**
   * Checks whether the activity is asynchronous.
   * @return true if the activity is asynchronous; false otherwise
   */
  boolean isAsynchronous();

  /**
   * Checks whether the activity sends events.
   * @return true if the activity sends events; false otherwise
   */
  boolean isSendEvents();

  /**
   * Checks whether the activity is a sub-process.
   * @return true if the activity is sub-process; false otherwise
   */
  boolean isSubflow();

  /**
   * Checks whether the activity is a timer.
   * @return true if the activity is timer; false otherwise
   */
  boolean isTimer();

  /**
   * Checks whether the activity is a task.
   * @return true if the activity is task; false otherwise
   */
  boolean isTask();

  /**
   * Checks whether the activity receives events.
   * @return true if the activity is receives events; false otherwise
   */
  boolean isReceiveEvent();

  /**
   * Checks whether the activity is an automatic one.
   * @return true if the activity is an automatic one; false otherwise
   */
  boolean isAutomatic();

  /**
   * Checks whether the activity is a throwing error event task.
   * @return true if the activity is a throwing error event task; false otherwise
   */
  boolean isThrowingErrorEvent();

  /**
   * Checks whether the activity is a signal event task.
   * @return true if the activity is a signal event task; false otherwise
   */
  boolean isSignalEvent();

  /**
   * Checks whether the activity is a throwing signal event task.
   * @return true if the activity is a throwing signal event task; false otherwise
   */
  boolean isThrowingSignalEvent();

  /**
   * Checks whether the activity is a catching signal event task.
   * @return true if the activity is a catching signal event task; false otherwise
   */
  boolean isCatchingSignalEvent();

  /**
   * Checks whether the activity is a catching error event task.
   * @return true if the activity is a catching error event task; false otherwise
   */
  boolean isCatchingErrorEvent();

  /**
   * Gets the timer condition if the activity is a timer or null otherwise
   * @return the timer condition if the activity is a timer; null otherwise
   */
  String getTimerCondition();

  Set<SubflowParameterDefinition> getSubflowInParameters();

  Set<SubflowParameterDefinition> getSubflowOutParameters();

  /**
   * Gets the time that this activity should be executed.
   * @return the executing time
   */
  long getExecutingTime();

  /**
   * Obtains the activity priority.
   * @return the number of the activity priority 
   */
  int getPriority();

  /**
   * Gets the class names which depend on this activity.
   * @return the class name list
   */
  Set<String> getClassDependencies();

  /**
   * Checks whether the activity is in a cycle.
   * @return true is the activity is in a cycle; false otherwise
   */
  boolean isInCycle();

  /**
   * Gets the incoming event or null.
   * @return the incoming event or null.
   */
  IncomingEventDefinition getIncomingEvent();

  /**
   * Gets the outgoing event or null.
   * @return the outgoing event or null.
   */
  Set<OutgoingEventDefinition> getOutgoingEvents();
  
  /**
   * Gets the activity type.
   * @return the activity type
   */
	Type getType();

	/**
	 * Checks whether the activity is repeated as a standard loop.
	 * @return true if the activity is repeated as a loop; false otherwise
	 */
	boolean isInALoop();

	/**
	 * Gets the loop condition expression.
	 * @return the loop condition expression as a Groovy expression
	 */
	String getLoopCondition();
	
	/**
	 * Gets the maximum number of loop iterations
	 * @return the maximum number of loop iterations as an integer or a Groovy expression
	 */
	String getLoopMaximum();

	/**
	 * Checks whether the loop condition is evaluated before the activity creation ("while" loop)
	 * or after the activity execution ("until" loop)
	 * @return true if the loop condition must be evaluated before the activity creation.
	 */
	boolean evaluateLoopConditionBeforeExecution();
	
	/**
	 * Gets the dynamic label expression. This expression will be used when creating a corresponding ActivityInstance.
	 */
	String getDynamicLabel();
	
	/**
	 * Gets the dynamic description expression. This expression will be used when creating a corresponding ActivityInstance.
	 */
	String getDynamicDescription();
	
	/**
   * Gets the dynamic execution summary expression. This expression will be used when completing a corresponding ActivityInstance.
   */
	String getDynamicExecutionSummary();

	/**
	 * Checks whether the event activity catches event.
	 * @return true if the event activity catches event; false if the event activity throws event.
	 */
	boolean catchEvent();

	/**
	 * Returns true if the activity ends the process. It works only when the activity have not got any outgoing transitions.
	 */
	boolean isTerminateProcess();
}
