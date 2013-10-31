/**
 * Copyright (C) 2009  BonitaSoft S.A.
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
package org.ow2.bonita.connector.core.desc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 
 * @author Matthieu Chaffotte
 *
 */
public class ConnectorDescriptor {

  private String connectorId;
  private String version;
  private String icon;
  private List<Category> categories;
  private List<Setter> inputs;
  private List<Getter> outputs;
  private List<Page> pages;
  private ClassLoader classLoader;

  protected ConnectorDescriptor() {}

  public ConnectorDescriptor(String connectorId, String version, ClassLoader classLoader) {
    this.connectorId = connectorId;
    this.version = version;
    this.classLoader = classLoader;
  }

  public void setIcon(String iconPath) {
    icon = iconPath;
  }

  public void addCategory(String name, String icon) {
    if (categories == null) {
      categories = new ArrayList<Category>();
    }
    Category category = new Category(name, icon, classLoader);
    if (!categories.contains(category)) {
      categories.add(category);
    }
  }

  public void removeCategory(Category category) {
    if (categories != null) {
      categories.remove(category);
    }
  }
  
  public void addSetter(Setter setter) {
    if (inputs == null) {
      inputs = new ArrayList<Setter>();
    }
    if (!inputs.contains(setter)) {
      inputs.add(setter);
    }
  }

  public void addGetter(Getter getter) {
    if (outputs == null) {
      outputs = new ArrayList<Getter>();
    }
    if (!outputs.contains(getter)) {
      outputs.add(getter);
    }
  }

  public void addPage(Page page) {
    if (pages == null) {
      pages = new ArrayList<Page>();
    }
    if (!pages.contains(page)) {
      pages.add(page);
    }
  }

  public ConnectorDescriptor(String connectorId, List<Category> categories,
      String icon, List<Setter> inputs, List<Getter> outputs, List<Page> pages) {
    this(connectorId, categories, icon, "1.0", inputs, outputs, pages);
  }

  public ConnectorDescriptor(String connectorId, List<Category> categories,
      String icon, String version, List<Setter> inputs, List<Getter> outputs,
      List<Page> pages) {
    this.connectorId = connectorId;
    this.categories = categories;
    this.icon = icon;
    this.inputs = inputs;
    this.outputs = outputs;
    this.pages = pages;
    this.version = version;
  }

  public String getConnectorId() {
    return connectorId;
  }

  public List<Category> getCategories() {
    return  categories;
  }

  public String getVersion() {
    return version;
  }

  public List<Setter> getInputs() {
    if (inputs == null) {
      return Collections.emptyList();
    } else {
      return new ArrayList<Setter>(inputs);
    }
  }

  public List<Getter> getOutputs() {
    if (outputs == null) {
      return Collections.emptyList();
    } else {
      return new ArrayList<Getter>(outputs);
    }
  }

  public List<Page> getPages() {
    return pages;
  }

  public String getIcon() {
    return icon;
  }

  public void setVersion(String version) {
    this.version = version;
  }

}
