/*
 * JBoss, Home of Professional Open Source
 * 
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
package org.ow2.bonita.util.xml;

import java.io.File;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Properties;
import java.util.Stack;

import javax.xml.parsers.DocumentBuilder;

import org.ow2.bonita.util.stream.FileStreamSource;
import org.ow2.bonita.util.stream.InputStreamSource;
import org.ow2.bonita.util.stream.ResourceStreamSource;
import org.ow2.bonita.util.stream.StreamSource;
import org.ow2.bonita.util.stream.StringStreamSource;
import org.ow2.bonita.util.stream.UrlStreamSource;
import org.w3c.dom.Document;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;

/**
 * information related to one single parse operation, for instructions see {@link Parser}.
 * 
 * @author Tom Baeyens
 */
public class Parse implements Serializable, ErrorHandler {

  private static final long serialVersionUID = 1L;

  protected Parser parser;

  protected ClassLoader classLoader;
  protected StreamSource streamSource;
  protected InputStream inputStream;
  protected InputSource inputSource;

  protected Properties contextProperties;

  protected DocumentBuilder documentBuilder = null;
  protected Document document = null;

  protected Stack<Object> objectStack;
  protected List<Problem> problems = null;
  protected Object documentObject;

  protected Parse(final Parser parser) {
    this.parser = parser;
  }

  /** specify an input stream as the source for this parse */
  public Parse setInputStream(final InputStream inputStream) {
    this.streamSource = new InputStreamSource(inputStream);
    return this;
  }

  /** specify a URL as the source for this parse */
  public Parse setUrl(final URL url) {
    this.streamSource = new UrlStreamSource(url);
    return this;
  }

  /** specify a file as the source for this parse */
  public Parse setFile(final File file) {
    this.streamSource = new FileStreamSource(file);
    return this;
  }

  /**
   * specify the classLoader to be used for resource input (this is optional)
   */
  public Parse setClassLoader(final ClassLoader classLoader) {
    this.classLoader = classLoader;
    return this;
  }

  /** specify a resource as the source for this parse */
  public Parse setResource(final String resource) {
    this.streamSource = new ResourceStreamSource(resource, classLoader);
    return this;
  }

  /** specify an XML string as the source for this parse */
  public Parse setString(final String xmlString) {
    this.streamSource = new StringStreamSource(xmlString);
    return this;
  }

  /** specify a {@link StreamSource} as the source for this parse */
  public Parse setStreamSource(final StreamSource streamSource) {
    this.streamSource = streamSource;
    return this;
  }

  /** specify an InputStream as the source for this parse */
  public Parse setInputSource(final InputSource inputSource) {
    this.inputSource = inputSource;
    return this;
  }

  /**
   * normally the Document Object Model is created during the parse execution, but providing a document can be
   * convenient when the DOM is already available and only the walking of the DOM needs to be done by the parser. If the
   * document is provide, building the DOM from a source is skipped.
   */
  public Parse setDocument(final Document document) {
    this.document = document;
    return this;
  }

  /** provides the result of this parse operation. */
  public Parse setDocumentObject(final Object object) {
    this.documentObject = object;
    return this;
  }

  /** perform the actual parse operation with the specified input source. */
  public Parse execute() {
    parser.execute(this);
    return this;
  }

  // problems /////////////////////////////////////////////////////////////////

  /** all problems encountered */
  public List<Problem> getProblems() {
    return problems;
  }

  /** to add parsing problems during XML parsing and DOM walking. */
  public void addProblem(final Problem problem) {
    if (problems == null) {
      problems = new ArrayList<Problem>();
    }
    problems.add(problem);
  }

  /** add a problem with {@link Problem#SEVERITY_ERROR the default severity}. */
  public void addProblem(final String msg) {
    addProblem(msg, null);
  }

  /**
   * add a problem with an exception cause and {@link Problem#SEVERITY_ERROR the default severity}.
   */
  public void addProblem(final String msg, final Exception e) {
    addProblem(msg, e, Problem.SEVERITY_ERROR);
  }

  /** adds a problem with {@link Problem#SEVERITY_WARNING severity warning}. */
  public void addWarning(final String msg) {
    addWarning(msg, null);
  }

  /**
   * adds a problem with {@link Problem#SEVERITY_WARNING severity warning} and an exception as the cause.
   */
  public void addWarning(final String msg, final Exception e) {
    addProblem(msg, e, Problem.SEVERITY_WARNING);
  }

  /** adds a problem given message, exception cause and severity */
  public void addProblem(final String msg, final Exception e, final String severity) {
    addProblem(new Problem(msg, e, severity));
  }

  /** indicates presence of problems */
  public boolean hasProblems() {
    return problems != null && problems.size() > 0;
  }

  /**
   * allows to provide the list object that should be used to capture the parsing problems.
   */
  public Parse setProblems(final List<Problem> problems) {
    this.problems = problems;
    return this;
  }

  /** part of {@link ErrorHandler} to capture XML parsing problems. */
  @Override
  public void error(final SAXParseException e) {
    addProblem(e.getMessage(), e, Problem.SEVERITY_ERROR);
  }

  /** part of {@link ErrorHandler} to capture XML parsing problems. */
  @Override
  public void fatalError(final SAXParseException e) {
    addProblem(e.getMessage(), e, Problem.SEVERITY_FATALERROR);
  }

  /** part of {@link ErrorHandler} to capture XML parsing problems. */
  @Override
  public void warning(final SAXParseException e) {
    addProblem(e.getMessage(), e, Problem.SEVERITY_WARNING);
  }

  // contextual objects ///////////////////////////////////////////////////////

  /** push a contextual object on the stack of this parse. */
  public Parse pushObject(final Object object) {
    if (objectStack == null) {
      objectStack = new Stack<Object>();
    }
    objectStack.push(object);
    return this;
  }

  /** remove a contextual object from the stack. */
  public Object popObject() {
    if (objectStack != null) {
      return objectStack.pop();
    }
    return null;
  }

  /** look up the top contextual object from the stack. */
  public Object peekObject() {
    if (objectStack != null) {
      return objectStack.peek();
    }
    return null;
  }

  /** search a contextual object in the stack by type. */
  @SuppressWarnings("unchecked")
  public <T> T findObject(final Class<T> clazz) {
    if (objectStack != null && !objectStack.isEmpty()) {
      final ListIterator<Object> listIter = objectStack.listIterator(objectStack.size());
      while (listIter.hasPrevious()) {
        final Object object = listIter.previous();
        if (object != null && clazz.isAssignableFrom(object.getClass())) {
          return (T) object;
        }
      }
      return null;
    }
    return null;
  }

  // getters and setters //////////////////////////////////////////////////////

  /** the result of this parse operation. */
  public Object getDocumentObject() {
    return documentObject;
  }

  /** the Document Object Model (DOM). */
  public Document getDocument() {
    return document;
  }

  /** the ClassLoader used to resolve input resources. */
  public ClassLoader getClassLoader() {
    return classLoader;
  }

  public Parser getParser() {
    return this.parser;
  }

  public Properties getContextProperties() {
    return contextProperties;
  }

  public void setContextProperties(final Properties contextProperties) {
    this.contextProperties = contextProperties;
  }

}
