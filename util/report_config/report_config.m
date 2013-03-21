function []=report_config(configfile,pptfile)
% Powerpoint slides for scenario input
% Arguments:
%   1. configfile ... name of the xml config file
%   2. pptfile ... name of the output powerpoint file

scenario = [];

% scenario = xml_read(configfile);
load aaa

% param
nodetypes = {'simple','onramp','offramp','signalized_intersection','unsignalized_intersection','terminal','highway','stop_intersection','other'};
linktypes = {'freeway','HOV','HOT','onramp','offramp','freeway_connector','street','intersection_approach','heavy_vehicle','electric_toll'};
sensortypes = {'loop','magnetic','radar','camera','TMC'};
sensorlinktypes = {'freeway','HOV','onramp','offramp','other'};

% open ppt
[ppt,op]=openppt(pptfile,true);

addslideTitle(op,{'Configuration report',getUserName(),datestr(today)});
addslideText(op,'Configuration file',configfile)
addslideText(op,'Networks',getNetworkSummary());
addslideText(op,'Sensors',getSensorsSummary());
addslideText(op,'Events',getEventsSummary());
addslideText(op,'Controllers',getControllersSummary());
% 
% if(getNumVehicleTypes()>1)
%     closeppt(ppt,op)
%     disp('Aborting. Plots have not been implemented for multiple vehicle types')
%     return
% end

% Split ratios ----------------------------------------------------
addslideTitle(op,{'Split ratio profiles'});
insertSplitRatioSlides();

% Demands  ----------------------------------------------------
addslideTitle(op,{'Demand profiles'});
insertDemandProfileSlides();

% Fundamental Diagram profiles  --------------------------------------
addslideTitle(op,{'Fundamental diagram profiles'});
insertFDProfileSlides();

closeppt(ppt,op)

% =============================================================
    function [str] = getNetworkSummary()
        
        str = sprintf('Number of networks: %d\n',length(scenario.NetworkList.network));
        
        for i=1:length(scenario.NetworkList.network)
            N =  scenario.NetworkList.network(i);
            str = sprintf('%sNetwork %d\n',str,i);
            
            nodetypecount = zeros(1,length(nodetypes));
            for j=1:length(N.NodeList.node)
                node = N.NodeList.node(j);
                nodetypeindex = strcmp(node.ATTRIBUTE.type,nodetypes);
                nodetypecount(nodetypeindex) = nodetypecount(nodetypeindex)+1;
            end
            
            linktypecount = zeros(1,length(linktypes));
            for j=1:length(N.LinkList.link)
                link = N.LinkList.link(j);
                linktypeindex = strcmp(link.ATTRIBUTE.type,linktypes);
                linktypecount(linktypeindex) = linktypecount(linktypeindex)+1;
            end
            
            str = sprintf('%s\tnumber of nodes: %d\n',str,length(N.NodeList.node));
            for j=1:length(nodetypes)
                if(nodetypecount(j)>0)
                    str = sprintf('%s\t\t%s: %d\n',str,nodetypes{j},nodetypecount(j));
                end
            end
            
            str = sprintf('%s\tnumber of links: %d\n',str,length(N.LinkList.link));
            for j=1:length(linktypes)
                if(linktypecount(j)>0)
                    str = sprintf('%s\t\t%s: %d\n',str,linktypes{j},linktypecount(j));
                end
            end
        end
        clear N node link nodetypecount linktypecount linktypeindex nodetypeindex
    end
% =============================================================
    function [str]=getSensorsSummary()
        
        str = sprintf('Number of sensors: %d\n',length(scenario.SensorList.sensor));
        
        sensortypecount = zeros(1,length(sensortypes));
        sensorlinktypecount = zeros(1,length(sensorlinktypes));
        attached = 0;
        unattached = 0;
        for j=1:length(scenario.SensorList.sensor)
            sensor = scenario.SensorList.sensor(j);
            sensortypeindex = strcmp(sensor.ATTRIBUTE.type,sensortypes);
            sensortypecount(sensortypeindex) = sensortypecount(sensortypeindex)+1;
            sensorlinktypeindex = strcmp(getParameter(sensor,'link_type'),sensorlinktypes);
            sensorlinktypecount(sensorlinktypeindex) = sensorlinktypecount(sensorlinktypeindex)+1;
            if(~isempty(sensor.link_reference.ATTRIBUTE.id))
                attached = attached+1;
            else
                unattached = unattached+1;
            end
        end
        
        str = sprintf('%sSensor types\n',str);
        for j=1:length(sensortypes)
            if(sensortypecount(j)>0)
                str = sprintf('%s\t%s: %d\n',str,sensortypes{j},sensortypecount(j));
            end
        end
        
        str = sprintf('%sSensor link types\n',str);
        for j=1:length(sensorlinktypes)
            if(sensorlinktypecount(j)>0)
                str = sprintf('%s\t%s: %d\n',str,sensorlinktypes{j},sensorlinktypecount(j));
            end
        end
        
        str = sprintf('%sAttached sensors: %d\n',str,attached);
        str = sprintf('%sUnattached sensors: %d\n',str,unattached);
    end


% =============================================================
    function [str]=getEventsSummary()
        str = '<not implemented>';
    end

% =============================================================
    function [str]=getControllersSummary()
        str = '<not implemented>';
    end


% =============================================================
    function []=insertSplitRatioSlides()
        if(~isfield(scenario,'SplitRatioProfileSet'))
            return
        end
    end
% =============================================================
    function []=insertDemandProfileSlides()
        if(~isfield(scenario,'DemandProfileSet'))
            return
        end
        if(~isfield(scenario.DemandProfileSet,'demandProfile'))
            return
        end
        for i=1:3 %length(scenario.DemandProfileSet.demandProfile)
            demand = readCommaSeparatedVector(scenario.DemandProfileSet.demandProfile(i).CONTENT);
            dt = readAttribute(scenario.DemandProfileSet.demandProfile(i),'dt',300);
            knob = readAttribute(scenario.DemandProfileSet.demandProfile(i),'knob',1);
            link_id_origin = readAttribute(scenario.DemandProfileSet.demandProfile(i),'link_id_origin','');
            start_time = readAttribute(scenario.DemandProfileSet.demandProfile(i),'start_time',0);
            
            
            numIntervals = length(demand);
            time = start_time+[0 cumsum(dt*ones(1,numIntervals-1))];
            time = time/3600;
            
            fillplot(time,knob*demand,{'b'});
            set(gcf,'Position',[441   141   711   420])
            if(length(time)>1)
                set(gca,'XLim',[time(1) time(end)])
            end
            grid
            timelabelXTick(gca,'hm')
            ylabel('vehicles/hour')
            
            addslide(op,['Link id = ' num2str(link_id_origin)])
            close all
        end
        
    end
% =============================================================
    function []=insertFDProfileSlides()
        if(~isfield(scenario,'FundamentalDiagramProfileSet'))
            return
        end
        if(~isfield(scenario.FundamentalDiagramProfileSet,'fundamentalDiagramProfile'))
            return
        end
        
        for i=1:3 %length(scenario.FundamentalDiagramProfileSet.fundamentalDiagramProfile)
            
            FDP = scenario.FundamentalDiagramProfileSet.fundamentalDiagramProfile(i);
            
            link_id = readAttribute(FDP,'link_id',nan);
            
            if(isnan(link_id))
                continue
            end
            
            if(~isfield(FDP,'fundamentalDiagram'))
                continue
            end
            
            if(length(FDP.fundamentalDiagram)>1)
                disp('Warning: Fundamental diagram reports not implemented for vector profiles. Keeping only first entry')
            end
            
            FDparam = computeFDparameters(FDP.fundamentalDiagram(1));
            
            rho = linspace(0,FDparam.jam_density);
            ff = rho<FDparam.density_critical;
            
            S = nan*rho;
            S(ff) = rho(ff) * FDparam.free_flow_speed;
            S(~ff) = max(FDparam.capacity-FDparam.capacity_drop,0);
            
            R = nan*rho;
            R(ff) =  FDparam.capacity;
            R(~ff) = FDparam.congestion_speed*(FDparam.jam_density - rho(~ff));
            
            figure
            plot(rho,S,'r','LineWidth',2)
            hold on
            plot(rho,R,'b','LineWidth',2)
            xlabel('density [veh/m/lane]')
            xlabel('flow [veh/s/lane]')
            
            addslide(op,['Link id = ' num2str(link_id)]);
            close
            
        end
    end
% =============================================================
    function [v]=getParameter(obj,name)
        
        v = [];
        if(~isfield(obj,'parameters'))
            return
        end
        if(~isfield(obj.parameters,'parameter'))
            return
        end
        for i=1:length(obj.parameters.parameter)
            if(~isfield(obj.parameters.parameter(i),'ATTRIBUTE'))
                continue
            end
            if(~all(isfield(obj.parameters.parameter(i).ATTRIBUTE,{'name','value'})))
                continue
            end
            if(strcmp(obj.parameters.parameter(i).ATTRIBUTE.name,name))
                v = obj.parameters.parameter(i).ATTRIBUTE.value;
                return
            end
        end
        
    end

% =============================================================
    function [A]=readCommaSeparatedVector(str)
        if(ischar(str))
            A = str2double(str);
        else
            A = str;
        end
    end

% =============================================================
    function [v] = readAttribute(obj,name,default)
        v = default;
        if(~isfield(obj,'ATTRIBUTE'))
            return
        end
        if(isfield(obj.ATTRIBUTE,name))
            v = obj.ATTRIBUTE.(name);
        end
    end


% =============================================================
    function [FDparam] = computeFDparameters(FD)
        % copied from BeATS
        
        nummissing = 0;
        default = struct( 'capacity', 2400.0/3600.0,...           % [veh/sec/lane]
                          'free_flow_speed', 60.0*0.44704,...     % [m/s]
                          'congestion_speed',20.0*0.44704,...     % [m/s]
                          'jam_density',160.0/1609.344,...        % [veh/meter/lane]
                          'capacity_drop',0);                     % [veh/sec/lane]
        
        % read input values
        FDparam.capacity = readAttribute(FD,'capacity',nan);                    % [veh/s/lane]
        FDparam.free_flow_speed = readAttribute(FD,'free_flow_speed',nan);      % [m/s]
        FDparam.congestion_speed = readAttribute(FD,'congestion_speed',nan);    % [m/s]
        FDparam.jam_density = readAttribute(FD,'jam_density',nan);              % [veh/m/lane]
        FDparam.capacity_drop = readAttribute(FD,'capacity_drop',0);            % [veh/s/lane]
        
        if(isnan(FDparam.capacity))
            missing_capacity = false;
        else
            nummissing = nummissing+1;
            missing_capacity = true;
        end
        
        if(isnan(FDparam.free_flow_speed))
            missing_vf = false;
        else
            nummissing = nummissing+1;
            missing_vf = true;
        end
        
        if(isnan(FDparam.congestion_speed))
            missing_w = false;
        else
            nummissing = nummissing+1;
            missing_w = true;
        end
        
        if(isnan(FDparam.jam_density))
            missing_densityJam = false;
        else
            nummissing = nummissing+1;
            missing_densityJam = true;
        end
        
        % in order, check for missing values and fill in until we are able to make triangle
        if(missing_capacity && nummissing>1)
            FDparam.capacity = default.capacity;
            nummissing = nummissing-1;
        end
        
        if(missing_vf && nummissing>1)
            FDparam.free_flow_speed = default.free_flow_speed;
            nummissing = nummissing-1;
        end
        
        if(missing_w && nummissing>1)
            FDparam.congestion_speed = default.congestion_speed;
            nummissing = nummissing-1;
        end
        
        if(missing_densityJam && nummissing>1)
            FDparam.jam_density = default.jam_density;
            nummissing = nummissing-1;
        end
        
        % now there should be no more than one missing value
        if(nummissing>1)
            disp('BIG MISTAKE!!!!')
        end
        
        % if there is one missing, compute it with triangular assumption
        if(nummissing==1)
            if(missing_capacity)
                FDparam.capacity = FDparam.jam_density / (1.0/FDparam.free_flow_speed + 1.0/FDparam.congestion_speed);
            end
            if(missing_vf)
                FDparam.free_flow_speed = 1.0 / ( FDparam.jam_density/FDparam.capacity - 1.0/FDparam.congestion_speed );
            end
            if(missing_w)
                FDparam.congestion_speed = 1.0 / ( FDparam.jam_density/FDparam.capacity - 1.0/FDparam.free_flow_speed );
            end
            if(missing_densityJam)
                FDparam.jam_density = FDparam.capacity*(1.0/FDparam.free_flow_speed + 1.0/FDparam.congestion_speed);
            end
        end
        
        % set critical density
        FDparam.density_critical = FDparam.capacity/FDparam.free_flow_speed;
        
    end


end