package edu.berkeley.path.beats;

import edu.berkeley.path.beats.control.Controller_CRM_MPC;
import edu.berkeley.path.beats.jaxb.Controller;
import edu.berkeley.path.beats.jaxb.FundamentalDiagramSet;
import edu.berkeley.path.beats.simulator.DemandSet;
import edu.berkeley.path.beats.simulator.InitialDensitySet;
import edu.berkeley.path.beats.simulator.Network;
import edu.berkeley.path.beats.simulator.ObjectFactory;
import edu.berkeley.path.beats.simulator.Scenario;
import edu.berkeley.path.beats.simulator.SplitRatioSet;

public class Act2Runner {

    public static void main(String[] args) {

        Scenario scenario;
        double start_time = 0;                  // midnight
        double time_current = start_time;       // [sec]
        double dt = 5;                          // [sec] largest common divisor of all dts involved.
        double end_time = 3600;                 // [sec]
        double optimizer_update_period = 300;   // [sec] call optimization this often
        double actuator_deploy_period = 30;     // [sec] send actuation to aimsun every 30 seconds;

        // load a scenario
        try{
            String configFile = "data\\config\\bla.xml";
            scenario = ObjectFactory.createAndLoadScenario(configFile);
            }
        catch ( Exception e) {
            System.out.println("whoops!");
            return;
        }

        // deconstruct the scenario
        Network network = (Network) scenario.getNetworkSet().getNetwork().get(0);
        FundamentalDiagramSet fd_set = scenario.getFundamentalDiagramSet();
        Controller jaxbC = scenario.getControllerSet().getController().get(0);

        // create the controller
        Controller_CRM_MPC controller = new Controller_CRM_MPC(
                jaxbC,
                network,
                fd_set);

        // main loop
        while(time_current<end_time){

            double run_time = time_current-start_time;

            // update the controller  ..............
            if( (run_time%optimizer_update_period)==0 ){       // call optimizer

                // Run your predictors (here it is a 1 hour prediction with 5-min intervals)
                DemandSet demand_set = scenario.predict_demands(time_current,300,12);
                SplitRatioSet split_ratio_set = scenario.predict_split_ratios(time_current,300,12);

                // Run estimation
                InitialDensitySet init_dens_set = scenario.gather_current_densities();

                // Call the controller
                controller.update_and_send_policy_to_actuators(time_current, demand_set, split_ratio_set, init_dens_set);
            }
            else{                                       // lightweight controller call
                controller.send_policy_to_actuators(time_current);
            }

//            // deploy the actuation ..............
//            if( (run_time%actuator_deploy_period)==0){
//                controller.deploy_actuators();
//            }

            // update time .....................
            time_current += dt;
        }

    }
}
