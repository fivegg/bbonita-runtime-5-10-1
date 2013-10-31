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
package org.ow2.bonita.parsing.xpdl.binding;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ow2.bonita.definition.VariablePerformerAssign;
import org.ow2.bonita.facade.def.element.HookDefinition;
import org.ow2.bonita.facade.def.element.HookDefinition.Event;
import org.ow2.bonita.facade.def.majorElement.ActivityDefinition.JoinType;
import org.ow2.bonita.facade.def.majorElement.ActivityDefinition.SplitType;
import org.ow2.bonita.util.ExceptionManager;
import org.ow2.bonita.util.GroovyExpression;
import org.ow2.bonita.util.ProcessBuilder;
import org.ow2.bonita.util.xml.Parse;
import org.ow2.bonita.util.xml.Parser;
import org.ow2.bonita.util.xml.XmlUtil;
import org.w3c.dom.Element;

/**
 * @author Marc Blachon, Guillaume Porcher, Charles Souillard, Miguel Valdes, Pierre Vigneras
 */
public class ActivityBinding extends MajorElementBinding {

  private static final Logger LOG = Logger.getLogger(ActivityBinding.class.getName());

  public ActivityBinding() {
    super("Activity");
  }

  @Override
  @SuppressWarnings("unchecked")
  public Object parse(final Element activityElement, final Parse parse, final Parser parser) {
    if (ActivityBinding.LOG.isLoggable(Level.FINE)) {
      ActivityBinding.LOG.fine("parsing element = " + activityElement);
    }
    final String id = getId(activityElement);

    final ProcessBuilder processBuilder = parse.findObject(ProcessBuilder.class);
    final Collection<Element> activityDatafields = parse.findObject(Collection.class);

    final String limit = getChildTextContent(activityElement, "Limit");
    if (limit != null) {
      parse.addProblem("'Limit' element not yet supported on activity definition." + "Please remove it from activity: "
          + id);
      if ("".equals(limit.trim())) {
        parse.addProblem("Limit definition incorrect: no value specified in element 'Limit' for activity: " + id);
      }
    }

    Element subFlowElement = null;
    boolean noImplementation = false;
    final Element implementationElement = XmlUtil.element(activityElement, "Implementation");
    if (implementationElement != null) {
      noImplementation = XmlUtil.element(implementationElement, "No") != null;
      subFlowElement = XmlUtil.element(implementationElement, "SubFlow");
    }

    String startMode = "Automatic";
    final Element startModeElement = XmlUtil.element(activityElement, "StartMode");
    if (startModeElement != null) {
      final Element startModeFirstChildElement = XmlUtil.element(startModeElement);
      startMode = startModeFirstChildElement.getLocalName();
    }
    String finishMode = null;
    final Element finishModeElement = XmlUtil.element(activityElement, "FinishMode");
    if (finishModeElement != null) {
      final Element finishModeFirstChildElement = XmlUtil.element(finishModeElement);
      finishMode = finishModeFirstChildElement.getLocalName();
    } else {
      if ("Automatic".equals(startMode)) {
        finishMode = "Automatic";
      } else {
        finishMode = "Manual";
      }
    }
    final String performer = getChildTextContent(activityElement, "Performer");
    checkStartAndFinishMode(startMode, finishMode, performer, id, noImplementation, parse);
    final String priority = getChildTextContent(activityElement, "Priority");
    if (priority != null) {
      parse.addProblem("'Priority' element not yet supported on activity definition. Please remove it from activity: "
          + id);
    }

    final boolean route = XmlUtil.element(activityElement, "Route") != null;

    if (route) {
      processBuilder.addDecisionNode(id);
    } else if (subFlowElement != null) {
      final Element actualparametersElement = XmlUtil.element(subFlowElement, "ActualParameters");
      if (actualparametersElement != null) {
        parse.addWarning("Subflow ActualParameters not yet supported.");
      }

      final String subFlowId = XmlUtil.attribute(subFlowElement, "Id");
      final String execution = XmlUtil.attribute(subFlowElement, "Execution");
      if (execution != null && "Asyncr".equals(execution)) {
        parse.addProblem("Asyncr SubFlow is not supported");
      }
      processBuilder.addSubProcess(id, subFlowId);
    } else if ("Manual".equals(startMode)) {
      processBuilder.addHumanTask(id, performer);
    } else {
      processBuilder.addSystemTask(id);
    }

    final String description = getChildTextContent(activityElement, "Description");
    processBuilder.addDescription(description);
    processBuilder.addLabel(XmlUtil.attribute(activityElement, "Name"));

    boolean isAsync = false;
    final Element asyncElement = getExtendedAttribute(activityElement, "Async");
    if (asyncElement != null) {
      isAsync = Boolean.valueOf(XmlUtil.attribute(asyncElement, "Value"));
    }
    if (isAsync) {
      processBuilder.asynchronous();
    }

    parseDeadlines(activityElement, parse, processBuilder);

    SplitType splitType = null;
    JoinType joinType = JoinType.AND;
    final Element transitionRestrictionsElement = XmlUtil.element(activityElement, "TransitionRestrictions");
    if (transitionRestrictionsElement != null) {
      final List<Element> transitionRestrictionElements = XmlUtil.elements(transitionRestrictionsElement,
          "TransitionRestriction");
      if (transitionRestrictionElements != null) {
        for (final Element transitionrestrictionElement : transitionRestrictionElements) {
          final Element joinElement = XmlUtil.element(transitionrestrictionElement, "Join");
          if (joinElement != null) {
            joinType = this.getEnumValue(JoinType.class, XmlUtil.attribute(joinElement, "Type"), null);
            if (joinType == null) {
              parse.addProblem("Mandatory type attribute is not specified on join element");
            }
          }
          final Element splitElement = XmlUtil.element(transitionrestrictionElement, "Split");
          if (splitElement != null) {
            splitType = this.getEnumValue(SplitType.class, XmlUtil.attribute(splitElement, "Type"), null);
            if (splitType == null) {
              parse.addProblem("Mandatory type attribute is not specified on Split element!");
            }
            processBuilder.addSplitType(splitType);
          }
          break;
        }
      }
    }
    processBuilder.addJoinType(joinType);
    // parse hooks
    final boolean isAutomaticActivity = "Automatic".equals(startMode);
    parseHooks(activityElement, parse, isAutomaticActivity, processBuilder);
    // parse performer asign
    parsePerformerAssign(activityElement, parse, processBuilder);
    // parse datafields
    parseDataFields(activityElement, parse, processBuilder, activityDatafields, parser);
    // parse multi instantiation
    parseMultiInstantiationDefinition(activityElement, parse, route, processBuilder);
    return null;
  }

  /**
   * Parse multi instantiation info from the activity element.
   * 
   * @param activityElement
   * @param parse
   * @return
   */
  private void parseMultiInstantiationDefinition(final Element activityElement, final Parse parse,
      final boolean isRoute, final ProcessBuilder processuilder) {
    final Element activityInstantiatorElement = getExtendedAttribute(activityElement, "MultiInstantiation");
    if (activityInstantiatorElement != null) {
      final String className = getChildTextContent(activityInstantiatorElement, "MultiInstantiator");
      if (className == null) {
        parse.addProblem("MultiInstantiation needs to specify a MultiInstantiator class name "
            + "(in a nested MultiInstantiator element)");
      }
      final String variableId = getChildTextContent(activityInstantiatorElement, "Variable");
      if (variableId == null) {
        parse.addProblem("MultiInstantiation needs to specify a variable id " + "(in a nested Variable element)");
      }
      if (className != null && variableId != null) {
        if (isRoute) {
          parse.addProblem("Multi Instantiation cannot be defined on a Route activity");
        } else {
          processuilder.addMultiInstanciation(variableId, className);
          // parameters always null ?
        }
      }
    }
  }

  protected void parsePerformerAssign(final Element activityElement, final Parse parse,
      final ProcessBuilder processBuilder) {
    final Element performerAssignElement = getExtendedAttribute(activityElement, "PerformerAssign");
    if (performerAssignElement != null) {
      final String value = XmlUtil.attribute(performerAssignElement, "Value");
      String className = null;
      Map<String, Object[]> parameters = null;
      if ("Callback".equals(value)) {
        final Element callbackElement = XmlUtil.element(performerAssignElement, "Callback");
        className = callbackElement.getTextContent();
      } else if ("Custom".equals(value)) {
        final Element callbackElement = XmlUtil.element(performerAssignElement, "Custom");
        className = callbackElement.getTextContent();
      } else if ("Variable".equals(value)) {
        final Element propertyElement = XmlUtil.element(performerAssignElement, "Variable");
        parameters = new HashMap<String, Object[]>();
        parameters.put("variableId", new Object[] { propertyElement.getTextContent() });
        className = VariablePerformerAssign.class.getName();
      } else {
        parse.addProblem("Unsupported value on extendedAttribute PerformerAssign: " + value);
      }
      processBuilder.addFilter(className);
      if (parameters != null) {
        for (final Entry<String, Object[]> parameter : parameters.entrySet()) {
          final String key = parameter.getKey();
          if (GroovyExpression.isGroovyExpression(key)) {
            processBuilder.addOutputParameter(key, (String) parameter.getValue()[0]);
          } else {
            processBuilder.addInputParameter(key, parameter.getValue());
          }
        }
      }
    }
  }

  private void checkStartAndFinishMode(final String startMode, final String finishMode, final String performer,
      final String activityId, final boolean isNoImpl, final Parse parse) {
    if ("Manual".equals(finishMode) && "Automatic".equals(startMode) || "Automatic".equals(finishMode)
        && "Manual".equals(startMode)) {
      parse.addProblem("StartMode and FinishMode have different values: this feature is not yet supported.");
    }
    final boolean hasManualMode = "Manual".equals(startMode) || "Manual".equals(finishMode);
    if (hasManualMode) {
      if (performer == null) {
        parse
            .addProblem("StartMode or FinishMode is Manual and no performer is specified on activity processDefinitionUUID = "
                + activityId + "! Please specify one.");
      }
      if (!isNoImpl) {
        parse.addProblem("StartMode or FinishMode is Manual and activity implementation is not No:"
            + "this feature is not yet supported.");
      }
    }
  }

  protected void parseDataFields(final Element activityElement, final Parse parse, final ProcessBuilder processBuilder,
      final Collection<Element> activityDatafields, final Parser parser) {
    final Set<Element> propertyElements = getExtendedAttributes(activityElement, "property");
    if (propertyElements != null && !propertyElements.isEmpty()) {
      if (activityDatafields == null) {
        parse.addProblem("No activity dataFields is defined within the process.");
      } else {
        for (final Element propertyElement : propertyElements) {
          final String dataFieldId = XmlUtil.attribute(propertyElement, "Value");
          boolean found = false;
          for (final Element datafieldElement : activityDatafields) {
            final String id = getId(datafieldElement);
            if (id.equals(dataFieldId)) {
              parser.parseElement(datafieldElement, parse, "majorElements");
              found = true;
              break;
            }
          }
          if (!found) {
            parse.addProblem("Looking for a datafield with id: " + dataFieldId
                + " in enclosing process but unable to find it.");
          }
          final String propagatedValue = getChildTextContent(propertyElement, "Propagated");
          if (propagatedValue != null
              && !("no".equalsIgnoreCase(propagatedValue) || "false".equalsIgnoreCase(propagatedValue))) {
            parse.addProblem("Propagated value not supported: " + propagatedValue + ". Use instance variables instead."
                + "(Only 'no' or 'false' are supported for backward compatibility.)");
          }
        }
      }
    }
  }

  protected void parseHooks(final Element activityElement, final Parse parse, final boolean isAutomatic,
      final ProcessBuilder processBuilder) {
    final Set<Element> hookElements = getExtendedAttributes(activityElement, "hook");
    if (hookElements != null) {
      for (final Element hookElement : hookElements) {
        final String className = XmlUtil.attribute(hookElement, "Value");
        final String hookEventName = getChildTextContent(hookElement, "HookEventName");
        final Map<String, Object[]> parameters = getHookParameters(hookElement, parse, className);
        if (hookEventName == null) {
          parse.addProblem("hook ExtendedAttribute needs an element child called HookEventName");
          return;
        }
        HookDefinition.Event event = null;
        boolean throwingException = false;

        final String rollBackFlag = getChildTextContent(hookElement, "Rollback");
        // We can't use Boolean's class methods since in their case,
        // true == "true" and false == !true (anything different to "true")
        // In our case, we really want either "true" or "false". Any other string
        // is simply an invalid flag.
        throwingException = false;
        if ("true".equals(rollBackFlag)) {
          throwingException = true;
        }
        try {
          event = getEventFromString(hookEventName);
        } catch (final IllegalArgumentException iae) {
          parse.addProblem("Unsupported HookEventName: " + hookEventName + " for "
              + (isAutomatic ? "automatic" : "manual") + " activity.");
        }
        if (event != null) {
          processBuilder.addConnector(event, className, throwingException);
          if (parameters != null) {
            for (final Entry<String, Object[]> parameter : parameters.entrySet()) {
              final String key = parameter.getKey();
              if (GroovyExpression.isGroovyExpression(key)) {
                processBuilder.addOutputParameter(key, (String) parameter.getValue()[0]);
              } else {
                processBuilder.addInputParameter(key, parameter.getValue());
              }
            }
          }
        }
      }
    }
  }

  private HookDefinition.Event getEventFromString(final String hookEventName) {
    if ("task:onReady".equals(hookEventName)) {
      return Event.taskOnReady;
    } else if ("task:onStart".equals(hookEventName)) {
      return Event.taskOnStart;
    } else if ("task:onFinish".equals(hookEventName)) {
      return Event.taskOnFinish;
    } else if ("task:onSuspend".equals(hookEventName)) {
      return Event.taskOnSuspend;
    } else if ("task:onResume".equals(hookEventName)) {
      return Event.taskOnResume;
    } else if ("task:onCancel".equals(hookEventName)) {
      return Event.taskOnCancel;
    } else if ("automatic:onEnter".equals(hookEventName)) {
      return Event.automaticOnEnter;
    } else {
      final String message = ExceptionManager.getInstance().getFullMessage("buc_M_11",
          HookDefinition.Event.class.getName(), hookEventName);
      throw new IllegalArgumentException(message);
    }
  }

  private Map<String, Object[]> getHookParameters(final Element hookElement, final Parse parse, final String className) {
    final Map<String, Object[]> hookParameters = new HashMap<String, Object[]>();
    final Element hookParametersElement = XmlUtil.element(hookElement, "Parameters");
    if (hookParametersElement != null) {
      final List<Element> parameters = XmlUtil.elements(hookParametersElement);
      for (final Element param : parameters) {
        final String paramType = param.getLocalName();
        final String variable = param.getAttribute("Var");
        String method;
        if ("InParameter".equals(paramType)) {
          if (param.hasAttribute("Setter")) {
            method = param.getAttribute("Setter");
            if (method.length() != 0) {
              hookParameters.put(method, new Object[] { variable });
            } else {
              parse.addProblem("The Setter attribute should not be empty!");
            }
          } else {
            parse.addProblem("InParameter need the Setter attribute");
          }
        } else if ("OutParameter".equals(paramType)) {
          if (param.hasAttribute("Getter")) {
            method = param.getAttribute("Getter");
            if (method.length() != 0) {
              hookParameters.put(method, new Object[] { variable });
            } else {
              parse.addProblem("The Getter attribute should not be empty!");
            }
          } else {
            parse.addProblem("OutParameter need the Getter attribute");
          }
        } else if ("Properties".equals(paramType)) {
          if (variable.startsWith("file:")) {
            hookParameters.put("file:", new Object[] { variable.substring(5) });
          } else if (variable.startsWith("bar:")) {
            hookParameters.put("bar:", new Object[] { variable.substring(4) });
          } else if (variable.startsWith("resource:")) {
            hookParameters.put("resource:", new Object[] { variable.substring(9) });
          } else {
            parse.addProblem("The value of Var attribute can only be either "
                + "file:<absolute_file_path> or bar:<file_path_in_bar_file> or" + "resource:<resource_path>");
          }
        } // ignores all other tags
      }
    }
    return hookParameters;
  }

  protected void parseDeadlines(final Element activityElement, final Parse parse, final ProcessBuilder processBuilder) {
    final List<Element> deadlineElements = XmlUtil.elements(activityElement, "Deadline");

    if (deadlineElements != null) {
      if (ActivityBinding.LOG.isLoggable(Level.WARNING)) {
        ActivityBinding.LOG.warning("Bonita use of deadlines is specific: refer to bonita User Guide for more details");
      }
      for (final Element deadlineElement : deadlineElements) {
        // schema validation ensures that deadlineCondtion and ExceptionName
        // are specified once and only once
        final String deadlineCondition = getChildTextContent(deadlineElement, "DeadlineCondition");
        if (deadlineCondition == null || "".equals(deadlineCondition)) {
          parse.addProblem("DeadlineCondition element is not specified on deadline element");
        }
        final String exceptionName = getChildTextContent(deadlineElement, "ExceptionName");
        if (exceptionName == null) {
          parse.addProblem("ExceptionName element is not specified on deadline element");
        }
        processBuilder.addDeadline(deadlineCondition, exceptionName);
      }
    }
  }

}
