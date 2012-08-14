function pltnet(latd,lond,sym,label,xoffset,yoffset)
% PLTNET  Plots a network of points in decimal degrees with labels.
% Version: 20 Jan 99
% Usage:   pltnet(label,latd,lond,xoffset,yoffset)
% Input:   latd  - latitude of points (deg)
%          lond  - longitude of points (deg)
%          sym   - symbol type for points (string, optional)
%                  Valid choices: o + * - x . v ^ > < square
%                  diamond pentagram hexagram
%          label - point labels (string vector)
%          xoffset - label offset in x from point (optional)
%          yoffset - label offset in y from point (optional)

% Copyright (c) 2011, Michael R. Craymer
% All rights reserved.
% Email: mike@craymer.com

if nargin<2
  error('Too few input arguments');
elseif nargin==2
  sym='o';
  label=[];
  xoffset=0;
  yoffset=0;
elseif nargin==3
  label=[];
  xoffset=0;
  yoffset=0;
elseif nargin==4
  xoffset=0;
  yoffset=0;
elseif nargin==5
  yoffset=0;
elseif nargin>6
  error('Too many input arguments');
end

n=length(latd);
plot(lond,latd,sym);
if ~isempty(label)
  hold on;
  for i=1:n
    text(lond(i)+xoffset,latd(i)+yoffset,label(i,:));
  end
  hold off;
end
xlabel('Longitude (deg)');
ylabel('Latitude (deg)');
