package org.vansama.connectionguard;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ConnectionCommand implements CommandExecutor {

    private final ConnectionGuard plugin;
    private final Connection connection;

    public ConnectionCommand(ConnectionGuard plugin, Connection connection) {
        this.plugin = plugin;
        this.connection = connection;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String sub = args[0].toLowerCase();

        // ---------- HELP ----------
        if (sub.equals("help") || sub.equals("?")) {
            sendHelp(sender);
            return true;
        }

        // ---------- CREATOR ----------
        if (sub.equals("creator") || sub.equals("author")) {
            if (!sender.hasPermission(getPerm("creator", "connectionguard.creator"))) {
                noPerm(sender); return true;
            }
            sender.sendMessage(colorize("&8&m----------------------------------"));
            sender.sendMessage(colorize("&6&lConnectionGuard"));
            sender.sendMessage(colorize("&7Plugin: &fUnstable Connection / Ping Guard"));
            sender.sendMessage(colorize("&7Author: &bmuvixo"));
            sender.sendMessage(colorize("&7Version: &f" + plugin.getDescription().getVersion()));
            sender.sendMessage(colorize("&7API: &f1.8 - 1.16"));
            sender.sendMessage(colorize("&8&m----------------------------------"));
            return true;
        }

        // ---------- STATUS ----------
        if (sub.equals("status") || sub.equals("info")) {
            if (!sender.hasPermission(getPerm("admin", "connectionguard.admin"))) {
                noPerm(sender); return true;
            }
            sender.sendMessage(colorize("&8&m----------------------------------"));
            sender.sendMessage(colorize("&6&lConnectionGuard Status"));
            sender.sendMessage(colorize("&7Enabled: " + (connection.isEnabled() ? "&aYES" : "&cNO")));
            sender.sendMessage(colorize("&7Ping threshold: &e" + connection.getPingThreshold() + "ms"));
            sender.sendMessage(colorize("&7Grace period: &e" + connection.getGraceSeconds() + "s"));
            sender.sendMessage(colorize("&7Check interval: &e" + connection.getCheckInterval() + " ticks"));
            sender.sendMessage(colorize("&7Bypassed: &e" + connection.getBypassPlayers().size()));
            sender.sendMessage(colorize("&8&m----------------------------------"));
            return true;
        }

        // ---------- ON / OFF / TOGGLE ----------
        if (sub.equals("on")) {
            if (!sender.hasPermission(getPerm("toggle", "connectionguard.toggle"))) { noPerm(sender); return true; }
            connection.setEnabled(true);
            sender.sendMessage(colorize(connection.getPrefix() + connection.getMsgEnabled()));
            return true;
        }
        if (sub.equals("off")) {
            if (!sender.hasPermission(getPerm("toggle", "connectionguard.toggle"))) { noPerm(sender); return true; }
            connection.setEnabled(false);
            sender.sendMessage(colorize(connection.getPrefix() + connection.getMsgDisabled()));
            return true;
        }
        if (sub.equals("toggle")) {
            if (!sender.hasPermission(getPerm("toggle", "connectionguard.toggle"))) { noPerm(sender); return true; }
            boolean state = !connection.isEnabled();
            connection.setEnabled(state);
            sender.sendMessage(colorize(connection.getPrefix()
                    + (state ? connection.getMsgEnabled() : connection.getMsgDisabled())));
            return true;
        }

        // ---------- RELOAD ----------
        if (sub.equals("reload")) {
            if (!sender.hasPermission(getPerm("reload", "connectionguard.reload"))) { noPerm(sender); return true; }
            plugin.reload();
            sender.sendMessage(colorize(connection.getPrefix() + connection.getMsgReloaded()));
            return true;
        }

        // ---------- PING ----------
        if (sub.equals("ping")) {
            if (!sender.hasPermission(getPerm("check", "connectionguard.check"))
                    && !sender.hasPermission(getPerm("use", "connectionguard.use"))) {
                noPerm(sender); return true;
            }

            if (args.length < 2) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage(colorize("&cConsole must specify a player: /cg ping <player>"));
                    return true;
                }
                Player p = (Player) sender;
                int ping = connection.getEffectivePing(p);
                sender.sendMessage(colorize(connection.getPrefix()
                        + connection.getMsgPingSelf().replace("%ping%", String.valueOf(ping))));
                return true;
            }

            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage(colorize(connection.getPrefix()
                        + connection.getMsgPlayerNotFound().replace("%player%", args[1])));
                return true;
            }
            int ping = connection.getEffectivePing(target);
            sender.sendMessage(colorize(connection.getPrefix()
                    + connection.getMsgPingOther()
                        .replace("%player%", target.getName())
                        .replace("%ping%", String.valueOf(ping))));
            return true;
        }

        // ---------- SET ----------
        if (sub.equals("set")) {
            if (!sender.hasPermission(getPerm("set", "connectionguard.set"))) { noPerm(sender); return true; }
            if (args.length < 3) {
                sender.sendMessage(colorize(connection.getPrefix()
                        + connection.getMsgUsage().replace("%usage%", "/cg set <ping|grace|interval> <value>")));
                return true;
            }
            String key = args[1].toLowerCase();
            int value;
            try {
                value = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                sender.sendMessage(colorize("&cInvalid number: &e" + args[2]));
                return true;
            }

            if (key.equals("ping")) {
                connection.setPingThreshold(value);
                sender.sendMessage(colorize(connection.getPrefix()
                        + connection.getMsgPingSet().replace("%ping%", String.valueOf(value))));
            } else if (key.equals("grace") || key.equals("grace-seconds")) {
                connection.setGraceSeconds(value);
                sender.sendMessage(colorize(connection.getPrefix()
                        + connection.getMsgGraceSet().replace("%seconds%", String.valueOf(value))));
            } else if (key.equals("interval") || key.equals("check-interval")) {
                connection.setCheckInterval(value);
                sender.sendMessage(colorize(connection.getPrefix()
                        + connection.getMsgIntervalSet().replace("%ticks%", String.valueOf(value))));
            } else {
                sender.sendMessage(colorize("&cUnknown key: &e" + key
                        + " &7(use: ping, grace, interval)"));
            }
            return true;
        }

        // ---------- BYPASS ----------
        if (sub.equals("bypass")) {
            if (!sender.hasPermission(getPerm("admin", "connectionguard.admin"))) { noPerm(sender); return true; }
            if (args.length < 2) {
                sender.sendMessage(colorize(connection.getPrefix()
                        + connection.getMsgUsage().replace("%usage%", "/cg bypass <player>")));
                return true;
            }
            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage(colorize(connection.getPrefix()
                        + connection.getMsgPlayerNotFound().replace("%player%", args[1])));
                return true;
            }
            connection.addBypass(target.getUniqueId());
            sender.sendMessage(colorize(connection.getPrefix()
                    + connection.getMsgBypassAdded().replace("%player%", target.getName())));
            return true;
        }

        if (sub.equals("unbypass")) {
            if (!sender.hasPermission(getPerm("admin", "connectionguard.admin"))) { noPerm(sender); return true; }
            if (args.length < 2) {
                sender.sendMessage(colorize(connection.getPrefix()
                        + connection.getMsgUsage().replace("%usage%", "/cg unbypass <player>")));
                return true;
            }
            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage(colorize(connection.getPrefix()
                        + connection.getMsgPlayerNotFound().replace("%player%", args[1])));
                return true;
            }
            connection.removeBypass(target.getUniqueId());
            sender.sendMessage(colorize(connection.getPrefix()
                    + connection.getMsgBypassRemoved().replace("%player%", target.getName())));
            return true;
        }

        // ---------- LIST ----------
        if (sub.equals("list")) {
            if (!sender.hasPermission(getPerm("admin", "connectionguard.admin"))) { noPerm(sender); return true; }
            sender.sendMessage(colorize("&8&m----------------------------------"));
            sender.sendMessage(colorize("&6&lBypassed Players"));
            if (connection.getBypassPlayers().isEmpty()) {
                sender.sendMessage(colorize("&7None."));
            } else {
                for (UUID id : connection.getBypassPlayers()) {
                    Player p = Bukkit.getPlayer(id);
                    String name = (p != null) ? p.getName() : id.toString();
                    sender.sendMessage(colorize("&7- &e" + name));
                }
            }
            sender.sendMessage(colorize("&8&m----------------------------------"));
            return true;
        }

        // ---------- FORCEPING ----------
        if (sub.equals("forceping")) {
            if (!sender.hasPermission(getPerm("forceping", "connectionguard.forceping"))) { noPerm(sender); return true; }
            if (args.length < 3) {
                sender.sendMessage(colorize(connection.getPrefix()
                        + connection.getMsgUsage().replace("%usage%", "/cg forceping <player> <ms>")));
                return true;
            }
            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage(colorize(connection.getPrefix()
                        + connection.getMsgPlayerNotFound().replace("%player%", args[1])));
                return true;
            }
            int pingAmount;
            try {
                pingAmount = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                sender.sendMessage(colorize("&cInvalid ping: &e" + args[2]));
                return true;
            }
            if (pingAmount < 0) {
                sender.sendMessage(colorize("&cPing cannot be negative."));
                return true;
            }
            int realPing = PingUtil.getPing(target);
            if (pingAmount <= realPing) {
                sender.sendMessage(colorize("&cForced ping must be higher than current (&e"
                        + realPing + "ms&c)."));
                return true;
            }
            connection.setForcedPing(target.getUniqueId(), pingAmount);
            sender.sendMessage(colorize(connection.getPrefix()
                    + connection.getMsgForcedPingSet()
                        .replace("%player%", target.getName())
                        .replace("%ping%", String.valueOf(pingAmount))));
            return true;
        }

        if (sub.equals("clearping")) {
            if (!sender.hasPermission(getPerm("forceping", "connectionguard.forceping"))) { noPerm(sender); return true; }
            if (args.length < 2) {
                sender.sendMessage(colorize(connection.getPrefix()
                        + connection.getMsgUsage().replace("%usage%", "/cg clearping <player>")));
                return true;
            }
            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage(colorize(connection.getPrefix()
                        + connection.getMsgPlayerNotFound().replace("%player%", args[1])));
                return true;
            }
            connection.clearForcedPing(target.getUniqueId());
            connection.removeBypass(target.getUniqueId());
            sender.sendMessage(colorize(connection.getPrefix()
                    + connection.getMsgForcedPingCleared().replace("%player%", target.getName())));
            return true;
        }

        // ---------- UNKNOWN ----------
        sender.sendMessage(colorize("&cUnknown subcommand. Use &e/cg help"));
        return true;
    }

    // ============================================================
    //  PERMISSION HELPER
    //  Reads "permissions.<action>" from config.yml; falls back
    //  to the provided default if not set or empty.
    // ============================================================
    private String getPerm(String action, String defaultPerm) {
        String path = "permissions." + action;
        String value = plugin.getConfig().getString(path);
        if (value == null || value.trim().isEmpty()) {
            return defaultPerm;
        }
        return value.trim();
    }

    private void noPerm(CommandSender sender) {
        sender.sendMessage(colorize(connection.getPrefix() + connection.getMsgNoPerm()));
    }

    // ============================================================
    //  HELP MENU
    //  - General commands: always shown (permission-checked
    //    individually where applicable).
    //  - Admin section: only shown if the sender has at least
    //    one admin permission; each command is then checked
    //    individually.
    // ============================================================
    private void sendHelp(CommandSender sender) {

        // Load all permission nodes from config.yml
        String permUse       = getPerm("use",       "connectionguard.use");
        String permCreator   = getPerm("creator",   "connectionguard.creator");
        String permCheck     = getPerm("check",     "connectionguard.check");
        String permAdmin     = getPerm("admin",     "connectionguard.admin");
        String permToggle    = getPerm("toggle",    "connectionguard.toggle");
        String permReload    = getPerm("reload",    "connectionguard.reload");
        String permSet       = getPerm("set",       "connectionguard.set");
        String permForcePing = getPerm("forceping", "connectionguard.forceping");

        // isAdmin = OR of all admin-level permissions
        boolean isAdmin =
                sender.hasPermission(permAdmin)
             || sender.hasPermission(permToggle)
             || sender.hasPermission(permReload)
             || sender.hasPermission(permSet)
             || sender.hasPermission(permForcePing);

        // ---------------- HEADER ----------------
        sender.sendMessage(colorize("&8&m----------------------------------"));
        sender.sendMessage(colorize("&6&lConnectionGuard &7- Commands"));
        sender.sendMessage(colorize("&8&m----------------------------------"));

        // ---------------- GENERAL COMMANDS ----------------
        sender.sendMessage(colorize("&e&lGeneral Commands"));

        // /cg help — always visible (this is the help command itself)
        sender.sendMessage(colorize("  &6/cg help &8- &7Show this help"));

        // /cg creator — permission-checked
        if (sender.hasPermission(permCreator)) {
            sender.sendMessage(colorize("  &6/cg creator &8- &7Show plugin credits"));
        }

        // /cg ping — permission-checked (either "check" or "use" grants access)
        if (sender.hasPermission(permCheck) || sender.hasPermission(permUse)) {
            sender.sendMessage(colorize("  &6/cg ping [player] &8- &7Show ping"));
        }

        // ---------------- ADMIN COMMANDS ----------------
        if (isAdmin) {
            sender.sendMessage(colorize("&8&m----------------------------------"));
            sender.sendMessage(colorize("&c&lAdmin Commands"));

            // toggle group
            if (sender.hasPermission(permToggle)) {
                sender.sendMessage(colorize("  &6/cg on|off|toggle &8- &7Toggle the checker"));
            }

            // admin group (status, bypass, unbypass, list)
            if (sender.hasPermission(permAdmin)) {
                sender.sendMessage(colorize("  &6/cg status &8- &7Show plugin status"));
                sender.sendMessage(colorize("  &6/cg bypass <player> &8- &7Add bypass"));
                sender.sendMessage(colorize("  &6/cg unbypass <player> &8- &7Remove bypass"));
                sender.sendMessage(colorize("  &6/cg list &8- &7List bypassed players"));
            }

            // forceping group
            if (sender.hasPermission(permForcePing)) {
                sender.sendMessage(colorize("  &6/cg forceping <player> <ms> &8- &7Force a ping"));
                sender.sendMessage(colorize("  &6/cg clearping <player> &8- &7Remove forced ping"));
            }

            // set group
            if (sender.hasPermission(permSet)) {
                sender.sendMessage(colorize("  &6/cg set ping <ms> &8- &7Change ping threshold"));
                sender.sendMessage(colorize("  &6/cg set grace <sec> &8- &7Change grace period"));
                sender.sendMessage(colorize("  &6/cg set interval <ticks> &8- &7Change check interval"));
            }

            // reload
            if (sender.hasPermission(permReload)) {
                sender.sendMessage(colorize("  &6/cg reload &8- &7Reload configuration"));
            }
        }

        // ---------------- FOOTER ----------------
        sender.sendMessage(colorize("&8&m----------------------------------"));
        sender.sendMessage(colorize("&7Use &e/cg help <command> &7for more info."));
        sender.sendMessage(colorize("&8&m----------------------------------"));
    }

    private String colorize(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    public List<String> tabComplete(String[] args) {
        List<String> out = new ArrayList<String>();
        if (args.length == 1) {
            String[] subs = {"help","creator","ping","on","off","toggle","status",
                    "bypass","unbypass","list","forceping","clearping","set","reload"};
            String prefix = args[0].toLowerCase();
            for (String s : subs) {
                if (s.startsWith(prefix)) out.add(s);
            }
        } else if (args.length == 2) {
            String sub = args[0].toLowerCase();
            if (sub.equals("ping") || sub.equals("bypass") || sub.equals("unbypass")
                    || sub.equals("forceping") || sub.equals("clearping")) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (p.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                        out.add(p.getName());
                    }
                }
            } else if (sub.equals("set")) {
                out.add("ping");
                out.add("grace");
                out.add("interval");
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("set")) {
                out.add("<value>");
            }
        }
        return out;
    }
}