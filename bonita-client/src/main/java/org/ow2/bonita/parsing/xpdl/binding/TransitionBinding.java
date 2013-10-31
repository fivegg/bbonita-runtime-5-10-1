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

import java.util.List;
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
public class TransitionBinding extends MajorElementBinding {

  public TransitionBinding() {
    super("Transition");
  }

  private static final Logger LOG = Logger.getLogger(TransitionBinding.class.getName());

  public Object parse(final Element transitionElement, final Parse parse, final Parser parser) {
    if (LOG.isLoggable(Level.FINE)) {
      LOG.fine("parsing element = " + transitionElement);
    }
    final String id = getId(transitionElement);
    final String description = getChildTextContent(transitionElement, "Description");
    final String from = XmlUtil.attribute(transitionElement, "From");
    final String to = XmlUtil.attribute(transitionElement, "To");
    ProcessBuilder processBuilder = parse.findObject(ProcessBuilder.class);
    processBuilder.addTransition(id, from, to);
    processBuilder.addDescription(description);
    processBuilder.addLabel(XmlUtil.attribute(transitionElement, "Name"));
    
    final Element conditionElement = XmlUtil.element(transitionElement, "Condition");
    if (conditionElement != null) {
      final String contentAsString = conditionElement.getTextContent();
      final List<Element> xpressionElements = XmlUtil.elements(conditionElement, "Xpression");
      if (xpressionElements != null && !xpressionElements.isEmpty()) {
        parse.addProblem("Element Xpression not supported on Condition element");
      }
      processBuilder.addCondition(contentAsString);
    }
    return null;
  }
}
