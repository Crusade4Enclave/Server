package engine.objects;

import engine.Enum;
import engine.Enum.PortalType;
import engine.gameManager.DbManager;
import engine.net.ByteBufferWriter;

import java.util.ArrayList;
import java.util.HashMap;

/* Runegates are tied to particular buildings at
 * bootstrap derived from the Portal creation.
 * This is only to enable the toggling of effect
 * bits when traveler is used.
 */

public class Runegate {

	// Runegate class Instance variables
	public static HashMap<Integer, Runegate>  _runegates = new HashMap<>();

	public Portal[] _portals;
	public Building gateBuilding;

	private Runegate(Building gateBuilding) {

		this._portals = new Portal[8];
		this.gateBuilding = gateBuilding;

		// Chaos, Khar and Oblivion are on by default

		_portals[Enum.PortalType.CHAOS.ordinal()].activate(false);
		_portals[Enum.PortalType.OBLIV.ordinal()].activate(false);
		_portals[Enum.PortalType.MERCHANT.ordinal()].activate(false);

	}

	public void activatePortal(Enum.PortalType portalType) {

		this._portals[portalType.ordinal()].activate(true);

	}

	public void deactivatePortal(Enum.PortalType portalType) {

		this._portals[portalType.ordinal()].deactivate();

	}


	public Portal[] getPortals() {

		return this._portals;

	}

	public void collidePortals() {

		for (Portal portal : this.getPortals()) {

			if (portal.isActive())
				portal.collide();
		}
	}

	public static void loadAllRunegates() {

	ArrayList<Integer>	gateList;

	gateList = DbManager.RunegateQueries.GET_RUNEGATE_LIST();

	for (int gateID : gateList) {

		Building gateBuilding = (Building) DbManager.getObject(Enum.GameObjectType.Building, gateID);

		Runegate runegate = new Runegate(gateBuilding);
		runegate.configurePortals();
		_runegates.put(gateID, runegate);
	}

	}

	public void configurePortals() {

		ArrayList<Portal> portalList = DbManager.RunegateQueries.GET_PORTAL_LIST(this.gateBuilding.getObjectUUID());

		for (Portal portal : portalList) {
			this._portals[portal.portalType.ordinal()] = portal;
		}
	}

	public void _serializeForEnterWorld(ByteBufferWriter writer) {

		writer.putInt(gateBuilding.getObjectType().ordinal());
		writer.putInt(gateBuilding.getObjectUUID());
		writer.putString(gateBuilding.getParentZone().getName());
		writer.putFloat(gateBuilding.getLoc().getLat());
		writer.putFloat(gateBuilding.getLoc().getAlt());
		writer.putFloat(gateBuilding.getLoc().getLong());
	}

	
	public static ArrayList<String> GetAllOpenGateIDStrings(){
		ArrayList<String> openGateIDStrings = new ArrayList<>();
		
		openGateIDStrings.add("TRA-003");
		openGateIDStrings.add("TRA-004");
		openGateIDStrings.add("TRA-005");
		openGateIDStrings.add("TRA-006");
		openGateIDStrings.add("TRA-007");
		openGateIDStrings.add("TRA-008");
		openGateIDStrings.add("TRA-009");
		openGateIDStrings.add("TRA-010");
		return openGateIDStrings;
	}

}
