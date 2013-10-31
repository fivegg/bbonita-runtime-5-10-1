/**
 * Copyright (C) 2010 BonitaSoft S.A.
 * BonitaSoft, 31 rue Gustave Eiffel - 38000 Grenoble
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.ow2.bonita.integration.connector;

import java.util.ArrayList;
import java.util.List;

import org.ow2.bonita.APITestCase;
import org.ow2.bonita.facade.def.element.HookDefinition.Event;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.integration.connector.test.OutputConnector;
import org.ow2.bonita.util.BonitaConstants;
import org.ow2.bonita.util.ProcessBuilder;
import org.w3c.dom.Document;

/**
 * @author Mickael Istria
 *
 */
public class ComplexConnectorOutputTargetTest extends APITestCase {

	  public void testComplexJavaOutputTarget() throws Exception {
	    ProcessDefinition definition = ProcessBuilder.createProcess("complexJavaOutputTarget", "1.0")
	      .addHuman(getLogin())
	      .addObjectData("javaList", ArrayList.class.getName(), "${new ArrayList()}")
	      .addHumanTask("task", getLogin())
	        .addConnector(Event.taskOnReady, OutputConnector.class.getName(), true)
	          .addOutputParameter("output", "javaList" + BonitaConstants.JAVA_VAR_SEPARATOR + BonitaConstants.JAVA_VAR_SEPARATOR + "add")
	    .done();

	    definition = getManagementAPI().deploy(getBusinessArchive(definition, null, OutputConnector.class));
	    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(definition.getUUID());
	    executeTask(instanceUUID, "task");
	    List<?> javaList = (List<?>) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "javaList");
	    assertEquals("Something", javaList.get(0));
	    
	    getManagementAPI().deleteProcess(definition.getUUID());
	  }
	  
	  public void testComplexXMLOutputTarget() throws Exception {
		    ProcessDefinition definition = ProcessBuilder.createProcess("complexXMLOutputTarget", "1.0")
		      .addHuman(getLogin())
		      .addXMLData("xmlEmployee", "<Employee><Name>before</Name></Employee>")
		      .addHumanTask("task", getLogin())
		        .addConnector(Event.taskOnReady, OutputConnector.class.getName(), true)
		          .addOutputParameter("output", "xmlEmployee" + BonitaConstants.XPATH_VAR_SEPARATOR + "/Employee/Name/text()")
		    .done();

		    definition = getManagementAPI().deploy(getBusinessArchive(definition, null, OutputConnector.class));
		    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(definition.getUUID());
		    executeTask(instanceUUID, "task");
		    Document doc = (Document) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "xmlEmployee");
		    assertEquals("Something", doc.getFirstChild().getFirstChild().getTextContent());
		    
		    getManagementAPI().deleteProcess(definition.getUUID());
		  }
}
