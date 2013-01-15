function []=plot_text(configfile,outname,export_to_ppt)

if(nargin<2)
    error('too few input arguments')
end

if(nargin<3)
    export_to_ppt = false;
end

addpath([fileparts(fileparts(mfilename('fullpath'))) filesep 'xml_io_tools_2007_07']);

[outpath,outprefix] = fileparts(outname);

fprintf('Reading %s\n', configfile);

% load the scenario and keep only what we need
%scenario = xml_read(configfile);

load aaa

if(length(length(scenario.NetworkList.network))~=1)
    error('plot_text does not work for scenarios with multiple networks')
end

% ... links
scenario_links = scenario.NetworkList.network(1).LinkList.link;

% ... vehicle type names
vtNames = {};
for i=1:length(scenario.settings.VehicleTypes.vehicle_type)
    vtNames{i} = scenario.settings.VehicleTypes.vehicle_type(i).ATTRIBUTE.name;
end

% ... output time step
outdt = round(2*scenario.NetworkList.network(1).ATTRIBUTE.dt)/2/3600;

clear scenario

% load data
time = load(sprintf('%s%s%s_%s_0.txt',outpath,filesep,outprefix,'time'));
for v=1:length(vtNames)
    
    % density in veh/mile
    density{v} = load(sprintf('%s%s%s_%s_%s_0.txt',outpath,filesep,outprefix,'density',vtNames{v}));
    for i=1:length(scenario_links)
        lgth = scenario_links(i).ATTRIBUTE.length;
        density{v}(:,i) = density{v}(:,i)/lgth;
    end
    
    % flow in veh/hr
    flow{v} = load(sprintf('%s%s%s_%s_%s_0.txt',outpath,filesep,outprefix,'outflow',vtNames{v}));
    flow{v} = flow{v}/outdt;
    
    % speed in mile/hr
    speed{v} = flow{v}./density{v}(1:(end - 1),:);
    
end

% compute performance....


% plot
if(export_to_ppt)
    fig = figure('Visible','off');
    [ppt,op]=openppt(outname,1);
else
    fig = nan;
    op = nan;
end
for v=1:length(vtNames)
    plot_timeseries(fig,time(1:end-1),speed{v},[vtNames{v} ' speed in [length]/hr'],op,export_to_ppt)
    plot_timeseries(fig,time(1:end-1),flow{v},[vtNames{v} ' flow in veh/hr'],op,export_to_ppt)
    plot_timeseries(fig,time,density{v},[vtNames{v} ' density in veh/[length]'],op,export_to_ppt)
    plot_contour(fig,speed{v},[vtNames{v} ' speed in [length]/hr'],op,export_to_ppt)
    plot_contour(fig,flow{v},[vtNames{v} ' flow in veh/hr'],op,export_to_ppt)
    plot_contour(fig,density{v},[vtNames{v} ' density in veh/[length]'],op,export_to_ppt)
end
if(export_to_ppt)
    closeppt(ppt,op)
    close all
end

disp('done')

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
function plot_timeseries(fig,t,y,tit,op,export_to_ppt)
if(export_to_ppt)
    clf(fig)
else
    figure
end

plot(t,y)

if(export_to_ppt)
    addslide(op,tit)
end

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
function plot_contour(fig,y,tit,op,export_to_ppt)

if(export_to_ppt)
    clf(fig)
else
    figure
end

set(pcolor(y), 'EdgeAlpha', 0);
colorbar;

if(export_to_ppt)
    addslide(op,tit)
end
