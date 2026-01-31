package org.dopelegend.multiItemDisplayEngine.utils.classes;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import org.bukkit.Bukkit;
import org.bukkit.UnsafeValues;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * A util class for making sure each player has unique EID this should be used to get all EntityIDs for packets.
 */
public class EntityHandler {
    /**
     * A map of all entityHandlers indexed by the players UUID
     */
    private static Map<UUID, EntityHandler> entityHandlers = new HashMap<>();
    /**
     * All currently used EIDs
     */
    private IntSet activeEntities;
    /**
     * The currentID, this is decremented on each use.
     */
    private int currentID;

    /**
     *
     * Creates a new EntityHandler for this player. You should always try getting the players EntityHandler before making a new one.
     *
     * @param uuid The uuid of the player this is linked to.
     */
    private EntityHandler(UUID uuid) {
        currentID = Integer.MAX_VALUE;
        activeEntities = new IntOpenHashSet();
        entityHandlers.put(uuid, this);
    }

    /**
     *
     * Gets the next entityID, this should be used any time you want to create a new packet.
     *
     * @return The entityID
     */
    public int getID() {
        currentID--;
        activeEntities.add(currentID);
        return currentID;
    }

    /**
     * Removes all entities. This should only be used when removing the handler.
     */
    public void removeAllEntities() {
        activeEntities.clear();
    }

    /**
     * Removes a EntityHandler.
     *
     * @param uuid The uuid of the player this entityHandler is linked to.
     */
    public static void remove(UUID uuid) {
        entityHandlers.remove(uuid);
    }

    /**
     *
     * Removes an Entity.
     *
     * @param entityID The id of the entity to remove.
     */
    public void removeEntity(int entityID) {
        activeEntities.remove(entityID);
        CraftWorld world = (CraftWorld) Bukkit.getWorlds().get(0);
        world.getEntities()
    }

    /**
     *
     * Gets all active entities for this EntityHandler.
     *
     * @return An int array with all the active entities.
     */
    public int[] getActiveEntities() {
        return activeEntities.toIntArray();
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
