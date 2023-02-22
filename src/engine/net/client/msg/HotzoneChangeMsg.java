// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.net.client.msg;


import engine.gameManager.ConfigManager;
import engine.math.FastMath;
import engine.net.AbstractConnection;
import engine.net.ByteBufferReader;
import engine.net.ByteBufferWriter;
import engine.net.client.Protocol;
import engine.server.world.WorldServer;

import java.time.Duration;
import java.time.LocalDateTime;


public class HotzoneChangeMsg extends ClientNetMsg {

    private int zoneType;
    private int zoneID;
    private Duration endOfCycle;

    /**
     * This is the general purpose constructor.
     */
    public HotzoneChangeMsg(int zoneType, int zoneID) {
        super(Protocol.ARCHOTZONECHANGE);
        this.zoneType = zoneType;
        this.zoneID = zoneID;

        int hotZoneDuration = Integer.parseInt(ConfigManager.MB_HOTZONE_DURATION.getValue());

        endOfCycle = Duration.between(WorldServer.hotZoneLastUpdate, WorldServer.hotZoneLastUpdate.plusSeconds(hotZoneDuration * 3600));

    }

    /**
     * This constructor is used by NetMsgFactory. It attempts to deserialize the
     * ByteBuffer into a message. If a BufferUnderflow occurs (based on reading
     * past the limit) then this constructor Throws that Exception to the
     * caller.
     */
    public HotzoneChangeMsg(AbstractConnection origin, ByteBufferReader reader)
             {
        super(Protocol.ARCHOTZONECHANGE, origin, reader);
    }

    /**
     * Serializes the subclass specific items to the supplied NetMsgWriter.
     */
    @Override
    protected void _serialize(ByteBufferWriter writer) {
        writer.putInt(this.zoneType);
        writer.putInt(this.zoneID);
        writer.putInt((int) endOfCycle.getSeconds());
    }

    /**
     * Deserializes the subclass specific items from the supplied NetMsgReader.
     */
    @Override
    protected void _deserialize(ByteBufferReader reader)  {
        this.zoneType = reader.getInt();
        this.zoneID = reader.getInt();
        reader.getInt();
    }

    public int getZoneType() {
        return this.zoneType;
    }

    public int getZoneID() {
        return this.zoneID;
    }

    public void setZoneType(int value) {
        this.zoneType = value;
    }

    public void setZoneID(int value) {
        this.zoneID = value;
    }
}
