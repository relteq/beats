/**
 * Copyright (c) 2012, Regents of the University of California
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 *   Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 *   Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 **/

package edu.berkeley.path.beats.db;

import java.util.Calendar;
import java.util.Date;

import org.apache.torque.TorqueException;

@SuppressWarnings("serial")
public abstract class BaseObject extends org.apache.torque.om.BaseObject {
	public void setCreated(Date date) {}
	public void setModified(Date date) {}
	public void setCreatedBy(String user_name) {}
	public void setModifiedBy(String user_name) {}
	public void setModstamp(Date v) {}

	public Date getCreated() { return null; }

	private final static String default_user = "admin";

	private void create(String user_name, Date date) {
		setCreated(date);
		setCreatedBy(user_name);
		modify(user_name, date);
	}

	private void modify(String user_id, Date date) {
		setModified(date);
		setModifiedBy(user_id);
		setModstamp(date);
	}

	private void createNow() {
		create(default_user, Calendar.getInstance().getTime());
	}

	private void modifyNow() {
		modify(default_user, Calendar.getInstance().getTime());
	}

	public void setNew(boolean is_new) {
		boolean was_new = isNew();
		super.setNew(is_new);
		if (is_new && !was_new) createNow();
	}

	public void setModified(boolean is_modified) {
		boolean was_modified = isModified();
		super.setModified(is_modified);
		if (is_modified) {
			if (isNew()) {
				if (null == getCreated()) createNow();
			} else if (!was_modified) modifyNow();
		}
	}

	public Long getId() { return null; }

	/**
	 * Retrieves an element type which is a transformed table name
	 * @return String element type
	 * @throws TorqueException
	 */
	public String getElementType() throws TorqueException {
		String name = getTableMap().getName();
		if (name.endsWith("s")) name = name.substring(0, name.length() - 1);
		return name.replace('_', ' ');
	}

}
