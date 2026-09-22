package org.vansama.connectionguard;

import org.bukkit.entity.Player;

public final class PingUtil {

    private PingUtil() {}

    public static int getPing(Player player) {
        if (player == null) return 0;
        try {
            Object craftPlayer = player.getClass().getMethod("getHandle").invoke(player);
            return ((Integer) craftPlayer.getClass().getField("ping").get(craftPlayer)).intValue();
        } catch (Throwable t) {
            return 0;
        }
    }

    public static double getTps() {
        try {
            Object server = org.bukkit.Bukkit.getServer().getClass().getMethod("getServer").invoke(null);
            java.lang.reflect.Field recentTpsField = server.getClass().getField("recentTps");
            double[] recentTps = (double[]) recentTpsField.get(server);
            return recentTps[0];
        } catch (Throwable t) {
            return 20.0D;
        }
    }
}
