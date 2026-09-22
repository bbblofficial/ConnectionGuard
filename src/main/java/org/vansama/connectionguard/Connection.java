package org.vansama.connectionguard;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class Connection implements Listener {

    private final ConnectionGuard plugin;

    private boolean enabled;
    private int pingThreshold;
    private int checkInterval;
    private int graceSeconds;
    private int warnCooldown;

    private String prefix;
    private String msgKick;
    private String msgBroadcast;
    private String msgWarnTitle;
    private String msgWarnSubtitle;
    private String msgStableAgain;
    private String msgNoPerm;
    private String msgReloaded;
    private String msgEnabled;
    private String msgDisabled;
    private String msgBypassAdded;
    private String msgBypassRemoved;
    private String msgForcedPingSet;
    private String msgForcedPingCleared;
    private String msgPlayerNotFound;
    private String msgPingSelf;
    private String msgPingOther;
    private String msgPingSet;
    private String msgGraceSet;
    private String msgIntervalSet;
    private String msgUsage;

    private boolean kickTitleEnabled;
    private String kickTitle;
    private String kickSubtitle;
    private boolean kickSoundEnabled;
    private String kickSoundName;

    private boolean warnTitleEnabled;
    private boolean warnChatEnabled;
    private boolean warnSoundEnabled;
    private String warnSoundName;

    private boolean countdownChatEnabled;
    private boolean countdownSoundEnabled;
    private String countdownSoundName;
    private int countdownCooldownSeconds;

    private String bypassPermission;
    private String bypassLegacyPermission;
    private Set<String> bypassNames = new HashSet<String>();

    private boolean whitelistEnabled;
    private Set<String> whitelistNames = new HashSet<String>();

    private boolean tpsGuardEnabled;
    private double tpsGuardMinTps;
    private boolean logKicks;
    private boolean broadcastKicks;
    private boolean ignoreCreative;
    private boolean debug;

    private final Map<UUID, Long> highPingSince = new HashMap<UUID, Long>();
    private final Map<UUID, Long> lastWarnTime = new HashMap<UUID, Long>();
    private final Set<UUID> bypassPlayers = new HashSet<UUID>();
    private final Map<UUID, Integer> forcedPing = new HashMap<UUID, Integer>();

    private int taskId = -1;

    public Connection(ConnectionGuard plugin) {
        this.plugin = plugin;
        loadConfiguration();
        startTask();
    }

    public void loadConfiguration() {
        FileConfiguration c = this.plugin.getConfig();

        this.enabled = c.getBoolean("enabled", true);

        this.pingThreshold = c.getInt("ping", 150);
        this.checkInterval = c.getInt("check-interval", 20);
        this.graceSeconds = c.getInt("grace-seconds", 30);
        this.warnCooldown = c.getInt("warn-cooldown", 5);

        this.prefix = c.getString("messages.prefix", "&8[&cCG&8] &r");
        this.msgKick = c.getString("messages.kicked",
                "&cUnstable connection\n&fYour ping is too high: &e%ping%ms&7/&e%max%ms");
        this.msgBroadcast = c.getString("messages.broadcast",
                "&c%player% &7was kicked for &eUnstable Connection &7(&c%ping%ms&7)");
        this.msgWarnTitle = c.getString("messages.warn-title", "&c&lUnstable Connection");
        this.msgWarnSubtitle = c.getString("messages.warn-subtitle",
                "&7Ping: &e%ping%ms &7| Kick in &e%seconds%s");
        this.msgStableAgain = c.getString("messages.stable-again", "&aYour connection is stable again.");
        this.msgNoPerm = c.getString("messages.no-permission", "&cYou do not have permission.");
        this.msgReloaded = c.getString("messages.reloaded", "&aConfiguration reloaded.");
        this.msgEnabled = c.getString("messages.enabled-msg", "&aConnection check &lENABLED&a.");
        this.msgDisabled = c.getString("messages.disabled-msg", "&cConnection check &lDISABLED&c.");
        this.msgBypassAdded = c.getString("messages.bypass-added",
                "&a%player% is now bypassing connection check.");
        this.msgBypassRemoved = c.getString("messages.bypass-removed",
                "&a%player% is no longer bypassing.");
        this.msgForcedPingSet = c.getString("messages.forced-ping-set",
                "&aForced ping for &e%player% &aset to &e%ping%ms&a.");
        this.msgForcedPingCleared = c.getString("messages.forced-ping-cleared",
                "&aForced ping removed for &e%player%&a.");
        this.msgPlayerNotFound = c.getString("messages.player-not-found",
                "&cPlayer not found: &e%player%");
        this.msgPingSelf = c.getString("messages.ping-self", "&7Your ping: &e%ping%ms");
        this.msgPingOther = c.getString("messages.ping-other", "&7%player%'s ping: &e%ping%ms");
        this.msgPingSet = c.getString("messages.ping-set", "&aPing threshold set to &e%ping%ms&a.");
        this.msgGraceSet = c.getString("messages.grace-set", "&aGrace period set to &e%seconds%s&a.");
        this.msgIntervalSet = c.getString("messages.interval-set", "&aCheck interval set to &e%ticks% ticks&a.");
        this.msgUsage = c.getString("messages.usage", "&cUsage: &e%usage%");

        this.kickTitleEnabled = c.getBoolean("kick.title-enabled", true);
        this.kickTitle = c.getString("kick.title", "&c&lUNSTABLE CONNECTION");
        this.kickSubtitle = c.getString("kick.subtitle", "&7Ping: &e%ping%ms &7/ &e%max%ms");
        this.kickSoundEnabled = c.getBoolean("kick.sound-enabled", true);
        this.kickSoundName = c.getString("kick.sound", "NOTE_BASS");

        this.warnTitleEnabled = c.getBoolean("warnings.title-enabled", true);
        this.warnChatEnabled = c.getBoolean("warnings.chat-enabled", true);
        this.warnSoundEnabled = c.getBoolean("warnings.sound-enabled", true);
        this.warnSoundName = c.getString("warnings.sound", "NOTE_BASS");

        this.countdownChatEnabled = c.getBoolean("warnings.countdown.chat-enabled", true);
        this.countdownSoundEnabled = c.getBoolean("warnings.countdown.sound-enabled", true);
        this.countdownSoundName = c.getString("warnings.countdown.sound", "CLICK");
        this.countdownCooldownSeconds = c.getInt("warnings.countdown.cooldown-seconds", 5);

        this.bypassPermission = c.getString("bypass.permission", "connectionguard.bypass");
        this.bypassLegacyPermission = c.getString("bypass.legacy-permission",
                "buildffa.connection.bypass");
        this.bypassNames = new HashSet<String>();
        List<String> bpNames = c.getStringList("bypass.players");
        if (bpNames != null) {
            for (String n : bpNames) {
                if (n != null && !n.isEmpty()) this.bypassNames.add(n.toLowerCase());
            }
        }

        this.whitelistEnabled = c.getBoolean("whitelist.enabled", false);
        this.whitelistNames = new HashSet<String>();
        List<String> wlNames = c.getStringList("whitelist.players");
        if (wlNames != null) {
            for (String n : wlNames) {
                if (n != null && !n.isEmpty()) this.whitelistNames.add(n.toLowerCase());
            }
        }

        this.tpsGuardEnabled = c.getBoolean("advanced.tps-guard.enabled", true);
        this.tpsGuardMinTps = c.getDouble("advanced.tps-guard.min-tps", 15.0D);
        this.logKicks = c.getBoolean("advanced.log-kicks", true);
        this.broadcastKicks = c.getBoolean("advanced.broadcast-kicks", true);
        this.ignoreCreative = c.getBoolean("advanced.ignore-creative", true);
        this.debug = c.getBoolean("advanced.debug", false);

        this.plugin.getLogger().info("Connection check " +
                (this.enabled ? "ENABLED at " + this.pingThreshold + "ms" : "DISABLED"));
    }

    public void reloadConfig() {
        loadConfiguration();
        restartTask();
    }

    private void startTask() {
        if (this.taskId != -1) {
            Bukkit.getScheduler().cancelTask(this.taskId);
            this.taskId = -1;
        }
        this.taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(this.plugin, new Runnable() {
            @Override
            public void run() {
                if (!enabled) return;
                tick();
            }
        }, 20L, (long) this.checkInterval);
    }

    private void restartTask() {
        startTask();
    }

    public void shutdown() {
        if (this.taskId != -1) {
            Bukkit.getScheduler().cancelTask(this.taskId);
            this.taskId = -1;
        }
        this.highPingSince.clear();
        this.lastWarnTime.clear();
        this.bypassPlayers.clear();
        this.forcedPing.clear();
    }

    private void tick() {
        if (this.tpsGuardEnabled) {
            double tps = PingUtil.getTps();
            if (tps < this.tpsGuardMinTps) {
                if (this.debug) {
                    this.plugin.getLogger().info("[DEBUG] Skipping check (TPS=" + tps + ")");
                }
                return;
            }
        }

        long now = System.currentTimeMillis();

        for (Player player : Bukkit.getOnlinePlayers()) {
            UUID id = player.getUniqueId();

            if (shouldSkip(player)) {
                this.highPingSince.remove(id);
                this.lastWarnTime.remove(id);
                continue;
            }

            int ping = getEffectivePing(player);

            if (ping >= this.pingThreshold) {
                if (!this.highPingSince.containsKey(id)) {
                    this.highPingSince.put(id, Long.valueOf(now));
                    this.lastWarnTime.put(id, Long.valueOf(now));
                    sendWarning(player, ping);
                    continue;
                }

                long since = this.highPingSince.get(id).longValue();
                long highPingMillis = now - since;

                if (highPingMillis >= (this.graceSeconds * 1000L)) {
                    kickPlayer(player, ping);
                    this.highPingSince.remove(id);
                    this.lastWarnTime.remove(id);
                    continue;
                }

                long lastWarn = this.lastWarnTime.containsKey(id)
                        ? this.lastWarnTime.get(id).longValue() : 0L;
                if (now - lastWarn >= (this.countdownCooldownSeconds * 1000L)) {
                    sendCountdown(player, ping, highPingMillis);
                    this.lastWarnTime.put(id, Long.valueOf(now));
                }
            } else {
                if (this.highPingSince.containsKey(id)) {
                    this.highPingSince.remove(id);
                    this.lastWarnTime.remove(id);
                    player.sendMessage(colorize(this.prefix + this.msgStableAgain));
                }
            }
        }
    }

    private boolean shouldSkip(Player player) {
        UUID id = player.getUniqueId();

        if (this.bypassPlayers.contains(id)) return true;

        if (this.bypassPermission != null && !this.bypassPermission.isEmpty()
                && player.hasPermission(this.bypassPermission)) return true;

        if (this.bypassLegacyPermission != null && !this.bypassLegacyPermission.isEmpty()
                && player.hasPermission(this.bypassLegacyPermission)) return true;

        if (this.bypassNames.contains(player.getName().toLowerCase())) return true;

        if (this.whitelistEnabled
                && !this.whitelistNames.contains(player.getName().toLowerCase())) {
            return true;
        }

        if (this.ignoreCreative
                && (player.getGameMode() == GameMode.CREATIVE
                    || player.getGameMode() == GameMode.SPECTATOR)) {
            return true;
        }

        return false;
    }

    private void sendWarning(Player player, int ping) {
        if (this.warnTitleEnabled) {
            sendTitle(player,
                    this.msgWarnTitle.replace("%ping%", String.valueOf(ping))
                            .replace("%seconds%", String.valueOf(this.graceSeconds)),
                    this.msgWarnSubtitle.replace("%ping%", String.valueOf(ping))
                            .replace("%seconds%", String.valueOf(this.graceSeconds)));
        }

        if (this.warnChatEnabled) {
            player.sendMessage(colorize("&8&m----------------------------------"));
            player.sendMessage(colorize(this.prefix + this.msgWarnTitle
                    .replace("%ping%", String.valueOf(ping))));
            player.sendMessage(colorize("&7Your ping is too high: &e" + ping + "ms"));
            player.sendMessage(colorize("&7You have &e" + this.graceSeconds
                    + " seconds &7to stabilize."));
            player.sendMessage(colorize("&8&m----------------------------------"));
        }

        if (this.warnSoundEnabled) {
            playSound(player, this.warnSoundName, 1.0F, 1.0F);
        }
    }

    private void sendCountdown(Player player, int ping, long highPingMillis) {
        long remaining = (this.graceSeconds * 1000L) - highPingMillis;
        int seconds = (int) Math.ceil(remaining / 1000.0);

        ChatColor color;
        if (seconds <= 3) color = ChatColor.RED;
        else if (seconds <= 10) color = ChatColor.GOLD;
        else color = ChatColor.YELLOW;

        if (this.countdownChatEnabled) {
            player.sendMessage(colorize("&c[!] " + color + "Kick in " + seconds + "s "
                    + "&7(Ping: &c" + ping + "ms&7)"));
        }

        if (this.countdownSoundEnabled && seconds <= 3) {
            playSound(player, this.countdownSoundName, 1.0F, 1.5F);
        }
    }

    private void kickPlayer(Player player, int ping) {
        if (this.kickTitleEnabled) {
            sendTitle(player,
                    this.kickTitle.replace("%ping%", String.valueOf(ping))
                            .replace("%max%", String.valueOf(this.pingThreshold)),
                    this.kickSubtitle.replace("%ping%", String.valueOf(ping))
                            .replace("%max%", String.valueOf(this.pingThreshold)));
        }

        if (this.kickSoundEnabled) {
            playSound(player, this.kickSoundName, 1.0F, 1.0F);
        }

        String reason = this.msgKick
                .replace("%ping%", String.valueOf(ping))
                .replace("%max%", String.valueOf(this.pingThreshold))
                .replace("%player%", player.getName());

        if (this.broadcastKicks) {
            String broadcast = this.msgBroadcast
                    .replace("%player%", player.getName())
                    .replace("%ping%", String.valueOf(ping))
                    .replace("%max%", String.valueOf(this.pingThreshold));
            Bukkit.broadcastMessage(colorize(broadcast));
        }

        if (this.logKicks) {
            this.plugin.getLogger().info("Kicked " + player.getName()
                    + " for unstable connection (" + ping + "ms / "
                    + this.pingThreshold + "ms)");
        }

        player.kickPlayer(colorize(reason));
    }

    public int getEffectivePing(Player player) {
        UUID id = player.getUniqueId();
        if (this.forcedPing.containsKey(id)) {
            return this.forcedPing.get(id).intValue();
        }
        return PingUtil.getPing(player);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void sendTitle(Player player, String title, String subtitle) {
        String t = colorize(title);
        String s = colorize(subtitle);

        try {
            player.getClass().getMethod("sendTitle", String.class, String.class)
                    .invoke(player, t, s);
            return;
        } catch (Throwable ignored) {}

        try {
            String packageName = Bukkit.getServer().getClass().getPackage().getName();
            String nms = packageName.substring(packageName.lastIndexOf('.') + 1);

            Class<?> craftPlayerClass = Class.forName("org.bukkit.craftbukkit." + nms + ".entity.CraftPlayer");
            Object craftPlayer = craftPlayerClass.cast(player);
            Object entityPlayer = craftPlayerClass.getMethod("getHandle").invoke(craftPlayer);
            Object playerConnection = entityPlayer.getClass().getField("playerConnection").get(entityPlayer);

            Class<?> chatComponentClass = Class.forName("net.minecraft.server." + nms + ".ChatComponentText");
            Object titleComponent = chatComponentClass.getConstructor(String.class).newInstance(t);
            Object subtitleComponent = chatComponentClass.getConstructor(String.class).newInstance(s);

            Class<?> packetTitleClass = Class.forName("net.minecraft.server." + nms + ".PacketPlayOutTitle");
            Class<?> enumTitleActionClass = Class.forName("net.minecraft.server." + nms + ".PacketPlayOutTitle$EnumTitleAction");

            Object actionTitle = Enum.valueOf((Class<Enum>) enumTitleActionClass, "TITLE");
            Object actionSubtitle = Enum.valueOf((Class<Enum>) enumTitleActionClass, "SUBTITLE");

            java.lang.reflect.Constructor<?> ctor = packetTitleClass.getConstructor(enumTitleActionClass, chatComponentClass);
            Object packetTitle = ctor.newInstance(actionTitle, titleComponent);
            Object packetSubtitle = ctor.newInstance(actionSubtitle, subtitleComponent);

            Class<?> packetClass = Class.forName("net.minecraft.server." + nms + ".Packet");
            java.lang.reflect.Method sendPacket = playerConnection.getClass().getMethod("sendPacket", packetClass);

            sendPacket.invoke(playerConnection, packetTitle);
            sendPacket.invoke(playerConnection, packetSubtitle);

            java.lang.reflect.Constructor<?> timingCtor = packetTitleClass.getConstructor(int.class, int.class, int.class);
            Object packetTiming = timingCtor.newInstance(Integer.valueOf(0), Integer.valueOf(40), Integer.valueOf(0));
            sendPacket.invoke(playerConnection, packetTiming);
        } catch (Throwable ignored) {
            if (this.warnChatEnabled) {
                player.sendMessage(t);
            }
        }
    }

    @SuppressWarnings("deprecation")
    private void playSound(Player player, String soundName, float volume, float pitch) {
        if (soundName == null || soundName.isEmpty()) return;
        try {
            Sound sound = Sound.valueOf(soundName.toUpperCase());
            player.playSound(player.getLocation(), sound, volume, pitch);
        } catch (Throwable ignored) {}
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        this.highPingSince.remove(event.getPlayer().getUniqueId());
        this.lastWarnTime.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        UUID id = event.getPlayer().getUniqueId();
        this.highPingSince.remove(id);
        this.lastWarnTime.remove(id);
    }

    // ============================================================
    //  GETTERS / SETTERS
    // ============================================================
    public boolean isEnabled() { return this.enabled; }

    public void setEnabled(boolean value) {
        this.enabled = value;
        this.plugin.getConfig().set("enabled", Boolean.valueOf(value));
        this.plugin.saveConfig();
        if (!value) {
            this.highPingSince.clear();
            this.lastWarnTime.clear();
        }
    }

    public int getPingThreshold() { return this.pingThreshold; }

    public void setPingThreshold(int value) {
        this.pingThreshold = value;
        this.plugin.getConfig().set("ping", Integer.valueOf(value));
        this.plugin.saveConfig();
    }

    public int getGraceSeconds() { return this.graceSeconds; }

    public void setGraceSeconds(int value) {
        this.graceSeconds = value;
        this.plugin.getConfig().set("grace-seconds", Integer.valueOf(value));
        this.plugin.saveConfig();
    }

    public int getCheckInterval() { return this.checkInterval; }

    public void setCheckInterval(int value) {
        this.checkInterval = value;
        this.plugin.getConfig().set("check-interval", Integer.valueOf(value));
        this.plugin.saveConfig();
        restartTask();
    }

    public String getPrefix() { return this.prefix; }
    public String getMsgNoPerm() { return this.msgNoPerm; }
    public String getMsgReloaded() { return this.msgReloaded; }
    public String getMsgEnabled() { return this.msgEnabled; }
    public String getMsgDisabled() { return this.msgDisabled; }
    public String getMsgBypassAdded() { return this.msgBypassAdded; }
    public String getMsgBypassRemoved() { return this.msgBypassRemoved; }
    public String getMsgForcedPingSet() { return this.msgForcedPingSet; }
    public String getMsgForcedPingCleared() { return this.msgForcedPingCleared; }
    public String getMsgPlayerNotFound() { return this.msgPlayerNotFound; }
    public String getMsgPingSelf() { return this.msgPingSelf; }
    public String getMsgPingOther() { return this.msgPingOther; }
    public String getMsgPingSet() { return this.msgPingSet; }
    public String getMsgGraceSet() { return this.msgGraceSet; }
    public String getMsgIntervalSet() { return this.msgIntervalSet; }
    public String getMsgUsage() { return this.msgUsage; }

    public Set<UUID> getBypassPlayers() { return this.bypassPlayers; }

    public void addBypass(UUID uuid) { this.bypassPlayers.add(uuid); }
    public void removeBypass(UUID uuid) { this.bypassPlayers.remove(uuid); }
    public boolean hasBypass(UUID uuid) { return this.bypassPlayers.contains(uuid); }

    public void setForcedPing(UUID uuid, int ping) {
        this.forcedPing.put(uuid, Integer.valueOf(ping));
    }
    public void clearForcedPing(UUID uuid) { this.forcedPing.remove(uuid); }
    public boolean hasForcedPing(UUID uuid) { return this.forcedPing.containsKey(uuid); }
    public int getForcedPing(UUID uuid) {
        if (!this.forcedPing.containsKey(uuid)) return -1;
        return this.forcedPing.get(uuid).intValue();
    }

    public String colorize(String message) {
        if (message == null) return "";
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
