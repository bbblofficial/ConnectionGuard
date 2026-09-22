package org.vansama.connectionguard;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

public class ConnectionGuardExpansion extends PlaceholderExpansion {

    private final ConnectionGuard plugin;
    private final Connection connection;

    public ConnectionGuardExpansion(ConnectionGuard plugin, Connection connection) {
        this.plugin = plugin;
        this.connection = connection;
    }

    @Override public String getIdentifier() { return "connectionguard"; }
    @Override public String getAuthor()     { return "muvixo"; }
    @Override public String getVersion()    { return plugin.getDescription().getVersion(); }
    @Override public boolean persist()      { return true; }
    @Override public boolean canRegister()  { return true; }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        if (identifier == null) return "";
        String id = identifier.toLowerCase();

        if (id.equals("enabled"))     return connection.isEnabled() ? "true" : "false";
        if (id.equals("threshold"))   return String.valueOf(connection.getPingThreshold());
        if (id.equals("grace"))       return String.valueOf(connection.getGraceSeconds());
        if (id.equals("interval"))    return String.valueOf(connection.getCheckInterval());
        if (id.equals("bypassed"))    return String.valueOf(connection.getBypassPlayers().size());

        if (player != null) {
            if (id.equals("ping"))     return String.valueOf(connection.getEffectivePing(player));
            if (id.equals("realping")) return String.valueOf(PingUtil.getPing(player));
            if (id.equals("bypass"))   return connection.hasBypass(player.getUniqueId()) ? "true" : "false";
            if (id.equals("forced"))   return connection.hasForcedPing(player.getUniqueId())
                    ? String.valueOf(connection.getForcedPing(player.getUniqueId())) : "0";
        }

        return null;
    }
}
