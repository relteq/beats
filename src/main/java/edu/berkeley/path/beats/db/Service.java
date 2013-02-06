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

import org.apache.log4j.Logger;
import org.apache.torque.Torque;
import org.apache.torque.TorqueException;

import edu.berkeley.path.beats.simulator.BeatsException;

/**
 * DB service initialization and shutdown
 */
public class Service {
	/**
	 * Initializes the DB service.
	 * Connection parameters are read from the environment,
	 * as described in the "Concept of Operations"
	 * @throws BeatsException
	 */
	public static void init() throws BeatsException {
		init(Parameters.fromEnvironment());
	}

	private static Logger logger = Logger.getLogger(Service.class);

	/**
	 * Initializes the DB service for the specified parameters
	 * @param params
	 * @throws BeatsException
	 */
	public static void init(Parameters params) throws BeatsException {
		try {
			logger.info("Connection URL: " + params.getUrl());
			Torque.init(params.toConfiguration());
		} catch (TorqueException exc) {
			throw new BeatsException(exc);
		}
	}

	/**
	 * @return true if the DB service is already initialized
	 */
	public static boolean isInit() {
		return Torque.isInit();
	}

	/**
	 * Initializes the DB service if it hasn't been initialized yet
	 * @throws BeatsException
	 */
	public static void ensureInit() throws BeatsException {
		if (!isInit()) init();
	}

	/**
	 * Shuts down the DB service
	 * @throws BeatsException
	 */
	public static void shutdown() {
		try {
			Torque.shutdown();
		} catch (TorqueException exc) {
			logger.error("Database shutdown failed", exc);
		}
	}
}
