package org.ow2.bonita.services.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.ow2.bonita.APITestCase;
import org.ow2.bonita.command.LDRCleanCommand;
import org.ow2.bonita.command.LDRDeleteDataCommand;
import org.ow2.bonita.command.LDRGetData;
import org.ow2.bonita.command.LDRGetKeys;
import org.ow2.bonita.command.LDRIsEmptyCommand;
import org.ow2.bonita.command.LDRSlashCommand;
import org.ow2.bonita.command.LDRStoreDataCommand;
import org.ow2.bonita.command.ResetCommonClassLoader;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.util.Misc;
import org.ow2.bonita.util.ProcessBuilder;

public class LDRTest extends APITestCase {

  @SuppressWarnings("unchecked")
  public void testLargeDataRepository() throws Exception {
    byte[] jar = Misc.generateJar(LDRCleanCommand.class, LDRDeleteDataCommand.class, LDRGetData.class,
        LDRGetKeys.class, LDRIsEmptyCommand.class, LDRStoreDataCommand.class, ResetCommonClassLoader.class);
    getManagementAPI().deployJar("ldr1.jar", jar);

    getCommandAPI().execute(new LDRCleanCommand());
    if (Misc.isOnWindows()) {
      getCommandAPI().execute(new ResetCommonClassLoader());
    }

    Boolean empty = getCommandAPI().execute(new LDRIsEmptyCommand());
    assertTrue(empty);

    List<String> categories = new ArrayList<String>();
    Class<String> clazz = String.class;

    categories.clear();
    categories.add("a");
    categories.add("b");

    getCommandAPI().execute(new LDRStoreDataCommand(categories, "key1", "test", true));
    check(categories, "key1", "test", 1);

    Set<String> keys = getCommandAPI().execute(new LDRGetKeys(categories, "k.*"));
    assertEquals("key1", keys.iterator().next());
    keys = getCommandAPI().execute(new LDRGetKeys(categories, "z.*"));
    assertEquals(0, keys.size());
    getCommandAPI().execute(new LDRStoreDataCommand(categories, "key2", "test2", true));

    keys = getCommandAPI().execute(new LDRGetKeys(categories, "k.*"));
    assertEquals(2, keys.size());
    check(categories, "key1", "test", 2);
    check(categories, "key2", "test2", 2);
    keys.clear();
    keys.add("key1");
    keys.add("key2");

    Map<String, String> data = (Map<String, String>)
        getCommandAPI().execute(new LDRGetData(clazz, categories, keys));
    assertEquals(2, data.size());
    getCommandAPI().execute(new LDRDeleteDataCommand(categories, "key1"));
    check(categories, "key2", "test2", 1);
    data = (Map<String, String>) getCommandAPI().execute(new LDRGetData(clazz, categories, keys));
    assertEquals(1, data.size());
    String dat = (String) getCommandAPI().execute(new LDRGetData(clazz, categories, "key1"));
    assertNull(dat);
    getCommandAPI().execute(new LDRDeleteDataCommand(categories));

    dat = (String) getCommandAPI().execute(new LDRGetData(clazz, categories, "key1"));
    assertNull(dat);
    dat = (String) getCommandAPI().execute(new LDRGetData(clazz, categories, "key2"));
    assertNull(dat);
    empty = getCommandAPI().execute(new LDRIsEmptyCommand());
    assertTrue(empty);

    getCommandAPI().execute(new LDRStoreDataCommand(categories, "key3", "test3", true));
    check(categories, "key3", "test3", 1);
    getCommandAPI().execute(new LDRStoreDataCommand(categories, "key3", "newValue", false));
    check(categories, "key3", "test3", 1);
    getCommandAPI().execute(new LDRStoreDataCommand(categories, "key3", "newValue", true));
    check(categories, "key3", "newValue", 1);
    empty = getCommandAPI().execute(new LDRIsEmptyCommand());
    assertFalse(empty);
    getCommandAPI().execute(new LDRCleanCommand());
    empty = getCommandAPI().execute(new LDRIsEmptyCommand());
    assertTrue(empty);

    //do not remove the class as the repository is cleaned at the end of the Command
    //getManagementAPI().removeClass(LDRCommand.class.getName());
  }

  @SuppressWarnings({ "unchecked" })
  private void check(final List<String> categories, final String key, final String value, final int categoriesElements) throws Exception {
    Class<String> clazz = String.class;
    Map<String, String> map;
    Set<String> keys = new HashSet<String>();
    keys.add(key);
    keys.add(UUID.randomUUID().toString());

    String dat = (String) getCommandAPI().execute(new LDRGetData(clazz, categories, key));
    assertEquals(value, dat);

    map = (Map<String, String>)
    getCommandAPI().execute(new LDRGetData(clazz, categories));
    assertEquals(categoriesElements, map.size());
    assertEquals(value, map.get(key));

    map = (Map<String, String>)
    getCommandAPI().execute(new LDRGetData(clazz, categories, keys));
    assertEquals(1, map.size());
    assertEquals(value, map.get(key));

    keys = getCommandAPI().execute(new LDRGetKeys(categories));
    assertEquals(categoriesElements, keys.size());
  }

  public void testResourceWithSlash() throws Exception {
    ProcessDefinition process = ProcessBuilder.createProcess("p", "1.0")
    .addHuman(getLogin())
    .addHumanTask("t", getLogin())
    .done();

    process = getManagementAPI().deploy(getBusinessArchive(process, getResources("a.jar")));
    ProcessDefinitionUUID processUUID = process.getUUID();

    getManagementAPI().deployJar("ldr2.jar", Misc.generateJar(LDRSlashCommand.class));
    assertTrue(getCommandAPI().execute(new LDRSlashCommand("a/a.xml", processUUID)));
    assertTrue(getCommandAPI().execute(new LDRSlashCommand("/a/a.xml", processUUID)));
    assertTrue(getCommandAPI().execute(new LDRSlashCommand("a/a.java", processUUID)));
    assertTrue(getCommandAPI().execute(new LDRSlashCommand("/a/a.java", processUUID)));
    assertTrue(getCommandAPI().execute(new LDRSlashCommand("a/a.properties", processUUID)));
    assertTrue(getCommandAPI().execute(new LDRSlashCommand("/a/a.properties", processUUID)));

    getManagementAPI().removeJar("ldr2.jar");
    getManagementAPI().deleteProcess(processUUID);
  }

}
