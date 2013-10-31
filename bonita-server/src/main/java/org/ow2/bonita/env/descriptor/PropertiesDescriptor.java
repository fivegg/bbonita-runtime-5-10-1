/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 * 
 * Modified by Matthieu Chaffotte - BonitaSoft S.A.
 */
package org.ow2.bonita.env.descriptor;

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import org.ow2.bonita.env.WireContext;
import org.ow2.bonita.env.WireDefinition;
import org.ow2.bonita.env.WireException;
import org.ow2.bonita.util.BonitaConstants;
import org.ow2.bonita.util.ExceptionManager;

/**
 * @author Tom Baeyens
 */
public class PropertiesDescriptor extends MapDescriptor {

  private static final long serialVersionUID = 1L;

  protected String url;

  protected String file;

  protected String resource;

  protected boolean isXml;

  public PropertiesDescriptor() {
    super();
    className = Properties.class.getName();
  }

  @Override
  public Object construct(final WireContext wireContext) {
    return new Properties();
  }

  @Override
  public void initialize(final Object object, final WireContext wireContext) {
    String description = null;
    try {
      if (url != null) {
        description = "url " + url;
        final InputStream inputStream = new URL(url).openStream();
        load(object, inputStream);
        inputStream.close();
      }

      if (file != null) {
        final String bonitaHome = "${" + BonitaConstants.HOME + "}";
        if (file.startsWith(bonitaHome)) {
          final String bhPropertyValue = System.getProperty(BonitaConstants.HOME);
          file = file.replace(bonitaHome, bhPropertyValue);
        }
        description = "file " + file;
        final InputStream inputStream = new FileInputStream(file);
        load(object, inputStream);
        inputStream.close();
      }

      if (resource != null) {
        description = "resource " + resource;
        final InputStream inputStream = wireContext.getClassLoader().getResourceAsStream(resource);
        if (inputStream == null) {
          throw new RuntimeException("resource " + resource + " doesn't exist");
        }
        load(object, inputStream);
        inputStream.close();
      }

    } catch (final Exception e) {
      final String message = ExceptionManager.getInstance().getFullMessage("bp_PD_1", description);
      throw new WireException(message, e);
    }

    super.initialize(object, wireContext);
  }

  @Override
  public Class<?> getType(final WireDefinition wireDefinition) {
    return Properties.class;
  }

  protected void load(final Object object, final InputStream inputStream) throws Exception {
    final Properties properties = (Properties) object;
    if (isXml) {
      properties.loadFromXML(inputStream);
    } else {
      properties.load(inputStream);
    }
  }

  public String getFile() {
    return file;
  }

  public void setFile(final String file) {
    this.file = file;
  }

  public String getResource() {
    return resource;
  }

  public void setResource(final String resource) {
    this.resource = resource;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(final String url) {
    this.url = url;
  }

  public boolean isXml() {
    return isXml;
  }

  public void setXml(final boolean isXml) {
    this.isXml = isXml;
  }

}
