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
 **/
package org.ow2.bonita.services.impl;

import java.util.ArrayList;
import java.util.List;

import org.ow2.bonita.services.Archiver;
import org.ow2.bonita.services.Querier;
import org.ow2.bonita.services.Recorder;
import org.ow2.bonita.util.BonitaRuntimeException;
import org.ow2.bonita.util.ExceptionManager;

/**
 * @author Guillaume Porcher
 *
 * Access to Chains of Queriers.
 */
public class QuerierListAccessor {

  private Querier allQueriers;
  private Querier journals;
  private Querier histories;
  
  public QuerierListAccessor(List<Querier> queriers) {
    List<Querier> journalsList = new ArrayList<Querier>();
    List<Querier> historiesList = new ArrayList<Querier>();
    for (Querier q :  queriers) {
      if (q instanceof Recorder) {
        journalsList.add(q);
      } else if (q instanceof Archiver) {
        historiesList.add(q);
      } else {
      	String message = ExceptionManager.getInstance().getMessage(
      			"bs_QLA_1", q, Recorder.class.getName(), Archiver.class.getName());
        throw new BonitaRuntimeException(message);
      }
    }
    this.allQueriers = new QuerierChainer(queriers);
    this.journals = new QuerierChainer(journalsList);
    this.histories = new QuerierChainer(historiesList);
  }
 
  public Querier getAllQueriers() {
    return allQueriers;
  }
  public Querier getJournals() {
    return journals;
  }
  public Querier getHistories() {
    return histories;
  }

}
