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
package org.ow2.bonita.activity.instantiation.instantiator;

import java.util.ArrayList;
import java.util.List;

import org.ow2.bonita.definition.MultiInstantiator;
import org.ow2.bonita.definition.MultiInstantiatorDescriptor;
import org.ow2.bonita.facade.QueryAPIAccessor;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;


/**
 * @author Guillaume Porcher
 *
 */
public class TestOneJoinActivityInstantiator implements MultiInstantiator {

  public MultiInstantiatorDescriptor execute(
      QueryAPIAccessor accessor, 
      ProcessInstanceUUID instanceUUID, 
      String activityId, 
      String iterationId) throws Exception {
    List<Object> vals = new ArrayList<Object>();
    vals.add("val1");
    vals.add("val2");
    return new MultiInstantiatorDescriptor(1, vals);
  }

}
