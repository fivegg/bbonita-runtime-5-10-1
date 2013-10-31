package org.ow2.bonita.runtime.event;

public class Master {

	protected long dbid;
	protected long heartbeat;
	protected String node;

	public Master() {
		// TODO Auto-generated constructor stub
	}

	
	public Master(long heartbeat, String node) {
		super();
		this.heartbeat = heartbeat;
		this.node = node;
	}


	public long getDbid() {
		return dbid;
	}

	public void setDbid(long dbid) {
		this.dbid = dbid;
	}

	public long getHeartbeat() {
		return heartbeat;
	}

	public void setHeartbeat(long heartbeat) {
		this.heartbeat = heartbeat;
	}

	public String getNode() {
		return node;
	}

	public void setNode(String node) {
		this.node = node;
	}

	
}
