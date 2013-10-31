/**
 * Copyright (C) 2007  Bull S. A. S.
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
package org.ow2.bonita.definition.activity;

import org.ow2.bonita.facade.exception.BonitaWrapperException;
import org.ow2.bonita.facade.exception.ExpressionEvaluationException;
import org.ow2.bonita.facade.runtime.ActivityInstance;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.runtime.model.Execution;
import org.ow2.bonita.services.Querier;
import org.ow2.bonita.util.EnvTool;
import org.ow2.bonita.util.GroovyException;
import org.ow2.bonita.util.GroovyExpression;
import org.ow2.bonita.util.GroovyUtil;

/**
 * @author Marc Blachon, Guillaume Porcher, Charles Souillard, Miguel Valdes,
 *         Pierre Vigneras
 */
public class ConditionEvaluator {

  public static boolean evaluate(final String value, final Execution internalExecution) {
    try {
      return evaluateGroovy(value, internalExecution);
    } catch (final Exception e) {
      throw new BonitaWrapperException(new ExpressionEvaluationException(e));
    }
  }

  private static boolean evaluateGroovy(final String value, final Execution execution) throws GroovyException {
    final String groovyExpression = GroovyExpression.START_DELIMITER + value + GroovyExpression.END_DELIMITER;
    final ProcessInstanceUUID instanceUUID = execution.getInstance().getUUID();
    final Querier journal = EnvTool.getJournalQueriers();
    final ActivityInstance instance = execution.getActivityInstance();
    String loopId = "noLoop";
    if (instance != null) {
      loopId = instance.getLoopId();
    }

    final ActivityInstance activityInstance = journal.getActivityInstance(instanceUUID, execution.getNodeName(),
        execution.getIterationId(), execution.getActivityInstanceId(), loopId);

    Object result = null;
    if (activityInstance != null) {
      result = GroovyUtil.evaluate(groovyExpression, null, activityInstance.getUUID(), false, false);
    } else {
      result = GroovyUtil.evaluate(groovyExpression, null, instanceUUID, false, false);
    }
    return ((Boolean) result).booleanValue();
  }

}
