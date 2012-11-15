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

package edu.berkeley.path.beats.util;

import java.math.BigDecimal;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

public class Data1D {
	private BigDecimal[] data = null;

	private static Logger logger = Logger.getLogger(Data1D.class);

	/**
	 * @param str a serialized vector
	 * @param delim a delimiter
	 */
	public Data1D(String str, String delim) {
		if (null == str) logger.error("str == null");
		else if (null == delim) logger.error("delim == null");
		else {
			StringTokenizer st = new StringTokenizer(str, delim);
			data = new BigDecimal[st.countTokens()];
			for (int i = 0; st.hasMoreTokens(); ++i)
				data[i] = new BigDecimal(st.nextToken());
		}
	}

	public boolean isEmpty() {
		return null == data;
	}

	public BigDecimal[] getData() {
		return data;
	}
}
