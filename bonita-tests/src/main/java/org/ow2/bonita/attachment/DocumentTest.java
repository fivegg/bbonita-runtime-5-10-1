package org.ow2.bonita.attachment;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ow2.bonita.APITestCase;
import org.ow2.bonita.facade.def.element.BusinessArchive;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.exception.DocumentAlreadyExistsException;
import org.ow2.bonita.facade.exception.DocumentNotFoundException;
import org.ow2.bonita.facade.runtime.AttachmentInstance;
import org.ow2.bonita.facade.runtime.Document;
import org.ow2.bonita.facade.runtime.ProcessInstance;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.light.LightActivityInstance;
import org.ow2.bonita.search.DocumentResult;
import org.ow2.bonita.search.DocumentSearchBuilder;
import org.ow2.bonita.search.index.DocumentIndex;
import org.ow2.bonita.util.BusinessArchiveFactory;
import org.ow2.bonita.util.ProcessBuilder;
import org.ow2.bonita.util.ServerWebDeleteDocumentsOfProcessesCommand;

public class DocumentTest extends APITestCase {

  private final String mimeType = "plain/text";

  public void testCreateAnInstanceDocument() throws Exception {
    final ProcessDefinition definition = ProcessBuilder.createProcess("doc", "1.0").addHuman(getLogin())
        .addHumanTask("step1", getLogin()).done();

    getManagementAPI().deploy(getBusinessArchive(definition));
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(definition.getUUID());

    final String content = "Aleksi\nEljas\nHeikki\nMatti";
    final Document document = getRuntimeAPI().createDocument("myDocument1", instanceUUID, "names.txt", mimeType,
        content.getBytes());
    final byte[] actualContent = getQueryRuntimeAPI().getDocumentContent(document.getUUID());
    assertEquals(content, new String(actualContent));
    assertEquals(content.getBytes().length, document.getContentSize());

    getRuntimeAPI().deleteDocuments(true, document.getUUID());
    getManagementAPI().deleteProcess(definition.getUUID());
  }

  public void testCreateAProcessDocument() throws Exception {
    final ProcessDefinition definition = ProcessBuilder.createProcess("doc", "1.0").addHuman(getLogin())
        .addHumanTask("step1", getLogin()).done();

    final ProcessDefinition process = getManagementAPI().deploy(getBusinessArchive(definition));

    final String content = "Aleksi\nEljas\nHeikki\nMatti";
    final Document document = getRuntimeAPI().createDocument("myDocument1", process.getUUID(), "names.txt", mimeType,
        content.getBytes());
    final byte[] actualContent = getQueryRuntimeAPI().getDocumentContent(document.getUUID());
    assertEquals(content, new String(actualContent));

    getRuntimeAPI().deleteDocuments(true, document.getUUID());
    getManagementAPI().deleteProcess(definition.getUUID());
  }

  public void testDeleteDocumentsOfProcessDefinition() throws Exception {
    final ProcessDefinition definition1 = ProcessBuilder.createProcess("doc1", "1.0").addHuman(getLogin())
        .addHumanTask("step1", getLogin()).done();
    final ProcessDefinition definition2 = ProcessBuilder.createProcess("doc2", "1.0").addHuman(getLogin())
        .addHumanTask("step1", getLogin()).done();

    final ProcessDefinition process = getManagementAPI().deploy(getBusinessArchive(definition1));
    final ProcessDefinition process2 = getManagementAPI().deploy(getBusinessArchive(definition2));

    final String content = "Aleksi\nEljas\nHeikki\nMatti";
    Document document = getRuntimeAPI().createDocument("myDocument1", process.getUUID(), "names.txt", mimeType,
        content.getBytes());
    Document document2 = getRuntimeAPI().createDocument("myDocument1", process2.getUUID(), "names.txt", mimeType,
        content.getBytes());
    byte[] actualContent1 = getQueryRuntimeAPI().getDocumentContent(document.getUUID());
    assertEquals(content, new String(actualContent1));
    byte[] actualContent2 = getQueryRuntimeAPI().getDocumentContent(document2.getUUID());
    assertEquals(content, new String(actualContent2));

    // execute command
    getCommandAPI().execute(
        new ServerWebDeleteDocumentsOfProcessesCommand(Collections.singletonList(process.getUUID())));

    try {
      document = getQueryRuntimeAPI().getDocument(document.getUUID());
      fail("the document was not deleted");
    } catch (final DocumentNotFoundException e) {

    }

    try {
      actualContent1 = getQueryRuntimeAPI().getDocumentContent(document.getUUID());
      fail("the document content was not deleted");
    } catch (final DocumentNotFoundException e) {

    }
    document2 = getQueryRuntimeAPI().getDocument(document2.getUUID());
    assertNotNull(document2);
    actualContent2 = getQueryRuntimeAPI().getDocumentContent(document2.getUUID());
    assertEquals(content, new String(actualContent2));

    getRuntimeAPI().deleteDocuments(true, document2.getUUID());
    getManagementAPI().deleteProcess(definition1.getUUID());
    getManagementAPI().deleteProcess(definition2.getUUID());
  }

  public void testThrowAnExceptionIfDocumentAlreadyExistsInProcesInstance() throws Exception {
    final ProcessDefinition definition = ProcessBuilder.createProcess("doc", "1.0").addHuman(getLogin())
        .addHumanTask("step1", getLogin()).done();

    getManagementAPI().deploy(getBusinessArchive(definition));
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(definition.getUUID());

    final String content = "Aleksi\nEljas\nHeikki\nMatti";
    final Document document = getRuntimeAPI().createDocument("myDocument1", instanceUUID, "names.txt", mimeType,
        content.getBytes());
    try {
      getRuntimeAPI().createDocument("myDocument1", instanceUUID, "names.txt", mimeType, content.getBytes());
      fail("Document already exists");
    } catch (final DocumentAlreadyExistsException e) {
    }
    getRuntimeAPI().deleteDocuments(true, document.getUUID());
    getManagementAPI().deleteProcess(definition.getUUID());
  }

  public void testThrowAnExceptionIfDocumentAlreadyExistsInProcesDefinition() throws Exception {
    final ProcessDefinition definition = ProcessBuilder.createProcess("doc", "1.0").addHuman(getLogin())
        .addHumanTask("step1", getLogin()).done();

    getManagementAPI().deploy(getBusinessArchive(definition));
    final String content = "Aleksi\nEljas\nHeikki\nMatti";
    final Document document = getRuntimeAPI().createDocument("myDocument1", definition.getUUID(), "names.txt",
        mimeType, content.getBytes());
    try {
      getRuntimeAPI().createDocument("myDocument1", definition.getUUID(), "names.txt", mimeType, content.getBytes());
      fail("Document already exists");
    } catch (final DocumentAlreadyExistsException e) {
    }
    getRuntimeAPI().deleteDocuments(true, document.getUUID());
    getManagementAPI().deleteProcess(definition.getUUID());
  }

  public void testUpdateADocument() throws Exception {
    final ProcessDefinition definition = ProcessBuilder.createProcess("doc", "1.0").addHuman(getLogin())
        .addHumanTask("step1", getLogin()).done();

    getManagementAPI().deploy(getBusinessArchive(definition));
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(definition.getUUID());

    final String content = "Aleksi\nEljas\nHeikki\nMatti";
    Document document = getRuntimeAPI().createDocument("myDocument1", instanceUUID, "names.txt", mimeType,
        content.getBytes());
    final String updatedContent = content.concat("\nTiina");
    final Document update = getRuntimeAPI().addDocumentVersion(document.getUUID(), true, "names(1).txt", mimeType,
        updatedContent.getBytes());

    final byte[] actualUpdatedContent = getQueryRuntimeAPI().getDocumentContent(update.getUUID());
    assertEquals(updatedContent, new String(actualUpdatedContent));
    final List<Document> documentVersions = getQueryRuntimeAPI().getDocumentVersions(update.getUUID());
    assertTrue(checkAllPropertiesButIsLatest(update, documentVersions.get(0)));
    assertTrue(checkAllPropertiesButIsLatest(document, documentVersions.get(1)));
    // check version label
    assertEquals("1", document.getVersionLabel());
    assertEquals("2", update.getVersionLabel());

    document = documentVersions.get(1);// FIXME should not have to do that but there is a bg on xcmis ( CMIS-507)
    final byte[] actualContent = getQueryRuntimeAPI().getDocumentContent(document.getUUID());
    assertEquals(content, new String(actualContent));

    // add document without content
    final Document docWithNoContent = getRuntimeAPI().addDocumentVersion(document.getUUID(), true, "names(1).txt",
        mimeType, null);
    assertNull(getQueryRuntimeAPI().getDocumentContent(docWithNoContent.getUUID()));

    getRuntimeAPI().deleteDocuments(true, update.getUUID());
    getManagementAPI().deleteProcess(definition.getUUID());
  }

  public void testGetLastDocument() throws Exception {
    final ProcessDefinition definition = ProcessBuilder.createProcess("doc", "1.0").addHuman(getLogin())
        .addHumanTask("step1", getLogin()).done();
    
    getManagementAPI().deploy(getBusinessArchive(definition));
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(definition.getUUID());
    
    final String content = "Aleksi\nEljas\nHeikki\nMatti";
    final String documentName = "myDocument1";
    final Document document = getRuntimeAPI().createDocument(documentName, instanceUUID, "names.txt", mimeType,
        content.getBytes());
    
    final Date date = new Date();
    
    Thread.sleep(5);
    final String updatedContent = content.concat("\nTiina");
    final Document update = getRuntimeAPI().addDocumentVersion(document.getUUID(), true, "names(1).txt", mimeType,
        updatedContent.getBytes());
    final Document docV1 = getQueryRuntimeAPI().getLastDocument(instanceUUID, documentName, date);
    final Document docV2 = getQueryRuntimeAPI().getLastDocument(instanceUUID, documentName, new Date());
    checkProperties(content, "myDocument1", "names.txt", "1", docV1);
    checkProperties(updatedContent, "myDocument1", "names(1).txt", "2", docV2);
    
    getRuntimeAPI().deleteDocuments(true, update.getUUID());
    getManagementAPI().deleteProcess(definition.getUUID());
  }

  public void testGetLastDocumentFromActivityInstance() throws Exception {
    final ProcessDefinition definition = ProcessBuilder.createProcess("doc", "1.0").addHuman(getLogin())
        .addHumanTask("step1", getLogin())
        .addHumanTask("step2", getLogin())
        .addTransition("step1", "step2")
        .done();
    
    getManagementAPI().deploy(getBusinessArchive(definition));
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(definition.getUUID());
    
    final String content = "Aleksi\nEljas\nHeikki\nMatti";
    final String documentName = "myDocument1";
    final Document document = getRuntimeAPI().createDocument(documentName, instanceUUID, "names.txt", mimeType,
        content.getBytes());
    
    final LightActivityInstance step1 = getActivityInstance(instanceUUID, "step1");
    getRuntimeAPI().executeTask(step1.getUUID(), true);
    Thread.sleep(5);
    
    final String updatedContent = content.concat("\nTiina");
    final Document update = getRuntimeAPI().addDocumentVersion(document.getUUID(), true, "names(1).txt", mimeType,
        updatedContent.getBytes());
    final LightActivityInstance step2 = getActivityInstance(instanceUUID, "step2");
    getRuntimeAPI().executeTask(step2.getUUID(), true);
    
    final Document docV1 = getQueryRuntimeAPI().getLastDocument(instanceUUID, documentName, step1.getUUID());
    final Document docV2 = getQueryRuntimeAPI().getLastDocument(instanceUUID, documentName, step2.getUUID());
    checkProperties(content, "myDocument1", "names.txt", "1", docV1);
    checkProperties(updatedContent, "myDocument1", "names(1).txt", "2", docV2);
    
    getRuntimeAPI().deleteDocuments(true, update.getUUID());
    getManagementAPI().deleteProcess(definition.getUUID());
  }

  public void testCreateDocumentOrAddDocumentVersion() throws Exception {
    final ProcessDefinition definition = ProcessBuilder.createProcess("doc", "1.0").addHuman(getLogin())
        .addHumanTask("step1", getLogin()).done();
    
    getManagementAPI().deploy(getBusinessArchive(definition));
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(definition.getUUID());
    
    final String content = "Aleksi\nEljas\nHeikki\nMatti";
    getRuntimeAPI().createDocumentOrAddDocumentVersion(instanceUUID, "myDocument1", "names.txt", mimeType,
        content.getBytes());
    final String updatedContent = content.concat("\nTiina");
    getRuntimeAPI().createDocumentOrAddDocumentVersion(instanceUUID, "myDocument1", "names(1).txt", mimeType,
        updatedContent.getBytes());
    
    final List<Document> documentVersions = getDocumentVersionsOfProcessWithSingleDocument(instanceUUID);
    assertEquals(2, documentVersions.size());
    
    final Document docV1 = documentVersions.get(1);
    final Document docV2 = documentVersions.get(0);
    checkProperties(content, "myDocument1", "names.txt", "1", docV1);
    checkProperties(updatedContent, "myDocument1", "names(1).txt", "2", docV2);

    getRuntimeAPI().deleteDocuments(true, docV2.getUUID());
    getManagementAPI().deleteProcess(definition.getUUID());
  }

  private List<Document> getDocumentVersionsOfProcessWithSingleDocument(final ProcessInstanceUUID instanceUUID) throws DocumentNotFoundException {
    final DocumentSearchBuilder searchBuilder = new DocumentSearchBuilder();
    searchBuilder.criterion(DocumentIndex.PROCESS_INSTANCE_UUID).equalsTo(instanceUUID.getValue());
    final DocumentResult searchResult = getQueryRuntimeAPI().searchDocuments(searchBuilder, 0, 10);
    assertEquals(1, searchResult.getCount());
    final Document document = searchResult.getDocuments().get(0);
    final List<Document> documentVersions = getQueryRuntimeAPI().getDocumentVersions(document.getUUID());
    return documentVersions;
  }

  private void checkProperties(final String expectedContent, final String expectedName, final String expectedFilename, final String expectedLabel, final Document document)
      throws DocumentNotFoundException {
    final byte[] actualUpdatedContent = getQueryRuntimeAPI().getDocumentContent(document.getUUID());
    assertEquals(expectedName, document.getName());
    assertEquals(expectedFilename, document.getContentFileName());
    assertEquals(expectedLabel, document.getVersionLabel());
    assertEquals(expectedContent, new String(actualUpdatedContent));
  }

  public void testDeleteDocumentsAllVersions() throws Exception {
    final ProcessDefinition definition = ProcessBuilder.createProcess("doc", "1.0").addHuman(getLogin())
        .addHumanTask("step1", getLogin()).done();

    getManagementAPI().deploy(getBusinessArchive(definition));
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(definition.getUUID());

    final String content = "Aleksi\nEljas\nHeikki\nMatti";
    final Document document = getRuntimeAPI().createDocument("myDocument1", instanceUUID, "names.txt", mimeType,
        content.getBytes());
    final String updatedContent = content.concat("\nTiina");
    final Document update = getRuntimeAPI().addDocumentVersion(document.getUUID(), true, "names(1).txt", mimeType,
        updatedContent.getBytes());

    final DocumentSearchBuilder builder = new DocumentSearchBuilder();
    builder.criterion(DocumentIndex.PROCESS_DEFINITION_UUID).equalsTo(definition.getUUID().getValue());

    DocumentResult documentResult = getQueryRuntimeAPI().searchDocuments(builder, 0, 100);
    assertEquals(1, documentResult.getCount());
    getRuntimeAPI().deleteDocuments(true, update.getUUID());
    documentResult = getQueryRuntimeAPI().searchDocuments(builder, 0, 100);
    assertEquals(0, documentResult.getCount());

    getManagementAPI().deleteProcess(definition.getUUID());
  }

  public void testDeleteDocumentsNotAllVersions() throws Exception {
    final ProcessDefinition definition = ProcessBuilder.createProcess("doc", "1.0").addHuman(getLogin())
        .addHumanTask("step1", getLogin()).done();

    getManagementAPI().deploy(getBusinessArchive(definition));
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(definition.getUUID());

    final String content = "Aleksi\nEljas\nHeikki\nMatti";
    final Document document = getRuntimeAPI().createDocument("myDocument1", instanceUUID, "names.txt", mimeType,
        content.getBytes());
    final String updatedContent = content.concat("\nTiina");
    final Document update = getRuntimeAPI().addDocumentVersion(document.getUUID(), true, "names(1).txt", mimeType,
        updatedContent.getBytes());

    final DocumentSearchBuilder builder = new DocumentSearchBuilder();
    builder.criterion(DocumentIndex.PROCESS_DEFINITION_UUID).equalsTo(definition.getUUID().getValue());

    DocumentResult documentResult = getQueryRuntimeAPI().searchDocuments(builder, 0, 100);
    assertEquals(1, documentResult.getCount());
    getRuntimeAPI().deleteDocuments(false, update.getUUID());
    documentResult = getQueryRuntimeAPI().searchDocuments(builder, 0, 100);
    assertEquals(1, documentResult.getCount());
    assertEquals("names.txt", documentResult.getDocuments().get(0).getContentFileName());

    getRuntimeAPI().deleteDocuments(true, document.getUUID());

    getManagementAPI().deleteProcess(definition.getUUID());
  }

  private boolean checkAllPropertiesButIsLatest(final Document expectedDocument, final Document actualDocument) {
    if (expectedDocument == actualDocument) {
      return true;
    }
    if (actualDocument == null) {
      return false;
    }
    if (expectedDocument.getAuthor() == null) {
      if (actualDocument.getAuthor() != null) {
        return false;
      }
    } else if (!expectedDocument.getAuthor().equals(actualDocument.getAuthor())) {
      return false;
    }
    if (expectedDocument.getContentFileName() == null) {
      if (actualDocument.getContentFileName() != null) {
        return false;
      }
    } else if (!expectedDocument.getContentFileName().equals(actualDocument.getContentFileName())) {
      return false;
    }
    if (expectedDocument.getContentMimeType() == null) {
      if (actualDocument.getContentMimeType() != null) {
        return false;
      }
    } else if (!expectedDocument.getContentMimeType().equals(actualDocument.getContentMimeType())) {
      return false;
    }
    if (expectedDocument.getContentSize() != actualDocument.getContentSize()) {
      return false;
    }
    if (expectedDocument.getCreationDate() == null) {
      if (actualDocument.getCreationDate() != null) {
        return false;
      }
    } else if (!expectedDocument.getCreationDate().equals(actualDocument.getCreationDate())) {
      return false;
    }
    if (expectedDocument.isMajorVersion() != actualDocument.isMajorVersion()) {
      return false;
    }
    if (expectedDocument.getLastModificationDate() == null) {
      if (actualDocument.getLastModificationDate() != null) {
        return false;
      }
    } else if (!expectedDocument.getLastModificationDate().equals(actualDocument.getLastModificationDate())) {
      return false;
    }
    if (expectedDocument.getLastModifiedBy() == null) {
      if (actualDocument.getLastModifiedBy() != null) {
        return false;
      }
    } else if (!expectedDocument.getLastModifiedBy().equals(actualDocument.getLastModifiedBy())) {
      return false;
    }
    if (expectedDocument.getName() == null) {
      if (actualDocument.getName() != null) {
        return false;
      }
    } else if (!expectedDocument.getName().equals(actualDocument.getName())) {
      return false;
    }
    if (expectedDocument.getProcessDefinitionUUID() == null) {
      if (actualDocument.getProcessDefinitionUUID() != null) {
        return false;
      }
    } else if (!expectedDocument.getProcessDefinitionUUID().equals(actualDocument.getProcessDefinitionUUID())) {
      return false;
    }
    if (expectedDocument.getProcessInstanceUUID() == null) {
      if (actualDocument.getProcessInstanceUUID() != null) {
        return false;
      }
    } else if (!expectedDocument.getProcessInstanceUUID().equals(actualDocument.getProcessInstanceUUID())) {
      return false;
    }
    if (expectedDocument.getUUID() == null) {
      if (actualDocument.getUUID() != null) {
        return false;
      }
    } else if (!expectedDocument.getUUID().equals(actualDocument.getUUID())) {
      return false;
    }
    if (expectedDocument.getVersionLabel() == null) {
      if (actualDocument.getVersionLabel() != null) {
        return false;
      }
    } else if (!expectedDocument.getVersionLabel().equals(actualDocument.getVersionLabel())) {
      return false;
    }
    if (expectedDocument.getVersionSeriesId() == null) {
      if (actualDocument.getVersionSeriesId() != null) {
        return false;
      }
    } else if (!expectedDocument.getVersionSeriesId().equals(actualDocument.getVersionSeriesId())) {
      return false;
    }
    return true;
  }

  public void testGetDocumentsOfAnInstance() throws Exception {
    final ProcessDefinition definition = ProcessBuilder.createProcess("doc", "1.0").addHuman(getLogin())
        .addHumanTask("step1", getLogin()).done();

    getManagementAPI().deploy(getBusinessArchive(definition));
    final ProcessInstanceUUID instanceUUID1 = getRuntimeAPI().instantiateProcess(definition.getUUID());
    final ProcessInstanceUUID instanceUUID2 = getRuntimeAPI().instantiateProcess(definition.getUUID());

    final String content1 = "Aleksi\nEljas\nHeikki\nMatti";
    final Document document1i1 = getRuntimeAPI().createDocument("myDoc", instanceUUID1, "names.txt", mimeType,
        content1.getBytes());
    final String content2 = content1.concat("\nTiina");
    final Document document2 = getRuntimeAPI().createDocument("update1.0", instanceUUID1, "names(1).txt", mimeType,
        content2.getBytes());

    final Document documentI2 = getRuntimeAPI().createDocument("myDocI2", instanceUUID2, "i2_names.txt", mimeType,
        content1.getBytes());

    final DocumentSearchBuilder builder = new DocumentSearchBuilder();
    builder.criterion(DocumentIndex.PROCESS_INSTANCE_UUID).equalsTo(instanceUUID1.getValue());

    final DocumentResult result = getQueryRuntimeAPI().searchDocuments(builder, 0, 10);
    assertNotNull(result);
    assertEquals(2, result.getCount());
    final List<Document> documents = result.getDocuments();
    final Document searchdocument1 = documents.get(0);
    final Document searchdocument2 = documents.get(1);
    assertEquals(document1i1, searchdocument1);
    assertEquals(document2, searchdocument2);
    getRuntimeAPI().deleteDocuments(true, document1i1.getUUID(), document2.getUUID(), documentI2.getUUID());
    getManagementAPI().deleteProcess(definition.getUUID());
  }

  public void testGetDocumentsOfAProcess() throws Exception {
    final ProcessDefinition definition = ProcessBuilder.createProcess("doc", "1.0").addHuman(getLogin())
        .addHumanTask("step1", getLogin()).done();
    final ProcessDefinition definition2 = ProcessBuilder.createProcess("p2doc", "1.0").addHuman(getLogin())
        .addHumanTask("step1", getLogin()).done();

    final ProcessDefinition process = getManagementAPI().deploy(getBusinessArchive(definition));
    final ProcessDefinition process2 = getManagementAPI().deploy(getBusinessArchive(definition2));
    final ProcessDefinitionUUID processDefinitionUUID = process.getUUID();

    final String content1 = "Aleksi\nEljas\nHeikki\nMatti";
    final Document document1 = getRuntimeAPI().createDocument("myDoc", processDefinitionUUID, "names.txt", mimeType,
        content1.getBytes());
    final String content2 = content1.concat("\nTiina");
    final Document document2 = getRuntimeAPI().createDocument("update1.0", processDefinitionUUID, "names(1).txt",
        mimeType, content2.getBytes());

    final Document document1P2 = getRuntimeAPI().createDocument("myDoc", process2.getUUID(), "names.txt", mimeType,
        content1.getBytes());

    final DocumentSearchBuilder builder = new DocumentSearchBuilder();
    builder.criterion(DocumentIndex.PROCESS_DEFINITION_UUID_WITHOUT_INSTANCES).equalsTo(
        processDefinitionUUID.getValue());

    final DocumentResult result = getQueryRuntimeAPI().searchDocuments(builder, 0, 10);
    assertNotNull(result);
    assertEquals(2, result.getCount());
    final List<Document> documents = result.getDocuments();
    final Document searchdocument1 = documents.get(0);
    final Document searchdocument2 = documents.get(1);
    if ("myDoc".equals(searchdocument1.getName())) {
      assertEquals(document1, searchdocument1);
      assertEquals(document2, searchdocument2);
    } else {
      assertEquals(document2, searchdocument1);
      assertEquals(document1, searchdocument2);
    }
    assertNotNull(searchdocument1.getProcessDefinitionUUID());
    assertNotNull(searchdocument2.getProcessDefinitionUUID());
    getRuntimeAPI().deleteDocuments(true, document1.getUUID(), document2.getUUID(), document1P2.getUUID());
    getManagementAPI().deleteProcess(definition.getUUID());
    getManagementAPI().deleteProcess(definition2.getUUID());
  }

  public void testAttachmentFromProcessDefinitionToDocument() throws Exception {
    final String attachmentName = "aName";
    final byte[] initialValue = new byte[] { 1, 2, 3, 4, 5, 6 };
    final Map<String, byte[]> resources = new HashMap<String, byte[]>();
    resources.put(attachmentName, initialValue);

    final ProcessDefinition process = ProcessBuilder.createProcess("rmAttchmnt", "1.0").addAttachment(attachmentName)
        .addHuman(getLogin()).addHumanTask("step", getLogin()).done();

    getManagementAPI().deploy(getBusinessArchive(process, resources));
    final ProcessDefinitionUUID processUUID = process.getUUID();
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    final Set<String> attachmentNames = getQueryRuntimeAPI().getAttachmentNames(instanceUUID);
    assertEquals(1, attachmentNames.size());
    assertTrue(attachmentNames.contains(attachmentName));

    final DocumentSearchBuilder builder = new DocumentSearchBuilder();
    builder.criterion(DocumentIndex.NAME).equalsTo(attachmentName).and().criterion(DocumentIndex.PROCESS_INSTANCE_UUID)
        .equalsTo(instanceUUID.toString());
    final DocumentResult result = getQueryRuntimeAPI().searchDocuments(builder, 0, 10);
    assertEquals(1, result.getCount());
    final List<Document> documents = result.getDocuments();
    assertEquals(1, documents.size());

    final List<AttachmentInstance> attachments = getQueryRuntimeAPI().getAttachments(instanceUUID, attachmentName);
    assertEquals(attachments.get(0), documents.get(0));

    getManagementAPI().deleteProcess(processUUID);
  }

  public void testAttachmentToDocument() throws Exception {
    final String attachment = "aName";
    final byte[] initialValue = new byte[] { 1, 2, 3, 4, 5, 6 };
    final Map<String, byte[]> resources = new HashMap<String, byte[]>();
    resources.put(attachment, initialValue);

    final ProcessDefinition process = ProcessBuilder.createProcess("rmAttchmnt", "1.0").addHuman(getLogin())
        .addHumanTask("step", getLogin()).done();

    getManagementAPI().deploy(getBusinessArchive(process, resources));
    final ProcessDefinitionUUID processUUID = process.getUUID();
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    final Map<String, String> metadata = new HashMap<String, String>();
    metadata.put("mime-type", mimeType);
    getRuntimeAPI().addAttachment(instanceUUID, attachment, "label", "description", "toto", metadata,
        "hello".getBytes());

    final Set<String> attachmentNames = getQueryRuntimeAPI().getAttachmentNames(instanceUUID);
    assertEquals(1, attachmentNames.size());
    assertTrue(attachmentNames.contains(attachment));

    final List<AttachmentInstance> attachments = getQueryRuntimeAPI().getAttachments(instanceUUID, attachment);
    final DocumentSearchBuilder builder = new DocumentSearchBuilder();
    builder.criterion(DocumentIndex.NAME).equalsTo(attachment);
    final DocumentResult result = getQueryRuntimeAPI().searchDocuments(builder, 0, 10);
    assertEquals(1, result.getCount());
    final List<Document> documents = result.getDocuments();
    assertEquals(1, documents.size());
    assertEquals(attachments.get(0), documents.get(0));
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testDocumentToAttachment() throws Exception {
    final ProcessDefinition process = ProcessBuilder.createProcess("rmAttchmnt", "1.0").addHuman(getLogin())
        .addHumanTask("step", getLogin()).done();

    getManagementAPI().deploy(getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = process.getUUID();
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    final Document doc = getRuntimeAPI()
        .createDocument("doc", instanceUUID, "fileName", mimeType, "content".getBytes());
    final List<AttachmentInstance> attachments = getQueryRuntimeAPI().getAttachments(instanceUUID, "doc");
    assertEquals(1, attachments.size());

    assertEquals(attachments.get(0), doc);
    getManagementAPI().deleteProcess(processUUID);
  }

  private void assertEquals(final AttachmentInstance attachment, final Document document) {
    assertEquals(attachment.getName(), document.getName());
    assertEquals(attachment.getAuthor(), document.getAuthor());
    assertEquals(attachment.getFileName(), document.getContentFileName());
    assertEquals(attachment.getProcessInstanceUUID(), document.getProcessInstanceUUID());
    assertEquals(attachment.getVersionDate(), document.getCreationDate());

    final String mimeType = attachment.getMetaData().get("content-type");
    if (mimeType != null) {
      assertEquals(mimeType, document.getContentMimeType());
    }
  }

  public void testDeleteADocumentOfAnArchivedProcess() throws Exception {
    final ProcessDefinition definition = ProcessBuilder.createProcess("doc", "1.0").addHuman(getLogin())
        .addHumanTask("step1", getLogin()).done();

    getManagementAPI().deploy(getBusinessArchive(definition));
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(definition.getUUID());

    final String content = "Aleksi\nEljas\nHeikki\nMatti";
    final Document document = getRuntimeAPI().createDocument("myDocument1", instanceUUID, "names.txt", mimeType,
        content.getBytes());

    executeTask(instanceUUID, "step1");
    getRuntimeAPI().deleteDocuments(true, document.getUUID());
    getManagementAPI().deleteProcess(definition.getUUID());
  }

  public void testCreateDocumentAndAddDocumentVersionChangesLastUpdate() throws Exception {
    ProcessDefinition attachmentProcess = ProcessBuilder.createProcess("p", "1.0").addHuman(getLogin())
        .addHumanTask("task1", getLogin()).done();

    final BusinessArchive businessArchive = BusinessArchiveFactory.getBusinessArchive(attachmentProcess);
    attachmentProcess = getManagementAPI().deploy(businessArchive);

    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(attachmentProcess.getUUID());
    final ProcessInstance initialInstance = getQueryRuntimeAPI().getProcessInstance(instanceUUID);
    Thread.sleep(100);
    final Document document = getRuntimeAPI().createDocument("doc", instanceUUID, "doc1.txt", mimeType,
        "content".getBytes());
    final ProcessInstance instanceAfterCreateDocument = getQueryRuntimeAPI().getProcessInstance(instanceUUID);
    assertFalse(initialInstance.getLastUpdate().equals(instanceAfterCreateDocument.getLastUpdate()));
    Thread.sleep(100);
    getRuntimeAPI().addDocumentVersion(document.getUUID(), true, "doc1(1).txt", mimeType, "update".getBytes());
    final ProcessInstance instanceAfterAddVersion = getQueryRuntimeAPI().getProcessInstance(instanceUUID);
    assertFalse(instanceAfterCreateDocument.getLastUpdate().equals(instanceAfterAddVersion.getLastUpdate()));
    getManagementAPI().deleteProcess(attachmentProcess.getUUID());
  }

  public void testCanAddAVersionOnAnArchivedProcessInstance() throws Exception {
    ProcessDefinition attachmentProcess = ProcessBuilder.createProcess("p", "1.0").addHuman(getLogin())
        .addHumanTask("task1", getLogin()).done();

    final BusinessArchive businessArchive = BusinessArchiveFactory.getBusinessArchive(attachmentProcess);
    attachmentProcess = getManagementAPI().deploy(businessArchive);
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(attachmentProcess.getUUID());
    final ProcessInstance initialInstance = getQueryRuntimeAPI().getProcessInstance(instanceUUID);
    Thread.sleep(100);
    final Document document = getRuntimeAPI().createDocument("doc", instanceUUID, "doc1.txt", mimeType,
        "content".getBytes());
    final ProcessInstance instanceAfterCreateDocument = getQueryRuntimeAPI().getProcessInstance(instanceUUID);
    assertFalse(initialInstance.getLastUpdate().equals(instanceAfterCreateDocument.getLastUpdate()));

    executeTask(instanceUUID, "task1");
    getRuntimeAPI().addDocumentVersion(document.getUUID(), true, "doc1(1).txt", mimeType, "update".getBytes());
    final ProcessInstance instanceAfterAddVersion = getQueryRuntimeAPI().getProcessInstance(instanceUUID);
    assertFalse(instanceAfterCreateDocument.getLastUpdate().equals(instanceAfterAddVersion.getLastUpdate()));
    getManagementAPI().deleteProcess(attachmentProcess.getUUID());
  }

  public void testSearchWithInClauseInProcessInstance() throws Exception {
    // create an instance document
    final ProcessDefinition definition1 = ProcessBuilder.createProcess("doc1", "1.0").addHuman(getLogin())
        .addHumanTask("step1", getLogin()).done();
    final ProcessDefinition definition2 = ProcessBuilder.createProcess("doc2", "1.0").addHuman(getLogin())
        .addHumanTask("step2", getLogin()).done();

    getManagementAPI().deploy(getBusinessArchive(definition1));
    final ProcessInstanceUUID instanceUUID1 = getRuntimeAPI().instantiateProcess(definition1.getUUID());
    getManagementAPI().deploy(getBusinessArchive(definition2));
    final ProcessInstanceUUID instanceUUID2 = getRuntimeAPI().instantiateProcess(definition2.getUUID());

    final String content = "Aleksi\nEljas\nHeikki\nMatti";
    final Document document1 = getRuntimeAPI().createDocument("names", instanceUUID1, "names.txt", mimeType,
        content.getBytes());
    final Document document2 = getRuntimeAPI().createDocument("users", instanceUUID2, "names.txt", mimeType,
        content.getBytes());

    // Delete It by using
    // "documentSearchBuilder.criterion(DocumentIndex.PROCESS_INSTANCE_UUID).in(set<ProcessesInstancesUuid>)"
    final DocumentSearchBuilder documentSearchBuilder = new DocumentSearchBuilder();
    final String[] myProcessInstanceUUIDs = { instanceUUID1.getValue(), instanceUUID2.getValue() };
    final HashSet<String> myProcessInstanceUUIDsSet = new HashSet<String>(Arrays.asList(myProcessInstanceUUIDs));
    documentSearchBuilder.criterion(DocumentIndex.PROCESS_INSTANCE_UUID).in(myProcessInstanceUUIDsSet);

    final DocumentResult searchResult = getQueryRuntimeAPI().searchDocuments(documentSearchBuilder, 0, 100);
    final List<Document> documentsFound = searchResult.getDocuments();
    assertEquals(2, documentsFound.size());

    getRuntimeAPI().deleteDocuments(true, document1.getUUID());
    getRuntimeAPI().deleteDocuments(true, document2.getUUID());
    getManagementAPI().deleteProcess(definition1.getUUID());
    getManagementAPI().deleteProcess(definition2.getUUID());

  }

  public void testSearchDocumentsByName() throws Exception {
    final ProcessDefinition definition1 = ProcessBuilder.createProcess("doc1", "1.0").addHuman(getLogin())
        .addHumanTask("step1", getLogin()).done();

    getManagementAPI().deploy(getBusinessArchive(definition1));
    final ProcessInstanceUUID instanceUUID1 = getRuntimeAPI().instantiateProcess(definition1.getUUID());

    // create some documents
    final String content = "Aleksi\nEljas\nHeikki\nMatti";
    final Document document1 = getRuntimeAPI().createDocument("myDocument1", instanceUUID1, "names.txt", mimeType,
        content.getBytes());
    final Document document2 = getRuntimeAPI().createDocument("myDocument2", instanceUUID1, "names.txt", mimeType,
        content.getBytes());
    final Document documentProcDef1 = getRuntimeAPI().createDocument("myDocument1", definition1.getUUID(), "names.txt",
        mimeType, content.getBytes());
    final Document documentProcDef2 = getRuntimeAPI().createDocument("myDocument2", definition1.getUUID(), "names.txt",
        mimeType, content.getBytes());

    // define criterion
    final DocumentSearchBuilder documentSearchBuilder = new DocumentSearchBuilder();
    documentSearchBuilder.criterion(DocumentIndex.NAME).equalsTo("myDocument1");

    final DocumentResult searchResult = getQueryRuntimeAPI().searchDocuments(documentSearchBuilder, 0, 100);
    final List<Document> documentsFound = searchResult.getDocuments();

    // check result
    assertEquals(2, documentsFound.size());
    assertEquals(documentProcDef1, documentsFound.get(0));
    assertEquals(document1, documentsFound.get(1));

    getRuntimeAPI().deleteDocuments(true, document1.getUUID(), document2.getUUID(), documentProcDef1.getUUID(),
        documentProcDef2.getUUID());
    getManagementAPI().deleteProcess(definition1.getUUID());

  }

  public void testSearchDocumentsByDate() throws Exception {

    final ProcessDefinition definition1 = ProcessBuilder.createProcess("doc1", "1.0").addHuman(getLogin())
        .addHumanTask("step1", getLogin()).done();

    getManagementAPI().deploy(getBusinessArchive(definition1));
    final ProcessInstanceUUID instanceUUID1 = getRuntimeAPI().instantiateProcess(definition1.getUUID());

    // create some documents
    final String content = "Aleksi\nEljas\nHeikki\nMatti";
    final Document document1 = getRuntimeAPI().createDocument("myDocument1", instanceUUID1, "names.txt", mimeType,
        content.getBytes());
    Thread.sleep(3);
    final Date before = new Date();
    final Document document2 = getRuntimeAPI().createDocument("myDocument2", instanceUUID1, "names.txt", mimeType,
        content.getBytes());
    final Document documentProcDef1 = getRuntimeAPI().createDocument("myDocument1", definition1.getUUID(), "names.txt",
        mimeType, content.getBytes());
    final Date after = new Date();
    Thread.sleep(3);
    final Document documentProcDef2 = getRuntimeAPI().createDocument("myDocument2", definition1.getUUID(), "names.txt",
        mimeType, content.getBytes());

    // define criterion
    final DocumentSearchBuilder documentSearchBuilder = new DocumentSearchBuilder();
    documentSearchBuilder.criterion(DocumentIndex.CREATION_DATE).between(before, after);

    final DocumentResult searchResult = getQueryRuntimeAPI().searchDocuments(documentSearchBuilder, 0, 100);
    final List<Document> documentsFound = searchResult.getDocuments();

    // check result
    assertEquals(2, documentsFound.size());
    assertEquals(documentProcDef1, documentsFound.get(0));
    assertEquals(document2, documentsFound.get(1));

    getRuntimeAPI().deleteDocuments(true, document1.getUUID(), document2.getUUID(), documentProcDef1.getUUID(),
        documentProcDef2.getUUID());
    getManagementAPI().deleteProcess(definition1.getUUID());

  }

  public void testSearchDocumentsByProcessDefinition() throws Exception {
    ProcessDefinition definition1 = ProcessBuilder.createProcess("doc1", "1.0").addHuman(getLogin())
        .addHumanTask("step1", getLogin()).done();
    ProcessDefinition definition2 = ProcessBuilder.createProcess("doc2", "1.0").addHuman(getLogin())
        .addHumanTask("step1", getLogin()).done();

    definition1 = getManagementAPI().deploy(getBusinessArchive(definition1));
    definition2 = getManagementAPI().deploy(getBusinessArchive(definition2));
    final ProcessInstanceUUID instanceUUID1 = getRuntimeAPI().instantiateProcess(definition1.getUUID());

    // create some documents
    final String content = "Aleksi\nEljas\nHeikki\nMatti";
    final Document document1 = getRuntimeAPI().createDocument("myDocument1", instanceUUID1, "names.txt", mimeType,
        content.getBytes());
    final Document document2 = getRuntimeAPI().createDocument("myDocument2", instanceUUID1, "names.txt", mimeType,
        content.getBytes());
    final Document documentProcDef1 = getRuntimeAPI().createDocument("myDocument1", definition2.getUUID(), "names.txt",
        mimeType, content.getBytes());
    final Document documentProcDef2 = getRuntimeAPI().createDocument("myDocument2", definition1.getUUID(), "names.txt",
        mimeType, content.getBytes());

    // define criterion
    final DocumentSearchBuilder documentSearchBuilder = new DocumentSearchBuilder();
    documentSearchBuilder.criterion(DocumentIndex.PROCESS_DEFINITION_UUID).equalsTo(definition1.getUUID().getValue());

    final DocumentResult searchResult = getQueryRuntimeAPI().searchDocuments(documentSearchBuilder, 0, 100);
    final List<Document> documentsFound = searchResult.getDocuments();

    // check result
    assertEquals(3, documentsFound.size());
    assertEquals(document1, documentsFound.get(0));
    assertEquals(documentProcDef2, documentsFound.get(1));
    assertEquals(document2, documentsFound.get(2));

    getRuntimeAPI().deleteDocuments(true, document1.getUUID(), document2.getUUID(), documentProcDef1.getUUID(),
        documentProcDef2.getUUID());
    getManagementAPI().deleteProcess(definition1.getUUID());
    getManagementAPI().deleteProcess(definition2.getUUID());

  }

  public void testSearchDocumentsByProcessDefinitionAndName() throws Exception {
    ProcessDefinition definition1 = ProcessBuilder.createProcess("doc1", "1.0").addHuman(getLogin())
        .addHumanTask("step1", getLogin()).done();
    ProcessDefinition definition2 = ProcessBuilder.createProcess("doc2", "1.0").addHuman(getLogin())
        .addHumanTask("step1", getLogin()).done();

    definition1 = getManagementAPI().deploy(getBusinessArchive(definition1));
    definition2 = getManagementAPI().deploy(getBusinessArchive(definition2));
    final ProcessInstanceUUID instanceUUID1 = getRuntimeAPI().instantiateProcess(definition1.getUUID());

    // create some documents
    final String content = "Aleksi\nEljas\nHeikki\nMatti";
    final Document document1 = getRuntimeAPI().createDocument("myDocument1", instanceUUID1, "names.txt", mimeType,
        content.getBytes());
    final Document document2 = getRuntimeAPI().createDocument("myDocument2", instanceUUID1, "names.txt", mimeType,
        content.getBytes());
    final Document documentProcDef1 = getRuntimeAPI().createDocument("myDocument1", definition2.getUUID(), "names.txt",
        mimeType, content.getBytes());
    final Document documentProcDef2 = getRuntimeAPI().createDocument("myDocument2", definition1.getUUID(), "names.txt",
        mimeType, content.getBytes());

    // define criterion
    final DocumentSearchBuilder documentSearchBuilder = new DocumentSearchBuilder();
    documentSearchBuilder.criterion(DocumentIndex.PROCESS_DEFINITION_UUID).equalsTo(definition1.getUUID().getValue())
        .and().criterion(DocumentIndex.NAME).equalsTo("myDocument2");

    final DocumentResult searchResult = getQueryRuntimeAPI().searchDocuments(documentSearchBuilder, 0, 100);
    final List<Document> documentsFound = searchResult.getDocuments();

    // check result
    assertEquals(2, documentsFound.size());
    assertEquals(documentProcDef2, documentsFound.get(0));
    assertEquals(document2, documentsFound.get(1));

    getRuntimeAPI().deleteDocuments(true, document1.getUUID(), document2.getUUID(), documentProcDef1.getUUID(),
        documentProcDef2.getUUID());
    getManagementAPI().deleteProcess(definition1.getUUID());
    getManagementAPI().deleteProcess(definition2.getUUID());

  }

  public void testSearchDocumentsByProcessDefinitionWithoutInstances() throws Exception {
    ProcessDefinition definition1 = ProcessBuilder.createProcess("doc1", "1.0").addHuman(getLogin())
        .addHumanTask("step1", getLogin()).done();
    ProcessDefinition definition2 = ProcessBuilder.createProcess("doc2", "1.0").addHuman(getLogin())
        .addHumanTask("step1", getLogin()).done();

    definition1 = getManagementAPI().deploy(getBusinessArchive(definition1));
    definition2 = getManagementAPI().deploy(getBusinessArchive(definition2));
    final ProcessInstanceUUID instanceUUID1 = getRuntimeAPI().instantiateProcess(definition1.getUUID());

    // create some documents
    final String content = "Aleksi\nEljas\nHeikki\nMatti";
    final Document document1 = getRuntimeAPI().createDocument("myDocument1", instanceUUID1, "names.txt", mimeType,
        content.getBytes());
    final Document document2 = getRuntimeAPI().createDocument("myDocument2", instanceUUID1, "names.txt", mimeType,
        content.getBytes());
    final Document documentProcDef1 = getRuntimeAPI().createDocument("myDocument1", definition2.getUUID(), "names.txt",
        mimeType, content.getBytes());
    final Document documentProcDef2 = getRuntimeAPI().createDocument("myDocument2", definition1.getUUID(), "names.txt",
        mimeType, content.getBytes());

    // define criterion
    final DocumentSearchBuilder documentSearchBuilder = new DocumentSearchBuilder();
    documentSearchBuilder.criterion(DocumentIndex.PROCESS_DEFINITION_UUID_WITHOUT_INSTANCES).equalsTo(
        definition1.getUUID().getValue());

    final DocumentResult searchResult = getQueryRuntimeAPI().searchDocuments(documentSearchBuilder, 0, 100);
    final List<Document> documentsFound = searchResult.getDocuments();

    // check result
    assertEquals(1, documentsFound.size());
    assertEquals(documentProcDef2, documentsFound.get(0));

    getRuntimeAPI().deleteDocuments(true, document1.getUUID(), document2.getUUID(), documentProcDef1.getUUID(),
        documentProcDef2.getUUID());
    getManagementAPI().deleteProcess(definition1.getUUID());
    getManagementAPI().deleteProcess(definition2.getUUID());

  }

  public void testSearchDocumentsByProcessInstanceUUID() throws Exception {
    final ProcessDefinition definition1 = ProcessBuilder.createProcess("doc1", "1.0").addHuman(getLogin())
        .addHumanTask("step1", getLogin()).done();

    getManagementAPI().deploy(getBusinessArchive(definition1));
    final ProcessInstanceUUID instanceUUID1 = getRuntimeAPI().instantiateProcess(definition1.getUUID());
    final ProcessInstanceUUID instanceUUID2 = getRuntimeAPI().instantiateProcess(definition1.getUUID());

    // create some documents
    final String content = "Aleksi\nEljas\nHeikki\nMatti";
    final Document document1 = getRuntimeAPI().createDocument("myDocument1", instanceUUID2, "names.txt", mimeType,
        content.getBytes());
    final Document document2 = getRuntimeAPI().createDocument("myDocument2", instanceUUID1, "names.txt", mimeType,
        content.getBytes());
    final Document documentProcDef1 = getRuntimeAPI().createDocument("myDocument1", definition1.getUUID(), "names.txt",
        mimeType, content.getBytes());
    final Document documentProcDef2 = getRuntimeAPI().createDocument("myDocument2", definition1.getUUID(), "names.txt",
        mimeType, content.getBytes());

    // define criterion
    final DocumentSearchBuilder documentSearchBuilder = new DocumentSearchBuilder();
    documentSearchBuilder.criterion(DocumentIndex.PROCESS_INSTANCE_UUID).equalsTo(instanceUUID1.getValue());

    final DocumentResult searchResult = getQueryRuntimeAPI().searchDocuments(documentSearchBuilder, 0, 100);
    final List<Document> documentsFound = searchResult.getDocuments();

    // check result
    assertEquals(1, documentsFound.size());
    assertEquals(document2, documentsFound.get(0));

    getRuntimeAPI().deleteDocuments(true, document1.getUUID(), document2.getUUID(), documentProcDef1.getUUID(),
        documentProcDef2.getUUID());
    getManagementAPI().deleteProcess(definition1.getUUID());

  }

  public void testSearchDocumentsByAuthor() throws Exception {
    final ProcessDefinition definition1 = ProcessBuilder.createProcess("doc1", "1.0").addHuman(getLogin())
        .addHumanTask("step1", getLogin()).done();

    getManagementAPI().deploy(getBusinessArchive(definition1));
    final ProcessInstanceUUID instanceUUID1 = getRuntimeAPI().instantiateProcess(definition1.getUUID());

    // create some documents
    final String content = "Aleksi\nEljas\nHeikki\nMatti";
    final Document document1 = getRuntimeAPI().createDocument("myDocument1", instanceUUID1, "names.txt", mimeType,
        content.getBytes());
    final Document document2 = getRuntimeAPI().createDocument("myDocument2", instanceUUID1, "names.txt", mimeType,
        content.getBytes());
    loginAs("john", "bpm");
    final Document documentProcDef1 = getRuntimeAPI().createDocument("myDocument1", definition1.getUUID(), "names.txt",
        mimeType, content.getBytes());
    final Document documentProcDef2 = getRuntimeAPI().createDocument("myDocument2", definition1.getUUID(), "names.txt",
        mimeType, content.getBytes());

    // define criterion
    final DocumentSearchBuilder documentSearchBuilder = new DocumentSearchBuilder();
    documentSearchBuilder.criterion(DocumentIndex.AUTHOR).equalsTo("john");

    final DocumentResult searchResult = getQueryRuntimeAPI().searchDocuments(documentSearchBuilder, 0, 100);
    final List<Document> documentsFound = searchResult.getDocuments();

    // check result
    assertEquals(2, documentsFound.size());
    assertEquals(documentProcDef1, documentsFound.get(0));
    assertEquals(documentProcDef2, documentsFound.get(1));

    getRuntimeAPI().deleteDocuments(true, document1.getUUID(), document2.getUUID(), documentProcDef1.getUUID(),
        documentProcDef2.getUUID());
    getManagementAPI().deleteProcess(definition1.getUUID());

  }

  public void testSearchDocumentsInClause() throws Exception {
    final ProcessDefinition definition1 = ProcessBuilder.createProcess("doc1", "1.0").addHuman(getLogin())
        .addHumanTask("step1", getLogin()).done();

    getManagementAPI().deploy(getBusinessArchive(definition1));
    final ProcessInstanceUUID instanceUUID1 = getRuntimeAPI().instantiateProcess(definition1.getUUID());

    // create some documents
    final String content = "Aleksi\nEljas\nHeikki\nMatti";
    final Document document1 = getRuntimeAPI().createDocument("myDocument1", instanceUUID1, "names.txt", mimeType,
        content.getBytes());
    final Document document2 = getRuntimeAPI().createDocument("myDocument2", instanceUUID1, "names.txt", mimeType,
        content.getBytes());
    final Document documentProcDef1 = getRuntimeAPI().createDocument("myDocument1", definition1.getUUID(), "names.txt",
        mimeType, content.getBytes());
    final Document documentProcDef2 = getRuntimeAPI().createDocument("myDocument2", definition1.getUUID(), "names.txt",
        mimeType, content.getBytes());

    // define criterion
    final DocumentSearchBuilder documentSearchBuilder = new DocumentSearchBuilder();
    documentSearchBuilder.criterion(DocumentIndex.ID).in(
        Arrays.asList(document1.getUUID().getValue(), documentProcDef2.getUUID().getValue()));

    final DocumentResult searchResult = getQueryRuntimeAPI().searchDocuments(documentSearchBuilder, 0, 100);
    final List<Document> documentsFound = searchResult.getDocuments();

    // check result
    assertEquals(2, documentsFound.size());
    assertEquals(document1, documentsFound.get(0));
    assertEquals(documentProcDef2, documentsFound.get(1));

    getRuntimeAPI().deleteDocuments(true, document1.getUUID(), document2.getUUID(), documentProcDef1.getUUID(),
        documentProcDef2.getUUID());
    getManagementAPI().deleteProcess(definition1.getUUID());

  }

  public void testSearchDocumentsWithAndWithoutContent() throws Exception {
    final ProcessDefinition definition1 = ProcessBuilder.createProcess("doc1", "1.0").addHuman(getLogin())
        .addHumanTask("step1", getLogin()).done();

    getManagementAPI().deploy(getBusinessArchive(definition1));
    final ProcessInstanceUUID instanceUUID1 = getRuntimeAPI().instantiateProcess(definition1.getUUID());

    // create some documents
    final String content = "Aleksi\nEljas\nHeikki\nMatti";
    final Document document1 = getRuntimeAPI().createDocument("myDocument1", instanceUUID1, "names.txt", mimeType,
        content.getBytes());
    final Document document2 = getRuntimeAPI()
        .createDocument("myDocument2", instanceUUID1, "names.txt", mimeType, null);
    final Document documentProcDef1 = getRuntimeAPI().createDocument("myDocument1", definition1.getUUID(), "names.txt",
        mimeType, content.getBytes());
    final Document documentProcDef2 = getRuntimeAPI().createDocument("myDocument2", definition1.getUUID(), "names.txt",
        mimeType, content.getBytes());

    // define criterion
    DocumentSearchBuilder documentSearchBuilder = new DocumentSearchBuilder();
    documentSearchBuilder.criterion(DocumentIndex.IS_EMPTY).equalsTo(true);

    DocumentResult searchResult = getQueryRuntimeAPI().searchDocuments(documentSearchBuilder, 0, 100);
    List<Document> documentsFound = searchResult.getDocuments();

    // check result
    assertEquals(1, documentsFound.size());
    assertEquals(document2, documentsFound.get(0));

    // documents with content
    documentSearchBuilder = new DocumentSearchBuilder();
    documentSearchBuilder.criterion(DocumentIndex.IS_EMPTY).equalsTo(false);

    searchResult = getQueryRuntimeAPI().searchDocuments(documentSearchBuilder, 0, 100);
    documentsFound = searchResult.getDocuments();

    assertEquals(3, documentsFound.size());
    assertEquals(documentProcDef1, documentsFound.get(0));
    assertEquals(document1, documentsFound.get(1));
    assertEquals(documentProcDef2, documentsFound.get(2));

    getRuntimeAPI().deleteDocuments(true, document1.getUUID(), document2.getUUID(), documentProcDef1.getUUID(),
        documentProcDef2.getUUID());
    getManagementAPI().deleteProcess(definition1.getUUID());

  }

  public void testSearchPaginated() throws Exception {
    final ProcessDefinition definition1 = ProcessBuilder.createProcess("doc1", "1.0").addHuman(getLogin())
        .addHumanTask("step1", getLogin()).done();

    getManagementAPI().deploy(getBusinessArchive(definition1));
    final ProcessInstanceUUID instanceUUID1 = getRuntimeAPI().instantiateProcess(definition1.getUUID());

    // create some documents
    final String content = "Aleksi\nEljas\nHeikki\nMatti";
    final Document document1 = getRuntimeAPI().createDocument("myDocument1", instanceUUID1, "names.txt", mimeType,
        content.getBytes());
    final Document document2 = getRuntimeAPI().createDocument("myDocument2", instanceUUID1, "names.txt", mimeType,
        content.getBytes());
    final Document documentProcDef1 = getRuntimeAPI().createDocument("myDocument1", definition1.getUUID(), "names.txt",
        mimeType, content.getBytes());
    final Document documentProcDef2 = getRuntimeAPI().createDocument("myDocument2", definition1.getUUID(), "names.txt",
        mimeType, content.getBytes());

    // first page
    final DocumentSearchBuilder documentSearchBuilder = new DocumentSearchBuilder();
    DocumentResult searchResult = getQueryRuntimeAPI().searchDocuments(documentSearchBuilder, 0, 3);
    List<Document> documentsFound = searchResult.getDocuments();

    assertEquals(4, searchResult.getCount());
    assertEquals(3, documentsFound.size());
    assertEquals(documentProcDef1, documentsFound.get(0));
    assertEquals(document1, documentsFound.get(1));
    assertEquals(documentProcDef2, documentsFound.get(2));

    // second page
    searchResult = getQueryRuntimeAPI().searchDocuments(documentSearchBuilder, 3, 3);
    documentsFound = searchResult.getDocuments();
    assertEquals(4, searchResult.getCount());
    assertEquals(1, documentsFound.size());
    assertEquals(document2, documentsFound.get(0));

    getRuntimeAPI().deleteDocuments(true, document1.getUUID(), document2.getUUID(), documentProcDef1.getUUID(),
        documentProcDef2.getUUID());
    getManagementAPI().deleteProcess(definition1.getUUID());

  }

}