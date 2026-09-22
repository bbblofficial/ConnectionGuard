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
            if (!sender.hasPermission("connectionguard.creator")) {
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
            if (!sender.hasPermission("connectionguard.admin")) {
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
            if (!sender.hasPermission("connectionguard.toggle")) { noPerm(sender); return true; }
            connection.setEnabled(true);
            sender.sendMessage(colorize(connection.getPrefix() + connection.getMsgEnabled()));
            return true;
        }
        if (sub.equals("off")) {
            if (!sender.hasPermission("connectionguard.toggle")) { noPerm(sender); return true; }
            connection.setEnabled(false);
            sender.sendMessage(colorize(connection.getPrefix() + connection.getMsgDisabled()));
            return true;
        }
        if (sub.equals("toggle")) {
            if (!sender.hasPermission("connectionguard.toggle")) { noPerm(sender); return true; }
            boolean state = !connection.isEnabled();
            connection.setEnabled(state);
            sender.sendMessage(colorize(connection.getPrefix()
                    + (state ? connection.getMsgEnabled() : connection.getMsgDisabled())));
            return true;
        }

        // ---------- RELOAD ----------
        if (sub.equals("reload")) {
            if (!sender.hasPermission("connectionguard.reload")) { noPerm(sender); return true; }
            plugin.reload();
            sender.sendMessage(colorize(connection.getPrefix() + connection.getMsgReloaded()));
            return true;
        }

        // ---------- PING ----------
        if (sub.equals("ping")) {
            if (!sender.hasPermission("connectionguard.check")) { noPerm(sender); return true; }

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
            if (!sender.hasPermission("connectionguard.set")) { noPerm(sender); return true; }
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
            if (!sender.hasPermission("connectionguard.admin")) { noPerm(sender); return true; }
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
            if (!sender.hasPermission("connectionguard.admin")) { noPerm(sender); return true; }
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
            if (!sender.hasPermission("connectionguard.admin")) { noPerm(sender); return true; }
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
            if (!sender.hasPermission("connectionguard.forceping")) { noPerm(sender); return true; }
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
            if (!sender.hasPermission("connectionguard.forceping")) { noPerm(sender); return true; }
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

    private void noPerm(CommandSender sender) {
        sender.sendMessage(colorize(connection.getPrefix() + connection.getMsgNoPerm()));
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(colorize("&8&m----------------------------------"));
        sender.sendMessage(colorize("&6&lConnectionGuard &7- Commands"));
        sender.sendMessage(colorize("&8&m----------------------------------"));

        sender.sendMessage(colorize("&e/cg help &7- Show this help"));
        sender.sendMessage(colorize("&e/cg creator &7- Show plugin credits"));
        sender.sendMessage(colorize("&e/cg ping [player] &7- Show ping"));

        if (sender.hasPermission("connectionguard.toggle")) {
            sender.sendMessage(colorize("&e/cg on|off|toggle &7- Toggle the checker"));
        }
        if (sender.hasPermission("connectionguard.admin")) {
            sender.sendMessage(colorize("&e/cg status &7- Show plugin status"));
            sender.sendMessage(colorize("&e/cg bypass <player> &7- Add bypass"));
            sender.sendMessage(colorize("&e/cg unbypass <player> &7- Remove bypass"));
            sender.sendMessage(colorize("&e/cg list &7- List bypassed players"));
        }
        if (sender.hasPermission("connectionguard.forceping")) {
            sender.sendMessage(colorize("&e/cg forceping <player> <ms> &7- Force a ping"));
            sender.sendMessage(colorize("&e/cg clearping <player> &7- Remove forced ping"));
        }
        if (sender.hasPermission("connectionguard.set")) {
            sender.sendMessage(colorize("&e/cg set ping <ms> &7- Change ping threshold"));
            sender.sendMessage(colorize("&e/cg set grace <sec> &7- Change grace period"));
            sender.sendMessage(colorize("&e/cg set interval <ticks> &7- Change check interval"));
        }
        if (sender.hasPermission("connectionguard.reload")) {
            sender.sendMessage(colorize("&e/cg reload &7- Reload configuration"));
        }

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
