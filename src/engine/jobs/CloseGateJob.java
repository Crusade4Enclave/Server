// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


 package engine.jobs;

import engine.Enum;
import engine.Enum.PortalType;
import engine.job.AbstractScheduleJob;
import engine.objects.Building;
import engine.objects.Runegate;
import org.pmw.tinylog.Logger;
 
public class CloseGateJob extends AbstractScheduleJob {

	private final Building building;
        private final Enum.PortalType portalType;

	public CloseGateJob(Building building, PortalType portalType) {

		super();
		this.building = building;
		this.portalType = portalType;
	}

	@Override
	protected void doJob() {
            
		if (building == null) {
                    Logger.error("Rungate building was null");
                    return;
                }

                Runegate._runegates.get(building.getObjectUUID()).deactivatePortal(portalType);
	}

	@Override
	protected void _cancelJob() {
	}
}

