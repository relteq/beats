function [X] = readSiriusOutput_txt(configfile,outprefix)

fprintf('Reading %s\n', configfile);
scenario = xml_read(configfile);

dt = round(2*scenario.NetworkList.network(1).ATTRIBUTE.dt)/2;
dt = dt/3600;

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

% number of links and link lengths in miles
numLinks = 0;
c=0;
for i=1:length(scenario.NetworkList.network)
    numLinks = numLinks + length(scenario.NetworkList.network(i).LinkList.link);
    for j=1:length(scenario.NetworkList.network(i).LinkList.link)
        c=c+1;
        llength(c) = scenario.NetworkList.network(i).LinkList.link(j).ATTRIBUTE.length;
    end
end

% time
time = load([outprefix '_time_0.txt']);
numTime = length(time);    

X.density = zeros(numDN,numVT,numTime,numLinks);
X.flow = zeros(numDN,numVT,numTime-1,numLinks);

for i=1:numDN
    for j=1:numVT
        X.density(i,j,:,:) = bsxfun(@rdivide , load([outprefix '_density_' dn_id{i} '_' vt_id{j} '_0.txt']) , llength );
        X.flow(i,j,:,:)    = load([outprefix '_outflow_' dn_id{i} '_' vt_id{j} '_0.txt'])/dt;
    end 
end

X.time = load([outprefix '_time_0.txt']);


X.numDN = numDN;
X.numVT = numVT;
X.numTime = numTime;
X.numLinks = numLinks;
