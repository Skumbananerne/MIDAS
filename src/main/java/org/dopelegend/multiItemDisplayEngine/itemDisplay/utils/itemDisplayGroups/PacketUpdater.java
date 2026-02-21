package org.dopelegend.multiItemDisplayEngine.itemDisplay.utils.itemDisplayGroups;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.dopelegend.multiItemDisplayEngine.MultiItemDisplayEngine;
import org.dopelegend.multiItemDisplayEngine.utils.classes.EntityHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PacketUpdater {
    private static final List<ItemDisplayGroup> activeItemDisplayGroups = new ArrayList<>();
    private static PacketUpdater instance = null;

    /**
     * Creates a new PacketUpdater. This also includes initializing the loop to check for players in range of itemDisplayGroups.
     */
    private PacketUpdater() {
        Bukkit.getScheduler().runTaskTimer(MultiItemDisplayEngine.plugin, () -> {
            for (ItemDisplayGroup itemDisplayGroup : activeItemDisplayGroups) {
                renderItemDisplayGroup(itemDisplayGroup);
            }

        }, 5, 5);

    }

    /**
     *
     * Renders an ItemDisplayGroup for all players that meet the requirements (in range, in world, not already rendering, etc.)
     *
     * @param itemDisplayGroup The ItemDisplayGroup to render.
     */
    private static void renderItemDisplayGroup(ItemDisplayGroup itemDisplayGroup){
        for (Player player : Bukkit.getOnlinePlayers()) {
            Location playerlocation = player.getLocation();
            EntityHandler entityHandler = EntityHandler.getEntityHandler(player.getUniqueId());
            if (playerlocation.getWorld() != itemDisplayGroup.getPivotPoint().getWorld()) continue;

            boolean isRendered = entityHandler.getActiveItemDisplayGroups().contains(itemDisplayGroup.getUuid());


            if (playerlocation.distanceSquared(itemDisplayGroup.getPivotPoint()) < itemDisplayGroup.getViewRangeSquared()) {
                if (!isRendered) {
                    itemDisplayGroup.render(player);
                    entityHandler.addActiveItemDisplayGroup(itemDisplayGroup.getUuid());
                }
            } else {
                if (isRendered) {
                    itemDisplayGroup.unrender(player);
                    entityHandler.removeActiveItemDisplayGroup(itemDisplayGroup.getUuid());
                }
            }
        }
    }

    /**
     *
     * Gets the packetUpdater instance.
     *
     */
    public static PacketUpdater getInstance() {
        if (instance == null) {
            instance = new PacketUpdater();
        }
        return  instance;
    }

    /**
     *
     * Adds a new active ItemDisplayGroup to the PacketUpdater, meaning that it will render / unrender for players when needed.
     * This function also immediately renders the ItemDisplayGroup for players in range
     *
     * @param itemDisplayGroup The ItemDisplayGroup to add
     */
    public static void addActiveItemDisplayGroup(ItemDisplayGroup itemDisplayGroup) {
        renderItemDisplayGroup(itemDisplayGroup);
        activeItemDisplayGroups.add(itemDisplayGroup);
    }

    public static void removeActiveItemDisplayGroup(ItemDisplayGroup itemDisplayGroup) {
        activeItemDisplayGroups.remove(itemDisplayGroup);
    }

}
