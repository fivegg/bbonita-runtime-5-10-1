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
package org.ow2.bonita.facade.runtime.impl;

import org.ow2.bonita.facade.runtime.Category;
import org.ow2.bonita.facade.runtime.Label;

/**
 * This class defines the concept of 'Label'. A Label is a human readable text
 * value that can be associated to a 'Case'. This joins the concept of 'key
 * words' that can be associated to documents to help the indexing.
 * 
 * @author Nicolas Chabanoles, Matthieu Chaffotte
 * 
 */
public class LabelImpl extends CategoryImpl implements Label {

  private static final long serialVersionUID = 888850443702378668L;

  protected String ownerName;
  protected String editableCSSStyleName;

  protected boolean isVisible;
  protected boolean hasToBeDisplayed;
  protected int displayOrder;
  protected boolean isSystemLabel;

  protected LabelImpl() {
    super();
    // Mandatory for serialization.
  }

  public LabelImpl(final String labelName, final String ownerName, final String editableCSSStyleName,
      final String readonlyCSSStyleName, final String previewCSSStyleName, final boolean isVisible,
      final boolean hasToBeDisplayed, final String iconCSSStyle, final int displayOrder, final boolean isSystemLabel) {
    super(labelName);
    this.ownerName = ownerName;
    this.editableCSSStyleName = editableCSSStyleName;
    this.readonlyCSSStyleName = readonlyCSSStyleName;
    this.previewCSSStyleName = previewCSSStyleName;
    this.isVisible = isVisible;
    this.hasToBeDisplayed = hasToBeDisplayed;
    this.iconCSSStyle = iconCSSStyle;
    this.displayOrder = displayOrder;
    this.isSystemLabel = isSystemLabel;
  }

  public LabelImpl(final Label src) {
    super(src.getName());
    ownerName = src.getOwnerName();
    editableCSSStyleName = src.getEditableCSSStyleName();
    readonlyCSSStyleName = src.getReadonlyCSSStyleName();
    previewCSSStyleName = src.getPreviewCSSStyleName();
    isVisible = src.isVisible();
    hasToBeDisplayed = src.isHasToBeDisplayed();
    iconCSSStyle = src.getIconCSSStyle();
    displayOrder = src.getDisplayOrder();
    isSystemLabel = src.isSystemLabel();
  }

  @Override
  public String toString() {
    final StringBuffer sb = new StringBuffer();
    sb.append(this.getClass().getSimpleName());
    sb.append(", name= " + getName());
    sb.append(", owner= " + getOwnerName());
    return sb.toString();
  }

  @Override
  public String getOwnerName() {
    return ownerName;
  }

  public void setOwnerName(final String aOwnerName) {
    ownerName = aOwnerName;
  }

  @Override
  public String getEditableCSSStyleName() {
    return editableCSSStyleName;
  }

  public void setEditableCSSStyleName(final String aEditableCSSStyleName) {
    editableCSSStyleName = aEditableCSSStyleName;
  }

  @Override
  public int getDisplayOrder() {
    return displayOrder;
  }

  public void setDisplayOrder(final int aDisplayOrder) {
    displayOrder = aDisplayOrder;
  }

  @Override
  public boolean isVisible() {
    return isVisible;
  }

  public void setVisible(final boolean aIsVisible) {
    isVisible = aIsVisible;
  }

  @Override
  public boolean isHasToBeDisplayed() {
    return hasToBeDisplayed;
  }

  public void setHasToBeDisplayed(final boolean aHasToBeDisplayed) {
    hasToBeDisplayed = aHasToBeDisplayed;
  }

  @Override
  public boolean isSystemLabel() {
    return isSystemLabel;
  }

  public void setSystemLabel(final boolean aIsSystemLabel) {
    isSystemLabel = aIsSystemLabel;
  }

  @Override
  public boolean equals(final Object anObject) {
    if (anObject == null) {
      return false;
    }
    if (!(anObject instanceof LabelImpl)) {
      return false;
    }
    final LabelImpl other = (LabelImpl) anObject;
    return super.equals(other) && getOwnerName().equals(other.getOwnerName());
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + (ownerName == null ? 0 : ownerName.hashCode());
    return result;
  }

  @Override
  public int compareTo(final Category o) {
    if (o instanceof Label) {
      final Label other = (Label) o;
      final int thisVal = displayOrder;
      final int anotherVal = other.getDisplayOrder();
      return thisVal < anotherVal ? -1 : thisVal == anotherVal ? 0 : 1;
    } else {
      return super.compareTo(o);
    }
  }

}
