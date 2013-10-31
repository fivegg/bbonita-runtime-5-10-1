/**
 * Copyright (C) 2012 BonitaSoft S.A.
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
package org.ow2.bonita.runtime.event;

import java.util.Set;

/**
 * 
 * @author Charles Souillard, Matthieu Chaffotte, Elias Ricken de Medeiros
 * 
 */
public class ExpressionMessageEventMatcher extends SlicedEventExecutorThread {

  ExpressionMessageEventMatcher(final EventExecutor executor, final String name) {
    super(executor, name);
  }

  @Override
  protected void activate() {
    // nothing to do
  }

  @Override
  protected void execute() {
    final Set<EventCoupleId> coupleIds = getCommandService().execute(new GetExpressionMessageEventCouples());
    storeJobs(coupleIds);
  }

  @Override
  protected String getJobExecutorName() {
    return "Expression message event matcher";
  }

  @Override
  protected Long getNextDueDate() {
    return getCommandService().execute(new GetNextExpressionEventDueDate());
  }

}
