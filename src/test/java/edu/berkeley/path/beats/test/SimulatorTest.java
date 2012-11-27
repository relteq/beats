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
 
package edu.berkeley.path.beats.test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;

import org.junit.Test;

import edu.berkeley.path.beats.simulator.SiriusFormatter;
import edu.berkeley.path.beats.simulator.SiriusMath;

public class SimulatorTest {

    private String fixture_folder = "data/test/fixture/";
    private String output_folder = "data/test/output/";
    private String config_folder = "data/test/config/";
    
	private static String [] config_names = {
											 "_scenario_2009_02_12",
											 "Albany & Berkeley_sirius",
										 	 "_smalltest_multipletypes",
											 "complete",
											 "test_event",
		                                     "_scenario_constantsplits" };
		
	private static String CONF_SUFFIX = ".xml";
	private static String [] outfile = {"_density_0.txt" , 
								  	    "_inflow_0.txt" , 
									    "_outflow_0.txt" , 
									    "_time_0.txt"};
	
	@Test
	public void testSimulator() {
		
		for(String config_name : config_names ){

			String [] args = {config_folder+config_name+CONF_SUFFIX, 
					output_folder+config_name,
					String.format("%d", 0), 
					String.format("%d", 3600), 
					String.format("%d", 300),
					String.format("%d", 1) };
			
			System.out.println("Running " + config_name);
			edu.berkeley.path.beats.simulator.Runner.main(args);

			for(String str : outfile){
				String filename = config_name + str;
				try {
					System.out.println("Checking " + filename);
					System.out.println("Reading " + fixture_folder+filename);
					ArrayList<ArrayList<ArrayList<Double>>> A = SiriusFormatter.readCSV(fixture_folder+filename,"\t",":");
					System.out.println("Reading " + output_folder+filename);
					ArrayList<ArrayList<ArrayList<Double>>> B = SiriusFormatter.readCSV(output_folder+filename,"\t",":");
					assertTrue("The files are not equal.",SiriusMath.equals3D(A,B));
				} catch (IOException e) {
					System.out.println(e.getMessage());
					fail("problem reading a file");
				}
			}
		}
	}

}
