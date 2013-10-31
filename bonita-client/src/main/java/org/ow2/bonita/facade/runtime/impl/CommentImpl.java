/**
 * Copyright (C) 2009  BonitaSoft S.A.
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
package org.ow2.bonita.facade.runtime.impl;

import java.util.Date;

import org.ow2.bonita.facade.runtime.Comment;
import org.ow2.bonita.facade.uuid.ActivityInstanceUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.util.Misc;

/**
 * 
 * @author Matthieu Chaffotte
 *
 */
public class CommentImpl implements Comment {

  private static final long serialVersionUID = -2528309336083325621L;
  protected long dbid;
  protected String userId;
  protected long date;
  protected String message;
  protected ActivityInstanceUUID activityUUID;
  protected ProcessInstanceUUID instanceUUID;

  protected CommentImpl() {
    super();
  }

  public CommentImpl(String userId, String message, ProcessInstanceUUID instanceUUID) {
    this.userId = userId;
    this.message = message;
    this.instanceUUID = instanceUUID;
    date = System.currentTimeMillis();
  }

  public CommentImpl(String userId, String message, ActivityInstanceUUID activityUUID, ProcessInstanceUUID instanceUUID) {
    this.userId = userId;
    this.message = message;
    this.instanceUUID = instanceUUID;
    this.activityUUID = activityUUID;
    date = System.currentTimeMillis();
  }

  public CommentImpl(final Comment comment) {
    this.userId = comment.getUserId();
    this.message = comment.getMessage();
    this.date = Misc.getTime(comment.getDate());
    this.activityUUID = comment.getActivityUUID();
    this.instanceUUID = comment.getInstanceUUID();
  }

  public String getUserId() {
    return userId;
  }

  public Date getDate() {
    return Misc.getDate(date);
  }

  public String getMessage() {
    return message;
  }

  public ActivityInstanceUUID getActivityUUID() {
    return activityUUID;
  }

  public ProcessInstanceUUID getInstanceUUID() {
    return instanceUUID;
  }

  public void setInstanceUUID(ProcessInstanceUUID instanceUUID) {
    this.instanceUUID = instanceUUID;
  }

}