package kr.namemy.invalidteleportpackethandler;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public final class HandlerPlugin extends JavaPlugin {
    private ProtocolManager protocolManager;
    private final List<PacketAdapter> adapterList = new ArrayList<>();

    private final Map<UUID, Integer> lastPlayerTeleportIdsSendingSide = new HashMap<>();

    @Override
    public void onEnable() {
        // Plugin startup logic

        getServer().getPluginManager().registerEvents(new EventListener(), this);

        var logger = getLogger();

        protocolManager = ProtocolLibrary.getProtocolManager();

        var setPlayerPositionPacketAdapter = new PacketAdapter(this, ListenerPriority.MONITOR, PacketType.Play.Server.POSITION) {
            @Override
            public void onPacketSending(PacketEvent event) {
                var player = event.getPlayer();
                var teleportId = event.getPacket().getIntegers().read(0);
                logger.info(String.format("Sending PacketPlayOutPosition to %s (%s) [id=%d]", player.getName(), player.getUniqueId(), teleportId));

                // teleportId는 0이나 양의 정수만 가능함. 음수일 경우 다른 곳에서 이상한 id를 집어넣어서 강제로 패킷을 보내려고 시도하는 것임.
                if (teleportId < 0 || (lastPlayerTeleportIdsSendingSide.containsKey(player.getUniqueId()) && lastPlayerTeleportIdsSendingSide.get(player.getUniqueId()).equals(teleportId))) {
                    logger.warning("Detected sending packet with duplicate teleport id");
                    Thread.dumpStack();
                }

                TeleportIDCounter.setSendingSideTeleportId(player.getUniqueId(), teleportId);
            }
        };
        protocolManager.addPacketListener(setPlayerPositionPacketAdapter);
        adapterList.add(setPlayerPositionPacketAdapter);

        var teleportAcceptPacketAdapter = new PacketAdapter(this, ListenerPriority.HIGHEST, PacketType.Play.Client.TELEPORT_ACCEPT) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                var player = event.getPlayer();
                var teleportId = event.getPacket().getIntegers().read(0);
                logger.info(String.format("Received PacketPlayInTeleportAccept from %s (%s) [id=%d]", player.getName(), player.getUniqueId(), teleportId));

                if (!TeleportIDCounter.isValidTeleportConfirmation(player.getUniqueId(), teleportId)) {
                    logger.warning("Detected receiving packet with duplicate teleport id, cancelling packet to avoid player kick");
//                    Thread.dumpStack();

                    event.setCancelled(true);
                } else {
                    TeleportIDCounter.setReceivingSideTeleportId(player.getUniqueId(), teleportId);
                }
            }
        };
        protocolManager.addPacketListener(teleportAcceptPacketAdapter);
        adapterList.add(teleportAcceptPacketAdapter);

        logger.info("PacketListenerTest is enabled");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        adapterList.forEach(protocolManager::removePacketListener);
    }
}
