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
package org.ow2.bonita.util;

import java.util.BitSet;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import junit.framework.TestCase;

/**
 * @author Pierre Vigneras
 */
public class ChainTest extends TestCase {

  private BitSet bitSet = new BitSet();
  private Op chain; 
  private int nb;

  private interface Op {
    void call();
  }

  private class Impl implements Op {
    private final int rank;

    Impl(final int rank) {
      this.rank = rank;
    }

    public void call() {
      bitSet.set(rank);
    }
  }

  public void setUp() {
    final List<Op> list = new LinkedList<Op>();
    nb = Misc.random(1, 100);
    list.add(Misc.getLoggerProxyFor(new Impl(0), 
        Logger.getLogger(ChainTest.class.getName())));
    for (int i = 1; i < nb; i++) {
      list.add(new Impl(i));
    }
    chain = Misc.getChainOf(list);
  }

  public void testVoidMethod() {
    chain.call();
    assertEquals(nb, bitSet.cardinality());
  }

  public void testObjectMethod() {
    assertNotNull(chain.toString());
    assertTrue(chain.equals(chain));
    assertTrue(chain.hashCode() != 0);
  }
}
