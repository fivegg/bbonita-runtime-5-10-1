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
 * Modified by Matthieu Chaffotte, Anthony Birembaut - BonitaSoft S.A.
 **/
package org.ow2.bonita.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.ow2.bonita.facade.def.element.BusinessArchive;
import org.ow2.bonita.facade.def.element.impl.BusinessArchiveImpl;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;

public final class BusinessArchiveFactory {

  private BusinessArchiveFactory() { }

  public static void generateBusinessArchiveFile(final File barFile, final ProcessDefinition process, final Map<String, byte[]> resources, final Class< ? >... classes)
  throws IOException, ClassNotFoundException {
    Misc.checkArgsNotNull(barFile);
    byte[] barContent = getBusinessArchiveContent(process, resources, classes);
    generateBusinessArchiveFile(barFile, barContent);
  }

  public static void generateBusinessArchiveFile(final File barFile, final BusinessArchive businessArchive) throws IOException {
    Misc.checkArgsNotNull(barFile, businessArchive);
    byte[] barContent = Misc.generateJar(businessArchive.getResources());
    generateBusinessArchiveFile(barFile, barContent);
  }

  private static void generateBusinessArchiveFile(final File barFile, final byte[] barContent) throws IOException {
    Misc.checkArgsNotNull(barFile, barContent);
    Misc.write(barFile, barContent);
  }

  public static BusinessArchive getBusinessArchive(final URL businessArchiveFileUrl)
  throws URISyntaxException, IOException, ClassNotFoundException {
    return getBusinessArchive(new File(businessArchiveFileUrl.toURI()));
  }

  public static BusinessArchive getBusinessArchive(final Map<String, byte[]> resources)
  throws IOException, ClassNotFoundException {
    return getBusinessArchive(resources, true);
  }

  public static BusinessArchive getBusinessArchive(final Map<String, byte[]> resources, final boolean deserializeProcess)
  throws IOException, ClassNotFoundException {
    Map<String, byte[]> newResources = new HashMap<String, byte[]>();
    ProcessDefinition process = null;
    for (Map.Entry<String, byte[]> resource : resources.entrySet()) {
      if (resource.getKey().endsWith(".xpdl")) {
        if (deserializeProcess) {
          File tempDir = Misc.createDirectories(BonitaConstants.getTemporaryFolder());
          File xpdlFile = Misc.createTempFile("xpdl", null, tempDir);
          Misc.getFile(xpdlFile, resource.getValue());
          try {
            process = ProcessBuilder.createProcessFromXpdlFile(xpdlFile.toURL());
          } finally {
            xpdlFile.delete();
          }
        }
      } else if (resource.getKey().equals(BusinessArchiveImpl.PROCESS_RESOURCE_NAME)) {
        if (deserializeProcess) {
          File tempDir = Misc.createDirectories(BonitaConstants.getTemporaryFolder());
          File xmlDefFile = Misc.createTempFile("xmlDef", null, tempDir);
          Misc.getFile(xmlDefFile, resource.getValue());
          Properties contextProperties = createPropertiesFromResources(resources);
          try {
            process = ProcessBuilder.createProcessFromXmlDefFile(xmlDefFile.toURL(), contextProperties);
          } finally {
            xmlDefFile.delete();
          }
        }
      } else {
        newResources.put(resource.getKey(), resource.getValue());
      }
    }
    return new BusinessArchiveImpl(process, newResources);
  }

  /**
   * @param resources
   * @return
   */
  public static Properties createPropertiesFromResources(Map<String, byte[]> resources) throws IOException {
    Properties res = new Properties();
    for (Entry<String, byte[]> entry : resources.entrySet()) {
      if (entry.getKey().startsWith(BonitaConstants.CONTEXTS_FOLDER_IN_BAR)) {
        byte[] bytes = entry.getValue();
        ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
        Properties props = new Properties();
        props.load(stream);
        stream.close();
        res.putAll(props);
      }
    }
    return res;
  }

  public static BusinessArchive getBusinessArchive(final File businessArchiveFile)
  throws IOException, ClassNotFoundException {
    return getBusinessArchive(businessArchiveFile, true);
  }

  public static BusinessArchive getBusinessArchive(final File businessArchiveFile, final boolean deserializeProcess)
  throws IOException, ClassNotFoundException {
    if (!businessArchiveFile.exists()) {
      throw new FileNotFoundException("File " + businessArchiveFile + "doesn't exists.");
    }
    byte[] fileAsBytes = Misc.getAllContentFrom(businessArchiveFile);
    Map<String, byte[]> resources = Misc.getResourcesFromZip(fileAsBytes);

    return getBusinessArchive(resources, deserializeProcess);
  }

  public static BusinessArchive getBusinessArchive(final ProcessDefinition process, final Class< ? >... classes)
  throws IOException, ClassNotFoundException {
    Misc.checkArgsNotNull(process);
    return new BusinessArchiveImpl(process, null, classes);
  }

  public static BusinessArchive getBusinessArchive(final ProcessDefinition process, final Map<String, byte[]> resources, final Class< ? >... classes)
  throws IOException, ClassNotFoundException {
    Misc.checkArgsNotNull(process);
    return new BusinessArchiveImpl(process, resources, classes);
  }

  private static byte[] getBusinessArchiveContent(final ProcessDefinition process, final Map<String, byte[]> resources , final Class< ? >... classes)
  throws IOException, ClassNotFoundException {
    BusinessArchive businessArchive = getBusinessArchive(process, resources, classes);
    return Misc.generateJar(businessArchive.getResources());
  }

}
