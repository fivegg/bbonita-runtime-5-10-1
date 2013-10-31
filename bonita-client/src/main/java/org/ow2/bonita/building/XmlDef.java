/**
 * Copyright (C) 2010  BonitaSoft S.A.
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
package org.ow2.bonita.building;

/**
 * XML process definition file constants for elements and attributes names
 * 
 * @author Anthony Birembaut, Matthieu Chaffotte, Elias Ricken de Medeiros
 * 
 */
public final class XmlDef {

  public static final String PROCESS_DEFINITION = "process-definition";

  public static final String PRODUCT_VERSION = "product-version";

  public static final String PROCESS = "process";

  public static final String LABEL = "label";

  public static final String VALUE = "value";

  public static final String CLASS = "class";

  public static final String VARIABLE = "variable";

  public static final String ATTACHMENT = "attachment";

  public static final String ACTIVITIES = "activities";

  public static final String ACTIVITY = "activity";

  public static final String NAME = "name";

  public static final String VERSION = "version";

  public static final String DESCRIPTION = "description";

  public static final String DYNAMIC_DESCRIPTION = "dynamic-description";

  public static final String DYNAMIC_LABEL = "dynamic-label";

  public static final String DYNAMIC_EXECUTION_SUMMARY = "dynamic-execution-summary";

  public static final String STATE = "state";

  public static final String PRIORITY = "priority";

  public static final String EXECUTING_TIME = "executing-time";

  public static final String SPLIT_TYPE = "split-type";

  public static final String JOIN_TYPE = "join-type";

  public static final String SUBFLOW_PROCESS_NAME = "subflow-process-name";

  public static final String SUBFLOW_PROCESS_VERSION = "subflow-process-version";

  public static final String TIMER_CONDITION = "timer-condition";

  public static final String TYPE = "type";

  public static final String PERFORMERS = "performers";

  public static final String PERFORMER = "performer";

  public static final String TRANSITIONS = "transitions";

  public static final String TRANSITION = "transition";

  public static final String CONDITION = "condition";

  public static final String FROM = "from";

  public static final String TO = "to";

  public static final String PROCESS_DEPENDENCIES = "process-dependencies";

  public static final String PROCESS_DEPENDENCY = "process-dependency";

  public static final String META_DATAS = "meta-datas";

  public static final String META_DATA = "meta-data";

  public static final String DATA_FIELDS = "data-fields";

  public static final String DATA_FIELD = "data-field";

  public static final String DATATYPE_CLASSNAME = "datatype-classname";

  public static final String SCRIPTING_VALUE = "scripting-value";

  public static final String IS_TRANSIENT = "is-transient";

  public static final String ATTACHMENTS = "attachments";

  public static final String FILE_PATH = "file-path";

  public static final String FILE_NAME = "file-name";

  public static final String PARTICIPANTS = "participants";

  public static final String PARTICIPANT = "participant";

  public static final String ROLE_MAPPER = "role-mapper";

  public static final String CLASSNAME = "classname";

  public static final String PARAMETERS = "parameters";

  public static final String PARAMETER = "parameter";

  public static final String CONNECTORS = "connectors";

  public static final String CONNECTOR = "connector";

  public static final String EVENT = "event";

  public static final String ENUMERATION_VALUES = "enumeration-values";

  public static final String ENUMERATION_VALUE = "enumeration-value";

  public static final String SUBFLOW_IN_PARAMETER = "subflow-in-parameter";

  public static final String SUBFLOW_IN_PARAMETERS = "subflow-in-parameters";

  public static final String SUBFLOW_OUT_PARAMETER = "subflow-out-parameter";

  public static final String SUBFLOW_OUT_PARAMETERS = "subflow-out-parameters";

  public static final String SOURCE = "source";

  public static final String DESTINATION = "destination";

  public static final String MULTI_INSTANTIATION = "multi-instantiation";

  public static final String VARIABLE_NAME = "variable-name";

  public static final String MULTIPLE_ACT_INSTANTIATOR = "multiple-act-instantiator";

  public static final String MULTIPLE_ACT_JOINCHECKER = "multiple-act-joinchecker";

  public static final String FILTER = "filter";

  public static final String DEADLINES = "deadlines";

  public static final String DEADLINE = "deadline";

  public static final String OUTGOING_EVENTS = "outgoing-events";

  public static final String OUTGOING_EVENT = "outgoing-event";

  public static final String TIME_TO_LIVE = "time-to-live";

  public static final String TO_ACTIVITY = "to-activity";

  public static final String TO_PROCESS = "to-process";

  public static final String INCOMING_EVENT = "incoming-event";

  public static final String EXPRESSION = "expression";

  public static final String IS_THROWING_EXCEPTION = "throw-exception";

  public static final String ASYNCHRONOUS = "asynchronous";

  public static final String ISDEFAULT = "is-default";

  public static final String LOOP_CONDITION = "loop-condition";

  public static final String LOOP_MAXIMUM = "loop-maximum";

  public static final String BEFORE_EXECUTION = "before-execution";

  public static final String CATEGORIES = "categories";

  public static final String CATEGORY = "category";

  public static final String BOUNDARY_EVENTS = "boundary-events";

  public static final String BOUNDARY_EVENT = "boundary-event";

  public static final String TIMER_EVENTS = "timers";

  public static final String TIMER_EVENT = "timer";

  public static final String MESSAGE_EVENTS = "messages";

  public static final String MESSAGE_EVENT = "message";

  public static final String ERROR_EVENTS = "errors";

  public static final String ERROR_EVENT = "error";

  public static final String ERROR_CODE = "error-code";

  public static final String CATCH_EVENT = "catch-event";

  public static final String SIGNAL_CODE = "signal-code";

  public static final String SIGNAL_EVENTS = "signals";

  public static final String SIGNAL_EVENT = "signal";

  public static final String EVENT_SUB_PROCESSES = "event-sub-processes";

  public static final String EVENT_SUB_PROCESS = "event-sub-process";

  public static final String TERMINATE_PROCESS = "terminate-process";

  public static final String CORRELATION_KEY_NAME_1 = "correlation-key-name-1";

  public static final String CORRELATION_KEY_EXPR_1 = "correlation-key-expression-1";

  public static final String CORRELATION_KEY_NAME_2 = "correlation-key-name-2";

  public static final String CORRELATION_KEY_EXPR_2 = "correlation-key-expression-2";

  public static final String CORRELATION_KEY_NAME_3 = "correlation-key-name-3";

  public static final String CORRELATION_KEY_EXPR_3 = "correlation-key-expression-3";

  public static final String CORRELATION_KEY_NAME_4 = "correlation-key-name-4";

  public static final String CORRELATION_KEY_EXPR_4 = "correlation-key-expression-4";

  public static final String CORRELATION_KEY_NAME_5 = "correlation-key-name-5";

  public static final String CORRELATION_KEY_EXPR_5 = "correlation-key-expression-5";

}
