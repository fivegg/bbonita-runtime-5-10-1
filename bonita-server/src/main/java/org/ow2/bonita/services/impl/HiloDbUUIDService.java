/**
 * Copyright (C) 2009-2012 BonitaSoft S.A.
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
package org.ow2.bonita.services.impl;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;

import org.ow2.bonita.env.Environment;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.services.Journal;
import org.ow2.bonita.services.UUIDService;
import org.ow2.bonita.util.Command;
import org.ow2.bonita.util.EnvTool;
import org.ow2.bonita.util.ProcessUtil;

/**
 * @author Charles Souillard, Guillaume Holler
 */
public class HiloDbUUIDService implements UUIDService {

  static final Logger LOG = Logger.getLogger(ProcessUtil.class.getName());

  private final int increment;

  // sequences store. We use a high concurrency structure that doesn't need external synchronization
  private final ConcurrentMap<ProcessDefinitionUUID, Sequence> sequences = new ConcurrentHashMap<ProcessDefinitionUUID, Sequence>();

  public HiloDbUUIDService() {
    this(1);
  }

  public HiloDbUUIDService(final int increment) {
    this.increment = increment;
  }

  private String getMetadataName(final ProcessDefinitionUUID processUUID) {
    return "*****" + processUUID + "*****instance-nb*****";
  }

  @Override
  public long getNewProcessInstanceNb(final ProcessDefinitionUUID processUUID) {
    final String metaName = getMetadataName(processUUID);
    // look for sequence object
    final Sequence sequence = getSequence(processUUID);

    long processInstanceNb;
    // noinspection SynchronizationOnLocalVariableOrMethodParameter
    synchronized (sequence) {
      if (sequence.shouldReset()) {
        final long nextHi = EnvTool.getCommandService().execute(new Command<Long>() {
          private static final long serialVersionUID = 1L;

          @Override
          public Long execute(final Environment environment) throws Exception {
            final Journal journal = EnvTool.getJournal();

            journal.lockMetadata(metaName);

            long nb = journal.getLockedMetadata(metaName);
            nb += increment;
            journal.updateLockedMetadata(metaName, nb);

            return nb;
          }
        });
        sequence.reset(nextHi, increment);
      }
      processInstanceNb = sequence.inc();
    }
    return processInstanceNb;
  }

  @Override
  public void archiveOrDeleteProcess(final ProcessDefinitionUUID processUUID) {
    final String metaName = getMetadataName(processUUID);
    // look for sequence object
    final Sequence sequence = getSequence(processUUID);
    synchronized (sequence) {
      EnvTool.getJournal().removeLockedMetadata(metaName);
    }
  }

  private Sequence getSequence(final ProcessDefinitionUUID processUUID) {
    Sequence sequence = sequences.get(processUUID);
    if (sequence == null) {
      sequence = new Sequence();
      final Sequence old = sequences.putIfAbsent(processUUID, sequence);
      if (old != null) {
        // ok, some other thread already put one: just use it
        sequence = old;
      }
    }

    return sequence;
  }

  private static final class Sequence {
    private long hi = -1;
    private long low = -1;

    private Sequence() {
    }

    boolean shouldReset() {
      return low < 0 || low >= hi;
    }

    void reset(final long nextHi, final long increment) {
      assert nextHi >= 0 : "nextHi is negative: " + nextHi;
      assert increment > 0 : "increment is negative or null";

      hi = nextHi;
      low = nextHi - increment;
    }

    public long inc() {
      assert !shouldReset() : "inc() should not be called in a context where reset is needed";
      low++;
      return low;
    }
  }

}
