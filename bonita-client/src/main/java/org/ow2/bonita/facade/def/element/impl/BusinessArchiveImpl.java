/**
 * Copyright (C) 2007  Bull S. A. S.
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
 * Modified by Matthieu Chaffotte, Charles Souillard, Anthony Birembaut, 
 * Elias Ricken de Medeiros - BonitaSoft S.A.
 **/
package org.ow2.bonita.facade.def.element.impl;

/**
 * Utility class to create a Deployment object containing the the XPDL file and optionally its
 */
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.ow2.bonita.building.XmlDefExporter;
import org.ow2.bonita.facade.def.element.BusinessArchive;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.util.BonitaConstants;
import org.ow2.bonita.util.BonitaRuntimeException;
import org.ow2.bonita.util.BusinessArchiveFactory;
import org.ow2.bonita.util.ClassDataTool;
import org.ow2.bonita.util.Misc;
import org.ow2.bonita.util.ProcessBuilder;
import org.ow2.bonita.util.xml.XStreamUtil;

import com.thoughtworks.xstream.XStream;

/**
 * This class implements a deployment object. This object can be provided to one of the method of the ManagementAPI
 * {@link org.ow2.bonita.facade.ManagementAPI#deploy(BusinessArchive) method} to allow the deployment of a XPDL file and
 * optionally its depending java classes for hooks, performer assignments, mappers...
 * 
 * @author Marc Blachon, Guillaume Porcher, Charles Souillard, Miguel Valdes, Pierre Vigneras
 */
public class BusinessArchiveImpl implements Serializable, BusinessArchive {

  private static final long serialVersionUID = 218650935088396315L;

  public static final String PROCESS_RESOURCE_NAME = "process-def.xml";

  protected Set<Resource> resources = new HashSet<Resource>();
  protected ProcessDefinitionUUID processUUID;

  protected BusinessArchiveImpl() {
  }

  public BusinessArchiveImpl(final ProcessDefinition clientProcess, final Map<String, byte[]> resources,
      final Class<?>... classes) throws IOException, ClassNotFoundException {
    if (clientProcess != null) {
      processUUID = clientProcess.getUUID();
      addResource(PROCESS_RESOURCE_NAME, XmlDefExporter.getInstance().createProcessDefinition(clientProcess));
    }

    final Map<String, byte[]> allClasses = new HashMap<String, byte[]>();
    if (resources != null) {
      for (final Map.Entry<String, byte[]> resource : resources.entrySet()) {
        final String resourcePath = resource.getKey();
        if (resourcePath.endsWith(".class")) {
          allClasses.put(resource.getKey(), resource.getValue());
        } else {
          addResource(resource.getKey(), resource.getValue());
        }
      }
    }
    if (classes != null && classes.length > 0) {
      for (final Class<?> clazz : classes) {
        if (clazz != null) {
          allClasses.put(clazz.getName().replace(".", "/") + ".class", ClassDataTool.getClassData(clazz));
        }
      }
    }
    if (!allClasses.isEmpty()) {
      final byte[] jar = Misc.generateJar(allClasses);
      addResource("bonita-generated.jar", jar);
    }
  }

  public BusinessArchiveImpl(final ProcessDefinitionUUID processUUID, final Map<String, byte[]> resources) {
    this.processUUID = processUUID;
    if (resources != null) {
      for (final Map.Entry<String, byte[]> resource : resources.entrySet()) {
        addResource(resource.getKey(), resource.getValue());
      }
    }
  }

  @Override
  public ProcessDefinitionUUID getProcessUUID() {
    return processUUID;
  }

  @Override
  public ProcessDefinition getProcessDefinition() {
    final byte[] processBytes = getResource(PROCESS_RESOURCE_NAME);
    if (processBytes == null) {
      return null;
    }
    File xmlDefFile = null;
    try {
      final File tempDir = Misc.createDirectories(BonitaConstants.getTemporaryFolder());
      xmlDefFile = Misc.createTempFile("xmlDef", null, tempDir);
      Misc.getFile(xmlDefFile, processBytes);
      final Properties contextProperties = BusinessArchiveFactory.createPropertiesFromResources(getResources());
      return ProcessBuilder.createProcessFromXmlDefFile(xmlDefFile.toURL(), contextProperties);
    } catch (final IOException e) {
      throw new BonitaRuntimeException(e);
    } finally {
      if (xmlDefFile != null) {
        xmlDefFile.delete();
      }
    }
  }

  @Override
  public Map<String, byte[]> getJarFiles() {
    return getResources(".*\\.jar");
  }

  @Override
  public byte[] getResource(final String resourcePath) {
    for (final Resource resource : resources) {
      if (resource.getPath().equals(resourcePath)) {
        return resource.getData();
      }
    }
    return null;
  }

  @Override
  public Map<String, byte[]> getResources(final String regex) {
    final Map<String, byte[]> result = new HashMap<String, byte[]>();
    for (final Resource resource : resources) {
      if (resource.getPath().matches(regex)) {
        result.put(resource.getPath(), resource.getData());
      }
    }
    return result;
  }

  @Override
  public Map<String, byte[]> getOtherResources(final String regex) {
    final Map<String, byte[]> result = new HashMap<String, byte[]>();
    for (final Resource resource : resources) {
      if (!resource.getPath().matches(regex)) {
        result.put(resource.getPath(), resource.getData());
      }
    }
    return result;
  }

  public void addResource(final String resourcePath, final byte[] resourceData) {
    Misc.checkArgsNotNull(resourcePath, resourceData);
    resources.add(new Resource(resourcePath, resourceData));
  }

  @Override
  public Map<String, byte[]> getResources() {
    final Map<String, byte[]> allResources = new HashMap<String, byte[]>();
    for (final Resource resource : resources) {
      allResources.put(resource.getPath(), resource.getData());
    }
    return allResources;
  }

  // Used by REST API
  @Override
  public String toString() {
    final XStream xstream = XStreamUtil.getDefaultXstream();
    return xstream.toXML(this);
  }

  protected static class Resource implements Serializable {

    private static final long serialVersionUID = 4985009903197019706L;
    protected String path;
    protected byte[] data;

    protected Resource() {
    }

    public Resource(final String path, final byte[] data) {
      super();
      this.path = path;
      this.data = data;
    }

    public String getPath() {
      return path;
    }

    public byte[] getData() {
      return data;
    }
  }

}
