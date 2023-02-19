// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.devcmd.cmds;

import engine.Enum.DropRateType;
import engine.devcmd.AbstractDevCmd;
import engine.objects.AbstractGameObject;
import engine.objects.PlayerCharacter;
import engine.server.MBServerStatics;
import engine.server.world.WorldServer;

/**
 *
 * @author Murray
 *
 */
public class SetRateCmd extends AbstractDevCmd {

	public SetRateCmd() {
        super("setrate");
    }

	@Override
	protected void _doCmd(PlayerCharacter pc, String[] args, AbstractGameObject target) {

		if (args.length != 2) {
			this.sendUsage(pc);
			return;
		}

		float mod = 0f;

		try {
			mod = Float.parseFloat(args[1]);
		} catch (NumberFormatException e) {
			throwbackError(pc, "Supplied data failed to parse to Float.");
			return;
		}


		if (args[0].equals("exp")){

			DropRateType.EXP_RATE_MOD.rate = mod;
			throwbackInfo(pc, "Experience Rate set to: " + mod);

		} else if (args[0].equals("gold")){

			DropRateType.GOLD_RATE_MOD.rate = mod;
			throwbackInfo(pc, "Gold Rate set to: " + mod);

		} else if (args[0].equals("drop")){

			DropRateType.DROP_RATE_MOD.rate = mod;
			throwbackInfo(pc, "Drop Multiplier Rate set to: " + mod);

		} else if (args[0].equals("hotexp")){

			DropRateType.HOT_EXP_RATE_MOD.rate = mod;
			throwbackInfo(pc, "HOTZONE Experience Rate set to: " + mod);

		} else if (args[0].equals("hotgold")){

			DropRateType.HOT_GOLD_RATE_MOD.rate = mod;
			throwbackInfo(pc, "HOTZONE Gold Rate set to: " + mod);

		} else if (args[0].equals("hotdrop")){

			DropRateType.HOT_DROP_RATE_MOD.rate = mod;
			throwbackInfo(pc, "HOTZONE Drop Multiplier Rate set to: " + mod);

		} else if (args[0].equals("production")){

			MBServerStatics.PRODUCTION_TIME_MULTIPLIER = mod;
			throwbackInfo(pc, "Production Time Multiplier Rate set to: " + mod);

		} else {
			this.sendUsage(pc);
		}

	}

	@Override
	protected String _getUsageString() {
        return "' /setrate {exp|gold|drop|hotexp|hotgold|hotdrop} rate'";
	}

	@Override
	protected String _getHelpString() {
        return "Sets the rates for exp, gold or drops. Accepts a float, defaults to 1.0";
	}

}