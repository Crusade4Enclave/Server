// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com

package engine.gameManager;

import engine.Enum;
import engine.math.Bounds;
import engine.math.Vector2f;
import engine.math.Vector3f;
import engine.math.Vector3fImmutable;
import engine.objects.Building;
import engine.objects.City;
import engine.objects.Zone;
import engine.server.MBServerStatics;
import engine.server.world.WorldServer;
import org.pmw.tinylog.Logger;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/*
 * Class contains methods and structures which
 * track in-game Zones
 */
public enum ZoneManager {

    ZONEMANAGER;

    /* Instance variables */
    private static Zone seaFloor = null;
    public static Zone hotZone = null;
    public static int hotZoneCycle = 0;  // Used with HOTZONE_DURATION from config.
    private static final ConcurrentHashMap<Integer, Zone> zonesByID = new ConcurrentHashMap<>(MBServerStatics.CHM_INIT_CAP, MBServerStatics.CHM_LOAD);
    private static final ConcurrentHashMap<Integer, Zone> zonesByUUID = new ConcurrentHashMap<>(MBServerStatics.CHM_INIT_CAP, MBServerStatics.CHM_LOAD);
    private static final ConcurrentHashMap<String, Zone> zonesByName = new ConcurrentHashMap<>(MBServerStatics.CHM_INIT_CAP, MBServerStatics.CHM_LOAD);
    public static final Set<Zone> macroZones = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private static final Set<Zone> npcCityZones = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private static final Set<Zone> playerCityZones = Collections.newSetFromMap(new ConcurrentHashMap<>());

    // Find all zones coordinates fit into, starting with Sea Floor

    public static ArrayList<Zone> getAllZonesIn(final Vector3fImmutable loc) {

        ArrayList<Zone> allIn = new ArrayList<>();
        Zone zone;

        zone = ZoneManager.findSmallestZone(loc);

        if (zone != null) {
            allIn.add(zone);
            while (zone.getParent() != null) {
                zone = zone.getParent();
                allIn.add(zone);
            }
        }
        return allIn;
    }

    // Find smallest zone coordinates fit into.

    public static final Zone findSmallestZone(final Vector3fImmutable loc) {

        Zone zone = ZoneManager.seaFloor;

        if (zone == null)
            return null;

        boolean childFound = true;

        while (childFound) {

            childFound = false;

            ArrayList<Zone> nodes = zone.getNodes();

            // Logger.info("soze", "" + nodes.size());
            if (nodes != null)
                for (Zone child : nodes) {

                    if (Bounds.collide(loc, child.getBounds()) == true) {
                        zone = child;
                        childFound = true;
                        break;
                    }
                }
        }
        return zone;
    }

    public static void addZone(final int zoneID, final Zone zone) {

        ZoneManager.zonesByID.put(zoneID, zone);

        if (zone != null)
            ZoneManager.zonesByUUID.put(zone.getObjectUUID(), zone);

        ZoneManager.zonesByName.put(zone.getName().toLowerCase(), zone);

    }

    // Returns the number of available hotZones
    // remaining in this cycle (1am)

    public static int availableHotZones() {

        int count = 0;

        for (Zone zone : ZoneManager.macroZones)
            if (ZoneManager.validHotZone(zone))
                count = count + 1;

        return count;
    }

    // Resets the  availability of hotZones
    // for this cycle

    public static void resetHotZones() {

        for (Zone zone : ZoneManager.macroZones)
            if (zone.hasBeenHotzone)
                zone.hasBeenHotzone = false;

    }

    public static Zone getZoneByUUID(final int zoneUUID) {
        return ZoneManager.zonesByUUID.get(zoneUUID);
    }

    public static Zone getZoneByZoneID(final int zoneID) {

        return ZoneManager.zonesByID.get(zoneID);
    }

    public static final Collection<Zone> getAllZones() {
        return ZoneManager.zonesByUUID.values();
    }

    public static final void setHotZone(final Zone zone) {

        if (!zone.isMacroZone())
            return;

        ZoneManager.hotZone = zone;
        ZoneManager.hotZoneCycle = 1;  // Used with HOTZONE_DURATION from config.
        zone.hasBeenHotzone = true;
        WorldServer.hotZoneLastUpdate = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant();

    }

    public static boolean inHotZone(final Vector3fImmutable loc) {

        if (ZoneManager.hotZone == null)
            return false;

        return (Bounds.collide(loc, ZoneManager.hotZone.getBounds()) == true);
    }

    public static void setSeaFloor(final Zone value) {
        ZoneManager.seaFloor = value;
    }

    public static Zone getSeaFloor() {
        return ZoneManager.seaFloor;
    }

    public static final void populateWorldZones(final Zone zone) {

        int loadNum = zone.getLoadNum();

        // Zones are added to separate
        // collections for quick access
        // based upon their type.

        if (zone.isMacroZone()) {
            addMacroZone(zone);
            return;
        }


        if (zone.isPlayerCity()) {
            addPlayerCityZone(zone);
            return;
        }

        if (zone.isNPCCity())
            addNPCCityZone(zone);

    }

    private static void addMacroZone(final Zone zone) {
        ZoneManager.macroZones.add(zone);
    }

    private static void addNPCCityZone(final Zone zone) {
        zone.setNPCCity(true);
        ZoneManager.npcCityZones.add(zone);
    }

    public static final void addPlayerCityZone(final Zone zone) {
        zone.setPlayerCity(true);
        ZoneManager.playerCityZones.add(zone);
    }

    public static final void generateAndSetRandomHotzone() {

        Zone hotzone;
        ArrayList<Integer> zoneArray = new ArrayList<>();

        if (ZoneManager.macroZones.isEmpty())
            return;

        for (Zone zone : ZoneManager.macroZones) {

            if (validHotZone(zone))
                zoneArray.add(zone.getObjectUUID());

        }

        int entryIndex = ThreadLocalRandom.current().nextInt(zoneArray.size());

        hotzone = ZoneManager.getZoneByUUID(zoneArray.get(entryIndex));

        if (hotzone == null) {
            Logger.error("Hotzone is null");
            return;
        }

        ZoneManager.setHotZone(hotzone);

    }

    public static final boolean validHotZone(Zone zone) {

        if (zone.getSafeZone() == (byte) 1)
            return false; // no safe zone hotzones// if (this.hotzone == null)

        if (zone.getNodes().isEmpty())
            return false;

        if (zone.equals(ZoneManager.seaFloor))
            return false;

        //no duplicate hotZones

        if (zone.hasBeenHotzone == true)
            return false;

        // Enforce min level

        if (zone.minLvl < Integer.parseInt(ConfigManager.MB_HOTZONE_MIN_LEVEL.getValue()))
            return false;

        if (ZoneManager.hotZone != null)
            return ZoneManager.hotZone.getObjectUUID() != zone.getObjectUUID();

        return true;
    }

    /**
     * Gets a MacroZone by name.
     *
     * @param inputName MacroZone name to search for
     * @return Zone of the MacroZone, or Null
     */

    public static Zone findMacroZoneByName(String inputName) {
        synchronized (ZoneManager.macroZones) {
            for (Zone zone : ZoneManager.macroZones) {
                String zoneName = zone.getName();
                if (zoneName.equalsIgnoreCase(inputName))
                    return zone;
            }
        }
        return null;
    }

    // Converts world coordinates to coordinates local to a given zone.

    public static Vector3fImmutable worldToLocal(Vector3fImmutable worldVector,
                                                 Zone serverZone) {

        Vector3fImmutable localCoords;

        localCoords = new Vector3fImmutable(worldVector.x - serverZone.absX,
                worldVector.y - serverZone.absY, worldVector.z
                - serverZone.absZ);

        return localCoords;
    }

    public static Vector2f worldToZoneSpace(Vector3fImmutable worldVector,
                                            Zone serverZone) {

        Vector2f localCoords;
        Vector2f zoneOrigin;

        // Top left corner of zone is calculated in world space by the center and it's extents.

        zoneOrigin = new Vector2f(serverZone.getLoc().x, serverZone.getLoc().z);
        zoneOrigin = zoneOrigin.subtract(new Vector2f(serverZone.getBounds().getHalfExtents().x, serverZone.getBounds().getHalfExtents().y));

        // Local coordinate in world space translated to an offset from the calculated zone origin.

        localCoords = new Vector2f(worldVector.x, worldVector.z);
        localCoords = localCoords.subtract(zoneOrigin);

        localCoords.setY((serverZone.getBounds().getHalfExtents().y * 2) - localCoords.y);


        // TODO : Make sure this value does not go outside the zone's bounds.

        return localCoords;
    }

    // Converts local zone coordinates to world coordinates

    public static Vector3fImmutable localToWorld(Vector3fImmutable worldVector,
                                                 Zone serverZone) {

        Vector3fImmutable worldCoords;

        worldCoords = new Vector3fImmutable(worldVector.x + serverZone.absX,
                worldVector.y + serverZone.absY, worldVector.z
                + serverZone.absZ);

        return worldCoords;
    }


    /**
     * Converts from local (relative to this building) to world.
     *
     * @param localPos position in local reference (relative to this building)
     * @return position relative to world
     */

    public static Vector3fImmutable convertLocalToWorld(Building building, Vector3fImmutable localPos) {

        // convert from SB rotation value to radians


        if (building.getBounds().getQuaternion() == null)
            return building.getLoc();
        Vector3fImmutable rotatedLocal = Vector3fImmutable.rotateAroundPoint(Vector3fImmutable.ZERO, localPos, building.getBounds().getQuaternion());
        // handle building rotation
        // handle building translation

        return building.getLoc().add(rotatedLocal.x, rotatedLocal.y, rotatedLocal.z);
    }


    //used for regions, Building bounds not set yet.
    public static Vector3f convertLocalToWorld(Building building, Vector3f localPos, Bounds bounds) {

        // convert from SB rotation value to radians


        Vector3f rotatedLocal = Vector3f.rotateAroundPoint(Vector3f.ZERO, localPos, bounds.getQuaternion());
        // handle building rotation
        // handle building translation

        return new Vector3f(building.getLoc().add(rotatedLocal.x, rotatedLocal.y, rotatedLocal.z));
    }

    public static Vector3fImmutable convertWorldToLocal(Building building, Vector3fImmutable WorldPos) {
        Vector3fImmutable convertLoc = Vector3fImmutable.rotateAroundPoint(building.getLoc(), WorldPos, -building.getBounds().getQuaternion().angleY);


        convertLoc = convertLoc.subtract(building.getLoc());

        // convert from SB rotation value to radians

        return convertLoc;

    }

    public static Vector3fImmutable convertNPCLoc(Building building, Vector3fImmutable npcLoc) {

        return Vector3fImmutable.rotateAroundPoint(Vector3fImmutable.ZERO, npcLoc, -building.getBounds().getQuaternion().angleY);

    }

    // Method returns a city if the given location is within
    // a city siege radius.

    public static City getCityAtLocation(Vector3fImmutable worldLoc) {

        Zone currentZone;
        ArrayList<Zone> zoneList;
        City city;

        currentZone = ZoneManager.findSmallestZone(worldLoc);

        if (currentZone.isPlayerCity())
            return City.getCity(currentZone.getPlayerCityUUID());

        // Not currently on a city grid.  Test nearby cities
        // to see if we are on one of their seige bounds.

        zoneList = currentZone.getNodes();

        for (Zone zone : zoneList) {

            if (zone == currentZone)
                continue;

            if (zone.isPlayerCity() == false)
                continue;

            city = City.getCity(zone.getPlayerCityUUID());

            if (worldLoc.isInsideCircle(city.getLoc(), Enum.CityBoundsType.SIEGE.extents))
                return city;
        }

        return null;
    }

    /* Method is called when creating a new player city to
     * validate that the new zone does not overlap any other
     * zone that might currently exist
     */

    public static boolean validTreePlacementLoc(Zone currentZone, float positionX, float positionZ) {

        // Member Variable declaration

        ArrayList<Zone> zoneList;
        boolean validLocation = true;
        Bounds treeBounds;

        if (currentZone.isContininent() == false)
            return false;


        treeBounds = Bounds.borrow();
        treeBounds.setBounds(new Vector2f(positionX, positionZ), new Vector2f(Enum.CityBoundsType.SIEGE.extents, Enum.CityBoundsType.SIEGE.extents), 0.0f);

        zoneList = currentZone.getNodes();


        for (Zone zone : zoneList) {

            if (zone.isContininent())
                continue;

            if (Bounds.collide(treeBounds, zone.getBounds(), 0.0f))
                validLocation = false;
        }

        treeBounds.release();
        return validLocation;
    }
}
