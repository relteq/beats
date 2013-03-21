clear
close all

rootdir = fileparts(fileparts(fileparts(mfilename('fullpath'))));
configfile = fullfile(rootdir,'data','config','_scenario_2009_02_12.xml');
report_config(configfile,'report')

disp('done')