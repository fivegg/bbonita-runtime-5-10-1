/**
 * Copyright (C) 2011  BonitaSoft S.A.
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
package org.ow2.bonita.facade;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.ow2.bonita.APITestCase;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.exception.ProcessNotFoundException;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.util.ProcessBuilder;

/**
 * @author Elias Ricken de Medeiros
 *
 */
public class FormsMigrationTest extends APITestCase {
  
  private ProcessDefinition getSimpleProcess() {
    final ProcessDefinition process = ProcessBuilder.createProcess("simpleProcess", "1.0")
        .done();
    return process;
  }
  
  public void testMigrationDateIsInitiallyNull() throws Exception {
    final ProcessDefinition process = getManagementAPI().deploy(getBusinessArchive(getSimpleProcess()));
    final ProcessDefinitionUUID processUUID = process.getUUID();
    
    final ProcessDefinition deployedProcess = getQueryDefinitionAPI().getProcess(processUUID);
    assertNotNull(deployedProcess);
    assertNull(deployedProcess.getMigrationDate());
    
    getManagementAPI().deleteProcess(processUUID);
  }
  
  public void testUpdateMigrationDate() throws Exception {
    final ProcessDefinition process = getManagementAPI().deploy(getBusinessArchive(getSimpleProcess()));
    final ProcessDefinitionUUID processUUID = process.getUUID();
    
    ProcessDefinition deployedProcess = getQueryDefinitionAPI().getProcess(processUUID);
    assertNotNull(deployedProcess);
    assertNull(deployedProcess.getMigrationDate());
    
    final Date migrationDate = new Date();    
    getManagementAPI().updateMigrationDate(processUUID, migrationDate);
    
    deployedProcess = getQueryDefinitionAPI().getProcess(processUUID);
    assertNotNull(deployedProcess);
    assertEquals(migrationDate, deployedProcess.getMigrationDate());
    
    final Date retrievedMigrationDate = getQueryDefinitionAPI().getMigrationDate(processUUID);
    assertEquals(migrationDate, retrievedMigrationDate);
    
    getManagementAPI().deleteProcess(processUUID);
  }
  
  public void testUpdateMigrationDateWitArchivedProcess() throws Exception {
    final ProcessDefinition process = getManagementAPI().deploy(getBusinessArchive(getSimpleProcess()));
    final ProcessDefinitionUUID processUUID = process.getUUID();
    
    getManagementAPI().disable(processUUID);
    getManagementAPI().archive(processUUID);
    ProcessDefinition deployedProcess = getQueryDefinitionAPI().getProcess(processUUID);
    
    assertNotNull(deployedProcess);
    assertNull(deployedProcess.getMigrationDate());
    
    final Date migrationDate = new Date();    
    getManagementAPI().updateMigrationDate(processUUID, migrationDate);
    
    deployedProcess = getQueryDefinitionAPI().getProcess(processUUID);
    assertNotNull(deployedProcess);
    assertEquals(migrationDate, deployedProcess.getMigrationDate());
    
    final Date retrievedMigrationDate = getQueryDefinitionAPI().getMigrationDate(processUUID);
    assertEquals(migrationDate, retrievedMigrationDate);
    
    getManagementAPI().deleteProcess(processUUID);
  }
  
  public void testSetResource() throws Exception {
    final String firstKey = "firstResource";
    final String secondKey = "parent/secondResource";
    final Map<String, byte[]> resources = new HashMap<String, byte[]>();
    final byte[] contentFirst = "aaaaa".getBytes();
    resources.put(firstKey, contentFirst);
    final byte[] contentSecond = "bbbb".getBytes();
    resources.put(secondKey, contentSecond);
    
    final ProcessDefinition process = getManagementAPI().deploy(getBusinessArchive(getSimpleProcess(), resources));
    final ProcessDefinitionUUID processUUID = process.getUUID();
    
    byte[] retrievedContentFirst = getQueryDefinitionAPI().getResource(processUUID, firstKey);
    byte[] retrievedContentSecond = getQueryDefinitionAPI().getResource(processUUID, secondKey);
    checkEquals(contentFirst, retrievedContentFirst);
    checkEquals(contentSecond, retrievedContentSecond);
    
    final byte[] updatedContentFirst = "ccccc".getBytes();
    getManagementAPI().setResource(processUUID, firstKey, updatedContentFirst);
    
    retrievedContentFirst = getQueryDefinitionAPI().getResource(processUUID, firstKey);
    retrievedContentSecond = getQueryDefinitionAPI().getResource(processUUID, secondKey);
    checkEquals(updatedContentFirst, retrievedContentFirst);
    checkEquals(contentSecond, retrievedContentSecond);
    
    final byte[] updatedContentSecond = "ddddd".getBytes();
    getManagementAPI().setResource(processUUID, secondKey, updatedContentSecond);
    
    retrievedContentFirst = getQueryDefinitionAPI().getResource(processUUID, firstKey);
    retrievedContentSecond = getQueryDefinitionAPI().getResource(processUUID, secondKey);
    checkEquals(updatedContentFirst, retrievedContentFirst);
    checkEquals(updatedContentSecond, retrievedContentSecond);
    
    getManagementAPI().deleteProcess(processUUID);
    
  }

  private void checkEquals(final byte[] contentFirst, final byte[] contentSecond) {
    assertEquals(contentFirst.length, contentSecond.length);
    for (int i = 0; i < contentFirst.length; i++) {
      final byte b = contentFirst[i];
      assertEquals(b, contentSecond[i]);
    }
  }
  
  public void testCannotSetResourceOfInexistentProcess() throws Exception {
    final byte[] content = "aaaaa".getBytes();
    ProcessDefinitionUUID processUUID = new ProcessDefinitionUUID("doesnotexist");
    try {
      getManagementAPI().setResource(processUUID, "key", content);
      fail("Exception Expected");
    } catch (ProcessNotFoundException e) {
      //OK
    }
  }
  
  public void testSetInexistentResource() throws Exception {
    final String resourceKey = "resourceKey";
    
    final ProcessDefinition process = getManagementAPI().deploy(getBusinessArchive(getSimpleProcess()));
    final ProcessDefinitionUUID processUUID = process.getUUID();
    
    byte[] retrievedContent = getQueryDefinitionAPI().getResource(processUUID, resourceKey);
    assertNull(retrievedContent);
    
    final byte[] content = "aaaaa".getBytes();
    getManagementAPI().setResource(processUUID, resourceKey, content);
    
    retrievedContent = getQueryDefinitionAPI().getResource(processUUID, resourceKey);
    checkEquals(content, retrievedContent);
    
    getManagementAPI().deleteProcess(processUUID);
  }
  
  public void testSetResourceCyrillicCaracter() throws Exception {
    final String resourceKey = "выйKey";
    final Map<String, byte[]> resources = new HashMap<String, byte[]>();
    final byte[] contentFirst = "выйaaaaa".getBytes();
    resources.put(resourceKey, contentFirst);
    
    final ProcessDefinition process = getManagementAPI().deploy(getBusinessArchive(getSimpleProcess(), resources));
    final ProcessDefinitionUUID processUUID = process.getUUID();
    
    byte[] retrievedContent = getQueryDefinitionAPI().getResource(processUUID, resourceKey);
    checkEquals(contentFirst, retrievedContent);
    
    final byte[] updatedContent = "выйccccc".getBytes();
    getManagementAPI().setResource(processUUID, resourceKey, updatedContent);
    
    retrievedContent = getQueryDefinitionAPI().getResource(processUUID, resourceKey);
    checkEquals(updatedContent, retrievedContent);
    
    getManagementAPI().deleteProcess(processUUID);
    
  }
  
  public void testSetResourceWithLongPath() throws Exception {
    final String resourceKey = "org/ow2/bonita/common/util/resources/AResourceWithLongName.java";
    final Map<String, byte[]> resources = new HashMap<String, byte[]>();
    final byte[] contentFirst = "aaaaa".getBytes();
    resources.put(resourceKey, contentFirst);
    
    final ProcessDefinition process = getManagementAPI().deploy(getBusinessArchive(getSimpleProcess(), resources));
    final ProcessDefinitionUUID processUUID = process.getUUID();
    
    byte[] retrievedContent = getQueryDefinitionAPI().getResource(processUUID, resourceKey);
    checkEquals(contentFirst, retrievedContent);
    
    final byte[] updatedContent = "ccccc".getBytes();
    getManagementAPI().setResource(processUUID, resourceKey, updatedContent);
    
    retrievedContent = getQueryDefinitionAPI().getResource(processUUID, resourceKey);
    checkEquals(updatedContent, retrievedContent);
    
    getManagementAPI().deleteProcess(processUUID);
    
  }

}
