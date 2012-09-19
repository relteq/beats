function [X] = loadSiriusOutput_txt(configfile,outprefix)

fprintf('Reading %s\n', configfile);
scenario = xml_read(configfile);

dt = round(2*scenario.NetworkList.network(1).ATTRIBUTE.dt)/2;
outdt = dt/3600;
clear dt

% destination network names
for i=1:length(scenario.DestinationNetworks.destination_network)
    dn_id{i} = scenario.DestinationNetworks.destination_network(i).ATTRIBUTE.id;
end
numDN = length(dn_id);

% vehicle type names
for i=1:length(scenario.settings.VehicleTypes.vehicle_type)
    vt_id{i} = scenario.settings.VehicleTypes.vehicle_type(i).ATTRIBUTE.name;
end
numVT = length(vt_id);

% number of links
numLinks = 0;
for i=1:length(scenario.NetworkList.network)
    numLinks = numLinks + length(scenario.NetworkList.network(i).LinkList.link);
end

% time
time = load([outprefix '_time_0.txt']);
numTime = length(time);    

X.density = zeros(numDN,numVT,numTime,numLinks);
X.outflow = zeros(numDN,numVT,numTime-1,numLinks);

for i=1:numDN
    for j=1:numVT
        X.density(i,j,:,:) = load([outprefix '_density_' dn_id{i} '_' vt_id{j} '_0.txt']);
        X.outflow(i,j,:,:) = load([outprefix '_outflow_' dn_id{i} '_' vt_id{j} '_0.txt']);
    end 
end

% % density in veh/mile
% density = load([outprefix '_density_0.txt']);
% for i=1:length(scenario.NetworkList.network(1).LinkList.link)
%     lgth = scenario.NetworkList.network(1).LinkList.link(i).ATTRIBUTE.length;
%     density(:,i) = density(:,i)/lgth;
% end
% 
% % flow in veh/hr
% flow = load([outprefix '_outflow_0.txt']);
% flow = flow/outdt;

% speed in mile/hr
speed = outflow./density(1:(end - 1),:);
