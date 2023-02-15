// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.powers.poweractions;

import engine.Enum;
import engine.Enum.BuildingGroup;
import engine.Enum.GameObjectType;
import engine.Enum.PortalType;
import engine.math.Vector3fImmutable;
import engine.objects.AbstractCharacter;
import engine.objects.AbstractWorldObject;
import engine.objects.Building;
import engine.objects.Runegate;
import engine.powers.ActionsBase;
import engine.powers.PowersBase;

import java.sql.ResultSet;
import java.sql.SQLException;

public class OpenGatePowerAction extends AbstractPowerAction {

	/**
	 * ResultSet Constructor
	 */
	public OpenGatePowerAction(ResultSet rs) throws SQLException {
		super(rs);
	}

	public OpenGatePowerAction(int uUID, String iDString, String type, boolean isAggressive, long validItemFlags) {
		super(uUID, iDString, type, isAggressive, validItemFlags);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void _startAction(AbstractCharacter source, AbstractWorldObject awo, Vector3fImmutable targetLoc, int trains, ActionsBase ab, PowersBase pb) {

		
		if (awo.getObjectType().equals(GameObjectType.Building) == false)
			return;

		Building targetBuilding = (Building) awo;
		int token;

		// Sanity check.

		if (source == null || awo == null || !(awo.getObjectType().equals(Enum.GameObjectType.Building)) || pb == null)
			return;

		// Make sure building has a blueprint

		if (targetBuilding.getBlueprintUUID() == 0)
			return;

		// Make sure building is actually a runegate.

		if (targetBuilding.getBlueprint().getBuildingGroup() != BuildingGroup.RUNEGATE)
			return;

		// Which portal was opened?

		token = pb.getToken();
		PortalType portalType = PortalType.AIR;

		switch (token) {
		case 428937084: //Death Gate
			portalType = PortalType.OBLIV;
			break;

		case 429756284: //Chaos Gate
			portalType = PortalType.CHAOS;
			break;

		case 429723516: //Khar Gate
			portalType = PortalType.MERCHANT;
			break;

		case 429559676: //Spirit Gate
			portalType = PortalType.SPIRIT;
			break;

		case 429592444: //Water Gate
			portalType = PortalType.WATER;
			break;

		case 429428604: //Fire Gate
			portalType = PortalType.FIRE;
			break;

		case 429526908: //Air Gate
			portalType = PortalType.AIR;
			break;

		case 429625212: //Earth Gate
			portalType = PortalType.EARTH;
			break;

		default:
		}

		// Which runegate was clicked on?

		Runegate runeGate = Runegate._runegates.get(targetBuilding.getObjectUUID());
		runeGate.activatePortal(portalType);

	}

	@Override
	protected void _handleChant(AbstractCharacter source, AbstractWorldObject target, Vector3fImmutable targetLoc, int trains, ActionsBase ab, PowersBase pb) {
	}

	@Override
	protected void _startAction(AbstractCharacter source, AbstractWorldObject awo, Vector3fImmutable targetLoc,
			int numTrains, ActionsBase ab, PowersBase pb, int duration) {
		// TODO Auto-generated method stub

	}
}
