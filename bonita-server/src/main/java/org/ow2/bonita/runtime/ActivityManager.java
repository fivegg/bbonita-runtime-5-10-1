/**
 * Copyright (C) 2011  BonitaSoft S.A.
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
package org.ow2.bonita.runtime;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ow2.bonita.definition.activity.AbstractActivity;
import org.ow2.bonita.facade.APIAccessor;
import org.ow2.bonita.facade.def.InternalActivityDefinition;
import org.ow2.bonita.facade.exception.ActivityNotFoundException;
import org.ow2.bonita.facade.exception.IllegalTaskStateException;
import org.ow2.bonita.facade.impl.StandardAPIAccessorImpl;
import org.ow2.bonita.facade.runtime.ActivityInstance;
import org.ow2.bonita.facade.runtime.ActivityState;
import org.ow2.bonita.facade.runtime.impl.InternalActivityInstance;
import org.ow2.bonita.facade.uuid.ActivityInstanceUUID;
import org.ow2.bonita.runtime.model.Execution;
import org.ow2.bonita.services.Recorder;
import org.ow2.bonita.util.EnvTool;
import org.ow2.bonita.util.ExceptionManager;
import org.ow2.bonita.util.Misc;
import org.ow2.bonita.util.TransientData;

/**
 * @author Elias Ricken de Medeiros
 *
 */
public class ActivityManager {
  
  private static final Logger LOG = Logger.getLogger(ActivityManager.class.getName());
  
  protected static ActivityInstance getActivity(final ActivityInstanceUUID activityUUID) throws ActivityNotFoundException {
    final InternalActivityInstance activityInstance = EnvTool.getJournalQueriers().getActivityInstance(activityUUID);
    if (activityInstance == null) {
      throw new ActivityNotFoundException("bai_RAPII_19", activityUUID);
    }
    return activityInstance;
  }
  
  protected static Execution getExecution(final ActivityInstance activityInstance) throws ActivityNotFoundException {
    return EnvTool.getJournalQueriers().getExecutionOnActivity(activityInstance.getProcessInstanceUUID(), activityInstance.getUUID());
  }
  
  
  
  public static void skip(final ActivityInstanceUUID activityInstanceUUID, Map<String, Object> variablesToUpdate) throws ActivityNotFoundException, IllegalTaskStateException {
    final ActivityInstance activityInstance = getActivity(activityInstanceUUID);
    final Execution internalExecution = getExecution(activityInstance);
    skip(internalExecution, activityInstance, variablesToUpdate);
  }
  
  protected static void skip(final Execution internalExecution, final ActivityInstance activityInstance, Map<String, Object> variablesToUpdate) throws IllegalTaskStateException {
    if (LOG.isLoggable(Level.FINE)) {
      LOG.fine("Checking compatible state of " + activityInstance);
    }
    final ActivityInstanceUUID activityInstanceUUID = activityInstance.getUUID();
    final ActivityState state = activityInstance.getState();
    if (!state.equals(ActivityState.READY) && !state.equals(ActivityState.FAILED)) {
      final Set<ActivityState> expectedStates = new HashSet<ActivityState>();
      expectedStates.add(ActivityState.READY);
      expectedStates.add(ActivityState.FAILED);
      
      String message = ExceptionManager.getInstance().getFullMessage("bai_RAPII_13");
      throw new IllegalTaskStateException("bai_RAPII_13", message,
          activityInstanceUUID, expectedStates, state);
    }
    if (variablesToUpdate != null && !variablesToUpdate.isEmpty()) {
      try {
        for (Entry<String, Object> entry : variablesToUpdate.entrySet()) {
          APIAccessor accessor = new StandardAPIAccessorImpl();
          accessor.getRuntimeAPI().setVariable(activityInstanceUUID, entry.getKey(), entry.getValue());
        }
      } catch (Exception e) {
        if (LOG.isLoggable(Level.SEVERE)) {
          LOG.severe("Error while setting the variable on skip task : "
              + activityInstance.getActivityName() + ". Exception: "
              + Misc.getStackTraceFrom(e));
        }
      }
    }

    final String currentUserId = EnvTool.getUserId();
    String activityName = activityInstance.getActivityName();
    if (LOG.isLoggable(Level.FINE)) {
      LOG.fine("Skiping activity : " + activityInstanceUUID + " on activity " + activityName);
    }

    final Recorder recorder = EnvTool.getRecorder();
    recorder.recordActivitySkipped(activityInstance, currentUserId);
    TransientData.removeTransientData(activityInstanceUUID);

    Set<InternalActivityInstance> activities = EnvTool.getJournalQueriers().getActivityInstances(activityInstance.getProcessInstanceUUID(), activityName);
    // in case of a multi-instantiation
    if (activities.size() > 1) {
      for (InternalActivityInstance currentActivity : activities) {         
        if ((ActivityState.READY.equals(currentActivity.getState()) || ActivityState.FAILED.equals(currentActivity.getState()))
            && !activityInstanceUUID.equals(currentActivity.getUUID())) {
          recorder.recordActivitySkipped(currentActivity, currentUserId);
          TransientData.removeTransientData(activityInstanceUUID);
        }
      }       
    }
    
    InternalActivityDefinition activityDef = internalExecution.getNode();       
    AbstractActivity abstractActivity = (AbstractActivity) activityDef.getBehaviour();
    
    if (!ActivityState.ABORTED.equals(internalExecution.getActivityInstance().getState())) {
      abstractActivity.signal(internalExecution, AbstractActivity.BODY_SKIPPED, null);
      if (LOG.isLoggable(Level.FINE)) {
        LOG.fine("Skipped: " + activityInstance);
      }
    }
  }

}
