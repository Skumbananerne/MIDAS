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
    private static List<ItemDisplayGroup> activeItemDisplayGroups = new ArrayList<>();
    private static PacketUpdater instance = null;

    /**
     * Creates a new PacketUpdater. This also includes initializing the loop to check for players in range of itemDisplayGroups.
     */
    private PacketUpdater() {

        Bukkit.getScheduler().runTaskTimer(MultiItemDisplayEngine.plugin, () -> {

            for (Player player : Bukkit.getOnlinePlayers()) {
                Location playerlocation = player.getLocation();
                EntityHandler entityHandler = EntityHandler.getEntityHandler(player.getUniqueId());
                for (ItemDisplayGroup itemDisplayGroup : activeItemDisplayGroups) {
                    if (playerlocation.getWorld() != itemDisplayGroup.getPivotPoint().getWorld()) continue;

                    boolean isActive = entityHandler.getActiveItemDisplayGroups().contains(itemDisplayGroup.getUuid());


                    if (playerlocation.distanceSquared(itemDisplayGroup.getPivotPoint())<itemDisplayGroup.getViewRange()){
                        if (!isActive) {
                            itemDisplayGroup.render(player);
                            entityHandler.addActiveItemDisplayGroup(itemDisplayGroup.getUuid());
                        }
                    }
                    else {
                        if (isActive) {
                            itemDisplayGroup.unrender(player);
                            entityHandler.removeActiveItemDisplayGroup(itemDisplayGroup.getUuid());
                        }
                    }
                }
            }
                }, 5, 10);

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

    public static void addActiveItemDisplayGroup(ItemDisplayGroup itemDisplayGroup) {
        activeItemDisplayGroups.add(itemDisplayGroup);
    }

    public static void removeActiveItemDisplayGroup(ItemDisplayGroup itemDisplayGroup) {
        activeItemDisplayGroups.remove(itemDisplayGroup);
    }

}
