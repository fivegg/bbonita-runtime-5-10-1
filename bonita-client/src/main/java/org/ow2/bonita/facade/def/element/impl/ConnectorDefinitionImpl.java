/**
 * Copyright (C) 2006  Bull S. A. S.
 * Bull, Rue Jean Jaures, B.P.68, 78340, Les Clayes-sous-Bois
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA  02110-1301, USA.
 * 
 * Modified by Matthieu Chaffotte - BonitaSoft S.A.
 **/
package org.ow2.bonita.facade.def.element.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.ow2.bonita.facade.def.element.ConnectorDefinition;
import org.ow2.bonita.facade.def.element.DeadlineDefinition;
import org.ow2.bonita.facade.def.element.FilterDefinition;
import org.ow2.bonita.facade.def.element.HookDefinition;
import org.ow2.bonita.facade.def.element.MultiInstantiationDefinition;
import org.ow2.bonita.facade.def.element.MultipleActivitiesInstantiatorDefinition;
import org.ow2.bonita.facade.def.element.RoleMapperDefinition;
import org.ow2.bonita.facade.def.majorElement.impl.DescriptionElementImpl;
import org.ow2.bonita.util.ExceptionManager;
import org.ow2.bonita.util.Misc;

public class ConnectorDefinitionImpl extends DescriptionElementImpl implements ConnectorDefinition, DeadlineDefinition,
    FilterDefinition, MultiInstantiationDefinition, RoleMapperDefinition, HookDefinition,
    MultipleActivitiesInstantiatorDefinition {

  private static final long serialVersionUID = 4943554886602562216L;
  protected String className;
  protected Map<String, Object[]> clientParameters;
  protected String condition;
  protected String variableName;
  protected Event event;
  protected boolean throwingException = true;
  protected String errorCode;

  protected ConnectorDefinitionImpl() {
    super();
  }

  public ConnectorDefinitionImpl(final String className) {
    super();
    this.className = className;
  }

  public ConnectorDefinitionImpl(final ConnectorDefinition src) {
    super(src);
    final String message = ExceptionManager.getInstance().getFullMessage("baoi_RMDI_1");
    Misc.badStateIfNull(src, message);
    className = src.getClassName();
    clientParameters = src.getParameters();

    final ConnectorDefinitionImpl srcImpl = (ConnectorDefinitionImpl) src;
    condition = srcImpl.condition;
    variableName = srcImpl.variableName;
    event = srcImpl.event;
    throwingException = srcImpl.throwingException;
    errorCode = srcImpl.errorCode;
  }

  public void setCondition(final String condition) {
    this.condition = condition;
  }

  public void setVariableName(final String variableName) {
    this.variableName = variableName;
  }

  public void setEvent(final Event event) {
    this.event = event;
  }

  public void setThrowingException(final boolean throwingException) {
    this.throwingException = throwingException;
  }

  @Override
  public String getClassName() {
    return className;
  }

  @Override
  public Map<String, Object[]> getParameters() {
    if (getClientParameters() != null) {
      return getClientParameters();
    }
    return Collections.emptyMap();
  }

  public void addParameter(final String key, final Object... value) {
    if (clientParameters == null) {
      clientParameters = new HashMap<String, Object[]>();
    }
    clientParameters.put(key, value);
  }

  public void addParameters(final Map<String, Object[]> parameters) {
    if (clientParameters == null) {
      clientParameters = new HashMap<String, Object[]>();
    }
    clientParameters.putAll(parameters);
  }

  @Override
  public String getCondition() {
    return condition;
  }

  @Override
  public String getVariableName() {
    return variableName;
  }

  @Override
  public Event getEvent() {
    return event;
  }

  @Override
  public boolean isThrowingException() {
    return throwingException;
  }

  public Map<String, Object[]> getClientParameters() {
    return clientParameters;
  }

  @Override
  public String getErrorCode() {
    return errorCode;
  }

  public void setErrorCode(final String errorCode) {
    this.errorCode = errorCode;
  }

}
