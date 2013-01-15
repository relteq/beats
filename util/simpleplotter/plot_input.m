function []=plot_input(configfile)

if(nargin==0)
    error('too few input arguments')
end

addpath([fileparts(fileparts(mfilename('fullpath'))) filesep 'xml_io_tools_2007_07']);

fprintf('Reading %s\n', configfile);

% load the scenario and keep only what we need
scenario = xml_read(configfile,struct('Str2Num',false));

% settings
vtNames = {};
for i=1:length(scenario.settings.VehicleTypes.vehicle_type)
    vtNames{i} = scenario.settings.VehicleTypes.vehicle_type(i).ATTRIBUTE.name;
end

% demands
if(isfield(scenario,'DemandProfileSet'))
    vt_ind = getVTindex(vtNames,scenario.DemandProfileSet.VehicleTypeOrder.vehicle_type);
    for i=1:length(scenario.DemandProfileSet.demandProfile)
        X = readDemandProfile(scenario.DemandProfileSet.demandProfile(i),vt_ind);
        plotDemandProfile(X);
    end
end

% split ratios



%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
function [vt_ind]=getVTindex(vtNames,X)
    
for i=1:length(X)
    ind = strcmp(X(i).ATTRIBUTE.name,vtNames);
    if(sum(ind)~=1)
        error('error')
    else
        vt_ind(i) = find(ind);
    end
end

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
function [X]=readDemandProfile(D,vt_ind)

X.demand = read_comma_colon_format(D.CONTENT);
X.demand = X.demand(:,vt_ind);

if(isfield(D.ATTRIBUTE,'dt'))
    X.dt = D.ATTRIBUTE.dt;
else
    X.dt = 1;
end

if(isfield(D.ATTRIBUTE,'knob'))
    X.knob = D.ATTRIBUTE.knob;
else
    X.knob = 1;
end
if(isfield(D.ATTRIBUTE,'start_time'))
    X.start_time = D.ATTRIBUTE.start_time;
else
    X.start_time = 0;
end

if(isfield(D.ATTRIBUTE,'std_dev_mult'))
    X.std_dev_mult = D.ATTRIBUTE.std_dev_mult;
else
    X.std_dev_mult = 1;
end

if(isfield(D.ATTRIBUTE,'std_dev_add'))
    X.std_dev_add = D.ATTRIBUTE.std_dev_add;
else
    X.std_dev_add = inf;
end

X.link = D.ATTRIBUTE.link_id_origin;

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
function plotDemandProfile(X)

n = size(X.demand,1);

time = X.start_time + (0:n-1)*X.dt;
time = time/3600;

d = X.demand*X.knob;
for i=1:size(d,2)
    std_dev_apply = min([d(:,i)*X.std_dev_mult X.std_dev_add*ones(size(d,1),1)],[],2);
    dup(:,i) = d(:,i) + std_dev_apply;
    ddn(:,i) = d(:,i) - std_dev_apply;
end
ddn = max(ddn,0);

figure
jbfill(gcf,time,dup',ddn','c','c')
hold on
plot(time,d,'k','LineWidth',1)
grid
if(length(time)>1)
    set(gca,'XLim',[time(1) time(end)])
end



