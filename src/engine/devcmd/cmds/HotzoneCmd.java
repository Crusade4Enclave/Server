// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com



package engine.devcmd.cmds;

import engine.devcmd.AbstractDevCmd;
import engine.gameManager.ZoneManager;
import engine.math.FastMath;
import engine.net.client.msg.HotzoneChangeMsg;
import engine.objects.AbstractGameObject;
import engine.objects.PlayerCharacter;
import engine.objects.Zone;
import engine.server.world.WorldServer;

/**
 * ./hotzone                      <- display the current hotzone & time remaining
 * ./hotzone random               <- change hotzone to random new zone
 */

public class HotzoneCmd extends AbstractDevCmd {

    public HotzoneCmd() {
        super("hotzone");
    }

    @Override
    protected void _doCmd(PlayerCharacter pc, String[] words,
                          AbstractGameObject target) {

        StringBuilder data = new StringBuilder();

        for (String s : words) {
            data.append(s);
            data.append(' ');
        }

        String input = data.toString().trim();

        if (input.length() == 0) {
            throwbackInfo(pc, "Current hotZone: " + ZoneManager.hotZone.getName());
            return;
        }

        if (input.equalsIgnoreCase("random")) {
            ZoneManager.generateAndSetRandomHotzone();
            throwbackInfo(pc, "New hotZone: " + ZoneManager.hotZone.getName());
        }

        if (input.equalsIgnoreCase("reset"))
            ZoneManager.resetHotZones();

        return;
    }
    @Override
    protected String _getHelpString() {
        return "Use no arguments to see the current hotzone or \"random\" to change it randomly.";
    }

    @Override
    protected String _getUsageString() {
        return "'./hotzone [random]";
    }


}
