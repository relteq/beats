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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

import org.junit.After;
import org.junit.Test;

import edu.berkeley.path.beats.simulator.ObjectFactory;
import edu.berkeley.path.beats.simulator.Scenario;
import edu.berkeley.path.beats.simulator.SimulationSettings;
import edu.berkeley.path.beats.simulator.SiriusException;
import edu.berkeley.path.beats.simulator.SiriusFormatter;
import edu.berkeley.path.beats.simulator.SiriusMath;

public class SimulatorTest {

    private String fixture_folder = "data/test/fixture/";
    private String output_folder = "data/test/output/";
    private String config_folder = "data/config/";
    
	private static String [] config_names = {
												"Albany-and-Berkeley",
												"_scenario_2009_02_12",
												"_scenario_constantsplits",
												//"_smalltest",
												"_smalltest_multipletypes",
												//"complete",
												//"multipletypes-SI",
												//"multipletypes",
												//"scenario_twotypes",
												"test_event",
												"testfwy2",
												"testfwy_w" };
	
	private static String [] quantities = {"density","inflow","outflow"};
		
	private static String CONF_SUFFIX = ".xml";
	
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
			
			for(String config_name : config_names ){

				System.out.println(config_name);
				
				String configfile = config_folder+config_name+CONF_SUFFIX;
				String outputprefix = output_folder+config_name;
	
				// process input parameters
				SimulationSettings simsettings = new SimulationSettings(0d,3600d,30d,1);
	
				// load configuration file
				System.out.println("\tLoading");
				scenario = ObjectFactory.createAndLoadScenario(configfile);
	
				if (null == scenario)
					throw new SiriusException("UNEXPECTED! Scenario was not loaded");
	
				// set output format properties
				Properties owr_props = new Properties();
				owr_props.setProperty("prefix", outputprefix);
				owr_props.setProperty("type", "text");
				
				// run the scenario
				System.out.println("\tRunning");
				scenario.run(simsettings, owr_props);
				
				String [] vehicleTypes = scenario.getVehicleTypeNames();
								
				// compare output
				System.out.println("\tComparing outputS");
				for(String vt : vehicleTypes)
					for(String q : quantities){
						String filename = config_name+"_"+q+"_"+vt+"_0.txt";
						ArrayList<ArrayList<Double>> A = SiriusFormatter.readCSV(fixture_folder+filename,"\t");
						ArrayList<ArrayList<Double>> B = SiriusFormatter.readCSV(output_folder+filename,"\t");
						assertTrue("The files are not equal.",SiriusMath.equals2D(A,B));
					}
			
			}

		} catch (SiriusException exc) {
			exc.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 		
	}

}
