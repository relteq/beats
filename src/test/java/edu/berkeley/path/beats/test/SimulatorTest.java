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

import java.io.File;
import java.util.ArrayList;
import java.util.Vector;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import edu.berkeley.path.beats.simulator.Defaults;
import edu.berkeley.path.beats.simulator.ObjectFactory;
import edu.berkeley.path.beats.simulator.Scenario;
import edu.berkeley.path.beats.simulator.BeatsException;
import edu.berkeley.path.beats.simulator.BeatsFormatter;
import edu.berkeley.path.beats.simulator.BeatsMath;

@RunWith(Parameterized.class)
public class SimulatorTest {

	/** scenario file */
	private File conffile;
	
    private String fixture_folder = "data/test/fixture/";
    private String output_folder = "data/test/output/";
    	
	private static String [] quantities = {"density","inflow","outflow"};
		
	/**
	 * Initializes the testing environment
	 * @param conffile File the configuration file
	 */
	public SimulatorTest(File conffile) {
		this.conffile = conffile;
	}

	/**
	 * Retrieves a list of scenario files
	 * @return
	 */
	@Parameters
	public static Vector<Object[]> conffiles() {
		return edu.berkeley.path.beats.test.simulator.BrokenScenarioTest.getWorkingConfigs();
	}
	
	@Before
	public void createOutput(){
		File file = new File(output_folder);   
		if(!file.exists())
			file.mkdir();
	}
	
	@After
	public void clearOutput(){
		File file = new File(output_folder);        
        String[] myFiles;      
        if(file.isDirectory()){  
            myFiles = file.list();  
            for (int i=0; i<myFiles.length; i++) {  
                File myFile = new File(file, myFiles[i]);   
                myFile.delete();  
            }  
         }  
	} 
	
	@Test
	public void testSimulator() {

		Scenario scenario;

		System.out.println("Test");
					
		try {
			
			System.out.println(conffile);
			
			String conffile_nameonly= conffile.getName().split(".xml")[0];
			String outputprefix = output_folder+conffile_nameonly;

			// input parameters
			double startTime = 0d;
			double duration = 3600d;
			double outDt = 30d;
			int numReps = 1;

			// load configuration file
			System.out.println("\tLoading");
			scenario = ObjectFactory.createAndLoadScenario(conffile.toString());

			if (null == scenario)
				throw new BeatsException("UNEXPECTED! Scenario was not loaded");
			
			double timestep = Defaults.getTimestepFor(conffile.getName());
			
			if(Double.isNaN(timestep))
				throw new BeatsException("Unknown configuration file.");
				
			// initialize the scenario
			scenario.initialize(timestep, startTime, startTime+duration, outDt, "text", outputprefix, 1, 1);
			
			// run the scenario
			System.out.println("\tRunning");
			scenario.run();
			
			String [] vehicleTypes = scenario.getVehicleTypeNames();
							
			// compare output
			System.out.println("\tComparing outputS");
			for(String vt : vehicleTypes)
				for(String q : quantities){
					String filename = conffile_nameonly +"_"+q+"_"+vt+"_0.txt";
					ArrayList<ArrayList<Double>> A = BeatsFormatter.readCSV(fixture_folder+filename,"\t");
					ArrayList<ArrayList<Double>> B = BeatsFormatter.readCSV(output_folder+filename,"\t");
					assertNotNull(A);
					assertNotNull(B);
					assertTrue("The files are not equal.",BeatsMath.equals2D(A,B));
				}

		} catch (BeatsException exc) {
			System.out.print(exc.getMessage());
			fail(exc.getMessage());
		} 	
	}

}
