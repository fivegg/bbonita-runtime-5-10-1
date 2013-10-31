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
 */
package org.ow2.bonita.env.binding;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.hibernate.cfg.Configuration;
import org.ow2.bonita.env.descriptor.HibernateConfigurationDescriptor;
import org.ow2.bonita.env.descriptor.PropertiesDescriptor;
import org.ow2.bonita.util.ReflectUtil;
import org.ow2.bonita.util.stream.FileStreamSource;
import org.ow2.bonita.util.stream.ResourceStreamSource;
import org.ow2.bonita.util.stream.StreamSource;
import org.ow2.bonita.util.stream.UrlStreamSource;
import org.ow2.bonita.util.xml.Parse;
import org.ow2.bonita.util.xml.Parser;
import org.ow2.bonita.util.xml.XmlUtil;
import org.w3c.dom.Element;

/**
 * parses a descriptor for creating a hibernate Configuration.
 * 
 * See schema docs for more details.
 * 
 * @author Tom Baeyens
 */
public class HibernateConfigurationBinding extends WireDescriptorBinding {

  static final Logger LOG = Logger.getLogger(HibernateConfigurationBinding.class.getName());

  private static final PropertiesBinding PROPERTIES_BINDING = new PropertiesBinding();

  private static final MappingParser MAPPING_PARSER = new MappingParser();

  public HibernateConfigurationBinding() {
    super("hibernate-configuration");
  }

  protected HibernateConfigurationBinding(final String tagName) {
    super(tagName);
  }

  static class MappingParser extends Parser {
    @Override
    public Object parseDocumentElement(final Element documentElement, final Parse parse) {
      final List<Element> elements = XmlUtil.elements(documentElement, "mapping");
      if (elements != null) {
        final HibernateConfigurationDescriptor descriptor = parse.findObject(HibernateConfigurationDescriptor.class);
        for (final Element element : elements) {
          parseMapping(element, descriptor, parse);
        }
      }
      return null;
    }

  }

  static void parseMapping(final Element element, final HibernateConfigurationDescriptor descriptor, final Parse parse) {
    if (element.hasAttribute("resource")) {
      final String resource = element.getAttribute("resource");
      if (LOG.isLoggable(Level.FINE)) {
        LOG.fine("adding hibernate configuration resource " + resource);
      }
      descriptor.addMappingOperation(new HibernateConfigurationDescriptor.AddResource(resource));

    } else if (element.hasAttribute("file")) {
      final String fileName = element.getAttribute("file");
      if (LOG.isLoggable(Level.FINE)) {
        LOG.fine("adding hibernate configuration file " + fileName);
      }
      descriptor.addMappingOperation(new HibernateConfigurationDescriptor.AddFile(fileName));

    } else if (element.hasAttribute("class")) {
      final String className = element.getAttribute("class");
      if (LOG.isLoggable(Level.FINE)) {
        LOG.fine("adding hibernate configuration class " + className);
      }
      descriptor.addMappingOperation(new HibernateConfigurationDescriptor.AddClass(className));

    } else if (element.hasAttribute("url")) {
      final String urlText = element.getAttribute("url");
      if (LOG.isLoggable(Level.FINE)) {
        LOG.fine("adding hibernate configuration url " + urlText);
      }
      descriptor.addMappingOperation(new HibernateConfigurationDescriptor.AddUrl(urlText));

    } else {
      parse.addProblem("exactly 1 attribute in {resource, file, class, url} was expected in mapping: "
          + XmlUtil.toString(element));
    }
  }

  @Override
  public Object parse(final Element element, final Parse parse, final Parser parser) {
    final HibernateConfigurationDescriptor descriptor = new HibernateConfigurationDescriptor();

    String configurationClassName = null;
    if (element.hasAttribute("class")) {
      configurationClassName = element.getAttribute("class");
    } else {
      configurationClassName = Configuration.class.getName();
    }
    descriptor.setClassName(configurationClassName);

    if (element.hasAttribute("schema-operation")) {
      final String schemaOperation = element.getAttribute("schema-operation");
      if ("create".equals(schemaOperation)) {
        descriptor.setSchemaOperation(HibernateConfigurationDescriptor.CreateSchema.getInstance());
      } else if ("update".equals(schemaOperation)) {
        descriptor.setSchemaOperation(HibernateConfigurationDescriptor.UpdateSchema.getInstance());
      }
    }

    final List<Element> configElements = XmlUtil.elements(element);
    if (configElements != null) {
      for (final Element configElement : configElements) {

        if ("mappings".equals(XmlUtil.getTagLocalName(configElement))) {
          if (configElement.hasAttribute("resource")) {
            final String resources = configElement.getAttribute("resource");
            try {
              final URL url = ReflectUtil.getResource(parse.getClassLoader(), resources);
              if (url != null) {
                if (LOG.isLoggable(Level.FINE)) {
                  LOG.fine("importing mappings from " + url);
                }
                final InputStream inputStream = url.openStream();
                MAPPING_PARSER.createParse().pushObject(descriptor).setInputStream(inputStream).execute();
              }
            } catch (final Exception e) {
              parse.addProblem("couldn't parse hibernate mapping resources '" + resources + "'", e);
            }
          }

        } else if ("mapping".equals(XmlUtil.getTagLocalName(configElement))) {

          parseMapping(configElement, descriptor, parse);

        } else if ("properties".equals(XmlUtil.getTagLocalName(configElement))) {
          final PropertiesDescriptor propertiesDescriptor = (PropertiesDescriptor) PROPERTIES_BINDING.parse(
              configElement, parse, parser);
          descriptor.setPropertiesDescriptor(propertiesDescriptor);

        } else if ("cache-configuration".equals(XmlUtil.getTagLocalName(configElement))) {
          StreamSource streamSource = null;

          final String cacheUsage = configElement.getAttribute("usage");
          if (!("read-only".equals(cacheUsage) || "nonstrict-read-write".equals(cacheUsage)
              || "read-write".equals(cacheUsage) || "transactional".equals(cacheUsage))) {
            parse.addProblem("problem in cache-configuration: no usage attribute or illegal value: " + cacheUsage
                + " Possible values are {read-only, nonstrict-read-write, read-write, transactional}");
          } else {

            if (configElement.hasAttribute("file")) {
              final String fileName = configElement.getAttribute("file");
              final File file = new File(fileName);
              if (file.exists() && file.isFile()) {
                streamSource = new FileStreamSource(file);
              } else {
                parse.addProblem("file " + fileName + " isn't a file");
              }
            }

            if (configElement.hasAttribute("resource")) {
              final String resource = configElement.getAttribute("resource");
              streamSource = new ResourceStreamSource(resource, parse.getClassLoader());
            }

            if (configElement.hasAttribute("url")) {
              final String urlText = configElement.getAttribute("url");
              try {
                final URL url = new URL(urlText);
                streamSource = new UrlStreamSource(url);
              } catch (final Exception e) {
                parse.addProblem("couldn't open url " + urlText, e);
              }
            }

            if (streamSource != null) {
              parser.importStream(streamSource, configElement, parse);
            }

            // parse the cache configurations in the same way as the hibernate
            // cfg schema
            // translate the contents of the file into invoke operations for
            // methods
            // Configuration.setCacheConcurrencyStrategy(String clazz, String
            // concurrencyStrategy, String region)
            // Configuration.setCollectionCacheConcurrencyStrategy(String
            // collectionRole, String concurrencyStrategy)
            // <class-cache class="org.hibernate.auction.Item"
            // usage="read-write"/>
            // <class-cache class="org.hibernate.auction.Bid"
            // usage="read-only"/>
            // <collection-cache collection="org.hibernate.auction.Item.bids"
            // usage="read-write"/>
            final List<Element> cacheElements = XmlUtil.elements(configElement);
            if (cacheElements != null) {
              for (final Element cacheElement : cacheElements) {

                if ("class-cache".equals(XmlUtil.getTagLocalName(cacheElement))) {
                  final String className = cacheElement.getAttribute("class");
                  descriptor.addCacheOperation(new HibernateConfigurationDescriptor.SetCacheConcurrencyStrategy(
                      className, cacheUsage));

                } else if ("collection-cache".equals(XmlUtil.getTagLocalName(cacheElement))) {
                  final String collection = cacheElement.getAttribute("collection");
                  descriptor
                      .addCacheOperation(new HibernateConfigurationDescriptor.SetCollectionCacheConcurrencyStrategy(
                          collection, cacheUsage));

                } else {
                  parse.addProblem("unknown hibernate cache configuration element " + XmlUtil.toString(configElement));
                }
              }
            }
          }

        } else {
          parse.addProblem("unknown hibernate configuration element " + XmlUtil.toString(configElement));
        }
      }
    }

    return descriptor;
  }

}
