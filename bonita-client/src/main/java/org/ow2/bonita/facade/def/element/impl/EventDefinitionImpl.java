/**
 * Copyright (C) 2012 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.ow2.bonita.facade.def.element.impl;

import org.ow2.bonita.facade.def.element.EventDefinition;

/**
 * 
 * @author Matthieu Chaffotte
 *
 */
public abstract class EventDefinitionImpl implements EventDefinition {

  private static final long serialVersionUID = 7866644260107462397L;

  private String name;

  private String correlationKeyName1;

  private String correlationKeyExpression1;
  
  private String correlationKeyName2;
  
  private String correlationKeyExpression2;
  
  private String correlationKeyName3;
  
  private String correlationKeyExpression3;
  
  private String correlationKeyName4;
  
  private String correlationKeyExpression4;
  
  private String correlationKeyName5;
  
  private String correlationKeyExpression5;

  protected EventDefinitionImpl() {
    super();
  }

  public EventDefinitionImpl(String name) {
    super();
    this.name = name;
  }

  public EventDefinitionImpl(EventDefinition event) {
    super();
    this.name = event.getName();
    this.correlationKeyName1 = event.getCorrelationKeyName1();
    this.correlationKeyExpression1 = event.getCorrelationKeyExpression1();
    this.correlationKeyName2 = event.getCorrelationKeyName2();
    this.correlationKeyExpression2 = event.getCorrelationKeyExpression2();
    this.correlationKeyName3 = event.getCorrelationKeyName3();
    this.correlationKeyExpression3 = event.getCorrelationKeyExpression3();
    this.correlationKeyName4 = event.getCorrelationKeyName4();
    this.correlationKeyExpression4 = event.getCorrelationKeyExpression4();
    this.correlationKeyName5 = event.getCorrelationKeyName5();
    this.correlationKeyExpression5 = event.getCorrelationKeyExpression5();
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getCorrelationKeyName1() {
    return correlationKeyName1;
  }

  @Override
  public String getCorrelationKeyExpression1() {
    return correlationKeyExpression1;
  }

  @Override
  public String getCorrelationKeyName2() {
    return correlationKeyName2;
  }

  @Override
  public String getCorrelationKeyExpression2() {
    return correlationKeyExpression2;
  }

  @Override
  public String getCorrelationKeyName3() {
    return correlationKeyName3;
  }

  @Override
  public String getCorrelationKeyExpression3() {
    return correlationKeyExpression3;
  }

  @Override
  public String getCorrelationKeyName4() {
    return correlationKeyName4;
  }

  @Override
  public String getCorrelationKeyExpression4() {
    return correlationKeyExpression4;
  }

  @Override
  public String getCorrelationKeyName5() {
    return correlationKeyName5;
  }

  @Override
  public String getCorrelationKeyExpression5() {
    return correlationKeyExpression5;
  }

  public void setCorrelationKey1(final String name, final String expression) {
    this.correlationKeyName1 = name;
    this.correlationKeyExpression1 = expression;
  }

  public void setCorrelationKey2(final String name, final String expression) {
    this.correlationKeyName2 = name;
    this.correlationKeyExpression2 = expression;
  }

  public void setCorrelationKey3(final String name, final String expression) {
    this.correlationKeyName3 = name;
    this.correlationKeyExpression3 = expression;
  }

  public void setCorrelationKey4(final String name, final String expression) {
    this.correlationKeyName4 = name;
    this.correlationKeyExpression4 = expression;
  }

  public void setCorrelationKey5(final String name, final String expression) {
    this.correlationKeyName5 = name;
    this.correlationKeyExpression5 = expression;
  }

}
