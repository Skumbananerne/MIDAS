package org.dopelegend.multiItemDisplayEngine.utils.classes;

import org.bukkit.Bukkit;

import java.util.*;

/**
 * A util class for making sure each player has unique EID this should be used to get all EntityIDs for packets.
 */
public class EntityHandler {
    /**
     * A map of all entityHandlers indexed by the players UUID
     */
    private static Map<UUID, EntityHandler> entityHandlers = new HashMap<>();

    private List<UUID> activeItemDisplayGroups = new ArrayList<>();
    /**
     *
     * Creates a new EntityHandler for this player. You should always try getting the players EntityHandler before making a new one.
     *
     * @param uuid The uuid of the player this is linked to.
     */
    private EntityHandler(UUID uuid) {
        entityHandlers.put(uuid, this);
    }

    /**
     *
     * Gets the next entityID, this should be used any time you want to create a new packet.
     *
     * @return The entityID
     */
    public int getID() {
        return Bukkit.getUnsafe().nextEntityId();
    }

    /**
     * Removes a EntityHandler.
     *
     * @param uuid The uuid of the player this entityHandler is linked to.
     */
    public static void remove(UUID uuid) {
        entityHandlers.remove(uuid);
    }


    public List<UUID> getActiveItemDisplayGroups() {
        return activeItemDisplayGroups;
    }

    public void setActiveItemDisplayGroups(List<UUID> activeItemDisplayGroups) {
        this.activeItemDisplayGroups = activeItemDisplayGroups;
    }

    public void addActiveItemDisplayGroup(UUID uuid) {
        activeItemDisplayGroups.add(uuid);
    }

    public void removeActiveItemDisplayGroup(UUID uuid) {
        activeItemDisplayGroups.remove(uuid);
    }

    /**
     *
     * Gets or creates an EntityHandler a player's uuid
     *
     * @param uuid The UUID of the player.
     * @return The EntityHandler
     */
    public static EntityHandler getEntityHandler(UUID uuid) {
        if (entityHandlers.containsKey(uuid)) {
            return entityHandlers.get(uuid);
        }
        return new EntityHandler(uuid);
    }
}
