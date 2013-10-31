/**
 * Copyright (C) 2009-2013 BonitaSoft S.A.
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
package org.ow2.bonita.util;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Stack;

import org.ow2.bonita.building.XmlDefExporter;
import org.ow2.bonita.connector.core.Connector;
import org.ow2.bonita.connector.core.RoleResolver;
import org.ow2.bonita.facade.def.element.AttachmentDefinition;
import org.ow2.bonita.facade.def.element.BoundaryEvent;
import org.ow2.bonita.facade.def.element.FilterDefinition;
import org.ow2.bonita.facade.def.element.HookDefinition.Event;
import org.ow2.bonita.facade.def.element.IncomingEventDefinition;
import org.ow2.bonita.facade.def.element.MultiInstantiationDefinition;
import org.ow2.bonita.facade.def.element.RoleMapperDefinition;
import org.ow2.bonita.facade.def.element.impl.AttachmentDefinitionImpl;
import org.ow2.bonita.facade.def.element.impl.ConnectorDefinitionImpl;
import org.ow2.bonita.facade.def.element.impl.ErrorBoundaryEventImpl;
import org.ow2.bonita.facade.def.element.impl.EventDefinitionImpl;
import org.ow2.bonita.facade.def.element.impl.IncomingEventDefinitionImpl;
import org.ow2.bonita.facade.def.element.impl.MessageBoundaryEventImpl;
import org.ow2.bonita.facade.def.element.impl.OutgoingEventDefinitionImpl;
import org.ow2.bonita.facade.def.element.impl.SignalBoundaryEventImpl;
import org.ow2.bonita.facade.def.element.impl.SubflowParameterDefinitionImpl;
import org.ow2.bonita.facade.def.element.impl.TimerBoundaryEventImpl;
import org.ow2.bonita.facade.def.majorElement.ActivityDefinition;
import org.ow2.bonita.facade.def.majorElement.ActivityDefinition.JoinType;
import org.ow2.bonita.facade.def.majorElement.ActivityDefinition.SplitType;
import org.ow2.bonita.facade.def.majorElement.DataFieldDefinition;
import org.ow2.bonita.facade.def.majorElement.ParticipantDefinition;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.def.majorElement.TransitionDefinition;
import org.ow2.bonita.facade.def.majorElement.impl.ActivityDefinitionImpl;
import org.ow2.bonita.facade.def.majorElement.impl.DataFieldDefinitionImpl;
import org.ow2.bonita.facade.def.majorElement.impl.DescriptionElementImpl;
import org.ow2.bonita.facade.def.majorElement.impl.EventProcessDefinitionImpl;
import org.ow2.bonita.facade.def.majorElement.impl.NamedElementImpl;
import org.ow2.bonita.facade.def.majorElement.impl.ParticipantDefinitionImpl;
import org.ow2.bonita.facade.def.majorElement.impl.ProcessDefinitionImpl;
import org.ow2.bonita.facade.def.majorElement.impl.TransitionDefinitionImpl;
import org.ow2.bonita.light.LightProcessDefinition.ProcessType;
import org.ow2.bonita.parsing.def.XmlDefParser;
import org.ow2.bonita.parsing.xpdl.XpdlParser;
import org.ow2.bonita.util.xml.Parse;
import org.ow2.bonita.util.xml.Problem;
import org.w3c.dom.Document;

/**
 * A ProcessBuilder constructs a process by adding methods.
 * 
 * @author Matthieu Chaffotte, Charles Souillard, Elias Ricken de Medeiros
 * 
 */
public final class ProcessBuilder {

  private final Collection<Problem> problems;

  private final Stack<Object> stack;

  private OutgoingEventDefinitionImpl outgoingEvent;

  /**
   * Creates a process definition with a unique name and a process version. In
   * order to get this process definition the done method should be called at
   * the end of the process build.
   * 
   * @param name
   *          the process name
   * @param version
   *          the process version
   * @return the ProcessBuilder in order to add BPM elements
   */
  public static ProcessBuilder createProcess(final String name, final String version) {
    return new ProcessBuilder(name, version);
  }

  /**
   * Creates a process definition using an XPDL file.
   * 
   * @param xpdlUrl
   *          the URL of the XPDL file
   * @return a process definition
   */
  public static ProcessDefinition createProcessFromXpdlFile(final URL xpdlUrl) {
    final Parse parse = new XpdlParser().createParse();
    parse.setUrl(xpdlUrl);
    final ProcessDefinition process = (ProcessDefinition) parse.execute().getDocumentObject();
    Misc.showProblems(parse.getProblems(), "xpdl file");
    Misc.badStateIfNull(process, "Ouch! The returned client process is null!");
    return process;
  }

  /**
   * Creates a process definition using an XML process definition file.
   * 
   * @param xmlDefUrl
   *          the URL of the XML process definition file
   * @return a process definition
   */
  public static ProcessDefinition createProcessFromXmlDefFile(final URL xmlDefUrl) {
    final Parse parse = new XmlDefParser().createParse();
    parse.setUrl(xmlDefUrl);
    final ProcessDefinition process = (ProcessDefinition) parse.execute().getDocumentObject();
    Misc.showProblems(parse.getProblems(), "xml file");
    Misc.badStateIfNull(process, "Ouch! The returned client process is null!");
    return process;
  }

  private ProcessBuilder(final String processame, final String processVersion) {
    problems = new ArrayList<Problem>();
    stack = new Stack<Object>();
    Misc.checkArgsNotNull(processame);
    ProcessDefinition process = null;
    if ("".equals(processame.trim())) {
      problems.add(new Problem("Process name is an empty string.", Problem.SEVERITY_ERROR));
    }
    if (processVersion == null || "".equals(processVersion.trim())) {
      process = new ProcessDefinitionImpl(processame, "1.0");
    } else {
      process = new ProcessDefinitionImpl(processame, processVersion);
    }
    push(process);
  }

  /**
   * Adds a description to the current BPM entity.
   * 
   * @param description
   *          the BPM entity description
   * @return the ProcessBuilder in order to add BPM elements
   */
  public ProcessBuilder addDescription(final String description) {
    final Object obj = peek(DescriptionElementImpl.class);
    if (isDescriptionElement(obj)) {
      ((DescriptionElementImpl) obj).setDescription(description);
    } else {
      problems.add(new Problem("Unable to set description " + description + " on an object of type: " + getClass(obj),
          Problem.SEVERITY_ERROR));
    }
    return this;
  }

  /**
   * Adds a label to the current BPM entity.
   * 
   * @param label
   *          the BPM label
   * @return the ProcessBuilder in order to add BPM elements
   */
  public ProcessBuilder addLabel(final String label) {
    final Object obj = peek(NamedElementImpl.class);
    if (isNamedElement(obj)) {
      ((NamedElementImpl) obj).setLabel(label);
    } else {
      problems.add(new Problem("Unable to set label " + label + " on an object of type: " + getClass(obj),
          Problem.SEVERITY_ERROR));
    }
    return this;
  }

  /**
   * Adds a Character variable to the current BPM entity. This entity must be
   * the process itself or a process activity.
   * 
   * @param dataName
   *          the variable name
   * @return the ProcessBuilder in order to add BPM elements
   */
  public ProcessBuilder addCharData(final String dataName) {
    return addCharData(dataName, (Character) null);
  }

  /**
   * Adds a Short variable to the current BPM entity. This entity must be the
   * process itself or a process activity.
   * 
   * @param dataName
   *          the variable name
   * @return the ProcessBuilder in order to add BPM elements
   */
  public ProcessBuilder addShortData(final String dataName) {
    return addShortData(dataName, (Short) null);
  }

  /**
   * Adds a Long variable to the current BPM entity. This entity must be the
   * process itself or a process activity.
   * 
   * @param dataName
   *          the variable name
   * @return the ProcessBuilder in order to add BPM elements
   */
  public ProcessBuilder addLongData(final String dataName) {
    return addLongData(dataName, (Long) null);
  }

  /**
   * Adds a Double variable to the current BPM entity. This entity must be the
   * process itself or a process activity.
   * 
   * @param dataName
   *          the variable name
   * @return the ProcessBuilder in order to add BPM elements
   */
  public ProcessBuilder addDoubleData(final String dataName) {
    return addDoubleData(dataName, (Double) null);
  }

  /**
   * Adds a Float variable to the current BPM entity. This entity must be the
   * process itself or a process activity.
   * 
   * @param dataName
   *          the variable name
   * @return the ProcessBuilder in order to add BPM elements
   */
  public ProcessBuilder addFloatData(final String dataName) {
    return addFloatData(dataName, (Float) null);
  }

  /**
   * Adds a Boolean variable to the current BPM entity. This entity must be the
   * process itself or a process activity.
   * 
   * @param dataName
   *          the variable name
   * @return the ProcessBuilder in order to add BPM elements
   */
  public ProcessBuilder addBooleanData(final String dataName) {
    return addBooleanData(dataName, (Boolean) null);
  }

  /**
   * Adds a Integer variable to the current BPM entity. This entity must be the
   * process itself or a process activity.
   * 
   * @param dataName
   *          the variable name
   * @return the ProcessBuilder in order to add BPM elements
   */
  public ProcessBuilder addIntegerData(final String dataName) {
    return addIntegerData(dataName, (Integer) null);
  }

  /**
   * Adds a String variable to the current BPM entity. This entity must be the
   * process itself or a process activity.
   * 
   * @param dataName
   *          the variable name
   * @return the ProcessBuilder in order to add BPM elements
   */
  public ProcessBuilder addStringData(final String dataName) {
    return addStringData(dataName, null);
  }

  /**
   * Adds a XML variable to the current BPM entity. This entity must be the
   * process itself or a process activity.
   * 
   * @param dataName
   *          the variable name
   * @return the ProcessBuilder in order to add BPM elements
   */
  public ProcessBuilder addXMLData(final String dataName) {
    return addXMLData(dataName, (Document) null);
  }

  /**
   * Adds a Date variable to the current BPM entity. This entity must be the
   * process itself or a process activity.
   * 
   * @param dataName
   *          the variable name
   * @return the ProcessBuilder in order to add BPM elements
   */
  public ProcessBuilder addDateData(final String dataName) {
    return addDateData(dataName, (Date) null);
  }

  /**
   * Adds a Character variable to the current BPM entity with an initial
   * variable. This entity must be the process itself or a process activity.
   * 
   * @param dataName
   *          the variable name
   * @param initialValue
   *          the character value
   * @return the ProcessBuilder in order to add BPM elements
   */
  public ProcessBuilder addCharData(final String dataName, final Character initialValue) {
    return addObjectData(dataName, Character.class.getName(), initialValue);
  }

  /**
   * Adds a Character variable to the current BPM entity. This entity must be
   * the process itself or a process activity. The scriptingValue
   * 
   * @param dataName
   *          the variable name
   * @param scriptingValue
   *          the Groovy expression as initial value
   * @return the ProcessBuilder in order to add BPM elements
   */
  public ProcessBuilder addCharData(final String dataName, final String scriptingValue) {
    return addObjectData(dataName, Character.class.getName(), scriptingValue);
  }

  /**
   * Adds a Short variable to the current BPM entity with an initial variable.
   * This entity must be the process itself or a process activity.
   * 
   * @param dataName
   *          the variable name
   * @param initialValue
   *          the short value
   * @return the ProcessBuilder in order to add BPM elements
   */
  public ProcessBuilder addShortData(final String dataName, final Short initialValue) {
    return addObjectData(dataName, Short.class.getName(), initialValue);
  }

  /**
   * Adds a Short variable to the current BPM entity with an initial variable.
   * This entity must be the process itself or a process activity.
   * 
   * @param dataName
   *          the variable name
   * @param scriptingValue
   *          the Groovy expression as initial value
   * @return the ProcessBuilder in order to add BPM elements
   */
  public ProcessBuilder addShortData(final String dataName, final String scriptingValue) {
    return addObjectData(dataName, Short.class.getName(), scriptingValue);
  }

  /**
   * Adds a Long variable to the current BPM entity with an initial variable.
   * This entity must be the process itself or a process activity.
   * 
   * @param dataName
   *          the variable name
   * @param initialValue
   *          the long value
   * @return the ProcessBuilder in order to add BPM elements
   */
  public ProcessBuilder addLongData(final String dataName, final Long initialValue) {
    return addObjectData(dataName, Long.class.getName(), initialValue);
  }

  /**
   * Adds a Long variable to the current BPM entity with an initial variable.
   * This entity must be the process itself or a process activity.
   * 
   * @param dataName
   *          the variable name
   * @param scriptingValue
   *          the Groovy expression as initial value
   * @return the ProcessBuilder in order to add BPM elements
   */
  public ProcessBuilder addLongData(final String dataName, final String scriptingValue) {
    return addObjectData(dataName, Long.class.getName(), scriptingValue);
  }

  /**
   * Adds a Double variable to the current BPM entity with an initial variable.
   * This entity must be the process itself or a process activity.
   * 
   * @param dataName
   *          the variable name
   * @param initialValue
   *          the double value
   * @return the ProcessBuilder in order to add BPM elements
   */
  public ProcessBuilder addDoubleData(final String dataName, final Double initialValue) {
    return addObjectData(dataName, Double.class.getName(), initialValue);
  }

  /**
   * Adds a Double variable to the current BPM entity with an initial variable.
   * This entity must be the process itself or a process activity.
   * 
   * @param dataName
   *          the variable name
   * @param scriptingValue
   *          the Groovy expression as initial value
   * @return the ProcessBuilder in order to add BPM elements
   */
  public ProcessBuilder addDoubleData(final String dataName, final String scriptingValue) {
    return addObjectData(dataName, Double.class.getName(), scriptingValue);
  }

  /**
   * Adds a Float variable to the current BPM entity with an initial variable.
   * This entity must be the process itself or a process activity.
   * 
   * @param dataName
   *          the variable name
   * @param initialValue
   *          the float value
   * @return the ProcessBuilder in order to add BPM elements
   */
  public ProcessBuilder addFloatData(final String dataName, final Float initialValue) {
    return addObjectData(dataName, Float.class.getName(), initialValue);
  }

  /**
   * Adds a Float variable to the current BPM entity with an initial variable.
   * This entity must be the process itself or a process activity.
   * 
   * @param dataName
   *          the variable name
   * @param scriptingValue
   *          the Groovy expression as initial value
   * @return the ProcessBuilder in order to add BPM elements
   */
  public ProcessBuilder addFloatData(final String dataName, final String scriptingValue) {
    return addObjectData(dataName, Float.class.getName(), scriptingValue);
  }

  /**
   * Adds a Boolean variable to the current BPM entity with an initial variable.
   * This entity must be the process itself or a process activity.
   * 
   * @param dataName
   *          the variable name
   * @param initialValue
   *          the boolean value
   * @return the ProcessBuilder in order to add BPM elements
   */
  public ProcessBuilder addBooleanData(final String dataName, final Boolean initialValue) {
    return addObjectData(dataName, Boolean.class.getName(), initialValue);
  }

  /**
   * Adds a Boolean variable to the current BPM entity with an initial variable.
   * This entity must be the process itself or a process activity.
   * 
   * @param dataName
   *          the variable name
   * @param scriptingValue
   *          the Groovy expression as initial value
   * @return the ProcessBuilder in order to add BPM elements
   */
  public ProcessBuilder addBooleanData(final String dataName, final String scriptingValue) {
    return addObjectData(dataName, Boolean.class.getName(), scriptingValue);
  }

  /**
   * Adds an Integer variable to the current BPM entity with an initial
   * variable. This entity must be the process itself or a process activity.
   * 
   * @param dataName
   *          the variable name
   * @param initialValue
   *          the integer value
   * @return the ProcessBuilder in order to add BPM elements
   */
  public ProcessBuilder addIntegerData(final String dataName, final Integer initialValue) {
    return addObjectData(dataName, Integer.class.getName(), initialValue);
  }

  /**
   * Adds an Integer variable to the current BPM entity with an initial
   * variable. This entity must be the process itself or a process activity.
   * 
   * @param dataName
   *          the variable name
   * @param scriptingValue
   *          the Groovy expression as initial value
   * @return the ProcessBuilder in order to add BPM elements
   */
  public ProcessBuilder addIntegerData(final String dataName, final String scriptingValue) {
    return addObjectData(dataName, Integer.class.getName(), scriptingValue);
  }

  /**
   * Adds a String variable to the current BPM entity with an initial variable.
   * This entity must be the process itself or a process activity.
   * 
   * @param dataName
   *          the variable name
   * @param initialValue
   *          the string value
   * @return the ProcessBuilder in order to add BPM elements
   */
  public ProcessBuilder addStringData(final String dataName, final String initialValue) {
    return addObjectData(dataName, String.class.getName(), (Object) initialValue);
  }

  /**
   * Adds a XML variable to the current BPM entity with an initial variable.
   * This entity must be the process itself or a process activity.
   * 
   * @param dataName
   *          the variable name
   * @param initialValue
   *          the string value
   * @return the ProcessBuilder in order to add BPM elements
   */
  public ProcessBuilder addXMLData(final String dataName, final String initialValue) {
    Document doc = null;
    if (initialValue != null) {
      try {
        doc = Misc.generateDocument(initialValue);
      } catch (final Exception e) {
        throw new BonitaRuntimeException("Unable to generate Document from String: " + initialValue);
      }
    }
    return addObjectData(dataName, Document.class.getName(), doc);
  }

  /**
   * Adds a XML variable to the current BPM entity with an initial variable
   * based on a script. This entity must be the process itself or a process
   * activity.
   * 
   * @param dataName
   *          the variable name
   * @param scriptValue
   *          the script value. This script must generate a text representing an
   *          XML
   * @return the ProcessBuilder in order to add BPM elements
   */
  public ProcessBuilder addXMLDataFromScript(final String dataName, final String scriptValue) {
    return addObjectData(dataName, Document.class.getName(), scriptValue);
  }

  /**
   * Adds a XML variable to the current BPM entity with an initial variable.
   * This entity must be the process itself or a process activity.
   * 
   * @param dataName
   *          the variable name
   * @param initialValue
   *          the string value
   * @return the ProcessBuilder in order to add BPM elements
   */
  public ProcessBuilder addXMLData(final String dataName, final Document initialValue) {
    return addObjectData(dataName, Document.class.getName(), initialValue);
  }

  /**
   * Adds a String variable to the current BPM entity with an initial variable.
   * This entity must be the process itself or a process activity.
   * 
   * @param dataName
   *          the variable name
   * @param scriptingValue
   *          the Groovy expression as initial value
   * @return the ProcessBuilder in order to add BPM elements
   */
  public ProcessBuilder addStringDataFromScript(final String dataName, final String scriptingValue) {
    return addObjectData(dataName, String.class.getName(), scriptingValue);
  }

  /**
   * Adds a Date variable to the current BPM entity with an initial variable.
   * This entity must be the process itself or a process activity.
   * 
   * @param dataName
   *          the variable name
   * @param initialValue
   *          the date value
   * @return the ProcessBuilder in order to add BPM elements
   */
  public ProcessBuilder addDateData(final String dataName, final Date initialValue) {
    return addObjectData(dataName, Date.class.getName(), initialValue);
  }

  /**
   * Adds a Date variable to the current BPM entity with an initial variable.
   * This entity must be the process itself or a process activity.
   * 
   * @param dataName
   *          the variable name
   * @param scriptingValue
   *          the Groovy expression as initial value
   * @return the ProcessBuilder in order to add BPM elements
   */
  public ProcessBuilder addDateData(final String dataName, final String scriptingValue) {
    return addObjectData(dataName, Date.class.getName(), scriptingValue);
  }

  /**
   * Adds an Enumeration variable to the current BPM entity with an initial
   * variable. This entity must be the process itself or a process activity.
   * 
   * @param dataName
   *          the variable name
   * @param enumeariontValues
   *          the value of each enumeration member
   * @param initialValue
   *          the initial enumeration value
   * @return the ProcessBuilder in order to add BPM elements
   */
  public ProcessBuilder addEnumData(final String dataName, final Set<String> enumeariontValues,
      final String initialValue) {
    return addObjectData(dataName, String.class.getName(), initialValue, null, enumeariontValues);
  }

  /**
   * Adds an Enumeration variable to the current BPM entity with an initial
   * variable. This entity must be the process itself or a process activity.
   * 
   * @param dataName
   *          the variable name
   * @param scriptingValue
   *          the Groovy expression as initial value
   * @param enumeariontValues
   *          the value of each enumeration member
   * @return the ProcessBuilder in order to add BPM elements
   */
  public ProcessBuilder addEnumData(final String dataName, final String scriptingValue,
      final Set<String> enumeariontValues) {
    return addObjectData(dataName, String.class.getName(), null, scriptingValue, enumeariontValues);
  }

  /**
   * Adds an attachment on a process.
   * 
   * @param name
   *          the attachment name
   * @return the ProcessBuilder in order to add BPM elements
   */
  public ProcessBuilder addAttachment(final String name) {
    return addAttachment(name, null, null);
  }

  /**
   * Adds an attachment on a process.
   * 
   * @param name
   *          the attachment name
   * @param filePath
   *          the attachment file path
   * @return the ProcessBuilder in order to add BPM elements
   */
  public ProcessBuilder addAttachment(final String name, final String filePath) {
    return addAttachment(name, filePath, null);
  }

  /**
   * Adds an attachment on a process.
   * 
   * @param name
   *          the attachment name
   * @param filePath
   *          the attachment file path
   * @param fileName
   *          the attachment file name
   * @return the ProcessBuilder in order to add BPM elements
   */
  public ProcessBuilder addAttachment(final String name, final String filePath, final String fileName) {
    Misc.checkArgsNotNull(name);
    if ("".equals(name.trim())) {
      problems.add(new Problem("Attachment name is an empty string.", Problem.SEVERITY_ERROR));
    }
    final Object obj = peek(ProcessDefinitionImpl.class);
    if (isProcess(obj)) {
      final ProcessDefinitionImpl process = (ProcessDefinitionImpl) obj;
      final AttachmentDefinitionImpl attachment = new AttachmentDefinitionImpl(process.getUUID(), name);
      attachment.setFilePath(filePath);
      attachment.setFileName(fileName);
      process.addAttachment(attachment);
      push(attachment);
    }
    return this;
  }

  private ProcessBuilder addObjectData(final String dataName, final String dataTypeClassName,
      final Object initialValue, final String scriptingValue, final Set<String> enumerationValues) {
    Misc.checkArgsNotNull(dataName, dataTypeClassName);
    if ("".equals(dataName.trim())) {
      problems.add(new Problem("Data name is an empty string.", Problem.SEVERITY_ERROR));
    }
    if ("".equals(dataTypeClassName.trim())) {
      problems.add(new Problem("Data type class name is an empty string.", Problem.SEVERITY_ERROR));
    }
    if (initialValue != null && scriptingValue != null) {
      problems.add(new Problem("Either the initial value or the scripting value must be set.", Problem.SEVERITY_ERROR));
    }
    if (scriptingValue != null && !Misc.isJustAGroovyExpression(scriptingValue)) {
      problems.add(new Problem("The scripting value must be a groovy expression.", Problem.SEVERITY_ERROR));
    }
    final Object obj = peek(ActivityDefinitionImpl.class);
    DataFieldDefinitionImpl data = null;
    if (isActivity(obj)) {
      final ActivityDefinitionImpl activity = (ActivityDefinitionImpl) obj;
      data = new DataFieldDefinitionImpl(activity.getProcessDefinitionUUID(), activity.getUUID(), dataName,
          dataTypeClassName);
      activity.addData(data);
      push(data);
    } else if (isProcess(obj)) {
      final ProcessDefinitionImpl process = (ProcessDefinitionImpl) obj;
      data = new DataFieldDefinitionImpl(process.getUUID(), dataName, dataTypeClassName);
      process.addData(data);
      push(data);
    }

    if (initialValue != null) {
      if (!(initialValue instanceof Serializable)) {
        throw new BonitaRuntimeException("Unable to store initialValue for data: " + dataTypeClassName
            + ". The given initialValue must be serializable.");
      }
      Class<?> clazz = null;
      try {
        clazz = Thread.currentThread().getContextClassLoader().loadClass(dataTypeClassName);
      } catch (final ClassNotFoundException e) {
        throw new BonitaRuntimeException("Unable to load data type class: " + dataTypeClassName);
      }
      if (!clazz.isAssignableFrom(initialValue.getClass())) {
        throw new BonitaRuntimeException("Unable to store initialValue for data: " + dataTypeClassName
            + ". The given initialValue does not correspond to the given data type className.");
      }
    }
    data.setInitialValue((Serializable) initialValue);
    data.setEnumerationValues(enumerationValues);
    data.setScriptingValue(scriptingValue);
    return this;
  }

  /**
   * Adds an Object variable to the current BPM entity with an initial variable.
   * This entity must be the process itself or a process activity.
   * 
   * @param dataName
   *          the variable name
   * @param dataTypeClassName
   *          the data type class name
   * @return the ProcessBuilder in order to add BPM elements
   */
  public ProcessBuilder addObjectData(final String dataName, final String dataTypeClassName) {
    return addObjectData(dataName, dataTypeClassName, null, null, null);
  }

  /**
   * Adds an Object variable to the current BPM entity with an initial variable.
   * This entity must be the process itself or a process activity.
   * 
   * @param dataName
   *          the variable name
   * @param dataTypeClassName
   *          the variable type class name
   * @param initialValue
   *          the initial value of the variable
   * @return the ProcessBuilder in order to add BPM elements
   */
  public ProcessBuilder addObjectData(final String dataName, final String dataTypeClassName, final Object initialValue) {
    return addObjectData(dataName, dataTypeClassName, initialValue, null, null);
  }

  /**
   * Adds an Object variable to the current BPM entity with an initial variable.
   * This entity must be the process itself or a process activity.
   * 
   * @param dataName
   *          the variable name
   * @param dataTypeClassName
   *          the variable type class name
   * @param scriptingValue
   * @return the ProcessBuilder in order to add BPM elements
   */
  public ProcessBuilder addObjectData(final String dataName, final String dataTypeClassName, final String scriptingValue) {
    return addObjectData(dataName, dataTypeClassName, null, scriptingValue, null);
  }

  /**
   * Adds a group.
   * 
   * @param groupName
   *          the group name
   * @return the ProcessBuilder in order to add BPM elements
   */
  public ProcessBuilder addGroup(final String groupName) {
    Misc.checkArgsNotNull(groupName);
    if ("".equals(groupName.trim())) {
      problems.add(new Problem("Group name is an empty string.", Problem.SEVERITY_ERROR));
    }

    final Object obj = peek(ProcessDefinitionImpl.class);
    if (isProcess(obj)) {
      final ProcessDefinitionImpl process = (ProcessDefinitionImpl) obj;
      final ParticipantDefinitionImpl group = new ParticipantDefinitionImpl(process.getUUID(), groupName);
      process.addGroup(group);
      push(group);
    } else {
      problems.add(new Problem("Unable to add group " + groupName + " on an object of type: " + getClass(obj),
          Problem.SEVERITY_ERROR));
    }
    return this;
  }

  /**
   * Adds a {@link RoleResolver} to a group.
   * 
   * @param groupResolverClassName
   *          the {@link RoleResolver} class name
   * @return the ProcessBuilder in order to add BPM elements
   */
  public ProcessBuilder addGroupResolver(final String groupResolverClassName) {
    Misc.checkArgsNotNull(groupResolverClassName);
    if ("".equals(groupResolverClassName.trim())) {
      problems.add(new Problem("Resolver class name is an empty string.", Problem.SEVERITY_ERROR));
    }

    final Object obj = peek(ParticipantDefinitionImpl.class);
    if (isParticipant(obj)) {
      final ParticipantDefinitionImpl group = (ParticipantDefinitionImpl) obj;
      final RoleMapperDefinition resolver = new ConnectorDefinitionImpl(groupResolverClassName);
      group.setResolver(resolver);
      final ProcessDefinitionImpl process = getProcess();
      process.addGroup(group);
      push(group);
      push(resolver);
    } else {
      problems.add(new Problem("Unable to add resolver " + groupResolverClassName + " on an object of type: "
          + getClass(obj), Problem.SEVERITY_ERROR));
    }
    return this;
  }

  /**
   * Adds a human to a process.
   * 
   * @param humanName
   *          the human name
   * @return the ProcessBuilder in order to add BPM elements
   */
  public ProcessBuilder addHuman(final String humanName) {
    Misc.checkArgsNotNull(humanName);
    if ("".equals(humanName.trim())) {
      problems.add(new Problem("Human name is an empty string.", Problem.SEVERITY_ERROR));
    }
    final Object obj = peek(ProcessDefinitionImpl.class);
    if (isProcess(obj)) {
      final ProcessDefinitionImpl process = (ProcessDefinitionImpl) obj;
      final ParticipantDefinitionImpl group = new ParticipantDefinitionImpl(process.getUUID(), humanName);
      process.addGroup(group);
      push(group);
    } else {
      problems.add(new Problem("Unable to add human " + humanName + " on an object of type: " + getClass(obj),
          Problem.SEVERITY_ERROR));
    }
    return this;
  }

  /**
   * Defines that the current process is an event sub-process.
   * 
   * @return the ProcessBuilder in order to add BPM elements
   */
  public ProcessBuilder setEventSubProcess() {
    final Object obj = peek(ProcessDefinitionImpl.class);
    if (isProcess(obj)) {
      final ProcessDefinitionImpl process = (ProcessDefinitionImpl) obj;
      process.setType(ProcessType.EVENT_SUB_PROCESS);
    } else {
      problems.add(new Problem("Unable to define that the process is a sub-process on an object of type: "
          + getClass(obj), Problem.SEVERITY_ERROR));
    }
    return this;
  }

  public ProcessBuilder setTransient() {
    final Object obj = peek(DataFieldDefinitionImpl.class);
    if (isDataFieldDefinition(obj)) {
      final DataFieldDefinitionImpl data = (DataFieldDefinitionImpl) obj;
      data.setTransient(true);
    } else {
      problems.add(new Problem("Unable to define as transient an object of type: " + getClass(obj),
          Problem.SEVERITY_ERROR));
    }
    return this;
  }

  /**
   * Adds a system task. By default The join type is XOR and the split types is
   * AND.
   * 
   * @param taskName
   *          the system task name
   * @return the ProcessBuilder in order to add BPM elements
   */
  public ProcessBuilder addSystemTask(final String taskName) {
    Misc.checkArgsNotNull(taskName);
    if ("".equals(taskName.trim())) {
      problems.add(new Problem("Task name is an empty string.", Problem.SEVERITY_ERROR));
    }
    final Object obj = peek(ProcessDefinitionImpl.class);
    if (isProcess(obj)) {
      final ProcessDefinitionImpl process = (ProcessDefinitionImpl) obj;
      final ActivityDefinitionImpl activity = ActivityDefinitionImpl.createAutomaticActivity(process.getUUID(),
          taskName);
      process.addActivity(activity);
      push(activity);
    } else {
      problems.add(new Problem("Unable to add system task " + taskName + " on an object of type: " + getClass(obj),
          Problem.SEVERITY_ERROR));
    }
    return this;
  }

  /**
   * Add a human task. By default The join type is XOR and the split types is
   * AND.
   * 
   * @param taskName
   *          the human task name
   * @param authorityNames
   *          the group name(s) or a user name(s)
   * @return the ProcessBuilder in order to add BPM elements
   */
  public ProcessBuilder addHumanTask(final String taskName, final String... authorityNames) {
    Misc.checkArgsNotNull(taskName, authorityNames);
    if ("".equals(taskName.trim())) {
      problems.add(new Problem("Task name is an empty string.", Problem.SEVERITY_ERROR));
    }
    for (final String authorityName : authorityNames) {
      if ("".equals(authorityName.trim())) {
        problems.add(new Problem("Authority name is an empty string.", Problem.SEVERITY_ERROR));
      }
    }
    final Object obj = peek(ProcessDefinitionImpl.class);
    if (isProcess(obj)) {
      final Set<String> groups = new HashSet<String>();
      for (final String authorityName : authorityNames) {
        groups.add(authorityName);
      }
      final ProcessDefinitionImpl process = (ProcessDefinitionImpl) obj;
      final ActivityDefinitionImpl activity = ActivityDefinitionImpl.createHumanActivity(process.getUUID(), taskName,
          groups);
      process.addActivity(activity);
      push(activity);
    } else {
      problems.add(new Problem("Unable to add human task " + taskName + " on an object of type: " + getClass(obj),
          Problem.SEVERITY_ERROR));
    }
    return this;
  }

  public ProcessBuilder addEventSubProcess(final String processName, final String version) {
    if ("".equals(processName.trim())) {
      problems.add(new Problem("Event sub-process name is an empty string.", Problem.SEVERITY_ERROR));
    }

    final Object obj = peek(ProcessDefinitionImpl.class);
    if (isProcess(obj)) {
      final ProcessDefinitionImpl process = (ProcessDefinitionImpl) obj;
      final EventProcessDefinitionImpl event = new EventProcessDefinitionImpl(processName, version);
      process.addEventSubProcess(event);
      push(event);
    } else {
      problems.add(new Problem("Unable to add an event sub process " + processName + " on an object of type: "
          + getClass(obj), Problem.SEVERITY_ERROR));
    }
    return this;
  }

  /**
   * Adds a sub-process. By default The join type is XOR and the split types is
   * AND.
   * 
   * @param taskName
   *          the task name
   * @param processName
   *          the sub-process name.
   * @return the ProcessBuilder in order to add BPM elements
   */
  public ProcessBuilder addSubProcess(final String taskName, final String processName) {
    return addSubProcess(taskName, processName, null);
  }

  /**
   * Adds a sub-process. By default The join type is XOR and the split types is
   * AND.
   * 
   * @param taskName
   *          the task name
   * @param processName
   *          the sub-process name.
   * @param version
   *          the sub-process version.
   * @return the ProcessBuilder in order to add BPM elements
   */
  public ProcessBuilder addSubProcess(final String taskName, final String processName, final String version) {
    Misc.checkArgsNotNull(taskName);
    if ("".equals(taskName.trim())) {
      problems.add(new Problem("Task name is an empty string.", Problem.SEVERITY_ERROR));
    }
    if ("".equals(processName.trim())) {
      problems.add(new Problem("Subprocess name is an empty string.", Problem.SEVERITY_ERROR));
    }

    final Object obj = peek(ProcessDefinitionImpl.class);
    if (isProcess(obj)) {
      final ProcessDefinitionImpl process = (ProcessDefinitionImpl) obj;
      final ActivityDefinitionImpl activity = ActivityDefinitionImpl.createSubflowActivity(process.getUUID(), taskName,
          processName, version);
      process.addActivity(activity);
      push(activity);
    } else {
      problems.add(new Problem("Unable to add sub process " + taskName + " on an object of type: " + getClass(obj),
          Problem.SEVERITY_ERROR));
    }
    return this;
  }

  /**
   * Adds an input parameter on the current sub-process entity.
   * 
   * @param parentProcessDatafieldName
   *          the parent process variable name
   * @param subProcessDatafieldName
   *          the sub-process variable name
   * @return the ProcessBuilder in order to add BPM elements
   */
  public ProcessBuilder addSubProcessInParameter(final String parentProcessDatafieldName,
      final String subProcessDatafieldName) {
    Misc.checkArgsNotNull(parentProcessDatafieldName, subProcessDatafieldName);
    if ("".equals(parentProcessDatafieldName.trim())) {
      problems.add(new Problem("Parent process datafield name is an empty string.", Problem.SEVERITY_ERROR));
    }
    if ("".equals(subProcessDatafieldName.trim())) {
      problems.add(new Problem("Sub process datafield name is an empty string.", Problem.SEVERITY_ERROR));
    }

    final Object obj = peek(ActivityDefinitionImpl.class);
    if (isActivity(obj)) {
      final ActivityDefinitionImpl activity = (ActivityDefinitionImpl) obj;
      activity.addSubflowInParameter(new SubflowParameterDefinitionImpl(parentProcessDatafieldName,
          subProcessDatafieldName));
    } else {
      problems.add(new Problem("Unable to add sub process parameter {" + parentProcessDatafieldName + ", "
          + subProcessDatafieldName + "} on an object of type: " + getClass(obj), Problem.SEVERITY_ERROR));
    }
    return this;
  }

  /**
   * Adds an output parameter on the current sub-process entity.
   * 
   * @param parentProcessDatafieldName
   *          the parent process variable name
   * @param subProcessDatafieldName
   *          the sub-process variable name
   * @return the ProcessBuilder in order to add BPM elements
   */
  public ProcessBuilder addSubProcessOutParameter(final String subProcessDatafieldName,
      final String parentProcessDatafieldName) {
    Misc.checkArgsNotNull(parentProcessDatafieldName, subProcessDatafieldName);
    if ("".equals(subProcessDatafieldName.trim())) {
      problems.add(new Problem("Sub process datafield name is an empty string.", Problem.SEVERITY_ERROR));
    }
    if ("".equals(parentProcessDatafieldName.trim())) {
      problems.add(new Problem("Parent process datafield name is an empty string.", Problem.SEVERITY_ERROR));
    }

    final Object obj = peek(ActivityDefinitionImpl.class);
    if (isActivity(obj)) {
      final ActivityDefinitionImpl activity = (ActivityDefinitionImpl) obj;
      activity.addSubflowOutParameter(new SubflowParameterDefinitionImpl(subProcessDatafieldName,
          parentProcessDatafieldName));
    } else {
      problems.add(new Problem("Unable to add sub process parameter {" + parentProcessDatafieldName + ", "
          + subProcessDatafieldName + "} on an object of type: " + getClass(obj), Problem.SEVERITY_ERROR));
    }
    return this;
  }

  /**
   * Add a decision node to the ProcessBuilder. By default The join type is XOR
   * and the split types is AND.
   * 
   * @param taskName
   *          the decision node name
   * @return the ProcessBuilder in order to add BPM elements
   */
  public ProcessBuilder addDecisionNode(final String taskName) {
    Misc.checkArgsNotNull(taskName);
    if ("".equals(taskName.trim())) {
      problems.add(new Problem("Task name is an empty string.", Problem.SEVERITY_ERROR));
    }
    final Object obj = peek(ProcessDefinitionImpl.class);
    if (isProcess(obj)) {
      final ProcessDefinitionImpl process = (ProcessDefinitionImpl) obj;
      final ActivityDefinitionImpl activity = ActivityDefinitionImpl.createAutomaticActivity(process.getUUID(),
          taskName);
      process.addActivity(activity);
      push(activity);
    } else {
      problems.add(new Problem("Unable to add decision node " + taskName + " on an object of type: " + getClass(obj),
          Problem.SEVERITY_ERROR));
    }
    return this;
  }

  /**
   * Add a deadline to the current activity. If the deadline fails during
   * execution an exception is thrown.
   * 
   * @param condition
   *          the deadline condition.
   * @param connectorClassName
   *          the {@link Connector} class name
   * @return the ProcessBuilder in order to add BPM elements
   */
  public ProcessBuilder addDeadline(final String condition, final String connectorClassName) {
    return addDeadline(condition, connectorClassName, true);
  }

  /**
   * Add a deadline to the current activity.
   * 
   * @param condition
   *          the deadline condition.
   * @param connectorClassName
   *          the {@link Connector} class name
   * @param throwingException
   *          define whether an exception is thrown if an exception occurs
   *          during deadline execution
   * @return the ProcessBuilder in order to add BPM elements
   */
  public ProcessBuilder addDeadline(final String condition, final String connectorClassName,
      final boolean throwingException) {
    Misc.checkArgsNotNull(condition, connectorClassName);
    if ("".equals(condition.trim())) {
      problems.add(new Problem("Condition is an empty string.", Problem.SEVERITY_ERROR));
    }
    if ("".equals(connectorClassName.trim())) {
      problems.add(new Problem("Connector class name is an empty string.", Problem.SEVERITY_ERROR));
    }
    final Object obj = peek(ActivityDefinitionImpl.class);
    if (isActivity(obj)) {
      if (!Misc.isJustAGroovyExpression(condition)) {
        // check the deadline condition:
        try {
          Long.parseLong(condition);
        } catch (final NumberFormatException e1) {
          try {
            DateUtil.parseDate(condition);
          } catch (final IllegalArgumentException e2) {
            problems.add(new Problem("deadline condition '" + condition + "' is neither a Long nor a formatted date",
                Problem.SEVERITY_ERROR));
          }
        }
      }
      final ActivityDefinitionImpl activity = (ActivityDefinitionImpl) obj;
      final ConnectorDefinitionImpl deadline = new ConnectorDefinitionImpl(connectorClassName);
      deadline.setCondition(condition);
      deadline.setThrowingException(throwingException);
      activity.addDeadline(deadline);
      push(deadline);
    } else {
      problems.add(new Problem("Unable to add a deadline with condition " + condition + " on an object of type: "
          + getClass(obj), Problem.SEVERITY_ERROR));
    }
    return this;
  }

  /**
   * Adds a connector to an activity or a process.
   * 
   * @param className
   *          the connector class name
   * @param event
   *          the connector event
   * @param throwingException
   *          if the connector throws exception
   * @return the ProcessBuilder in order to add BPM elements
   */
  public ProcessBuilder addConnector(final Event event, final String className, final boolean throwingException) {
    Misc.checkArgsNotNull(className, event);
    if ("".equals(className.trim())) {
      problems.add(new Problem("Class name is an empty string.", Problem.SEVERITY_ERROR));
    }
    Object obj = peek(ActivityDefinitionImpl.class);
    if (isActivity(obj)) {
      final ActivityDefinitionImpl activity = (ActivityDefinitionImpl) obj;
      if (activity.isTimer()) {
        if (!Event.onTimer.equals(event)) {
          problems.add(new Problem("A timer task accepts only a connector with an " + Event.onTimer + " event" + " ",
              Problem.SEVERITY_ERROR));
        }
      } else if (activity.isAutomatic()) {
        if (!(Event.automaticOnEnter.equals(event) || Event.automaticOnExit.equals(event))) {
          problems.add(new Problem("An automatic activity accepts only a connector with an " + Event.automaticOnEnter
              + " or an " + Event.automaticOnExit + " event" + " ", Problem.SEVERITY_ERROR));
        }
      } else if (activity.isSubflow()) {
        if (!(Event.automaticOnEnter.equals(event) || Event.automaticOnExit.equals(event))) {
          problems.add(new Problem("A sub process accepts only a connector with an " + Event.automaticOnEnter
              + " or an " + Event.automaticOnExit + " event" + " ", Problem.SEVERITY_ERROR));
        }
      } else {
        if (!Event.TASK_EVENTS.contains(event)) {
          problems.add(new Problem("A human task accepts only a connector with an event of this list: "
              + Event.TASK_EVENTS, Problem.SEVERITY_ERROR));
        }
      }
      final ConnectorDefinitionImpl connector = new ConnectorDefinitionImpl(className);
      connector.setEvent(event);
      connector.setThrowingException(throwingException);
      activity.addConnector(connector);
      push(connector);
    } else {
      obj = peek(ProcessDefinitionImpl.class);
      if (isProcess(obj)) {
        if (!Event.PROCESS_EVENTS.contains(event)) {
          problems.add(new Problem("A process accepts only a connector with an event of this list: "
              + Event.PROCESS_EVENTS, Problem.SEVERITY_ERROR));
        }
        final ProcessDefinitionImpl process = (ProcessDefinitionImpl) obj;
        final ConnectorDefinitionImpl connector = new ConnectorDefinitionImpl(className);
        connector.setEvent(event);
        connector.setThrowingException(throwingException);
        process.addConnector(connector);
        push(connector);
      } else {
        problems.add(new Problem("Unable to add a connector with class " + className + " on an object of type: "
            + getClass(obj), Problem.SEVERITY_ERROR));
      }
    }
    return this;
  }

  /**
   * Adds an input parameter on the current connector.
   * 
   * @param fieldName
   *          a connector field name
   * @param value
   *          the value(s) to set
   * @return the ProcessBuilder in order to add BPM elements
   */
  public ProcessBuilder addInputParameter(final String fieldName, final Object... value) {
    Misc.checkArgsNotNull(fieldName);
    if ("".equals(fieldName.trim())) {
      problems.add(new Problem("Connector field name is an empty string.", Problem.SEVERITY_ERROR));
    }
    String setterName = null;
    if (fieldName.startsWith("set") || fieldName.startsWith("get") || fieldName.startsWith("is")
        || "file:".equals(fieldName) || "resource:".equals(fieldName) || "bar:".equals(fieldName)) {
      setterName = fieldName;
    } else {
      final StringBuilder builder = new StringBuilder("set");
      builder.append(String.valueOf(fieldName.charAt(0)).toUpperCase());
      builder.append(fieldName.substring(1));
      setterName = builder.toString();
    }
    return setParameter(setterName, value);
  }

  /**
   * @see #addInputParameter(String, Object...)
   */
  @Deprecated
  public ProcessBuilder addParameter(final String fieldName, final Object... value) {
    return addInputParameter(fieldName, value);
  }

  /**
   * Adds an output parameter on the current connector. The result of Groovy
   * expression (containing getter fields) will be set in the variable name.
   * 
   * @param groovyExpression
   *          the Groovy expression.
   * @param variableName
   *          the variable name
   * @return the ProcessBuilder in order to add BPM elements
   */
  public ProcessBuilder addOutputParameter(String groovyExpression, final String variableName) {
    Misc.checkArgsNotNull(groovyExpression, variableName);
    if ("".equals(groovyExpression.trim())) {
      problems.add(new Problem("Connector field name is an empty string.", Problem.SEVERITY_ERROR));
    }
    if ("".equals(variableName.trim())) {
      problems.add(new Problem("Connector field name is an empty string.", Problem.SEVERITY_ERROR));
    }
    if (!GroovyExpression.isGroovyExpression(groovyExpression)) {
      final StringBuilder builder = new StringBuilder(GroovyExpression.START_DELIMITER);
      builder.append(groovyExpression);
      builder.append(GroovyExpression.END_DELIMITER);
      groovyExpression = builder.toString();
    }
    return setParameter(variableName, groovyExpression);
  }

  private ProcessBuilder setParameter(final String setterOrGetterName, final Object... value) {
    Misc.checkArgsNotNull(setterOrGetterName, value);
    if ("".equals(setterOrGetterName.trim())) {
      problems.add(new Problem("Setter/Getter name is an empty string.", Problem.SEVERITY_ERROR));
    }
    final Object obj = peek(ConnectorDefinitionImpl.class);
    if (isConnector(obj)) {
      ((ConnectorDefinitionImpl) obj).addParameter(setterOrGetterName, value);
    } else {
      problems.add(new Problem("Unable to add a parameter with field " + setterOrGetterName + " on an object of type: "
          + getClass(obj), Problem.SEVERITY_ERROR));
    }
    return this;
  }

  /**
   * Add a filter to an activity (A filter is a specific connector)
   * 
   * @param className
   *          the filter class name
   * @return the ProcessBuilder in order to add BPM elements
   */
  public ProcessBuilder addFilter(final String className) {
    Misc.checkArgsNotNull(className);
    if ("".equals(className.trim())) {
      problems.add(new Problem("Filter class name is an empty string.", Problem.SEVERITY_ERROR));
    }
    final Object obj = peek(ActivityDefinitionImpl.class);
    if (isActivity(obj)) {
      final ActivityDefinitionImpl activity = (ActivityDefinitionImpl) obj;
      final FilterDefinition filter = new ConnectorDefinitionImpl(className);
      activity.setFilter(filter);
      push(filter);
    } else {
      problems.add(new Problem("Unable to add a filter with class name " + className + " on an object of type: "
          + getClass(obj), Problem.SEVERITY_ERROR));
    }
    return this;
  }

  /**
   * Adds a multiInstantiation to an activity.
   * 
   * @param variableName
   *          the variable name
   * @param className
   *          the multi-instantiation class name
   * @return the ProcessBuilder in order to add BPM elements
   */
  @Deprecated
  public ProcessBuilder addMultiInstanciation(final String variableName, final String className) {
    Misc.checkArgsNotNull(className, variableName);
    if ("".equals(variableName.trim())) {
      problems.add(new Problem("Multi instantiation variable name is an empty string.", Problem.SEVERITY_ERROR));
    }
    if ("".equals(className.trim())) {
      problems.add(new Problem("Multi instantiation class name is an empty string.", Problem.SEVERITY_ERROR));
    }
    final Object obj = peek(ActivityDefinitionImpl.class);
    if (isActivity(obj)) {
      final ActivityDefinitionImpl activity = (ActivityDefinitionImpl) obj;
      final ConnectorDefinitionImpl multiInstanciation = new ConnectorDefinitionImpl(className);
      multiInstanciation.setVariableName(variableName);
      activity.setMultiInstanciation(multiInstanciation);
      push(multiInstanciation);
    } else {
      problems.add(new Problem("Unable to add a multi instantiation with variable name " + variableName
          + " on an object of type: " + getClass(obj), Problem.SEVERITY_ERROR));
    }
    return this;
  }

  public ProcessBuilder addMultipleActivitiesInstantiator(final String className) {
    Misc.checkArgsNotNull(className);
    if ("".equals(className.trim())) {
      problems.add(new Problem("Multiple Activities Instantiator class name is an empty string.",
          Problem.SEVERITY_ERROR));
    }
    final Object obj = peek(ActivityDefinitionImpl.class);
    if (isActivity(obj)) {
      final ActivityDefinitionImpl activity = (ActivityDefinitionImpl) obj;
      final ConnectorDefinitionImpl instantiator = new ConnectorDefinitionImpl(className);
      activity.setMultipleInstancesInstantiator(instantiator);
      push(instantiator);
    } else {
      problems.add(new Problem("Unable to add a multiple activities instantiatior with class name " + className
          + " on an object of type: " + getClass(obj), Problem.SEVERITY_ERROR));
    }
    return this;
  }

  public ProcessBuilder addMultipleActivitiesJoinChecker(final String className) {
    Misc.checkArgsNotNull(className);
    if ("".equals(className.trim())) {
      problems.add(new Problem("Multiple Activities Join Checker class name is an empty string.",
          Problem.SEVERITY_ERROR));
    }
    final Object obj = peek(ActivityDefinitionImpl.class);
    if (isActivity(obj)) {
      final ActivityDefinitionImpl activity = (ActivityDefinitionImpl) obj;
      final ConnectorDefinitionImpl joinChecker = new ConnectorDefinitionImpl(className);
      activity.setMultipleInstancesJoinChecker(joinChecker);
      push(joinChecker);
    } else {
      problems.add(new Problem("Unable to add a multiple activities join checker with class name " + className
          + " on an object of type: " + getClass(obj), Problem.SEVERITY_ERROR));
    }
    return this;
  }

  /**
   * Defines that the current activity is asynchronous.
   * 
   * @return the ProcessBuilder in order to add BPM elements
   */
  public ProcessBuilder asynchronous() {
    final Object obj = peek(ActivityDefinitionImpl.class);
    if (isActivity(obj)) {
      final ActivityDefinitionImpl activity = (ActivityDefinitionImpl) obj;
      activity.setAsynchronous(true);
    } else {
      problems.add(new Problem("Unable to set asynchronous on an object of type: " + getClass(obj),
          Problem.SEVERITY_ERROR));
    }
    return this;
  }

  /**
   * Defines the join type of an activity.
   * 
   * @param join
   *          the join type
   * @return the ProcessBuilder in order to add BPM elements
   */
  public ProcessBuilder addJoinType(final JoinType join) {
    Misc.checkArgsNotNull(join);
    final Object obj = peek(ActivityDefinitionImpl.class);
    if (isActivity(obj)) {
      final ActivityDefinitionImpl activity = (ActivityDefinitionImpl) obj;
      activity.setJoinType(join);
    } else {
      problems.add(new Problem("Unable to set join type " + join + " on an object of type: " + getClass(obj),
          Problem.SEVERITY_ERROR));
    }
    return this;
  }

  /**
   * Defines the split type of an activity
   * 
   * @param split
   *          the split type
   * @return the ProcessBuilder in order to add BPM elements
   */
  public ProcessBuilder addSplitType(final SplitType split) {
    Misc.checkArgsNotNull(split);
    final Object obj = peek(ActivityDefinitionImpl.class);
    if (isActivity(obj)) {
      final ActivityDefinitionImpl activity = (ActivityDefinitionImpl) obj;
      activity.setSplitType(split);
    } else {
      problems.add(new Problem("Unable to set split type " + split + " on an object of type: " + getClass(obj),
          Problem.SEVERITY_ERROR));
    }
    return this;
  }

  /**
   * Adds a dynamic description to the current BPM entity.
   * 
   * @param dynamicDescription
   *          the BPM entity dynamicDescription
   * @return the ProcessBuilder in order to add BPM elements
   */
  public ProcessBuilder addDynamicDescription(final String dynamicDescription) {
    final Object obj = peek(ActivityDefinitionImpl.class);
    if (isActivity(obj)) {
      ((ActivityDefinitionImpl) obj).setDynamicDescription(dynamicDescription);
    } else {
      problems.add(new Problem("Unable to set dynamicDescription " + dynamicDescription + " on an object of type: "
          + getClass(obj), Problem.SEVERITY_ERROR));
    }
    return this;
  }

  /**
   * Adds a dynamic label to the current BPM entity.
   * 
   * @param dynamicLabel
   *          the BPM entity dynamicLabel
   * @return the ProcessBuilder in order to add BPM elements
   */
  public ProcessBuilder addDynamicLabel(final String dynamicLabel) {
    final Object obj = peek(ActivityDefinitionImpl.class);
    if (isActivity(obj)) {
      ((ActivityDefinitionImpl) obj).setDynamicLabel(dynamicLabel);
    } else {
      problems.add(new Problem(
          "Unable to set dynamicLabel " + dynamicLabel + " on an object of type: " + getClass(obj),
          Problem.SEVERITY_ERROR));
    }
    return this;
  }

  /**
   * Adds a dynamic execution summary to the current BPM entity.
   * 
   * @param expression
   *          the dynamic expression
   * @return the ProcessBuilder in order to add BPM elements
   */
  public ProcessBuilder addDynamicExecutionSummary(final String expression) {
    final Object obj = peek(ActivityDefinitionImpl.class);
    if (isActivity(obj)) {
      ((ActivityDefinitionImpl) obj).setDynamicExecutionSummary(expression);
    } else {
      problems.add(new Problem("Unable to set the dynamic execution summary " + expression + " on an object of type: "
          + getClass(obj), Problem.SEVERITY_ERROR));
    }
    return this;
  }

  /**
   * Adds a category defining the process
   * 
   * @param category
   *          the category name
   * @return the ProcessBuilder in order to add BPM elements
   */
  public ProcessBuilder addCategory(final String category) {
    Misc.checkArgsNotNull(category);
    if ("".equals(category.trim())) {
      problems.add(new Problem("Category name is an empty string.", Problem.SEVERITY_ERROR));
    }
    final Object obj = peek(ProcessDefinitionImpl.class);
    if (isProcess(obj)) {
      final ProcessDefinitionImpl process = (ProcessDefinitionImpl) obj;
      process.addCategory(category);
    } else {
      problems.add(new Problem("Unable to add category " + category + " on an object of type: " + getClass(obj),
          Problem.SEVERITY_ERROR));
    }
    return this;
  }

  /**
   * Adds a timer.
   * 
   * @param taskName
   *          the timer name.
   * @param condition
   *          the timer condition.
   * @return the ProcessBuilder in order to add BPM elements
   */
  public ProcessBuilder addTimerTask(final String taskName, final String condition) {
    Misc.checkArgsNotNull(taskName);
    if ("".equals(taskName.trim())) {
      problems.add(new Problem("Task name is an empty string.", Problem.SEVERITY_ERROR));
    }
    if ("".equals(condition.trim())) {
      problems.add(new Problem("Timer condition name is an empty string.", Problem.SEVERITY_ERROR));
    }
    final Object obj = peek(ProcessDefinitionImpl.class);
    if (isProcess(obj)) {
      final ProcessDefinitionImpl process = (ProcessDefinitionImpl) obj;
      final ActivityDefinitionImpl activity = ActivityDefinitionImpl.createTimerActivity(process.getUUID(), taskName,
          condition);
      process.addActivity(activity);
      push(activity);
    } else {
      problems.add(new Problem("Unable to add timer task " + taskName + " on an object of type: " + getClass(obj),
          Problem.SEVERITY_ERROR));
    }
    return this;
  }

  /**
   * Adds a received event task.
   * 
   * @param taskName
   *          the task name
   * @param eventName
   *          the event name
   * @return the ProcessBuilder in order to add BPM elements
   */
  public ProcessBuilder addReceiveEventTask(final String taskName, final String eventName) {
    return addReceiveEventTask(taskName, eventName, null);
  }

  /**
   * Adds a received event task.
   * 
   * @deprecated replaced by {@link #addReceiveEventTask(String, String)} and
   *             {@link #addMessageCorrelationKey(String, String)}
   * @param taskName
   *          the task name
   * @param eventName
   *          the event name
   * @param expression
   *          the expression
   * @return the ProcessBuilder in order to add BPM elements
   */
  @Deprecated
  public ProcessBuilder addReceiveEventTask(final String taskName, final String eventName, final String expression) {
    Misc.checkArgsNotNull(taskName);
    if ("".equals(taskName.trim())) {
      problems.add(new Problem("Task name is an empty string.", Problem.SEVERITY_ERROR));
    }
    if ("".equals(eventName.trim())) {
      problems.add(new Problem("Event name is an empty string.", Problem.SEVERITY_ERROR));
    }
    if (expression != null && "".equals(expression.trim())) {
      problems.add(new Problem("Expression is an empty string.", Problem.SEVERITY_ERROR));
    }
    final Object obj = peek(ProcessDefinitionImpl.class);
    if (isProcess(obj)) {
      final ProcessDefinitionImpl process = (ProcessDefinitionImpl) obj;
      final ActivityDefinitionImpl activity = ActivityDefinitionImpl.createReceiveEventActivity(process.getUUID(),
          taskName, eventName, expression);
      process.addActivity(activity);
      push(activity);
    } else {
      problems.add(new Problem("Unable to add receive event task " + taskName + " on an object of type: "
          + getClass(obj), Problem.SEVERITY_ERROR));
    }
    return this;
  }

  /**
   * Adds a connector to the current received event.
   * 
   * @param className
   *          the connector class name
   * @param throwingException
   *          if the connector throws exception.
   * @return the ProcessBuilder in order to add BPM elements
   */
  public ProcessBuilder addReceiveEventConnector(final String className, final boolean throwingException) {
    Misc.checkArgsNotNull(className);
    if ("".equals(className.trim())) {
      problems.add(new Problem("Class name is an empty string.", Problem.SEVERITY_ERROR));
    }
    final Object obj = peek(ActivityDefinitionImpl.class);
    if (isActivity(obj)) {
      final ActivityDefinitionImpl activity = (ActivityDefinitionImpl) obj;
      final ConnectorDefinitionImpl connector = new ConnectorDefinitionImpl(className);
      connector.setEvent(Event.onEvent);
      connector.setThrowingException(throwingException);
      activity.addConnector(connector);
      push(connector);
    } else {
      problems.add(new Problem("Unable to add an event connector with class " + className + " on an object of type: "
          + getClass(obj), Problem.SEVERITY_ERROR));
    }
    return this;
  }

  /**
   * Add to the current connector of an activity the possibility to throw an
   * error event.
   * 
   * @param errorCode
   *          the error code of the event to throw.
   * @return the ProcessBuilder in order to add BPM elements
   */
  public ProcessBuilder throwCatchError(final String errorCode) {
    Misc.checkArgsNotNull(errorCode);
    if ("".equals(errorCode.trim())) {
      problems.add(new Problem("Connector errorCode is an empty string.", Problem.SEVERITY_ERROR));
    }
    final Object obj = peek(ConnectorDefinitionImpl.class);
    if (isConnector(obj)) {
      ((ConnectorDefinitionImpl) obj).setErrorCode(errorCode);
    } else {
      problems.add(new Problem("Unable to add an errorCode: " + errorCode + " on an object of type: " + getClass(obj),
          Problem.SEVERITY_ERROR));
    }
    return this;
  }

  /**
   * Add a end error event task.
   * 
   * @param eventName
   *          the name of event.
   * @param errorCode
   *          the error code of the event.
   * @return the ProcessBuilder in order to add BPM elements
   */
  public ProcessBuilder addErrorEventTask(final String eventName, final String errorCode) {
    Misc.checkArgsNotNull(eventName, errorCode);
    if ("".equals(eventName.trim())) {
      problems.add(new Problem("Event name is an empty string.", Problem.SEVERITY_ERROR));
    }
    if ("".equals(errorCode.trim())) {
      problems.add(new Problem("Error code is an empty string.", Problem.SEVERITY_ERROR));
    }
    final Object obj = peek(ProcessDefinitionImpl.class);
    if (isProcess(obj)) {
      final ProcessDefinitionImpl process = (ProcessDefinitionImpl) obj;
      final ActivityDefinitionImpl activity = ActivityDefinitionImpl.createErrorEventActivity(process.getUUID(),
          eventName, errorCode);
      process.addActivity(activity);
      push(activity);
    } else {
      problems.add(new Problem("Unable to add an error event task " + eventName + " on an object of type: "
          + getClass(obj), Problem.SEVERITY_ERROR));
    }
    return this;
  }

  /**
   * Add a throwing signal event task.
   * 
   * @param eventName
   *          the name of event.
   * @param signalCode
   *          the signal code of the event.
   * @return the ProcessBuilder in order to add BPM elements
   */
  public ProcessBuilder addSignalEventTask(final String eventName, final String signalCode) {
    return addSignalEventTask(eventName, signalCode, false);
  }

  public ProcessBuilder addSignalEventTask(final String eventName, final String signalCode, final boolean catchEvent) {
    Misc.checkArgsNotNull(eventName, signalCode);
    if ("".equals(eventName.trim())) {
      problems.add(new Problem("Event name is an empty string.", Problem.SEVERITY_ERROR));
    }
    if ("".equals(signalCode.trim())) {
      problems.add(new Problem("Signal code is an empty string.", Problem.SEVERITY_ERROR));
    }
    final Object obj = peek(ProcessDefinitionImpl.class);
    if (isProcess(obj)) {
      final ProcessDefinitionImpl process = (ProcessDefinitionImpl) obj;
      final ActivityDefinitionImpl activity = ActivityDefinitionImpl.createSignalEventActivity(process.getUUID(),
          eventName, signalCode);
      activity.setCatchEvent(catchEvent);
      process.addActivity(activity);
      push(activity);
    } else {
      problems.add(new Problem("Unable to add an error event task " + eventName + " on an object of type: "
          + getClass(obj), Problem.SEVERITY_ERROR));
    }
    return this;
  }

  /**
   * Add a signal boundary event onto an activity.
   * 
   * @param eventName
   *          the name of the event.
   * @param signalCode
   *          the expression of the event.
   * @return the ProcessBuilder in order to add BPM elements.
   */
  public ProcessBuilder addSignalBoundaryEvent(final String eventName, final String signalCode) {
    Misc.checkArgsNotNull(eventName);
    if ("".equals(eventName.trim())) {
      problems.add(new Problem("Event name is an empty string.", Problem.SEVERITY_ERROR));
    }
    if (signalCode != null && "".equals(signalCode.trim())) {
      problems.add(new Problem("Expression is an empty string.", Problem.SEVERITY_ERROR));
    }
    final Object obj = peek(ActivityDefinitionImpl.class);
    if (isActivity(obj)) {
      final ActivityDefinitionImpl activity = (ActivityDefinitionImpl) obj;
      if (canAddBoundaryEvent(activity, "signal")) {
        final SignalBoundaryEventImpl message = new SignalBoundaryEventImpl(eventName,
            activity.getProcessDefinitionUUID(), activity.getUUID(), null, signalCode);
        activity.addBoundaryEvent(message);
        push(message);
      } else {
        problems.add(new Problem(
            "Unable to add a boundary message event on an activity of type: " + activity.getType(),
            Problem.SEVERITY_ERROR));
      }
    } else {
      problems.add(new Problem("Unable to add a boundary message event on an object of type: " + getClass(obj),
          Problem.SEVERITY_ERROR));
    }
    return this;
  }

  private EventDefinitionImpl getEventDefinition(final String name) {
    final Object obj = stack.peek();
    if (isActivity(obj)) {
      final ActivityDefinitionImpl activity = (ActivityDefinitionImpl) obj;
      if (activity.isReceiveEvent()) {
        final IncomingEventDefinition incomingEvent = activity.getIncomingEvent();
        if (incomingEvent.getExpression() != null) {
          problems.add(new Problem("A correlation key cannot be added when an expression is set.",
              Problem.SEVERITY_ERROR));
        }
        return (IncomingEventDefinitionImpl) incomingEvent;
      } else if (activity.isSendEvents()) {
        return outgoingEvent;
      } else {
        problems.add(new Problem("Unable to add a correlation key " + name + " on an object of type: " + getClass(obj),
            Problem.SEVERITY_ERROR));
      }
    } else {
      problems.add(new Problem("Unable to add a correlation key " + name + " on an object of type: " + getClass(obj),
          Problem.SEVERITY_ERROR));
    }
    return null;
  }

  private MessageBoundaryEventImpl getMessageBoundaryEvent(final String name) {
    final Object obj = stack.peek();
    if (isMessageBoundaryEvent(obj)) {
      final MessageBoundaryEventImpl messageEvent = (MessageBoundaryEventImpl) obj;
      if (messageEvent.getExpression() != null) {
        problems
            .add(new Problem("A correlation key cannot be added when an expression is set.", Problem.SEVERITY_ERROR));
      }
      return messageEvent;
    }
    return null;
  }

  public ProcessBuilder addMessageCorrelationKey(final String name, final String expression) {
    Misc.checkArgsNotNull(name, expression);
    if ("".equals(name.trim())) {
      problems.add(new Problem("Correlation key name is an empty string.", Problem.SEVERITY_ERROR));
    }
    final MessageBoundaryEventImpl messageEvent = getMessageBoundaryEvent(name);
    if (messageEvent != null) {
      if (messageEvent.getCorrelationKeyName1() == null) {
        messageEvent.setCorrelationKey1(name, expression);
      } else if (messageEvent.getCorrelationKeyName2() == null) {
        messageEvent.setCorrelationKey2(name, expression);
      } else if (messageEvent.getCorrelationKeyName3() == null) {
        messageEvent.setCorrelationKey3(name, expression);
      } else if (messageEvent.getCorrelationKeyName4() == null) {
        messageEvent.setCorrelationKey4(name, expression);
      } else if (messageEvent.getCorrelationKeyName5() == null) {
        messageEvent.setCorrelationKey5(name, expression);
      } else {
        problems.add(new Problem("5 Correlation keys are already set on " + messageEvent.getName(),
            Problem.SEVERITY_ERROR));
      }
    } else {
      final EventDefinitionImpl event = getEventDefinition(name);
      if (event.getCorrelationKeyName1() == null) {
        event.setCorrelationKey1(name, expression);
      } else if (event.getCorrelationKeyName2() == null) {
        event.setCorrelationKey2(name, expression);
      } else if (event.getCorrelationKeyName3() == null) {
        event.setCorrelationKey3(name, expression);
      } else if (event.getCorrelationKeyName4() == null) {
        event.setCorrelationKey4(name, expression);
      } else if (event.getCorrelationKeyName5() == null) {
        event.setCorrelationKey5(name, expression);
      } else {
        problems.add(new Problem("5 Correlation keys are already set on " + event.getName(), Problem.SEVERITY_ERROR));
      }
    }
    return this;
  }

  /**
   * Adds a send event task.
   * 
   * @param taskName
   *          the task name.
   * @return the ProcessBuilder in order to add BPM elements
   */
  public ProcessBuilder addSendEventTask(final String taskName) {
    Misc.checkArgsNotNull(taskName);
    if ("".equals(taskName.trim())) {
      problems.add(new Problem("Task name is an empty string.", Problem.SEVERITY_ERROR));
    }
    final Object obj = peek(ProcessDefinitionImpl.class);
    if (isProcess(obj)) {
      final ProcessDefinitionImpl process = (ProcessDefinitionImpl) obj;
      final ActivityDefinitionImpl activity = ActivityDefinitionImpl.createSendEventActivity(process.getUUID(),
          taskName);
      process.addActivity(activity);
      push(activity);
    } else {
      problems.add(new Problem("Unable to add send event task " + taskName + " on an object of type: " + getClass(obj),
          Problem.SEVERITY_ERROR));
    }
    return this;
  }

  /**
   * Adds an outgoing event on the current activity.
   * 
   * @param eventName
   *          the event name
   * @return the ProcessBuilder in order to add BPM elements
   */
  public ProcessBuilder addOutgoingEvent(final String eventName) {
    return addOutgoingEvent(eventName, null);
  }

  /**
   * Adds an outgoing event on the current activity.
   * 
   * @param eventName
   *          the event name
   * @param destProcessName
   *          the destination process
   * @return the ProcessBuilder in order to add BPM elements
   */
  public ProcessBuilder addOutgoingEvent(final String eventName, final String destProcessName) {
    return addOutgoingEvent(eventName, destProcessName, null);
  }

  /**
   * Adds an outgoing event on the current activity.
   * 
   * @param eventName
   *          the event name
   * @param destProcessName
   *          the destination process
   * @param destActivityName
   *          the destination activity
   * @return the ProcessBuilder in order to add BPM elements
   */
  public ProcessBuilder addOutgoingEvent(final String eventName, final String destProcessName,
      final String destActivityName) {
    return addOutgoingEvent(eventName, destProcessName, destActivityName, null);
  }

  /**
   * Adds an outgoing event on the current activity.
   * 
   * @param eventName
   *          the event name
   * @param destProcessName
   *          the destination process
   * @param destActivityName
   *          the destination activity
   * @param parameters
   *          the parameters to transfer
   * @return the ProcessBuilder in order to add BPM elements
   */
  public ProcessBuilder addOutgoingEvent(final String eventName, final String destProcessName,
      final String destActivityName, final Map<String, Object> parameters) {
    return addOutgoingEvent(eventName, destProcessName, destActivityName, -1, parameters);
  }

  /**
   * Adds an outgoing event on the current activity.
   * 
   * @param eventName
   *          the event name
   * @param destProcessName
   *          the destination process
   * @param destActivityName
   *          the destination activity
   * @param timeToLive
   *          the time to live of this event
   * @param parameters
   *          the parameters to transfer
   * @return the ProcessBuilder in order to add BPM elements
   */
  public ProcessBuilder addOutgoingEvent(final String eventName, final String destProcessName,
      final String destActivityName, final long timeToLive, final Map<String, Object> parameters) {
    Misc.checkArgsNotNull(eventName);
    if ("".equals(eventName.trim())) {
      problems.add(new Problem("Event name is an empty string.", Problem.SEVERITY_ERROR));
    }
    if (destProcessName != null && "".equals(destProcessName.trim())) {
      problems.add(new Problem("Dest process name is an empty string.", Problem.SEVERITY_ERROR));
    }
    if (destActivityName != null && "".equals(destActivityName.trim())) {
      problems.add(new Problem("Dest activity name is an empty string.", Problem.SEVERITY_ERROR));
    }
    final Object obj = peek(ActivityDefinitionImpl.class);
    if (isActivity(obj)) {
      final ActivityDefinitionImpl activity = (ActivityDefinitionImpl) obj;
      final OutgoingEventDefinitionImpl outgoingEvent = new OutgoingEventDefinitionImpl(eventName, destProcessName,
          destActivityName, parameters, timeToLive);
      activity.addOutgoingEvent(outgoingEvent);
      this.outgoingEvent = outgoingEvent;
    } else {
      problems.add(new Problem("Unable to add outgoing event " + eventName + " on an object of type: " + getClass(obj),
          Problem.SEVERITY_ERROR));
    }
    return this;
  }

  /**
   * Adds a terminate end event. This event ends the process execution even if
   * other branches are active.
   * 
   * @param eventName
   *          the event name
   * @return the ProcessBuilder in order to add BPM elements
   */
  public ProcessBuilder addTerminateEndEvent(final String eventName) {
    Misc.checkArgsNotNull(eventName);
    if ("".equals(eventName.trim())) {
      problems.add(new Problem("Event name is an empty string.", Problem.SEVERITY_ERROR));
    }
    final Object obj = peek(ProcessDefinitionImpl.class);
    if (isProcess(obj)) {
      final ProcessDefinitionImpl process = (ProcessDefinitionImpl) obj;
      final ActivityDefinitionImpl activity = ActivityDefinitionImpl.createAutomaticActivity(process.getUUID(),
          eventName);
      activity.setTerminateProcess(true);
      process.addActivity(activity);
      push(activity);
    } else {
      problems.add(new Problem("Unable to add terminate end event " + eventName + " on an object of type: "
          + getClass(obj), Problem.SEVERITY_ERROR));
    }
    return this;
  }

  /**
   * Adds an executing time (in ms) to the current activity. This time should be
   * greater than 1s.
   * 
   * @param executingTime
   *          the executing time of the activity
   * @return the ProcessBuilder in order to add BPM elements
   */
  public ProcessBuilder addActivityExecutingTime(final long executingTime) {
    if (executingTime < 1000) {
      problems.add(new Problem("The executing time cannot be less than one second.", Problem.SEVERITY_ERROR));
    }
    final Object obj = peek(ActivityDefinitionImpl.class);
    if (isActivity(obj)) {
      final ActivityDefinitionImpl activity = (ActivityDefinitionImpl) obj;
      activity.setExecutingTime(executingTime);
    } else {
      problems.add(new Problem("Unable to set an executing time on an object of type: " + getClass(obj),
          Problem.SEVERITY_ERROR));
    }
    return this;
  }

  /**
   * Adds a priority to the current activity.
   * 
   * @param priority
   *          the activity priority.
   * @return the ProcessBuilder in order to add BPM elements
   */
  public ProcessBuilder addActivityPriority(final int priority) {
    final Object obj = peek(ActivityDefinitionImpl.class);
    if (isActivity(obj)) {
      final ActivityDefinitionImpl activity = (ActivityDefinitionImpl) obj;
      activity.setPriority(priority);
    } else {
      problems.add(new Problem("Unable to set a priority on an object of type: " + getClass(obj),
          Problem.SEVERITY_ERROR));
    }
    return this;
  }

  /**
   * Defines that the current activity is a loop one. While the condition is
   * true a new instance of the activity is created.
   * 
   * @param condition
   *          a Groovy script which must return a boolean value.
   * @param beforeExecution
   *          true if the condition is evaluated before the activity creation.
   * @return the ProcessBuilder in order to add BPM elements
   */
  public ProcessBuilder addLoop(final String condition, final boolean beforeExecution) {
    return addLoop(condition, beforeExecution, null);
  }

  /**
   * Defines that the current activity is a loop one. While the condition is
   * true a new instance of the activity is created.
   * 
   * @param condition
   *          a Groovy script which must return a boolean value.
   * @param beforeExecution
   *          true if the condition is evaluated before the activity creation.
   * @param loopMaximum
   *          the maximum number of loops.
   * @return the ProcessBuilder in order to add BPM elements
   */
  public ProcessBuilder addLoop(final String condition, final boolean beforeExecution, final String loopMaximum) {
    Misc.checkArgsNotNull(condition);
    if ("".equals(condition.trim())) {
      problems.add(new Problem("Condition expression is an empty string.", Problem.SEVERITY_ERROR));
    }
    final Object obj = peek(ActivityDefinitionImpl.class);
    if (isActivity(obj)) {
      final ActivityDefinitionImpl activity = (ActivityDefinitionImpl) obj;
      activity.setLoop(condition, beforeExecution, loopMaximum);
    } else {
      problems.add(new Problem("Unable to set a loop on an object of type: " + getClass(obj), Problem.SEVERITY_ERROR));
    }
    return this;
  }

  /**
   * Add a timer boundary event onto an activity.
   * 
   * @param eventName
   *          the name of the event.
   * @param condition
   *          the condition of the timer event.
   * @return the ProcessBuilder in order to add BPM elements.
   */
  public ProcessBuilder addTimerBoundaryEvent(final String eventName, final String condition) {
    Misc.checkArgsNotNull(eventName, condition);
    final Object obj = peek(ActivityDefinitionImpl.class);
    if (isActivity(obj)) {
      final ActivityDefinitionImpl activity = (ActivityDefinitionImpl) obj;
      if (canAddBoundaryEvent(activity, "timer")) {
        final TimerBoundaryEventImpl timer = new TimerBoundaryEventImpl(eventName, activity.getProcessDefinitionUUID(),
            activity.getUUID(), null, condition);
        activity.addBoundaryEvent(timer);
        push(timer);
      } else {
        problems.add(new Problem("Unable to add a boundary timer event on an activity of type: " + activity.getType(),
            Problem.SEVERITY_ERROR));
      }
    } else {
      problems.add(new Problem("Unable to add a boundary timer event on an object of type: " + getClass(obj),
          Problem.SEVERITY_ERROR));
    }
    return this;
  }

  /**
   * Add a message boundary event onto an activity.
   * 
   * @param eventName
   *          the name of the event.
   * @param expression
   *          the expression of the event.
   * @return the ProcessBuilder in order to add BPM elements.
   */
  public ProcessBuilder addMessageBoundaryEvent(final String eventName, final String expression) {
    Misc.checkArgsNotNull(eventName);
    if ("".equals(eventName.trim())) {
      problems.add(new Problem("Event name is an empty string.", Problem.SEVERITY_ERROR));
    }
    if (expression != null && "".equals(expression.trim())) {
      problems.add(new Problem("Expression is an empty string.", Problem.SEVERITY_ERROR));
    }
    final Object obj = peek(ActivityDefinitionImpl.class);
    if (isActivity(obj)) {
      final ActivityDefinitionImpl activity = (ActivityDefinitionImpl) obj;
      if (canAddBoundaryEvent(activity, "message")) {
        final MessageBoundaryEventImpl message = new MessageBoundaryEventImpl(eventName,
            activity.getProcessDefinitionUUID(), activity.getUUID(), null, expression);
        activity.addBoundaryEvent(message);
        push(message);
      } else {
        problems.add(new Problem(
            "Unable to add a boundary message event on an activity of type: " + activity.getType(),
            Problem.SEVERITY_ERROR));
      }
    } else {
      problems.add(new Problem("Unable to add a boundary message event on an object of type: " + getClass(obj),
          Problem.SEVERITY_ERROR));
    }
    return this;
  }

  /**
   * Add an error boundary event onto a task or a system task.
   * 
   * @param eventName
   *          the name of the event
   * @return the ProcessBuilder in order to add BPM elements.
   */
  public ProcessBuilder addErrorBoundaryEvent(final String eventName) {
    return addErrorBoundaryEvent(eventName, null);
  }

  /**
   * Add an error boundary event onto a sub-process activity.
   * 
   * @param eventName
   *          the name of the event
   * @param errorCode
   *          the error code of the event
   * @return the ProcessBuilder in order to add BPM elements.
   */
  public ProcessBuilder addErrorBoundaryEvent(final String eventName, final String errorCode) {
    Misc.checkArgsNotNull(eventName);
    if ("".equals(eventName.trim())) {
      problems.add(new Problem("Event name is an empty string.", Problem.SEVERITY_ERROR));
    }
    if (errorCode != null && "".equals(errorCode.trim())) {
      problems.add(new Problem("Expression is an empty string.", Problem.SEVERITY_ERROR));
    }
    final Object obj = peek(ActivityDefinitionImpl.class);
    if (isActivity(obj)) {
      final ActivityDefinitionImpl activity = (ActivityDefinitionImpl) obj;
      if (canAddBoundaryEvent(activity, "error")) {
        final ErrorBoundaryEventImpl error = new ErrorBoundaryEventImpl(eventName, activity.getProcessDefinitionUUID(),
            activity.getUUID(), null, errorCode);
        activity.addBoundaryEvent(error);
        push(error);
      } else {
        problems.add(new Problem("Unable to add a boundary message event on an object of type: " + activity.getType(),
            Problem.SEVERITY_ERROR));
      }
    } else {
      problems.add(new Problem("Unable to add a boundary message event on an object of type: " + getClass(obj),
          Problem.SEVERITY_ERROR));
    }
    return this;
  }

  /**
   * Adds a transition between two activities.
   * 
   * @param fromActivityName
   *          the activity name where the transition leaves
   * @param toActivityName
   *          the activity name where the transition arrives
   * @return the ProcessBuilder in order to add BPM elements
   */
  public ProcessBuilder addTransition(final String fromActivityName, final String toActivityName) {
    return addTransition(fromActivityName + "__" + toActivityName, fromActivityName, toActivityName);
  }

  /**
   * Adds an exception transition between a boundary event of an activity to
   * another activity.
   * 
   * @param fromActivityName
   *          the activity name where the boundary event is
   * @param boundaryEventName
   *          the name of the boundary event
   * @param toActivityName
   *          the activity name where the transition arrives
   * @return the ProcessBuilder in order to add BPM elements
   */
  public ProcessBuilder addExceptionTransition(final String fromActivityName, final String boundaryEventName,
      final String toActivityName) {
    Misc.checkArgsNotNull(boundaryEventName);
    return addTransition(fromActivityName + "__" + boundaryEventName + "__" + toActivityName, fromActivityName,
        boundaryEventName, toActivityName);
  }

  /**
   * Adds a transition between two activities.
   * 
   * @param transitionName
   *          the transition name
   * @param fromActivityName
   *          the activity name where the transition leaves
   * @param toActivityName
   *          the activity name where the transition arrives
   * @return the ProcessBuilder in order to add BPM elements
   */
  public ProcessBuilder addTransition(final String transitionName, final String fromActivityName,
      final String toActivityName) {
    return addTransition(transitionName, fromActivityName, null, toActivityName);
  }

  private ProcessBuilder addTransition(final String transitionName, final String fromActivityName,
      final String boundaryEventName, final String toActivityName) {
    Misc.checkArgsNotNull(transitionName, fromActivityName, toActivityName);
    if ("".equals(transitionName.trim())) {
      problems.add(new Problem("Transition name is an empty string.", Problem.SEVERITY_ERROR));
    }
    if (fromActivityName == null) {
      problems
          .add(new Problem("From attribute is not defined on transition: " + transitionName, Problem.SEVERITY_ERROR));
    } else if ("".equals(fromActivityName.trim())) {
      problems.add(new Problem("Transition from activity name is an empty string.", Problem.SEVERITY_ERROR));
    }
    if (toActivityName == null) {
      problems.add(new Problem("To attribute is not defined on transition: " + transitionName, Problem.SEVERITY_ERROR));
    } else if ("".equals(toActivityName.trim())) {
      problems.add(new Problem("Transition to activity name is an empty string.", Problem.SEVERITY_ERROR));
    }
    if (boundaryEventName != null && "".equals(boundaryEventName.trim())) {
      problems.add(new Problem("The name of the boundary event is an empty string.", Problem.SEVERITY_ERROR));
    }

    final Object obj = peek(ProcessDefinitionImpl.class);
    if (isProcess(obj)) {
      final ProcessDefinitionImpl process = (ProcessDefinitionImpl) obj;
      final ActivityDefinition fromActivity = process.getActivity(fromActivityName);
      BoundaryEvent event = null;
      if (fromActivity != null && boundaryEventName != null) {
        event = fromActivity.getBoundaryEvent(boundaryEventName);
      }
      final ActivityDefinition toActivity = process.getActivity(toActivityName);
      if (fromActivity == null) {
        problems.add(new Problem("Unable to add transition from " + fromActivityName + " to " + toActivityName
            + ". From activity does not exists", Problem.SEVERITY_ERROR));
      } else if (toActivity == null) {
        problems.add(new Problem("Unable to add transition from " + fromActivityName + " to " + toActivityName
            + ". To activity does not exists", Problem.SEVERITY_ERROR));
      } else if (boundaryEventName != null && event == null) {
        problems.add(new Problem("Unable to add an exception transition from " + fromActivityName
            + ", boundary event: " + boundaryEventName + " to " + toActivityName + ". Boundary event does not exists",
            Problem.SEVERITY_ERROR));
      } else {
        final TransitionDefinitionImpl transition = new TransitionDefinitionImpl(process.getUUID(), transitionName,
            fromActivityName, toActivityName);
        ((ActivityDefinitionImpl) toActivity).addIncomingTransition(transition);
        if (boundaryEventName != null) {
          transition.setFromBoundaryEvent(boundaryEventName);
          ((ActivityDefinitionImpl) fromActivity).addExceptionTransition(boundaryEventName, transition);
        } else {
          ((ActivityDefinitionImpl) fromActivity).addOutgoingTransition(transition);
        }
        push(transition);
      }
    } else {
      problems.add(new Problem(
          "Unable to add transition " + transitionName + " on an object of type: " + getClass(obj),
          Problem.SEVERITY_ERROR));
    }
    return this;
  }

  /**
   * Set the last transition as the default transition
   * 
   * @return the ProcessBuilder in order to add BPM elements
   */
  public ProcessBuilder setDefault() {
    final Object obj = peek(TransitionDefinitionImpl.class);
    if (isTransition(obj)) {
      final TransitionDefinitionImpl transition = (TransitionDefinitionImpl) obj;
      if (transition.getFromBoundaryEvent() != null) {
        problems.add(new Problem("Unable to set the default exception transition '" + transition.getName()
            + "' as a default transition", Problem.SEVERITY_ERROR));
      }
      transition.setDefault(true);
    } else {
      problems.add(new Problem("Unable to set default transition on an object of type: " + getClass(obj),
          Problem.SEVERITY_ERROR));
    }
    return this;
  }

  /**
   * Adds a condition on a transition
   * 
   * @param expression
   *          the condition.
   * @return the ProcessBuilder in order to add BPM elements
   */
  public ProcessBuilder addCondition(final String expression) {
    Misc.checkArgsNotNull(expression);
    if ("".equals(expression.trim())) {
      problems.add(new Problem("Condition expression is an empty string.", Problem.SEVERITY_ERROR));
    }
    final Object obj = peek(TransitionDefinitionImpl.class);
    if (isTransition(obj)) {
      final TransitionDefinitionImpl transition = (TransitionDefinitionImpl) obj;
      if (transition.getFromBoundaryEvent() != null) {
        problems.add(new Problem("Unable to set condition " + expression + " on the exception transition: "
            + transition.getName(), Problem.SEVERITY_ERROR));
      }
      transition.setCondition(expression);
    } else {
      problems.add(new Problem("Unable to set condition " + expression + " on an object of type: " + getClass(obj),
          Problem.SEVERITY_ERROR));
    }
    return this;
  }

  /**
   * Finishes the process definition. This method checks how the definition was
   * done and throws all the errors that the process definition contains.
   * 
   * @return the process definition ready to use.
   */
  public ProcessDefinition done() {
    final ProcessDefinitionImpl process = (ProcessDefinitionImpl) stack.get(0);
    checkNames();
    checkStartEvents(process);
    checkEventSubProcess(process);
    checkDefaultTransitions(process);
    checkExceptionsTransitions(process);
    checkMultiInstantiation(process);
    checkTransientVariables();
    Misc.showProblems(problems, "process builder");
    setSubProcesses(process);
    return process;
  }

  private void checkStartEvents(final ProcessDefinitionImpl process) {
    for (final ActivityDefinition activity : process.getActivities()) {
      if (activity.getIncomingTransitions().size() == 0) {
        if (activity.isThrowingSignalEvent()) {
          problems.add(new Problem("Process " + process.getName() + " cannot start with a throw signal event",
              Problem.SEVERITY_ERROR));
        } else if (activity.isThrowingSignalEvent()) {
          problems.add(new Problem("Process " + process.getName() + " cannot start with a throw error event",
              Problem.SEVERITY_ERROR));
        } else if (activity.isCatchingErrorEvent() && ProcessType.PROCESS.equals(process.getType())) {
          problems.add(new Problem("Process " + process.getName()
              + " cannot start with a catch error event because it is not an event sub-process.",
              Problem.SEVERITY_ERROR));
        }
      }
    }
  }

  private void setSubProcesses(final ProcessDefinitionImpl process) {
    final Set<String> subProcesses = new HashSet<String>();
    for (final ActivityDefinition activity : process.getActivities()) {
      if (activity.isSubflow()) {
        subProcesses.add(activity.getSubflowProcessName());
      }
    }
    if (!subProcesses.isEmpty()) {
      process.setSubProcesses(subProcesses);
    }
  }

  private void checkEventSubProcess(final ProcessDefinitionImpl process) {
    if (ProcessType.EVENT_SUB_PROCESS.equals(process.getType())) {
      for (final ActivityDefinition activity : process.getActivities()) {
        if (activity.getIncomingTransitions().size() == 0) {
          if (!(activity.isTimer() || activity.isCatchingSignalEvent() || activity.isReceiveEvent() || activity
              .isCatchingErrorEvent())) {
            problems.add(new Problem("Process " + process.getName() + " must not begin with a \"none\" start event",
                Problem.SEVERITY_ERROR));
          }
        }
      }
    }
  }

  private void checkDefaultTransitions(final ProcessDefinitionImpl process) {
    for (final ActivityDefinition activity : process.getActivities()) {
      boolean hasDefault = false;
      final Set<TransitionDefinition> outgoingTransitions = activity.getOutgoingTransitions();
      if (!outgoingTransitions.isEmpty()) {
        for (final TransitionDefinition tr : activity.getOutgoingTransitions()) {
          if (tr.isDefault()) {
            if (hasDefault) {
              problems.add(new Problem("Activity " + activity.getName() + " has multiple default outgoing transitions",
                  Problem.SEVERITY_ERROR));
            }
            if (tr.getCondition() != null) {
              problems.add(new Problem("Activity " + activity.getName() + " has a default condition with a condition:"
                  + tr.getName(), Problem.SEVERITY_ERROR));
            }
            hasDefault = true;
          }
        }
      }
    }
  }

  private void checkExceptionsTransitions(final ProcessDefinitionImpl process) {
    for (final ActivityDefinition activity : process.getActivities()) {
      final List<BoundaryEvent> boundaryEvents = activity.getBoundaryEvents();
      for (final BoundaryEvent boundaryEvent : boundaryEvents) {
        if (boundaryEvent.getTransition() == null) {
          problems.add(new Problem("The boundary event '" + boundaryEvent.getName() + "' on activity '"
              + activity.getName() + "' must have an exception transition", Problem.SEVERITY_ERROR));
        }
      }
    }
  }

  private void checkMultiInstantiation(final ProcessDefinitionImpl process) {
    for (final ActivityDefinition activity : process.getActivities()) {
      final MultiInstantiationDefinition multiInstantiationDefinition = activity.getMultiInstantiationDefinition();
      final MultiInstantiationDefinition instantiator = activity.getMultipleInstancesInstantiator();
      final MultiInstantiationDefinition joinChecker = activity.getMultipleInstancesJoinChecker();

      if (instantiator != null || joinChecker != null || multiInstantiationDefinition != null) {
        if (multiInstantiationDefinition != null) {
          final String variableId = multiInstantiationDefinition.getVariableName();
          boolean variableExists = false;
          if (activity.getDataFields() != null) {
            for (final DataFieldDefinition var : activity.getDataFields()) {
              if (var.getName().equals(variableId)) {
                variableExists = true;
                break;
              }
            }
          }
          if (!variableExists) {
            problems.add(new Problem("MultiInstantiation variable " + variableId
                + " must be a local variable of activity " + activity.getName(), Problem.SEVERITY_ERROR));
          }
        } else if (instantiator != null && joinChecker == null) {
          problems.add(new Problem("JoinChecker is undefined", Problem.SEVERITY_ERROR));
        } else if (instantiator == null && joinChecker != null) {
          problems.add(new Problem("Instantiator is undefined", Problem.SEVERITY_ERROR));
        }
      }
    }
  }

  private boolean isConnector(final Object obj) {
    return obj instanceof ConnectorDefinitionImpl;
  }

  private boolean isDescriptionElement(final Object obj) {
    return obj instanceof DescriptionElementImpl;
  }

  private boolean isNamedElement(final Object obj) {
    return obj instanceof NamedElementImpl;
  }

  private boolean isActivity(final Object obj) {
    return obj instanceof ActivityDefinitionImpl;
  }

  private boolean isMessageBoundaryEvent(final Object obj) {
    return obj instanceof MessageBoundaryEventImpl;
  }

  private boolean isProcess(final Object obj) {
    return obj instanceof ProcessDefinitionImpl;
  }

  private boolean isParticipant(final Object obj) {
    return obj instanceof ParticipantDefinitionImpl;
  }

  private boolean isTransition(final Object obj) {
    return obj instanceof TransitionDefinitionImpl;
  }

  private boolean isDataFieldDefinition(final Object obj) {
    return obj instanceof DataFieldDefinitionImpl;
  }

  private ProcessDefinitionImpl getProcess() {
    return (ProcessDefinitionImpl) stack.get(0);
  }

  private void push(final Object obj) {
    stack.push(obj);
  }

  private Object peek(final Class<?> clazz) {
    Misc.checkArgsNotNull(clazz);
    while (stack.size() > 1) {
      if (clazz.isAssignableFrom(stack.peek().getClass())) {
        return stack.peek();
      }
      stack.pop();
    }
    return stack.peek();
  }

  private String getClass(final Object obj) {
    if (obj == null) {
      return null;
    }
    final Class<?> clazz = obj.getClass();
    final Class<?>[] itf = clazz.getInterfaces();
    if (itf == null || itf.length == 0) {
      return clazz.toString();
    }
    return itf[0].getName();
  }

  private void checkNames() {
    final ProcessDefinitionImpl definition = (ProcessDefinitionImpl) stack.get(0);
    final Set<ParticipantDefinition> groups = definition.getParticipants();
    if (groups != null) {
      final List<String> groupNames = new ArrayList<String>();
      for (final ParticipantDefinition group : groups) {
        final String groupName = group.getName();
        if (groupNames.contains(groupName)) {
          problems.add(new Problem("Impossible to have more than one group/human with the name: " + groupName,
              Problem.SEVERITY_ERROR));
        } else {
          groupNames.add(groupName);
        }
      }
    }
    final Set<DataFieldDefinition> processDataFields = definition.getDataFields();
    final List<String> dataFieldNames = new ArrayList<String>();
    if (processDataFields != null) {
      for (final DataFieldDefinition datafield : processDataFields) {
        final String dataFieldName = datafield.getName();
        if (dataFieldNames.contains(dataFieldName)) {
          problems.add(new Problem("Impossible to have more than one data with the name: " + dataFieldName,
              Problem.SEVERITY_ERROR));
        } else {
          dataFieldNames.add(dataFieldName);
        }
      }
    }
    final Map<String, AttachmentDefinition> attachments = definition.getAttachments();
    if (attachments != null) {
      for (final String attachmentName : attachments.keySet()) {
        if (dataFieldNames.contains(attachmentName)) {
          problems.add(new Problem("Impossible to have data and an attachment with the same name: " + attachmentName,
              Problem.SEVERITY_ERROR));
        }
      }
    }

    final Set<TransitionDefinition> transitions = definition.getTransitions();
    if (transitions != null) {
      final List<String> transitionNames = new ArrayList<String>();
      for (final TransitionDefinition transition : transitions) {
        final String transitionName = transition.getName();
        if (transitionNames.contains(transitionName)) {
          problems.add(new Problem("Impossible to have more than one transition with the name: " + transitionName,
              Problem.SEVERITY_ERROR));
        } else {
          transitionNames.add(transitionName);
        }
      }
    }
    final Set<ActivityDefinition> activities = definition.getActivities();
    if (activities != null) {
      final List<String> activityNames = new ArrayList<String>();
      for (final ActivityDefinition activity : activities) {
        final String activityName = activity.getName();
        if (activityNames.contains(activityName)) {
          problems.add(new Problem("Impossible to have more than one activity with the name: " + activityName,
              Problem.SEVERITY_ERROR));
        } else {
          activityNames.add(activityName);
        }
        final Set<DataFieldDefinition> activityDataFields = activity.getDataFields();
        if (activityDataFields != null) {
          final List<String> activityDataNames = new ArrayList<String>();
          for (final DataFieldDefinition activityDataField : activityDataFields) {
            final String activityDataName = activityDataField.getName();
            if (activityDataNames.contains(activityDataName)) {
              problems.add(new Problem("Impossible to have more than one data with name:" + activityDataName
                  + " in the same activity", Problem.SEVERITY_ERROR));
            } else {
              activityDataNames.add(activityDataName);
            }
          }
        }
        final List<BoundaryEvent> events = activity.getBoundaryEvents();
        for (final BoundaryEvent boundaryEvent : events) {
          if (boundaryEvent.getTransition() == null) {
            problems.add(new Problem("The boundary event " + boundaryEvent
                + " must have an outgoing transition in activity " + activityName + ".", Problem.SEVERITY_ERROR));
          }
        }

        if (activity.isTerminateProcess() && !activity.getOutgoingTransitions().isEmpty()) {
          problems.add(new Problem("The activity " + activityName
              + " cannot terminate a process with outgoing transitions", Problem.SEVERITY_ERROR));
        }

        if (activity.isSendEvents() && activity.getOutgoingEvents().isEmpty()) {
          problems.add(new Problem("The send message event " + activityName + " must throw at least a message",
              Problem.SEVERITY_ERROR));
        }
      }
    }
  }

  private void checkTransientVariables() {
    final ProcessDefinitionImpl definition = (ProcessDefinitionImpl) stack.get(0);
    for (final DataFieldDefinition data : definition.getDataFields()) {
      if (data.isTransient()) {
        problems.add(new Problem("The process variable " + data.getName()
            + " cannot be setted as transient. Only activity variables can be transient.", Problem.SEVERITY_ERROR));
      }
    }
  }

  public static ProcessDefinition createProcessFromXmlDefFile(final URL xmlDefUrl, final Properties context) {
    final Parse parse = new XmlDefParser().createParse();
    parse.setContextProperties(context);
    File file = null;
    try {
      final InputStream in = xmlDefUrl.openStream();
      final BufferedReader reader = new BufferedReader(new InputStreamReader(in, BonitaConstants.FILE_ENCONDING));
      String line = null;
      file = Misc.createTempFile("tempProcessDef", ".xml", new File(BonitaConstants.getTemporaryFolder()));
      final FileOutputStream fos = new FileOutputStream(file);
      final OutputStream bos = new BufferedOutputStream(fos);
      final OutputStreamWriter out = new OutputStreamWriter(bos, "UTF-8");
      final String productVersionTag = "<process-definition product-version=\"";
      String productVersion = null;
      while ((line = reader.readLine()) != null) {
        if (line.contains(productVersionTag)) {
          final int startIndex = line.indexOf(productVersionTag) + productVersionTag.length();
          final int lastIndex = line.indexOf("\">");
          productVersion = line.substring(startIndex, lastIndex);
        }
        line = resolveWithContext(line, context);
        out.write(line);
      }
      Misc.close(reader);
      Misc.close(in);
      Misc.close(out);
      parse.setFile(file);
      if (!XmlDefExporter.PRODUCT_VERSION.equals(productVersion)) {
        final String message = "The given business archive was created with a different version of BOS. Please use the current BOS version ("
            + XmlDefExporter.PRODUCT_VERSION + ") to export your process";
        file.delete();
        Misc.showProblems(Collections.singleton(new Problem(message, Problem.SEVERITY_FATALERROR)), " your xml");
      }
      final ProcessDefinition process = (ProcessDefinition) parse.execute().getDocumentObject();
      file.delete();
      Misc.showProblems(parse.getProblems(), "xml file");
      Misc.badStateIfNull(process, "Ouch! The returned Clientprocess is null!");
      return process;
    } catch (final IOException ex) {
      throw new BonitaRuntimeException(ex);
    }
  }

  static String resolveWithContext(final String value, final Properties context) {
    if (!value.contains(BonitaConstants.CONTEXT_PREFIX)) {
      return value;
    } else {
      final StringBuilder builder = new StringBuilder(value);
      int startIndex = 0;
      while ((startIndex = builder.indexOf(BonitaConstants.CONTEXT_PREFIX, startIndex)) != -1) {
        final int closingIndex = builder.indexOf(BonitaConstants.CONTEXT_SUFFIX, startIndex);
        final String key = builder.substring(startIndex + BonitaConstants.CONTEXT_PREFIX.length(), closingIndex);
        if (context.containsKey(key)) {
          builder.replace(startIndex, closingIndex + 1, context.get(key).toString());
        }
        startIndex++;
      }
      return builder.toString();
    }
  }

  private boolean canAddBoundaryEvent(final ActivityDefinition activity, final String eventType) {
    boolean can = false;
    if (activity.isAutomatic() && "error".equals(eventType)) {
      can = true;
    }
    if (activity.isSubflow() || activity.isTask() || activity.isReceiveEvent()) {
      can = true;
    }
    return can;
  }

}
