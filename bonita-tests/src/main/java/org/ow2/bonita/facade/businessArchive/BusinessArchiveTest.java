package org.ow2.bonita.facade.businessArchive;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.ow2.bonita.APITestCase;
import org.ow2.bonita.facade.def.element.BusinessArchive;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.runtime.ActivityState;
import org.ow2.bonita.facade.uuid.ActivityInstanceUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.util.BonitaConstants;
import org.ow2.bonita.util.BonitaException;
import org.ow2.bonita.util.BusinessArchiveFactory;
import org.ow2.bonita.util.GroovyExpression;
import org.ow2.bonita.util.Misc;
import org.ow2.bonita.util.ProcessBuilder;

public class BusinessArchiveTest extends APITestCase {

	public void testBusinessArchive() throws BonitaException, IOException, ClassNotFoundException {
		// xpdl, class, properties, xml, jar

		final String xpdl = "businessArchive.xpdl";
		final ProcessDefinition clientProcess = ProcessBuilder.createProcessFromXpdlFile(this.getClass().getResource(xpdl));
		
		final String jar1 = "jar1.jar";
		final byte[] jar1Content = Misc.generateJar(MyClass1.class, MyClass2.class);
		final String jar2 = "dir1/jar2.jar";
		final byte[] jar2Content = Misc.generateJar(MyClass2.class, MyClass3.class);
		final String form1 = "form1.xml";
		final byte[] form1Content = Misc.getAllContentFrom(this.getClass().getResource("form1.xml"));
		final String form2 = "dir1/form2.xml";
		final byte[] form2Content = Misc.getAllContentFrom(this.getClass().getResource("form2.xml"));
		final String properties = "myProps.properties";
		final byte[] propertiesContent = Misc.getAllContentFrom(this.getClass().getResource("myProps.properties"));

		Map<String, byte[]> resources = new HashMap<String, byte[]>();
		resources.put(jar1, jar1Content);
		resources.put(jar2, jar2Content);
		resources.put(form1, form1Content);
		resources.put(form2, form2Content);
		resources.put(properties, propertiesContent);

		BusinessArchive ba = BusinessArchiveFactory.getBusinessArchive(clientProcess, resources);
		ProcessDefinition process = getManagementAPI().deploy(ba);

		BusinessArchive businessArchive = getQueryDefinitionAPI().getBusinessArchive(process.getUUID());

		assertNotNull(businessArchive.getProcessDefinition());

		Map<String, byte[]> baJarFiles = businessArchive.getJarFiles();
		assertNotNull(baJarFiles);
		assertEquals(2, baJarFiles.size());
		for (Map.Entry<String, byte[]> r : baJarFiles.entrySet()) {
			if (r.getKey().equals(jar1)) {
				Arrays.equals(jar1Content, r.getValue());
			} else if (r.getKey().equals(jar2)) {
				Arrays.equals(jar2Content, r.getValue());
			} else {
				fail("Unexpected jar path: " + r.getKey());
			}
		}

		Map<String, byte[]> baResources = businessArchive.getResources();
		assertNotNull(baResources);
		assertEquals(resources.size() + 1, baResources.size());
		Collection<String> baResourcesPath = new ArrayList<String>();
		for (Map.Entry<String, byte[]> r : baResources.entrySet()) {
			baResourcesPath.add(r.getKey());

		}

		assertTrue("baResources does not contain:" + jar1, baResourcesPath.contains(jar1));
		assertTrue("baResources does not contain:" + jar2, baResourcesPath.contains(jar2));
		assertTrue("baResources does not contain:" + form1, baResourcesPath.contains(form1));
		assertTrue("baResources does not contain:" + form2, baResourcesPath.contains(form2));
		assertTrue("baResources does not contain:" + properties, baResourcesPath.contains(properties));

		byte[] baForm2 = businessArchive.getResource(form2);
		assertNotNull(baForm2);
		Arrays.equals(form2Content, baForm2);

		Map<String, byte[]> baXmlFiles = businessArchive.getResources(".*\\.xml");
		assertNotNull(baXmlFiles);
		assertEquals(3, baXmlFiles.size());
		for (Map.Entry<String, byte[]> r : baXmlFiles.entrySet()) {
			if (r.getKey().equals(form1)) {
				Arrays.equals(form1Content, r.getValue());
			} else if (r.getKey().equals(form2)) {
				Arrays.equals(form2Content, r.getValue());
			} else if (!r.getKey().equals("process-def.xml")){
				fail("Unexpected xml path: " + r.getKey());
			}
		}
		
		Map<String, byte[]> baDir1Files = businessArchive.getResources("^dir1/.*");
		assertNotNull(baDir1Files);
		assertEquals(2, baDir1Files.size());
		for (Map.Entry<String, byte[]> r : baDir1Files.entrySet()) {
			if (r.getKey().equals(jar2)) {
				Arrays.equals(jar2Content, r.getValue());
			} else if (r.getKey().equals(form2)) {
				Arrays.equals(form2Content, r.getValue());
			} else {
				fail("Unexpected dir1/ path: " + r.getKey());
			}
		}

		getManagementAPI().deleteProcess(process.getUUID());
	}
	
	public void testBusinessArchiveWithContexts() throws Exception {
		byte[] context1 = Misc.getAllContentFrom(this.getClass().getResource("context1.properties"));
		byte[] context2 = Misc.getAllContentFrom(this.getClass().getResource("context2.properties"));
		
		Map<String, byte[]> resources = new HashMap<String, byte[]>();
		resources.put(BonitaConstants.CONTEXTS_FOLDER_IN_BAR + "context1.properties", context1);
		resources.put(BonitaConstants.CONTEXTS_FOLDER_IN_BAR + "context2.properties", context2);
		
		ProcessBuilder builder = ProcessBuilder.createProcess("testBusinessArchiveWithContexts", "0.1");
		builder.
			addHuman(getLogin()).
			addStringData("data1", BonitaConstants.CONTEXT_PREFIX + "key1" + BonitaConstants.CONTEXT_SUFFIX).
			addHumanTask("task", getLogin()).
				addStringDataFromScript("data2", GroovyExpression.START_DELIMITER + "\"" + BonitaConstants.CONTEXT_PREFIX + "key2" + BonitaConstants.CONTEXT_SUFFIX + "\"" + GroovyExpression.END_DELIMITER);
		ProcessDefinition devProcessDef = builder.done();
		
		BusinessArchive bar = BusinessArchiveFactory.getBusinessArchive(devProcessDef, resources);
		ProcessDefinition processDef = getManagementAPI().deploy(bar);
		
		ProcessInstanceUUID processInstance = getRuntimeAPI().instantiateProcess(processDef.getUUID());
		ActivityInstanceUUID activityInstance = getQueryRuntimeAPI().getOneTask(processInstance, ActivityState.READY);
		String data1 = (String) getQueryRuntimeAPI().getVariable(activityInstance, "data1");
		String data2 = (String) getQueryRuntimeAPI().getVariable(activityInstance, "data2");
		assertEquals("value1", data1);
		assertEquals("value2", data2);
		
		getManagementAPI().deleteProcess(processDef.getUUID());
	}

	private static class MyClass1 { }
	private static class MyClass2 { }
	private static class MyClass3 { }

}
