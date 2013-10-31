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
package org.ow2.bonita.util.xml;

import java.io.Serializable;

import org.xml.sax.SAXParseException;

/**
 * a unification for an XML parsing errors and DOM interpretation problems, see
 * also {@link Parser}.
 * 
 * See also <a href="./Parser.html#problems">'Problems'</a>
 * 
 * @author Tom Baeyens
 */
public class Problem implements Serializable {

  private static final long serialVersionUID = 1L;

  public static final String SEVERITY_WARNING = "warning";
  public static final String SEVERITY_ERROR = "error";
  public static final String SEVERITY_FATALERROR = "fatal-error";

  String resource;
  String severity;
  Integer line = -1;
  Integer column = -1;
  String msg;
  Throwable cause;

  public Problem(String msg, String severity) {
    this.severity = severity;
    this.msg = msg;
  }
  
  public Problem(String msg, Exception e, String severity) {
    this.severity = severity;
    this.msg = msg;
    this.cause = e;
    if (e instanceof SAXParseException) {
      SAXParseException spe = (SAXParseException) e;
      this.resource = spe.getPublicId();
      this.line = spe.getLineNumber();
      this.column = spe.getColumnNumber();
    }
  }

  public Throwable getCause() {
    return cause;
  }

  public void setCause(Throwable cause) {
    this.cause = cause;
  }

  public int getColumn() {
    return column;
  }

  public void setColumn(int columnNumber) {
    this.column = columnNumber;
  }

  public int getLine() {
    return line;
  }

  public void setLine(int lineNumber) {
    this.line = lineNumber;
  }

  public String getMsg() {
    return msg;
  }

  public void setMsg(String msg) {
    this.msg = msg;
  }

  public String getResource() {
    return resource;
  }

  public void setResource(String resource) {
    this.resource = resource;
  }

  public String getSeverity() {
    return severity;
  }

  public void setSeverity(String severity) {
    this.severity = severity;
  }

  public String toString() {
    StringBuffer text = new StringBuffer();
    text.append(severity);
    text.append(" : ");
    text.append(msg);
    text.append(" ");

    if ((line != -1) || (column != -1) || (resource != null)) {
      text.append("[");
      if (line != -1) {
        text.append("line=" + line + " ");
      }

      if (column != -1) {
        text.append("column=" + column + " ");
      }

      if (resource != null) {
        text.append("resource=" + resource + " ");
      }
      text.append("]");
    }

    if (cause != null) {
      text.append(": " + cause.toString());
    }

    return text.toString();
  }
}
