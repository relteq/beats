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

import java.sql.Connection;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.torque.TorqueException;
import org.apache.torque.util.Criteria;

import com.workingdogs.village.DataSetException;

@SuppressWarnings("serial")
public class BasePeer extends org.apache.torque.util.BasePeer {

	/**
	 * Returns a maximum column value
	 * @param colname column name
	 * @return null if the table is empty
	 * @throws TorqueException
	 * @throws DataSetException
	 */
	public static com.workingdogs.village.Value maxColumnValue(String colname, Criteria crit, Connection conn) throws TorqueException, DataSetException {
		if (null == crit) crit = new Criteria();
		crit.addSelectColumn("COUNT(" + colname + ")");
		crit.addSelectColumn("MAX(" + colname + ")");
		@SuppressWarnings("unchecked")
		List<com.workingdogs.village.Record> record_l = null == conn ? doSelect(crit) : doSelect(crit, conn);
		com.workingdogs.village.Record record = record_l.get(0);
		int count = record.getValue(1).asInt();
		return 0 == count ? null : record.getValue(2);
	}

	private static Logger logger = Logger.getLogger(BasePeer.class);

	/**
	 * Generates an ID
	 * @param colname column name (table.column)
	 * @param conn DB connection on NULL for autocommit
	 * @return a new ID or NULL if an error occurred
	 * @throws TorqueException
	 */
	public static Long nextId(String colname, Connection conn) throws TorqueException {
		try {
			com.workingdogs.village.Value maxval = maxColumnValue(colname, null, conn);
			return Long.valueOf(null == maxval ? 0 : maxval.asLong() + 1);
		} catch (DataSetException exc) {
			logger.error(exc.getMessage(), exc);
			return null;
		}
	}

}
