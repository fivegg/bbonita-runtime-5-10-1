package org.ow2.bonita.search;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ow2.bonita.APITestCase;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.light.LightProcessDefinition;
import org.ow2.bonita.search.index.ProcessDefinitionIndex;
import org.ow2.bonita.util.BonitaRuntimeException;
import org.ow2.bonita.util.ProcessBuilder;

public class SearchProcessDefinitionTest extends APITestCase {
  
  public void testSearchProcessDefinitions() throws Exception {
    ProcessDefinition first = ProcessBuilder.createProcess("first", "2.1")
    .addDescription("description")
    .addSystemTask("test")
    .done();

    ProcessDefinition second = ProcessBuilder.createProcess("second", "2.2")
    .addDescription("first")
    .addSystemTask("test")
    .done();

    ProcessDefinition third = ProcessBuilder.createProcess("first", "2.2")
    .addDescription("first")
    .addSystemTask("test")
    .done();

    first = getManagementAPI().deploy(getBusinessArchive(first));
    second = getManagementAPI().deploy(getBusinessArchive(second));
    third = getManagementAPI().deploy(getBusinessArchive(third));

    ProcessDefinitionIndex index = new ProcessDefinitionIndex();
    SearchQueryBuilder query = new SearchQueryBuilder(index);
    query.criterion().equalsTo("first");
    List<LightProcessDefinition> processDefinitions = getQueryRuntimeAPI().search(query, 0, 10);
    assertEquals(2, processDefinitions.size());

    query = new SearchQueryBuilder(index);
    query.criterion(ProcessDefinitionIndex.NAME).equalsTo("first");
    processDefinitions = getQueryRuntimeAPI().search(query, 0, 10);
    assertEquals(2, processDefinitions.size());

    query = new SearchQueryBuilder(index);
    query.criterion(ProcessDefinitionIndex.DESCRIPTION).equalsTo("description");
    processDefinitions = getQueryRuntimeAPI().search(query, 0, 10);
    assertEquals(1, processDefinitions.size());

    query = new SearchQueryBuilder(index);
    query.criteria(index.getAllFields()).equalsTo("first").inclusion();
    processDefinitions = getQueryRuntimeAPI().search(query, 0, 10);
    assertEquals(0, processDefinitions.size());

    query = new SearchQueryBuilder(index);
    query.criterion(ProcessDefinitionIndex.NAME).equalsTo("first").and()
    .criterion(ProcessDefinitionIndex.DESCRIPTION).equalsTo("first");
    processDefinitions = getQueryRuntimeAPI().search(query, 0, 10);
    assertEquals(1, processDefinitions.size());
    LightProcessDefinition def = processDefinitions.get(0);
    assertEquals("first", def.getName());
    assertEquals("2.2", def.getVersion());

    query = new SearchQueryBuilder(index);
    query.criteria(index.getAllFields()).equalsTo("first");
    processDefinitions = getQueryRuntimeAPI().search(query, 0, 10);
    assertEquals(3, processDefinitions.size());
    
    query = new SearchQueryBuilder(new ProcessDefinitionIndex());
    query.criterion(ProcessDefinitionIndex.NAME).equalsTo("first").or()
    .criterion(ProcessDefinitionIndex.DESCRIPTION).equalsTo("first");
    processDefinitions = getQueryRuntimeAPI().search(query, 0, 10);
    assertEquals(3, processDefinitions.size());

    getManagementAPI().deleteProcess(first.getUUID());
    getManagementAPI().deleteProcess(second.getUUID());
    getManagementAPI().deleteProcess(third.getUUID());
  }

  public void testSearchCategories() throws Exception {
    ProcessDefinition first = ProcessBuilder.createProcess("fruits", "1.1")
    .addCategory("banana")
    .addCategory("apple")
    .done();

    ProcessDefinition second = ProcessBuilder.createProcess("second", "2.1")
    .addCategory("banana")
    .addCategory("pear")
    .done();

    getManagementAPI().deploy(getBusinessArchive(first));
    getManagementAPI().deploy(getBusinessArchive(second));

    SearchQueryBuilder query = new SearchQueryBuilder(new ProcessDefinitionIndex());
    query.criterion(ProcessDefinitionIndex.CATEGORY_NAME).equalsTo("pear");
    List<LightProcessDefinition> processDefinitions = getQueryRuntimeAPI().search(query, 0, 10);
    assertEquals(1, processDefinitions.size());
    LightProcessDefinition def = processDefinitions.get(0);
    assertEquals("second", def.getName());
    assertEquals("2.1", def.getVersion());

    query = new SearchQueryBuilder(new ProcessDefinitionIndex());
    query.criterion(ProcessDefinitionIndex.CATEGORY_NAME).equalsTo("apple");
    processDefinitions = getQueryRuntimeAPI().search(query, 0, 10);
    assertEquals(1, processDefinitions.size());
    def = processDefinitions.get(0);
    assertEquals("fruits", def.getName());
    assertEquals("1.1", def.getVersion());

    query = new SearchQueryBuilder(new ProcessDefinitionIndex());
    query.criterion(ProcessDefinitionIndex.CATEGORY_NAME).equalsTo("banana");
    processDefinitions = getQueryRuntimeAPI().search(query, 0, 10);
    assertEquals(2, processDefinitions.size());

    ProcessDefinitionIndex index = new ProcessDefinitionIndex(); 
    query= new SearchQueryBuilder(index);
    query.criteria(index.getAllFields()).equalsTo("apple");
    processDefinitions = getQueryRuntimeAPI().search(query, 0, 10);
    assertEquals(1, processDefinitions.size());
    def = processDefinitions.get(0);
    assertEquals("fruits", def.getName());
    assertEquals("1.1", def.getVersion());

    Set<String> categories =  new HashSet<String>();
    categories.add("banana");
    categories.add("pear");
    categories.add("apple");
    getWebAPI().deleteCategories(categories);
    getManagementAPI().deleteProcess(first.getUUID());
    getManagementAPI().deleteProcess(second.getUUID());
  }

  public void testSearchProcessesByUUID() throws Exception {
    ProcessDefinition first = ProcessBuilder.createProcess("second", "1.1")
    .addSystemTask("system")
    .done();

    ProcessDefinition second = ProcessBuilder.createProcess("second", "2.1")
    .addHuman(getLogin())
    .addHumanTask("human", getLogin())
    .done();

    getManagementAPI().deploy(getBusinessArchive(first));
    getManagementAPI().deploy(getBusinessArchive(second));
    
    SearchQueryBuilder query = new SearchQueryBuilder(new ProcessDefinitionIndex());
    query.criterion(ProcessDefinitionIndex.UUID).startsWith("second");

    List<LightProcessDefinition> processDefinitions = getQueryRuntimeAPI().search(query, 0, 10);
    assertEquals(2, processDefinitions.size());

    query = new SearchQueryBuilder(new ProcessDefinitionIndex());
    query.criterion(ProcessDefinitionIndex.UUID).equalsTo("second\\-\\-2.1");

    processDefinitions = getQueryRuntimeAPI().search(query, 0, 10);
    assertEquals(1, processDefinitions.size());
    LightProcessDefinition def = processDefinitions.get(0);
    assertEquals("second", def.getName());
    assertEquals("2.1", def.getVersion());
    assertEquals(second.getUUID().getValue(), def.getUUID().getValue());
    
    query = new SearchQueryBuilder(new ProcessDefinitionIndex());
    query.criterion(ProcessDefinitionIndex.UUID).equalsTo(second.getUUID().getValue());

    processDefinitions = getQueryRuntimeAPI().search(query, 0, 10);
    assertEquals(1, processDefinitions.size());
    def = processDefinitions.get(0);
    assertEquals("second", def.getName());
    assertEquals("2.1", def.getVersion());
    assertEquals(second.getUUID().getValue(), def.getUUID().getValue());

    getManagementAPI().deleteProcess(first.getUUID());
    getManagementAPI().deleteProcess(second.getUUID());
  }

  public void testThrowARuntimeExceptionWithInvalidQueries() throws Exception {
    ProcessDefinition first = ProcessBuilder.createProcess("first", "2.1")
    .addDescription("description")
    .addSystemTask("test")
    .done();

    first = getManagementAPI().deploy(getBusinessArchive(first));

    ProcessDefinitionIndex index = new ProcessDefinitionIndex();
    SearchQueryBuilder query = new SearchQueryBuilder(index);
    query.criterion().equalsTo("first");
    List<LightProcessDefinition> processDefinitions = getQueryRuntimeAPI().search(query, 0, 10);
    assertEquals(1, processDefinitions.size());

    query = new SearchQueryBuilder(index);
    query.criterion(ProcessDefinitionIndex.NAME).equalsTo("first").and()
    .criterion(ProcessDefinitionIndex.UUID).equalsTo("NOT" + first.getUUID().getValue()).and();
    try {
      getQueryRuntimeAPI().search(query, 0, 10);
      fail("The query is not well-formed");
    } catch (BonitaRuntimeException e) {
    }
    getManagementAPI().deleteProcess(first.getUUID());
  }

}
