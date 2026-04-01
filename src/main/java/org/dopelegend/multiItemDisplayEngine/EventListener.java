package org.dopelegend.multiItemDisplayEngine;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;

public class EventListener implements Listener {
    @EventHandler
    public void OnPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        MultiItemDisplayEngine.packWebServer.givePlayerPack(player);
    }

    @EventHandler
    public void onResourcePackStatus(PlayerResourcePackStatusEvent event) {
        event.getPlayer().sendMessage("§ePack status: §f" + event.getStatus().name());
        MultiItemDisplayEngine.plugin.getLogger().warning(
                event.getPlayer().getName() + " -> " + event.getStatus().name()
        );
    }
}
