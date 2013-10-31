/**
 * Copyright (C) 2010-2012 BonitaSoft S.A.
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

import org.ow2.bonita.facade.def.element.CatchMessageEvent;
import org.ow2.bonita.facade.def.majorElement.TransitionDefinition;
import org.ow2.bonita.facade.uuid.ActivityDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;

/**
 * 
 * @author Matthieu Chaffotte
 * 
 */
public class MessageBoundaryEventImpl extends BoundaryEventImpl implements CatchMessageEvent {

  private static final long serialVersionUID = -5187827646315564051L;

  protected String expression;

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

  protected MessageBoundaryEventImpl() {
    super();
  }

  public MessageBoundaryEventImpl(final String eventName, final ProcessDefinitionUUID processUUID,
      final ActivityDefinitionUUID activityUUID, final TransitionDefinition exceptionTransition, final String expression) {
    super(eventName, processUUID, activityUUID, exceptionTransition);
    this.expression = expression;
  }

  public MessageBoundaryEventImpl(final MessageBoundaryEventImpl src) {
    super(src);
    this.expression = src.getExpression();
    this.correlationKeyName1 = src.getCorrelationKeyName1();
    this.correlationKeyExpression1 = src.getCorrelationKeyExpression1();
    this.correlationKeyName2 = src.getCorrelationKeyName2();
    this.correlationKeyExpression2 = src.getCorrelationKeyExpression2();
    this.correlationKeyName3 = src.getCorrelationKeyName3();
    this.correlationKeyExpression3 = src.getCorrelationKeyExpression3();
    this.correlationKeyName4 = src.getCorrelationKeyName4();
    this.correlationKeyExpression4 = src.getCorrelationKeyExpression4();
    this.correlationKeyName5 = src.getCorrelationKeyName5();
    this.correlationKeyExpression5 = src.getCorrelationKeyExpression5();
  }

  @Override
  public String getExpression() {
    return expression;
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
