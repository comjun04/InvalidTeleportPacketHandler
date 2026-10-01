package kr.namemy.invalidteleportpackethandler;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class EventListener implements Listener {
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        TeleportIDCounter.resetPlayerTeleportId(event.getPlayer().getUniqueId());
    }
}
