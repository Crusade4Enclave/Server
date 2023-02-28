// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com



package engine.devcmd.cmds;


import engine.InterestManagement.RealmMap;
import engine.devcmd.AbstractDevCmd;
import engine.gameManager.ZoneManager;
import engine.objects.AbstractGameObject;
import engine.objects.PlayerCharacter;
import engine.objects.Realm;
import engine.objects.Zone;

public class RealmInfoCmd extends AbstractDevCmd {

	public RealmInfoCmd() {
        super("realminfo");
    }

	@Override
	protected void _doCmd(PlayerCharacter playerCharacter, String[] words,
			AbstractGameObject target) {

		Zone serverZone;
		Realm serverRealm;
		int realmID;
		String outString = "";

		if (playerCharacter == null)
			return;

		serverZone = ZoneManager.findSmallestZone(playerCharacter.getLoc());

		if (serverZone == null) {
			throwbackError(playerCharacter, "Zone not found");
			return;
		}

		realmID = RealmMap.getRealmIDAtLocation(playerCharacter.getLoc());

		String newline = "\r\n ";

		outString = newline;
		outString += "Realm: " + realmID + "(";

		serverRealm = Realm.getRealm(realmID);

		if (serverRealm == null)
			outString += "SeaFloor";
		else
			outString += serverRealm.getRealmName();

		outString += ")";
		outString += newline;

		outString += " Zone: " + serverZone.getName();

		outString += newline;

		if (serverZone.getParent() != null)
			outString += " Parent: " + serverZone.getParent().getName();
		else
			outString += "Parent: NONE";

		outString += newline;

		throwbackInfo(playerCharacter, outString);
	}

	@Override
	protected String _getHelpString() {
        return "Returns info on realm.";
	}

	@Override
	protected String _getUsageString() {
        return "' /info targetID'";
	}

}
