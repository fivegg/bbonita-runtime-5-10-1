/**
 * Copyright (C) 2013 BonitaSoft S.A.
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
package org.ow2.bonita.runtime.event;

/**
 * @author Charles Souillard
 * @author Nicolas Chabanoles
 *
 */
public class Node {

	protected long dbid;
	protected long heartbeat;
	protected String node;
	protected String status;
	protected String option;

	public static String STATUS_ALIVE = "alive";
	public static String STATUS_DEAD = "dead";
	
	public Node() {
		// TODO Auto-generated constructor stub
	}

	
	public Node(final long heartbeat, final String node, final String status) {
		super();
		this.heartbeat = heartbeat;
		this.node = node;
		this.status = status;
	}


	public long getDbid() {
		return dbid;
	}

	public void setDbid(final long dbid) {
		this.dbid = dbid;
	}

	public long getHeartbeat() {
		return heartbeat;
	}

	public void setHeartbeat(final long heartbeat) {
		this.heartbeat = heartbeat;
	}

	public String getNode() {
		return node;
	}

	public void setNode(final String node) {
		this.node = node;
	}
	
	public String getStatus() {
		return status;
	}
	
	public void setStatus(final String status) {
		this.status = status;
	}

	
}
