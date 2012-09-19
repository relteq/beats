function []=plot_text(configfile,outprefix)

figure
plot(speed)
title('Speed in [length]/hr')

figure
plot(flow)
title('Flow in veh/hr')

figure
plot(density)
title('Density in veh/[length]')


figure
set(pcolor(speed), 'EdgeAlpha', 0);
colorbar;
title('Speed in [length]/hr')

figure
set(pcolor(flow), 'EdgeAlpha', 0);
colorbar;
title('Flow in veh/hr')

figure
set(pcolor(density), 'EdgeAlpha', 0);
colorbar;
title('Density in veh/[length]')

