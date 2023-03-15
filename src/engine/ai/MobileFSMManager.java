// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.ai;

import engine.gameManager.SessionManager;
import engine.gameManager.ZoneManager;
import engine.objects.Mob;
import engine.objects.Zone;
import engine.server.MBServerStatics;
import engine.util.ThreadUtils;
import org.pmw.tinylog.Logger;

import java.util.Random;


public class MobileFSMManager {

	private static final MobileFSMManager INSTANCE = new MobileFSMManager();

	private volatile boolean alive;
	private long timeOfKill = -1;

	private MobileFSMManager() {

		Runnable worker = new Runnable() {
			@Override
			public void run() {
				execution();
			}
		};

		alive = true;

		Thread t = new Thread(worker, "MobileFSMManager");
		t.start();
	}

	public static MobileFSMManager getInstance() {
		return INSTANCE;
	}

	/**
	 * Stops the MobileFSMManager
	 */
	public void shutdown() {
		if (alive) {
			alive = false;
			timeOfKill = System.currentTimeMillis();
		}
	}


	public long getTimeOfKill() {
		return this.timeOfKill;
	}

	public boolean isAlive() {
		return this.alive;
	}


	private void execution() {

		//no players online means no mob action required
		long mobPulse = System.currentTimeMillis() + MBServerStatics.AI_PULSE_MOB_THRESHOLD;

		while (alive) {
			//assign random range of delay between 1ms and 2000ms so mob actions don't appear synchronized
			Random r = new Random();
			ThreadUtils.sleep(r.nextInt(2000-1) + 1);

			if (System.currentTimeMillis() > mobPulse) {

				for (Zone zone : ZoneManager.getAllZones()) {
					for (Mob mob : zone.zoneMobSet) {

						try {
							if (mob != null)
								MobileFSM.run(mob);
						} catch (Exception e) {
							Logger.error(e);
							e.printStackTrace();
						}
					}
				}

				mobPulse = System.currentTimeMillis() + MBServerStatics.AI_PULSE_MOB_THRESHOLD;
			}
		}
	}

}
