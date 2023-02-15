package engine.objects;

import engine.Enum;
import engine.Enum.PortalType;
import engine.InterestManagement.WorldGrid;
import engine.gameManager.ConfigManager;
import engine.job.JobScheduler;
import engine.jobs.CloseGateJob;
import engine.math.Vector3fImmutable;
import engine.server.MBServerStatics;

import java.util.HashSet;

/* A Runegate object holds an array of these
 * portals.  This class controls their triggers
 * and visual effects.
 */

public class Portal {

	private boolean active;
	public Enum.PortalType portalType;
	public Building  sourceGate;
	public Building  targetGate;
	public final Vector3fImmutable portalLocation;
	private long lastActive = 0;

	public Portal(Building sourceGate, PortalType portalType, Building targetGate) {

		this.active = false;
		this.sourceGate = sourceGate;
		this.targetGate = targetGate;
		this.portalType = portalType;

		this.portalLocation = sourceGate.getLoc().add(new Vector3fImmutable(portalType.offset.x, 6, portalType.offset.y));
	}

	public boolean isActive() {

		return this.active;

	}

	public void deactivate() {

		// Remove effect bit from the runegate building, which turns off this
		// portal type's particle effect

		sourceGate.removeEffectBit(portalType.effectFlag);
		this.active = false;
		sourceGate.updateEffects();
	}

	public void activate(boolean autoClose) {

		Building sourceBuilding;


		// Apply  effect bit to the runegate building, which turns on this
		// portal type's particle effect


		sourceGate.addEffectBit(portalType.effectFlag);
		this.lastActive = System.currentTimeMillis();
		this.active = true;

		// Do not update effects at bootstrap as it
		// tries to send a dispatch.

		if (ConfigManager.worldServer.isRunning == true)
			sourceGate.updateEffects();

		if (autoClose == true) {
            CloseGateJob cgj = new CloseGateJob(sourceGate, portalType);
			JobScheduler.getInstance().scheduleJob(cgj, MBServerStatics.RUNEGATE_CLOSE_TIME);
		}
	}

	public void collide() {

		HashSet<AbstractWorldObject> playerList;

		playerList = WorldGrid.getObjectsInRangePartial(this.portalLocation, 2, MBServerStatics.MASK_PLAYER);

		if (playerList.isEmpty())
			return;

		for (AbstractWorldObject player : playerList) {

			onEnter((PlayerCharacter) player);

		}
	}

	public void onEnter(PlayerCharacter player) {

		if (player.getTimeStamp("lastMoveGate") < this.lastActive)
			return;

        	player.teleport(targetGate.getLoc());
    		player.setSafeMode();
		
	}

	/**
	 * @return the portalLocation
	 */
	public Vector3fImmutable getPortalLocation() {
		return portalLocation;
	}
}
