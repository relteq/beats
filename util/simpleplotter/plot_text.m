function []=plot_text(configfile,outprefix)

path = fullfile(fileparts(fileparts(mfilename('fullpath'))),'loadOutput');
addpath(path);
X = readSiriusOutput_txt(configfile,outprefix);
rmpath(path);

% aggregate vehicle types
density = reshape(sum(X.density,2),X.numDN,X.numTime,X.numLinks);
flow = reshape(sum(X.flow,2),X.numDN,X.numTime-1,X.numLinks);

% aggregate desination networks
density = reshape(sum(density,1),X.numTime,X.numLinks);
flow = reshape(sum(flow,1),X.numTime-1,X.numLinks);

figure
plot(X.time(1:end-1)/3600,flow)
ylabel('Flow in veh/hr')
xlabel('time [hr]')

figure
plot(X.time/3600,density)
ylabel('Density in veh/mile')
xlabel('time [hr]')

% figure
% set(pcolor(simdata.speed), 'EdgeAlpha', 0);
% colorbar;
% title('Speed in [length]/hr')
% 
% figure
% set(pcolor(flow), 'EdgeAlpha', 0);
% colorbar;
% title('Flow in veh/hr')
% 
% figure
% set(pcolor(density), 'EdgeAlpha', 0);
% colorbar;
% title('Density in veh/[length]')

