package edu.berkeley.path.beats.db;

import java.sql.Connection;

import org.apache.torque.TorqueException;

@SuppressWarnings("serial")
public abstract class BaseTypes extends BaseObject {
	public abstract void setName(String value);
	public abstract void setDescription(String value);
	public void setInUse(Boolean value) {}

	public abstract void save(Connection conn) throws TorqueException;
}
