package kr.namemy.invalidteleportpackethandler;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TeleportIDCounter {
    private static final Map<UUID, Integer> sendingSide = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> receivingSide = new ConcurrentHashMap<>();

    public static boolean isValidTeleportConfirmation(UUID playerId, int teleportId) {
        if (sendingSide.containsKey(playerId) && receivingSide.containsKey(playerId)) {
            var sendingSideTeleportId = sendingSide.get(playerId);
            var receivingSideTeleportId = receivingSide.get(playerId);
            if (Objects.equals(sendingSideTeleportId, receivingSideTeleportId) && teleportId == receivingSideTeleportId) {
                // duplicate teleport id request
                return false;
            }
        }

        return true;
    }

    public static void setSendingSideTeleportId(UUID playerId, int teleportId) {
        sendingSide.put(playerId, teleportId);
    }

    public static void setReceivingSideTeleportId(UUID playerId, int teleportId) {
        // 예상된 teleportId인지 확인 (서버에서 실제로 보낸 id가 맞는지)
        if (sendingSide.containsKey(playerId) && sendingSide.get(playerId) == teleportId) {
            receivingSide.put(playerId, teleportId);
        }
    }

    public static void resetPlayerTeleportId(UUID playerId) {
        sendingSide.remove(playerId);
        receivingSide.remove(playerId);
    }
}
