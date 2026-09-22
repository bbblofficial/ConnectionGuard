package org.vansama.connectionguard;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class ConnectionGuard extends JavaPlugin {

    private Connection connection;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        reloadConfig();

        this.connection = new Connection(this);
        getServer().getPluginManager().registerEvents(this.connection, this);

        ConnectionCommand cmd = new ConnectionCommand(this, this.connection);
        getCommand("connectionguard").setExecutor(cmd);
        getCommand("connectionguard").setTabCompleter(new ConnectionTabCompleter(this.connection));

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            try {
                new ConnectionGuardExpansion(this, this.connection).register();
                getLogger().info("PlaceholderAPI expansion registered.");
            } catch (Throwable t) {
                getLogger().warning("Failed to register PlaceholderAPI expansion: " + t.getMessage());
            }
        }

        printBanner();
    }

    @Override
    public void onDisable() {
        if (this.connection != null) {
            this.connection.shutdown();
        }
        getLogger().info("ConnectionGuard disabled.");
    }

    public void reload() {
        reloadConfig();
        if (this.connection != null) {
            this.connection.reloadConfig();
        }
    }

    public Connection getConnection() {
        return this.connection;
    }

    private void printBanner() {
        getLogger().info("=================================================");
        getLogger().info("  ConnectionGuard v" + getDescription().getVersion() + " - Enabled");
        getLogger().info("  Author: muvixo");
        getLogger().info("  Ping threshold: " + getConfig().getInt("ping", 150) + "ms");
        getLogger().info("  Grace period: " + getConfig().getInt("grace-seconds", 30) + "s");
        getLogger().info("=================================================");
    }
}
