/**
 * Copyright (C) 2010 BonitaSoft S.A.
 * BonitaSoft, 31 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.ow2.bonita.facade.runtime.impl;

import java.util.UUID;

import org.ow2.bonita.facade.runtime.Category;

/**
 * @author Nicolas Chabanoles
 *
 */
public class CategoryImpl implements Category, Comparable<Category>{

  private static final long serialVersionUID = 3739642393183711592L;
  
  protected long dbid;
  protected String uuid;
  protected String name;
  protected String readonlyCSSStyleName;
  protected String iconCSSStyle;
  protected String previewCSSStyleName;

  
  
  protected CategoryImpl() {
    super();
	// Mandatory for serialization.
  }
  
  /**
   * Default constructor.
   * @param aLabelName
   */
  public CategoryImpl(String name) {
    this.name = name;
    this.uuid = UUID.randomUUID().toString();
  }
  
  public CategoryImpl(Category category) {
	 this.name = category.getName();
	 this.uuid = category.getUUID();
	 this.iconCSSStyle = category.getIconCSSStyle();
	 this.previewCSSStyleName = category.getPreviewCSSStyleName();
	 this.readonlyCSSStyleName = category.getReadonlyCSSStyleName();
  }

  public String getUUID() {
    return uuid;
  }
  
  public String getName() {
	return this.name;
  }

  public void setName(String name) {
	this.name = name;
  }
  
  public String getIconCSSStyle() {
    return this.iconCSSStyle;
  }

  public void setIconCSSStyle(String aIconCSSStyle) {
    this.iconCSSStyle = aIconCSSStyle;
  }

  public String getReadonlyCSSStyleName() {
    return this.readonlyCSSStyleName;
  }

  public void setReadonlyCSSStyleName(String aReadonlyCSSStyleName) {
    this.readonlyCSSStyleName = aReadonlyCSSStyleName;
  }

  public String getPreviewCSSStyleName() {
    return this.previewCSSStyleName;
  }

  public void setPreviewCSSStyleName(String aPreviewCSSStyleName) {
    this.previewCSSStyleName = aPreviewCSSStyleName;
  }
  
  @Override
  public boolean equals(Object o) {
    if (o == null) {
      return false;
    }
    if (!(o instanceof CategoryImpl)) {
      return false;
    }
    CategoryImpl other = (CategoryImpl) o;
    return name.equals(other.getName());
  }
  
  @Override
  public int hashCode() {
    return name.hashCode();
  }
  public int compareTo(Category o) {
    return name.compareTo(o.getName());
  }

}
