package engine.net.client.handlers;

import engine.Enum;
import engine.Enum.DispatchChannel;
import engine.exception.MsgSendException;
import engine.gameManager.SessionManager;
import engine.gameManager.ZoneManager;
import engine.net.Dispatch;
import engine.net.DispatchMessage;
import engine.net.client.ClientConnection;
import engine.net.client.msg.*;
import engine.objects.City;
import engine.objects.PlayerCharacter;
import engine.session.Session;

/*
 * @Author:
 * @Summary: Processes application protocol message which displays
 * the map interface.  (Zones, Cities, Realms, Hot-zones)
 */

public class CityDataHandler extends AbstractClientMsgHandler {

    public CityDataHandler() {
        super(KeepAliveServerClientMsg.class);
    }

    @Override
    protected boolean _handleNetMsg(ClientNetMsg baseMsg, ClientConnection origin) throws MsgSendException {

        boolean updateMine = false;
        boolean updateCity = false;
        Session playerSession;
        PlayerCharacter playerCharacter;
        Dispatch dispatch;

        playerCharacter = origin.getPlayerCharacter();

        if (playerCharacter == null)
            return true;

        // Session is needed as param for worldObjectMsg.

        playerSession = SessionManager.getSession(playerCharacter);

        if (playerSession == null)
            return true;

        // No reason to serialize cities everytime map is
        // opened.  Wait until something has changed.
        // This does not work for mines.

        if (playerCharacter.getTimeStamp("cityUpdate") <= City.lastCityUpdate) {
            playerCharacter.setTimeStamp("cityUpdate", System.currentTimeMillis());
            updateCity = true;
        }

        cityDataMsg cityDataMsg = new cityDataMsg(SessionManager.getSession(playerCharacter), false);
        cityDataMsg.updateMines(true);
        cityDataMsg.updateCities(updateCity);

        dispatch = Dispatch.borrow(playerCharacter, cityDataMsg);
        DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);

        // If the hotZone has changed then update the client's map accordingly.

        if (playerCharacter.getTimeStamp("hotzoneupdate") <= ZoneManager.hotZoneLastUpdate.toEpochMilli() && ZoneManager.hotZone != null) {
                HotzoneChangeMsg hotzoneChangeMsg = new HotzoneChangeMsg(Enum.GameObjectType.Zone.ordinal(), ZoneManager.hotZone.getObjectUUID());
                dispatch = Dispatch.borrow(playerCharacter, hotzoneChangeMsg);
                DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);
                playerCharacter.setTimeStamp("hotzoneupdate", System.currentTimeMillis() - 100);
        }

        // Serialize the realms for this map

        WorldRealmMsg worldRealmMsg = new WorldRealmMsg();
        dispatch = Dispatch.borrow(playerCharacter, worldRealmMsg);
        DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);

        return true;
    }

}