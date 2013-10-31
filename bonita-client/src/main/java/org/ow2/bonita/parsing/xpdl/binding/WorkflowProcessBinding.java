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
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ow2.bonita.util.ProcessBuilder;
import org.ow2.bonita.util.xml.Parse;
import org.ow2.bonita.util.xml.Parser;
import org.ow2.bonita.util.xml.XmlUtil;
import org.w3c.dom.Element;

/**
 * @author Marc Blachon, Guillaume Porcher, Charles Souillard, Miguel Valdes, Pierre Vigneras
 */
public class WorkflowProcessBinding extends MajorElementBinding {

  private static final Logger LOG = Logger.getLogger(WorkflowProcessBinding.class.getName());

  public WorkflowProcessBinding() {
    super("WorkflowProcess");
  }

  @Override
  public Object parse(final Element workflowProcessElement, final Parse parse, final Parser parser) {
    if (LOG.isLoggable(Level.FINE)) {
      LOG.fine("parsing element = " + workflowProcessElement);
    }
    final String id = getId(workflowProcessElement);
    final Element processHeaderElement = XmlUtil.element(workflowProcessElement, "ProcessHeader");
    final String description = getChildTextContent(processHeaderElement, "Description");

    final Element redefinableHeaderElement = XmlUtil.element(workflowProcessElement, "RedefinableHeader");
    String version = getChildTextContent(redefinableHeaderElement, "Version");
    if (version == null) {
      // use package version
      final Element packageElement = (Element) workflowProcessElement.getParentNode().getParentNode();
      final Element packageRedefinableHeaderElement = XmlUtil.element(packageElement, "RedefinableHeader");
      version = getChildTextContent(packageRedefinableHeaderElement, "Version");
    }

    final ProcessBuilder processBuilder = ProcessBuilder.createProcess(id, version);
    processBuilder.addDescription(description);
    processBuilder.addLabel(XmlUtil.attribute(workflowProcessElement, "Name"));

    parse.pushObject(processBuilder);

    parseFormalParameters(workflowProcessElement, parse, parser, processBuilder);

    parseXpdlMajorElementList(workflowProcessElement, "Participants", "Participant", parse, parser);
    final Collection<Element> activityDatafields = parseXpdlMajorElementList(workflowProcessElement, "DataFields",
        "DataField", parse, parser);
    parse.pushObject(activityDatafields);
    parse.pushObject(Boolean.TRUE);
    parseXpdlMajorElementList(workflowProcessElement, "Activities", "Activity", parse, parser);
    parse.popObject();
    parse.popObject();
    parseXpdlMajorElementList(workflowProcessElement, "Transitions", "Transition", parse, parser);
    parse.popObject();
    // parse iterations
    parseIterationElements(workflowProcessElement, processBuilder);
    return processBuilder.done();
  }

  /**
   * Parse iteration transitions (inherited from bonita 3)
   * 
   * @param activityElement
   * @return
   */
  private void parseIterationElements(final Element workflowProcessElement, final ProcessBuilder processBuilder) {
    final Element activities = XmlUtil.element(workflowProcessElement, "Activities");
    if (activities != null) {
      for (final Element activity : XmlUtil.elements(activities, "Activity")) {
        final Set<Element> iterationElements = getExtendedAttributes(activity, "Iteration");
        if (iterationElements != null) {
          for (final Element iterationElement : iterationElements) {
            final String iterationCondition = XmlUtil.attribute(iterationElement, "Value");
            final String iterationTo = getChildTextContent(iterationElement, "To");
            final String activityName = getId(activity);
            processBuilder.addTransition(activityName + "_" + iterationTo, activityName, iterationTo);
            processBuilder.addCondition(iterationCondition);
          }
        }
      }
    }
  }

  /**
   * returns a list of formal parameters: parameters are ordered and index attribute is not required (index attribute
   * has been removed in Xpdl 2) TODO: check that a formal parameter in a workflow process does not hide a data field
   * name.
   */
  protected void parseFormalParameters(final Element fatherElement, final Parse parse, final Parser parser,
      final ProcessBuilder processBuilder) {
    final Element formalParametersElement = XmlUtil.element(fatherElement, "FormalParameters");
    if (formalParametersElement != null) {
      parse.addWarning("Process FormalParameters not yet supported.");
    }
  }
}
