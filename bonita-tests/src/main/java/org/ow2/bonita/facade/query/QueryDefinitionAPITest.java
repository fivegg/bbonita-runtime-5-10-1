/**
 * Copyright (C) 2009  BonitaSoft S.A..
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
package org.ow2.bonita.facade.query;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ow2.bonita.APITestCase;
import org.ow2.bonita.facade.def.element.BusinessArchive;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition.ProcessState;
import org.ow2.bonita.facade.exception.ProcessNotFoundException;
import org.ow2.bonita.facade.paging.ProcessDefinitionCriterion;
import org.ow2.bonita.facade.uuid.ActivityDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.light.LightProcessDefinition;
import org.ow2.bonita.util.Misc;
import org.ow2.bonita.util.ProcessBuilder;

/**
 * @author Charles Souillard, Matthieu Chaffotte
 */
public class QueryDefinitionAPITest extends APITestCase {

  public void testGetProcessPagesNumber() throws Exception {
    final int size = 33;
    for (int i = 0 ; i < size ; i++) {
      ProcessDefinition p = ProcessBuilder.createProcess("p" + i, "1.0")
      .addSystemTask("s")
      .done();
      getManagementAPI().deploy(getBusinessArchive(p));
    }
    assertEquals(16, getQueryDefinitionAPI().getLightProcesses(0, 16).size());
    assertEquals(33, getQueryDefinitionAPI().getLightProcesses(0, 50).size());

    assertEquals(16, getQueryDefinitionAPI().getLightProcesses(0, 16).size());
    assertEquals(33, getQueryDefinitionAPI().getLightProcesses(0, 50).size());

    getManagementAPI().deleteAllProcesses();
  }

  public void testGetProcessPagesOrder() throws Exception {
    ProcessDefinition p1 = ProcessBuilder.createProcess("p1", "1.0")
    .addSystemTask("s")
    .done();

    ProcessDefinition p2 = ProcessBuilder.createProcess("p2", "1.0")
    .addLabel("a")
    .addSystemTask("s")
    .done();

    ProcessDefinition p3 = ProcessBuilder.createProcess("p3", "1.0")
    .addLabel("b")
    .addSystemTask("s")
    .done();

    //ORDER MUST BE: p2, p3, p1
    getManagementAPI().deploy(getBusinessArchive(p1));
    getManagementAPI().deploy(getBusinessArchive(p2));
    getManagementAPI().deploy(getBusinessArchive(p3));

    List<LightProcessDefinition> lightProcesses = getQueryDefinitionAPI().getLightProcesses(0, 20);
    assertEquals(3, lightProcesses.size());
    assertEquals(lightProcesses.get(0).getUUID(), p2.getUUID());
    assertEquals(lightProcesses.get(1).getUUID(), p3.getUUID());
    assertEquals(lightProcesses.get(2).getUUID(), p1.getUUID());

    getManagementAPI().deleteAllProcesses();
  }

  public void testGetAvailableResource() throws Exception {
    ProcessDefinition definition =
      ProcessBuilder.createProcess("resource", "1.1")
      .addHuman(getLogin())
      .addHumanTask("rest", getLogin())
      .done();

    String resource = "bonita4.png";
    InputStream in = this.getClass().getResourceAsStream(resource);
    byte[] content = Misc.getAllContentFrom(in);
    in.close();

    Map<String, byte[]> resources = new HashMap<String, byte[]>();
    resources.put("bonita4.png", content);

    definition = getManagementAPI().deploy(getBusinessArchive(definition, resources));
    assertNotNull(getQueryDefinitionAPI().getResource(definition.getUUID(), resource));

    getManagementAPI().deleteAllProcesses();
  }

  public void testGetUnknownResource() throws Exception {
    ProcessDefinition definition =
      ProcessBuilder.createProcess("resource", "1.1")
      .addHuman(getLogin())
      .addHumanTask("rest", getLogin())
      .done();

    String resource = "bonita4.png";
    InputStream in = this.getClass().getResourceAsStream(resource);
    byte[] content = Misc.getAllContentFrom(in);
    in.close();

    Map<String, byte[]> resources = new HashMap<String, byte[]>();
    resources.put("bonita5.png", content);

    definition = getManagementAPI().deploy(getBusinessArchive(definition, resources));
    assertNull(getQueryDefinitionAPI().getResource(definition.getUUID(), resource));

    getManagementAPI().deleteAllProcesses();
  }

  public void testGetProcessCategoryName() throws Exception {
    final String categoryName = "Nicolas";
    final String categoryName1 = categoryName + "1";
    final String categoryName2 = categoryName + "2";
    final String categoryName3 = categoryName + "3";

    final Set<String> categoryNames = new HashSet<String>();
    categoryNames.add(categoryName);
    categoryNames.add(categoryName1);
    categoryNames.add(categoryName2);
    categoryNames.add(categoryName3);

    ProcessDefinition p = ProcessBuilder.createProcess("processWithACategory" , "1.0")
    .addSystemTask("s")
    .addCategory(categoryName)
    .done();

    BusinessArchive ba = getBusinessArchive(p);
    getManagementAPI().deploy(ba);

    LightProcessDefinition def = getQueryDefinitionAPI().getLightProcess(p.getUUID());
    assertEquals(1, def.getCategoryNames().size());
    assertEquals(categoryName, def.getCategoryNames().iterator().next());

    p = ProcessBuilder.createProcess("processWithoutCategory" , "1.0")
    .addSystemTask("s")
    .done();
    getManagementAPI().deploy(getBusinessArchive(p));
    def = getQueryDefinitionAPI().getLightProcess(p.getUUID());
    assertEquals(0, def.getCategoryNames().size());

    p = ProcessBuilder.createProcess("processWithMultipleCategories" , "1.0")
    .addSystemTask("s")
    .addCategory(categoryName)
    .addCategory(categoryName1)
    .addCategory(categoryName2)
    .addCategory(categoryName3)
    .done();
    getManagementAPI().deploy(getBusinessArchive(p));
    def = getQueryDefinitionAPI().getLightProcess(p.getUUID());
    assertEquals(4, def.getCategoryNames().size());

    p = ProcessBuilder.createProcess("processWithMultipleTimeTheSameCategory" , "1.0")
    .addSystemTask("s")
    .addCategory(categoryName)
    .addCategory(categoryName)
    .done();
    getManagementAPI().deploy(getBusinessArchive(p));
    def = getQueryDefinitionAPI().getLightProcess(p.getUUID());
    assertEquals(1, def.getCategoryNames().size());

    getWebAPI().deleteCategories(categoryNames);
    getManagementAPI().deleteAllProcesses();
  }

  public void testGetProcessesByCategoryName() throws Exception {
    final String categoryName = "Nicolas";
    final String categoryName1 = categoryName + "1";
    final String categoryName2 = categoryName + "2";
    final String categoryName3 = categoryName + "3";

    final Set<String> categoryNames = new HashSet<String>();
    categoryNames.add(categoryName);
    categoryNames.add(categoryName1);
    categoryNames.add(categoryName2);
    categoryNames.add(categoryName3);

    final ProcessDefinitionUUID processWithCategoryUUID;
    ProcessDefinition p = ProcessBuilder.createProcess("processWithACategory" , "1.0")
    .addSystemTask("s")
    .addCategory(categoryName)
    .done();

    processWithCategoryUUID = getManagementAPI().deploy(getBusinessArchive(p)).getUUID();

    Set<ProcessDefinitionUUID> uuids = getQueryDefinitionAPI().getProcessUUIDs(categoryName);
    assertNotNull(uuids);
    assertEquals(1, uuids.size());
    assertEquals(processWithCategoryUUID, uuids.iterator().next());

    p = ProcessBuilder.createProcess("processWithoutCategory" , "1.0")
    .addSystemTask("s")
    .done();
    getManagementAPI().deploy(getBusinessArchive(p));
    uuids = getQueryDefinitionAPI().getProcessUUIDs(categoryName);
    assertNotNull(uuids);
    assertEquals(1, uuids.size());
    assertEquals(processWithCategoryUUID, uuids.iterator().next());

    p = ProcessBuilder.createProcess("processWithMultipleCategories" , "1.0")
    .addSystemTask("s")
    .addCategory(categoryName)
    .addCategory(categoryName1)
    .addCategory(categoryName2)
    .addCategory(categoryName3)
    .done();
    ProcessDefinitionUUID processWithMultipleCategoriesUUID = getManagementAPI().deploy(getBusinessArchive(p)).getUUID();
    uuids = getQueryDefinitionAPI().getProcessUUIDs(categoryName);
    assertNotNull(uuids);
    assertEquals(2, uuids.size());
    assertTrue(uuids.contains(processWithCategoryUUID));
    assertTrue(uuids.contains(processWithMultipleCategoriesUUID));

    p = ProcessBuilder.createProcess("processWithMultipleTimeTheSameCategory" , "1.0")
    .addSystemTask("s")
    .addCategory(categoryName)
    .addCategory(categoryName)
    .done();
    ProcessDefinitionUUID processWithMultipleTimeTheSameCategoryUUID = getManagementAPI().deploy(getBusinessArchive(p)).getUUID();
    uuids = getQueryDefinitionAPI().getProcessUUIDs(categoryName);
    assertNotNull(uuids);
    assertEquals(3, uuids.size());
    assertTrue(uuids.contains(processWithCategoryUUID));
    assertTrue(uuids.contains(processWithMultipleCategoriesUUID));
    assertTrue(uuids.contains(processWithMultipleTimeTheSameCategoryUUID));

    getWebAPI().deleteCategories(categoryNames);
    getManagementAPI().deleteAllProcesses();
  }

  public void testGetTaskUUIDs() throws Exception {
    ProcessDefinition definition = ProcessBuilder.createProcess("resource", "1.1")
    .addHuman(getLogin())
    .addHumanTask("rest", getLogin())
    .addSystemTask("sys1")
    .addSystemTask("sys2")
    .addTransition("sys1", "rest")
    .addTransition("rest", "sys2")
    .done();

    definition = getManagementAPI().deploy(getBusinessArchive(definition));

    try {
      getQueryDefinitionAPI().getProcessTaskUUIDs(new ProcessDefinitionUUID("unknown", "version"));
      fail("This processDefinitionUUID does not exists!");
    } catch (ProcessNotFoundException e) {
      // nothing to do
    } 
    
    Set<ActivityDefinitionUUID> uuids = getQueryDefinitionAPI().getProcessTaskUUIDs(definition.getUUID());
    assertEquals(1, uuids.size());
    ActivityDefinitionUUID uuid = uuids.iterator().next();
    assertEquals(new ActivityDefinitionUUID(definition.getUUID(), "rest"), uuid);

    getManagementAPI().deleteAllProcesses();
  }
  
  public void testGetLigthProcessDefinitionOrderByNameAsc() throws Exception {
    List<ProcessDefinitionUUID> processUUIDs = new ArrayList<ProcessDefinitionUUID>();
    final int size = 6;
    for (int i = 0 ; i < size ; i++) {
      ProcessDefinition p = ProcessBuilder.createProcess("pdobna" + i, "1.0")
      .addSystemTask("s")
      .done();
      processUUIDs.add(getManagementAPI().deploy(getBusinessArchive(p)).getUUID());
    }
    
    List<LightProcessDefinition> retrievedProcess = getQueryDefinitionAPI().getLightProcesses(0, 2, ProcessDefinitionCriterion.NAME_ASC);
    assertEquals(2, retrievedProcess.size());
    assertEquals("pdobna0", retrievedProcess.get(0).getName());    
    assertEquals("pdobna1", retrievedProcess.get(1).getName());
    
    retrievedProcess = getQueryDefinitionAPI().getLightProcesses(2, 2, ProcessDefinitionCriterion.NAME_ASC);
    assertEquals(2, retrievedProcess.size());
    assertEquals("pdobna2", retrievedProcess.get(0).getName());    
    assertEquals("pdobna3", retrievedProcess.get(1).getName());
    
    retrievedProcess = getQueryDefinitionAPI().getLightProcesses(4, 2, ProcessDefinitionCriterion.NAME_ASC);
    assertEquals(2, retrievedProcess.size());
    assertEquals("pdobna4", retrievedProcess.get(0).getName());    
    assertEquals("pdobna5", retrievedProcess.get(1).getName());
    
    getManagementAPI().delete(processUUIDs);
  }
  
  public void testGetLigthProcessDefinitionOrderByLabelAsc() throws Exception {
    List<ProcessDefinitionUUID> processUUIDs = new ArrayList<ProcessDefinitionUUID>();
    final int size = 6;
    for (int i = 0 ; i < size ; i++) {
      ProcessDefinition p = ProcessBuilder.createProcess("pdobla" + i, "1.0")
        .addLabel("My Process" + i)
        .addSystemTask("s")
        .done();      
      processUUIDs.add(getManagementAPI().deploy(getBusinessArchive(p)).getUUID());
    }
    
    List<LightProcessDefinition> retrievedProcess = getQueryDefinitionAPI().getLightProcesses(0, 2, ProcessDefinitionCriterion.LABEL_ASC);
    assertEquals(2, retrievedProcess.size());
    assertEquals("My Process0", retrievedProcess.get(0).getLabel());    
    assertEquals("My Process1", retrievedProcess.get(1).getLabel());
    
    retrievedProcess = getQueryDefinitionAPI().getLightProcesses(2, 2, ProcessDefinitionCriterion.LABEL_ASC);
    assertEquals(2, retrievedProcess.size());
    assertEquals("My Process2", retrievedProcess.get(0).getLabel());    
    assertEquals("My Process3", retrievedProcess.get(1).getLabel());
    
    retrievedProcess = getQueryDefinitionAPI().getLightProcesses(4, 2, ProcessDefinitionCriterion.LABEL_ASC);
    assertEquals(2, retrievedProcess.size());
    assertEquals("My Process4", retrievedProcess.get(0).getLabel());    
    assertEquals("My Process5", retrievedProcess.get(1).getLabel());

    getManagementAPI().delete(processUUIDs);
  }

  public void testGetLigthProcessDefinitionOrderByVersionAsc() throws Exception {
    List<ProcessDefinitionUUID> processUUIDs = new ArrayList<ProcessDefinitionUUID>();
    final int size = 6;
    for (int i = 0 ; i < size ; i++) {
      ProcessDefinition p = ProcessBuilder.createProcess("pdobva" + i, "1." + i)        
        .addSystemTask("s")
        .done();      
      processUUIDs.add(getManagementAPI().deploy(getBusinessArchive(p)).getUUID());
    }

    List<LightProcessDefinition> retrievedProcess = getQueryDefinitionAPI().getLightProcesses(0, 2, ProcessDefinitionCriterion.VERSION_ASC);
    assertEquals(2, retrievedProcess.size());
    assertEquals("1.0", retrievedProcess.get(0).getVersion());    
    assertEquals("1.1", retrievedProcess.get(1).getVersion());

    retrievedProcess = getQueryDefinitionAPI().getLightProcesses(2, 2, ProcessDefinitionCriterion.VERSION_ASC);
    assertEquals(2, retrievedProcess.size());
    assertEquals("1.2", retrievedProcess.get(0).getVersion());    
    assertEquals("1.3", retrievedProcess.get(1).getVersion());

    retrievedProcess = getQueryDefinitionAPI().getLightProcesses(4, 2, ProcessDefinitionCriterion.VERSION_ASC);
    assertEquals(2, retrievedProcess.size());
    assertEquals("1.4", retrievedProcess.get(0).getVersion());    
    assertEquals("1.5", retrievedProcess.get(1).getVersion());

    getManagementAPI().delete(processUUIDs);
  }

  public void testGetLigthProcessDefinitionOrderByStateAsc() throws Exception {
    List<ProcessDefinitionUUID> processUUIDs = new ArrayList<ProcessDefinitionUUID>();
    final int size = 3;
    for (int i = 0 ; i < size ; i++) {
      ProcessDefinition p = ProcessBuilder.createProcess("pdobsa" + i, "1." + i)        
        .addSystemTask("s")
        .done();      
      processUUIDs.add(getManagementAPI().deploy(getBusinessArchive(p)).getUUID());
    }
    
    //disable one process
    getManagementAPI().disable(processUUIDs.get(1));
    
    //archive one process
    getManagementAPI().disable(processUUIDs.get(2));
    getManagementAPI().archive(processUUIDs.get(2));
    
    List<LightProcessDefinition> retrievedProcess = getQueryDefinitionAPI().getLightProcesses(0, 2, ProcessDefinitionCriterion.STATE_ASC);
    assertEquals(2, retrievedProcess.size());
    assertEquals(ProcessState.ARCHIVED, retrievedProcess.get(0).getState());    
    assertEquals(ProcessState.DISABLED, retrievedProcess.get(1).getState());
    
    retrievedProcess = getQueryDefinitionAPI().getLightProcesses(2, 2, ProcessDefinitionCriterion.STATE_ASC);
    assertEquals(1, retrievedProcess.size());
    assertEquals(ProcessState.ENABLED, retrievedProcess.get(0).getState());        
    
    getManagementAPI().delete(processUUIDs);
  }
  
  public void testGetLigthProcessDefinitionOrderByNameDesc() throws Exception {
    List<ProcessDefinitionUUID> processUUIDs = new ArrayList<ProcessDefinitionUUID>();
    final int size = 6;
    for (int i = 0 ; i < size ; i++) {
      ProcessDefinition p = ProcessBuilder.createProcess("pdobna" + i, "1.0")
      .addSystemTask("s")
      .done();
      processUUIDs.add(getManagementAPI().deploy(getBusinessArchive(p)).getUUID());
    }
    
    List<LightProcessDefinition> retrievedProcess = getQueryDefinitionAPI().getLightProcesses(0, 2, ProcessDefinitionCriterion.NAME_DESC);
    assertEquals(2, retrievedProcess.size());
    assertEquals("pdobna5", retrievedProcess.get(0).getName());    
    assertEquals("pdobna4", retrievedProcess.get(1).getName());
    
    retrievedProcess = getQueryDefinitionAPI().getLightProcesses(2, 2, ProcessDefinitionCriterion.NAME_DESC);
    assertEquals(2, retrievedProcess.size());
    assertEquals("pdobna3", retrievedProcess.get(0).getName());    
    assertEquals("pdobna2", retrievedProcess.get(1).getName());
    
    retrievedProcess = getQueryDefinitionAPI().getLightProcesses(4, 2, ProcessDefinitionCriterion.NAME_DESC);
    assertEquals(2, retrievedProcess.size());
    assertEquals("pdobna1", retrievedProcess.get(0).getName());    
    assertEquals("pdobna0", retrievedProcess.get(1).getName());
    
    getManagementAPI().delete(processUUIDs);
  }
  
  public void testGetLigthProcessDefinitionOrderByLabelDesc() throws Exception {
    List<ProcessDefinitionUUID> processUUIDs = new ArrayList<ProcessDefinitionUUID>();
    final int size = 6;
    for (int i = 0 ; i < size ; i++) {
      ProcessDefinition p = ProcessBuilder.createProcess("pdobla" + i, "1.0")
        .addLabel("My Process" + i)
        .addSystemTask("s")
        .done();      
      processUUIDs.add(getManagementAPI().deploy(getBusinessArchive(p)).getUUID());
    }
    
    List<LightProcessDefinition> retrievedProcess = getQueryDefinitionAPI().getLightProcesses(0, 2, ProcessDefinitionCriterion.LABEL_DESC);
    assertEquals(2, retrievedProcess.size());
    assertEquals("My Process5", retrievedProcess.get(0).getLabel());    
    assertEquals("My Process4", retrievedProcess.get(1).getLabel());
    
    retrievedProcess = getQueryDefinitionAPI().getLightProcesses(2, 2, ProcessDefinitionCriterion.LABEL_DESC);
    assertEquals(2, retrievedProcess.size());
    assertEquals("My Process3", retrievedProcess.get(0).getLabel());    
    assertEquals("My Process2", retrievedProcess.get(1).getLabel());
    
    retrievedProcess = getQueryDefinitionAPI().getLightProcesses(4, 2, ProcessDefinitionCriterion.LABEL_DESC);
    assertEquals(2, retrievedProcess.size());
    assertEquals("My Process1", retrievedProcess.get(0).getLabel());    
    assertEquals("My Process0", retrievedProcess.get(1).getLabel());
    
    getManagementAPI().delete(processUUIDs);
  }
  
  public void testGetLigthProcessDefinitionOrderByVersionDesc() throws Exception {
    List<ProcessDefinitionUUID> processUUIDs = new ArrayList<ProcessDefinitionUUID>();
    final int size = 6;
    for (int i = 0 ; i < size ; i++) {
      ProcessDefinition p = ProcessBuilder.createProcess("pdobva" + i, "1." + i)        
        .addSystemTask("s")
        .done();      
      processUUIDs.add(getManagementAPI().deploy(getBusinessArchive(p)).getUUID());
    }
   
    List<LightProcessDefinition> retrievedProcess = getQueryDefinitionAPI().getLightProcesses(0, 2, ProcessDefinitionCriterion.VERSION_DESC);
    assertEquals(2, retrievedProcess.size());
    assertEquals("1.5", retrievedProcess.get(0).getVersion());    
    assertEquals("1.4", retrievedProcess.get(1).getVersion());
    
    retrievedProcess = getQueryDefinitionAPI().getLightProcesses(2, 2, ProcessDefinitionCriterion.VERSION_DESC);
    assertEquals(2, retrievedProcess.size());
    assertEquals("1.3", retrievedProcess.get(0).getVersion());    
    assertEquals("1.2", retrievedProcess.get(1).getVersion());
    
    retrievedProcess = getQueryDefinitionAPI().getLightProcesses(4, 2, ProcessDefinitionCriterion.VERSION_DESC);
    assertEquals(2, retrievedProcess.size());
    assertEquals("1.1", retrievedProcess.get(0).getVersion());    
    assertEquals("1.0", retrievedProcess.get(1).getVersion());
    
    getManagementAPI().delete(processUUIDs);
  }
  
  public void testGetLigthProcessDefinitionOrderByStateDesc() throws Exception {
    List<ProcessDefinitionUUID> processUUIDs = new ArrayList<ProcessDefinitionUUID>();
    final int size = 3;
    for (int i = 0 ; i < size ; i++) {
      ProcessDefinition p = ProcessBuilder.createProcess("pdobsa" + i, "1." + i)        
        .addSystemTask("s")
        .done();      
      processUUIDs.add(getManagementAPI().deploy(getBusinessArchive(p)).getUUID());
    }
    
    //disable one process
    getManagementAPI().disable(processUUIDs.get(1));
    
    //archive one process
    getManagementAPI().disable(processUUIDs.get(2));
    getManagementAPI().archive(processUUIDs.get(2));
    
    List<LightProcessDefinition> retrievedProcess = getQueryDefinitionAPI().getLightProcesses(0, 2, ProcessDefinitionCriterion.STATE_DESC);
    assertEquals(2, retrievedProcess.size());
    assertEquals(ProcessState.ENABLED, retrievedProcess.get(0).getState());    
    assertEquals(ProcessState.DISABLED, retrievedProcess.get(1).getState());
    
    retrievedProcess = getQueryDefinitionAPI().getLightProcesses(2, 2, ProcessDefinitionCriterion.STATE_DESC);
    assertEquals(1, retrievedProcess.size());
    assertEquals(ProcessState.ARCHIVED, retrievedProcess.get(0).getState());        
    
    getManagementAPI().delete(processUUIDs);
  }
  
  public void testGetLigthProcessDefinitionExceptOrderByNameAsc() throws Exception {
    List<ProcessDefinitionUUID> processUUIDs = new ArrayList<ProcessDefinitionUUID>();
    final int size = 6;
    for (int i = 0 ; i < size ; i++) {
      ProcessDefinition p = ProcessBuilder.createProcess("pdobna" + i, "1.0")
      .addSystemTask("s")
      .done();
      processUUIDs.add(getManagementAPI().deploy(getBusinessArchive(p)).getUUID());
    }
    
    final Set<ProcessDefinitionUUID> exceptProcess = new HashSet<ProcessDefinitionUUID>(processUUIDs.subList(4, size));    
    
    List<LightProcessDefinition> retrievedProcess = getQueryDefinitionAPI().getAllLightProcessesExcept(exceptProcess, 0, 2, ProcessDefinitionCriterion.NAME_ASC);
    assertEquals(2, retrievedProcess.size());
    assertEquals("pdobna0", retrievedProcess.get(0).getName());    
    assertEquals("pdobna1", retrievedProcess.get(1).getName());
    
    retrievedProcess = getQueryDefinitionAPI().getAllLightProcessesExcept(exceptProcess, 2, 2, ProcessDefinitionCriterion.NAME_ASC);
    assertEquals(2, retrievedProcess.size());
    assertEquals("pdobna2", retrievedProcess.get(0).getName());    
    assertEquals("pdobna3", retrievedProcess.get(1).getName());
    
    retrievedProcess = getQueryDefinitionAPI().getAllLightProcessesExcept(exceptProcess, 4, 2, ProcessDefinitionCriterion.NAME_ASC);
    assertEquals(0, retrievedProcess.size());
    
    getManagementAPI().delete(processUUIDs);
  }
  
  public void testGetLigthProcessDefinitionExceptOrderByLabelAsc() throws Exception {
    List<ProcessDefinitionUUID> processUUIDs = new ArrayList<ProcessDefinitionUUID>();
    final int size = 6;
    for (int i = 0 ; i < size ; i++) {
      ProcessDefinition p = ProcessBuilder.createProcess("pdeobla" + i, "1.0")
        .addLabel("My Process" + i)
        .addSystemTask("s")
        .done();      
      processUUIDs.add(getManagementAPI().deploy(getBusinessArchive(p)).getUUID());
    }
    
    final Set<ProcessDefinitionUUID> exceptProcess = new HashSet<ProcessDefinitionUUID>(processUUIDs.subList(4, size));
    
    List<LightProcessDefinition> retrievedProcess = getQueryDefinitionAPI().getAllLightProcessesExcept(exceptProcess, 0, 2, ProcessDefinitionCriterion.LABEL_ASC);
    assertEquals(2, retrievedProcess.size());
    assertEquals("My Process0", retrievedProcess.get(0).getLabel());    
    assertEquals("My Process1", retrievedProcess.get(1).getLabel());
    
    retrievedProcess = getQueryDefinitionAPI().getAllLightProcessesExcept(exceptProcess, 2, 2, ProcessDefinitionCriterion.LABEL_ASC);
    assertEquals(2, retrievedProcess.size());
    assertEquals("My Process2", retrievedProcess.get(0).getLabel());    
    assertEquals("My Process3", retrievedProcess.get(1).getLabel());
    
    retrievedProcess = getQueryDefinitionAPI().getAllLightProcessesExcept(exceptProcess, 4, 2, ProcessDefinitionCriterion.LABEL_ASC);
    assertEquals(0, retrievedProcess.size());
    
    getManagementAPI().delete(processUUIDs);
  }
  
  public void testGetLigthProcessDefinitionExceptOrderByVersionAsc() throws Exception {
    List<ProcessDefinitionUUID> processUUIDs = new ArrayList<ProcessDefinitionUUID>();
    final int size = 6;
    for (int i = 0 ; i < size ; i++) {
      ProcessDefinition p = ProcessBuilder.createProcess("pdeobva" + i, "1." + i)        
        .addSystemTask("s")
        .done();      
      processUUIDs.add(getManagementAPI().deploy(getBusinessArchive(p)).getUUID());
    }
   
    final Set<ProcessDefinitionUUID> exceptProcess = new HashSet<ProcessDefinitionUUID>(processUUIDs.subList(4, size));
    
    List<LightProcessDefinition> retrievedProcess = getQueryDefinitionAPI().getAllLightProcessesExcept(exceptProcess, 0, 2, ProcessDefinitionCriterion.VERSION_ASC);
    assertEquals(2, retrievedProcess.size());
    assertEquals("1.0", retrievedProcess.get(0).getVersion());    
    assertEquals("1.1", retrievedProcess.get(1).getVersion());
    
    retrievedProcess = getQueryDefinitionAPI().getAllLightProcessesExcept(exceptProcess, 2, 2, ProcessDefinitionCriterion.VERSION_ASC);
    assertEquals(2, retrievedProcess.size());
    assertEquals("1.2", retrievedProcess.get(0).getVersion());    
    assertEquals("1.3", retrievedProcess.get(1).getVersion());
    
    retrievedProcess = getQueryDefinitionAPI().getAllLightProcessesExcept(exceptProcess, 4, 2, ProcessDefinitionCriterion.VERSION_ASC);
    assertEquals(0, retrievedProcess.size());
    
    getManagementAPI().delete(processUUIDs);
  }
  
  public void testGetLigthProcessDefinitionExceptOrderByStateAsc() throws Exception {
    List<ProcessDefinitionUUID> processUUIDs = new ArrayList<ProcessDefinitionUUID>();
    final int size = 3;
    for (int i = 0 ; i < size ; i++) {
      ProcessDefinition p = ProcessBuilder.createProcess("pdeobsa" + i, "1." + i)        
        .addSystemTask("s")
        .done();      
      processUUIDs.add(getManagementAPI().deploy(getBusinessArchive(p)).getUUID());
    }
    
    //disable one process
    getManagementAPI().disable(processUUIDs.get(1));
    
    final Set<ProcessDefinitionUUID> exceptProcess = Collections.singleton(processUUIDs.get(0));    
    
    List<LightProcessDefinition> retrievedProcess = getQueryDefinitionAPI().getAllLightProcessesExcept(exceptProcess, 0, 2, ProcessDefinitionCriterion.STATE_ASC);
    assertEquals(2, retrievedProcess.size());
    assertEquals(ProcessState.DISABLED, retrievedProcess.get(0).getState());    
    assertEquals(ProcessState.ENABLED, retrievedProcess.get(1).getState());
    
    retrievedProcess = getQueryDefinitionAPI().getAllLightProcessesExcept(exceptProcess, 2, 2, ProcessDefinitionCriterion.STATE_ASC);
    assertEquals(0, retrievedProcess.size());        
    
    getManagementAPI().delete(processUUIDs);
  }
  
  public void testGetLigthProcessDefinitionExceptOrderByNameDesc() throws Exception {
    List<ProcessDefinitionUUID> processUUIDs = new ArrayList<ProcessDefinitionUUID>();
    final int size = 6;
    for (int i = 0 ; i < size ; i++) {
      ProcessDefinition p = ProcessBuilder.createProcess("pdeobna" + i, "1.0")
      .addSystemTask("s")
      .done();
      processUUIDs.add(getManagementAPI().deploy(getBusinessArchive(p)).getUUID());
    }
    
    final Set<ProcessDefinitionUUID> exceptProcess = new HashSet<ProcessDefinitionUUID>(processUUIDs.subList(0, 2));
    
    List<LightProcessDefinition> retrievedProcess = getQueryDefinitionAPI().getAllLightProcessesExcept(exceptProcess, 0, 2, ProcessDefinitionCriterion.NAME_DESC);
    assertEquals(2, retrievedProcess.size());
    assertEquals("pdeobna5", retrievedProcess.get(0).getName());    
    assertEquals("pdeobna4", retrievedProcess.get(1).getName());
    
    retrievedProcess = getQueryDefinitionAPI().getAllLightProcessesExcept(exceptProcess, 2, 2, ProcessDefinitionCriterion.NAME_DESC);
    assertEquals(2, retrievedProcess.size());
    assertEquals("pdeobna3", retrievedProcess.get(0).getName());    
    assertEquals("pdeobna2", retrievedProcess.get(1).getName());
    
    retrievedProcess = getQueryDefinitionAPI().getAllLightProcessesExcept(exceptProcess, 4, 2, ProcessDefinitionCriterion.NAME_DESC);
    assertEquals(0, retrievedProcess.size());
    
    getManagementAPI().delete(processUUIDs);
  }
  
  public void testGetLigthProcessDefinitionExceptOrderByLabelDesc() throws Exception {
    List<ProcessDefinitionUUID> processUUIDs = new ArrayList<ProcessDefinitionUUID>();
    final int size = 6;
    for (int i = 0 ; i < size ; i++) {
      ProcessDefinition p = ProcessBuilder.createProcess("pdeobla" + i, "1.0")
        .addLabel("My Process" + i)
        .addSystemTask("s")
        .done();      
      processUUIDs.add(getManagementAPI().deploy(getBusinessArchive(p)).getUUID());
    }
    
    final Set<ProcessDefinitionUUID> exceptProcess = new HashSet<ProcessDefinitionUUID>(processUUIDs.subList(0, 2));
    
    List<LightProcessDefinition> retrievedProcess = getQueryDefinitionAPI().getAllLightProcessesExcept(exceptProcess, 0, 2, ProcessDefinitionCriterion.LABEL_DESC);
    assertEquals(2, retrievedProcess.size());
    assertEquals("My Process5", retrievedProcess.get(0).getLabel());    
    assertEquals("My Process4", retrievedProcess.get(1).getLabel());
    
    retrievedProcess = getQueryDefinitionAPI().getAllLightProcessesExcept(exceptProcess, 2, 2, ProcessDefinitionCriterion.LABEL_DESC);
    assertEquals(2, retrievedProcess.size());
    assertEquals("My Process3", retrievedProcess.get(0).getLabel());    
    assertEquals("My Process2", retrievedProcess.get(1).getLabel());
    
    retrievedProcess = getQueryDefinitionAPI().getAllLightProcessesExcept(exceptProcess, 4, 2, ProcessDefinitionCriterion.LABEL_DESC);
    assertEquals(0, retrievedProcess.size());
    
    getManagementAPI().delete(processUUIDs);
  }
  
  public void testGetLigthProcessDefinitionExceptOrderByVersionDesc() throws Exception {
    List<ProcessDefinitionUUID> processUUIDs = new ArrayList<ProcessDefinitionUUID>();
    final int size = 6;
    for (int i = 0 ; i < size ; i++) {
      ProcessDefinition p = ProcessBuilder.createProcess("pdeobva" + i, "1." + i)        
        .addSystemTask("s")
        .done();      
      processUUIDs.add(getManagementAPI().deploy(getBusinessArchive(p)).getUUID());
    }

    final Set<ProcessDefinitionUUID> exceptProcess = new HashSet<ProcessDefinitionUUID>(processUUIDs.subList(0, 2));

    List<LightProcessDefinition> retrievedProcess = getQueryDefinitionAPI().getAllLightProcessesExcept(exceptProcess, 0, 2, ProcessDefinitionCriterion.VERSION_DESC);
    assertEquals(2, retrievedProcess.size());
    assertEquals("1.5", retrievedProcess.get(0).getVersion());    
    assertEquals("1.4", retrievedProcess.get(1).getVersion());

    retrievedProcess = getQueryDefinitionAPI().getAllLightProcessesExcept(exceptProcess, 2, 2, ProcessDefinitionCriterion.VERSION_DESC);
    assertEquals(2, retrievedProcess.size());
    assertEquals("1.3", retrievedProcess.get(0).getVersion());    
    assertEquals("1.2", retrievedProcess.get(1).getVersion());

    retrievedProcess = getQueryDefinitionAPI().getAllLightProcessesExcept(exceptProcess, 4, 2, ProcessDefinitionCriterion.VERSION_DESC);
    assertEquals(0, retrievedProcess.size());

    getManagementAPI().delete(processUUIDs);
  }

  public void testGetLigthProcessDefinitionExceptOrderByStateDesc() throws Exception {
    List<ProcessDefinitionUUID> processUUIDs = new ArrayList<ProcessDefinitionUUID>();
    final int size = 3;
    for (int i = 0 ; i < size ; i++) {
      ProcessDefinition p = ProcessBuilder.createProcess("pdeobsa" + i, "1." + i)        
        .addSystemTask("s")
        .done();      
      processUUIDs.add(getManagementAPI().deploy(getBusinessArchive(p)).getUUID());
    }
    final Set<ProcessDefinitionUUID> exceptProcess = Collections.singleton(processUUIDs.get(0));
    //disable one process
    getManagementAPI().disable(processUUIDs.get(1));

    List<LightProcessDefinition> retrievedProcess = getQueryDefinitionAPI().getAllLightProcessesExcept(exceptProcess, 0, 2, ProcessDefinitionCriterion.STATE_DESC);
    assertEquals(2, retrievedProcess.size());
    assertEquals(ProcessState.ENABLED, retrievedProcess.get(0).getState());    
    assertEquals(ProcessState.DISABLED, retrievedProcess.get(1).getState());

    retrievedProcess = getQueryDefinitionAPI().getAllLightProcessesExcept(exceptProcess, 2, 2, ProcessDefinitionCriterion.STATE_DESC);
    assertEquals(0, retrievedProcess.size());

    getManagementAPI().delete(processUUIDs);
  }

  public void testGetLigthProcessDefinitionFromUUIDsOrderByNameAsc() throws Exception {
    List<ProcessDefinitionUUID> processUUIDs = new ArrayList<ProcessDefinitionUUID>();
    final int size = 6;
    for (int i = 0 ; i < size ; i++) {
      ProcessDefinition p = ProcessBuilder.createProcess("pdobna" + i, "2.0")
      .addSystemTask("s")
      .done();
      processUUIDs.add(getManagementAPI().deploy(getBusinessArchive(p)).getUUID());
    }
    
    final Set<ProcessDefinitionUUID> fromProcess = new HashSet<ProcessDefinitionUUID>(processUUIDs.subList(0, 4));    
    
    List<LightProcessDefinition> retrievedProcess = getQueryDefinitionAPI().getLightProcesses(fromProcess, 0, 2, ProcessDefinitionCriterion.NAME_ASC);
    assertEquals(2, retrievedProcess.size());
    assertEquals("pdobna0", retrievedProcess.get(0).getName());    
    assertEquals("pdobna1", retrievedProcess.get(1).getName());
    
    retrievedProcess = getQueryDefinitionAPI().getLightProcesses(fromProcess, 2, 2, ProcessDefinitionCriterion.NAME_ASC);
    assertEquals(2, retrievedProcess.size());
    assertEquals("pdobna2", retrievedProcess.get(0).getName());    
    assertEquals("pdobna3", retrievedProcess.get(1).getName());
    
    retrievedProcess = getQueryDefinitionAPI().getLightProcesses(fromProcess, 4, 2, ProcessDefinitionCriterion.NAME_ASC);
    assertEquals(0, retrievedProcess.size());
    
    getManagementAPI().delete(processUUIDs);
  }
  
  public void testGetLigthProcessDefinitionFromUUIDsOrderByLabelAsc() throws Exception {
    List<ProcessDefinitionUUID> processUUIDs = new ArrayList<ProcessDefinitionUUID>();
    final int size = 6;
    for (int i = 0 ; i < size ; i++) {
      ProcessDefinition p = ProcessBuilder.createProcess("pdeobla" + i, "1.0")
        .addLabel("My Process" + i)
        .addSystemTask("s")
        .done();      
      processUUIDs.add(getManagementAPI().deploy(getBusinessArchive(p)).getUUID());
    }
    
    final Set<ProcessDefinitionUUID> fromProcess = new HashSet<ProcessDefinitionUUID>(processUUIDs.subList(0, 4));
    
    List<LightProcessDefinition> retrievedProcess = getQueryDefinitionAPI().getLightProcesses(fromProcess, 0, 2, ProcessDefinitionCriterion.LABEL_ASC);
    assertEquals(2, retrievedProcess.size());
    assertEquals("My Process0", retrievedProcess.get(0).getLabel());    
    assertEquals("My Process1", retrievedProcess.get(1).getLabel());
    
    retrievedProcess = getQueryDefinitionAPI().getLightProcesses(fromProcess, 2, 2, ProcessDefinitionCriterion.LABEL_ASC);
    assertEquals(2, retrievedProcess.size());
    assertEquals("My Process2", retrievedProcess.get(0).getLabel());    
    assertEquals("My Process3", retrievedProcess.get(1).getLabel());
    
    retrievedProcess = getQueryDefinitionAPI().getLightProcesses(fromProcess, 4, 2, ProcessDefinitionCriterion.LABEL_ASC);
    assertEquals(0, retrievedProcess.size());
    
    getManagementAPI().delete(processUUIDs);
  }
  
  public void testGetLigthProcessDefinitionFromUUIDsOrderByVersionAsc() throws Exception {
    List<ProcessDefinitionUUID> processUUIDs = new ArrayList<ProcessDefinitionUUID>();
    final int size = 6;
    for (int i = 0 ; i < size ; i++) {
      ProcessDefinition p = ProcessBuilder.createProcess("pdeobva" + i, "1." + i)        
        .addSystemTask("s")
        .done();      
      processUUIDs.add(getManagementAPI().deploy(getBusinessArchive(p)).getUUID());
    }
   
    final Set<ProcessDefinitionUUID> fromProcess = new HashSet<ProcessDefinitionUUID>(processUUIDs.subList(0, 4));
    
    List<LightProcessDefinition> retrievedProcess = getQueryDefinitionAPI().getLightProcesses(fromProcess, 0, 2, ProcessDefinitionCriterion.VERSION_ASC);
    assertEquals(2, retrievedProcess.size());
    assertEquals("1.0", retrievedProcess.get(0).getVersion());    
    assertEquals("1.1", retrievedProcess.get(1).getVersion());
    
    retrievedProcess = getQueryDefinitionAPI().getLightProcesses(fromProcess, 2, 2, ProcessDefinitionCriterion.VERSION_ASC);
    assertEquals(2, retrievedProcess.size());
    assertEquals("1.2", retrievedProcess.get(0).getVersion());    
    assertEquals("1.3", retrievedProcess.get(1).getVersion());
    
    retrievedProcess = getQueryDefinitionAPI().getLightProcesses(fromProcess, 4, 2, ProcessDefinitionCriterion.VERSION_ASC);
    assertEquals(0, retrievedProcess.size());
    
    getManagementAPI().delete(processUUIDs);
  }
  
  public void testGetLigthProcessDefinitionFromUUIDsOrderByStateAsc() throws Exception {
    List<ProcessDefinitionUUID> processUUIDs = new ArrayList<ProcessDefinitionUUID>();
    final int size = 3;
    for (int i = 0 ; i < size ; i++) {
      ProcessDefinition p = ProcessBuilder.createProcess("pdeobsa" + i, "1." + i)        
        .addSystemTask("s")
        .done();      
      processUUIDs.add(getManagementAPI().deploy(getBusinessArchive(p)).getUUID());
    }
    
    //disable one process
    getManagementAPI().disable(processUUIDs.get(1));
    
    final Set<ProcessDefinitionUUID> fromProcess = new HashSet<ProcessDefinitionUUID>(processUUIDs.subList(1, processUUIDs.size()));    
    
    List<LightProcessDefinition> retrievedProcess = getQueryDefinitionAPI().getLightProcesses(fromProcess, 0, 2, ProcessDefinitionCriterion.STATE_ASC);
    assertEquals(2, retrievedProcess.size());
    assertEquals(ProcessState.DISABLED, retrievedProcess.get(0).getState());    
    assertEquals(ProcessState.ENABLED, retrievedProcess.get(1).getState());
    
    retrievedProcess = getQueryDefinitionAPI().getLightProcesses(fromProcess, 2, 2, ProcessDefinitionCriterion.STATE_ASC);
    assertEquals(0, retrievedProcess.size());        
    
    getManagementAPI().delete(processUUIDs);
  }
  
  public void testGetLigthProcessDefinitionFromUUIDsOrderByNameDesc() throws Exception {
    List<ProcessDefinitionUUID> processUUIDs = new ArrayList<ProcessDefinitionUUID>();
    final int size = 6;
    for (int i = 0 ; i < size ; i++) {
      ProcessDefinition p = ProcessBuilder.createProcess("pdeobna" + i, "1.0")
      .addSystemTask("s")
      .done();
      processUUIDs.add(getManagementAPI().deploy(getBusinessArchive(p)).getUUID());
    }
    
    final Set<ProcessDefinitionUUID> fromProcess = new HashSet<ProcessDefinitionUUID>(processUUIDs.subList(2, processUUIDs.size()));
    
    List<LightProcessDefinition> retrievedProcess = getQueryDefinitionAPI().getLightProcesses(fromProcess, 0, 2, ProcessDefinitionCriterion.NAME_DESC);
    assertEquals(2, retrievedProcess.size());
    assertEquals("pdeobna5", retrievedProcess.get(0).getName());    
    assertEquals("pdeobna4", retrievedProcess.get(1).getName());
    
    retrievedProcess = getQueryDefinitionAPI().getLightProcesses(fromProcess, 2, 2, ProcessDefinitionCriterion.NAME_DESC);
    assertEquals(2, retrievedProcess.size());
    assertEquals("pdeobna3", retrievedProcess.get(0).getName());    
    assertEquals("pdeobna2", retrievedProcess.get(1).getName());
    
    retrievedProcess = getQueryDefinitionAPI().getLightProcesses(fromProcess, 4, 2, ProcessDefinitionCriterion.NAME_DESC);
    assertEquals(0, retrievedProcess.size());
    
    getManagementAPI().delete(processUUIDs);
  }
  
  public void testGetLigthProcessDefinitionFromUUIDsOrderByLabelDesc() throws Exception {
    List<ProcessDefinitionUUID> processUUIDs = new ArrayList<ProcessDefinitionUUID>();
    final int size = 6;
    for (int i = 0 ; i < size ; i++) {
      ProcessDefinition p = ProcessBuilder.createProcess("pdeobla" + i, "1.0")
        .addLabel("My Process" + i)
        .addSystemTask("s")
        .done();      
      processUUIDs.add(getManagementAPI().deploy(getBusinessArchive(p)).getUUID());
    }
    
    final Set<ProcessDefinitionUUID> fromProcess = new HashSet<ProcessDefinitionUUID>(processUUIDs.subList(2, processUUIDs.size()));
    
    List<LightProcessDefinition> retrievedProcess = getQueryDefinitionAPI().getLightProcesses(fromProcess, 0, 2, ProcessDefinitionCriterion.LABEL_DESC);
    assertEquals(2, retrievedProcess.size());
    assertEquals("My Process5", retrievedProcess.get(0).getLabel());    
    assertEquals("My Process4", retrievedProcess.get(1).getLabel());
    
    retrievedProcess = getQueryDefinitionAPI().getLightProcesses(fromProcess, 2, 2, ProcessDefinitionCriterion.LABEL_DESC);
    assertEquals(2, retrievedProcess.size());
    assertEquals("My Process3", retrievedProcess.get(0).getLabel());    
    assertEquals("My Process2", retrievedProcess.get(1).getLabel());
    
    retrievedProcess = getQueryDefinitionAPI().getLightProcesses(fromProcess, 4, 2, ProcessDefinitionCriterion.LABEL_DESC);
    assertEquals(0, retrievedProcess.size());
    
    getManagementAPI().delete(processUUIDs);
  }
  
  public void testGetLigthProcessDefinitionFromUUIDsOrderByVersionDesc() throws Exception {
    List<ProcessDefinitionUUID> processUUIDs = new ArrayList<ProcessDefinitionUUID>();
    final int size = 6;
    for (int i = 0 ; i < size ; i++) {
      ProcessDefinition p = ProcessBuilder.createProcess("pdeobva" + i, "1." + i)        
        .addSystemTask("s")
        .done();      
      processUUIDs.add(getManagementAPI().deploy(getBusinessArchive(p)).getUUID());
    }
    
    final Set<ProcessDefinitionUUID> fromProcess = new HashSet<ProcessDefinitionUUID>(processUUIDs.subList(2, processUUIDs.size()));
   
    List<LightProcessDefinition> retrievedProcess = getQueryDefinitionAPI().getLightProcesses(fromProcess, 0, 2, ProcessDefinitionCriterion.VERSION_DESC);
    assertEquals(2, retrievedProcess.size());
    assertEquals("1.5", retrievedProcess.get(0).getVersion());    
    assertEquals("1.4", retrievedProcess.get(1).getVersion());
    
    retrievedProcess = getQueryDefinitionAPI().getLightProcesses(fromProcess, 2, 2, ProcessDefinitionCriterion.VERSION_DESC);
    assertEquals(2, retrievedProcess.size());
    assertEquals("1.3", retrievedProcess.get(0).getVersion());    
    assertEquals("1.2", retrievedProcess.get(1).getVersion());
    
    retrievedProcess = getQueryDefinitionAPI().getLightProcesses(fromProcess, 4, 2, ProcessDefinitionCriterion.VERSION_DESC);
    assertEquals(0, retrievedProcess.size());
    
    getManagementAPI().delete(processUUIDs);
  }
  
  public void testGetLigthProcessDefinitionFromUUIDsOrderByStateDesc() throws Exception {
    List<ProcessDefinitionUUID> processUUIDs = new ArrayList<ProcessDefinitionUUID>();
    final int size = 3;
    for (int i = 0 ; i < size ; i++) {
      ProcessDefinition p = ProcessBuilder.createProcess("pdeobsa" + i, "1." + i)        
        .addSystemTask("s")
        .done();      
      processUUIDs.add(getManagementAPI().deploy(getBusinessArchive(p)).getUUID());
    }
    
    final Set<ProcessDefinitionUUID> fromProcess = new HashSet<ProcessDefinitionUUID>(processUUIDs.subList(1, processUUIDs.size()));
    
    //disable one process
    getManagementAPI().disable(processUUIDs.get(1));
    
    List<LightProcessDefinition> retrievedProcess = getQueryDefinitionAPI().getLightProcesses(fromProcess, 0, 2, ProcessDefinitionCriterion.STATE_DESC);
    assertEquals(2, retrievedProcess.size());
    assertEquals(ProcessState.ENABLED, retrievedProcess.get(0).getState());    
    assertEquals(ProcessState.DISABLED, retrievedProcess.get(1).getState());
    
    retrievedProcess = getQueryDefinitionAPI().getLightProcesses(fromProcess, 2, 2, ProcessDefinitionCriterion.STATE_DESC);
    assertEquals(0, retrievedProcess.size());
    
    getManagementAPI().delete(processUUIDs);
  }
  
}
