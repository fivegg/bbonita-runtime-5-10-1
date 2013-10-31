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
import java.net.URL;
import java.util.List;

import org.ow2.bonita.env.descriptor.VariableTypeResolverDescriptor;
import org.ow2.bonita.type.Converter;
import org.ow2.bonita.type.Matcher;
import org.ow2.bonita.type.Type;
import org.ow2.bonita.type.TypeMapping;
import org.ow2.bonita.type.matcher.ClassNameMatcher;
import org.ow2.bonita.type.matcher.HibernateLongIdMatcher;
import org.ow2.bonita.type.matcher.HibernateStringIdMatcher;
import org.ow2.bonita.type.matcher.SerializableMatcher;
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
 * parses a descriptor for Boolean.TRUE.
 * 
 * See schema docs for more details.
 * 
 * @author Tom Baeyens
 */
public class VariableTypesBinding extends WireDescriptorBinding {

  public VariableTypesBinding() {
    super("variable-types");
  }

  public Object parse(Element element, Parse parse, Parser parser) {
    StreamSource streamSource = null;
    if (element.hasAttribute("file")) {
      String fileName = element.getAttribute("file");
      File file = new File(fileName);
      if (file.exists() && file.isFile()) {
        streamSource = new FileStreamSource(file);
        parser.importStream(streamSource, element, parse);
      } else {
        parse.addProblem("file " + fileName + " isn't a file");
      }
    }

    if (element.hasAttribute("resource")) {
      String resource = element.getAttribute("resource");
      streamSource = new ResourceStreamSource(resource, parse.getClassLoader());
      parser.importStream(streamSource, element, parse);
    }

    if (element.hasAttribute("url")) {
      String urlText = element.getAttribute("url");
      try {
        URL url = new URL(urlText);
        streamSource = new UrlStreamSource(url);
        parser.importStream(streamSource, element, parse);
      } catch (Exception e) {
        parse.addProblem("couldn't open url " + urlText, e);
      }
    }

    VariableTypeResolverDescriptor variableTypeResolverDescriptor = new VariableTypeResolverDescriptor();

    List<Element> typeElements = XmlUtil.elements(element, "type");
    if (typeElements != null) {
      for (Element typeElement : typeElements) {
        TypeMapping typeMapping = parseTypeMapping(typeElement, parse, parser);
        variableTypeResolverDescriptor.addTypeMapping(typeMapping);
      }
    }
    return variableTypeResolverDescriptor;
  }

  protected TypeMapping parseTypeMapping(Element element, Parse parse,
      Parser parser) {
    TypeMapping typeMapping = new TypeMapping();
    Type type = new Type();
    typeMapping.setType(type);

    // type name
    if (element.hasAttribute("name")) {
      type.setName(element.getAttribute("name"));
    }

    String hibernateSessionFactoryName = XmlUtil.attribute(element,
        "hibernate-session-factory");

    // first we get the matcher
    Matcher matcher = null;
    if (element.hasAttribute("class")) {
      String className = element.getAttribute("class");

      // if type="serializable"
      if ("serializable".equals(className)) {
        matcher = new SerializableMatcher();

        // if type="persistable"
      } else if ("persistable".equals(className)) {
        if (element.hasAttribute("id-type")) {
          String idType = element.getAttribute("id-type");
          if ("long".equalsIgnoreCase(idType)) {
            matcher = new HibernateLongIdMatcher(hibernateSessionFactoryName);
          } else if ("string".equalsIgnoreCase(idType)) {
            matcher = new HibernateStringIdMatcher(hibernateSessionFactoryName);
          } else {
            parse.addProblem("id-type was not 'long' or 'string': " + idType);
          }
        } else {
          parse.addProblem("id-type is required in a persistable type");
        }

        // otherwise, we expect type="some.java.ClassName"
      } else {
        matcher = new ClassNameMatcher(className);
      }

    } else {
      // look for the matcher element
      Element matcherElement = XmlUtil.element(element, "matcher");
      Element matcherObjectElement = XmlUtil.element(matcherElement);
      if (matcherObjectElement != null) {
        try {
          matcher = (Matcher) parser.parseElement(matcherObjectElement, parse);
        } catch (ClassCastException e) {
          parse.addProblem("matcher is not a " + Matcher.class.getName() + ": "
              + (matcher != null ? matcher.getClass().getName() : "null"));
        }
      } else {
        parse
            .addProblem("no matcher specified in " + XmlUtil.toString(element));
      }
    }

    typeMapping.setMatcher(matcher);

    // parsing the converter
    Converter converter = null;
    if (element.hasAttribute("converter")) {
      String converterClassName = element.getAttribute("converter");
      ClassLoader classLoader = parse.getClassLoader();
      try {
        Class<?> converterClass = ReflectUtil.loadClass(classLoader,
            converterClassName);
        converter = (Converter) converterClass.newInstance();
      } catch (Exception e) {
        parse
            .addProblem("couldn't instantiate converter " + converterClassName);
      }
    } else {
      // look for the matcher element
      Element converterElement = XmlUtil.element(element, "converter");
      Element converterObjectElement = XmlUtil.element(converterElement);
      if (converterObjectElement != null) {
        try {
          converter = (Converter) parser.parseElement(converterObjectElement,
              parse);
        } catch (ClassCastException e) {
          parse.addProblem("converter is not a " + Converter.class.getName()
              + ": "
              + (converter != null ? converter.getClass().getName() : "null"));
        }
      }
    }

    type.setConverter(converter);

    // parsing the variable class

    Class<?> variableClass = null;
    if (element.hasAttribute("variable-class")) {
      String variableClassName = element.getAttribute("variable-class");
      ClassLoader classLoader = parse.getClassLoader();
      try {
        variableClass = ReflectUtil.loadClass(classLoader, variableClassName);
      } catch (Exception e) {
        parse.addProblem("couldn't instantiate variable-class "
            + variableClassName, e);
      }
    } else {
      parse.addProblem("variable-class is required on a type: "
          + XmlUtil.toString(element));
    }

    type.setVariableClass(variableClass);

    return typeMapping;
  }
}
