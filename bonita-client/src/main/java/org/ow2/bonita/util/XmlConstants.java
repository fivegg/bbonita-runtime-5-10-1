/**
 * Copyright (C) 2010  BonitaSoft S.A.
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
package org.ow2.bonita.util;

/**
 * @author Marc Blachon, Guillaume Porcher, Charles Souillard, Miguel Valdes, Pierre Vigneras
 */
public final class XmlConstants {

  private XmlConstants() { }
  /**
   * XML schema xsd file name.
   */
  public static final String XML_SCHEMA = "XMLSchema.xsd";

  /**
   * XML schema ns.
   */
  public static final String XML_NS = "http://www.w3.org/2001/XMLSchema";

  /**
   * XML schema ns.
   */
  public static final String XML_NS2 = "http://www.w3.org/2001/xml.xsd";

  /**
   * XPDL 1.0 schema xsd file named.
   */
  public static final String XPDL_1_0_SCHEMA = "XPDL_1_0.xsd";
  
  /**
   * XML process definition transitional schema xsd file name (compatibility mode).
   */
  public static final String XML_PROCESS_DEF_TRANSITIONAL_SCHEMA = "XMLProcessDef_transitional.xsd";
  
 
  /**
   * XML process definition strict schema xsd file name (current version of xsd).
   */
  public static final String XML_PROCESS_DEF_STRICT_SCHEMA = "XMLProcessDef_strict.xsd";

  /**
   * XPDL 1.0 executable schema ns.
   */
  public static final String XPDL_1_0_NS = "http://www.wfmc.org/2002/XPDL1.0";

  public static final String JAXP_SCHEMALANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
  public static final String JAXP_SCHEMASOURCE = "http://java.sun.com/xml/jaxp/properties/schemaSource";

}
